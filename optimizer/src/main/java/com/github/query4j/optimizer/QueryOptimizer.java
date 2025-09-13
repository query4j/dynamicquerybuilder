package com.github.query4j.optimizer;

import com.github.query4j.core.QueryBuilder;

/**
 * Main interface for query optimization service that analyzes dynamic queries
 * and provides comprehensive optimization suggestions including index recommendations,
 * predicate pushdown, and join reordering.
 * 
 * Implementations must be thread-safe and immutable following the library's design principles.
 *
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface QueryOptimizer {
    
    /**
     * Creates a new QueryOptimizer instance with default configuration.
     *
     * @return a new QueryOptimizer instance, never null
     */
    static QueryOptimizer create() {
        return new QueryOptimizerImpl();
    }
    
    /**
     * Creates a new QueryOptimizer instance with custom configuration.
     *
     * @param config optimizer configuration, must not be null
     * @return a new QueryOptimizer instance, never null
     * @throws IllegalArgumentException if config is null
     */
    static QueryOptimizer create(OptimizerConfig config) {
        return new QueryOptimizerImpl(config);
    }
    
    /**
     * Analyzes a query builder and returns comprehensive optimization suggestions.
     *
     * @param queryBuilder the query builder to analyze, must not be null
     * @return optimization result with suggestions, never null
     * @throws IllegalArgumentException if queryBuilder is null
     * @throws OptimizationException if analysis fails
     */
    OptimizationResult optimize(QueryBuilder<?> queryBuilder);
    
    /**
     * Analyzes a SQL query string and returns optimization suggestions.
     * Note: SQL analysis capabilities may be limited compared to QueryBuilder analysis.
     *
     * @param sqlQuery the SQL query to analyze, must not be null or empty
     * @return optimization result with suggestions, never null
     * @throws IllegalArgumentException if sqlQuery is null or empty
     * @throws OptimizationException if analysis fails or SQL parsing fails
     */
    OptimizationResult optimize(String sqlQuery);
    
    /**
     * Returns the current optimizer configuration.
     *
     * @return optimizer configuration, never null
     */
    OptimizerConfig getConfig();
    
    /**
     * Creates a new QueryOptimizer with updated configuration.
     * This follows the immutable design pattern.
     *
     * @param config new configuration, must not be null
     * @return new QueryOptimizer instance with updated config, never null
     * @throws IllegalArgumentException if config is null
     */
    QueryOptimizer withConfig(OptimizerConfig config);
}