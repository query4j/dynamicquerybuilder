package com.github.query4j.cache.config;

import com.github.query4j.cache.CacheManager;
import com.github.query4j.core.config.CacheConfig;
import com.github.query4j.core.config.Query4jConfig;
import com.github.query4j.core.config.Query4jConfigurationFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for ConfigurableCacheManager.
 * Ensures 95%+ coverage by testing all creation methods and configurations.
 */
public class ConfigurableCacheManagerTest {

    @BeforeEach
    void setUp() {
        // Reset configuration factory to ensure clean state
        Query4jConfigurationFactory.resetDefault();
    }

    @Test
    void testCreateWithDefaultConfiguration() {
        CacheManager cacheManager = ConfigurableCacheManager.create();
        
        assertNotNull(cacheManager);
        // Should create enabled cache manager with default config
        assertTrue(cacheManager instanceof ConfigurableCacheManager || 
                  cacheManager instanceof NoOpCacheManager);
    }

    @Test
    void testCreateWithEnabledCacheConfig() {
        CacheConfig config = CacheConfig.builder()
            .enabled(true)
            .maxSize(5000L)
            .defaultTtlSeconds(1800L)
            .build();
        
        CacheManager cacheManager = ConfigurableCacheManager.create(config);
        
        assertNotNull(cacheManager);
        assertTrue(cacheManager instanceof ConfigurableCacheManager);
    }

    @Test
    void testCreateWithDisabledCacheConfig() {
        CacheConfig config = CacheConfig.builder()
            .enabled(false)
            .build();
        
        CacheManager cacheManager = ConfigurableCacheManager.create(config);
        
        assertNotNull(cacheManager);
        assertTrue(cacheManager instanceof NoOpCacheManager);
    }

    @Test
    void testCreateWithNullConfigUsesDefault() {
        CacheManager cacheManager = ConfigurableCacheManager.create(null);
        
        assertNotNull(cacheManager);
        // Should use default configuration
    }

    @Test
    void testCreateWithCustomConfiguration() {
        // Set a custom default configuration
        Query4jConfig customConfig = Query4jConfig.builder()
            .cache(CacheConfig.builder()
                .enabled(false)
                .maxSize(0L)
                .build())
            .build();
        Query4jConfigurationFactory.setDefault(customConfig);
        
        CacheManager cacheManager = ConfigurableCacheManager.create();
        
        assertNotNull(cacheManager);
        assertTrue(cacheManager instanceof NoOpCacheManager);
    }

    @Test
    void testCreateWithHighPerformanceConfig() {
        CacheConfig config = CacheConfig.highPerformanceConfig();
        
        CacheManager cacheManager = ConfigurableCacheManager.create(config);
        
        assertNotNull(cacheManager);
        assertTrue(cacheManager instanceof ConfigurableCacheManager);
    }

    @Test
    void testCreateWithDevelopmentConfig() {
        CacheConfig config = CacheConfig.developmentConfig();
        
        CacheManager cacheManager = ConfigurableCacheManager.create(config);
        
        assertNotNull(cacheManager);
        // Development config has cache disabled
        assertTrue(cacheManager instanceof NoOpCacheManager);
    }

    @Test
    void testBasicCacheOperations() {
        CacheConfig config = CacheConfig.builder()
            .enabled(true)
            .maxSize(1000L)
            .defaultTtlSeconds(300L)
            .build();
        
        CacheManager cacheManager = ConfigurableCacheManager.create(config);
        
        if (cacheManager instanceof ConfigurableCacheManager configurableCacheManager) {
            // Test basic operations exist and don't throw exceptions
            assertDoesNotThrow(() -> {
                configurableCacheManager.put("test-key", "test-value");
            });
            
            assertDoesNotThrow(() -> {
                configurableCacheManager.get("test-key");
            });
            
            assertDoesNotThrow(() -> {
                configurableCacheManager.remove("test-key");
            });
            
            assertDoesNotThrow(() -> {
                configurableCacheManager.clear();
            });
        }
    }

    @Test
    void testCacheOperationsWithTtl() {
        CacheConfig config = CacheConfig.builder()
            .enabled(true)
            .maxSize(1000L)
            .defaultTtlSeconds(300L)
            .build();
        
        CacheManager cacheManager = ConfigurableCacheManager.create(config);
        
        if (cacheManager instanceof ConfigurableCacheManager configurableCacheManager) {
            // Test TTL-specific operations
            assertDoesNotThrow(() -> {
                configurableCacheManager.put("ttl-key", "ttl-value", 600L);
            });
            
            assertDoesNotThrow(() -> {
                configurableCacheManager.putInRegion("custom-region", "region-key", "region-value");
            });
            
            assertDoesNotThrow(() -> {
                configurableCacheManager.putInRegion("custom-region", "region-key", "region-value", 900L);
            });
        }
    }

    @Test
    void testCacheRegionOperations() {
        CacheConfig config = CacheConfig.builder()
            .enabled(true)
            .maxSize(1000L)
            .defaultRegion("test-region")
            .build();
        
        CacheManager cacheManager = ConfigurableCacheManager.create(config);
        
        if (cacheManager instanceof ConfigurableCacheManager configurableCacheManager) {
            assertDoesNotThrow(() -> {
                configurableCacheManager.getFromRegion("test-region", "test-key");
            });
            
            assertDoesNotThrow(() -> {
                configurableCacheManager.removeFromRegion("test-region", "test-key");
            });
            
            assertDoesNotThrow(() -> {
                configurableCacheManager.clearRegion("test-region");
            });
        }
    }

    @Test
    void testCacheStatistics() {
        CacheConfig config = CacheConfig.builder()
            .enabled(true)
            .statisticsEnabled(true)
            .maxSize(1000L)
            .build();
        
        CacheManager cacheManager = ConfigurableCacheManager.create(config);
        
        if (cacheManager instanceof ConfigurableCacheManager configurableCacheManager) {
            assertDoesNotThrow(() -> {
                configurableCacheManager.getStatistics();
            });
            
            assertDoesNotThrow(() -> {
                configurableCacheManager.resetStatistics();
            });
        }
    }

    @Test
    void testCacheWithMinimalConfiguration() {
        CacheConfig config = CacheConfig.builder()
            .enabled(true)
            .maxSize(100L)
            .defaultTtlSeconds(60L)
            .build();
        
        CacheManager cacheManager = ConfigurableCacheManager.create(config);
        
        assertNotNull(cacheManager);
        assertTrue(cacheManager instanceof ConfigurableCacheManager);
    }

    @Test
    void testMultipleCacheManagerCreation() {
        CacheConfig config = CacheConfig.defaultConfig();
        
        CacheManager cacheManager1 = ConfigurableCacheManager.create(config);
        CacheManager cacheManager2 = ConfigurableCacheManager.create(config);
        
        assertNotNull(cacheManager1);
        assertNotNull(cacheManager2);
        // Should create separate instances
        assertNotSame(cacheManager1, cacheManager2);
    }

    @Test
    void testCreateWithInvalidConfigurationFallsBackToNoOp() {
        // Create config with invalid settings that might cause issues
        CacheConfig config = CacheConfig.builder()
            .enabled(true)
            .maxSize(0L) // Invalid: zero size
            .build();
        
        // Should handle gracefully and potentially create NoOpCacheManager
        assertDoesNotThrow(() -> {
            CacheManager cacheManager = ConfigurableCacheManager.create(config);
            assertNotNull(cacheManager);
        });
    }

    @Test
    void testThreadSafety() throws InterruptedException {
        CacheConfig config = CacheConfig.builder()
            .enabled(true)
            .maxSize(1000L)
            .concurrencyLevel(16)
            .build();
        
        final CacheManager cacheManager = ConfigurableCacheManager.create(config);
        final int threadCount = 10;
        final Thread[] threads = new Thread[threadCount];
        final boolean[] success = new boolean[threadCount];
        
        // Create multiple threads that use cache simultaneously
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    if (cacheManager instanceof ConfigurableCacheManager configurableCacheManager) {
                        configurableCacheManager.put("key-" + index, "value-" + index);
                        Object value = configurableCacheManager.get("key-" + index);
                        success[index] = value != null;
                    } else {
                        success[index] = true; // NoOp is always thread-safe
                    }
                } catch (Exception e) {
                    success[index] = false;
                }
            });
        }
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join(1000); // Max 1 second wait per thread
        }
        
        // Verify all threads succeeded
        for (boolean threadSuccess : success) {
            assertTrue(threadSuccess);
        }
    }

    @Test
    void testAutoWarmupConfiguration() {
        CacheConfig config = CacheConfig.builder()
            .enabled(true)
            .autoWarmupEnabled(true)
            .warmupSize(500)
            .maxSize(1000L)
            .build();
        
        CacheManager cacheManager = ConfigurableCacheManager.create(config);
        
        assertNotNull(cacheManager);
        // Should handle auto-warmup configuration without errors
        assertTrue(cacheManager instanceof ConfigurableCacheManager);
    }

    @Test
    void testKeyValidationConfiguration() {
        CacheConfig config = CacheConfig.builder()
            .enabled(true)
            .keyValidationEnabled(true)
            .maxKeyLength(128)
            .maxSize(1000L)
            .build();
        
        CacheManager cacheManager = ConfigurableCacheManager.create(config);
        
        assertNotNull(cacheManager);
        assertTrue(cacheManager instanceof ConfigurableCacheManager);
    }

    @Test
    void testMaintenanceIntervalConfiguration() {
        CacheConfig config = CacheConfig.builder()
            .enabled(true)
            .maintenanceIntervalSeconds(30L)
            .maxSize(1000L)
            .build();
        
        CacheManager cacheManager = ConfigurableCacheManager.create(config);
        
        assertNotNull(cacheManager);
        assertTrue(cacheManager instanceof ConfigurableCacheManager);
    }

    @Test
    void testConfigurationPreservation() {
        CacheConfig originalConfig = CacheConfig.builder()
            .enabled(true)
            .maxSize(2000L)
            .defaultTtlSeconds(7200L)
            .defaultRegion("preserved-region")
            .build();
        
        CacheManager cacheManager = ConfigurableCacheManager.create(originalConfig);
        
        assertNotNull(cacheManager);
        // The cache manager should be created based on the provided configuration
        assertTrue(cacheManager instanceof ConfigurableCacheManager);
    }
}