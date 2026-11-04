package com.example.orders;

public record CreateOrderRequest(String productId, int quantity, String customerEmail) {}
