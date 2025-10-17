package com.keetlo.ai.controller;

import com.keetlo.ai.model.Order;
import com.keetlo.ai.service.OrderDocumentService;
import com.keetlo.ai.service.OrderService;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
@Slf4j
public class OrderController {
    private final OrderService orderService;
    private final OrderDocumentService orderDocumentService;

    public OrderController(OrderService orderService, OrderDocumentService orderDocumentService) {
        this.orderService = orderService;
        this.orderDocumentService = orderDocumentService;
    }

    @GetMapping("/{receipt}")
    public ResponseEntity<?> getMyOrderByReceipt(@PathVariable String receipt) {
    Map<String, Object> response = new HashMap<>();
      try {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) auth.getPrincipal();
        if (userId == null) return ResponseEntity.status(401).build();

          Optional<Order> orderOpt = orderService.getSingleOrderByReceiptIdAndUserId(receipt, userId);
          response.put("message", "Order not found");
          response.put("receipt", receipt);
            return orderOpt.<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(404).body(response));

        } catch (IllegalArgumentException iae) {
          response.put("message", "Bad request");
          response.put("detail", iae.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("Error fetching order receipt={} for current user", receipt, e);
            response.put("message", "Internal server error");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping
    public ResponseEntity<?> getMyOrders(
            @RequestParam(name = "limit", defaultValue = "10") int limit,
            @RequestParam(name = "offset", defaultValue = "0") int offset
    ) {
     Map<String, Object> response = new HashMap<>();
     try{
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
       String userId = (String) auth.getPrincipal();
        if (userId == null) return ResponseEntity.status(401).build();
        // Basic guards
        if (limit < 1) limit = 1;
        if (limit > 100) limit = 100;
        if (offset < 0) offset = 0;


        List<Order> list = orderService.getOrdersByUserId(userId, limit, offset);
        int totalCount = orderService.getOrderCountByUserId(userId);
        response.put("list", list);
        response.put("total", totalCount);
        return ResponseEntity.ok(response);

         } catch (IllegalArgumentException iae) {
            response.put("message", "Bad request");
            response.put("detail", iae.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("Error listing orders for current user", e);
            response.put("message", "Internal server error");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/{receiptId}/receipt.pdf")
    public ResponseEntity<?> downloadReceipt(@PathVariable String receiptId) {
        Map<String, Object> response = new HashMap<>();
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) auth.getPrincipal();
        if (userId == null) return ResponseEntity.status(401).build();

         Optional<Order> orderOpt = orderService.getSingleOrderByReceiptIdAndUserId(receiptId, userId);
        if (orderOpt.isEmpty()) return ResponseEntity.status(404).build();

        Order order = orderOpt.get();
         if (!"PAID".equalsIgnoreCase(String.valueOf(order.getStatus()))) {
        // 402 is semantically correct; use 403/409 if you prefer
        response.put("message", "Receipt is available only for paid orders.");
        response.put("status", String.valueOf(order.getStatus()));
        response.put("receiptId", receiptId);
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(response);
        }
        byte[] pdf = orderDocumentService.buildReceiptPdf(order);
        return ResponseEntity.ok()
            .header("Content-Type", "application/pdf")
            .header("Content-Disposition", "inline; filename=receipt-" + receiptId + ".pdf")
            .body(pdf);
    }

}
