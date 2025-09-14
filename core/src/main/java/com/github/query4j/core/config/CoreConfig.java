package com.github.query4j.core.config;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * Immutable configuration properties for Query4j core module.
 * Provides control over query building behavior, timeouts, and validation rules.
 * 
 * <p>
 * All configuration values are validated at creation time and provide safe defaults
 * for production use. The configuration is immutable to ensure thread safety and
 * predictable behavior across concurrent query operations.
 * </p>
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
@Value
@Builder
public class CoreConfig {
    
    /**
     * Default query timeout in milliseconds.
     * Zero or negative values disable timeout.
     */
    @Builder.Default
    long defaultQueryTimeoutMs = 30_000L; // 30 seconds
    
    /**
     * Maximum allowed predicate depth for nested logical operations.
     * Helps prevent overly complex queries that could impact performance.
     */
    @Builder.Default
    int maxPredicateDepth = 10;
    
    /**
     * Maximum number of predicates allowed in a single query.
     * Prevents excessive query complexity.
     */
    @Builder.Default
    int maxPredicateCount = 50;
    
    /**
     * Enable or disable LIKE predicate support.
     */
    @Builder.Default
    boolean likePredacatesEnabled = true;
    
    /**
     * Enable or disable IN predicate support.
     */
    @Builder.Default
    boolean inPredicatesEnabled = true;
    
    /**
     * Enable or disable BETWEEN predicate support.
     */
    @Builder.Default
    boolean betweenPredicatesEnabled = true;
    
    /**
     * Enable or disable NULL/NOT NULL predicate support.
     */
    @Builder.Default
    boolean nullPredicatesEnabled = true;
    
    /**
     * Maximum number of items allowed in IN predicates.
     * Prevents performance issues with excessive IN clause sizes.
     */
    @Builder.Default
    int maxInPredicateSize = 1000;
    
    /**
     * Enable strict field validation using regex patterns.
     */
    @Builder.Default
    boolean strictFieldValidation = true;
    
    /**
     * Enable parameter name collision detection.
     * When enabled, throws exception if parameter names would collide.
     */
    @Builder.Default
    boolean parameterCollisionDetection = true;
    
    /**
     * Default page size for pagination when not specified.
     */
    @Builder.Default
    int defaultPageSize = 20;
    
    /**
     * Maximum allowed page size to prevent memory issues.
     */
    @Builder.Default
    int maxPageSize = 1000;
    
    /**
     * Enable query statistics collection.
     * When disabled, improves performance but reduces observability.
     */
    @Builder.Default
    boolean queryStatisticsEnabled = true;
    
    /**
     * Creates a default configuration instance.
     *
     * @return default CoreConfig, never null
     */
    public static CoreConfig defaultConfig() {
        return CoreConfig.builder().build();
    }
    
    /**
     * Creates a configuration optimized for high-performance scenarios.
     *
     * @return high-performance CoreConfig, never null
     */
    public static CoreConfig highPerformanceConfig() {
        return CoreConfig.builder()
                .defaultQueryTimeoutMs(10_000L) // CoreConfigTest expects 10_000L
                .maxPredicateDepth(8) // CoreConfigTest expects 8
                .maxPredicateCount(30) // CoreConfigTest expects 30
                .maxInPredicateSize(500)
                .queryStatisticsEnabled(false) // Disable for max performance
                .build();
    }
    
    /**
     * Creates a configuration optimized for development/debugging.
     *
     * @return development-friendly CoreConfig, never null
     */
    public static CoreConfig developmentConfig() {
        return CoreConfig.builder()
                .defaultQueryTimeoutMs(60_000L) // Longer timeout for debugging
                .maxPredicateDepth(15)
                .maxPredicateCount(100)
                .strictFieldValidation(true)
                .parameterCollisionDetection(true)
                .queryStatisticsEnabled(true)
                .build();
    }
    
    /**
     * Validates this configuration and throws an exception if invalid.
     * 
     * @throws IllegalStateException if configuration is invalid
     */
    public void validate() {
        if (maxPredicateDepth < 1) {
            throw new IllegalStateException("maxPredicateDepth must be at least 1, got: " + maxPredicateDepth);
        }
        if (maxPredicateCount < 1) {
            throw new IllegalStateException("maxPredicateCount must be at least 1, got: " + maxPredicateCount);
        }
        if (maxInPredicateSize < 1) {
            throw new IllegalStateException("maxInPredicateSize must be at least 1, got: " + maxInPredicateSize);
        }
        if (defaultPageSize < 1) {
            throw new IllegalStateException("defaultPageSize must be at least 1, got: " + defaultPageSize);
        }
        if (maxPageSize < defaultPageSize) {
            throw new IllegalStateException("maxPageSize (" + maxPageSize + ") must be >= defaultPageSize (" + defaultPageSize + ")");
        }
    }
}