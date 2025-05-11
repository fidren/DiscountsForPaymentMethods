package org.example;

import java.math.BigDecimal;

public record Option(String orderId, String cardId, BigDecimal discount) {}