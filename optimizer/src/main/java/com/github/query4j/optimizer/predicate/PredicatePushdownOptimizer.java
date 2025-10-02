package com.github.query4j.optimizer.predicate;

import com.github.query4j.core.QueryBuilder;
import com.github.query4j.core.criteria.Predicate;

import java.util.List;

/**
 * Interface for analyzing and optimizing predicate placement within queries.
 * Detects predicates that can be pushed closer to data sources and reorders
 * predicates to apply the most selective filters earliest for improved performance.
 *
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface PredicatePushdownOptimizer {
    
    /**
     * Analyzes a query builder to suggest predicate pushdown optimizations.
     *
     * @param queryBuilder the query builder to analyze, must not be null
     * @return list of predicate pushdown suggestions, never null but may be empty
     * @throws IllegalArgumentException if queryBuilder is null
     */
    List<PredicatePushdownSuggestion> optimizeQuery(QueryBuilder<?> queryBuilder);
    
    /**
     * Analyzes a list of predicates and suggests optimal ordering based on selectivity.
     *
     * @param predicates list of predicates to reorder, must not be null
     * @return list of reordering suggestions, never null but may be empty
     * @throws IllegalArgumentException if predicates is null
     */
    List<PredicatePushdownSuggestion> suggestPredicateReordering(List<Predicate> predicates);
    
    /**
     * Estimates the selectivity of a predicate for optimization decisions.
     * Lower selectivity values indicate more selective predicates that should
     * be evaluated earlier.
     *
     * @param predicate the predicate to analyze, must not be null
     * @return selectivity estimate between 0.0 (most selective) and 1.0 (least selective)
     * @throws IllegalArgumentException if predicate is null
     */
    double estimateSelectivity(Predicate predicate);
    
    /**
     * Identifies predicates that can be pushed to specific join sources.
     *
     * @param predicates list of predicates to analyze, must not be null
     * @param joinGraph representation of join relationships, must not be null
     * @return list of pushdown suggestions, never null but may be empty
     * @throws IllegalArgumentException if parameters are null
     */
    List<PredicatePushdownSuggestion> identifyPushdownOpportunities(
        List<Predicate> predicates, 
        JoinGraph joinGraph
    );
    
    /**
     * Suggests predicate reordering to maximize index usage efficiency.
     *
     * @param predicates list of predicates to analyze, must not be null
     * @param availableIndexes list of available indexes on the table, must not be null
     * @return list of index-friendly reordering suggestions, never null but may be empty
     * @throws IllegalArgumentException if parameters are null
     */
    List<PredicatePushdownSuggestion> optimizeForIndexUsage(
        List<Predicate> predicates, 
        List<String> availableIndexes
    );
    
    /**
     * Simple representation of join relationships for pushdown analysis.
     */
    interface JoinGraph {
        /**
         * Returns the tables involved in the join graph.
         * @return set of table names, never null
         */
        java.util.Set<String> getTables();
        
        /**
         * Returns the join relationships between tables.
         * @return map of table pairs to join conditions, never null
         */
        java.util.Map<String, java.util.Set<String>> getJoinRelationships();
        
        /**
         * Checks if a predicate can be pushed to a specific table.
         * @param predicate the predicate to check, must not be null
         * @param tableName the target table, must not be null or empty
         * @return true if pushdown is possible
         */
        boolean canPushToTable(Predicate predicate, String tableName);
    }
}