package org.example;

import java.math.BigDecimal;

public class PaymentOption {
    private String method;
    private BigDecimal pointsUsed;
    private BigDecimal cardUsed;
    private BigDecimal discountAmount;

    public PaymentOption(String method, BigDecimal pointsUsed, BigDecimal cardUsed, BigDecimal discountAmount) {
        this.method = method;
        this.pointsUsed = pointsUsed;
        this.cardUsed = cardUsed;
        this.discountAmount = discountAmount;
    }

    public String getMethod() {
        return method;
    }

    public BigDecimal getPointsUsed() {
        return pointsUsed;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public BigDecimal getCardUsed() {
        return cardUsed;
    }

    @Override
    public String toString() {
        return "PaymentOption{" +
                "method='" + method + '\'' +
                ", pointsUsed=" + pointsUsed +
                ", cardUsed=" + cardUsed +
                ", discountAmount=" + discountAmount +
                '}';
    }
}
