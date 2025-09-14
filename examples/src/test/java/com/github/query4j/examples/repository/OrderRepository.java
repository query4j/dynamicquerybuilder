package com.github.query4j.examples.repository;

import com.github.query4j.examples.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for Order entities.
 * Provides standard CRUD operations and custom queries.
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * Find orders by customer ID.
     */
    List<Order> findByCustomerId(Long customerId);
    
    /**
     * Find orders by status.
     */
    List<Order> findByStatus(String status);
    
    /**
     * Find orders placed within date range.
     */
    List<Order> findByPlacedAtBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * Find orders with total above threshold.
     */
    @Query("SELECT o FROM Order o WHERE o.total > :threshold")
    List<Order> findOrdersWithTotalAbove(@Param("threshold") BigDecimal threshold);
    
    /**
     * Calculate total sales for a customer.
     */
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.customer.id = :customerId")
    BigDecimal getTotalSalesForCustomer(@Param("customerId") Long customerId);
}