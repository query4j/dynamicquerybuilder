package com.github.query4j.benchmark;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity class for DynamicQueryBuilder benchmarks.
 * Uses lowercase class name to match the database table name 'employees'.
 * Field names match the database column names for direct mapping.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class employees {
    
    private Long id;
    private String first_name;
    private String last_name;
    private String email;
    private String department;
    private String role;
    private LocalDate hire_date;
    private BigDecimal salary;
    private Boolean active;
    private String city;
    private String country;
}