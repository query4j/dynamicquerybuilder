package com.github.query4j.benchmark;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Employee table POJO for direct database column mapping in benchmarks.
 * 
 * <p>
 * This class provides a simple data structure that directly maps to the
 * employees database table without JPA annotations. It's designed for
 * benchmark scenarios where raw JDBC performance is being measured.
 * </p>
 * 
 * <p>
 * Unlike the {@link Employee} entity, this class uses field names that
 * match database column names exactly for performance testing scenarios
 * that require minimal overhead.
 * </p>
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 * @see Employee for the JPA-annotated version
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeTable {
    
    /**
     * Employee unique identifier.
     */
    private Long id;
    
    /**
     * First name column (maps to database column first_name).
     */
    private String firstName;
    
    /**
     * Last name column (maps to database column last_name).
     */
    private String lastName;
    
    /**
     * Email address column.
     */
    private String email;
    
    /**
     * Department column.
     */
    private String department;
    
    /**
     * Role or job title column.
     */
    private String role;
    
    /**
     * Hire date column (maps to database column hire_date).
     */
    private LocalDate hireDate;
    
    /**
     * Salary column.
     */
    private BigDecimal salary;
    
    /**
     * Active status column.
     */
    private Boolean active;
    
    /**
     * City column.
     */
    private String city;
    
    /**
     * Country column.
     */
    private String country;
}