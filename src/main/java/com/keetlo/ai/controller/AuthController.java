package com.keetlo.ai.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.keetlo.ai.dto.ForgotPasswordResult;
import com.keetlo.ai.dto.LoginResult;
import com.keetlo.ai.dto.ResetPasswordTokenCheckRequest;
import com.keetlo.ai.model.User;
import com.keetlo.ai.service.AuthService;
import com.keetlo.ai.service.UserService;
import com.keetlo.ai.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;



@RestController
@RequestMapping("/auth")
public class AuthController {
    @SuppressWarnings("rawtypes")
    private final RedisTemplate redisTemplate;
    private final AuthService authService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, UserService userService, RedisTemplate redisTemplate, JwtUtil jwtUtil) {
        this.authService = authService;
        this.userService = userService;
        this.redisTemplate = redisTemplate;
        this.jwtUtil = jwtUtil;
    }
    

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User request) throws Throwable {
        Map<String, Object> response = new HashMap<>();

        if (request.getEmail() == null
                || request.getPassword() == null
                || request.getConfirmPassword() == null
                || request.getFirstname() == null
                || request.getPassword() == null) {

            response.put("message", "Invalid data format");
            return ResponseEntity.status(400).body(response);
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            response.put("message", "Passwords do not match.");
            return ResponseEntity.status(400).body(response);
        }

        try {
            boolean isUserExists = userService.isUserExists(request.getEmail());
            if (isUserExists) {
                response.put("message", "User with email " + request.getEmail() + " already exists.");
                return ResponseEntity.status(400).body(response);
            }
            boolean createdUser = authService.createUser(request.getEmail(), request.getPassword(),
                    request.getFirstname(), request.getLastname());
            if (!createdUser) {
                response.put("message", "User with email " + request.getEmail() + " already exists.");
                return ResponseEntity.status(500).body(response);
            }
            authService.sendEmailVerification(request.getEmail(), request.getFirstname(), request.getLastname());
            response.put("message", "User registered successfully!");
            return ResponseEntity.status(201).body(response);

        } catch (DataAccessException error) {
            response.put("message", "Server interval error: " + error.getMessage());
            return ResponseEntity.status(500).body(response);

        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User request) throws Throwable {
        Map<String, Object> response = new HashMap<>();
        if (request.getEmail() == null
                || request.getPassword() == null) {
            response.put("message", "Email or password is incorrect.");
            return ResponseEntity.status(400).body(response);
        }
        try {
            LoginResult result = authService.loginUser(request.getEmail(), request.getPassword());
            if (!result.isSuccess()) {
                response.put("message", result.getMessage());
                response.put("requiresVerification", result.isRequiresVerification());
                return ResponseEntity.status(400).body(response);
            }

            response.put("message", "Login successfully!");
            response.put("token", result.getToken());
            return ResponseEntity.status(200).body(response);
        } catch (DataAccessException error) {
            response.put("message", "Server interval error: " + error.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/oauth/login")
    public ResponseEntity<?> getSession(@RequestParam String oauthToken, @RequestParam String registrationId) {
        Map<String, Object> response = new HashMap<>();
        try {
            String oauthId = jwtUtil.validateTokenAndGetValue(oauthToken);
            String token = authService.loginUserOAuth2(registrationId, oauthId);

            response.put("token", token);
            response.put("message", "Oauth login loaded successfully");
            return ResponseEntity.status(200).body(response);
        } catch (DataAccessException error) {
            response.put("message", "Server interval error: " + error.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody User request) throws Exception {
        Map<String, Object> response = new HashMap<>();
        if (request.getEmail() == null) {
            response.put("message", "Invalid data format");
            return ResponseEntity.status(400).body(response);
        }

        try {
            boolean isUserExists = userService.isUserExists(request.getEmail());
            if (!isUserExists) {
                response.put("message", "No account found with email " + request.getEmail() + ".");
                return ResponseEntity.status(400).body(response);
            }
            ForgotPasswordResult result = authService.createPasswordResetToken(request.getEmail());
            if (!result.isSuccess()) {
                response.put("message", result.getMessage());
                return ResponseEntity.status(400).body(response);
            }

            response.put("message", "Send email successfully!");
            return ResponseEntity.status(200).body(response);

        } catch (DataAccessException error) {
            response.put("message", "Server interval error: " + error.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("reset-password-token-check")
    public ResponseEntity<?> resetPasswordTokenCheck(@RequestBody ResetPasswordTokenCheckRequest request) {
       Map<String, Object> response = new HashMap<>();
       try{
         if (request.getToken() == null) {
            response.put("message", "Invalid data format");
            return ResponseEntity.status(400).body(response);
        }
        Boolean result = authService.checkResetPasswordByToken(request.getToken());
        if(!result){
              response.put("message", "Invalid token value");
            return ResponseEntity.status(400).body(response);
        }
        response.put("message", "The given token is correct!");
        return ResponseEntity.status(200).body(response);

        } catch (DataAccessException error) {
            response.put("message", "Server interval error: " + error.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    

    @PostMapping("/reset-password")
    public ResponseEntity<?> ResetPassword(@RequestBody User request) {
        Map<String, Object> response = new HashMap<>();

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            response.put("message", "Passwords do not match.");
            return ResponseEntity.status(400).body(response);
        }
        try {
            boolean success = authService.resetPasswordWithToken(request.getToken(), request.getNewPassword());
            if (!success) {
                response.put("message", "Invalid or expired token.");
                return ResponseEntity.status(400).body(response);
            }

            response.put("message", "Password has been reset successfully");
            return ResponseEntity.ok(response);
        } catch (DataAccessException error) {
            response.put("message", "Server interval error: " + error.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @SuppressWarnings("unchecked")
    @PostMapping("logout")
    public ResponseEntity<?> Logout(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try{
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                redisTemplate.delete(token);
            }
             SecurityContextHolder.clearContext();

             response.put("message", "Logout successfully!");
            return ResponseEntity.status(200).body(response);
          } catch (DataAccessException error) {
            response.put("message", "Server interval error: " + error.getMessage());
            SecurityContextHolder.clearContext();
            return ResponseEntity.status(500).body(response);
        }
    }
    
}
