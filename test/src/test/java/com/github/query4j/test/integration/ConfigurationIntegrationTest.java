package com.github.query4j.test.integration;

import com.github.query4j.cache.CacheManager;
import com.github.query4j.cache.config.ConfigurableCacheManager;
import com.github.query4j.core.config.*;
import com.github.query4j.optimizer.OptimizerConfig;
import com.github.query4j.optimizer.config.OptimizerConfigurationFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the Query4j configuration system.
 * Tests end-to-end configuration loading and usage across modules.
 */
class ConfigurationIntegrationTest {

    @TempDir
    Path tempDir;
    
    private Map<String, String> originalSystemProperties;
    
    @BeforeEach
    void setUp() {
        // Save original system properties
        originalSystemProperties = new HashMap<>();
        System.getProperties().forEach((key, value) -> {
            String keyStr = key.toString();
            if (keyStr.startsWith("query4j.")) {
                originalSystemProperties.put(keyStr, value.toString());
            }
        });
        
        // Reset configuration factories
        Query4jConfigurationFactory.resetDefault();
        OptimizerConfigurationFactory.resetDefault();
    }
    
    @AfterEach
    void tearDown() {
        // Restore original system properties
        System.getProperties().forEach((key, value) -> {
            String keyStr = key.toString();
            if (keyStr.startsWith("query4j.")) {
                System.clearProperty(keyStr);
            }
        });
        originalSystemProperties.forEach(System::setProperty);
        
        // Reset configuration factories
        Query4jConfigurationFactory.resetDefault();
        OptimizerConfigurationFactory.resetDefault();
    }
    
    @Test
    void fullConfigurationChain_shouldWorkEndToEnd() throws IOException {
        // Create comprehensive configuration file
        Path configFile = tempDir.resolve("integration.properties");
        String configContent = 
            "# Core configuration\n" +
            "query4j.core.defaultQueryTimeoutMs=45000\n" +
            "query4j.core.maxPredicateDepth=15\n" +
            "query4j.core.queryStatisticsEnabled=false\n" +
            "\n" +
            "# Cache configuration\n" +
            "query4j.cache.enabled=true\n" +
            "query4j.cache.maxSize=5000\n" +
            "query4j.cache.defaultTtlSeconds=1800\n" +
            "query4j.cache.defaultRegion=integration\n" +
            "\n" +
            "# Optimizer configuration\n" +
            "query4j.optimizer.verboseOutput=true\n" +
            "query4j.optimizer.maxAnalysisTimeMs=8000\n" +
            "query4j.optimizer.targetDatabase=POSTGRESQL\n";
        Files.write(configFile, configContent.getBytes());
        
        // Load configuration from file
        Query4jConfig coreConfig = Query4jConfigurationFactory.loadFromFile(configFile.toString());
        OptimizerConfig optimizerConfig = OptimizerConfigurationFactory.loadFromFile(configFile.toString());
        
        // Verify core configuration loaded correctly
        assertEquals(45_000L, coreConfig.getCore().getDefaultQueryTimeoutMs());
        assertEquals(15, coreConfig.getCore().getMaxPredicateDepth());
        assertFalse(coreConfig.getCore().isQueryStatisticsEnabled());
        
        // Verify cache configuration loaded correctly
        assertTrue(coreConfig.getCache().isEnabled());
        assertEquals(5000L, coreConfig.getCache().getMaxSize());
        assertEquals(1800L, coreConfig.getCache().getDefaultTtlSeconds());
        assertEquals("integration", coreConfig.getCache().getDefaultRegion());
        
        // Verify optimizer configuration loaded correctly
        assertTrue(optimizerConfig.isVerboseOutput());
        assertEquals(8000L, optimizerConfig.getMaxAnalysisTimeMs());
        assertEquals(OptimizerConfig.DatabaseType.POSTGRESQL, optimizerConfig.getTargetDatabase());
    }
    
    @Test
    void configurableCacheManager_shouldRespectConfiguration() throws IOException {
        // Create cache-specific configuration
        Path configFile = tempDir.resolve("cache.yml");
        String yamlContent = 
            "query4j:\n" +
            "  cache:\n" +
            "    enabled: true\n" +
            "    maxSize: 2500\n" +
            "    defaultTtlSeconds: 900\n" +
            "    defaultRegion: \"testcache\"\n";
        Files.write(configFile, yamlContent.getBytes());
        
        // Set the configuration as default
        Query4jConfig config = Query4jConfigurationFactory.loadFromFile(configFile.toString());
        Query4jConfigurationFactory.setDefault(config);
        
        // Create cache manager using auto-configuration
        CacheManager cacheManager = ConfigurableCacheManager.create();
        
        // Verify cache manager reflects configuration
        assertEquals("testcache", cacheManager.getCacheRegion());
        assertEquals(2500L, cacheManager.getMaxSize());
        assertEquals(900L, cacheManager.getDefaultTtlSeconds());
        
        // Test cache operations
        assertNull(cacheManager.get("test-key"));
        cacheManager.put("test-key", "test-value");
        assertEquals("test-value", cacheManager.get("test-key"));
        
        assertTrue(cacheManager.containsKey("test-key"));
        cacheManager.invalidate("test-key");
        assertFalse(cacheManager.containsKey("test-key"));
    }
    
    @Test
    void systemPropertyOverrides_shouldTakePrecedence() throws IOException {
        // Create base configuration file
        Path configFile = tempDir.resolve("base.properties");
        String configContent = 
            "query4j.core.maxPredicateDepth=10\n" +
            "query4j.cache.maxSize=1000\n";
        Files.write(configFile, configContent.getBytes());
        
        // Set system properties to override
        System.setProperty("query4j.core.maxPredicateDepth", "25");
        System.setProperty("query4j.cache.maxSize", "8000");
        
        // Load configuration (auto-load will pick up system properties)
        Query4jConfig config = Query4jConfigurationFactory.getDefault();
        
        // Verify system properties override file values
        assertEquals(25, config.getCore().getMaxPredicateDepth());
        assertEquals(8000L, config.getCache().getMaxSize());
    }
    
    @Test
    void disabledCacheConfiguration_shouldCreateNoOpCacheManager() throws IOException {
        // Create configuration with caching disabled
        Path configFile = tempDir.resolve("nocache.properties");
        String configContent = "query4j.cache.enabled=false\n";
        Files.write(configFile, configContent.getBytes());
        
        Query4jConfig config = Query4jConfigurationFactory.loadFromFile(configFile.toString());
        Query4jConfigurationFactory.setDefault(config);
        
        // Create cache manager
        CacheManager cacheManager = ConfigurableCacheManager.create();
        
        // Verify it's a no-op implementation
        assertEquals(0L, cacheManager.getMaxSize());
        assertEquals(-1L, cacheManager.getDefaultTtlSeconds());
        
        // Verify cache operations are no-ops
        assertNull(cacheManager.get("any-key"));
        cacheManager.put("test-key", "test-value");
        assertNull(cacheManager.get("test-key")); // Should still be null
        assertFalse(cacheManager.containsKey("test-key"));
    }
    
    @Test
    void predefinedProfiles_shouldLoadCorrectly() {
        // Test high-performance profile
        Query4jConfig highPerf = Query4jConfig.highPerformanceConfig();
        assertEquals(15_000L, highPerf.getCore().getDefaultQueryTimeoutMs());
        assertEquals(20, highPerf.getCore().getMaxPredicateDepth());
        assertFalse(highPerf.getCore().isQueryStatisticsEnabled());
        assertEquals(50_000L, highPerf.getCache().getMaxSize());
        
        // Test development profile
        Query4jConfig dev = Query4jConfig.developmentConfig();
        assertEquals(60_000L, dev.getCore().getDefaultQueryTimeoutMs());
        assertEquals(15, dev.getCore().getMaxPredicateDepth());
        assertTrue(dev.getCore().isQueryStatisticsEnabled());
        assertEquals(1_000L, dev.getCache().getMaxSize());
        
        // Test minimal profile
        Query4jConfig minimal = Query4jConfig.minimalConfig();
        assertFalse(minimal.getCache().isEnabled());
    }
    
    @Test
    void invalidConfiguration_shouldThrowException() throws IOException {
        // Create invalid configuration
        Path configFile = tempDir.resolve("invalid.properties");
        String configContent = 
            "query4j.core.maxPredicateDepth=-1\n" +  // Invalid: must be positive
            "query4j.cache.maxSize=0\n";            // Invalid: must be positive
        Files.write(configFile, configContent.getBytes());
        
        // Attempt to load invalid configuration
        assertThrows(Exception.class, () -> {
            Query4jConfigurationFactory.loadFromFile(configFile.toString());
        });
    }
    
    @Test
    void programmaticConfiguration_shouldOverrideDefaults() {
        // Create custom configuration programmatically
        CoreConfig customCore = CoreConfig.builder()
            .maxPredicateDepth(20)
            .queryStatisticsEnabled(false)
            .build();
        
        CacheConfig customCache = CacheConfig.builder()
            .maxSize(15_000L)
            .defaultTtlSeconds(3600L)
            .build();
        
        Query4jConfig customConfig = Query4jConfig.builder()
            .core(customCore)
            .cache(customCache)
            .build();
        
        // Set as default
        Query4jConfigurationFactory.setDefault(customConfig);
        
        // Verify the custom configuration is used
        Query4jConfig retrieved = Query4jConfigurationFactory.getDefault();
        assertEquals(20, retrieved.getCore().getMaxPredicateDepth());
        assertFalse(retrieved.getCore().isQueryStatisticsEnabled());
        assertEquals(15_000L, retrieved.getCache().getMaxSize());
        assertEquals(3600L, retrieved.getCache().getDefaultTtlSeconds());
    }
    
    @Test
    void configurationValidation_shouldCatchInvalidSettings() {
        // Test invalid core configuration
        assertThrows(IllegalStateException.class, () -> {
            CoreConfig.builder()
                .maxPredicateDepth(0)  // Invalid
                .build()
                .validate();
        });
        
        // Test invalid cache configuration
        assertThrows(IllegalStateException.class, () -> {
            CacheConfig.builder()
                .maxSize(-1)  // Invalid
                .build()
                .validate();
        });
        
        // Test invalid page size relationship
        assertThrows(IllegalStateException.class, () -> {
            CoreConfig.builder()
                .defaultPageSize(100)
                .maxPageSize(50)  // Invalid: max < default
                .build()
                .validate();
        });
    }
}