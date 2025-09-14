package com.github.query4j.examples.repository;

import com.github.query4j.examples.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for Customer entities.
 * Provides standard CRUD operations and custom queries.
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    /**
     * Find customers by region.
     */
    List<Customer> findByRegion(String region);
    
    /**
     * Find active customers by region.
     */
    List<Customer> findByRegionAndActiveTrue(String region);
    
    /**
     * Find customers with credit limit above threshold.
     */
    @Query("SELECT c FROM Customer c WHERE c.creditLimit > :threshold")
    List<Customer> findCustomersWithCreditLimitAbove(@Param("threshold") Double threshold);
    
    /**
     * Count customers by region.
     */
    Long countByRegion(String region);
}