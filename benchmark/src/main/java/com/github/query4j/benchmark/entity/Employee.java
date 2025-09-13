package com.github.query4j.benchmark.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JPA Entity for pagination benchmarks.
 * Represents a realistic employee record for comparing pagination performance
 * between DynamicQueryBuilder and baseline libraries.
 */
@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    
    @Column(nullable = false, unique = true, length = 255)
    private String email;
    
    @Column(nullable = false, length = 100)
    private String department;
    
    @Column(nullable = false, length = 50)
    private String role;
    
    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal salary;
    
    @Column(nullable = false)
    private Boolean active;
    
    @Column(length = 50)
    private String city;
    
    @Column(length = 50)
    private String country;
    
    /**
     * Constructor for benchmark data creation.
     */
    public Employee(String firstName, String lastName, String email, String department, 
                   String role, LocalDate hireDate, BigDecimal salary, Boolean active,
                   String city, String country) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.department = department;
        this.role = role;
        this.hireDate = hireDate;
        this.salary = salary;
        this.active = active;
        this.city = city;
        this.country = country;
    }
}