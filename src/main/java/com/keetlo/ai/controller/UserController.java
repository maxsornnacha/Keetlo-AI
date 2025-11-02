package com.keetlo.ai.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.keetlo.ai.model.User;
import com.keetlo.ai.service.FavoriteService;
import com.keetlo.ai.service.UserService;
import com.keetlo.ai.util.FileUtil;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/user")
public class UserController {
      private final UserService userService;
      private final FileUtil fileUtil;
      private final FavoriteService favoriteService;

    public UserController(UserService userService, FileUtil fileUtil, FavoriteService favoriteService) {
        this.userService = userService;
        this.fileUtil = fileUtil;
        this.favoriteService = favoriteService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userId = (String) auth.getPrincipal();
            User user = userService.getProfile(userId);
            if(user == null){
                response.put("message", "user is not found");
                return ResponseEntity.status(404).body(response);
            }
            response.put("message", "user profile got fetched successfully");
            response.put("user", user);
            return ResponseEntity.ok(response);
        } catch (DataAccessException error) {
            response.put("message", "Server interval error: " + error.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateInfo(@RequestBody User request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userId = (String) auth.getPrincipal();

            String base64Image = request.getAvatarUrl();
            if (base64Image != null && !base64Image.isEmpty() && fileUtil.isBase64(base64Image)) {
                String uploadDir = "/images/users/";
                String imageName = "user_image_" + System.currentTimeMillis() + ".webp";
                  User existingUser = userService.getProfile(userId);
                if(existingUser == null){
                    response.put("message", "user is not found");
                    return ResponseEntity.status(404).body(response);
                }
                String filePath = uploadDir + imageName;
                try {
                    fileUtil.saveBase64Image(base64Image, filePath);
                    if(existingUser.getAvatarUrl() != null && !existingUser.getAvatarUrl().trim().isEmpty()) {
                        String oldFilePath = existingUser.getAvatarUrl();
                        fileUtil.deleteFile(oldFilePath);
                    }
                } catch (IOException e) {
                    response.put("message","Failed to save image.");
                    return ResponseEntity.status(500).body(response);
                }
                request.setAvatarUrl("/images/users/" + imageName);
            }
            
            Boolean result = userService.updateProfile(userId, request.getFirstname(), request.getLastname(), request.getAvatarUrl());
            if(!result){
                response.put("message", "Updating user information failed, please try again.");
                return ResponseEntity.status(404).body(response);
            }
            response.put("message", "user profile got updated successfully");
            return ResponseEntity.ok(response);
        } catch (DataAccessException error) {
            response.put("message", "Server interval error: " + error.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
     @GetMapping("/favorites")
    public ResponseEntity<?> getFavorites(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "sort", required = false, defaultValue = "newFav") String sort,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "16") Integer pageSize
    ) {
        // Require auth: userId from SecurityContext (JwtAuthenticationFilter set principal=userId)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) auth.getPrincipal();


        Map<String, Object> result = favoriteService.getFavoriteProjects(
                userId, q, sort, page, pageSize
        );

        return ResponseEntity.ok(result);
    }

}
