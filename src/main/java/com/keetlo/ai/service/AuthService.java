package com.keetlo.ai.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.keetlo.ai.dto.ForgotPasswordResult;
import com.keetlo.ai.dto.LoginResult;
import com.keetlo.ai.model.PasswordResetToken;
import com.keetlo.ai.model.User;
import com.keetlo.ai.util.JwtUtil;

@Service
public class AuthService {
    private final JdbcTemplate database;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UserService userService;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;
    private final SessionService sessionService;
    private final SubscriptionService subscriptionService;
    @Value("${client.url}")
    private String CLIENT_URL;

    public AuthService(JdbcTemplate database, PasswordEncoder passwordEncoder, EmailService emailService,
            JwtUtil jwtUtil, SessionService sessionService, UserService userService, OtpService otpService,
            SubscriptionService subscriptionService) {
        this.database = database;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
        this.sessionService = sessionService;
        this.userService = userService;
        this.otpService = otpService;
        this.subscriptionService = subscriptionService;
    }

    public boolean createUser(String email, String password, String firstname, String lastname) {
        String sql = """
                    INSERT INTO users (user_id, email, password, firstname, lastname)
                    VALUES (?, ?, ?, ?, ?)
                """;

        try {
            String hashedPassword = passwordEncoder.encode(password);
            User user = new User();
            String newUserId = user.createUserId();

            int rows = database.update(sql, newUserId, email, hashedPassword, firstname, lastname);
            
            
            boolean subscriptionRows = subscriptionService.userDefaultSubscribe(newUserId);
            return (rows > 0 && subscriptionRows);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void sendEmailVerification(String email, String firstname, String lastname) throws Exception {
        String newOtp = otpService.sendOtp(email, 6);
        String htmlContent = emailService.loadEmailTemplate("email-verification-template.html");
        htmlContent = htmlContent
                .replace("{name}", firstname + " " + lastname)
                .replace("{code}", newOtp);

        emailService.sendHtmlEmail(email, "Keetlo Email Verification", htmlContent);
    }

    public String storeToken(String userId) {
        String token = jwtUtil.generateToken(userId);
        sessionService.storeToken(token, userId, 24 * 60 * 60);
        return token;
    }

    @SuppressWarnings({ "null", "deprecation" })
    public LoginResult loginUser(String email, String password) {
        try {
            String sql = "SELECT user_id, email, password, email_verified_at, deleted_at FROM users WHERE email = ?";

            User user;
            try {
                user = database.queryForObject(sql, new Object[] { email }, (resultRow, _) -> {
                    User u = new User();
                    u.setUserId(resultRow.getString("user_id"));
                    u.setEmail(resultRow.getString("email"));
                    u.setPassword(resultRow.getString("password"));
                    Timestamp emailVerifiedAt = resultRow.getTimestamp("email_verified_at");
                    u.setEmailVerifiedAt(emailVerifiedAt != null ? emailVerifiedAt.toLocalDateTime() : null);
                    Timestamp deletedAt = resultRow.getTimestamp("deleted_at");
                    u.setDeletedAt(deletedAt != null ? deletedAt.toLocalDateTime() : null);
                    return u;
                });
            } catch (org.springframework.dao.EmptyResultDataAccessException e) {
                // No user found for that email
                return new LoginResult(false, "Email or password is incorrect", null, false);
            }

            if (!passwordEncoder.matches(password, user.getPassword())) {
                return new LoginResult(false, "Email or password is incorrect", null, false);
            }

            if (user.getDeletedAt() != null) {
                return new LoginResult(false, "User has been deleted", null, false);
            }

            if (user.getEmailVerifiedAt() == null) {
                return new LoginResult(false, "Please verify your email before login", null, true);
            }

            String token = this.storeToken(user.getUserId());
            return new LoginResult(true, "Login successful", token, false);

        } catch (Exception e) {
            e.printStackTrace();
            return new LoginResult(false, "Server error: " + e.getMessage(), null, false);
        }
    }

    public String loginUserOAuth2(String registrationId, String oauthId) {
        String userId = database.queryForObject(
                "SELECT user_id FROM users WHERE " + registrationId + "_id = ?",
                String.class, oauthId);
        String token = this.storeToken(userId);
        return token;
    }

    public ForgotPasswordResult createPasswordResetToken(String email) throws Exception {
        try {
            if (!userService.isUserExists(email)) {
                return new ForgotPasswordResult(false, "No account found with email " + email + ".", null);
            }

            String userID = userService.getUserIdByEmail(email);
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);

            PasswordResetToken passwordResetToken = new PasswordResetToken();
            String newPasswordResetTokenId = passwordResetToken.createPasswordResetTokenId();
            String newToken = passwordResetToken.createToken();
            Boolean tokenExists = this.checkResetPasswordByEmail(email);
            if(tokenExists){
             String sql = """
                    UPDATE password_reset_tokens SET token=?, expires_at=? WHERE email = ? AND used = 0
                    """;
            database.update(sql, newToken, expiresAt, email);

            } else {
            String sql = """
                    INSERT INTO password_reset_tokens (password_reset_token_id, user_id, email, token, expires_at)
                    VALUES (?, ?, ?, ?, ?)
                    """;
            database.update(sql, newPasswordResetTokenId, userID, email, newToken, expiresAt);
            }
            // Prepare email
            this.sendForgotPasswordEmail(email, newToken);

            return new ForgotPasswordResult(true, "Password reset email sent successfully!", newToken);

        } catch (DataAccessException e) {
            return new ForgotPasswordResult(false, "Server error: " + e.getMessage(), null);
        }
    }

    public void sendForgotPasswordEmail(String email, String newToken) throws Exception {
        String htmlContent = emailService.loadEmailTemplate("forgot-password-template.html");
        htmlContent = htmlContent.replace("{name}", userService.getFullName(email))
                .replace("{resetLink}", CLIENT_URL + "/auth/forgot-password" + "?token=" + newToken);
        emailService.sendHtmlEmail(email, "Forgot Password", htmlContent);
    }

    public boolean checkResetPasswordByEmail(String email) {
        var rows = database.queryForList(
        "SELECT password_reset_token_id FROM password_reset_tokens WHERE email = ? AND used = 0", email);
        return !rows.isEmpty();
    }

    public boolean checkResetPasswordByToken(String token) {
        var rows = database.queryForList(
        "SELECT password_reset_token_id FROM password_reset_tokens WHERE token = ? AND expires_at > NOW() AND used = 0", token);
        return !rows.isEmpty();
    }

    public boolean resetPasswordWithToken(String token, String newPassword) {
        var rows = database.queryForList(
                "SELECT * FROM password_reset_tokens WHERE token = ? AND expires_at > NOW() AND used = 0", token);

        if (rows.isEmpty()) {
            return false;
        }

        Map<String, Object> row = rows.get(0);
        String userId = (String) row.get("user_id");
        String hashedPassword = passwordEncoder.encode(newPassword);

        database.update("UPDATE users SET password = ? WHERE user_id = ?", hashedPassword, userId);
        database.update("UPDATE password_reset_tokens SET used = 1 WHERE token = ?", token);

        return true;
    }

    public Boolean tokenChecking(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                String userId = jwtUtil.validateTokenAndGetValue(token);
                String sessionUserId = sessionService.getUserIdByToken(token);
                if (sessionUserId == null || !sessionUserId.equals(userId)) {
                    return false;
                }
                return true;

            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }

}
