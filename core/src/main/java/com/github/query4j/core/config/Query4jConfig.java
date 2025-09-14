package com.github.query4j.core.config;

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
    @NonNull
    @Builder.Default
    CoreConfig core = CoreConfig.defaultConfig();
    
    /**
     * Cache module configuration settings.
     */
    @NonNull
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
    int yamlFilePriority = 50;
    
    @Builder.Default
    int propertiesFilePriority = 40;
    
    @Builder.Default
    int defaultValuesPriority = 10;
    
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
                .core(CoreConfig.highPerformanceConfig())
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
                .core(CoreConfig.defaultConfig())
                .cache(CacheConfig.disabledConfig())
                .build();
    }
    
    /**
     * Validates the configuration and throws an exception if invalid.
     * Performs validation on core and cache configurations.
     * 
     * @throws IllegalStateException if any configuration is invalid
     */
    public void validate() {
        try {
            core.validate();
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Core configuration validation failed: " + e.getMessage(), e);
        }
        
        try {
            cache.validate();
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Cache configuration validation failed: " + e.getMessage(), e);
        }
        
        // Validate priority consistency
        if (environmentVariablePriority < 0 || systemPropertyPriority < 0 || 
            yamlFilePriority < 0 || propertiesFilePriority < 0 || defaultValuesPriority < 0) {
            throw new IllegalStateException("All configuration priorities must be non-negative");
        }
    }
    
    /**
     * Returns a builder initialized with this configuration's values.
     * Useful for creating modified copies.
     *
     * @return a builder with current values
     */
    public Query4jConfigBuilder toBuilder() {
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