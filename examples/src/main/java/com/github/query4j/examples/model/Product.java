package com.github.query4j.examples.model;

import java.math.BigDecimal;

/**
 * Example Product entity for Query4j documentation and examples.
 */
public class Product {
    
    private Long id;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private String currency;
    private Boolean featured;
    private Boolean active;
    private Integer stockQuantity;
    
    // Constructors
    public Product() {}
    
    public Product(String name, String category, BigDecimal price) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.currency = "USD";
        this.active = true;
        this.featured = false;
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public Boolean getFeatured() { return featured; }
    public void setFeatured(Boolean featured) { this.featured = featured; }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    
    @Override
    public String toString() {
        return String.format("Product{id=%d, name='%s', category='%s', price=%s}",
            id, name, category, price);
    }
}