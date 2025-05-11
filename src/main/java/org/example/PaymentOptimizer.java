package org.example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class PaymentOptimizer {
    private static final BigDecimal ONE_HUNDRED = new BigDecimal(100);
    private static final BigDecimal ONE_TENTH = new BigDecimal("0.10");

    private List<Order> orders;
    private List<PaymentMethod> paymentMethods;
    private Map<String, BigDecimal> spent = new HashMap<>();
    private Map<String, PaymentMethod> methodMap = new HashMap<>();

    public PaymentOptimizer(List<Order> orders, List<PaymentMethod> paymentMethods) {
        this.orders = orders;
        this.paymentMethods = paymentMethods;
        for (PaymentMethod pm : paymentMethods) {
            methodMap.put(pm.getId(), pm);
            spent.put(pm.getId(), BigDecimal.ZERO);
        }
    }

    public Map<String, BigDecimal> getSpent() {
        return spent;
    }

    public void optimize(){
        List<Option> options = collectPotentialCardToOrders();
        options.sort((a, b) -> b.discount().compareTo(a.discount()));
        Set<String> assignedOrders = assignOrdersToCard(options);
        handleUnassignedOrders(assignedOrders);
    }

    private List<Option> collectPotentialCardToOrders(){
        List<Option> options = new ArrayList<>();
        for (Order order : orders) {
            if (order.getPromotions() != null) {
                for (String promo : order.getPromotions()) {
                    PaymentMethod card = methodMap.get(promo);
                    if (card != null && !promo.equals("PUNKTY") && card.getLimit().compareTo(order.getValue()) >= 0) {
                        BigDecimal discount = order.getValue().multiply(card.getDiscount()).divide(ONE_HUNDRED);
                        options.add(new Option(order.getId(), card.getId(), discount));
                    }
                }
            }
        }
        return options;
    }

    private Set<String> assignOrdersToCard(List<Option> options) {
        Set<String> assignedOrders = new HashSet<>();
        for (Option opt : options) {
            if (!assignedOrders.contains(opt.orderId())) {
                Order order = orders.stream().filter(o -> o.getId().equals(opt.orderId())).findFirst().orElse(null);
                PaymentMethod card = methodMap.get(opt.cardId());
                if (order != null && card != null && card.getLimit().compareTo(order.getValue()) >= 0) {
                    BigDecimal discount = order.getValue().multiply(card.getDiscount()).divide(ONE_HUNDRED);
                    BigDecimal paid = order.getValue().subtract(discount);
                    card.setLimit(card.getLimit().subtract(order.getValue()));
                    spent.put(card.getId(), spent.get(card.getId()).add(paid));
                    assignedOrders.add(order.getId());
                }
            }
        }
        return assignedOrders;
    }

    private void handleUnassignedOrders(Set<String> assignedOrders) {
        PaymentMethod points = methodMap.get("PUNKTY");
        for (Order order : orders) {
            if (assignedOrders.contains(order.getId())) continue;

            if (canFullyPayWithPoints(order, points)) {
                payFullyWithPoints(order, points);
                continue;
            }

            if (canPartiallyPayWithPoints(order, points)) {
                payPartiallyWithPointsAndCard(order, points);
                continue;
            }

            payFullyByCard(order);
        }
    }

    private boolean canFullyPayWithPoints(Order order, PaymentMethod points) {
        return points != null && points.getLimit().compareTo(order.getValue()) >= 0;
    }

    private void payFullyWithPoints(Order order, PaymentMethod points) {
        BigDecimal discount = order.getValue().multiply(points.getDiscount()).divide(ONE_HUNDRED);
        BigDecimal paid = order.getValue().subtract(discount);
        points.setLimit(points.getLimit().subtract(paid));
        spent.put("PUNKTY", spent.get("PUNKTY").add(paid));
    }

    private boolean canPartiallyPayWithPoints(Order order, PaymentMethod points) {
        BigDecimal tenPercent = order.getValue().multiply(ONE_TENTH);
        return points != null && points.getLimit().compareTo(tenPercent) >= 0;
    }

    private void payPartiallyWithPointsAndCard(Order order, PaymentMethod points) {
        BigDecimal pointsUsed = points.getLimit().min(order.getValue());
        BigDecimal discount = order.getValue().multiply(ONE_TENTH);
        BigDecimal toPay = order.getValue().subtract(discount);
        BigDecimal rest = toPay.subtract(pointsUsed);

        PaymentMethod card = pickAnyCardWithSufficientLimit(rest);

        if (card != null) {
            points.setLimit(points.getLimit().subtract(pointsUsed));
            card.setLimit(card.getLimit().subtract(rest));
            spent.put("PUNKTY", spent.get("PUNKTY").add(pointsUsed));
            spent.put(card.getId(), spent.get(card.getId()).add(rest));
        }
    }

    private PaymentMethod pickAnyCardWithSufficientLimit(BigDecimal rest) {
        return paymentMethods.stream()
                .filter(pm -> !pm.getId().equals("PUNKTY") && pm.getLimit().compareTo(rest) >= 0)
                .findFirst().orElse(null);
    }

    private void payFullyByCard(Order order) {
        PaymentMethod card = pickAnyCardWithSufficientLimit(order.getValue());
        if (card != null) {
            card.setLimit(card.getLimit().subtract(order.getValue()));
            spent.put(card.getId(), spent.get(card.getId()).add(order.getValue()));
        }
    }

    public void printSpent() {
        for (Map.Entry<String, BigDecimal> entry : spent.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                System.out.println(entry.getKey() + " " + entry.getValue().setScale(2, RoundingMode.HALF_UP));
            }
        }
    }
}
