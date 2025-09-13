package com.github.query4j.optimizer.index;

import com.github.query4j.core.QueryBuilder;
import com.github.query4j.core.criteria.Predicate;
import com.github.query4j.core.criteria.SimplePredicate;
import com.github.query4j.core.criteria.InPredicate;
import com.github.query4j.core.criteria.BetweenPredicate;
import com.github.query4j.core.criteria.LikePredicate;
import com.github.query4j.optimizer.OptimizerConfig;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of IndexAdvisor that analyzes predicates and join conditions
 * to suggest database indexes for improved query performance.
 *
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
@RequiredArgsConstructor
public class IndexAdvisorImpl implements IndexAdvisor {
    
    @NonNull
    private final OptimizerConfig config;
    
    @Override
    public List<IndexSuggestion> analyzeQuery(QueryBuilder<?> queryBuilder) {
        if (queryBuilder == null) {
            throw new IllegalArgumentException("QueryBuilder must not be null");
        }
        
        List<IndexSuggestion> suggestions = new ArrayList<>();
        
        try {
            // Get the SQL to analyze predicates - this is a simplified approach
            String sql = queryBuilder.toSQL();
            String entityName = extractEntityName(queryBuilder);
            
            // For now, we'll create some basic suggestions based on common patterns
            // In a full implementation, this would parse the SQL or analyze the QueryBuilder structure
            suggestions.addAll(createBasicIndexSuggestions(entityName));
            
        } catch (Exception e) {
            // Log and continue - don't fail optimization for index analysis issues
            // In a production system, this would use proper logging
        }
        
        return suggestions;
    }
    
    @Override
    public List<IndexSuggestion> analyzePredicates(List<Predicate> predicates, String tableName) {
        if (predicates == null) {
            throw new IllegalArgumentException("Predicates list must not be null");
        }
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name must not be null or empty");
        }
        
        List<IndexSuggestion> suggestions = new ArrayList<>();
        
        for (Predicate predicate : predicates) {
            suggestions.addAll(analyzeSinglePredicate(predicate, tableName));
        }
        
        return suggestions;
    }
    
    @Override
    public List<IndexSuggestion> analyzeJoinConditions(List<String> joinFields, String tableName) {
        if (joinFields == null) {
            throw new IllegalArgumentException("Join fields list must not be null");
        }
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name must not be null or empty");
        }
        
        List<IndexSuggestion> suggestions = new ArrayList<>();
        
        for (String joinField : joinFields) {
            if (joinField != null && !joinField.trim().isEmpty()) {
                suggestions.add(IndexSuggestion.builder()
                        .tableName(tableName)
                        .columnNames(List.of(joinField))
                        .indexType(IndexSuggestion.IndexType.BTREE)
                        .selectivity(0.3) // Reasonable default for join columns
                        .reason("Join optimization for field: " + joinField)
                        .expectedImpact("Significant improvement for join operations")
                        .priority(IndexSuggestion.Priority.HIGH)
                        .build());
            }
        }
        
        return suggestions;
    }
    
    @Override
    public List<IndexSuggestion> suggestCompositeIndexes(
            Map<String, Integer> columnUsagePatterns, 
            String tableName, 
            double threshold) {
        
        if (columnUsagePatterns == null) {
            throw new IllegalArgumentException("Column usage patterns must not be null");
        }
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name must not be null or empty");
        }
        if (threshold < 0.0 || threshold > 1.0) {
            throw new IllegalArgumentException("Threshold must be between 0.0 and 1.0");
        }
        
        List<IndexSuggestion> suggestions = new ArrayList<>();
        
        // Find columns that are frequently used together
        List<String> frequentColumns = columnUsagePatterns.entrySet().stream()
                .filter(entry -> entry.getValue() > (columnUsagePatterns.size() * threshold))
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());
        
        // Create composite index suggestions for frequently used column combinations
        if (frequentColumns.size() >= 2 && frequentColumns.size() <= config.getMaxCompositeIndexColumns()) {
            suggestions.add(IndexSuggestion.builder()
                    .tableName(tableName)
                    .columnNames(frequentColumns)
                    .indexType(IndexSuggestion.IndexType.COMPOSITE)
                    .selectivity(threshold)
                    .reason("Composite index for frequently queried columns: " + String.join(", ", frequentColumns))
                    .expectedImpact("Improved performance for multi-column queries")
                    .priority(IndexSuggestion.Priority.MEDIUM)
                    .build());
        }
        
        return suggestions;
    }
    
    /**
     * Analyzes a single predicate and suggests appropriate indexes.
     */
    private List<IndexSuggestion> analyzeSinglePredicate(Predicate predicate, String tableName) {
        List<IndexSuggestion> suggestions = new ArrayList<>();
        
        if (predicate instanceof SimplePredicate) {
            SimplePredicate simple = (SimplePredicate) predicate;
            suggestions.add(createIndexSuggestionForField(simple.getField(), tableName, 
                    "Equality/comparison predicate", 0.2));
        } else if (predicate instanceof InPredicate) {
            InPredicate inPredicate = (InPredicate) predicate;
            suggestions.add(createIndexSuggestionForField(inPredicate.getField(), tableName,
                    "IN clause optimization", 0.4));
        } else if (predicate instanceof BetweenPredicate) {
            BetweenPredicate between = (BetweenPredicate) predicate;
            suggestions.add(createIndexSuggestionForField(between.getField(), tableName,
                    "Range query optimization", 0.3));
        } else if (predicate instanceof LikePredicate) {
            LikePredicate like = (LikePredicate) predicate;
            suggestions.add(IndexSuggestion.builder()
                    .tableName(tableName)
                    .columnNames(List.of(like.getField()))
                    .indexType(IndexSuggestion.IndexType.BTREE)
                    .selectivity(0.6) // LIKE patterns are usually less selective
                    .reason("Text search optimization (consider full-text index for complex patterns)")
                    .expectedImpact("Moderate improvement for LIKE operations")
                    .priority(IndexSuggestion.Priority.MEDIUM)
                    .build());
        }
        
        return suggestions;
    }
    
    /**
     * Creates a basic index suggestion for a field.
     */
    private IndexSuggestion createIndexSuggestionForField(String fieldName, String tableName, 
                                                        String reason, double selectivity) {
        return IndexSuggestion.builder()
                .tableName(tableName)
                .columnNames(List.of(fieldName))
                .indexType(IndexSuggestion.IndexType.BTREE)
                .selectivity(Math.max(selectivity, config.getIndexSelectivityThreshold()))
                .reason(reason + " for field: " + fieldName)
                .expectedImpact("Improved query performance")
                .priority(selectivity < 0.1 ? IndexSuggestion.Priority.HIGH : 
                         selectivity < 0.3 ? IndexSuggestion.Priority.MEDIUM : 
                         IndexSuggestion.Priority.LOW)
                .build();
    }
    
    /**
     * Extracts entity name from QueryBuilder - simplified implementation.
     */
    private String extractEntityName(QueryBuilder<?> queryBuilder) {
        // This is a simplified implementation
        // In reality, we'd need to access the entity class from the QueryBuilder
        return "entity_table";
    }
    
    /**
     * Creates basic index suggestions for common patterns.
     */
    private List<IndexSuggestion> createBasicIndexSuggestions(String entityName) {
        List<IndexSuggestion> suggestions = new ArrayList<>();
        
        // Common ID field suggestion
        suggestions.add(IndexSuggestion.builder()
                .tableName(entityName)
                .columnNames(List.of("id"))
                .indexType(IndexSuggestion.IndexType.UNIQUE)
                .selectivity(0.01) // Very selective
                .reason("Primary key optimization")
                .expectedImpact("Essential for entity lookups")
                .priority(IndexSuggestion.Priority.HIGH)
                .build());
        
        return suggestions;
    }
}