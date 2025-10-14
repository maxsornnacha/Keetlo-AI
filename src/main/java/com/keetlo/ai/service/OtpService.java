package com.keetlo.ai.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.keetlo.ai.model.OtpCode;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
public class OtpService {
    private final JdbcTemplate database;
    private final EmailService emailService;
    private final UserService userService;

    public OtpService(JdbcTemplate database, EmailService emailService, UserService userService) {
        this.database = database;
        this.emailService = emailService;
        this.userService = userService;
    }

    private String generateOtp(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    @Transactional
    public String sendOtp(String email, int length) {
        String otp = generateOtp(length);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);
        String expiresAtStr = expiresAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String checkSql = "SELECT COUNT(*) FROM otp_codes WHERE email = ? AND used = 0";
        @SuppressWarnings("null")
        int count = database.queryForObject(checkSql, Integer.class, email);
        if (count > 0) {
            String updateSql = "UPDATE otp_codes SET otp_code = ?, expires_at = ?, attempts = 0, created_at = NOW() " +
                    "WHERE email = ? AND used = 0";
            database.update(updateSql, otp, expiresAtStr, email);
        } else {
            String userId = userService.getUserIdByEmail(email);
            String insertSql = "INSERT INTO otp_codes (otp_code_id, user_id, email, otp_code, type, expires_at, used, attempts, created_at) "
                    +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, 0, NOW())";
            OtpCode otpCode = new OtpCode();
            String newOtpCodeId = otpCode.createOtpCodeId();
            database.update(insertSql, newOtpCodeId, userId, email, otp, "EMAIL", expiresAtStr, 0);
        }

        return otp;
    }

    public boolean isAbleToResendOtp(String email){
        String sql = "SELECT created_at FROM otp_codes WHERE email = ? AND used = 0 ORDER BY created_at DESC LIMIT 1";
        List<Timestamp> results = database.query(sql,
            (rs, _) -> rs.getTimestamp("created_at"), email);

        if (!results.isEmpty()) {
            LocalDateTime lastCreated = results.get(0).toLocalDateTime();
            if (lastCreated.isBefore(LocalDateTime.now().minusMinutes(3))) {
                return false;
            }
        }

        return true;
    }

    public void sendOtpEmail(String email) throws Exception{
         String newOtp = this.sendOtp(email, 6);
        String htmlContent = emailService.loadEmailTemplate("email-verification-template.html");
            htmlContent = htmlContent.replace("{name}", userService.getFullName(email))
                    .replace("{code}", newOtp);
            emailService.sendHtmlEmail(email, "Your OTP Code", htmlContent);
    }

    public boolean verifyOtp(String email, String otp) {
        String sql = "SELECT otp_code, expires_at, used FROM otp_codes WHERE email=? AND otp_code=? AND used=0 ORDER BY created_at DESC LIMIT 1";
        @SuppressWarnings("deprecation")
        Boolean isOtpVerified = database.query(sql, new Object[] { email, otp }, (resultRow) -> {
            if (resultRow.next()) {
                Integer used = resultRow.getInt("used");
                 System.out.println(used);
                LocalDateTime expiresAt = resultRow.getTimestamp("expires_at").toLocalDateTime();
                if (used == 0 && expiresAt.isAfter(LocalDateTime.now())) {
                    String updateSql = "UPDATE otp_codes SET used = 1 WHERE email = ? AND otp_code = ?";
                    database.update(updateSql, email, otp);
                    return true;
                }
            }
            return false;
        });
        if (isOtpVerified == null || !isOtpVerified) {
            String updateAttemptsSql = "UPDATE otp_codes SET attempts = attempts + 1 WHERE email = ?";
            database.update(updateAttemptsSql, email);
            return false;
        }
        return isOtpVerified;
    }

    public boolean isOtpAttemptExceeded(String email) {
        try {
            String sql = "SELECT attempts FROM otp_codes " +
                    "WHERE email = ?" +
                    "ORDER BY created_at DESC LIMIT 1";
            @SuppressWarnings("deprecation")
            Integer attempts = database.queryForObject(sql, new Object[] { email }, Integer.class);
            if (attempts != null && attempts >= 5) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }


}
