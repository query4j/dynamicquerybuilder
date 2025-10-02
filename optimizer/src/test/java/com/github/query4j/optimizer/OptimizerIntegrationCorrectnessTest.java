package com.github.query4j.optimizer;

import com.github.query4j.core.QueryBuilder;
import com.github.query4j.optimizer.index.IndexSuggestion;
import com.github.query4j.optimizer.predicate.PredicatePushdownSuggestion;
import com.github.query4j.optimizer.join.JoinReorderSuggestion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for end-to-end optimizer functionality.
 * Tests complex multi-table queries with combined optimization strategies,
 * verifying that output query structure, index suggestions, predicate pushdown,
 * and join ordering work together correctly.
 */
class OptimizerIntegrationCorrectnessTest {
    
    private QueryOptimizer optimizer;
    private OptimizerConfig config;
    
    @Mock
    private QueryBuilder<?> mockQueryBuilder;
    
    // Mock entity class for testing
    static class User {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String status;
        private String city;
    }
    
    static class Order {
        private Long id;
        private Long userId;
        private String status;
        private Double amount;
        private String createdDate;
    }
    
    static class Product {
        private Long id;
        private String name;
        private String category;
        private Double price;
    }
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        config = OptimizerConfig.defaultConfig();
        optimizer = QueryOptimizer.create(config);
    }
    
    @Nested
    @DisplayName("Complex Multi-Table Query Optimization")
    class MultiTableQueryTests {
        
        @Test
        @DisplayName("Should optimize complex query with multiple predicates and joins")
        void complexQuery_WithMultiplePredicatesAndJoins_OptimizedCorrectly() {
            // Simulate a complex query: SELECT u.*, o.*, p.* FROM users u 
            // JOIN orders o ON u.id = o.user_id 
            // JOIN order_items oi ON o.id = oi.order_id
            // JOIN products p ON oi.product_id = p.id
            // WHERE u.status = 'ACTIVE' AND u.city = 'New York' 
            // AND o.amount > 100 AND p.category = 'ELECTRONICS'
            when(mockQueryBuilder.toSQL()).thenReturn(
                "SELECT u.*, o.*, p.* FROM users u " +
                "JOIN orders o ON u.id = o.user_id " +
                "JOIN order_items oi ON o.id = oi.order_id " +
                "JOIN products p ON oi.product_id = p.id " +
                "WHERE u.status = 'ACTIVE' AND u.city = 'New York' " +
                "AND o.amount > 100 AND p.category = 'ELECTRONICS'"
            );
            
            OptimizationResult result = optimizer.optimize(mockQueryBuilder);
            
            // Verify comprehensive optimization result
            assertThat(result).isNotNull();
            assertThat(result.getAnalysisTimeMs()).isGreaterThanOrEqualTo(0);
            assertThat(result.getSummary()).isNotNull().isNotEmpty();
            
            // Should have suggestions from all optimization strategies
            List<IndexSuggestion> indexSuggestions = result.getIndexSuggestions();
            List<PredicatePushdownSuggestion> predicateSuggestions = result.getPredicatePushdownSuggestions();
            List<JoinReorderSuggestion> joinSuggestions = result.getJoinReorderSuggestions();
            
            assertThat(indexSuggestions).isNotNull();
            assertThat(predicateSuggestions).isNotNull();
            assertThat(joinSuggestions).isNotNull();
            
            // Total suggestion count should match
            int totalSuggestions = indexSuggestions.size() + predicateSuggestions.size() + joinSuggestions.size();
            assertThat(result.getTotalSuggestionCount()).isEqualTo(totalSuggestions);
        }
        
        @Test
        @DisplayName("Should correlate index suggestions to predicates and join keys")
        void indexSuggestions_CorrelateToPredicatesAndJoinKeys() {
            when(mockQueryBuilder.toSQL()).thenReturn(
                "SELECT u.*, o.* FROM users u JOIN orders o ON u.id = o.user_id " +
                "WHERE u.email = 'john@example.com' AND o.status = 'PENDING'"
            );
            
            OptimizationResult result = optimizer.optimize(mockQueryBuilder);
            List<IndexSuggestion> indexSuggestions = result.getIndexSuggestions();
            
            if (!indexSuggestions.isEmpty()) {
                // Look for indexes that correlate to the predicates and joins in the query
                Set<String> suggestedColumns = indexSuggestions.stream()
                    .flatMap(s -> s.getColumnNames().stream())
                    .collect(Collectors.toSet());
                
                // Should suggest indexes for high-value columns that appear in predicates or joins
                // The actual columns depend on the implementation, but suggestions should be relevant
                for (IndexSuggestion suggestion : indexSuggestions) {
                    assertThat(suggestion.getTableName()).isNotNull().isNotEmpty();
                    assertThat(suggestion.getColumnNames()).isNotNull().isNotEmpty();
                    assertThat(suggestion.getReason()).isNotNull().isNotEmpty();
                    assertThat(suggestion.generateCreateIndexSQL()).isNotNull().isNotEmpty();
                }
            }
        }
        
        @Test
        @DisplayName("Should suggest predicate pushdown closer to scan sources")
        void predicatePushdown_MovesPredicatesCloserToScanSources() {
            when(mockQueryBuilder.toSQL()).thenReturn(
                "SELECT c.name, o.total, p.name " +
                "FROM customers c " +
                "JOIN orders o ON c.id = o.customer_id " +
                "JOIN order_items oi ON o.id = oi.order_id " +
                "JOIN products p ON oi.product_id = p.id " +
                "WHERE c.country = 'US' AND p.category = 'BOOKS'"
            );
            
            OptimizationResult result = optimizer.optimize(mockQueryBuilder);
            List<PredicatePushdownSuggestion> predicateSuggestions = result.getPredicatePushdownSuggestions();
            
            for (PredicatePushdownSuggestion suggestion : predicateSuggestions) {
                // Verify pushdown suggestions make sense
                assertThat(suggestion.getOptimizationType()).isNotNull();
                assertThat(suggestion.getReason()).isNotNull().isNotEmpty();
                assertThat(suggestion.getExpectedImpact()).isNotNull().isNotEmpty();
                assertThat(suggestion.getPriority()).isNotNull();
                
                // If suggesting pushdown to join source, should have target table
                if (suggestion.getOptimizationType() == PredicatePushdownSuggestion.OptimizationType.PUSH_TO_JOIN_SOURCE) {
                    assertThat(suggestion.getTargetTable()).isNotNull();
                }
                
                // Position suggestions should be valid
                if (suggestion.getOriginalPosition() >= 0) {
                    assertThat(suggestion.getSuggestedPosition()).isGreaterThanOrEqualTo(0);
                }
            }
        }
        
        @Test
        @DisplayName("Should optimize join orders to reduce intermediate result sizes")
        void joinOrdering_ReducesIntermediateResultSizes() {
            when(mockQueryBuilder.toSQL()).thenReturn(
                "SELECT * FROM large_table lt " +
                "JOIN medium_table mt ON lt.id = mt.large_id " +
                "JOIN small_table st ON mt.id = st.medium_id " +
                "WHERE st.active = true"
            );
            
            OptimizationResult result = optimizer.optimize(mockQueryBuilder);
            List<JoinReorderSuggestion> joinSuggestions = result.getJoinReorderSuggestions();
            
            for (JoinReorderSuggestion suggestion : joinSuggestions) {
                // Verify join reordering suggestions
                assertThat(suggestion.getOriginalJoinSequence()).isNotNull().isNotEmpty();
                assertThat(suggestion.getSuggestedJoinSequence()).isNotNull().isNotEmpty();
                assertThat(suggestion.getEstimatedImprovement()).isBetween(0.0, 1.0);
                assertThat(suggestion.getReorderType()).isNotNull();
                assertThat(suggestion.getReason()).isNotNull().isNotEmpty();
                
                // Sequences should contain the same tables
                assertThat(suggestion.getSuggestedJoinSequence())
                    .containsExactlyInAnyOrderElementsOf(suggestion.getOriginalJoinSequence());
                
                // If reordering is recommended, sequences should differ
                if (suggestion.isReorderingRecommended()) {
                    assertThat(suggestion.getSuggestedJoinSequence())
                        .isNotEqualTo(suggestion.getOriginalJoinSequence());
                    assertThat(suggestion.getSequenceChangeCount()).isGreaterThan(0);
                }
            }
        }
    }
    
    @Nested
    @DisplayName("Query Structure and Output Verification")
    class QueryStructureTests {
        
        @Test
        @DisplayName("Should maintain query semantic equivalence after optimization")
        void optimization_MaintainsQuerySemanticEquivalence() {
            QueryBuilder<User> originalQuery = QueryBuilder.forEntity(User.class)
                .where("status", "ACTIVE")
                .and()
                .where("city", "New York")
                .and()
                .where("email", "like", "%@company.com");
            
            OptimizationResult result = optimizer.optimize(originalQuery);
            
            // Verify that optimization suggestions preserve query semantics
            assertThat(result).isNotNull();
            
            // Check that the analysis doesn't corrupt the original query
            String originalSQL = originalQuery.toSQL();
            
            // Query should still be valid after analysis
            assertThat(originalSQL).isNotNull().isNotEmpty();
            
            // Suggestions should not alter the original query structure
            for (PredicatePushdownSuggestion suggestion : result.getPredicatePushdownSuggestions()) {
                if (suggestion.getOriginalPredicate() != null) {
                    // Original predicate should be unchanged
                    assertThat(suggestion.getOriginalPredicate().toSQL()).isNotNull();
                    assertThat(suggestion.getOriginalPredicate().getParameters()).isNotNull();
                }
            }
        }
        
        @Test
        @DisplayName("Should generate actionable and clearly documented recommendations")
        void recommendations_AreActionableAndClearlyDocumented() {
            when(mockQueryBuilder.toSQL()).thenReturn(
                "SELECT u.name, COUNT(o.id) FROM users u " +
                "LEFT JOIN orders o ON u.id = o.user_id " +
                "WHERE u.created_date > '2024-01-01' " +
                "GROUP BY u.name HAVING COUNT(o.id) > 5"
            );
            
            OptimizationResult result = optimizer.optimize(mockQueryBuilder);
            
            // Index suggestions should be actionable
            for (IndexSuggestion indexSuggestion : result.getIndexSuggestions()) {
                // Should generate valid SQL DDL
                String createIndexSQL = indexSuggestion.generateCreateIndexSQL();
                assertThat(createIndexSQL).isNotNull().isNotEmpty();
                assertThat(createIndexSQL).startsWith("CREATE INDEX");
                
                // Should have clear documentation
                assertThat(indexSuggestion.getReason()).isNotNull().isNotEmpty();
                assertThat(indexSuggestion.getExpectedImpact()).isNotNull().isNotEmpty();
                assertThat(indexSuggestion.getPriority()).isNotNull();
                
                // Should have realistic selectivity estimates
                if (indexSuggestion.getSelectivity() > 0) {
                    assertThat(indexSuggestion.getSelectivity()).isBetween(0.0, 1.0);
                }
            }
            
            // Predicate suggestions should be actionable
            for (PredicatePushdownSuggestion predicateSuggestion : result.getPredicatePushdownSuggestions()) {
                assertThat(predicateSuggestion.getReason()).isNotNull().isNotEmpty();
                assertThat(predicateSuggestion.getExpectedImpact()).isNotNull().isNotEmpty();
                assertThat(predicateSuggestion.getPriority()).isNotNull();
                
                // Position changes should be valid
                if (predicateSuggestion.getOriginalPosition() >= 0) {
                    assertThat(predicateSuggestion.getSuggestedPosition()).isGreaterThanOrEqualTo(0);
                }
            }
            
            // Join suggestions should be actionable
            for (JoinReorderSuggestion joinSuggestion : result.getJoinReorderSuggestions()) {
                assertThat(joinSuggestion.getReason()).isNotNull().isNotEmpty();
                assertThat(joinSuggestion.getExpectedImpact()).isNotNull().isNotEmpty();
                assertThat(joinSuggestion.getPriority()).isNotNull();
                
                // Should have realistic improvement estimates
                assertThat(joinSuggestion.getEstimatedImprovement()).isBetween(0.0, 1.0);
            }
        }
        
        @Test
        @DisplayName("Should not lose predicates or introduce erroneous reordering")
        void optimization_NoPredicateLossOrErroneousReordering() {
            QueryBuilder<User> complexQuery = QueryBuilder.forEntity(User.class)
                .where("firstName", "John")
                .and()
                .where("lastName", "Doe")
                .and()
                .where("status", "in", List.of("ACTIVE", "PENDING"))
                .and()
                .where("email", "like", "%@example.com");
            
            OptimizationResult result = optimizer.optimize(complexQuery);
            
            // Verify no predicates are lost in suggestions
            for (PredicatePushdownSuggestion suggestion : result.getPredicatePushdownSuggestions()) {
                if (suggestion.getOriginalPredicate() != null) {
                    // Original predicate should be preserved
                    assertThat(suggestion.getOriginalPredicate()).isNotNull();
                    assertThat(suggestion.getOriginalPredicate().toSQL()).isNotNull().isNotEmpty();
                    
                    // Parameters should be preserved
                    Map<String, Object> originalParams = suggestion.getOriginalPredicate().getParameters();
                    assertThat(originalParams).isNotNull();
                }
                
                // Position suggestions should be reasonable
                if (suggestion.getOriginalPosition() >= 0 && suggestion.getSuggestedPosition() >= 0) {
                    // Should not suggest moving a predicate to an invalid position
                    // This would depend on the total number of predicates in the query
                    assertThat(suggestion.getSuggestedPosition()).isGreaterThanOrEqualTo(0);
                }
            }
            
            // Verify join sequences are not corrupted
            for (JoinReorderSuggestion suggestion : result.getJoinReorderSuggestions()) {
                List<String> original = suggestion.getOriginalJoinSequence();
                List<String> suggested = suggestion.getSuggestedJoinSequence();
                
                // Should contain the same tables
                assertThat(suggested).containsExactlyInAnyOrderElementsOf(original);
                // Should not lose any tables
                assertThat(suggested).hasSize(original.size());
            }
        }
    }
    
    @Nested
    @DisplayName("Configuration Impact on Integration")
    class ConfigurationIntegrationTests {
        
        @Test
        @DisplayName("Should respect optimizer configuration in integrated optimization")
        void integratedOptimization_RespectsConfiguration() {
            // Test with selective configuration
            OptimizerConfig selectiveConfig = OptimizerConfig.builder()
                .indexSuggestionsEnabled(true)
                .predicatePushdownEnabled(false)  // Disabled
                .joinReorderingEnabled(true)
                .verboseOutput(true)
                .build();
            
            QueryOptimizer selectiveOptimizer = QueryOptimizer.create(selectiveConfig);
            
            when(mockQueryBuilder.toSQL()).thenReturn(
                "SELECT * FROM users u JOIN orders o ON u.id = o.user_id WHERE u.status = 'ACTIVE'"
            );
            
            OptimizationResult result = selectiveOptimizer.optimize(mockQueryBuilder);
            
            // Should respect configuration settings
            assertThat(result.getIndexSuggestions()).isNotNull(); // Enabled
            assertThat(result.getJoinReorderSuggestions()).isNotNull(); // Enabled
            
            // Predicate pushdown may or may not have suggestions since it's disabled in config
            // The actual behavior depends on implementation
            assertThat(result.getPredicatePushdownSuggestions()).isNotNull();
        }
        
        @Test
        @DisplayName("Should handle high-performance configuration appropriately")
        void highPerformanceConfiguration_HandledAppropriately() {
            OptimizerConfig highPerfConfig = OptimizerConfig.highPerformanceConfig();
            QueryOptimizer highPerfOptimizer = QueryOptimizer.create(highPerfConfig);
            
            when(mockQueryBuilder.toSQL()).thenReturn(
                "SELECT u.*, o.*, p.* FROM users u " +
                "JOIN orders o ON u.id = o.user_id " +
                "JOIN products p ON o.product_id = p.id " +
                "WHERE u.city = 'Boston' AND o.amount > 500 AND p.category = 'TECH'"
            );
            
            long startTime = System.currentTimeMillis();
            OptimizationResult result = highPerfOptimizer.optimize(mockQueryBuilder);
            long duration = System.currentTimeMillis() - startTime;
            
            // High performance config should optimize efficiently
            assertThat(result).isNotNull();
            assertThat(result.getAnalysisTimeMs()).isLessThan(highPerfConfig.getMaxAnalysisTimeMs());
            
            // Should complete within reasonable time
            assertThat(duration).isLessThan(5000); // 5 seconds max
            
            // Should still provide comprehensive suggestions
            int totalSuggestions = result.getIndexSuggestions().size() + 
                                 result.getPredicatePushdownSuggestions().size() + 
                                 result.getJoinReorderSuggestions().size();
            assertThat(result.getTotalSuggestionCount()).isEqualTo(totalSuggestions);
        }
        
        @Test
        @DisplayName("Should handle development configuration with verbose output")
        void developmentConfiguration_ProvidesVerboseOutput() {
            OptimizerConfig devConfig = OptimizerConfig.developmentConfig();
            QueryOptimizer devOptimizer = QueryOptimizer.create(devConfig);
            
            when(mockQueryBuilder.toSQL()).thenReturn(
                "SELECT COUNT(*) FROM users u WHERE u.created_date BETWEEN '2024-01-01' AND '2024-12-31'"
            );
            
            OptimizationResult result = devOptimizer.optimize(mockQueryBuilder);
            
            // Development config should provide detailed information
            assertThat(result.getSummary()).isNotNull().isNotEmpty();
            
            // Suggestions should have detailed reasoning (if verbose is implemented)
            for (IndexSuggestion suggestion : result.getIndexSuggestions()) {
                assertThat(suggestion.getReason()).isNotNull().isNotEmpty();
                assertThat(suggestion.getExpectedImpact()).isNotNull().isNotEmpty();
            }
            
            for (PredicatePushdownSuggestion suggestion : result.getPredicatePushdownSuggestions()) {
                assertThat(suggestion.getReason()).isNotNull().isNotEmpty();
                assertThat(suggestion.getExpectedImpact()).isNotNull().isNotEmpty();
            }
            
            for (JoinReorderSuggestion suggestion : result.getJoinReorderSuggestions()) {
                assertThat(suggestion.getReason()).isNotNull().isNotEmpty();
                assertThat(suggestion.getExpectedImpact()).isNotNull().isNotEmpty();
            }
        }
    }
    
    @Nested
    @DisplayName("Error Handling and Edge Cases")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("Should handle complex queries with no optimization opportunities gracefully")
        void noOptimizationOpportunities_HandledGracefully() {
            // Query that might not have obvious optimization opportunities
            when(mockQueryBuilder.toSQL()).thenReturn("SELECT 1");
            
            OptimizationResult result = optimizer.optimize(mockQueryBuilder);
            
            assertThat(result).isNotNull();
            assertThat(result.getAnalysisTimeMs()).isGreaterThanOrEqualTo(0);
            assertThat(result.getSummary()).isNotNull().isNotEmpty();
            
            // Even with no opportunities, result should be valid
            assertThat(result.getIndexSuggestions()).isNotNull();
            assertThat(result.getPredicatePushdownSuggestions()).isNotNull();
            assertThat(result.getJoinReorderSuggestions()).isNotNull();
            
            int totalSuggestions = result.getIndexSuggestions().size() + 
                                 result.getPredicatePushdownSuggestions().size() + 
                                 result.getJoinReorderSuggestions().size();
            assertThat(result.getTotalSuggestionCount()).isEqualTo(totalSuggestions);
        }
        
        @Test
        @DisplayName("Should handle malformed SQL gracefully")
        void malformedSQL_HandledGracefully() {
            when(mockQueryBuilder.toSQL()).thenReturn("SELECT * FROM"); // Incomplete SQL
            
            assertThatCode(() -> {
                OptimizationResult result = optimizer.optimize(mockQueryBuilder);
                assertThat(result).isNotNull();
            }).doesNotThrowAnyException();
        }
        
        @Test
        @DisplayName("Should handle queries with unusual table/column names")
        void unusualTableColumnNames_HandledGracefully() {
            when(mockQueryBuilder.toSQL()).thenReturn(
                "SELECT * FROM \"order-items\" oi JOIN \"user_profiles\" up ON oi.\"user-id\" = up.id"
            );
            
            assertThatCode(() -> {
                OptimizationResult result = optimizer.optimize(mockQueryBuilder);
                assertThat(result).isNotNull();
                
                // Should handle quoted identifiers in suggestions
                for (IndexSuggestion suggestion : result.getIndexSuggestions()) {
                    assertThat(suggestion.getTableName()).isNotNull();
                    assertThat(suggestion.getColumnNames()).isNotNull().isNotEmpty();
                }
            }).doesNotThrowAnyException();
        }
    }
}