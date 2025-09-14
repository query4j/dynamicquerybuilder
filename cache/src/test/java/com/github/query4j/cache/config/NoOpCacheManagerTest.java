package com.github.query4j.cache.config;

import com.github.query4j.cache.CacheManager;
import com.github.query4j.cache.CacheStatistics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for NoOpCacheManager.
 * Ensures 95%+ coverage by testing all no-op operations and thread safety.
 */
public class NoOpCacheManagerTest {

    @Test
    void testConstructor() {
        NoOpCacheManager cacheManager = new NoOpCacheManager("test-region");
        assertNotNull(cacheManager);
    }

    @Test
    void testPutOperations() {
        CacheManager cacheManager = new NoOpCacheManager("test-region");
        
        // All put operations should complete without error but do nothing
        assertDoesNotThrow(() -> {
            cacheManager.put("key1", "value1");
            cacheManager.put("key2", "value2", 3600L);
        });
    }

    @Test
    void testGetOperations() {
        CacheManager cacheManager = new NoOpCacheManager("test-region");
        
        // All get operations should return null
        assertNull(cacheManager.get("any-key"));
        assertNull(cacheManager.get("another-key"));
        
        // Even after putting something, should still return null
        cacheManager.put("test-key", "test-value");
        assertNull(cacheManager.get("test-key"));
    }

    @Test
    void testInvalidateOperations() {
        CacheManager cacheManager = new NoOpCacheManager("test-region");
        
        // Invalidate operations should complete without error
        assertDoesNotThrow(() -> {
            cacheManager.invalidate("any-key");
            cacheManager.invalidate("non-existent-key");
        });
    }

    @Test
    void testClearOperation() {
        CacheManager cacheManager = new NoOpCacheManager("test-region");
        
        // Clear should complete without error
        assertDoesNotThrow(() -> {
            cacheManager.clear();
        });
        
        // Multiple clears should be safe
        assertDoesNotThrow(() -> {
            cacheManager.clear();
            cacheManager.clear();
        });
    }

    @Test
    void testContainsKeyOperations() {
        CacheManager cacheManager = new NoOpCacheManager("test-region");
        
        // Contains should always return false
        assertFalse(cacheManager.containsKey("any-key"));
        assertFalse(cacheManager.containsKey("test-key"));
        
        // Even after putting something
        cacheManager.put("test-key", "test-value");
        assertFalse(cacheManager.containsKey("test-key"));
    }

    @Test
    void testCacheRegionOperation() {
        CacheManager cacheManager = new NoOpCacheManager("test-region");
        
        // Should return the region name
        assertEquals("test-region", cacheManager.getCacheRegion());
    }

    @Test
    void testStatisticsOperations() {
        CacheManager cacheManager = new NoOpCacheManager("test-region");
        
        // Get statistics should return empty/zero statistics
        CacheStatistics stats = cacheManager.stats();
        assertNotNull(stats);
        assertEquals(0L, stats.getHitCount());
        assertEquals(0L, stats.getMissCount());
        assertEquals(0L, stats.getCurrentSize());
        assertEquals(0L, stats.getEvictionCount());
        
        // Statistics should still be zero after operations
        cacheManager.put("key", "value");
        cacheManager.get("key");
        cacheManager.invalidate("key");
        
        CacheStatistics afterStats = cacheManager.stats();
        assertNotNull(afterStats);
        assertEquals(0L, afterStats.getHitCount());
        assertEquals(0L, afterStats.getMissCount());
        assertEquals(0L, afterStats.getCurrentSize());
        assertEquals(0L, afterStats.getEvictionCount());
    }

    @Test
    void testThreadSafety() throws InterruptedException {
        final CacheManager cacheManager = new NoOpCacheManager("test-region");
        final int threadCount = 20;
        final Thread[] threads = new Thread[threadCount];
        final boolean[] success = new boolean[threadCount];
        
        // Create multiple threads that perform various operations
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    // Perform various operations that should all be safe
                    cacheManager.put("key-" + index, "value-" + index);
                    cacheManager.get("key-" + index);
                    cacheManager.invalidate("key-" + index);
                    cacheManager.containsKey("key-" + index);
                    cacheManager.getCacheRegion();
                    cacheManager.stats();
                    cacheManager.clear();
                    
                    success[index] = true;
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
            assertTrue(threadSuccess, "Thread operation should succeed");
        }
    }

    @Test
    void testConsistentBehavior() {
        CacheManager cacheManager = new NoOpCacheManager("test-region");
        
        // Operations should behave consistently
        for (int i = 0; i < 100; i++) {
            String key = "key-" + i;
            String value = "value-" + i;
            
            // Put should not throw
            assertDoesNotThrow(() -> cacheManager.put(key, value));
            
            // Get should always return null
            assertNull(cacheManager.get(key));
            
            // Contains should always return false
            assertFalse(cacheManager.containsKey(key));
            
            // Invalidate should not throw
            assertDoesNotThrow(() -> cacheManager.invalidate(key));
        }
    }

    @Test
    void testNullKeyHandling() {
        CacheManager cacheManager = new NoOpCacheManager("test-region");
        
        // Should handle null keys by throwing IllegalArgumentException  
        assertThrows(IllegalArgumentException.class, () -> {
            cacheManager.put(null, "value");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            cacheManager.get(null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            cacheManager.containsKey(null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            cacheManager.invalidate(null);
        });
    }

    @Test
    void testNullCacheRegionConstructor() {
        // Should handle null region by using default
        CacheManager cacheManager = new NoOpCacheManager(null);
        assertNotNull(cacheManager);
        assertEquals("disabled", cacheManager.getCacheRegion());
    }

    @Test
    void testLargeOperations() {
        CacheManager cacheManager = new NoOpCacheManager("performance-test");
        
        // Should handle large numbers of operations efficiently
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 10000; i++) {
            cacheManager.put("key-" + i, "value-" + i);
            cacheManager.get("key-" + i);
            cacheManager.invalidate("key-" + i);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // NoOp operations should be very fast (less than 1 second for 10k ops)
        assertTrue(duration < 1000, "NoOp operations should be very fast, took " + duration + "ms");
        
        // Statistics should still be zero
        CacheStatistics stats = cacheManager.stats();
        assertEquals(0L, stats.getHitCount());
        assertEquals(0L, stats.getCurrentSize());
    }
}