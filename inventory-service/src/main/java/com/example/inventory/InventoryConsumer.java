package com.example.inventory;

import com.example.orders.OrderCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryConsumer {
    @KafkaListener(topics = "orders.created")
    public void reserveInventory(OrderCreatedEvent event) {

        if (event.productId().equals("FAIL-TEST")) {
            System.out.println("Simulating inventory processing failure...");
            throw new RuntimeException("Simulated inventory failure");
        }

        System.out.printf(
                "[inventory-service] Received order=%s, product=%s, quantity=%d. Simulated stock reservation.%n",
                event.orderId(), event.productId(), event.quantity());
    }
}
