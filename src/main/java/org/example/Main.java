package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("Wrong number of arguments. First argument: [order absolute path]. Second argument: [payment methods absolute path]");
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Order> orders = mapper.readValue(new File(args[0]), new TypeReference<>(){});
            List<PaymentMethod> methods = mapper.readValue(new File(args[1]), new TypeReference<>(){});

            PaymentOptimizer paymentOptimizer = new PaymentOptimizer(orders, methods);
            paymentOptimizer.optimize();
            paymentOptimizer.printSpent();

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}