package com.example.orders;

public record OrderCreatedEvent(String orderId, String productId, int quantity, String customerEmail) {}
