package com.keetlo.ai.controller;

import org.springframework.web.bind.annotation.RestController;

import com.keetlo.ai.dto.GetSubscriptionPlansByTokenResult;
import com.keetlo.ai.service.SubscriptionService;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@RestController
@RequestMapping("/subscription")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService){
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/info")
    public ResponseEntity<?> getSubscriptionPlanInfo(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try{
            String token = request.getHeader("Authorization");
            GetSubscriptionPlansByTokenResult result = subscriptionService.getSubscribtionPlansByToken(token);
            return ResponseEntity.status(200).body(result);
        } catch (DataAccessException error) {
            response.put("message", "Server interval error: " + error.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    

}
