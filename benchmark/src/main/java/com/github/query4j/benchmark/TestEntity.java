package com.github.query4j.benchmark;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.math.BigDecimal;

/**
 * Test entity for benchmarking Query4j performance.
 * Represents a realistic domain model for performance testing.
 * 
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestEntity {
    
    private Long id;
    private String name;
    private String email;
    private Integer age;
    private String department;
    private String role;
    private Boolean active;
    private LocalDate joinDate;
    private BigDecimal salary;
    private String status;
    private String region;
    private Integer experience;
    
    /**
     * Creates a sample test entity for benchmarking.
     */
    public static TestEntity sample() {
        return new TestEntity(
            1L,
            "John Doe", 
            "john.doe@example.com",
            30,
            "Engineering",
            "Developer",
            true,
            LocalDate.of(2020, 1, 15),
            new BigDecimal("75000.00"),
            "ACTIVE",
            "US-WEST",
            5
        );
    }
}