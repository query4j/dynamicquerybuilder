package com.github.query4j.benchmark;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Simple POJO for DynamicQueryBuilder that matches the employees table structure.
 * Uses field names that map directly to database column names.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeTable {
    
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