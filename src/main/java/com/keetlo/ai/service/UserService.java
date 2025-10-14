package com.keetlo.ai.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.keetlo.ai.model.User;

@Service
public class UserService {
    private final JdbcTemplate database;

    public UserService(JdbcTemplate database) {
        this.database = database;
    }

    public String getUserIdByEmail(String email) {
        String sql = "SELECT user_id FROM users WHERE email = ?";
        return database.queryForObject(sql, String.class, email);
    }

    @SuppressWarnings("deprecation")
    public boolean isEmailVerified(String email) {
        String sql = "SELECT email_verified_at FROM users WHERE email = ?";
        LocalDateTime verifiedAt = database.queryForObject(sql, new Object[] { email }, LocalDateTime.class);
        return verifiedAt != null;
    }

    @SuppressWarnings("deprecation")
    public String getFullName(String email) {
        String sql = "SELECT firstname, lastname FROM users WHERE email = ?";
        return database.queryForObject(sql, new Object[] { email }, (resultRow, _) -> {
            String firstname = resultRow.getString("firstname");
            String lastname = resultRow.getString("lastname");
            return firstname + " " + lastname;
        });
    }

    public boolean markEmailAsVerified(String email) {
        String sql = "UPDATE users SET email_verified_at = ? WHERE email = ?";
        int updatedRows = database.update(sql, LocalDateTime.now(), email);
        return updatedRows > 0;
    }

    public boolean isUserExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = database.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    @SuppressWarnings("deprecation")
    public User getProfile(String userId) {
        String sql = """
        SELECT users.email, users.firstname, users.lastname, users.avatar_url, subscription_plans.name AS subscription_plan_name, subscription_plans.requests_per_day AS limit_requests, user_subscription_plans.left_requests,
        users.google_id, users.github_id
        FROM users 
            JOIN user_subscription_plans ON users.user_id = user_subscription_plans.user_id
            JOIN subscription_plans ON user_subscription_plans.subscription_plan_id = subscription_plans.subscription_plan_id
        WHERE users.user_id = ?
        """;
        List<User> users = database.query(sql, new Object[] { userId }, (resultRow, _) -> {
            User user = new User();
            user.setEmail(resultRow.getString("email"));
            user.setFirstname(resultRow.getString("firstname"));
            user.setLastname(resultRow.getString("lastname"));
            user.setAvatarUrl(resultRow.getString("avatar_url"));
            user.setSubscriptionPlanName(resultRow.getString("subscription_plan_name"));
            user.setLeftRequests(resultRow.getInt("left_requests"));
            user.setLimitRequests(resultRow.getInt("limit_requests"));
            if(resultRow.getString("github_id") != null){
                user.setIsGithubConnected(1);
            } else {
                user.setIsGithubConnected(0);
            }
            if(resultRow.getString("google_id") != null){
                user.setIsGoogleConnected(1);
            } else {
                user.setIsGoogleConnected(0);
            }
            return user;
        });

        return users.isEmpty() ? null : users.get(0);
    }

   public Boolean updateProfile(String userId, String firstname, String lastname, String avatarUrl) {
        String sql = "UPDATE users SET firstname = ?, lastname = ?, avatar_url = ? WHERE user_id = ?";
        int rows = database.update(sql, firstname, lastname, avatarUrl, userId);
        if (rows > 0) {
            return true;
        } else {
            return false;
        }
    }

}
