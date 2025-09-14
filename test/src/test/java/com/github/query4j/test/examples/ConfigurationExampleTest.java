package com.github.query4j.test.examples;

import com.github.query4j.cache.CacheManager;
import com.github.query4j.cache.config.ConfigurableCacheManager;
import com.github.query4j.core.config.*;
import com.github.query4j.optimizer.OptimizerConfig;
import com.github.query4j.optimizer.config.OptimizerConfigurationFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class that demonstrates the configuration system in action.
 * This serves as both a test and a working example.
 */
class ConfigurationExampleTest {

    @BeforeEach
    void setUp() {
        // Reset configuration before each test
        Query4jConfigurationFactory.resetDefault();
        OptimizerConfigurationFactory.resetDefault();
    }
    
    @AfterEach
    void tearDown() {
        // Clean up after each test
        Query4jConfigurationFactory.resetDefault();
        OptimizerConfigurationFactory.resetDefault();
    }

    @Test
    void demonstrateAutoConfiguration() {
        System.out.println("=== Auto-Configuration Demo ===");
        
        // Get default configuration (loads from files, env vars, system props)
        Query4jConfig config = Query4jConfigurationFactory.getDefault();
        
        System.out.println("Auto-loaded configuration:");
        printCoreConfig(config.getCore());
        printCacheConfig(config.getCache());
        
        // Verify default values are loaded
        assertNotNull(config);
        assertNotNull(config.getCore());
        assertNotNull(config.getCache());
        
        // Load optimizer configuration separately
        OptimizerConfig optimizerConfig = OptimizerConfigurationFactory.getDefault();
        printOptimizerConfig(optimizerConfig);
        assertNotNull(optimizerConfig);
        
        System.out.println("✓ Auto-configuration working correctly\n");
    }

    @Test
    void demonstrateProgrammaticConfiguration() {
        System.out.println("=== Programmatic Configuration Demo ===");
        
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
        assertDoesNotThrow(customConfig::validate);
        
        System.out.println("Custom programmatic configuration:");
        printCoreConfig(customConfig.getCore());
        printCacheConfig(customConfig.getCache());
        
        // Verify custom values
        assertEquals(15_000L, customConfig.getCore().getDefaultQueryTimeoutMs());
        assertEquals(20, customConfig.getCore().getMaxPredicateDepth());
        assertFalse(customConfig.getCore().isQueryStatisticsEnabled());
        assertEquals(25_000L, customConfig.getCache().getMaxSize());
        assertEquals("custom", customConfig.getCache().getDefaultRegion());
        
        System.out.println("✓ Programmatic configuration working correctly\n");
    }

    @Test
    void demonstrateConfigurationProfiles() {
        System.out.println("=== Configuration Profiles Demo ===");
        
        // Test different profiles
        Query4jConfig highPerf = Query4jConfig.highPerformanceConfig();
        Query4jConfig dev = Query4jConfig.developmentConfig();
        Query4jConfig minimal = Query4jConfig.minimalConfig();
        
        System.out.println("High-Performance Profile:");
        printConfigSummary(highPerf);
        assertEquals(15_000L, highPerf.getCore().getDefaultQueryTimeoutMs());
        assertFalse(highPerf.getCore().isQueryStatisticsEnabled());
        
        System.out.println("Development Profile:");
        printConfigSummary(dev);
        assertEquals(60_000L, dev.getCore().getDefaultQueryTimeoutMs());
        assertTrue(dev.getCore().isQueryStatisticsEnabled());
        
        System.out.println("Minimal Profile:");
        printConfigSummary(minimal);
        assertFalse(minimal.getCache().isEnabled());
        
        System.out.println("✓ Configuration profiles working correctly\n");
    }

    @Test
    void demonstrateModuleIntegration() {
        System.out.println("=== Module Integration Demo ===");
        
        // Create custom cache configuration
        CacheConfig cacheConfig = CacheConfig.builder()
            .enabled(true)
            .maxSize(5_000L)
            .defaultTtlSeconds(1800L)
            .defaultRegion("demo")
            .statisticsEnabled(true)
            .build();
        
        // Set up configuration
        Query4jConfig config = Query4jConfigurationFactory.builder()
            .cache(cacheConfig)
            .build();
        Query4jConfigurationFactory.setDefault(config);
        
        // Create cache managers
        CacheManager autoCache = ConfigurableCacheManager.create();
        CacheManager customCache = ConfigurableCacheManager.create(cacheConfig);
        
        System.out.printf("Auto-configured cache: region='%s', size=%d%n", 
            autoCache.getCacheRegion(), autoCache.getMaxSize());
        System.out.printf("Custom cache: region='%s', size=%d%n", 
            customCache.getCacheRegion(), customCache.getMaxSize());
        
        // Test cache operations
        customCache.put("demo-key", "demo-value");
        String value = (String) customCache.get("demo-key");
        assertEquals("demo-value", value);
        System.out.printf("Cache test: stored and retrieved '%s'%n", value);
        
        // Verify configuration is applied
        assertEquals("demo", customCache.getCacheRegion());
        assertEquals(5_000L, customCache.getMaxSize());
        assertEquals(1800L, customCache.getDefaultTtlSeconds());
        
        System.out.println("✓ Module integration working correctly\n");
    }

    @Test
    void demonstrateValidation() {
        System.out.println("=== Configuration Validation Demo ===");
        
        // Test valid configuration
        CoreConfig validConfig = CoreConfig.builder()
            .maxPredicateDepth(10)
            .defaultPageSize(20)
            .maxPageSize(100)
            .build();
        
        assertDoesNotThrow(validConfig::validate);
        System.out.println("✓ Valid configuration passed validation");
        
        // Test invalid configuration
        assertThrows(IllegalStateException.class, () -> {
            CoreConfig.builder()
                .maxPredicateDepth(-1)  // Invalid
                .build()
                .validate();
        });
        System.out.println("✓ Invalid configuration properly rejected");
        
        System.out.println("✓ Configuration validation working correctly\n");
    }

    // Helper methods for printing configuration details
    
    private void printCoreConfig(CoreConfig config) {
        System.out.printf("  Core - Timeout: %dms, MaxDepth: %d, Stats: %s%n",
            config.getDefaultQueryTimeoutMs(),
            config.getMaxPredicateDepth(),
            config.isQueryStatisticsEnabled());
    }
    
    private void printCacheConfig(CacheConfig config) {
        System.out.printf("  Cache - Enabled: %s, Size: %d, TTL: %ds, Region: %s%n",
            config.isEnabled(),
            config.getMaxSize(),
            config.getDefaultTtlSeconds(),
            config.getDefaultRegion());
    }
    
    private void printOptimizerConfig(OptimizerConfig config) {
        System.out.printf("  Optimizer - IndexSuggestions: %s, Verbose: %s, DB: %s%n",
            config.isIndexSuggestionsEnabled(),
            config.isVerboseOutput(),
            config.getTargetDatabase());
    }
    
    private void printConfigSummary(Query4jConfig config) {
        System.out.printf("  Timeout: %dms, Stats: %s, CacheSize: %d, CacheEnabled: %s%n",
            config.getCore().getDefaultQueryTimeoutMs(),
            config.getCore().isQueryStatisticsEnabled(),
            config.getCache().getMaxSize(),
            config.getCache().isEnabled());
    }
}