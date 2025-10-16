package com.keetlo.ai.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class StripeUtil {

    public static String formatAmount(Long unitAmount, String currency) {
        if (unitAmount == null)
            return "0.00";

        int decimalPlaces = getDecimalPlaces(currency);

        BigDecimal amount = new BigDecimal(unitAmount)
                .movePointLeft(decimalPlaces);

        return amount.setScale(decimalPlaces, RoundingMode.HALF_UP).toString();
    }

    public static Long toUnitAmount(BigDecimal amount, String currency) {
        if (amount == null)
            return 0L;

        int decimalPlaces = getDecimalPlaces(currency);

        BigDecimal unitAmount = amount.movePointRight(decimalPlaces);
        return unitAmount.setScale(0, RoundingMode.HALF_UP).longValue();
    }

    public static double fromUnitAmount(Long amount, String currency) {
        if (amount == null) return 0.0;
        int decimalPlaces = getDecimalPlaces(currency);
        return amount / Math.pow(10, decimalPlaces);
    }

    private static int getDecimalPlaces(String currency) {
        if (currency == null)
            return 2;

        switch (currency.toUpperCase()) {
            case "JPY":
            case "KRW":
                return 0;
            default:
                return 2;
        }
    }

    public static String createReceiptId(){
        String rawUuid = UUID.randomUUID().toString();  
        String receiptId = "kt_" + rawUuid.replaceAll("-", "");
        return receiptId;
    }
}
