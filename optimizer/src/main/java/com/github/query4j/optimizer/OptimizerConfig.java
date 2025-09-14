package com.github.query4j.optimizer;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * Immutable configuration for QueryOptimizer behavior and analysis parameters.
 * Provides control over optimization strategies, thresholds, and feature enablement.
 *
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
@Value
@Builder
public class OptimizerConfig {
    
    /**
     * Enable/disable index suggestion analysis.
     */
    @Builder.Default
    boolean indexSuggestionsEnabled = true;
    
    /**
     * Enable/disable predicate pushdown optimization.
     */
    @Builder.Default
    boolean predicatePushdownEnabled = true;
    
    /**
     * Enable/disable join reordering optimization.
     */
    @Builder.Default
    boolean joinReorderingEnabled = true;
    
    /**
     * Minimum selectivity threshold for index suggestions (0.0 to 1.0).
     * Lower values will suggest indexes for more selective predicates.
     */
    @Builder.Default
    double indexSelectivityThreshold = 0.1;
    
    /**
     * Minimum improvement threshold for predicate reordering suggestions (0.0 to 1.0).
     * Higher values require more significant improvements to suggest reordering.
     */
    @Builder.Default
    double predicateReorderingThreshold = 0.05;
    
    /**
     * Minimum cardinality reduction threshold for join reordering (0.0 to 1.0).
     * Higher values require more significant improvements to suggest reordering.
     */
    @Builder.Default
    double joinReorderingThreshold = 0.1;
    
    /**
     * Maximum analysis time in milliseconds before timeout.
     * Zero or negative values disable timeout.
     */
    @Builder.Default
    long maxAnalysisTimeMs = 5000;
    
    /**
     * Include detailed explanations in optimization suggestions.
     */
    @Builder.Default
    boolean verboseOutput = false;
    
    /**
     * Consider composite indexes with up to this many columns.
     */
    @Builder.Default
    int maxCompositeIndexColumns = 3;
    
    /**
     * Target database type for SQL-specific optimizations.
     * Never null.
     */
    @NonNull
    @Builder.Default
    DatabaseType targetDatabase = DatabaseType.GENERIC;
    
    /**
     * Creates a default configuration instance.
     *
     * @return default OptimizerConfig, never null
     */
    public static OptimizerConfig defaultConfig() {
        return OptimizerConfig.builder().build();
    }
    
    /**
     * Creates a configuration optimized for high-volume queries.
     *
     * @return high-performance OptimizerConfig, never null
     */
    public static OptimizerConfig highPerformanceConfig() {
        return OptimizerConfig.builder()
                .indexSelectivityThreshold(0.05)
                .predicateReorderingThreshold(0.02)
                .joinReorderingThreshold(0.05)
                .maxCompositeIndexColumns(4)
                .verboseOutput(false)
                .build();
    }
    
    /**
     * Creates a configuration optimized for development/debugging.
     *
     * @return development-friendly OptimizerConfig, never null
     */
    public static OptimizerConfig developmentConfig() {
        return OptimizerConfig.builder()
                .verboseOutput(true)
                .maxAnalysisTimeMs(10000)
                .build();
    }
    
    /**
     * Supported database types for targeted optimizations.
     */
    public enum DatabaseType {
        GENERIC("Generic SQL"),
        POSTGRESQL("PostgreSQL"),
        MYSQL("MySQL"),
        H2("H2 Database"),
        ORACLE("Oracle"),
        SQL_SERVER("Microsoft SQL Server");
        
        private final String displayName;
        
        DatabaseType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}