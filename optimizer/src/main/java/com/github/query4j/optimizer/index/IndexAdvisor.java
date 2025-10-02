package com.github.query4j.optimizer.index;

import com.github.query4j.core.QueryBuilder;
import com.github.query4j.core.criteria.Predicate;

import java.util.List;

/**
 * Interface for analyzing predicates and join conditions to suggest database indexes
 * that could improve query performance. Implementations analyze query patterns,
 * predicate selectivity, and join key usage to recommend optimal indexing strategies.
 *
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface IndexAdvisor {
    
    /**
     * Analyzes a query builder to suggest indexes based on predicates and joins.
     *
     * @param queryBuilder the query builder to analyze, must not be null
     * @return list of index suggestions, never null but may be empty
     * @throws IllegalArgumentException if queryBuilder is null
     */
    List<IndexSuggestion> analyzeQuery(QueryBuilder<?> queryBuilder);
    
    /**
     * Analyzes individual predicates to suggest single-column indexes.
     *
     * @param predicates list of predicates to analyze, must not be null
     * @param tableName the target table name, must not be null or empty
     * @return list of index suggestions, never null but may be empty
     * @throws IllegalArgumentException if predicates is null or tableName is null/empty
     */
    List<IndexSuggestion> analyzePredicates(List<Predicate> predicates, String tableName);
    
    /**
     * Analyzes join conditions to suggest composite indexes for join optimization.
     *
     * @param joinFields list of fields involved in joins, must not be null
     * @param tableName the target table name, must not be null or empty
     * @return list of composite index suggestions, never null but may be empty
     * @throws IllegalArgumentException if joinFields is null or tableName is null/empty
     */
    List<IndexSuggestion> analyzeJoinConditions(List<String> joinFields, String tableName);
    
    /**
     * Suggests composite indexes when multiple columns are frequently queried together.
     *
     * @param columnUsagePatterns map of column names to their usage frequency
     * @param tableName the target table name, must not be null or empty
     * @param threshold minimum correlation threshold for composite index suggestion (0.0 to 1.0)
     * @return list of composite index suggestions, never null but may be empty
     * @throws IllegalArgumentException if parameters are invalid
     */
    List<IndexSuggestion> suggestCompositeIndexes(
        java.util.Map<String, Integer> columnUsagePatterns, 
        String tableName, 
        double threshold
    );
}