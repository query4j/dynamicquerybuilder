package com.github.query4j.examples.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Customer entity for Spring Boot integration testing.
 * Represents a customer with orders in the e-commerce domain model.
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
@Entity
@Table(name = "customers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "orders")
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    
    @Column(nullable = false, length = 255)
    private String name;
    
    @Column(nullable = false, length = 100)
    private String region;
    
    @Column(length = 255)
    private String email;
    
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    
    @Column(name = "credit_limit")
    private Double creditLimit;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
    
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;
}