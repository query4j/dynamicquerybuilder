package com.github.query4j.optimizer.join;

import com.github.query4j.core.QueryBuilder;

import java.util.List;

/**
 * Interface for analyzing join graphs and suggesting optimal join sequences
 * to minimize intermediate result sizes and improve query performance.
 * Considers predicate selectivity, table cardinality, and available indexes.
 *
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface JoinReorderOptimizer {
    
    /**
     * Analyzes a query builder to suggest join reordering optimizations.
     *
     * @param queryBuilder the query builder to analyze, must not be null
     * @return list of join reordering suggestions, never null but may be empty
     * @throws IllegalArgumentException if queryBuilder is null
     */
    List<JoinReorderSuggestion> optimizeJoinOrder(QueryBuilder<?> queryBuilder);
    
    /**
     * Analyzes a join sequence and suggests optimal reordering based on selectivity.
     *
     * @param joinSequence current sequence of table names in join order, must not be null
     * @param joinConditions conditions that connect the tables, must not be null
     * @return list of reordering suggestions, never null but may be empty
     * @throws IllegalArgumentException if parameters are null
     */
    List<JoinReorderSuggestion> analyzeJoinSequence(
        List<String> joinSequence, 
        List<JoinReorderSuggestion.JoinCondition> joinConditions
    );
    
    /**
     * Estimates the cardinality reduction potential for a given join sequence.
     *
     * @param joinSequence sequence of table names, must not be null
     * @param tableStatistics statistics for table cardinalities, may be null for estimation
     * @return estimated improvement factor (0.0 to 1.0), higher is better
     * @throws IllegalArgumentException if joinSequence is null
     */
    double estimateCardinalityReduction(
        List<String> joinSequence, 
        TableStatistics tableStatistics
    );
    
    /**
     * Suggests join reordering to maximize usage of available indexes.
     *
     * @param joinSequence current join sequence, must not be null
     * @param indexInformation available indexes per table, must not be null
     * @return list of index-optimized reordering suggestions, never null but may be empty
     * @throws IllegalArgumentException if parameters are null
     */
    List<JoinReorderSuggestion> optimizeForIndexUsage(
        List<String> joinSequence,
        java.util.Map<String, List<String>> indexInformation
    );
    
    /**
     * Interface for providing table statistics to the join optimizer.
     */
    interface TableStatistics {
        /**
         * Returns the estimated row count for a table.
         * @param tableName the table name, must not be null or empty
         * @return estimated row count, or -1 if unknown
         */
        long getEstimatedRowCount(String tableName);
        
        /**
         * Returns the estimated selectivity for a join condition.
         * @param leftTable left side of join, must not be null or empty
         * @param rightTable right side of join, must not be null or empty
         * @param joinField the joining field, must not be null or empty
         * @return selectivity estimate (0.0 to 1.0), or -1 if unknown
         */
        double getJoinSelectivity(String leftTable, String rightTable, String joinField);
        
        /**
         * Checks if an index exists for a specific field on a table.
         * @param tableName the table name, must not be null or empty
         * @param fieldName the field name, must not be null or empty
         * @return true if an index exists on the field
         */
        boolean hasIndexOnField(String tableName, String fieldName);
    }
}