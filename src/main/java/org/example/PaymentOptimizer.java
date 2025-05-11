package org.example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class PaymentOptimizer {
    private List<Order> orders;
    private List<PaymentMethod> paymentMethods;
    private Map<String, PaymentMethod> methodMap;
    private Map<String, BigDecimal> limitsMap;

    public PaymentOptimizer(List<Order> orders, List<PaymentMethod> paymentMethods, Map<String, PaymentMethod> methodMap, Map<String, BigDecimal> limitsMap) {
        this.orders = orders;
        this.paymentMethods = paymentMethods;
        this.methodMap = methodMap;
        this.limitsMap = limitsMap;
    }

    public void calculateBestSolution() {
        for(Order order : orders) {
            List<PaymentOption> options = new ArrayList<>();
            BigDecimal value = order.getValue();

            if(limitsMap.get("PUNKTY").compareTo(value) >= 0) {
                options.add(pointsPaymentOption(value, methodMap.get("PUNKTY").getDiscount()));
            }
            options.addAll(cardPaymentOption(value, order.getPromotions()));
            if(limitsMap.get("PUNKTY").compareTo(BigDecimal.ZERO) >= 0) {
                options.addAll(mixedPaymentOption(value, order.getPromotions()));
            }

            options.sort(Comparator.comparing(PaymentOption::getDiscountAmount)
                    .thenComparing(PaymentOption::getPointsUsed)
                    .thenComparing(PaymentOption::getCardUsed).reversed());

            if(!options.isEmpty()) {
                PaymentOption bestOption = options.getFirst();
                System.out.println("ORDER " + order.getId() + " -> " + bestOption);

                updateLimits(bestOption);
            } else {
                System.out.println("ORDER " + order.getId() + " -> BRAK OPCJI");
            }
        }
    }

    private void updateLimits(PaymentOption bestOption) {
        limitsMap.put("PUNKTY", limitsMap.get("PUNKTY").subtract(bestOption.getPointsUsed()));
        if (!bestOption.getMethod().equals("PUNKTY")) {
            String card = bestOption.getMethod().replace("PUNKTY + ", "");
            limitsMap.put(card, limitsMap.get(card).subtract(bestOption.getCardUsed()));
        }
    }

    private List<PaymentOption> mixedPaymentOption(BigDecimal value, List<String> promotions) {
        List<PaymentOption> options = new ArrayList<>();
        BigDecimal tenPercent = value.multiply(BigDecimal.valueOf(0.10)).setScale(2, RoundingMode.HALF_UP);

        BigDecimal pointsToUse = limitsMap.get("PUNKTY").min(value);
        if(pointsToUse.compareTo(BigDecimal.ZERO) == 0) {
            return Collections.emptyList();
        }
        BigDecimal cardValueNeeded = value.subtract(pointsToUse);
        for (PaymentMethod m : paymentMethods) {
            if(!m.getId().equals("PUNKTY") && limitsMap.get(m.getId()).compareTo(cardValueNeeded) >= 0){
                boolean promo = pointsToUse.compareTo(tenPercent) >= 0;
                BigDecimal discount = promo ? tenPercent : BigDecimal.ZERO;
                if(cardValueNeeded.compareTo(discount) > 0) {
                    options.add(new PaymentOption("PUNKTY + " + m.getId(), pointsToUse, cardValueNeeded.subtract(discount), discount));
                } else {
                    options.add(new PaymentOption("PUNKTY + " + m.getId(), pointsToUse.subtract(discount.subtract(cardValueNeeded)), BigDecimal.ZERO, discount));
                }
            }
        }
        return options;
    }

    private List<PaymentOption> cardPaymentOption(BigDecimal value, List<String> promotions) {
        List<PaymentOption> options = new ArrayList<>();
        for (PaymentMethod m : paymentMethods) {
            if (!m.getId().equals("PUNKTY") && limitsMap.get(m.getId()).compareTo(value) >= 0) {
                boolean promo = promotions != null && promotions.contains(m.getId());
                BigDecimal discount = promo ? value.multiply(BigDecimal.valueOf(m.getDiscount())).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
                options.add(new PaymentOption(m.getId(), BigDecimal.ZERO, value.subtract(discount), discount));
            }
        }
        return options;
    }

    private PaymentOption pointsPaymentOption(BigDecimal value, int pointsDiscount) {
        BigDecimal discount = value.multiply(BigDecimal.valueOf(pointsDiscount)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return new PaymentOption("PUNKTY", value.subtract(discount), BigDecimal.ZERO, discount);
    }

    void printResult(){
        for (PaymentMethod pm : paymentMethods) {
            System.out.println(pm.getId() + " " + pm.getLimit().subtract(limitsMap.get(pm.getId())));
        }
    }
}
