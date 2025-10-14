package com.keetlo.ai.controller;

import com.keetlo.ai.service.AuthService;
import com.keetlo.ai.service.OtpService;
import com.keetlo.ai.service.UserService;
import com.keetlo.ai.model.OtpCode;

import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth/otp")
public class OtpController {
    private final OtpService otpService;
    private final AuthService authService;
    private final UserService userService;

    public OtpController(OtpService otpService, UserService userService, AuthService authService) {
        this.otpService = otpService;
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/resend")
    public ResponseEntity<?> resendOtp(@RequestParam String email) throws Exception {
        Map<String, Object> response = new HashMap<>();
        try {
            if (!userService.isUserExists(email)) {
                response.put("message", "User does not exist.");
                return ResponseEntity.status(400).body(response);
            }

            if (userService.isEmailVerified(email)) {
                response.put("message", "Email already verified.");
                return ResponseEntity.status(400).body(response);
            }

            if (otpService.isAbleToResendOtp(email)) {
                response.put("message", "Please wait a few minutes before requesting another OTP.");
                return ResponseEntity.status(429).body(response);
            }
            otpService.sendOtpEmail(email);

            response.put("message", "OTP has been resent successfully.");
            return ResponseEntity.ok(response);
        } catch (DataAccessException error) {
            response.put("message", "Server interval error: " + error.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpCode request) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean isOtpAttemptExceeded = otpService.isOtpAttemptExceeded(request.getEmail());
            if (isOtpAttemptExceeded) {
                response.put("message", "Invalid or expired OTP.");
                return ResponseEntity.status(400).body(response);
            }
            boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtpCode());
            if (!isValid) {
                response.put("message", "Invalid or expired OTP.");
                return ResponseEntity.status(400).body(response);
            }

            userService.markEmailAsVerified(request.getEmail());
            String userId = userService.getUserIdByEmail(request.getEmail());
            String token = authService.storeToken(userId);

            response.put("message", "OTP verified successfully.");
            response.put("token", token);
            return ResponseEntity.ok(response);
        } catch (DataAccessException error) {
            response.put("message", "Server interval error: " + error.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

}
