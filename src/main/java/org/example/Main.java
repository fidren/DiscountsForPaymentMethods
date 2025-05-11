package org.example;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        final String orderPath = "D:\\dodatkowe_cwiczenia\\zadania_rekrutacyjne\\DiscountsForPaymentMethods\\src\\main\\resources\\orders.json";
        final String paymentsMethodsPath = "D:\\dodatkowe_cwiczenia\\zadania_rekrutacyjne\\DiscountsForPaymentMethods\\src\\main\\resources\\paymentmethods.json";

        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Order> orders = mapper.readValue(new File(orderPath), new TypeReference<>(){});
            List<PaymentMethod> paymentMethods = mapper.readValue(new File(paymentsMethodsPath), new TypeReference<>(){});

            Map<String, PaymentMethod> methodMap = new HashMap<>();
            for (PaymentMethod paymentMethod : paymentMethods) {
                methodMap.put(paymentMethod.getId(), paymentMethod);
            }
            Map<String, BigDecimal> limits = new HashMap<>();
            for (PaymentMethod paymentMethod : paymentMethods) {
                limits.put(paymentMethod.getId(), paymentMethod.getLimit());
            }
            PaymentOptimizer paymentOptimizer = new PaymentOptimizer(orders, paymentMethods, methodMap, limits);
            paymentOptimizer.calculateBestSolution();
            paymentOptimizer.printResult();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}