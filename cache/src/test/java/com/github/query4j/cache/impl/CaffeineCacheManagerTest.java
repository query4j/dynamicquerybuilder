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