package com.github.query4j.examples.model;

import java.math.BigDecimal;

/**
 * Example OrderItem entity for Query4j documentation and examples.
 */
public class OrderItem {
    
    private Long id;
    private Long orderId;
    private Long productId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    
    // Associations
    private Order order;
    private Product product;
    
    // Constructors
    public OrderItem() {}
    
    public OrderItem(Long orderId, Long productId, Integer quantity, BigDecimal unitPrice) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    
    @Override
    public String toString() {
        return String.format("OrderItem{id=%d, orderId=%d, productId=%d, quantity=%d, totalPrice=%s}",
            id, orderId, productId, quantity, totalPrice);
    }
}