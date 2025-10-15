package com.keetlo.ai.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

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

    public StripeService(StripeUtil stripeUtil, JdbcTemplate database){
        this.database = database;
    }

    public String createInitialPaymentSession(String subscriptionPlanId, String userId, String productName, Double amount) {
        Stripe.apiKey = STRIPE_SECRET_KEY;
        Long unitAmount = StripeUtil.toUnitAmount(new BigDecimal(amount.toString()), "usd");
        String invoiceId = StripeUtil.createInvoiceId();
        
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

        SessionCreateParams params =
            SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(STRIPE_RETURN_URL+"/payment/success?invoice_id="+invoiceId)
                .setCancelUrl(STRIPE_RETURN_URL+"/payment/cancel")
                .addLineItem(lineItem)
                .putMetadata("userId", userId)
                .putMetadata("subscriptionPlanId", subscriptionPlanId)
                .putMetadata("invoiceId", invoiceId)
                .build();

        String sql = "INSERT INTO orders (invoice, user_id, subscription_plan_id, amount, currency, status) " +
                 "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            database.update(sql,
                invoiceId,
                userId,
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

    public void createOrder(String payload, String sigHeader) {
    try {
        Event event = Webhook.constructEvent(payload, sigHeader, STRIPE_ENDPOINT_SECRET);
        System.out.println("event :" + event.getType());
        if ("checkout.session.completed".equals(event.getType())) {
            com.stripe.model.checkout.Session session =
                    (com.stripe.model.checkout.Session) event.getDataObjectDeserializer()
                    .getObject().orElse(null);

            if (session != null) {
                LocalDateTime startDate = LocalDateTime.now();
                LocalDateTime endDate = startDate.plusMonths(1);

                String sql = "UPDATE orders SET stripe_session_id=?, stripe_payment_intent_id=?, status=?, updated_at=NOW(), start_date=?, end_date=? WHERE invoice=?";
                int rowsUpdated = database.update(sql,
                        session.getId(),
                        session.getPaymentIntent(),
                        session.getPaymentStatus().toUpperCase(),
                        startDate,
                        endDate,
                        session.getMetadata().get("invoiceId")
                );

                if (rowsUpdated == 0) {
                    String insertSql = "INSERT INTO orders (invoice, user_id, subscription_plan_id, stripe_session_id, stripe_payment_intent_id, amount, currency, status, start_date, end_date, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?,?,?,NOW(),NOW())";
                    database.update(insertSql,
                            session.getMetadata().get("invoiceId"),
                            session.getMetadata().get("userId"),
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
                        subscription_plan_id=? , invoice=?, left_requests=?, start_date=?, end_date=?, updated_at=NOW()
                        WHERE user_id = ?
                        """;
                 database.update(insertUserSubSql,
                        session.getMetadata().get("subscriptionPlanId"),
                        session.getMetadata().get("invoiceId"),
                        null, 
                        startDate,
                        endDate,
                        session.getMetadata().get("userId")
                );
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    }

}
