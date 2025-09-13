package com.github.query4j.optimizer;

import com.github.query4j.core.QueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for QueryOptimizer with real QueryBuilder instances.
 * Demonstrates end-to-end optimization scenarios.
 */
class QueryOptimizerIntegrationTest {
    
    private QueryOptimizer optimizer;
    
    @BeforeEach
    void setUp() {
        optimizer = QueryOptimizer.create(OptimizerConfig.developmentConfig());
    }
    
    @Test
    @DisplayName("Should optimize simple user query with email predicate")
    void optimizeSimpleUserQuery() {
        // Create a real QueryBuilder for user entity
        QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
                .where("email", "john@example.com")
                .and()
                .where("status", "ACTIVE");
        
        OptimizationResult result = optimizer.optimize(query);
        
        assertThat(result).isNotNull();
        assertThat(result.getSummary()).isNotNull();
        assertThat(result.getAnalysisTimeMs()).isGreaterThanOrEqualTo(0);
        
        // Should get some index suggestions for email and status fields
        // (The actual suggestions depend on the implementation details)
        assertThat(result.getIndexSuggestions()).isNotNull();
        assertThat(result.getPredicatePushdownSuggestions()).isNotNull();
        assertThat(result.getJoinReorderSuggestions()).isNotNull();
        
        System.out.println("Optimization Summary: " + result.getSummary());
        System.out.println("Total Suggestions: " + result.getTotalSuggestionCount());
    }
    
    @Test
    @DisplayName("Should optimize complex query with joins and multiple predicates")
    void optimizeComplexQueryWithJoins() {
        QueryBuilder<User> complexQuery = QueryBuilder.forEntity(User.class)
                .where("department", "Engineering")
                .and()
                .where("salary", ">", 50000)
                .and()
                .where("active", true)
                .join("orders")
                .where("orders.status", "PENDING")
                .and()
                .where("orders.amount", ">", 100)
                .orderBy("lastName")
                .limit(50);
        
        OptimizationResult result = optimizer.optimize(complexQuery);
        
        assertThat(result).isNotNull();
        assertThat(result.hasSuggestions()).isEqualTo(result.getTotalSuggestionCount() > 0);
        
        // Log all suggestions for manual verification
        result.getIndexSuggestions().forEach(suggestion -> {
            System.out.println("Index Suggestion: " + suggestion.generateCreateIndexSQL());
            System.out.println("  Reason: " + suggestion.getReason());
            System.out.println("  Priority: " + suggestion.getPriority());
            System.out.println("  Selectivity: " + suggestion.getSelectivity());
        });
        
        result.getPredicatePushdownSuggestions().forEach(suggestion -> {
            System.out.println("Predicate Optimization: " + suggestion.getOptimizationType());
            System.out.println("  Reason: " + suggestion.getReason());
            System.out.println("  Position: " + suggestion.getOriginalPosition() + 
                             " -> " + suggestion.getSuggestedPosition());
        });
        
        result.getJoinReorderSuggestions().forEach(suggestion -> {
            System.out.println("Join Reordering: " + suggestion.getReorderType());
            System.out.println("  Original: " + suggestion.getOriginalJoinSequence());
            System.out.println("  Suggested: " + suggestion.getSuggestedJoinSequence());
            System.out.println("  Improvement: " + (suggestion.getEstimatedImprovement() * 100) + "%");
        });
    }
    
    @Test
    @DisplayName("Should handle different optimizer configurations")
    void optimizeWithDifferentConfigurations() {
        QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
                .where("email", "test@example.com")
                .where("status", "ACTIVE");
        
        // Test with high-performance configuration
        QueryOptimizer highPerfOptimizer = QueryOptimizer.create(
                OptimizerConfig.highPerformanceConfig());
        OptimizationResult highPerfResult = highPerfOptimizer.optimize(query);
        
        // Test with default configuration
        QueryOptimizer defaultOptimizer = QueryOptimizer.create();
        OptimizationResult defaultResult = defaultOptimizer.optimize(query);
        
        // Test with index-only configuration
        OptimizerConfig indexOnlyConfig = OptimizerConfig.builder()
                .indexSuggestionsEnabled(true)
                .predicatePushdownEnabled(false)
                .joinReorderingEnabled(false)
                .build();
        QueryOptimizer indexOnlyOptimizer = QueryOptimizer.create(indexOnlyConfig);
        OptimizationResult indexOnlyResult = indexOnlyOptimizer.optimize(query);
        
        // All should return valid results
        assertThat(highPerfResult).isNotNull();
        assertThat(defaultResult).isNotNull();
        assertThat(indexOnlyResult).isNotNull();
        
        // Index-only configuration should not have predicate or join suggestions
        assertThat(indexOnlyResult.getPredicatePushdownSuggestions()).isEmpty();
        assertThat(indexOnlyResult.getJoinReorderSuggestions()).isEmpty();
    }
    
    @Test
    @DisplayName("Should demonstrate SQL optimization capabilities")
    void optimizeSQLString() {
        String sql = "SELECT u.id, u.name, o.total FROM users u " +
                    "JOIN orders o ON u.id = o.user_id " +
                    "WHERE u.email = 'test@example.com' AND o.status = 'PENDING'";
        
        OptimizationResult result = optimizer.optimize(sql);
        
        assertThat(result).isNotNull();
        assertThat(result.getSummary()).contains("SQL string analysis");
        
        // SQL analysis is currently limited - this demonstrates the API
        System.out.println("SQL Analysis Summary: " + result.getSummary());
    }
    
    @Test
    @DisplayName("Should handle optimization with custom table statistics")
    void optimizeWithCustomStatistics() {
        // This test demonstrates how custom statistics could be used
        // In a real application, you would provide actual database statistics
        
        QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
                .where("department_id", 1)
                .join("departments")
                .where("departments.budget", ">", 100000);
        
        OptimizationResult result = optimizer.optimize(query);
        
        // The optimizer should work even without custom statistics
        assertThat(result).isNotNull();
        assertThat(result.getAnalysisTimeMs()).isLessThan(5000); // Should complete quickly
    }
    
    // Test entity class
    public static class User {
        private Long id;
        private String email;
        private String status;
        private String department;
        private Double salary;
        private Boolean active;
        private String lastName;
        private Long departmentId;
        
        // In a real application, this would have proper JPA annotations
        // and getter/setter methods
    }
}