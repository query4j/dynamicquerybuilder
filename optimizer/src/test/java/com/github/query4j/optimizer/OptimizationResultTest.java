package com.github.query4j.optimizer;

import com.github.query4j.optimizer.index.IndexSuggestion;
import com.github.query4j.optimizer.join.JoinReorderSuggestion;
import com.github.query4j.optimizer.predicate.PredicatePushdownSuggestion;
import com.github.query4j.core.criteria.SimplePredicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for OptimizationResult to improve code coverage.
 */
@DisplayName("OptimizationResult Tests")
class OptimizationResultTest {
    
    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {
        
        @Test
        @DisplayName("Should create result with required fields")
        void shouldCreateResultWithRequiredFields() {
            OptimizationResult result = OptimizationResult.builder()
                    .analysisTimeMs(1000)
                    .summary("Analysis complete")
                    .build();
            
            assertThat(result.getAnalysisTimeMs()).isEqualTo(1000);
            assertThat(result.getSummary()).isEqualTo("Analysis complete");
            assertThat(result.getIndexSuggestions()).isEmpty();
            assertThat(result.getPredicatePushdownSuggestions()).isEmpty();
            assertThat(result.getJoinReorderSuggestions()).isEmpty();
        }
        
        @Test
        @DisplayName("Should create result with all fields")
        void shouldCreateResultWithAllFields() {
            List<IndexSuggestion> indexSuggestions = List.of(
                    IndexSuggestion.builder()
                            .tableName("users")
                            .columnNames(List.of("email"))
                            .indexType(IndexSuggestion.IndexType.BTREE)
                            .selectivity(0.95)
                            .reason("High cardinality column")
                            .priority(IndexSuggestion.Priority.HIGH)
                            .build()
            );
            
            List<PredicatePushdownSuggestion> predicateSuggestions = List.of(
                    PredicatePushdownSuggestion.builder()
                            .originalPosition(1)
                            .suggestedPosition(0)
                            .originalPredicate(new SimplePredicate("status", "=", "active", "status"))
                            .optimizationType(PredicatePushdownSuggestion.OptimizationType.REORDER_BY_SELECTIVITY)
                            .selectivity(0.1)
                            .reason("High selectivity predicate")
                            .build()
            );
            
            List<JoinReorderSuggestion> joinSuggestions = List.of(
                    JoinReorderSuggestion.builder()
                            .originalJoinSequence(List.of("table1", "table2"))
                            .suggestedJoinSequence(List.of("table2", "table1"))
                            .reorderType(JoinReorderSuggestion.JoinReorderType.SELECTIVITY_BASED)
                            .reason("Better selectivity")
                            .build()
            );
            
            OptimizationResult result = OptimizationResult.builder()
                    .analysisTimeMs(2500)
                    .summary("Found 3 optimization opportunities")
                    .indexSuggestions(indexSuggestions)
                    .predicatePushdownSuggestions(predicateSuggestions)
                    .joinReorderSuggestions(joinSuggestions)
                    .build();
            
            assertThat(result.getAnalysisTimeMs()).isEqualTo(2500);
            assertThat(result.getSummary()).isEqualTo("Found 3 optimization opportunities");
            assertThat(result.getIndexSuggestions()).hasSize(1);
            assertThat(result.getPredicatePushdownSuggestions()).hasSize(1);
            assertThat(result.getJoinReorderSuggestions()).hasSize(1);
        }
    }
    
    @Nested
    @DisplayName("Calculation Methods Tests")
    class CalculationMethodsTests {
        
        @Test
        @DisplayName("Should calculate total suggestion count correctly")
        void shouldCalculateTotalSuggestionCountCorrectly() {
            List<IndexSuggestion> indexSuggestions = List.of(
                    createIndexSuggestion("table1", List.of("col1")),
                    createIndexSuggestion("table2", List.of("col2"))
            );
            
            List<PredicatePushdownSuggestion> predicateSuggestions = List.of(
                    createPredicateSuggestion("field1", 0, 1),
                    createPredicateSuggestion("field2", 1, 0),
                    createPredicateSuggestion("field3", 2, 1)
            );
            
            List<JoinReorderSuggestion> joinSuggestions = List.of(
                    createJoinSuggestion(List.of("a", "b"), List.of("b", "a"))
            );
            
            OptimizationResult result = OptimizationResult.builder()
                    .analysisTimeMs(1000)
                    .summary("Multiple suggestions available")
                    .indexSuggestions(indexSuggestions)
                    .predicatePushdownSuggestions(predicateSuggestions)
                    .joinReorderSuggestions(joinSuggestions)
                    .build();
            
            assertThat(result.getTotalSuggestionCount()).isEqualTo(6); // 2 + 3 + 1
        }
        
        @Test
        @DisplayName("Should return zero for empty suggestions")
        void shouldReturnZeroForEmptySuggestions() {
            OptimizationResult result = OptimizationResult.builder()
                    .analysisTimeMs(500)
                    .summary("No optimizations needed")
                    .build();
            
            assertThat(result.getTotalSuggestionCount()).isEqualTo(0);
        }
        
        @Test
        @DisplayName("Should detect when suggestions exist")
        void shouldDetectWhenSuggestionsExist() {
            OptimizationResult resultWithSuggestions = OptimizationResult.builder()
                    .analysisTimeMs(1000)
                    .summary("Found suggestions")
                    .indexSuggestions(List.of(createIndexSuggestion("table1", List.of("col1"))))
                    .build();
            
            OptimizationResult resultWithoutSuggestions = OptimizationResult.builder()
                    .analysisTimeMs(1000)
                    .summary("No suggestions")
                    .build();
            
            assertThat(resultWithSuggestions.hasSuggestions()).isTrue();
            assertThat(resultWithoutSuggestions.hasSuggestions()).isFalse();
        }
    }
    
    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {
        
        @Test
        @DisplayName("Should be immutable - lists cannot be modified")
        void shouldBeImmutableListsCannotBeModified() {
            OptimizationResult result = OptimizationResult.builder()
                    .analysisTimeMs(1000)
                    .summary("Test result")
                    .indexSuggestions(List.of(createIndexSuggestion("table1", List.of("col1"))))
                    .predicatePushdownSuggestions(List.of(createPredicateSuggestion("field1", 0, 1)))
                    .joinReorderSuggestions(List.of(createJoinSuggestion(List.of("a", "b"), List.of("b", "a"))))
                    .build();
            
            // Lists should be unmodifiable
            assertThatThrownBy(() -> result.getIndexSuggestions().clear())
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> result.getPredicatePushdownSuggestions().clear())
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> result.getJoinReorderSuggestions().clear())
                    .isInstanceOf(UnsupportedOperationException.class);
        }
        
        @Test
        @DisplayName("Should maintain equals and hashcode contract")
        void shouldMaintainEqualsAndHashcodeContract() {
            List<IndexSuggestion> indexSuggestions = List.of(
                    createIndexSuggestion("table1", List.of("col1"))
            );
            
            OptimizationResult result1 = OptimizationResult.builder()
                    .analysisTimeMs(1000)
                    .summary("Test summary")
                    .indexSuggestions(indexSuggestions)
                    .build();
            
            OptimizationResult result2 = OptimizationResult.builder()
                    .analysisTimeMs(1000)
                    .summary("Test summary")
                    .indexSuggestions(indexSuggestions)
                    .build();
            
            assertThat(result1).isEqualTo(result2);
            assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
        }
        
        @Test
        @DisplayName("Should have proper toString representation")
        void shouldHaveProperToStringRepresentation() {
            OptimizationResult result = OptimizationResult.builder()
                    .analysisTimeMs(1500)
                    .summary("Email index needed")
                    .indexSuggestions(List.of(createIndexSuggestion("users", List.of("email"))))
                    .build();
            
            String toString = result.toString();
            assertThat(toString).contains("OptimizationResult");
            assertThat(toString).contains("1500");
            assertThat(toString).contains("users");
            assertThat(toString).contains("email");
        }
    }
    
    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {
        
        @Test
        @DisplayName("Should handle large analysis times")
        void shouldHandleLargeAnalysisTimes() {
            OptimizationResult result = OptimizationResult.builder()
                    .analysisTimeMs(Long.MAX_VALUE)
                    .summary("Long analysis")
                    .build();
            
            assertThat(result.getAnalysisTimeMs()).isEqualTo(Long.MAX_VALUE);
        }
        
        @Test
        @DisplayName("Should handle negative analysis times")
        void shouldHandleNegativeAnalysisTimes() {
            // Although semantically incorrect, test that it doesn't break
            OptimizationResult result = OptimizationResult.builder()
                    .analysisTimeMs(-1)
                    .summary("Invalid time")
                    .build();
            
            assertThat(result.getAnalysisTimeMs()).isEqualTo(-1);
        }
        
        @Test
        @DisplayName("Should handle empty summary")
        void shouldHandleEmptySummary() {
            OptimizationResult result = OptimizationResult.builder()
                    .analysisTimeMs(1000)
                    .summary("")
                    .build();
            
            assertThat(result.getSummary()).isEmpty();
        }
    }
    
    // Helper methods for creating test data
    private IndexSuggestion createIndexSuggestion(String tableName, List<String> columnNames) {
        return IndexSuggestion.builder()
                .tableName(tableName)
                .columnNames(columnNames)
                .indexType(IndexSuggestion.IndexType.BTREE)
                .selectivity(0.9)
                .reason("Test reason")
                .priority(IndexSuggestion.Priority.MEDIUM)
                .build();
    }
    
    private PredicatePushdownSuggestion createPredicateSuggestion(String fieldName, int originalPos, int suggestedPos) {
        return PredicatePushdownSuggestion.builder()
                .originalPosition(originalPos)
                .suggestedPosition(suggestedPos)
                .originalPredicate(new SimplePredicate(fieldName, "=", "value", fieldName))
                .optimizationType(PredicatePushdownSuggestion.OptimizationType.REORDER_BY_SELECTIVITY)
                .selectivity(0.1)
                .reason("Test reason")
                .build();
    }
    
    private JoinReorderSuggestion createJoinSuggestion(List<String> original, List<String> suggested) {
        return JoinReorderSuggestion.builder()
                .originalJoinSequence(original)
                .suggestedJoinSequence(suggested)
                .reorderType(JoinReorderSuggestion.JoinReorderType.SELECTIVITY_BASED)
                .reason("Test reason")
                .build();
    }
}