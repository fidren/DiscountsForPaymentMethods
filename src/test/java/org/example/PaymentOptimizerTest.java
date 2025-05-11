package org.example;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PaymentOptimizerTest {
    @Test
    public void testExampleCaseFromDocumentation() {
        List<Order> orders = List.of(
                new Order("ORDER1", new BigDecimal("100.00"), List.of("mZysk")),
                new Order("ORDER2", new BigDecimal("200.00"), List.of("BosBankrut")),
                new Order("ORDER3", new BigDecimal("150.00"), List.of("mZysk", "BosBankrut")),
                new Order("ORDER4", new BigDecimal("50.00"), null)
        );

        List<PaymentMethod> methods = List.of(
                new PaymentMethod("PUNKTY", BigDecimal.valueOf(15), new BigDecimal("100.00")),
                new PaymentMethod("mZysk", BigDecimal.valueOf(10), new BigDecimal("180.00")),
                new PaymentMethod("BosBankrut", BigDecimal.valueOf(5), new BigDecimal("200.00"))
        );

        PaymentOptimizer optimizer = new PaymentOptimizer(orders, methods);
        optimizer.optimize();
        Map<String, BigDecimal> result = optimizer.getSpent();

        assertEquals(new BigDecimal("100.00"), result.get("PUNKTY").setScale(2, RoundingMode.DOWN));
        assertEquals(new BigDecimal("165.00"), result.get("mZysk").setScale(2, RoundingMode.DOWN));
        assertEquals(new BigDecimal("190.00"), result.get("BosBankrut").setScale(2, RoundingMode.DOWN));
    }
    @Test
    public void testOnlyPoints() {
        List<Order> orders = List.of(
                new Order("ORDER1", new BigDecimal("50.00"), null),
                new Order("ORDER2", new BigDecimal("50.00"), null)
        );

        List<PaymentMethod> methods = List.of(
                new PaymentMethod("PUNKTY", BigDecimal.valueOf(20), new BigDecimal("120.00"))
        );

        PaymentOptimizer optimizer = new PaymentOptimizer(orders, methods);
        optimizer.optimize();
        Map<String, BigDecimal> result = optimizer.getSpent();

        assertEquals(new BigDecimal("80.00"), result.get("PUNKTY").setScale(2, RoundingMode.DOWN));
    }

    @Test
    public void testFallbackToCard() {
        List<Order> orders = List.of(
                new Order("ORDER1", new BigDecimal("100.00"), null)
        );

        List<PaymentMethod> methods = List.of(
                new PaymentMethod("PUNKTY", BigDecimal.valueOf(15), new BigDecimal("5.00")),
                new PaymentMethod("mZysk", BigDecimal.valueOf(10), new BigDecimal("100.00"))
        );

        PaymentOptimizer optimizer = new PaymentOptimizer(orders, methods);
        optimizer.optimize();
        Map<String, BigDecimal> result = optimizer.getSpent();

        assertEquals(new BigDecimal("100.00"), result.get("mZysk").setScale(2, RoundingMode.DOWN));
    }

    @Test
    public void testPartialPointsWithCardAnd10PercentDiscount() {
        List<Order> orders = List.of(
                new Order("ORDER1", new BigDecimal("100.00"), null)
        );

        List<PaymentMethod> methods = List.of(
                new PaymentMethod("PUNKTY", BigDecimal.valueOf(15), new BigDecimal("20.00")),
                new PaymentMethod("mZysk", BigDecimal.valueOf(0), new BigDecimal("100.00"))
        );

        PaymentOptimizer optimizer = new PaymentOptimizer(orders, methods);
        optimizer.optimize();
        Map<String, BigDecimal> result = optimizer.getSpent();

        assertEquals(new BigDecimal("20.00"), result.get("PUNKTY").setScale(2, RoundingMode.DOWN));
        assertEquals(new BigDecimal("70.00"), result.get("mZysk").setScale(2, RoundingMode.DOWN));
    }
}