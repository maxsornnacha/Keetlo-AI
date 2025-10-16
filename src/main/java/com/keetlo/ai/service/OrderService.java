package com.keetlo.ai.service;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import com.keetlo.ai.model.Order;
import com.keetlo.ai.model.Order.PaymentStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    private final JdbcTemplate database;

    public OrderService(JdbcTemplate database) {
        this.database = database;
    }

    private static String symbolFor(String currency) {
        if (currency == null)
            return "";
        return switch (currency.toLowerCase()) {
            case "usd" -> "$";
            case "eur" -> "€";
            case "gbp" -> "£";
            case "thb" -> "฿";
            default -> currency.toUpperCase();
        };
    }

    private final RowMapper<Order> orderRowMapper = new RowMapper<>() {
        @Override
        public Order mapRow(ResultSet resultRow, int rowNum) throws SQLException {
            Order order = new Order();
            String currency = resultRow.getString("currency");
            order.setReceiptId(resultRow.getString("receipt_id"));
            order.setUserId(resultRow.getString("user_id"));
            order.setName(resultRow.getString("name"));
            order.setEmail(resultRow.getString("email"));
            order.setSubscriptionPlanId(resultRow.getString("subscription_plan_id"));
            order.setAmount(resultRow.getBigDecimal("amount"));
            order.setCurrency(currency);
            order.setCurrencySymbol(symbolFor(currency));
            order.setStatus(PaymentStatus.fromDb(resultRow.getString("status")));
            order.setStartDate(resultRow.getTimestamp("start_date") != null ? resultRow.getTimestamp("start_date").toLocalDateTime() : null);
            order.setEndDate(resultRow.getTimestamp("end_date") != null ? resultRow.getTimestamp("end_date").toLocalDateTime() : null);
            order.setCreatedAt(resultRow.getTimestamp("created_at") != null ? resultRow.getTimestamp("created_at").toLocalDateTime() : null);
            order.setPlanName(resultRow.getString("plan_name"));
            order.setPlanDescription(resultRow.getString("plan_description"));
            
            return order;
        }
    };

    public Optional<Order> getSingleOrderByReceiptIdAndUserId(String receiptId, String userId) {
        final String sql = """ 
                SELECT
                    orders.receipt_id, orders.user_id, orders.subscription_plan_id,
                    orders.amount, orders.currency, orders.status, orders.start_date, orders.end_date, orders.created_at,
                    subscription_plans.name AS plan_name, subscription_plans.description AS plan_description,
                    CONCAT(orders.firstname, ' ', orders.lastname) AS name, orders.email
                FROM orders
                LEFT JOIN subscription_plans ON subscription_plans.subscription_plan_id = orders.subscription_plan_id
                WHERE orders.receipt_id = ? AND orders.user_id = ?
                """;

        try {
            return Optional.ofNullable(database.queryForObject(sql, orderRowMapper, receiptId, userId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

        public Optional<Order> getSingleOrderByReceiptId(String receiptId) {
        final String sql = """ 
                SELECT
                    orders.receipt_id, orders.user_id, orders.subscription_plan_id,
                    orders.amount, orders.currency, orders.status, orders.start_date, orders.end_date, orders.created_at,
                    subscription_plans.name AS plan_name, subscription_plans.description AS plan_description,
                    CONCAT(orders.firstname, ' ', orders.lastname) AS name, orders.email
                FROM orders
                LEFT JOIN subscription_plans ON subscription_plans.subscription_plan_id = orders.subscription_plan_id
                WHERE orders.receipt_id = ?
                """;

        try {
            return Optional.ofNullable(database.queryForObject(sql, orderRowMapper, receiptId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Order> getOrdersByUserId(String userId, int limit, int offset) {
        final String sql = """
                SELECT
                    orders.receipt_id, orders.user_id, orders.subscription_plan_id, 
                    orders.amount, orders.currency, orders.status, orders.start_date, orders.end_date, orders.created_at,
                    subscription_plans.name AS plan_name, subscription_plans.description AS plan_description,
                    CONCAT(orders.firstname, ' ', orders.lastname) AS name, orders.email
                FROM orders
                LEFT JOIN subscription_plans ON subscription_plans.subscription_plan_id = orders.subscription_plan_id
                WHERE orders.user_id = ?
                ORDER BY orders.created_at DESC
                LIMIT ? OFFSET ?
                """;

        return database.query(sql, orderRowMapper, userId, limit, offset);
    }

    public int getOrderCountByUserId(String userId) {
        final String sql = "SELECT COUNT(*) FROM orders WHERE user_id = ?";
        Integer n = database.queryForObject(sql, Integer.class, userId);
        return n != null ? n : 0;
    }

}
