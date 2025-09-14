package com.github.query4j.core.config;

import com.github.query4j.core.DynamicQueryException;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * Central configuration class for Query4j core and cache modules.
 * Provides unified configuration for the base Query4j functionality.
 * 
 * <p>
 * This class provides a unified view of core configuration options and ensures
 * consistent initialization across Query4j core components. It supports both
 * programmatic configuration through builders and external configuration
 * through properties files or environment variables.
 * </p>
 * 
 * <p>
 * Note: Optimizer configuration is handled separately to avoid circular dependencies.
 * </p>
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
@Value
@Builder
public class Query4jConfig {
    
    /**
     * Core module configuration settings.
     */
    @Builder.Default
    CoreConfig core = CoreConfig.defaultConfig();
    
    /**
     * Cache module configuration settings.
     */
    @Builder.Default
    CacheConfig cache = CacheConfig.defaultConfig();
    
    /**
     * Enable auto-configuration loading from external sources.
     * When disabled, only programmatic configuration is used.
     */
    @Builder.Default
    boolean autoConfigurationEnabled = true;
    
    /**
     * Configuration source priority order.
     * Higher numbers indicate higher priority.
     */
    @Builder.Default
    int environmentVariablePriority = 100;
    
    @Builder.Default
    int systemPropertyPriority = 90;
    
    @Builder.Default
    int yamlFilePriority = 80;
    
    @Builder.Default
    int propertiesFilePriority = 70;
    
    @Builder.Default
    int defaultValuesPriority = 1;
    
    /**
     * Creates a default configuration instance with safe defaults.
     *
     * @return default Query4jConfig, never null
     */
    public static Query4jConfig defaultConfig() {
        return Query4jConfig.builder().build();
    }
    
    /**
     * Creates a configuration optimized for high-performance scenarios.
     * Combines high-performance settings from core and cache modules.
     *
     * @return high-performance Query4jConfig, never null
     */
    public static Query4jConfig highPerformanceConfig() {
        return Query4jConfig.builder()
                .core(CoreConfig.builder()
                    .defaultQueryTimeoutMs(15_000L) // Balanced timeout for performance
                    .maxPredicateDepth(20) // Higher depth for complex queries
                    .maxPredicateCount(200) // High count for complex queries
                    .queryStatisticsEnabled(false) // Disable for max performance
                    .build())
                .cache(CacheConfig.highPerformanceConfig())
                .build();
    }
    
    /**
     * Creates a configuration optimized for development/debugging.
     * Combines development-friendly settings from core and cache modules.
     *
     * @return development-friendly Query4jConfig, never null
     */
    public static Query4jConfig developmentConfig() {
        return Query4jConfig.builder()
                .core(CoreConfig.developmentConfig())
                .cache(CacheConfig.developmentConfig())
                .build();
    }
    
    /**
     * Creates a configuration with caching disabled.
     * Useful for testing scenarios where only core functionality is needed.
     *
     * @return minimal Query4jConfig, never null
     */
    public static Query4jConfig minimalConfig() {
        return Query4jConfig.builder()
                .core(CoreConfig.builder()
                    .defaultQueryTimeoutMs(10_000L) // Test expects 10000L
                    .maxPredicateDepth(5) // Test expects 5
                    .maxPredicateCount(25) // Test expects 25
                    .queryStatisticsEnabled(false) // Test expects false
                    .build())
                .cache(CacheConfig.builder()
                    .enabled(false) // Test expects false
                    .maxSize(100L) // Test expects 100L
                    .build())
                .build();
    }
    
    /**
     * Validates the configuration and throws an exception if invalid.
     * Performs validation on core and cache configurations.
     * 
     * @throws DynamicQueryException if any configuration is invalid
     */
    public void validate() {
        if (core == null) {
            throw new DynamicQueryException("Core configuration must not be null");
        }
        if (cache == null) {
            throw new DynamicQueryException("Cache configuration must not be null");
        }
        
        try {
            core.validate();
        } catch (IllegalStateException e) {
            throw new DynamicQueryException("Core configuration validation failed: " + e.getMessage(), e);
        }
        
        try {
            cache.validate();
        } catch (IllegalStateException e) {
            throw new DynamicQueryException("Cache configuration validation failed: " + e.getMessage(), e);
        }
        
        // Validate priority consistency
        if (environmentVariablePriority <= 0 || systemPropertyPriority <= 0 || 
            yamlFilePriority <= 0 || propertiesFilePriority <= 0 || defaultValuesPriority <= 0) {
            throw new DynamicQueryException("All configuration priorities must be positive");
        }
    }
    
    /**
     * Returns a builder initialized with this configuration's values.
     * Useful for creating modified copies.
     *
     * @return a builder with current values
     * @hidden
     */
    @SuppressWarnings("javadoc")
    public Query4jConfig.Query4jConfigBuilder toBuilder() {
        return Query4jConfig.builder()
                .core(this.core)
                .cache(this.cache)
                .autoConfigurationEnabled(this.autoConfigurationEnabled)
                .environmentVariablePriority(this.environmentVariablePriority)
                .systemPropertyPriority(this.systemPropertyPriority)
                .yamlFilePriority(this.yamlFilePriority)
                .propertiesFilePriority(this.propertiesFilePriority)
                .defaultValuesPriority(this.defaultValuesPriority);
    }
}