package com.keetlo.ai.service;

import java.util.List;

import org.springframework.context.annotation.Lazy;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.keetlo.ai.dto.GetSubscriptionPlansByTokenResult;
import com.keetlo.ai.model.SubscriptionPlan;
import com.keetlo.ai.model.UserSubscriptionPlan;
import com.keetlo.ai.util.JwtUtil;

@Service
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
            INSERT INTO user_subscription_plans (user_subscription_plan_id, user_id, subscription_plan_id, left_requests)
            VALUES (?, ?, ?, ?)
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

}
