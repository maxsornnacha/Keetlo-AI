package com.keetlo.ai.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.keetlo.ai.dto.StripePackageRequest;
import com.keetlo.ai.model.Order;
import com.keetlo.ai.model.SubscriptionPlan;
import com.keetlo.ai.service.OrderDocumentService;
import com.keetlo.ai.service.OrderService;
import com.keetlo.ai.service.StripeService;
import com.keetlo.ai.service.SubscriptionService;

@RestController
@RequestMapping("/stripe")
public class StripeController {

    private final StripeService stripeService;
    private final SubscriptionService subscriptionService;

    public StripeController(StripeService stripeService,  SubscriptionService subscriptionService
    ) {
        this.stripeService = stripeService;
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/create-payment")
    public ResponseEntity<?> createPayment(@RequestBody StripePackageRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userId = (String) auth.getPrincipal();
            String sessionUrl = null;
            if(request.getSubscriptionPlanId() == null){
                response.put("message", "No package is found");
                return ResponseEntity.status(400).body(response);
            }
            SubscriptionPlan getPlanNameAndPrice = subscriptionService.getPlanNameAndPrice(request.getSubscriptionPlanId());
            if (getPlanNameAndPrice == null || getPlanNameAndPrice.getName() == null || getPlanNameAndPrice.getPrice() == null) {
                response.put("message", "Please define the valid package type of payment");
                return ResponseEntity.status(400).body(response);
            }
            sessionUrl = stripeService.createInitialPaymentSession(request.getSubscriptionPlanId(), userId, getPlanNameAndPrice.getName(), getPlanNameAndPrice.getPrice());
            if (sessionUrl == null) {
                response.put("message", "Cannot create payment, please try again");
                return ResponseEntity.status(500).body(response);
            }
            response.put("sessionUrl", sessionUrl);
            return ResponseEntity.ok(response);
        } catch (DataAccessException error) {
            response.put("message", "Server interval error: " + error.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (!stripeService.verifySignature(payload, sigHeader)) {
                System.out.print("Invalid Stripe signature");
                response.put("message", "Invalid Stripe signature");
                return ResponseEntity.status(400).body(response);
            }
            stripeService.createOrder(payload, sigHeader);
            

            response.put("message", "Webhook function got worked successfully");
            return ResponseEntity.status(200).body(response);

        } catch (DataAccessException error) {
            response.put("message", "Server interval error: " + error.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

}
