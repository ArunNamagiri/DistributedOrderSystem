
package com.example.orders;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.Objects;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @PutMapping("/{orderId}/status")
    public Order updateOrderStatus(
            @PathVariable @org.springframework.lang.NonNull String orderId,
            @RequestBody Map<String, String> request) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));

        String status = request.get("status");

        if (status == null || status.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Status is required");
        }

        status = status.trim().toUpperCase();

        if (!List.of("ACCEPTED", "PROCESSING", "COMPLETED", "FAILED")
                .contains(status)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid order status");
        }

        order.setStatus(status);

        Order updatedOrder = orderRepository.save(order);

        // Remove outdated cached order
        redisTemplate.delete("order:" + orderId);

        return updatedOrder;
    }

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private final OrderRepository orderRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public OrderController(
            KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate,
            OrderRepository orderRepository,
            RedisTemplate<String, Object> redisTemplate) {

        this.kafkaTemplate = kafkaTemplate;
        this.orderRepository = orderRepository;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, Object> createOrder(
            @RequestBody CreateOrderRequest request) {

        if (request.productId() == null || request.productId().isBlank()
                || request.quantity() <= 0
                || request.customerEmail() == null
                || request.customerEmail().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "productId, positive quantity, and customerEmail are required");
        }

        String orderId = UUID.randomUUID().toString();

        // 1. Create order
        Order order = new Order();
        order.setOrderId(orderId);
        order.setProductId(request.productId());
        order.setQuantity(request.quantity());
        order.setCustomerEmail(request.customerEmail());
        order.setStatus("ACCEPTED");
        order.setCreatedAt(LocalDateTime.now());

        // 2. Save order in MySQL
        orderRepository.save(order);

        // 3. Publish event to Kafka
        OrderCreatedEvent event = new OrderCreatedEvent(
                orderId,
                request.productId(),
                request.quantity(),
                request.customerEmail());

        kafkaTemplate.send(
                "orders.created",
                Objects.requireNonNull(orderId),
                event);

        return Map.of(
                "orderId", orderId,
                "status", "ACCEPTED",
                "message", "Order saved and event published to Kafka.");
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @GetMapping("/{orderId}")
    public Order getOrder(
            @PathVariable @org.springframework.lang.NonNull String orderId) {

        String key = "order:" + orderId;

        // 1. Check Redis
        Object cachedObject = redisTemplate.opsForValue().get(key);

        if (cachedObject instanceof Map<?, ?> cachedMap) {
            Order cachedOrder = new Order();

            cachedOrder.setOrderId((String) cachedMap.get("orderId"));
            cachedOrder.setProductId((String) cachedMap.get("productId"));
            cachedOrder.setQuantity((Integer) cachedMap.get("quantity"));
            cachedOrder.setCustomerEmail((String) cachedMap.get("customerEmail"));
            cachedOrder.setStatus((String) cachedMap.get("status"));

            Object createdAt = cachedMap.get("createdAt");

            if (createdAt instanceof String date) {
                cachedOrder.setCreatedAt(LocalDateTime.parse(date));
            }

            System.out.println("Redis Cache HIT: " + orderId);
            return cachedOrder;
        }

        // 2. Fetch from MySQL
        System.out.println("Redis Cache MISS: " + orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));

        // 3. Store in Redis for 10 minutes
        redisTemplate.opsForValue().set(
                key, Objects.requireNonNull(order), 10, TimeUnit.MINUTES);

        return order;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("service", "order-service", "status", "UP");
    }
}
