package com.example.inventory;

import com.example.orders.OrderCreatedEvent;
import java.util.Objects;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recovery")
public class DltReplayController {

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public DltReplayController(
            KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping("/replay")
    public String replayOrder(
            @RequestBody OrderCreatedEvent event) {
        String orderId = Objects.requireNonNull(event.orderId(), "event.orderId");

        kafkaTemplate.send(
                "orders.created",
                orderId,
                event);

        return "Order event submitted for replay: " + orderId;
    }
}