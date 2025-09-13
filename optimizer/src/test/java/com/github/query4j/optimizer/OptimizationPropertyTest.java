package com.github.query4j.optimizer;

import com.github.query4j.core.QueryBuilder;
import com.github.query4j.core.criteria.SimplePredicate;
import com.github.query4j.optimizer.index.IndexSuggestion;
import com.github.query4j.optimizer.predicate.PredicatePushdownSuggestion;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.NotEmpty;
import net.jqwik.api.constraints.StringLength;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Property-based tests for query optimization using jqwik.
 * Tests optimizer behavior with randomly generated inputs to ensure
 * robustness and correctness across a wide range of scenarios.
 */
class OptimizationPropertyTest {
    
    @Property
    @Label("Optimization should always return valid result for any configuration")
    void optimizationAlwaysReturnsValidResult(
            @ForAll("validOptimizerConfigs") OptimizerConfig config,
            @ForAll("mockQueryBuilders") QueryBuilder<?> queryBuilder) {
        
        QueryOptimizer optimizer = QueryOptimizer.create(config);
        
        OptimizationResult result = optimizer.optimize(queryBuilder);
        
        // Basic validity checks
        assertThat(result).isNotNull();
        assertThat(result.getAnalysisTimeMs()).isGreaterThanOrEqualTo(0);
        assertThat(result.getSummary()).isNotNull().isNotEmpty();
        assertThat(result.getIndexSuggestions()).isNotNull();
        assertThat(result.getPredicatePushdownSuggestions()).isNotNull();
        assertThat(result.getJoinReorderSuggestions()).isNotNull();
        
        // Check suggestion count consistency
        assertThat(result.getTotalSuggestionCount())
                .isEqualTo(result.getIndexSuggestions().size() + 
                          result.getPredicatePushdownSuggestions().size() + 
                          result.getJoinReorderSuggestions().size());
    }
    
    @Property
    @Label("Index suggestions should have valid SQL DDL")
    void indexSuggestionsGenerateValidSQL(
            @ForAll("indexSuggestions") IndexSuggestion suggestion) {
        
        String sql = suggestion.generateCreateIndexSQL();
        
        assertThat(sql).isNotNull().isNotEmpty();
        assertThat(sql).startsWith("CREATE INDEX");
        assertThat(sql).contains("ON " + suggestion.getTableName());
        assertThat(sql).contains("(");
        assertThat(sql).contains(")");
        
        // Verify all column names are included
        for (String column : suggestion.getColumnNames()) {
            assertThat(sql).contains(column);
        }
    }
    
    @Property
    @Label("Predicate selectivity estimation should be within valid range")
    void predicateSelectivityInValidRange(@ForAll("simplePredicates") SimplePredicate predicate) {
        var optimizer = new com.github.query4j.optimizer.predicate.PredicatePushdownOptimizerImpl(
                OptimizerConfig.defaultConfig());
        
        double selectivity = optimizer.estimateSelectivity(predicate);
        
        assertThat(selectivity).isBetween(0.0, 1.0);
    }
    
    @Property
    @Label("Reordering suggestions should maintain predicate count")
    void predicateReorderingMaintainsCount(
            @ForAll("predicateLists") List<SimplePredicate> predicates) {
        
        var optimizer = new com.github.query4j.optimizer.predicate.PredicatePushdownOptimizerImpl(
                OptimizerConfig.defaultConfig());
        
        List<PredicatePushdownSuggestion> suggestions = 
                optimizer.suggestPredicateReordering(
                        predicates.stream().map(p -> (com.github.query4j.core.criteria.Predicate) p).toList());
        
        // Should not suggest more reorderings than predicates available
        assertThat(suggestions).hasSizeLessThanOrEqualTo(predicates.size());
    }
    
    @Property
    @Label("Optimization timeout should be respected")
    void optimizationRespectsTimeout(
            @ForAll @IntRange(min = 1, max = 100) int timeoutMs,
            @ForAll("mockQueryBuilders") QueryBuilder<?> queryBuilder) {
        
        OptimizerConfig config = OptimizerConfig.builder()
                .maxAnalysisTimeMs(timeoutMs)
                .build();
        
        QueryOptimizer optimizer = QueryOptimizer.create(config);
        
        long startTime = System.currentTimeMillis();
        try {
            OptimizationResult result = optimizer.optimize(queryBuilder);
            long actualTime = System.currentTimeMillis() - startTime;
            
            // If no timeout occurred, result should be valid
            assertThat(result).isNotNull();
            assertThat(result.getAnalysisTimeMs()).isLessThanOrEqualTo(actualTime + 50); // Allow some tolerance
        } catch (OptimizationException e) {
            // Timeout is acceptable behavior
            assertThat(e.getMessage()).contains("timeout");
        }
    }
    
    @Property
    @Label("Configuration immutability should be preserved")
    void configurationImmutabilityPreserved(@ForAll("validOptimizerConfigs") OptimizerConfig originalConfig) {
        QueryOptimizer optimizer1 = QueryOptimizer.create(originalConfig);
        
        OptimizerConfig newConfig = OptimizerConfig.builder()
                .indexSuggestionsEnabled(originalConfig.isIndexSuggestionsEnabled())
                .predicatePushdownEnabled(originalConfig.isPredicatePushdownEnabled())
                .joinReorderingEnabled(originalConfig.isJoinReorderingEnabled())
                .indexSelectivityThreshold(originalConfig.getIndexSelectivityThreshold())
                .predicateReorderingThreshold(originalConfig.getPredicateReorderingThreshold())
                .joinReorderingThreshold(originalConfig.getJoinReorderingThreshold())
                .maxAnalysisTimeMs(originalConfig.getMaxAnalysisTimeMs())
                .maxCompositeIndexColumns(originalConfig.getMaxCompositeIndexColumns())
                .targetDatabase(originalConfig.getTargetDatabase())
                .verboseOutput(!originalConfig.isVerboseOutput()) // Changed field
                .build();
        
        QueryOptimizer optimizer2 = optimizer1.withConfig(newConfig);
        
        // Original optimizer should be unchanged
        assertThat(optimizer1.getConfig()).isEqualTo(originalConfig);
        assertThat(optimizer2.getConfig()).isEqualTo(newConfig);
        assertThat(optimizer1).isNotSameAs(optimizer2);
    }
    
    // Generators for property-based testing
    
    @Provide
    Arbitrary<OptimizerConfig> validOptimizerConfigs() {
        return Builders.withBuilder(OptimizerConfig::builder)
                .use(Arbitraries.of(true, false)).in((builder, enabled) -> builder.indexSuggestionsEnabled(enabled))
                .use(Arbitraries.of(true, false)).in((builder, enabled) -> builder.predicatePushdownEnabled(enabled))
                .use(Arbitraries.of(true, false)).in((builder, enabled) -> builder.joinReorderingEnabled(enabled))
                .use(Arbitraries.doubles().between(0.0, 1.0)).in((builder, threshold) -> builder.indexSelectivityThreshold(threshold))
                .use(Arbitraries.integers().between(100, 10000)).in((builder, timeout) -> builder.maxAnalysisTimeMs(timeout))
                .use(Arbitraries.of(true, false)).in((builder, verbose) -> builder.verboseOutput(verbose))
                .build(OptimizerConfig.OptimizerConfigBuilder::build);
    }
    
    @Provide
    Arbitrary<QueryBuilder<?>> mockQueryBuilders() {
        // Create simple mock QueryBuilder instances for testing
        return Arbitraries.just(QueryBuilder.forEntity(TestEntity.class));
    }
    
    @Provide
    Arbitrary<IndexSuggestion> indexSuggestions() {
        return Builders.withBuilder(IndexSuggestion::builder)
                .use(tableNames()).in((builder, table) -> builder.tableName(table))
                .use(columnNameLists()).in((builder, columns) -> builder.columnNames(columns))
                .use(Arbitraries.of(IndexSuggestion.IndexType.values())).in((builder, type) -> builder.indexType(type))
                .use(Arbitraries.doubles().between(0.0, 1.0)).in((builder, selectivity) -> builder.selectivity(selectivity))
                .use(reasonStrings()).in((builder, reason) -> builder.reason(reason))
                .use(Arbitraries.of(IndexSuggestion.Priority.values())).in((builder, priority) -> builder.priority(priority))
                .build(IndexSuggestion.IndexSuggestionBuilder::build);
    }
    
    @Provide
    Arbitrary<SimplePredicate> simplePredicates() {
        return Arbitraries.create(() -> {
            String field = fieldNames().sample();
            String operator = operators().sample(); 
            Object value = predicateValues().sample();
            String param = parameterNames().sample();
            return new SimplePredicate(field, operator, value, param);
        });
    }
    
    @Provide
    Arbitrary<List<SimplePredicate>> predicateLists() {
        return simplePredicates().list().ofMinSize(1).ofMaxSize(10);
    }
    
    @Provide
    Arbitrary<String> tableNames() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(3)
                .ofMaxLength(20)
                .map(s -> s + "s"); // Make it plural like table names
    }
    
    @Provide
    Arbitrary<List<String>> columnNameLists() {
        return columnNames().list().ofMinSize(1).ofMaxSize(5);
    }
    
    @Provide
    Arbitrary<String> columnNames() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(2)
                .ofMaxLength(15);
    }
    
    @Provide
    Arbitrary<String> fieldNames() {
        return Arbitraries.of("id", "name", "email", "status", "created_date", "user_id", "amount");
    }
    
    @Provide
    Arbitrary<String> operators() {
        return Arbitraries.of("=", "!=", "<", "<=", ">", ">=", "LIKE");
    }
    
    @Provide
    Arbitrary<Object> predicateValues() {
        return Arbitraries.oneOf(
                Arbitraries.strings().ofMinLength(1).ofMaxLength(50),
                Arbitraries.integers().between(1, 1000),
                Arbitraries.doubles().between(0.0, 1000.0),
                Arbitraries.of(true, false)
        );
    }
    
    @Provide
    Arbitrary<String> parameterNames() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(2)
                .ofMaxLength(10)
                .map(s -> "p" + s);
    }
    
    @Provide
    Arbitrary<String> reasonStrings() {
        return Arbitraries.of(
                "Equality predicate optimization",
                "Range query optimization",
                "Join optimization",
                "Composite index for multi-column queries",
                "Performance improvement"
        );
    }
    
    // Simple test entity for QueryBuilder creation
    public static class TestEntity {
        private Long id;
        private String name;
        private String email;
        
        // Getters and setters would be here in a real entity
    }
}