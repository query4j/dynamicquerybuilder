package com.github.query4j.test;

import com.github.query4j.core.QueryBuilder;
import com.github.query4j.core.criteria.HavingPredicate;
import com.github.query4j.core.impl.DynamicQueryBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests that verify end-to-end functionality
 * across all modules in the Query4j ecosystem.
 */
public class MainTest {

    @Test
    @DisplayName("should load context successfully")
    public void contextLoads() {
        // Simple test to verify build and test framework setup
    }

    @Test
    @DisplayName("should create query builder and build complex queries")
    public void shouldCreateQueryBuilderAndBuildComplexQueries() {
        // Integration test that exercises core functionality
        DynamicQueryBuilder<TestEntity> builder = new DynamicQueryBuilder<>(TestEntity.class);
        
        String sql = builder
            .where("status", "ACTIVE")
            .and()
            .where("age", ">", 18)
            .or()
            .where("role", "ADMIN")
            .orderBy("created_date", false)
            .limit(10)
            .toSQL();
        
        assertNotNull(sql);
        assertTrue(sql.contains("SELECT"));
        assertTrue(sql.contains("WHERE"));
        assertTrue(sql.contains("ORDER BY"));
        assertTrue(sql.contains("LIMIT"));
    }

    @Test
    @DisplayName("should support aggregation with HAVING clauses")
    public void shouldSupportAggregationWithHavingClauses() {
        // Test that the HAVING clause functionality works end-to-end
        DynamicQueryBuilder<TestEntity> builder = new DynamicQueryBuilder<>(TestEntity.class);
        
        // Build a query with aggregation and HAVING - using separate calls since select() has different validation
        builder = (DynamicQueryBuilder<TestEntity>) builder
            .where("status", "ACTIVE")
            .groupBy("department")
            .having("COUNT(id)", ">", 5)
            .orderBy("department");
        
        String sql = builder.toSQL();
        
        assertNotNull(sql);
        assertTrue(sql.contains("WHERE"));
        assertTrue(sql.contains("GROUP BY department"));
        assertTrue(sql.contains("HAVING COUNT(id) > :"));
        assertTrue(sql.contains("ORDER BY department"));
        
        // Verify parameters are generated correctly
        assertFalse(builder.getPredicates().isEmpty());
    }

    @Test
    @DisplayName("should work with factory method")
    public void shouldWorkWithFactoryMethod() {
        // Test that the public API factory method works correctly
        var builder = QueryBuilder.forEntity(TestEntity.class);
        
        assertNotNull(builder);
        assertInstanceOf(DynamicQueryBuilder.class, builder);
        
        String sql = builder
            .whereIn("category", java.util.Arrays.asList("A", "B", "C"))
            .and()
            .whereLike("name", "%test%")
            .toSQL();
        
        assertTrue(sql.contains("category IN"));
        assertTrue(sql.contains("LIKE"));
    }

    @Test
    @DisplayName("should handle HavingPredicate integration")
    public void shouldHandleHavingPredicateIntegration() {
        // Verify that HavingPredicate integrates correctly with the builder
        HavingPredicate predicate = new HavingPredicate("SUM(amount)", ">=", 1000, "sum_param");
        
        assertEquals("SUM(amount) >= :sum_param", predicate.toSQL());
        assertEquals(1000, predicate.getParameters().get("sum_param"));
        
        // Test immutability
        var params = predicate.getParameters();
        assertThrows(UnsupportedOperationException.class, () -> 
            params.put("new_key", "new_value"));
    }

    // Simple test entity for testing
    public static class TestEntity {
        private String status;
        private int age;
        private String role;
        private String department;
        private String category;
        private String name;
        
        // Getters and setters would be here in real implementation
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
