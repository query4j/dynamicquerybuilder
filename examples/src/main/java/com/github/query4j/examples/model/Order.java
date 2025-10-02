package com.github.query4j.examples.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Example Order entity for Query4j documentation and examples.
 */
public class Order {
    
    private Long id;
    private String orderNumber;
    private LocalDate orderDate;
    private String status;
    private BigDecimal totalAmount;
    private String currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Foreign keys
    private Long customerId;
    
    // Associations
    private User customer;
    private List<OrderItem> orderItems;
    private Product product; // For simplified examples
    
    // Constructors
    public Order() {}
    
    public Order(String orderNumber, LocalDate orderDate, Long customerId) {
        this.orderNumber = orderNumber;
        this.orderDate = orderDate;
        this.customerId = customerId;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    
    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    
    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }
    
    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }
    
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    
    @Override
    public String toString() {
        return String.format("Order{id=%d, orderNumber='%s', status='%s', totalAmount=%s}",
            id, orderNumber, status, totalAmount);
    }
}