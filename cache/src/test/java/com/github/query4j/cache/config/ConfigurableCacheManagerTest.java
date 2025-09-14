package com.github.query4j.cache.config;

import com.github.query4j.cache.CacheManager;
import com.github.query4j.cache.CacheStatistics;
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
        assertFalse(cacheManager instanceof NoOpCacheManager);
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
        // Should create a real cache manager (not NoOpCacheManager) when enabled
        assertFalse(cacheManager instanceof NoOpCacheManager);
        assertEquals(5000L, cacheManager.getMaxSize());
        assertEquals(1800L, cacheManager.getDefaultTtlSeconds());
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
    void testCreateWithNullConfigThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            ConfigurableCacheManager.create(null);
        });
    }

    @Test
    void testCreateWithCustomConfiguration() {
        // Set a custom default configuration with caching disabled
        Query4jConfig customConfig = Query4jConfig.builder()
            .cache(CacheConfig.disabledConfig())
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
        // High performance config should have caching enabled
        assertFalse(cacheManager instanceof NoOpCacheManager);
    }

    @Test
    void testCreateWithDevelopmentConfig() {
        CacheConfig config = CacheConfig.builder()
                .enabled(true)
                .maxSize(1_000L)
                .defaultTtlSeconds(300L)
                .defaultRegion("dev-test-region")
                .statisticsEnabled(true)
                .keyValidationEnabled(true)
                .autoWarmupEnabled(false)
                .build();
        
        CacheManager cacheManager = ConfigurableCacheManager.create(config);
        
        assertNotNull(cacheManager);
        // Development config has cache enabled with smaller size
        assertFalse(cacheManager instanceof NoOpCacheManager);
        assertEquals(1_000L, cacheManager.getMaxSize());
        assertEquals(300L, cacheManager.getDefaultTtlSeconds());
        assertEquals("dev-test-region", cacheManager.getCacheRegion());
    }

    @Test
    void testBasicCacheOperations() {
        CacheConfig config = CacheConfig.builder()
            .enabled(true)
            .maxSize(1000L)
            .defaultTtlSeconds(300L)
            .build();
        
        CacheManager cacheManager = ConfigurableCacheManager.create(config);
        
        // Test basic CacheManager interface operations
        assertDoesNotThrow(() -> {
            cacheManager.put("test-key", "test-value");
        });
        
        assertDoesNotThrow(() -> {
            cacheManager.get("test-key");
        });
        
        assertDoesNotThrow(() -> {
            cacheManager.invalidate("test-key");
        });
        
        assertDoesNotThrow(() -> {
            cacheManager.clear();
        });
        
        // Test containsKey and stats
        assertDoesNotThrow(() -> {
            cacheManager.containsKey("test-key");
        });
        
        assertNotNull(cacheManager.stats());
    }

    @Test
    void testCacheOperationsWithTtl() {
        CacheConfig config = CacheConfig.builder()
            .enabled(true)
            .maxSize(1000L)
            .defaultTtlSeconds(300L)
            .build();
        
        CacheManager cacheManager = ConfigurableCacheManager.create(config);
        
        // Test TTL-specific operations using CacheManager interface
        assertDoesNotThrow(() -> {
            cacheManager.put("ttl-key", "ttl-value", 600L);
        });
        
        // Test that we can retrieve the value
        assertDoesNotThrow(() -> {
            Object value = cacheManager.get("ttl-key");
            // Value might be null if TTL expired, but operation should not throw
        });
    }

    @Test
    void testCacheRegionOperations() {
        CacheConfig config = CacheConfig.builder()
            .enabled(true)
            .maxSize(1000L)
            .defaultRegion("test-region")
            .build();
        
        CacheManager cacheManager = ConfigurableCacheManager.create(config);
        
        // Test region-related properties
        assertNotNull(cacheManager.getCacheRegion());
        assertEquals("test-region", cacheManager.getCacheRegion());
        
        // Test basic cache operations work with custom region
        assertDoesNotThrow(() -> {
            cacheManager.put("test-key", "test-value");
        });
        
        assertDoesNotThrow(() -> {
            cacheManager.get("test-key");
        });
    }

    @Test
    void testCacheStatistics() {
        CacheConfig config = CacheConfig.builder()
            .enabled(true)
            .statisticsEnabled(true)
            .maxSize(1000L)
            .build();
        
        CacheManager cacheManager = ConfigurableCacheManager.create(config);
        
        // Test statistics using CacheManager interface
        assertDoesNotThrow(() -> {
            CacheStatistics stats = cacheManager.stats();
            assertNotNull(stats);
        });
    }

    @Test
    void testCacheWithMinimalConfiguration() {
        CacheConfig config = CacheConfig.builder()
            .enabled(true)
            .maxSize(100L)
            .defaultTtlSeconds(60L)
            .defaultRegion("minimal-test-region")
            .build();
        
        CacheManager cacheManager = ConfigurableCacheManager.create(config);
        
        assertNotNull(cacheManager);
        assertFalse(cacheManager instanceof NoOpCacheManager);
        assertEquals(100L, cacheManager.getMaxSize());
        assertEquals(60L, cacheManager.getDefaultTtlSeconds());
        assertEquals("minimal-test-region", cacheManager.getCacheRegion());
    }

    @Test
    void testMultipleCacheManagerCreation() {
        CacheConfig config = CacheConfig.defaultConfig();
        
        CacheManager cacheManager1 = ConfigurableCacheManager.create(config);
        CacheManager cacheManager2 = ConfigurableCacheManager.create(config);
        
        assertNotNull(cacheManager1);
        assertNotNull(cacheManager2);
        // Note: Cache managers for the same region might be reused by implementation
        // This is an implementation detail and both should work correctly
        assertEquals(cacheManager1.getCacheRegion(), cacheManager2.getCacheRegion());
    }

    @Test
    void testCreateWithInvalidConfigurationThrowsException() {
        // Building config with invalid settings should build but fail during validation
        assertDoesNotThrow(() -> {
            CacheConfig config = CacheConfig.builder()
                .enabled(true)
                .maxSize(0L) // Invalid: zero size
                .build(); // Should build successfully
            
            // Should fail during validation
            assertThrows(IllegalStateException.class, config::validate);
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
                    // Use CacheManager interface methods
                    cacheManager.put("key-" + index, "value-" + index);
                    Object value = cacheManager.get("key-" + index);
                    success[index] = true; // If no exception, it's thread-safe
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
        assertFalse(cacheManager instanceof NoOpCacheManager);
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
        assertFalse(cacheManager instanceof NoOpCacheManager);
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
        assertFalse(cacheManager instanceof NoOpCacheManager);
        
        // Test maintenance method exists
        assertDoesNotThrow(() -> cacheManager.maintenance());
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
        // The cache manager should preserve the provided configuration
        assertEquals(2000L, cacheManager.getMaxSize());
        assertEquals(7200L, cacheManager.getDefaultTtlSeconds());
        assertEquals("preserved-region", cacheManager.getCacheRegion());
        assertFalse(cacheManager instanceof NoOpCacheManager);
    }

    @Test
    void testForRegionWithAutoConfiguration() {
        CacheManager cacheManager = ConfigurableCacheManager.forRegion("custom-region");
        
        assertNotNull(cacheManager);
        assertEquals("custom-region", cacheManager.getCacheRegion());
    }

    @Test
    void testForRegionWithSpecificConfiguration() {
        CacheConfig config = CacheConfig.builder()
            .enabled(true)
            .maxSize(500L)
            .defaultTtlSeconds(1200L)
            .build();
        
        CacheManager cacheManager = ConfigurableCacheManager.forRegion("specific-region", config);
        
        assertNotNull(cacheManager);
        assertEquals("specific-region", cacheManager.getCacheRegion());
        assertEquals(500L, cacheManager.getMaxSize());
        assertEquals(1200L, cacheManager.getDefaultTtlSeconds());
    }

    @Test
    void testForRegionWithNullRegionThrowsException() {
        CacheConfig config = CacheConfig.defaultConfig();
        
        assertThrows(IllegalArgumentException.class, () -> {
            ConfigurableCacheManager.forRegion(null, config);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            ConfigurableCacheManager.forRegion("", config);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            ConfigurableCacheManager.forRegion("  ", config);
        });
    }

    @Test
    void testForRegionWithNullConfigThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            ConfigurableCacheManager.forRegion("test-region", null);
        });
    }
}