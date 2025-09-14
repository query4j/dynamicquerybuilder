package com.github.query4j.optimizer;

import com.github.query4j.core.QueryBuilder;
import com.github.query4j.optimizer.index.IndexAdvisor;
import com.github.query4j.optimizer.index.IndexAdvisorImpl;
import com.github.query4j.optimizer.join.JoinReorderOptimizer;
import com.github.query4j.optimizer.join.JoinReorderOptimizerImpl;
import com.github.query4j.optimizer.predicate.PredicatePushdownOptimizer;
import com.github.query4j.optimizer.predicate.PredicatePushdownOptimizerImpl;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Thread-safe implementation of QueryOptimizer that coordinates multiple
 * optimization strategies to provide comprehensive query analysis and suggestions.
 *
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class QueryOptimizerImpl implements QueryOptimizer {
    
    @NonNull
    private final OptimizerConfig config;
    
    private final IndexAdvisor indexAdvisor;
    private final PredicatePushdownOptimizer predicatePushdownOptimizer;
    private final JoinReorderOptimizer joinReorderOptimizer;
    
    /**
     * Creates a QueryOptimizer with default configuration.
     */
    public QueryOptimizerImpl() {
        this(OptimizerConfig.defaultConfig());
    }
    
    /**
     * Creates a QueryOptimizer with the specified configuration.
     *
     * @param config optimizer configuration, must not be null
     */
    public QueryOptimizerImpl(@NonNull OptimizerConfig config) {
        this.config = config;
        this.indexAdvisor = new IndexAdvisorImpl(config);
        this.predicatePushdownOptimizer = new PredicatePushdownOptimizerImpl(config);
        this.joinReorderOptimizer = new JoinReorderOptimizerImpl(config);
    }
    
    @Override
    public OptimizationResult optimize(QueryBuilder<?> queryBuilder) {
        if (queryBuilder == null) {
            throw new IllegalArgumentException("QueryBuilder must not be null");
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            var resultBuilder = OptimizationResult.builder();
            
            // Perform index analysis if enabled
            if (config.isIndexSuggestionsEnabled()) {
                var indexSuggestions = indexAdvisor.analyzeQuery(queryBuilder);
                resultBuilder.indexSuggestions(indexSuggestions);
            }
            
            // Perform predicate pushdown analysis if enabled
            if (config.isPredicatePushdownEnabled()) {
                var predicateSuggestions = predicatePushdownOptimizer.optimizeQuery(queryBuilder);
                resultBuilder.predicatePushdownSuggestions(predicateSuggestions);
            }
            
            // Perform join reordering analysis if enabled
            if (config.isJoinReorderingEnabled()) {
                var joinSuggestions = joinReorderOptimizer.optimizeJoinOrder(queryBuilder);
                resultBuilder.joinReorderSuggestions(joinSuggestions);
            }
            
            long analysisTime = System.currentTimeMillis() - startTime;
            
            // Check for timeout
            if (config.getMaxAnalysisTimeMs() > 0 && analysisTime > config.getMaxAnalysisTimeMs()) {
                throw new OptimizationException(
                    String.format("Optimization analysis exceeded timeout of %d ms (took %d ms)", 
                                config.getMaxAnalysisTimeMs(), analysisTime)
                );
            }
            
            var result = resultBuilder
                    .analysisTimeMs(analysisTime)
                    .summary(generateSummary(resultBuilder))
                    .build();
            
            return result;
            
        } catch (Exception e) {
            if (e instanceof OptimizationException) {
                throw e;
            }
            throw new OptimizationException("Failed to optimize query", e);
        }
    }
    
    @Override
    public OptimizationResult optimize(String sqlQuery) {
        if (sqlQuery == null || sqlQuery.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL query must not be null or empty");
        }
        
        // For now, SQL string analysis is limited - we focus on QueryBuilder analysis
        // This could be extended with a SQL parser in the future
        long startTime = System.currentTimeMillis();
        long analysisTime = System.currentTimeMillis() - startTime;
        
        return OptimizationResult.builder()
                .analysisTimeMs(analysisTime)
                .summary("SQL string analysis not yet fully implemented. " +
                        "Use QueryBuilder for comprehensive optimization analysis.")
                .build();
    }
    
    @Override
    public OptimizerConfig getConfig() {
        return config;
    }
    
    @Override
    public QueryOptimizer withConfig(OptimizerConfig newConfig) {
        if (newConfig == null) {
            throw new IllegalArgumentException("Config must not be null");
        }
        return new QueryOptimizerImpl(newConfig);
    }
    
    /**
     * Generates a human-readable summary of optimization results.
     * @hidden
     */
    @SuppressWarnings("javadoc")
    private String generateSummary(OptimizationResult.OptimizationResultBuilder resultBuilder) {
        // This is a simplified implementation - in reality we'd build from the actual suggestions
        var sb = new StringBuilder();
        sb.append("Query optimization analysis completed in ")
          .append(System.currentTimeMillis()).append(" ms. ");
        
        if (config.isIndexSuggestionsEnabled()) {
            sb.append("Index analysis: enabled. ");
        }
        if (config.isPredicatePushdownEnabled()) {
            sb.append("Predicate pushdown: enabled. ");
        }
        if (config.isJoinReorderingEnabled()) {
            sb.append("Join reordering: enabled. ");
        }
        
        return sb.toString();
    }
}