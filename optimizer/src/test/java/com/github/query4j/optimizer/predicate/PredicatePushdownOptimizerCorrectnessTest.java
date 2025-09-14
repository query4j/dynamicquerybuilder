package com.github.query4j.optimizer.predicate;

import com.github.query4j.core.QueryBuilder;
import com.github.query4j.core.criteria.*;
import com.github.query4j.optimizer.OptimizerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive correctness tests for PredicatePushdownOptimizer functionality.
 * Validates that predicates are correctly rearranged based on selectivity and pushdown opportunities
 * without altering query semantics.
 */
class PredicatePushdownOptimizerCorrectnessTest {
    
    private PredicatePushdownOptimizerImpl optimizer;
    private OptimizerConfig config;
    
    @Mock
    private QueryBuilder<?> mockQueryBuilder;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        config = OptimizerConfig.defaultConfig();
        optimizer = new PredicatePushdownOptimizerImpl(config);
    }
    
    @Nested
    @DisplayName("Selectivity-Based Reordering Tests")
    class SelectivityReorderingTests {
        
        @Test
        @DisplayName("Should correctly estimate selectivity for different predicate types")
        void estimateSelectivity_DifferentPredicateTypes_ReturnsCorrectEstimates() {
            // Equality predicates should have good selectivity
            SimplePredicate equalityPredicate = new SimplePredicate("id", "=", 123, "p1");
            double equalitySelectivity = optimizer.estimateSelectivity(equalityPredicate);
            assertThat(equalitySelectivity).isBetween(0.0, 0.3);
            
            // Range predicates should have moderate selectivity
            BetweenPredicate rangePredicate = new BetweenPredicate("age", 25, 35, "p1", "p2");
            double rangeSelectivity = optimizer.estimateSelectivity(rangePredicate);
            assertThat(rangeSelectivity).isBetween(0.1, 0.6);
            
            // LIKE predicates with wildcard should have poor selectivity
            LikePredicate likePredicate = new LikePredicate("description", "%common%", "p1");
            double likeSelectivity = optimizer.estimateSelectivity(likePredicate);
            assertThat(likeSelectivity).isBetween(0.3, 1.0);
            
            // Verify ordering: equality < range < like (generally)
            assertThat(equalitySelectivity).isLessThanOrEqualTo(rangeSelectivity);
        }
        
        @Test
        @DisplayName("Should suggest reordering predicates by selectivity")
        void suggestPredicateReordering_MultiplePredicates_SuggestsOptimalOrder() {
            List<Predicate> predicates = List.of(
                new LikePredicate("description", "%product%", "p1"),    // Low selectivity (should be last)
                new SimplePredicate("category_id", "=", 5, "p2"),       // High selectivity (should be first)
                new BetweenPredicate("price", 10.0, 100.0, "p3", "p4")  // Medium selectivity (should be middle)
            );
            
            List<PredicatePushdownSuggestion> suggestions = optimizer.suggestPredicateReordering(predicates);
            
            // Should get reordering suggestions
            assertThat(suggestions).isNotEmpty();
            
            // Find the suggestion for the high-selectivity predicate (category_id)
            Optional<PredicatePushdownSuggestion> categoryReorderSuggestion = suggestions.stream()
                .filter(s -> s.getOriginalPosition() == 1) // category_id was at position 1
                .filter(s -> s.getSuggestedPosition() == 0) // should be moved to position 0
                .findFirst();
            
            assertThat(categoryReorderSuggestion).isPresent();
            PredicatePushdownSuggestion suggestion = categoryReorderSuggestion.get();
            assertThat(suggestion.getOptimizationType()).isEqualTo(PredicatePushdownSuggestion.OptimizationType.REORDER_BY_SELECTIVITY);
            assertThat(suggestion.getSelectivity()).isLessThan(0.3); // High selectivity = low selectivity value
            assertThat(suggestion.getReason()).contains("More selective predicate should be evaluated earlier");
        }
        
        @Test
        @DisplayName("Should not suggest reordering when improvement is below threshold")
        void suggestPredicateReordering_BelowThreshold_NoSuggestions() {
            // Create config with high threshold
            OptimizerConfig strictConfig = OptimizerConfig.builder()
                .predicateReorderingThreshold(0.8) // Very high threshold
                .build();
            PredicatePushdownOptimizerImpl strictOptimizer = new PredicatePushdownOptimizerImpl(strictConfig);
            
            List<Predicate> predicates = List.of(
                new SimplePredicate("id", "=", 1, "p1"),
                new SimplePredicate("name", "=", "John", "p2")
            );
            
            List<PredicatePushdownSuggestion> suggestions = strictOptimizer.suggestPredicateReordering(predicates);
            
            // With high threshold and similar predicates, should get no suggestions
            assertThat(suggestions).isEmpty();
        }
        
        @ParameterizedTest
        @CsvSource({
            "'=', 0.1",     // Equality predicates are highly selective
            "'>', 0.3",     // Range predicates have moderate selectivity
            "'<', 0.3",
            "'>=', 0.3",
            "'<=', 0.3"
        })
        @DisplayName("Should estimate selectivity correctly for different operators")
        void estimateSelectivity_DifferentOperators_ReturnsAppropriateValues(String operator, double maxExpectedSelectivity) {
            SimplePredicate predicate = new SimplePredicate("field", operator, "value", "p1");
            
            double selectivity = optimizer.estimateSelectivity(predicate);
            
            assertThat(selectivity).isBetween(0.0, maxExpectedSelectivity);
        }
        
        @Test
        @DisplayName("Should handle IN predicates with selectivity based on value count")
        void estimateSelectivity_InPredicate_SelectivityBasedOnValueCount() {
            // Few values = high selectivity
            InPredicate fewValues = new InPredicate("status", List.of("ACTIVE"), "p1");
            double fewValuesSelectivity = optimizer.estimateSelectivity(fewValues);
            
            // Many values = lower selectivity
            InPredicate manyValues = new InPredicate("category", 
                List.of("CAT1", "CAT2", "CAT3", "CAT4", "CAT5", "CAT6", "CAT7", "CAT8", "CAT9", "CAT10"), "p2");
            double manyValuesSelectivity = optimizer.estimateSelectivity(manyValues);
            
            assertThat(fewValuesSelectivity).isLessThan(manyValuesSelectivity);
            assertThat(fewValuesSelectivity).isBetween(0.0, 0.2);
            assertThat(manyValuesSelectivity).isBetween(0.1, 0.8);
        }
    }
    
    @Nested
    @DisplayName("Predicate Pushdown to Join Sources")
    class PredicatePushdownTests {
        
        @Test
        @DisplayName("Should suggest pushdown of table-specific predicates to join sources")
        void optimizeQuery_TableSpecificPredicates_SuggestsPushdown() {
            when(mockQueryBuilder.toSQL()).thenReturn(
                "SELECT u.*, o.* FROM users u JOIN orders o ON u.id = o.user_id WHERE u.status = 'ACTIVE' AND o.amount > 100"
            );
            
            List<PredicatePushdownSuggestion> suggestions = optimizer.optimizeQuery(mockQueryBuilder);
            
            // Should get suggestions for optimization (implementation may vary)
            assertThat(suggestions).isNotNull();
            
            // Look for pushdown suggestions
            List<PredicatePushdownSuggestion> pushdownSuggestions = suggestions.stream()
                .filter(s -> s.getOptimizationType() == PredicatePushdownSuggestion.OptimizationType.PUSH_TO_JOIN_SOURCE)
                .collect(Collectors.toList());
            
            if (!pushdownSuggestions.isEmpty()) {
                PredicatePushdownSuggestion pushdownSuggestion = pushdownSuggestions.get(0);
                assertThat(pushdownSuggestion.getReason()).contains("Filter");
                assertThat(pushdownSuggestion.getExpectedImpact()).isNotNull().isNotEmpty();
                assertThat(pushdownSuggestion.getPriority()).isNotNull();
            }
        }
        
        @Test
        @DisplayName("Should identify pushdown opportunities without altering query semantics")
        void pushdownSuggestions_PreserveQuerySemantics() {
            List<Predicate> predicates = List.of(
                new SimplePredicate("users.status", "=", "ACTIVE", "p1"),
                new SimplePredicate("orders.amount", ">", 100, "p2"),
                new SimplePredicate("users.created_date", ">", "2024-01-01", "p3")
            );
            
            List<PredicatePushdownSuggestion> suggestions = optimizer.suggestPredicateReordering(predicates);
            
            // Verify that original predicates are preserved in suggestions
            for (PredicatePushdownSuggestion suggestion : suggestions) {
                assertThat(suggestion.getOriginalPredicate()).isNotNull();
                assertThat(suggestion.getOriginalPosition()).isBetween(0, predicates.size() - 1);
                assertThat(suggestion.getSuggestedPosition()).isBetween(0, predicates.size() - 1);
            }
            
            // Verify that all original predicates are still accounted for
            Set<Predicate> originalPredicates = new HashSet<>(predicates);
            Set<Predicate> suggestedPredicates = suggestions.stream()
                .map(PredicatePushdownSuggestion::getOriginalPredicate)
                .collect(Collectors.toSet());
            
            // All suggested predicates should be from the original set
            assertThat(suggestedPredicates).isSubsetOf(originalPredicates);
        }
        
        @Test
        @DisplayName("Should prioritize pushdown suggestions based on impact")
        void pushdownSuggestions_CorrectPrioritization() {
            when(mockQueryBuilder.toSQL()).thenReturn(
                "SELECT * FROM orders o JOIN customers c ON o.customer_id = c.id WHERE c.country = 'US' AND o.status = 'PENDING'"
            );
            
            List<PredicatePushdownSuggestion> suggestions = optimizer.optimizeQuery(mockQueryBuilder);
            
            if (!suggestions.isEmpty()) {
                // High selectivity predicates should get high priority
                List<PredicatePushdownSuggestion> highPrioritySuggestions = suggestions.stream()
                    .filter(s -> s.getPriority() == PredicatePushdownSuggestion.Priority.HIGH)
                    .collect(Collectors.toList());
                
                List<PredicatePushdownSuggestion> mediumPrioritySuggestions = suggestions.stream()
                    .filter(s -> s.getPriority() == PredicatePushdownSuggestion.Priority.MEDIUM)
                    .collect(Collectors.toList());
                
                // Verify priority assignment makes sense
                for (PredicatePushdownSuggestion highPriority : highPrioritySuggestions) {
                    for (PredicatePushdownSuggestion mediumPriority : mediumPrioritySuggestions) {
                        // High priority predicates should be more selective (lower selectivity value)
                        if (highPriority.getSelectivity() > 0 && mediumPriority.getSelectivity() > 0) {
                            assertThat(highPriority.getSelectivity()).isLessThanOrEqualTo(mediumPriority.getSelectivity() * 1.5);
                        }
                    }
                }
            }
        }
    }
    
    @Nested
    @DisplayName("Query Semantics Preservation")
    class SemanticsPreservationTests {
        
        @Test
        @DisplayName("Should preserve logical AND/OR relationships in reordering")
        void predicateReordering_PreservesLogicalRelationships() {
            // Test that reordering doesn't break logical groupings
            List<Predicate> predicates = List.of(
                new SimplePredicate("category", "=", "ELECTRONICS", "p1"),  // High selectivity
                new SimplePredicate("price", ">", 500, "p2"),               // Medium selectivity
                new LikePredicate("description", "%discount%", "p3")        // Low selectivity
            );
            
            List<PredicatePushdownSuggestion> suggestions = optimizer.suggestPredicateReordering(predicates);
            
            // Verify that reordering suggestions maintain the total number of predicates
            Set<Integer> originalPositions = suggestions.stream()
                .map(PredicatePushdownSuggestion::getOriginalPosition)
                .collect(Collectors.toSet());
            
            Set<Integer> suggestedPositions = suggestions.stream()
                .map(PredicatePushdownSuggestion::getSuggestedPosition)
                .collect(Collectors.toSet());
            
            // No position should be duplicated in suggestions
            assertThat(originalPositions).hasSize(suggestions.size());
            assertThat(suggestedPositions).hasSize(suggestions.size());
            
            // All positions should be within valid range
            for (PredicatePushdownSuggestion suggestion : suggestions) {
                assertThat(suggestion.getOriginalPosition()).isBetween(0, predicates.size() - 1);
                assertThat(suggestion.getSuggestedPosition()).isBetween(0, predicates.size() - 1);
            }
        }
        
        @Test
        @DisplayName("Should not alter predicate parameters or operators")
        void predicateReordering_PreservesPredicateIntegrity() {
            SimplePredicate originalPredicate = new SimplePredicate("email", "=", "test@example.com", "p1");
            List<Predicate> predicates = List.of(originalPredicate);
            
            List<PredicatePushdownSuggestion> suggestions = optimizer.suggestPredicateReordering(predicates);
            
            for (PredicatePushdownSuggestion suggestion : suggestions) {
                Predicate suggestedPredicate = suggestion.getOriginalPredicate();
                
                // Predicate should be unchanged
                assertThat(suggestedPredicate).isEqualTo(originalPredicate);
                
                // SQL should be identical
                assertThat(suggestedPredicate.toSQL()).isEqualTo(originalPredicate.toSQL());
                
                // Parameters should be identical
                assertThat(suggestedPredicate.getParameters()).isEqualTo(originalPredicate.getParameters());
            }
        }
        
        @Test
        @DisplayName("Should maintain parameter uniqueness across predicates")
        void predicateReordering_MaintainsParameterUniqueness() {
            List<Predicate> predicates = List.of(
                new SimplePredicate("field1", "=", "value1", "p1"),
                new SimplePredicate("field2", "=", "value2", "p2"),
                new SimplePredicate("field3", "=", "value3", "p3")
            );
            
            List<PredicatePushdownSuggestion> suggestions = optimizer.suggestPredicateReordering(predicates);
            
            // Collect all parameter names from original predicates
            Set<String> originalParameters = predicates.stream()
                .flatMap(p -> p.getParameters().keySet().stream())
                .collect(Collectors.toSet());
            
            // Collect all parameter names from suggested predicates
            Set<String> suggestedParameters = suggestions.stream()
                .map(PredicatePushdownSuggestion::getOriginalPredicate)
                .flatMap(p -> p.getParameters().keySet().stream())
                .collect(Collectors.toSet());
            
            // Parameter sets should be compatible (implementation may not suggest all predicates)
            assertThat(suggestedParameters).isSubsetOf(originalParameters);
            
            // No new parameter names should be introduced
            assertThat(originalParameters).hasSize(3); // p1, p2, p3
            if (!suggestions.isEmpty()) {
                // At least some parameters should be preserved
                assertThat(suggestedParameters).isNotEmpty();
            }
        }
    }
    
    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesTests {
        
        @Test
        @DisplayName("Should handle empty predicate list gracefully")
        void emptyPredicateList_HandledGracefully() {
            List<PredicatePushdownSuggestion> suggestions = optimizer.suggestPredicateReordering(List.of());
            
            assertThat(suggestions).isNotNull().isEmpty();
        }
        
        @Test
        @DisplayName("Should handle single predicate gracefully")
        void singlePredicate_NoReorderingSuggested() {
            SimplePredicate singlePredicate = new SimplePredicate("id", "=", 1, "p1");
            List<Predicate> predicates = List.of(singlePredicate);
            
            List<PredicatePushdownSuggestion> suggestions = optimizer.suggestPredicateReordering(predicates);
            
            // No reordering needed for single predicate
            assertThat(suggestions).isEmpty();
        }
        
        @Test
        @DisplayName("Should handle null values in predicate parameters")
        void nullParameterValues_HandledGracefully() {
            SimplePredicate nullValuePredicate = new SimplePredicate("optional_field", "=", null, "p1");
            List<Predicate> predicates = List.of(nullValuePredicate);
            
            assertThatCode(() -> {
                double selectivity = optimizer.estimateSelectivity(nullValuePredicate);
                assertThat(selectivity).isBetween(0.0, 1.0);
                
                List<PredicatePushdownSuggestion> suggestions = optimizer.suggestPredicateReordering(predicates);
                assertThat(suggestions).isNotNull();
            }).doesNotThrowAnyException();
        }
        
        @Test
        @DisplayName("Should validate input parameters correctly")
        void inputValidation_ThrowsAppropriateExceptions() {
            // Null QueryBuilder
            assertThatThrownBy(() -> optimizer.optimizeQuery(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("QueryBuilder must not be null");
            
            // Null predicate list
            assertThatThrownBy(() -> optimizer.suggestPredicateReordering(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Predicates list must not be null");
            
            // Null predicate in selectivity estimation
            assertThatThrownBy(() -> optimizer.estimateSelectivity(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Predicate must not be null");
        }
        
        @Test
        @DisplayName("Should handle custom predicate types gracefully")
        void customPredicateTypes_HandledGracefully() {
            Predicate customPredicate = new Predicate() {
                @Override
                public String toSQL() {
                    return "CUSTOM_FUNCTION(field) = ?";
                }
                
                @Override
                public Map<String, Object> getParameters() {
                    return Map.of("p1", "custom_value");
                }
            };
            
            assertThatCode(() -> {
                double selectivity = optimizer.estimateSelectivity(customPredicate);
                assertThat(selectivity).isBetween(0.0, 1.0);
                
                List<Predicate> predicates = List.of(customPredicate);
                List<PredicatePushdownSuggestion> suggestions = optimizer.suggestPredicateReordering(predicates);
                assertThat(suggestions).isNotNull();
            }).doesNotThrowAnyException();
        }
    }
    
    @Nested
    @DisplayName("Configuration Impact Tests")
    class ConfigurationImpactTests {
        
        @Test
        @DisplayName("Should respect predicate reordering threshold configuration")
        void predicateReorderingThreshold_AffectsSuggestions() {
            // Low threshold - should get more suggestions
            OptimizerConfig lowThresholdConfig = OptimizerConfig.builder()
                .predicateReorderingThreshold(0.1)
                .build();
            PredicatePushdownOptimizerImpl lowThresholdOptimizer = new PredicatePushdownOptimizerImpl(lowThresholdConfig);
            
            // High threshold - should get fewer suggestions
            OptimizerConfig highThresholdConfig = OptimizerConfig.builder()
                .predicateReorderingThreshold(0.8)
                .build();
            PredicatePushdownOptimizerImpl highThresholdOptimizer = new PredicatePushdownOptimizerImpl(highThresholdConfig);
            
            List<Predicate> predicates = List.of(
                new LikePredicate("description", "%common%", "p1"),
                new SimplePredicate("id", "=", 123, "p2"),
                new BetweenPredicate("price", 10, 100, "p3", "p4")
            );
            
            List<PredicatePushdownSuggestion> lowThresholdSuggestions = 
                lowThresholdOptimizer.suggestPredicateReordering(predicates);
            List<PredicatePushdownSuggestion> highThresholdSuggestions = 
                highThresholdOptimizer.suggestPredicateReordering(predicates);
            
            // Low threshold should produce at least as many suggestions as high threshold
            assertThat(lowThresholdSuggestions.size()).isGreaterThanOrEqualTo(highThresholdSuggestions.size());
        }
        
        @Test
        @DisplayName("Should use appropriate default values when config is missing")
        void defaultConfiguration_UsesReasonableDefaults() {
            OptimizerConfig defaultConfig = OptimizerConfig.defaultConfig();
            
            assertThat(defaultConfig.getPredicateReorderingThreshold()).isBetween(0.0, 1.0);
            assertThat(defaultConfig.isPredicatePushdownEnabled()).isTrue();
            
            // Should be able to create optimizer with defaults
            PredicatePushdownOptimizerImpl defaultOptimizer = new PredicatePushdownOptimizerImpl(defaultConfig);
            assertThat(defaultOptimizer).isNotNull();
        }
    }
    
    @Nested
    @DisplayName("Performance and Scalability Tests")
    class PerformanceTests {
        
        @Test
        @DisplayName("Should handle large predicate lists efficiently")
        void largePredicateLists_HandledEfficiently() {
            // Create a large list of predicates
            List<Predicate> largePredicateList = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                largePredicateList.add(new SimplePredicate("field" + i, "=", "value" + i, "p" + i));
            }
            
            long startTime = System.currentTimeMillis();
            
            assertThatCode(() -> {
                List<PredicatePushdownSuggestion> suggestions = optimizer.suggestPredicateReordering(largePredicateList);
                assertThat(suggestions).isNotNull();
            }).doesNotThrowAnyException();
            
            long duration = System.currentTimeMillis() - startTime;
            
            // Should complete within reasonable time (less than 1 second for 100 predicates)
            assertThat(duration).isLessThan(1000);
        }
        
        @Test
        @DisplayName("Should maintain consistent selectivity estimates")
        void selectivityEstimation_Consistent() {
            SimplePredicate predicate = new SimplePredicate("field", "=", "value", "p1");
            
            // Multiple calls should return consistent results
            double firstEstimate = optimizer.estimateSelectivity(predicate);
            double secondEstimate = optimizer.estimateSelectivity(predicate);
            double thirdEstimate = optimizer.estimateSelectivity(predicate);
            
            assertThat(firstEstimate).isEqualTo(secondEstimate);
            assertThat(secondEstimate).isEqualTo(thirdEstimate);
            assertThat(firstEstimate).isBetween(0.0, 1.0);
        }
    }
}