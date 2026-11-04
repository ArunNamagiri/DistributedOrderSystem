package com.example.orders;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    private String orderId;

    @Column(nullable = false)
    private String productId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private String customerEmail;

    @Column(nullable = false)
    private String status;

    private LocalDateTime createdAt;

    public Order() {
    }

    public Order(String orderId, String productId, int quantity,
                 String customerEmail, String status,
                 LocalDateTime createdAt) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.customerEmail = customerEmail;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getOrderId() { return orderId; }
    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public String getCustomerEmail() { return customerEmail; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setProductId(String productId) { this.productId = productId; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}