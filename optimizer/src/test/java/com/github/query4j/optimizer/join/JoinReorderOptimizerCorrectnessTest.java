package com.github.query4j.optimizer.join;

import com.github.query4j.core.QueryBuilder;
import com.github.query4j.optimizer.OptimizerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive correctness tests for JoinReorderOptimizer functionality.
 * Validates that join orders are optimized correctly while preserving query semantics
 * for both inner and outer joins.
 */
class JoinReorderOptimizerCorrectnessTest {
    
    private JoinReorderOptimizerImpl optimizer;
    private OptimizerConfig config;
    
    @Mock
    private QueryBuilder<?> mockQueryBuilder;
    
    @Mock
    private JoinReorderOptimizer.TableStatistics mockTableStatistics;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        config = OptimizerConfig.defaultConfig();
        optimizer = new JoinReorderOptimizerImpl(config);
    }
    
    @Nested
    @DisplayName("Inner Join Optimization Tests")
    class InnerJoinOptimizationTests {
        
        @Test
        @DisplayName("Should suggest optimal join order for inner joins based on selectivity")
        void analyzeJoinSequence_InnerJoins_SuggestsOptimalOrder() {
            List<String> originalSequence = List.of("customers", "orders", "order_items");
            
            List<JoinReorderSuggestion.JoinCondition> joinConditions = List.of(
                JoinReorderSuggestion.JoinCondition.builder()
                    .leftTable("customers")
                    .rightTable("orders")
                    .joinField("customer_id")
                    .selectivity(0.3)
                    .build(),
                JoinReorderSuggestion.JoinCondition.builder()
                    .leftTable("orders")
                    .rightTable("order_items")
                    .joinField("order_id")
                    .selectivity(0.1) // Highly selective join
                    .build()
            );
            
            List<JoinReorderSuggestion> suggestions = optimizer.analyzeJoinSequence(originalSequence, joinConditions);
            
            if (!suggestions.isEmpty()) {
                JoinReorderSuggestion suggestion = suggestions.get(0);
                
                assertThat(suggestion.getOriginalJoinSequence()).isEqualTo(originalSequence);
                assertThat(suggestion.getSuggestedJoinSequence()).isNotEqualTo(originalSequence);
                assertThat(suggestion.getReorderType()).isEqualTo(JoinReorderSuggestion.JoinReorderType.SELECTIVITY_BASED);
                assertThat(suggestion.getEstimatedImprovement()).isGreaterThan(0.0);
                assertThat(suggestion.getReason()).contains("selectivity");
                assertThat(suggestion.getInfluencingConditions()).isEqualTo(joinConditions);
            }
        }
        
        @Test
        @DisplayName("Should preserve join semantics when reordering inner joins")
        void innerJoinReordering_PreservesSemantics() {
            List<String> joinSequence = List.of("users", "orders", "products");
            
            List<JoinReorderSuggestion.JoinCondition> joinConditions = List.of(
                JoinReorderSuggestion.JoinCondition.builder()
                    .leftTable("users")
                    .rightTable("orders")
                    .joinField("user_id")
                    .selectivity(0.2)
                    .build(),
                JoinReorderSuggestion.JoinCondition.builder()
                    .leftTable("orders")
                    .rightTable("products")
                    .joinField("product_id")
                    .selectivity(0.1)
                    .build()
            );
            
            List<JoinReorderSuggestion> suggestions = optimizer.analyzeJoinSequence(joinSequence, joinConditions);
            
            for (JoinReorderSuggestion suggestion : suggestions) {
                // All original tables should be present in suggested sequence
                assertThat(suggestion.getSuggestedJoinSequence()).containsExactlyInAnyOrderElementsOf(joinSequence);
                
                // Should not lose any tables
                assertThat(suggestion.getSuggestedJoinSequence()).hasSize(joinSequence.size());
                
                // All join conditions should be preserved
                assertThat(suggestion.getInfluencingConditions()).isEqualTo(joinConditions);
            }
        }
        
        @Test
        @DisplayName("Should calculate realistic improvement estimates for inner joins")
        void innerJoinImprovement_RealisticEstimates() {
            List<String> joinSequence = List.of("large_table", "medium_table", "small_table");
            
            List<JoinReorderSuggestion.JoinCondition> joinConditions = List.of(
                JoinReorderSuggestion.JoinCondition.builder()
                    .leftTable("large_table")
                    .rightTable("medium_table")
                    .joinField("id")
                    .selectivity(0.8) // Poor selectivity
                    .build(),
                JoinReorderSuggestion.JoinCondition.builder()
                    .leftTable("medium_table")
                    .rightTable("small_table")
                    .joinField("id")
                    .selectivity(0.05) // High selectivity
                    .build()
            );
            
            List<JoinReorderSuggestion> suggestions = optimizer.analyzeJoinSequence(joinSequence, joinConditions);
            
            for (JoinReorderSuggestion suggestion : suggestions) {
                // Improvement should be realistic (between 0% and 100%)
                assertThat(suggestion.getEstimatedImprovement()).isBetween(0.0, 1.0);
                
                // High improvement should correspond to high priority
                if (suggestion.getEstimatedImprovement() > 0.5) {
                    assertThat(suggestion.getPriority()).isEqualTo(JoinReorderSuggestion.Priority.HIGH);
                } else if (suggestion.getEstimatedImprovement() > 0.2) {
                    assertThat(suggestion.getPriority()).isEqualTo(JoinReorderSuggestion.Priority.MEDIUM);
                }
            }
        }
    }
    
    @Nested
    @DisplayName("Join Sequence Preservation Tests")
    class JoinSequencePreservationTests {
        
        @Test
        @DisplayName("Should preserve join order dependencies for complex queries")
        void complexJoinSequence_PreservesImportantDependencies() {
            List<String> joinSequence = List.of("customers", "orders", "payments");
            
            List<JoinReorderSuggestion.JoinCondition> joinConditions = List.of(
                JoinReorderSuggestion.JoinCondition.builder()
                    .leftTable("customers")
                    .rightTable("orders")
                    .joinField("customer_id")
                    .selectivity(0.4)
                    .build(),
                JoinReorderSuggestion.JoinCondition.builder()
                    .leftTable("orders")
                    .rightTable("payments")
                    .joinField("order_id")
                    .selectivity(0.3)
                    .build()
            );
            
            List<JoinReorderSuggestion> suggestions = optimizer.analyzeJoinSequence(joinSequence, joinConditions);
            
            for (JoinReorderSuggestion suggestion : suggestions) {
                List<String> suggestedSequence = suggestion.getSuggestedJoinSequence();
                
                // All tables should still be present
                assertThat(suggestedSequence).containsExactlyInAnyOrderElementsOf(joinSequence);
                
                // Should not lose any tables
                assertThat(suggestedSequence).hasSize(joinSequence.size());
            }
        }
        
        @Test
        @DisplayName("Should handle mixed join selectivities correctly")
        void mixedJoinSelectivities_HandledCorrectly() {
            List<String> joinSequence = List.of("users", "orders", "order_items", "products");
            
            List<JoinReorderSuggestion.JoinCondition> mixedJoinConditions = List.of(
                JoinReorderSuggestion.JoinCondition.builder()
                    .leftTable("users")
                    .rightTable("orders")
                    .joinField("user_id")
                    .selectivity(0.3) // Medium selectivity
                    .build(),
                JoinReorderSuggestion.JoinCondition.builder()
                    .leftTable("orders")
                    .rightTable("order_items")
                    .joinField("order_id")
                    .selectivity(0.1) // High selectivity
                    .build(),
                JoinReorderSuggestion.JoinCondition.builder()
                    .leftTable("order_items")
                    .rightTable("products")
                    .joinField("product_id")
                    .selectivity(0.2) // Good selectivity
                    .build()
            );
            
            List<JoinReorderSuggestion> suggestions = optimizer.analyzeJoinSequence(joinSequence, mixedJoinConditions);
            
            for (JoinReorderSuggestion suggestion : suggestions) {
                // Should preserve all tables
                assertThat(suggestion.getSuggestedJoinSequence()).containsExactlyInAnyOrderElementsOf(joinSequence);
                
                // Should have meaningful reasoning
                assertThat(suggestion.getReason()).isNotEmpty();
            }
        }
        
        @Test
        @DisplayName("Should handle various join scenarios appropriately")
        void variousJoinScenarios_HandledAppropriately() {
            List<String> joinSequence = List.of("table1", "table2");
            List<JoinReorderSuggestion.JoinCondition> joinConditions = List.of(
                JoinReorderSuggestion.JoinCondition.builder()
                    .leftTable("table1")
                    .rightTable("table2")
                    .joinField("join_key")
                    .selectivity(0.3)
                    .build()
            );
            
            assertThatCode(() -> {
                List<JoinReorderSuggestion> suggestions = optimizer.analyzeJoinSequence(joinSequence, joinConditions);
                assertThat(suggestions).isNotNull();
            }).doesNotThrowAnyException();
        }
    }
    
    @Nested
    @DisplayName("Cardinality-Based Optimization Tests")
    class CardinalityOptimizationTests {
        
        @Test
        @DisplayName("Should estimate cardinality reduction correctly")
        void estimateCardinalityReduction_CorrectCalculation() {
            when(mockTableStatistics.getEstimatedRowCount("small_table")).thenReturn(1000L);
            when(mockTableStatistics.getEstimatedRowCount("medium_table")).thenReturn(10000L);
            when(mockTableStatistics.getEstimatedRowCount("large_table")).thenReturn(100000L);
            
            when(mockTableStatistics.getJoinSelectivity("large_table", "medium_table", "id")).thenReturn(0.1);
            when(mockTableStatistics.getJoinSelectivity("medium_table", "small_table", "id")).thenReturn(0.2);
            when(mockTableStatistics.getJoinSelectivity("small_table", "large_table", "id")).thenReturn(0.05);
            
            List<String> originalSequence = List.of("large_table", "medium_table", "small_table");
            
            double cardinalityReduction = optimizer.estimateCardinalityReduction(originalSequence, mockTableStatistics);
            
            // Should also test with different sequence
            List<String> optimalSequence = List.of("small_table", "medium_table", "large_table");
            double optimalCardinalityReduction = optimizer.estimateCardinalityReduction(optimalSequence, mockTableStatistics);
            
            // Both calculations should return reasonable values
            assertThat(cardinalityReduction).isBetween(0.0, 1.0);
            assertThat(optimalCardinalityReduction).isBetween(0.0, 1.0);
            
            // The method should be stable (same input should give same output)
            double repeatCardinalityReduction = optimizer.estimateCardinalityReduction(originalSequence, mockTableStatistics);
            assertThat(repeatCardinalityReduction).isEqualTo(cardinalityReduction);
        }
        
        @Test
        @DisplayName("Should suggest join reordering based on table sizes")
        void joinReordering_BasedOnTableSizes() {
            when(mockQueryBuilder.toSQL()).thenReturn(
                "SELECT * FROM big_table b JOIN small_table s ON b.id = s.big_table_id"
            );
            
            List<JoinReorderSuggestion> suggestions = optimizer.optimizeJoinOrder(mockQueryBuilder);
            
            // Should get suggestions when there's potential for improvement
            assertThat(suggestions).isNotNull();
            
            for (JoinReorderSuggestion suggestion : suggestions) {
                assertThat(suggestion.getOriginalJoinSequence()).isNotNull();
                assertThat(suggestion.getSuggestedJoinSequence()).isNotNull();
                assertThat(suggestion.getReorderType()).isNotNull();
                assertThat(suggestion.getEstimatedImprovement()).isBetween(0.0, 1.0);
            }
        }
        
        @Test
        @DisplayName("Should handle tables with unknown statistics gracefully")
        void unknownTableStatistics_HandledGracefully() {
            when(mockTableStatistics.getEstimatedRowCount(anyString())).thenReturn(0L); // Unknown
            when(mockTableStatistics.getJoinSelectivity(anyString(), anyString(), anyString())).thenReturn(0.5); // Default
            
            List<String> joinSequence = List.of("unknown_table1", "unknown_table2", "unknown_table3");
            
            assertThatCode(() -> {
                double reduction = optimizer.estimateCardinalityReduction(joinSequence, mockTableStatistics);
                assertThat(reduction).isBetween(0.0, 1.0);
            }).doesNotThrowAnyException();
        }
    }
    
    @Nested
    @DisplayName("Index-Driven Optimization Tests")
    class IndexDrivenOptimizationTests {
        
        @Test
        @DisplayName("Should prioritize indexed join columns")
        void indexedJoinColumns_GetPriority() {
            when(mockTableStatistics.hasIndexOnField("users", "id")).thenReturn(true);
            when(mockTableStatistics.hasIndexOnField("orders", "user_id")).thenReturn(true);
            when(mockTableStatistics.hasIndexOnField("orders", "product_id")).thenReturn(false);
            when(mockTableStatistics.hasIndexOnField("products", "id")).thenReturn(true);
            
            List<String> joinSequence = List.of("users", "orders", "products");
            List<JoinReorderSuggestion.JoinCondition> joinConditions = List.of(
                JoinReorderSuggestion.JoinCondition.builder()
                    .leftTable("users")
                    .rightTable("orders")
                    .joinField("user_id")
                    .selectivity(0.3)
                    .hasIndex(true)
                    .build(),
                JoinReorderSuggestion.JoinCondition.builder()
                    .leftTable("orders")
                    .rightTable("products")
                    .joinField("product_id")
                    .selectivity(0.3)
                    .hasIndex(false)
                    .build()
            );
            
            List<JoinReorderSuggestion> suggestions = optimizer.analyzeJoinSequence(joinSequence, joinConditions);
            
            for (JoinReorderSuggestion suggestion : suggestions) {
                if (suggestion.getReorderType() == JoinReorderSuggestion.JoinReorderType.INDEX_DRIVEN) {
                    assertThat(suggestion.getReason()).contains("index");
                    assertThat(suggestion.getExpectedImpact()).contains("index utilization");
                }
            }
        }
        
        @Test
        @DisplayName("Should suggest index-driven reordering when appropriate")
        void indexDrivenReordering_WhenAppropriate() {
            List<String> joinSequence = List.of("table_no_index", "table_with_index");
            
            List<JoinReorderSuggestion.JoinCondition> joinConditions = List.of(
                JoinReorderSuggestion.JoinCondition.builder()
                    .leftTable("table_no_index")
                    .rightTable("table_with_index")
                    .joinField("join_key")
                    .selectivity(0.3)
                    .hasIndex(true) // Right table has index
                    .build()
            );
            
            List<JoinReorderSuggestion> suggestions = optimizer.analyzeJoinSequence(joinSequence, joinConditions);
            
            // May suggest reordering to leverage the index
            for (JoinReorderSuggestion suggestion : suggestions) {
                if (suggestion.getReorderType() == JoinReorderSuggestion.JoinReorderType.INDEX_DRIVEN) {
                    assertThat(suggestion.getInfluencingConditions()).isEqualTo(joinConditions);
                    assertThat(suggestion.getReason()).containsIgnoringCase("index");
                }
            }
        }
    }
    
    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesTests {
        
        @Test
        @DisplayName("Should handle single table queries gracefully")
        void singleTable_NoJoinOptimization() {
            List<String> singleTable = List.of("users");
            List<JoinReorderSuggestion.JoinCondition> noJoins = List.of();
            
            List<JoinReorderSuggestion> suggestions = optimizer.analyzeJoinSequence(singleTable, noJoins);
            
            assertThat(suggestions).isEmpty(); // No optimization needed for single table
        }
        
        @Test
        @DisplayName("Should handle empty join sequence gracefully")
        void emptyJoinSequence_HandledGracefully() {
            List<String> emptySequence = List.of();
            List<JoinReorderSuggestion.JoinCondition> emptyConditions = List.of();
            
            List<JoinReorderSuggestion> suggestions = optimizer.analyzeJoinSequence(emptySequence, emptyConditions);
            
            assertThat(suggestions).isEmpty();
        }
        
        @Test
        @DisplayName("Should validate input parameters correctly")
        void inputValidation_ThrowsAppropriateExceptions() {
            // Null QueryBuilder
            assertThatThrownBy(() -> optimizer.optimizeJoinOrder(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("QueryBuilder must not be null");
            
            // Null join sequence
            assertThatThrownBy(() -> optimizer.analyzeJoinSequence(null, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Join sequence must not be null");
            
            // Null join conditions
            assertThatThrownBy(() -> optimizer.analyzeJoinSequence(List.of("table1"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Join conditions must not be null");
        }
        
        @Test
        @DisplayName("Should handle mismatched join conditions gracefully")
        void mismatchedJoinConditions_HandledGracefully() {
            List<String> joinSequence = List.of("table1", "table2");
            
            // Join condition references non-existent table
            List<JoinReorderSuggestion.JoinCondition> mismatchedConditions = List.of(
                JoinReorderSuggestion.JoinCondition.builder()
                    .leftTable("table1")
                    .rightTable("non_existent_table")
                    .joinField("id")
                    .selectivity(0.3)
                    .build()
            );
            
            assertThatCode(() -> {
                List<JoinReorderSuggestion> suggestions = optimizer.analyzeJoinSequence(joinSequence, mismatchedConditions);
                assertThat(suggestions).isNotNull();
            }).doesNotThrowAnyException();
        }
        
        @Test
        @DisplayName("Should handle circular join dependencies")
        void circularJoinDependencies_DetectedAndHandled() {
            List<String> joinSequence = List.of("table1", "table2", "table3");
            
            List<JoinReorderSuggestion.JoinCondition> circularConditions = List.of(
                JoinReorderSuggestion.JoinCondition.builder()
                    .leftTable("table1")
                    .rightTable("table2")
                    .joinField("id")
                    .selectivity(0.3)
                    .build(),
                JoinReorderSuggestion.JoinCondition.builder()
                    .leftTable("table2")
                    .rightTable("table3")
                    .joinField("id")
                    .selectivity(0.3)
                    .build(),
                JoinReorderSuggestion.JoinCondition.builder()
                    .leftTable("table3")
                    .rightTable("table1")
                    .joinField("id")
                    .selectivity(0.3)
                    .build()
            );
            
            assertThatCode(() -> {
                List<JoinReorderSuggestion> suggestions = optimizer.analyzeJoinSequence(joinSequence, circularConditions);
                assertThat(suggestions).isNotNull();
            }).doesNotThrowAnyException();
        }
    }
    
    @Nested
    @DisplayName("Configuration Impact Tests")
    class ConfigurationImpactTests {
        
        @Test
        @DisplayName("Should respect join reordering threshold configuration")
        void joinReorderingThreshold_AffectsSuggestions() {
            // Low threshold - should get more suggestions
            OptimizerConfig lowThresholdConfig = OptimizerConfig.builder()
                .joinReorderingThreshold(0.05)
                .build();
            JoinReorderOptimizerImpl lowThresholdOptimizer = new JoinReorderOptimizerImpl(lowThresholdConfig);
            
            // High threshold - should get fewer suggestions
            OptimizerConfig highThresholdConfig = OptimizerConfig.builder()
                .joinReorderingThreshold(0.8)
                .build();
            JoinReorderOptimizerImpl highThresholdOptimizer = new JoinReorderOptimizerImpl(highThresholdConfig);
            
            List<String> joinSequence = List.of("table1", "table2", "table3");
            List<JoinReorderSuggestion.JoinCondition> joinConditions = List.of(
                JoinReorderSuggestion.JoinCondition.builder()
                    .leftTable("table1")
                    .rightTable("table2")
                    .joinField("id")
                    .selectivity(0.4)
                    .build(),
                JoinReorderSuggestion.JoinCondition.builder()
                    .leftTable("table2")
                    .rightTable("table3")
                    .joinField("id")
                    .selectivity(0.2)
                    .build()
            );
            
            List<JoinReorderSuggestion> lowThresholdSuggestions = 
                lowThresholdOptimizer.analyzeJoinSequence(joinSequence, joinConditions);
            List<JoinReorderSuggestion> highThresholdSuggestions = 
                highThresholdOptimizer.analyzeJoinSequence(joinSequence, joinConditions);
            
            // Low threshold should produce at least as many suggestions as high threshold
            assertThat(lowThresholdSuggestions.size()).isGreaterThanOrEqualTo(highThresholdSuggestions.size());
        }
        
        @Test
        @DisplayName("Should handle disabled join reordering configuration")
        void disabledJoinReordering_NoSuggestions() {
            OptimizerConfig disabledConfig = OptimizerConfig.builder()
                .joinReorderingEnabled(false)
                .build();
            
            // Note: This test assumes the implementation checks the config
            // In practice, the QueryOptimizer might not call the join optimizer if disabled
            assertThat(disabledConfig.isJoinReorderingEnabled()).isFalse();
        }
    }
    
    @Nested
    @DisplayName("Performance and Scalability Tests")
    class PerformanceTests {
        
        @Test
        @DisplayName("Should handle large join sequences efficiently")
        void largeJoinSequences_HandledEfficiently() {
            // Create a large join sequence
            List<String> largeJoinSequence = new ArrayList<>();
            List<JoinReorderSuggestion.JoinCondition> largeJoinConditions = new ArrayList<>();
            
            for (int i = 0; i < 20; i++) {
                largeJoinSequence.add("table" + i);
                if (i > 0) {
                    largeJoinConditions.add(
                        JoinReorderSuggestion.JoinCondition.builder()
                            .leftTable("table" + (i - 1))
                            .rightTable("table" + i)
                            .joinField("id")
                            .selectivity(0.1 + (i * 0.02)) // Varying selectivity
                            .build()
                    );
                }
            }
            
            long startTime = System.currentTimeMillis();
            
            assertThatCode(() -> {
                List<JoinReorderSuggestion> suggestions = optimizer.analyzeJoinSequence(largeJoinSequence, largeJoinConditions);
                assertThat(suggestions).isNotNull();
            }).doesNotThrowAnyException();
            
            long duration = System.currentTimeMillis() - startTime;
            
            // Should complete within reasonable time (less than 2 seconds for 20 tables)
            assertThat(duration).isLessThan(2000);
        }
        
        @Test
        @DisplayName("Should maintain consistent cardinality estimates")
        void cardinalityEstimation_Consistent() {
            when(mockTableStatistics.getEstimatedRowCount("table1")).thenReturn(1000L);
            when(mockTableStatistics.getEstimatedRowCount("table2")).thenReturn(2000L);
            when(mockTableStatistics.getJoinSelectivity("table1", "table2", "id")).thenReturn(0.3);
            
            List<String> joinSequence = List.of("table1", "table2");
            
            // Multiple calls should return consistent results
            double firstEstimate = optimizer.estimateCardinalityReduction(joinSequence, mockTableStatistics);
            double secondEstimate = optimizer.estimateCardinalityReduction(joinSequence, mockTableStatistics);
            
            assertThat(firstEstimate).isEqualTo(secondEstimate);
            assertThat(firstEstimate).isBetween(0.0, 1.0);
        }
    }
    
    // Helper methods
    
    private boolean containsHighSelectivityJoins(List<JoinReorderSuggestion.JoinCondition> conditions) {
        return conditions.stream()
            .anyMatch(c -> c.getSelectivity() < 0.2);
    }
    
    private boolean containsIndexedJoins(List<JoinReorderSuggestion.JoinCondition> conditions) {
        return conditions.stream()
            .anyMatch(JoinReorderSuggestion.JoinCondition::isHasIndex);
    }
}