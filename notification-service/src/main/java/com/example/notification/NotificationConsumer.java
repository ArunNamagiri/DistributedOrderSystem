package com.example.notification;

import com.example.orders.OrderCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {
    @KafkaListener(topics = "orders.created")
    public void sendConfirmation(OrderCreatedEvent event) {
        System.out.printf("[notification-service] Simulated confirmation to %s for order=%s.%n",
                event.customerEmail(), event.orderId());
    }
}
