package com.github.query4j.optimizer.join;

import com.github.query4j.optimizer.OptimizerConfig;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;
import net.jqwik.api.constraints.DoubleRange;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Property-based tests for join optimization functionality using jqwik
 * to ensure robustness across various input combinations.
 */
class JoinOptimizerPropertyTest {
    
    @Property
    @Label("Join sequence analysis should never throw exceptions with valid inputs")
    void joinSequenceAnalysisShouldNeverThrow(
            @ForAll("validJoinSequences") List<String> joinSequence,
            @ForAll("validJoinConditions") List<JoinReorderSuggestion.JoinCondition> conditions) {
        
        JoinReorderOptimizerImpl optimizer = new JoinReorderOptimizerImpl(OptimizerConfig.defaultConfig());
        
        assertThatCode(() -> optimizer.analyzeJoinSequence(joinSequence, conditions))
                .doesNotThrowAnyException();
    }
    
    @Property
    @Label("Cardinality reduction estimation should always return valid range")
    void cardinalityReductionShouldReturnValidRange(
            @ForAll("validJoinSequences") List<String> joinSequence,
            @ForAll("validTableStatistics") TestTableStatistics stats) {
        
        JoinReorderOptimizerImpl optimizer = new JoinReorderOptimizerImpl(OptimizerConfig.defaultConfig());
        
        double reduction = optimizer.estimateCardinalityReduction(joinSequence, stats);

        assertThat(reduction).isGreaterThanOrEqualTo(0.0).isLessThanOrEqualTo(1.0);
    }
    
    @Property
    @Label("Index-based optimization should never throw exceptions")
    void indexBasedOptimizationShouldNeverThrow(
            @ForAll("validJoinSequences") List<String> joinSequence,
            @ForAll("validIndexInformation") Map<String, List<String>> indexInfo) {
        
        JoinReorderOptimizerImpl optimizer = new JoinReorderOptimizerImpl(OptimizerConfig.defaultConfig());
        
        assertThatCode(() -> optimizer.optimizeForIndexUsage(joinSequence, indexInfo))
                .doesNotThrowAnyException();
    }
    
    @Property
    @Label("Join suggestions should maintain sequence size")
    void joinSuggestionsShouldMaintainSequenceSize(
            @ForAll("validJoinSequences") List<String> joinSequence,
            @ForAll("validJoinConditions") List<JoinReorderSuggestion.JoinCondition> conditions) {
        
        JoinReorderOptimizerImpl optimizer = new JoinReorderOptimizerImpl(OptimizerConfig.defaultConfig());
        
        List<JoinReorderSuggestion> suggestions = optimizer.analyzeJoinSequence(joinSequence, conditions);
        
        for (JoinReorderSuggestion suggestion : suggestions) {
            assertThat(suggestion.getSuggestedJoinSequence())
                    .hasSameSizeAs(suggestion.getOriginalJoinSequence());
        }
    }
    
    @Property
    @Label("Join suggestions should contain same tables as original")
    void joinSuggestionsShouldContainSameTables(
            @ForAll("validJoinSequences") List<String> joinSequence,
            @ForAll("validJoinConditions") List<JoinReorderSuggestion.JoinCondition> conditions) {
        
        JoinReorderOptimizerImpl optimizer = new JoinReorderOptimizerImpl(OptimizerConfig.defaultConfig());
        
        List<JoinReorderSuggestion> suggestions = optimizer.analyzeJoinSequence(joinSequence, conditions);
        
        for (JoinReorderSuggestion suggestion : suggestions) {
            assertThat(suggestion.getSuggestedJoinSequence())
                    .containsExactlyInAnyOrderElementsOf(suggestion.getOriginalJoinSequence());
        }
    }
    
    @Property
    @Label("Estimated improvement should be within valid range")
    void estimatedImprovementShouldBeWithinValidRange(
            @ForAll("validJoinSequences") List<String> joinSequence,
            @ForAll("validJoinConditions") List<JoinReorderSuggestion.JoinCondition> conditions) {
        
        JoinReorderOptimizerImpl optimizer = new JoinReorderOptimizerImpl(OptimizerConfig.defaultConfig());
        
        List<JoinReorderSuggestion> suggestions = optimizer.analyzeJoinSequence(joinSequence, conditions);
        
        for (JoinReorderSuggestion suggestion : suggestions) {
            assertThat(suggestion.getEstimatedImprovement()).isBetween(0.0, 1.0);
        }
    }
    
    @Property
    @Label("Join condition builder should handle all selectivity values")
    void joinConditionBuilderShouldHandleAllSelectivityValues(
            @ForAll("validTableName") String leftTable,
            @ForAll("validTableName") String rightTable,
            @ForAll("validFieldName") String joinField,
            @ForAll @DoubleRange(min = 0.0, max = 1.0) double selectivity) {
        
        assertThatCode(() -> {
            JoinReorderSuggestion.JoinCondition condition = JoinReorderSuggestion.JoinCondition.builder()
                    .leftTable(leftTable)
                    .rightTable(rightTable)
                    .joinField(joinField)
                    .selectivity(selectivity)
                    .hasIndex(true)
                    .build();
            
            assertThat(condition.getSelectivity()).isEqualTo(selectivity);
            assertThat(condition.isHasIndex()).isTrue();
        }).doesNotThrowAnyException();
    }
    
    @Property
    @Label("Sequence change count should never exceed sequence size")
    void sequenceChangeCountShouldNeverExceedSequenceSize(
            @ForAll("validJoinSequences") List<String> original,
            @ForAll("validJoinSequences") List<String> suggested) {
        
        // Only test when sequences have same size
        Assume.that(original.size() == suggested.size());
        
        JoinReorderSuggestion suggestion = JoinReorderSuggestion.builder()
                .originalJoinSequence(original)
                .suggestedJoinSequence(suggested)
                .reorderType(JoinReorderSuggestion.JoinReorderType.SELECTIVITY_BASED)
                .reason("Test")
                .build();
        
        assertThat(suggestion.getSequenceChangeCount()).isLessThanOrEqualTo(original.size());
    }
    
    @Property
    @Label("All enum values should have display names")
    void allEnumValuesShouldHaveDisplayNames() {
        for (JoinReorderSuggestion.JoinReorderType type : JoinReorderSuggestion.JoinReorderType.values()) {
            assertThat(type.getDisplayName()).isNotNull().isNotEmpty();
        }
        
        for (JoinReorderSuggestion.Priority priority : JoinReorderSuggestion.Priority.values()) {
            assertThat(priority.getDescription()).isNotNull().isNotEmpty();
        }
    }
    
    // Arbitraries for generating test data
    
    @Provide
    Arbitrary<List<String>> validJoinSequences() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(1).ofMaxLength(10)
                .map(s -> "table_" + s)
                .list()
                .ofMinSize(0).ofMaxSize(5);
    }
    
    @Provide
    Arbitrary<List<JoinReorderSuggestion.JoinCondition>> validJoinConditions() {
        return validJoinCondition().list().ofMaxSize(10);
    }
    
    @Provide
    Arbitrary<JoinReorderSuggestion.JoinCondition> validJoinCondition() {
        return Combinators.combine(
                validTableName(),
                validTableName(),
                validFieldName(),
                Arbitraries.doubles().between(0.0, 1.0),
                Arbitraries.of(true, false)
        ).as((leftTable, rightTable, field, selectivity, hasIndex) ->
                JoinReorderSuggestion.JoinCondition.builder()
                        .leftTable(leftTable)
                        .rightTable(rightTable)
                        .joinField(field)
                        .selectivity(selectivity)
                        .hasIndex(hasIndex)
                        .build()
        );
    }
    
    @Provide
    Arbitrary<String> validTableName() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(1).ofMaxLength(20)
                .map(s -> "table_" + s);
    }
    
    @Provide
    Arbitrary<String> validFieldName() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(1).ofMaxLength(20)
                .map(s -> "field_" + s);
    }
    
    @Provide
    Arbitrary<TestTableStatistics> validTableStatistics() {
        return Arbitraries.just(new TestTableStatistics())
                .map(stats -> {
                    stats.withRowCount("table_small", 1000);
                    stats.withRowCount("table_medium", 10000);
                    stats.withRowCount("table_large", 100000);
                    stats.withJoinSelectivity("table_small", "table_medium", "id", 0.1);
                    stats.withJoinSelectivity("table_medium", "table_large", "id", 0.05);
                    return stats;
                });
    }
    
    @Provide
    Arbitrary<Map<String, List<String>>> validIndexInformation() {
        return Arbitraries.maps(
                validTableName(),
                Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(10)
                        .map(s -> "idx_" + s)
                        .list().ofMaxSize(5)
        ).ofMaxSize(10);
    }
    
    /**
     * Test implementation of TableStatistics for property-based testing.
     */
    private static class TestTableStatistics implements JoinReorderOptimizer.TableStatistics {
        private final Map<String, Long> rowCounts = new HashMap<>();
        private final Map<String, Double> joinSelectivities = new HashMap<>();
        private final Map<String, Boolean> indexInfo = new HashMap<>();
        
        public TestTableStatistics withRowCount(String table, long count) {
            rowCounts.put(table, count);
            return this;
        }
        
        public TestTableStatistics withJoinSelectivity(String leftTable, String rightTable, String field, double selectivity) {
            joinSelectivities.put(leftTable + "." + rightTable + "." + field, selectivity);
            return this;
        }
        
        public TestTableStatistics withIndex(String table, String field, boolean hasIndex) {
            indexInfo.put(table + "." + field, hasIndex);
            return this;
        }
        
        @Override
        public long getEstimatedRowCount(String tableName) {
            return rowCounts.getOrDefault(tableName, 1000L); // Default reasonable value
        }
        
        @Override
        public double getJoinSelectivity(String leftTable, String rightTable, String joinField) {
            return joinSelectivities.getOrDefault(leftTable + "." + rightTable + "." + joinField, 0.1);
        }
        
        @Override
        public boolean hasIndexOnField(String tableName, String fieldName) {
            return indexInfo.getOrDefault(tableName + "." + fieldName, false);
        }
    }
}