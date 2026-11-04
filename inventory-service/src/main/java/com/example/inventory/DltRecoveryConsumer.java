
package com.example.inventory;

import com.example.orders.OrderCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DltRecoveryConsumer {

    @KafkaListener(
        topics = "orders.created.DLT",
        groupId = "inventory-dlt-recovery"
    )
    public void recoverFailedOrder(OrderCreatedEvent event) {

        System.out.println("===== DLT RECOVERY =====");

        System.out.printf(
            "Failed order received: orderId=%s, productId=%s, quantity=%d, customerEmail=%s%n",
            event.orderId(),
            event.productId(),
            event.quantity(),
            event.customerEmail()
        );

        System.out.println("Order is available for manual recovery.");
    }
}
