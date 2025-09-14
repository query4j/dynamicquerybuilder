package com.github.query4j.test.examples;

import com.github.query4j.cache.CacheManager;
import com.github.query4j.cache.config.ConfigurableCacheManager;
import com.github.query4j.core.config.*;
import com.github.query4j.optimizer.OptimizerConfig;
import com.github.query4j.optimizer.config.OptimizerConfigurationFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Comprehensive example demonstrating Query4j auto-configuration usage.
 * 
 * This example shows various ways to configure and use Query4j:
 * - Auto-configuration from external sources
 * - Programmatic configuration
 * - Configuration profiles
 * - Integration across modules
 */
public class ConfigurationExample {

    public static void main(String[] args) {
        System.out.println("=== Query4j Configuration Examples ===\n");
        
        demonstrateAutoConfiguration();
        demonstrateProgrammaticConfiguration();
        demonstrateConfigurationProfiles();
        demonstrateModuleIntegration();
        demonstrateCustomConfiguration();
        
        System.out.println("=== Configuration Examples Complete ===");
    }

    /**
     * Demonstrates auto-configuration from external sources.
     */
    private static void demonstrateAutoConfiguration() {
        System.out.println("1. Auto-Configuration Example");
        System.out.println("--------------------------------");
        
        // Get default configuration (loads from files, env vars, system props)
        Query4jConfig config = Query4jConfigurationFactory.getDefault();
        
        System.out.println("Auto-loaded configuration:");
        printCoreConfig(config.getCore());
        printCacheConfig(config.getCache());
        
        // Load optimizer configuration separately (avoids circular dependencies)
        OptimizerConfig optimizerConfig = OptimizerConfigurationFactory.getDefault();
        printOptimizerConfig(optimizerConfig);
        
        System.out.println();
    }

    /**
     * Demonstrates programmatic configuration creation.
     */
    private static void demonstrateProgrammaticConfiguration() {
        System.out.println("2. Programmatic Configuration Example");
        System.out.println("------------------------------------");
        
        // Build custom configuration programmatically
        Query4jConfig customConfig = Query4jConfigurationFactory.builder()
            .core(CoreConfig.builder()
                .defaultQueryTimeoutMs(15_000L)
                .maxPredicateDepth(20)
                .queryStatisticsEnabled(false)
                .strictFieldValidation(true)
                .build())
            .cache(CacheConfig.builder()
                .enabled(true)
                .maxSize(25_000L)
                .defaultTtlSeconds(2400L)
                .defaultRegion("custom")
                .build())
            .build();
        
        // Validate the configuration
        customConfig.validate();
        
        System.out.println("Custom programmatic configuration:");
        printCoreConfig(customConfig.getCore());
        printCacheConfig(customConfig.getCache());
        
        // Set as application default
        Query4jConfigurationFactory.setDefault(customConfig);
        System.out.println("✓ Set as application default\n");
    }

    /**
     * Demonstrates using predefined configuration profiles.
     */
    private static void demonstrateConfigurationProfiles() {
        System.out.println("3. Configuration Profiles Example");
        System.out.println("---------------------------------");
        
        // High-performance profile
        Query4jConfig highPerf = Query4jConfig.highPerformanceConfig();
        System.out.println("High-Performance Profile:");
        System.out.printf("  Query Timeout: %d ms%n", highPerf.getCore().getDefaultQueryTimeoutMs());
        System.out.printf("  Max Predicate Depth: %d%n", highPerf.getCore().getMaxPredicateDepth());
        System.out.printf("  Statistics Enabled: %s%n", highPerf.getCore().isQueryStatisticsEnabled());
        System.out.printf("  Cache Size: %d%n", highPerf.getCache().getMaxSize());
        
        // Development profile
        Query4jConfig dev = Query4jConfig.developmentConfig();
        System.out.println("\nDevelopment Profile:");
        System.out.printf("  Query Timeout: %d ms%n", dev.getCore().getDefaultQueryTimeoutMs());
        System.out.printf("  Max Predicate Depth: %d%n", dev.getCore().getMaxPredicateDepth());
        System.out.printf("  Statistics Enabled: %s%n", dev.getCore().isQueryStatisticsEnabled());
        System.out.printf("  Cache Size: %d%n", dev.getCache().getMaxSize());
        
        // Minimal profile
        Query4jConfig minimal = Query4jConfig.minimalConfig();
        System.out.println("\nMinimal Profile:");
        System.out.printf("  Cache Enabled: %s%n", minimal.getCache().isEnabled());
        System.out.println();
    }

    /**
     * Demonstrates integration between modules using configuration.
     */
    private static void demonstrateModuleIntegration() {
        System.out.println("4. Module Integration Example");
        System.out.println("-----------------------------");
        
        // Create cache-specific configuration
        CacheConfig cacheConfig = CacheConfig.builder()
            .enabled(true)
            .maxSize(5_000L)
            .defaultTtlSeconds(1800L)
            .defaultRegion("example")
            .statisticsEnabled(true)
            .build();
        
        // Create cache manager with auto-configuration
        CacheManager autoCache = ConfigurableCacheManager.create();
        System.out.printf("Auto-configured cache: region='%s', size=%d%n", 
            autoCache.getCacheRegion(), autoCache.getMaxSize());
        
        // Create cache manager with specific configuration
        CacheManager customCache = ConfigurableCacheManager.create(cacheConfig);
        System.out.printf("Custom cache: region='%s', size=%d%n", 
            customCache.getCacheRegion(), customCache.getMaxSize());
        
        // Demonstrate cache operations
        customCache.put("example-key", "example-value");
        String value = (String) customCache.get("example-key");
        System.out.printf("Cache test: stored and retrieved '%s'%n", value);
        
        // Show cache statistics
        var stats = customCache.stats();
        System.out.printf("Cache stats: hits=%d, misses=%d, size=%d%n",
            stats.getHitCount(), stats.getMissCount(), stats.getCurrentSize());
        
        System.out.println();
    }

    /**
     * Demonstrates loading configuration from properties map.
     */
    private static void demonstrateCustomConfiguration() {
        System.out.println("5. Custom Property Loading Example");
        System.out.println("----------------------------------");
        
        // Create properties map
        Map<String, String> properties = new HashMap<>();
        properties.put("query4j.core.maxPredicateDepth", "12");
        properties.put("query4j.core.queryStatisticsEnabled", "true");
        properties.put("query4j.cache.maxSize", "8000");
        properties.put("query4j.cache.defaultTtlSeconds", "3600");
        
        // Load configuration from properties
        Query4jConfig config = Query4jConfigurationFactory.fromProperties(properties);
        
        System.out.println("Configuration from properties map:");
        System.out.printf("  Max Predicate Depth: %d%n", config.getCore().getMaxPredicateDepth());
        System.out.printf("  Statistics Enabled: %s%n", config.getCore().isQueryStatisticsEnabled());
        System.out.printf("  Cache Size: %d%n", config.getCache().getMaxSize());
        System.out.printf("  Cache TTL: %d seconds%n", config.getCache().getDefaultTtlSeconds());
        
        System.out.println();
    }

    // Helper methods for printing configuration details
    
    private static void printCoreConfig(CoreConfig config) {
        System.out.println("Core Configuration:");
        System.out.printf("  Query Timeout: %d ms%n", config.getDefaultQueryTimeoutMs());
        System.out.printf("  Max Predicate Depth: %d%n", config.getMaxPredicateDepth());
        System.out.printf("  Max Predicate Count: %d%n", config.getMaxPredicateCount());
        System.out.printf("  Statistics Enabled: %s%n", config.isQueryStatisticsEnabled());
        System.out.printf("  Strict Validation: %s%n", config.isStrictFieldValidation());
    }
    
    private static void printCacheConfig(CacheConfig config) {
        System.out.println("Cache Configuration:");
        System.out.printf("  Enabled: %s%n", config.isEnabled());
        System.out.printf("  Max Size: %d%n", config.getMaxSize());
        System.out.printf("  Default TTL: %d seconds%n", config.getDefaultTtlSeconds());
        System.out.printf("  Default Region: %s%n", config.getDefaultRegion());
        System.out.printf("  Statistics Enabled: %s%n", config.isStatisticsEnabled());
    }
    
    private static void printOptimizerConfig(OptimizerConfig config) {
        System.out.println("Optimizer Configuration:");
        System.out.printf("  Index Suggestions: %s%n", config.isIndexSuggestionsEnabled());
        System.out.printf("  Predicate Pushdown: %s%n", config.isPredicatePushdownEnabled());
        System.out.printf("  Join Reordering: %s%n", config.isJoinReorderingEnabled());
        System.out.printf("  Verbose Output: %s%n", config.isVerboseOutput());
        System.out.printf("  Target Database: %s%n", config.getTargetDatabase());
    }
    
    /**
     * Example of environment-specific configuration.
     * This would typically be called based on deployment environment.
     */
    public static Query4jConfig getEnvironmentConfiguration(String environment) {
        switch (environment.toLowerCase()) {
            case "production":
                return Query4jConfig.builder()
                    .core(CoreConfig.builder()
                        .defaultQueryTimeoutMs(5_000L)  // Strict timeout
                        .queryStatisticsEnabled(true)   // Monitor production
                        .build())
                    .cache(CacheConfig.builder()
                        .maxSize(100_000L)              // Large production cache
                        .defaultTtlSeconds(3600L)       // 1-hour TTL
                        .statisticsEnabled(true)        // Monitor cache performance
                        .build())
                    .build();
                    
            case "development":
                return Query4jConfig.developmentConfig();
                
            case "testing":
                return Query4jConfig.builder()
                    .core(CoreConfig.defaultConfig())
                    .cache(CacheConfig.disabledConfig())  // No cache for tests
                    .build();
                    
            default:
                return Query4jConfig.defaultConfig();
        }
    }
}