package com.github.query4j.benchmark;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Benchmark entity representing employee data for performance testing.
 * 
 * <p>
 * This entity is specifically designed for benchmarking Query4j performance
 * against various database operations including filtering, sorting, and pagination.
 * Field names follow Java naming conventions with JPA annotations mapping
 * to the appropriate database column names.
 * </p>
 * 
 * <p>
 * The entity contains realistic employee data including:
 * </p>
 * <ul>
 * <li>Personal information (first name, last name, email)</li>
 * <li>Employment details (department, role, hire date, salary)</li>
 * <li>Status information (active status)</li>
 * <li>Location data (city, country)</li>
 * </ul>
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    
    /**
     * Unique identifier for the employee.
     */
    private Long id;
    
    /**
     * Employee's first name (maps to database column first_name).
     */
    private String firstName;
    
    /**
     * Employee's last name (maps to database column last_name).
     */
    private String lastName;
    
    /**
     * Employee's email address.
     */
    private String email;
    
    /**
     * Department where the employee works.
     */
    private String department;
    
    /**
     * Employee's role or job title.
     */
    private String role;
    
    /**
     * Date when the employee was hired (maps to database column hire_date).
     */
    private LocalDate hireDate;
    
    /**
     * Employee's current salary.
     */
    private BigDecimal salary;
    
    /**
     * Whether the employee is currently active.
     */
    private Boolean active;
    
    /**
     * City where the employee is located.
     */
    private String city;
    
    /**
     * Country where the employee is located.
     */
    private String country;
}