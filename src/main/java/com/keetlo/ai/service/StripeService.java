package com.keetlo.ai.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.keetlo.ai.model.Order;
import com.keetlo.ai.model.User;
import com.keetlo.ai.util.StripeUtil;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;

@Service
public class StripeService {
    @Value("${stripe.secret.key}")
    private String STRIPE_SECRET_KEY;
    @Value("${stripe.endpoint.secret}")
    private String STRIPE_ENDPOINT_SECRET;
    @Value("${stripe.return.url}")
    private String STRIPE_RETURN_URL;
     private final JdbcTemplate database;
     private final OrderService orderService;
    private final OrderDocumentService orderDocumentService;
    private final UserService userService;

    public StripeService(StripeUtil stripeUtil, JdbcTemplate database,
        OrderService orderService, OrderDocumentService orderDocumentService,
        UserService userService
    ){
        this.database = database;
        this.orderService = orderService;
        this.orderDocumentService = orderDocumentService;
        this.userService = userService;
    }

    public String createInitialPaymentSession(String subscriptionPlanId, String userId, String productName, Double amount) {
        Stripe.apiKey = STRIPE_SECRET_KEY;
        Long unitAmount = StripeUtil.toUnitAmount(new BigDecimal(amount.toString()), "usd");
        String receiptId = StripeUtil.createReceiptId();
        
        SessionCreateParams.LineItem lineItem =
        SessionCreateParams.LineItem.builder()
            .setQuantity(1L)
            .setPriceData(
                SessionCreateParams.LineItem.PriceData.builder()
                    .setCurrency("usd")
                    .setUnitAmount(unitAmount) 
                    .setProductData(
                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName(productName)
                            .build()
                    )
                    .build()
            )
        .build();

        User userInfo = userService.getProfile(userId);
        SessionCreateParams params =
            SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(STRIPE_RETURN_URL+"/payment/success?receipt_id="+receiptId)
                .setCancelUrl(STRIPE_RETURN_URL+"/payment/cancel")
                .addLineItem(lineItem)
                .putMetadata("userId", userId)
                .putMetadata("firstname", userInfo.getFirstname())
                .putMetadata("lastname", userInfo.getLastname())
                .putMetadata("email", userId)
                .putMetadata("subscriptionPlanId", subscriptionPlanId)
                .putMetadata("receiptId", receiptId)
                .build();

        String sql = "INSERT INTO orders (receipt_id, user_id, firstname, lastname, email, subscription_plan_id, amount, currency, status) " +
                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            database.update(sql,
                receiptId,
                userId,
                userInfo.getFirstname(),
                userInfo.getLastname(),
                userInfo.getEmail(),
                subscriptionPlanId,
                amount,
                "usd",
                "PENDING"
            );

             Session session = Session.create(params);
             String sessionUrl = session.getUrl();

             return sessionUrl;
        } catch (StripeException e) {
            e.printStackTrace();
        }
		return null;

    }

    public Boolean verifySignature(String payload, String sigHeader){
      try {
        Event event = Webhook.constructEvent(
                    payload,
                    sigHeader,
                    STRIPE_ENDPOINT_SECRET
                );
        if(event != null){
            return true;
        } else {
            return false;
        }
      } catch (SignatureVerificationException e) {
        e.printStackTrace();
        return false;
      }
    }

    @Transactional
    public void createOrder(String payload, String sigHeader) {
    try {
        Event event = Webhook.constructEvent(payload, sigHeader, STRIPE_ENDPOINT_SECRET);
        com.stripe.model.checkout.Session session =
        (com.stripe.model.checkout.Session) event.getDataObjectDeserializer()
        .getObject().orElse(null);

        if ("checkout.session.completed".equals(event.getType())) {

            if (session != null) {
                LocalDateTime startDate = LocalDateTime.now();
                LocalDateTime endDate = startDate.plusMonths(1);

                String sql = "UPDATE orders SET stripe_session_id=?, stripe_payment_intent_id=?, status=?, updated_at=NOW(), start_date=?, end_date=? WHERE receipt_id=?";
                int rowsUpdated = database.update(sql,
                        session.getId(),
                        session.getPaymentIntent(),
                        session.getPaymentStatus().toUpperCase(),
                        startDate,
                        endDate,
                        session.getMetadata().get("receiptId")
                );

                if (rowsUpdated == 0) {
                    String insertSql = "INSERT INTO orders (receipt_id, user_id, firstname, lastname, email, subscription_plan_id, stripe_session_id, stripe_payment_intent_id, amount, currency, status, start_date, end_date, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,NOW(),NOW())";
                    database.update(insertSql,
                            session.getMetadata().get("receiptId"),
                            session.getMetadata().get("userId"),
                            session.getMetadata().get("firstname"),
                            session.getMetadata().get("lastname"),
                            session.getMetadata().get("email"),
                            session.getMetadata().get("subscriptionPlanId"),
                            session.getId(),
                            session.getPaymentIntent(),
                            StripeUtil.fromUnitAmount(session.getAmountTotal(), session.getCurrency()),
                            session.getCurrency(),
                            session.getPaymentStatus().toUpperCase(),
                            startDate,
                            endDate
                    );
                }

                String insertUserSubSql = """
                        UPDATE user_subscription_plans SET
                        subscription_plan_id=? , receipt_id=?, left_requests=?, start_date=?, end_date=?, updated_at=NOW()
                        WHERE user_id = ?
                        """;
                 database.update(insertUserSubSql,
                        session.getMetadata().get("subscriptionPlanId"),
                        session.getMetadata().get("receiptId"),
                        null, 
                        startDate,
                        endDate,
                        session.getMetadata().get("userId")
                );
         
                
                orderService.getSingleOrderByReceiptId(session.getMetadata().get("receiptId")).ifPresent(o -> {
                try {
                    Optional<Order> orderOpt = orderService.getSingleOrderByReceiptId(session.getMetadata().get("receiptId"));
                    Order order = orderOpt.get();
                    orderDocumentService.emailReceiptPdf(order);
                } catch (Exception mailEx) {
                    mailEx.printStackTrace(); // don't fail webhook
                }
                });
            }
        }

          // Optional: mark other outcomes
      if ("checkout.session.async_payment_failed".equals(event.getType()) ||
          "checkout.session.expired".equals(event.getType())) {
        if (session != null) {
        database.update("""
            UPDATE orders SET status=?, updated_at=NOW()
             WHERE receipt_id=?
            """,
            "FAILED", session.getMetadata().get("receiptId")
        );
        }
      }
    } catch (Exception e) {
        e.printStackTrace();
    }
    }

}
