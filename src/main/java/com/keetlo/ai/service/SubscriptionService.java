package com.keetlo.ai.service;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Lazy;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.keetlo.ai.dto.GetSubscriptionPlansByTokenResult;
import com.keetlo.ai.model.SubscriptionPlan;
import com.keetlo.ai.model.UserSubscriptionPlan;
import com.keetlo.ai.util.JwtUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SubscriptionService {
    private final JdbcTemplate database;
    private final JwtUtil jwtUtil;
    private final AuthService authService;

    public SubscriptionService(JdbcTemplate database, JwtUtil jwtUtil, @Lazy AuthService authService){
        this.database = database;
        this.jwtUtil = jwtUtil;
        this.authService = authService;
    }

   public SubscriptionPlan getPlanNameAndPrice(String subscriptionPlanId) {
    String sql = "SELECT name, price FROM subscription_plans WHERE subscription_plan_id = ?";

    try {
        return database.queryForObject(sql, new Object[]{subscriptionPlanId}, (resultRow, _) -> {
            SubscriptionPlan subscriptionPlan = new SubscriptionPlan();
            subscriptionPlan.setName(resultRow.getString("name"));
            subscriptionPlan.setPrice(resultRow.getDouble("price"));
            return subscriptionPlan;
        });
    } catch (EmptyResultDataAccessException e) {
        return null;
    }
    }

    public GetSubscriptionPlansByTokenResult getSubscribtionPlansByToken(String token){
        GetSubscriptionPlansByTokenResult result = new GetSubscriptionPlansByTokenResult();
        String sql = """
                SELECT subscription_plans.subscription_plan_id, subscription_plans.name, subscription_plans.description, subscription_plans.price, 
                subscription_plans.requests_per_day, subscription_plans.advanced_features, subscription_plans.priority_support, subscription_plans.community_support,
                subscription_plans.package_type, subscription_plans.most_popular, subscription_plans.is_visible FROM subscription_plans 
                WHERE subscription_plans.is_visible = 1
                ORDER BY created_at ASC
                """;
        String checkUserActivePackageSql = """
                SELECT COUNT(*) FROM user_subscription_plans WHERE user_id = ? AND subscription_plan_id  = ?
                """;
        try{
            List<SubscriptionPlan> subscriptionPlans = database.query(sql, (resultRow, _) -> {
                        SubscriptionPlan plan = new SubscriptionPlan();
                        plan.setSubscriptionPlanId(resultRow.getString("subscription_plan_id"));
                        plan.setName(resultRow.getString("name"));
                        plan.setDescription(resultRow.getString("description"));
                        plan.setPrice(resultRow.getDouble("price"));
                        plan.setRequestsPerDay(resultRow.getInt("requests_per_day"));
                        plan.setAdvancedFeatures(resultRow.getInt("advanced_features"));
                        plan.setPrioritySupport(resultRow.getInt("priority_support"));
                        plan.setCommunitySupport(resultRow.getInt("community_support"));
                        plan.setPackageType(resultRow.getString("package_type"));
                        plan.setMostPopular(resultRow.getInt("most_popular"));
                        plan.setIsVisible(resultRow.getInt("is_visible"));
                        
                        if(token != null && authService.tokenChecking(token)){
                        String subStringToken = token.substring(7);
                        String userId = jwtUtil.validateTokenAndGetValue(subStringToken);
                        Integer countUserActivePackage = database.queryForObject(checkUserActivePackageSql, Integer.class, userId, plan.getSubscriptionPlanId());
                            if(countUserActivePackage != null && countUserActivePackage > 0){
                                plan.setIsActive(1);
                            } else {
                                plan.setIsActive(0);
                            }
                        } else {
                              plan.setIsActive(0);
                        }

                        return plan;
            });
            result.setMessage("Getting subscription plans information successfully!");
            result.setIsSuccess(true);
            result.setSubscriptionPlans(subscriptionPlans);
            return result; 
        }
        catch (Exception e) {
            e.printStackTrace();
            result.setMessage("Can not reach Subscription plans");
            result.setIsSuccess(false);
            return result; 
        }
    }

     public Boolean userDefaultSubscribe(String userId){
        String subscriptionSql = """
                SELECT subscription_plan_id, requests_per_day FROM subscription_plans WHERE package_type = 'DEFAULT'
                """;
        String sql = """
            INSERT INTO user_subscription_plans (user_subscription_plan_id, user_id, subscription_plan_id, left_requests,
            receipt_id, start_date, end_date
            )
            VALUES (?, ?, ?, ?, NULL, NULL, NULL)
        """;
        try{
            UserSubscriptionPlan userSubscriptionPlan = new UserSubscriptionPlan();
            String newUserSubscriptionPlanId = userSubscriptionPlan.createUserSubscriptionPlanId();
            SubscriptionPlan subscriptionPlan = database.queryForObject(subscriptionSql, new Object[]{}, (resultRow, _)->{
                SubscriptionPlan s = new SubscriptionPlan();
                s.setSubscriptionPlanId(resultRow.getString("subscription_plan_id"));
                s.setRequestsPerDay(resultRow.getInt("requests_per_day"));
                return s;
            });
            if(subscriptionPlan == null){
                return false;
            }
            int rows = database.update(sql, newUserSubscriptionPlanId, userId, subscriptionPlan.getSubscriptionPlanId(), subscriptionPlan.getRequestsPerDay());
            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false; 
        }
    }

    @Transactional
  public int resetDailyRequestTokens() {
    // Postgres
    String sql = """
        UPDATE user_subscription_plans
        JOIN subscription_plans
        ON subscription_plans.subscription_plan_id = user_subscription_plans.subscription_plan_id
        SET user_subscription_plans.left_requests = subscription_plans.requests_per_day,
            user_subscription_plans.updated_at   = NOW()
        WHERE user_subscription_plans.left_requests IS NOT NULL;
      """;
    int n = database.update(sql);
    log.info("resetDailyRequestTokens: {}", n);
    return n;
  }

  @Transactional
  public int downgradeExpiredToDefault(ZoneId zone) {
    // 1) fetch DEFAULT plan id + daily quota
    var def = database.queryForObject("""
      SELECT subscription_plan_id, requests_per_day
      FROM subscription_plans
      WHERE package_type = 'DEFAULT'
    """, (rs, _) -> Map.of(
        "id", rs.getString("subscription_plan_id"),
        "quota", rs.getInt("requests_per_day")
    ));
    if (def == null) return 0;
    String defaultPlanId = (String) def.get("id");
    Integer defaultQuota   = (Integer) def.get("quota");

    // 2) set DEFAULT where expired (end_date < today in zone)
    String sql = """
      UPDATE user_subscription_plans
      SET subscription_plan_id = ?,
          left_requests = ?,
          receipt_id = NULL,
          start_date = NULL,
          end_date = NULL
      WHERE end_date IS NOT NULL
        AND end_date < NOW()
    """;
    int n = database.update(sql, defaultPlanId, defaultQuota);
    log.info("downgradeExpiredToDefault: {}", n);
    return n;
  }

}
