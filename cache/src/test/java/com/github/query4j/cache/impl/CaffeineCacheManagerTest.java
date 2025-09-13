package com.github.query4j.cache.impl;

import com.github.query4j.cache.CacheManager;
import com.github.query4j.cache.CacheStatistics;
import com.github.query4j.core.DynamicQueryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for CaffeineCacheManager including performance and concurrency validation.
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
class CaffeineCacheManagerTest {

    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager = CaffeineCacheManager.create(100L, 60L);
    }

    // === Factory Method Tests ===

    @Test
    @DisplayName("Default factory method should create cache with default settings")
    void testDefaultFactory() {
        CacheManager cache = CaffeineCacheManager.create();
        
        assertEquals("default", cache.getCacheRegion());
        assertEquals(10_000L, cache.getMaxSize());
        assertEquals(3600L, cache.getDefaultTtlSeconds());
        assertNotNull(cache.stats());
    }

    @Test
    @DisplayName("Factory with max size should use default TTL")
    void testFactoryWithMaxSize() {
        CacheManager cache = CaffeineCacheManager.create(500L);
        
        assertEquals("default", cache.getCacheRegion());
        assertEquals(500L, cache.getMaxSize());
        assertEquals(3600L, cache.getDefaultTtlSeconds());
    }

    @Test
    @DisplayName("Factory with both parameters should use specified values")
    void testFactoryWithBothParams() {
        CacheManager cache = CaffeineCacheManager.create(200L, 1800L);
        
        assertEquals("default", cache.getCacheRegion());
        assertEquals(200L, cache.getMaxSize());
        assertEquals(1800L, cache.getDefaultTtlSeconds());
    }

    @Test
    @DisplayName("Named region factory should create singleton instances")
    void testNamedRegionSingleton() {
        CacheManager cache1 = CaffeineCacheManager.forRegion("test-region");
        CacheManager cache2 = CaffeineCacheManager.forRegion("test-region");
        
        assertSame(cache1, cache2, "Same region should return same instance");
        assertEquals("test-region", cache1.getCacheRegion());
    }

    @Test
    @DisplayName("Different regions should create different instances")
    void testDifferentRegions() {
        CacheManager cache1 = CaffeineCacheManager.forRegion("region1");
        CacheManager cache2 = CaffeineCacheManager.forRegion("region2");
        
        assertNotSame(cache1, cache2, "Different regions should return different instances");
        assertEquals("region1", cache1.getCacheRegion());
        assertEquals("region2", cache2.getCacheRegion());
    }

    @Test
    @DisplayName("Factory methods should validate parameters")
    void testFactoryValidation() {
        assertThrows(IllegalArgumentException.class, () -> 
            CaffeineCacheManager.create(0L));
        assertThrows(IllegalArgumentException.class, () -> 
            CaffeineCacheManager.create(-1L));
        assertThrows(IllegalArgumentException.class, () -> 
            CaffeineCacheManager.create(100L, 0L));
        assertThrows(IllegalArgumentException.class, () -> 
            CaffeineCacheManager.create(100L, -1L));
        assertThrows(IllegalArgumentException.class, () -> 
            CaffeineCacheManager.forRegion(null));
        assertThrows(IllegalArgumentException.class, () -> 
            CaffeineCacheManager.forRegion(""));
        assertThrows(IllegalArgumentException.class, () -> 
            CaffeineCacheManager.forRegion("  "));
    }

    @Test
    @DisplayName("Named region factory with custom settings should work correctly")
    void testNamedRegionWithCustomSettings() {
        String regionName = "custom-region";
        long customMaxSize = 500L;
        long customTtl = 120L;
        
        CacheManager cache1 = CaffeineCacheManager.forRegion(regionName, customMaxSize, customTtl);
        CacheManager cache2 = CaffeineCacheManager.forRegion(regionName, customMaxSize, customTtl);
        
        // Should return the same instance (singleton behavior)
        assertSame(cache1, cache2, "Same region should return same instance even with custom settings");
        assertEquals(regionName, cache1.getCacheRegion());
        assertEquals(customMaxSize, cache1.getMaxSize());
        assertEquals(customTtl, cache1.getDefaultTtlSeconds());
    }

    @Test
    @DisplayName("Named region factory with custom settings should validate parameters")
    void testNamedRegionCustomSettingsValidation() {
        // Test null region name
        assertThrows(IllegalArgumentException.class, () -> 
            CaffeineCacheManager.forRegion(null, 100L, 60L));
        
        // Test empty region name
        assertThrows(IllegalArgumentException.class, () -> 
            CaffeineCacheManager.forRegion("", 100L, 60L));
        
        // Test whitespace region name
        assertThrows(IllegalArgumentException.class, () -> 
            CaffeineCacheManager.forRegion("  ", 100L, 60L));
        
        // Test invalid max size
        assertThrows(IllegalArgumentException.class, () -> 
            CaffeineCacheManager.forRegion("test", 0L, 60L));
        assertThrows(IllegalArgumentException.class, () -> 
            CaffeineCacheManager.forRegion("test", -1L, 60L));
        
        // Test invalid TTL
        assertThrows(IllegalArgumentException.class, () -> 
            CaffeineCacheManager.forRegion("test", 100L, 0L));
        assertThrows(IllegalArgumentException.class, () -> 
            CaffeineCacheManager.forRegion("test", 100L, -1L));
    }

    // === Basic Cache Operations ===

    @Test
    @DisplayName("Put and get should work for basic operations")
    void testBasicPutGet() {
        String key = "test-key";
        String value = "test-value";
        
        assertNull(cacheManager.get(key), "Cache should be empty initially");
        
        cacheManager.put(key, value);
        assertEquals(value, cacheManager.get(key), "Should retrieve stored value");
        
        assertTrue(cacheManager.containsKey(key), "Cache should contain the key");
    }

    @Test
    @DisplayName("Put with TTL should store value")
    void testPutWithTtl() {
        String key = "ttl-key";
        String value = "ttl-value";
        
        cacheManager.put(key, value, 120L);
        assertEquals(value, cacheManager.get(key));
        assertTrue(cacheManager.containsKey(key));
    }

    @Test
    @DisplayName("Null values should be handled correctly")
    void testNullValues() {
        String key = "null-key";
        
        cacheManager.put(key, null);
        assertNull(cacheManager.get(key));
        // Caffeine doesn't store null values, so the key should not be present
        assertFalse(cacheManager.containsKey(key));
        
        // Put a real value first
        cacheManager.put(key, "real-value");
        assertTrue(cacheManager.containsKey(key));
        assertEquals("real-value", cacheManager.get(key));
        
        // Now put null - should effectively remove the entry
        cacheManager.put(key, null);
        assertNull(cacheManager.get(key));
        assertFalse(cacheManager.containsKey(key));
    }

    @Test
    @DisplayName("Overwriting values should work correctly")
    void testValueOverwrite() {
        String key = "overwrite-key";
        String value1 = "value1";
        String value2 = "value2";
        
        cacheManager.put(key, value1);
        assertEquals(value1, cacheManager.get(key));
        
        cacheManager.put(key, value2);
        assertEquals(value2, cacheManager.get(key), "Value should be overwritten");
    }

    @Test
    @DisplayName("Invalidation should remove entries")
    void testInvalidation() {
        String key = "invalidate-key";
        String value = "invalidate-value";
        
        cacheManager.put(key, value);
        assertTrue(cacheManager.containsKey(key));
        
        cacheManager.invalidate(key);
        assertNull(cacheManager.get(key));
        assertFalse(cacheManager.containsKey(key));
    }

    @Test
    @DisplayName("Clear should remove all entries")
    void testClear() {
        cacheManager.put("key1", "value1");
        cacheManager.put("key2", "value2");
        cacheManager.put("key3", "value3");
        
        assertTrue(cacheManager.containsKey("key1"));
        assertTrue(cacheManager.containsKey("key2"));
        assertTrue(cacheManager.containsKey("key3"));
        
        cacheManager.clear();
        
        assertFalse(cacheManager.containsKey("key1"));
        assertFalse(cacheManager.containsKey("key2"));
        assertFalse(cacheManager.containsKey("key3"));
        assertEquals(0L, cacheManager.stats().getCurrentSize());
    }

    // === Statistics Tests ===

    @Test
    @DisplayName("Statistics should track hits and misses correctly")
    void testStatisticsTracking() {
        CacheStatistics stats = cacheManager.stats();
        assertEquals(0L, stats.getHitCount());
        assertEquals(0L, stats.getMissCount());
        
        // Generate misses
        cacheManager.get("missing1");
        cacheManager.get("missing2");
        
        stats = cacheManager.stats();
        assertEquals(0L, stats.getHitCount());
        assertEquals(2L, stats.getMissCount());
        
        // Generate hits
        cacheManager.put("key1", "value1");
        cacheManager.get("key1"); // hit
        cacheManager.get("key1"); // hit
        
        stats = cacheManager.stats();
        assertEquals(2L, stats.getHitCount());
        assertEquals(2L, stats.getMissCount());
        assertEquals(0.5, stats.getHitRatio(), 0.001);
    }

    @Test
    @DisplayName("Statistics should track cache size")
    void testSizeTracking() {
        CacheStatistics stats = cacheManager.stats();
        assertEquals(0L, stats.getCurrentSize());
        
        cacheManager.put("key1", "value1");
        cacheManager.put("key2", "value2");
        
        stats = cacheManager.stats();
        assertTrue(stats.getCurrentSize() > 0, "Size should increase with entries");
        
        cacheManager.clear();
        stats = cacheManager.stats();
        assertEquals(0L, stats.getCurrentSize());
    }

    @Test
    @DisplayName("Statistics should track evictions when cache exceeds capacity")
    void testEvictionStatisticsIntegration() {
        // Create a small cache that will trigger evictions
        CacheManager smallCache = CaffeineCacheManager.create(3L, 60L);
        CacheStatistics stats = smallCache.stats();
        
        // Initial state
        assertEquals(0L, stats.getEvictionCount());
        assertEquals(0L, stats.getCurrentSize());
        
        // Fill cache to capacity
        smallCache.put("key1", "value1");
        smallCache.put("key2", "value2");
        smallCache.put("key3", "value3");
        
        stats = smallCache.stats();
        assertEquals(0L, stats.getEvictionCount(), "No evictions should occur within capacity");
        assertEquals(3L, stats.getCurrentSize());
        
        // Force evictions by adding many more items than capacity
        // This ensures evictions happen regardless of LRU timing
        for (int i = 4; i <= 10; i++) {
            smallCache.put("key" + i, "value" + i);
        }
        
        // Trigger cache maintenance to ensure eviction listeners are processed
        smallCache.maintenance();
        
        // Allow some time for async eviction processing
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        stats = smallCache.stats();
        
        // Verify that evictions were recorded and cache size is at max capacity
        assertTrue(stats.getEvictionCount() > 0, 
                  "Evictions should be recorded when cache exceeds capacity. " +
                  "Got eviction count: " + stats.getEvictionCount());
        assertEquals(3L, stats.getCurrentSize(), "Size should be at max capacity");
        
        // Verify that some original keys have been evicted
        int originalKeysRemaining = 0;
        for (int i = 1; i <= 3; i++) {
            if (smallCache.get("key" + i) != null) {
                originalKeysRemaining++;
            }
        }
        
        // At least some of the original keys should have been evicted
        assertTrue(originalKeysRemaining < 3, 
                  "Some original keys should have been evicted. Remaining: " + originalKeysRemaining);
    }

    // === Validation Tests ===

    @Test
    @DisplayName("Operations should validate key parameters")
    void testKeyValidation() {
        assertThrows(IllegalArgumentException.class, () -> cacheManager.get(null));
        assertThrows(IllegalArgumentException.class, () -> cacheManager.get(""));
        assertThrows(IllegalArgumentException.class, () -> cacheManager.get("  "));
        
        assertThrows(IllegalArgumentException.class, () -> cacheManager.put(null, "value"));
        assertThrows(IllegalArgumentException.class, () -> cacheManager.put("", "value"));
        assertThrows(IllegalArgumentException.class, () -> cacheManager.put("  ", "value"));
        
        assertThrows(IllegalArgumentException.class, () -> cacheManager.put(null, "value", 60L));
        assertThrows(IllegalArgumentException.class, () -> cacheManager.put("key", "value", -1L));
        
        assertThrows(IllegalArgumentException.class, () -> cacheManager.invalidate(null));
        assertThrows(IllegalArgumentException.class, () -> cacheManager.invalidate(""));
        
        assertThrows(IllegalArgumentException.class, () -> cacheManager.containsKey(null));
        assertThrows(IllegalArgumentException.class, () -> cacheManager.containsKey(""));
    }

    // === Cache Key Collision Tests ===

    @Test
    @DisplayName("Different keys should not collide")
    void testKeyCollisionAvoidance() {
        // Test SQL + parameter-based key generation scenario
        String sqlKey1 = "SELECT * FROM users WHERE id = :p1";
        String sqlKey2 = "SELECT * FROM users WHERE name = :p1";
        String sqlKey3 = "SELECT * FROM users WHERE id = :p1 AND active = :p2";
        
        cacheManager.put(sqlKey1, "result1");
        cacheManager.put(sqlKey2, "result2");
        cacheManager.put(sqlKey3, "result3");
        
        assertEquals("result1", cacheManager.get(sqlKey1));
        assertEquals("result2", cacheManager.get(sqlKey2));
        assertEquals("result3", cacheManager.get(sqlKey3));
        
        // Keys should be independent
        cacheManager.invalidate(sqlKey1);
        assertNull(cacheManager.get(sqlKey1));
        assertEquals("result2", cacheManager.get(sqlKey2));
        assertEquals("result3", cacheManager.get(sqlKey3));
    }

    // === Concurrency Tests ===

    @Test
    @DisplayName("Cache should handle concurrent access correctly")
    void testConcurrentAccess() throws InterruptedException {
        final int threadCount = 25; // > 20 as per requirements
        final int operationsPerThread = 100;
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch finishLatch = new CountDownLatch(threadCount);
        final AtomicInteger errors = new AtomicInteger(0);
        final AtomicReference<Exception> firstError = new AtomicReference<>();

        // Start concurrent threads
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    
                    for (int j = 0; j < operationsPerThread; j++) {
                        String key = "thread" + threadId + "_key" + j;
                        String value = "thread" + threadId + "_value" + j;
                        
                        // Mix of operations
                        cacheManager.put(key, value);
                        Object retrieved = cacheManager.get(key);
                        assertEquals(value, retrieved, "Retrieved value should match stored value");
                        
                        if (j % 10 == 0) {
                            cacheManager.invalidate(key);
                            assertNull(cacheManager.get(key));
                        }
                    }
                } catch (Exception e) {
                    errors.incrementAndGet();
                    firstError.compareAndSet(null, e);
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        // Start all threads and measure time
        long startTime = System.currentTimeMillis();
        startLatch.countDown();
        
        boolean completed = finishLatch.await(30, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();
        
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        
        assertTrue(completed, "Threads did not complete within timeout");
        assertEquals(0, errors.get(), "Errors occurred during concurrent access: " + 
                    (firstError.get() != null ? firstError.get().getMessage() : ""));
        
        // Verify performance requirement (should be much faster than 1ms average)
        long totalOperations = threadCount * operationsPerThread * 2L; // put + get
        double avgTimeMs = (double)(endTime - startTime) / totalOperations;
        assertTrue(avgTimeMs < 1.0, 
                  String.format("Average operation time %.3fms exceeds 1ms requirement", avgTimeMs));
        
        CacheStatistics finalStats = cacheManager.stats();
        assertTrue(finalStats.getTotalRequests() > 0, "Should have recorded cache operations");
    }

    @RepeatedTest(3)
    @DisplayName("Performance should consistently meet requirements under load")
    void testPerformanceRequirements() throws InterruptedException {
        final int threadCount = 20;
        final int operationsPerThread = 500;
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch finishLatch = new CountDownLatch(threadCount);

        // Pre-populate cache for more realistic get performance
        for (int i = 0; i < 100; i++) {
            cacheManager.put("preload_key_" + i, "preload_value_" + i);
        }

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    
                    for (int j = 0; j < operationsPerThread; j++) {
                        if (j % 2 == 0) {
                            // Get operation (50% of the time)
                            cacheManager.get("preload_key_" + (j % 100));
                        } else {
                            // Put operation (50% of the time)
                            cacheManager.put("perf_" + threadId + "_" + j, "value" + j);
                        }
                    }
                } catch (Exception e) {
                    fail("Performance test failed: " + e.getMessage());
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        long startTime = System.nanoTime();
        startLatch.countDown();
        
        boolean completed = finishLatch.await(10, TimeUnit.SECONDS);
        long endTime = System.nanoTime();
        
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        assertTrue(completed, "Performance test did not complete within timeout");

        // Calculate average operation time
        long totalOperations = threadCount * operationsPerThread;
        double avgTimeMs = (double)(endTime - startTime) / (totalOperations * 1_000_000.0);
        
        assertTrue(avgTimeMs < 1.0, 
                  String.format("Average operation time %.6fms exceeds 1ms requirement", avgTimeMs));
    }

    // === Maintenance and Cleanup Tests ===

    @Test
    @DisplayName("Maintenance should not throw exceptions")
    void testMaintenance() {
        cacheManager.put("key1", "value1");
        cacheManager.put("key2", "value2");
        
        assertDoesNotThrow(() -> cacheManager.maintenance());
        
        // Cache should still be functional after maintenance
        assertEquals("value1", cacheManager.get("key1"));
        assertEquals("value2", cacheManager.get("key2"));
    }

    // === Edge Cases and Exception Coverage Tests ===

    @Test
    @DisplayName("Cache operations should handle edge cases gracefully")
    void testEdgeCasesAndExceptionPaths() {
        // Test with negative TTL values (should validate)
        assertThrows(IllegalArgumentException.class, () -> 
            cacheManager.put("key", "value", -1));
        assertThrows(IllegalArgumentException.class, () -> 
            cacheManager.put("key", "value", -100));
        
        // Test with zero TTL (should validate)
        assertDoesNotThrow(() -> cacheManager.put("key-zero-ttl", "value", 0));
        
        // Test operations after clearing
        cacheManager.put("test-key", "test-value");
        assertTrue(cacheManager.containsKey("test-key"));
        cacheManager.clear();
        assertFalse(cacheManager.containsKey("test-key"));
        
        // Operations should still work normally after clear
        assertDoesNotThrow(() -> cacheManager.put("post-clear", "value"));
        assertDoesNotThrow(() -> cacheManager.get("post-clear"));
        assertDoesNotThrow(() -> cacheManager.containsKey("post-clear"));
        assertDoesNotThrow(() -> cacheManager.invalidate("post-clear"));
    }

    @Test
    @DisplayName("Large key operations should work correctly")
    void testLargeKeyOperations() {
        // Test with very long keys that might stress the cache
        StringBuilder largeKeyBuilder = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeKeyBuilder.append("very-long-cache-key-segment-").append(i).append("-");
        }
        String largeKey = largeKeyBuilder.toString();
        
        // Should handle large keys without exceptions
        assertDoesNotThrow(() -> cacheManager.put(largeKey, "large-key-value"));
        assertDoesNotThrow(() -> cacheManager.get(largeKey));
        assertDoesNotThrow(() -> cacheManager.containsKey(largeKey));
        assertEquals("large-key-value", cacheManager.get(largeKey));
        assertTrue(cacheManager.containsKey(largeKey));
        
        // Clean up
        assertDoesNotThrow(() -> cacheManager.invalidate(largeKey));
        assertFalse(cacheManager.containsKey(largeKey));
    }

    @Test
    @DisplayName("Bulk operations stress test for exception handling")
    void testBulkOperationsStressTest() {
        // Perform many operations in sequence to potentially trigger edge cases
        for (int i = 0; i < 100; i++) {
            String key = "bulk-key-" + i;
            String value = "bulk-value-" + i;
            
            assertDoesNotThrow(() -> cacheManager.put(key, value));
            assertDoesNotThrow(() -> cacheManager.get(key));
            assertDoesNotThrow(() -> cacheManager.containsKey(key));
            
            if (i % 10 == 0) {
                // Periodically clear some entries
                assertDoesNotThrow(() -> cacheManager.invalidate(key));
            }
            
            if (i % 25 == 0) {
                // Periodically run maintenance
                assertDoesNotThrow(() -> cacheManager.maintenance());
            }
        }
        
        // Final cleanup should not throw
        assertDoesNotThrow(() -> cacheManager.clear());
    }

    @Test
    @DisplayName("Comprehensive exception path coverage through intensive operations")
    void testComprehensiveExceptionCoverage() {
        // Create a stress scenario with multiple cache instances and intensive operations
        CacheManager stressCache = CaffeineCacheManager.create(10L, 1L); // Small cache, short TTL
        
        // Rapid-fire operations to stress the cache
        for (int cycle = 0; cycle < 5; cycle++) {
            // Fill cache beyond capacity
            for (int i = 0; i < 20; i++) {
                String key = "stress-key-" + cycle + "-" + i;
                String value = "stress-value-" + System.nanoTime();
                
                // All operations should not throw exceptions
                assertDoesNotThrow(() -> stressCache.put(key, value));
                assertDoesNotThrow(() -> stressCache.get(key));
                assertDoesNotThrow(() -> stressCache.containsKey(key));
                
                // Interleave with other operations
                if (i % 3 == 0) {
                    assertDoesNotThrow(() -> stressCache.invalidate(key));
                }
                if (i % 5 == 0) {
                    assertDoesNotThrow(() -> stressCache.maintenance());
                }
            }
            
            // Clear the cache periodically
            assertDoesNotThrow(() -> stressCache.clear());
        }
        
        // Verify final state
        assertNotNull(stressCache.stats());
        assertEquals(0L, stressCache.stats().getCurrentSize());
    }

    @Test 
    @DisplayName("Extreme key and value scenarios for complete coverage")
    void testExtremeScenarios() {
        // Test with various extreme but valid scenarios
        
        // Empty string values (not null, but empty)
        assertDoesNotThrow(() -> cacheManager.put("empty-value-key", ""));
        assertEquals("", cacheManager.get("empty-value-key"));
        
        // Very large values
        StringBuilder largeValue = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            largeValue.append("Large data segment ").append(i).append(" ");
        }
        String largeValueStr = largeValue.toString();
        
        assertDoesNotThrow(() -> cacheManager.put("large-value-key", largeValueStr));
        assertEquals(largeValueStr, cacheManager.get("large-value-key"));
        
        // Rapid put/get/invalidate cycles
        for (int i = 0; i < 50; i++) {
            final int index = i; // Make it effectively final
            String key = "rapid-cycle-key";
            assertDoesNotThrow(() -> cacheManager.put(key, "value-" + index));
            assertDoesNotThrow(() -> cacheManager.get(key));
            assertDoesNotThrow(() -> cacheManager.containsKey(key));
            assertDoesNotThrow(() -> cacheManager.invalidate(key));
            assertDoesNotThrow(() -> cacheManager.maintenance());
        }
        
        // Test operations on keys with special characters
        String[] specialKeys = {
            "key with spaces", 
            "key\twith\ttabs", 
            "key.with.dots",
            "key_with_underscores",
            "key-with-dashes",
            "key123with456numbers",
            "UPPER_CASE_KEY"
        };
        
        for (String specialKey : specialKeys) {
            assertDoesNotThrow(() -> cacheManager.put(specialKey, "special-value"));
            assertDoesNotThrow(() -> cacheManager.get(specialKey));
            assertDoesNotThrow(() -> cacheManager.containsKey(specialKey));
            assertDoesNotThrow(() -> cacheManager.invalidate(specialKey));
        }
        
        // Final comprehensive cleanup
        assertDoesNotThrow(() -> cacheManager.clear());
        assertDoesNotThrow(() -> cacheManager.maintenance());
    }

    @Test
    @DisplayName("Maximum stress test with concurrent operations and memory pressure")
    void testMaximumStressForExceptionCoverage() {
        // Create multiple cache instances with varying configurations
        CacheManager[] caches = {
            CaffeineCacheManager.create(1L, 1L),      // Minimal cache
            CaffeineCacheManager.create(5L, 2L),      // Small cache  
            CaffeineCacheManager.forRegion("stress-region", 10L, 1L),
            CaffeineCacheManager.forRegion("pressure-region", 2L, 3L)
        };
        
        // Intensive operations across all cache instances
        for (int round = 0; round < 20; round++) {
            for (int cacheIdx = 0; cacheIdx < caches.length; cacheIdx++) {
                CacheManager cache = caches[cacheIdx];
                
                // Rapid operations that might trigger edge cases
                for (int op = 0; op < 100; op++) {
                    String key = "stress-" + round + "-" + cacheIdx + "-" + op;
                    String value = "data-" + System.nanoTime() + "-" + Math.random();
                    
                    try {
                        // All operations should complete without throwing
                        cache.put(key, value);
                        cache.get(key);
                        cache.containsKey(key);
                        
                        if (op % 7 == 0) cache.invalidate(key);
                        if (op % 13 == 0) cache.maintenance();
                        if (op % 19 == 0) cache.clear();
                        
                        // Verify stats access doesn't throw
                        assertNotNull(cache.stats());
                    } catch (Exception e) {
                        fail("Unexpected exception during stress test: " + e.getMessage());
                    }
                }
            }
        }
        
        // Final verification - all caches should be functional
        for (CacheManager cache : caches) {
            assertDoesNotThrow(() -> cache.put("final-test", "final-value"));
            assertDoesNotThrow(() -> cache.get("final-test"));
            assertDoesNotThrow(() -> cache.clear());
            assertNotNull(cache.stats());
        }
    }

    // === toString Test ===

    @Test
    @DisplayName("toString should provide meaningful cache information")
    void testToString() {
        String result = cacheManager.toString();
        
        assertTrue(result.contains("CaffeineCacheManager"));
        assertTrue(result.contains("region="));
        assertTrue(result.contains("maxSize="));
        assertTrue(result.contains("defaultTtl="));
        assertTrue(result.contains("stats="));
    }

    // === Integration Test ===

    @Test
    @DisplayName("Real-world usage scenario with query-like keys")
    void testQueryCacheScenario() {
        // Simulate realistic query caching scenarios
        String baseSQL = "SELECT u.id, u.name FROM users u WHERE u.active = :p1";
        
        // Different parameter combinations should create different cache entries
        String key1 = baseSQL + "|p1=true";
        String key2 = baseSQL + "|p1=false";
        String key3 = "SELECT COUNT(*) FROM users WHERE department = :p1|p1=Engineering";
        
        // Store different result types
        cacheManager.put(key1, java.util.Arrays.asList("user1", "user2", "user3"));
        cacheManager.put(key2, java.util.Arrays.asList("inactiveUser1"));
        cacheManager.put(key3, 42L);
        
        // Retrieve and validate
        Object activeUsers = cacheManager.get(key1);
        Object inactiveUsers = cacheManager.get(key2);
        Object userCount = cacheManager.get(key3);
        
        assertNotNull(activeUsers);
        assertNotNull(inactiveUsers);
        assertNotNull(userCount);
        assertEquals(42L, userCount);
        
        CacheStatistics stats = cacheManager.stats();
        assertEquals(3L, stats.getHitCount());
        assertEquals(0L, stats.getMissCount());
        assertTrue(stats.getCurrentSize() >= 3);
    }
}