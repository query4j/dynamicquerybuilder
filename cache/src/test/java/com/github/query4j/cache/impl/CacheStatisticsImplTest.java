package com.github.query4j.cache.impl;

import com.github.query4j.cache.CacheStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for CacheStatisticsImpl including thread safety validation.
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
class CacheStatisticsImplTest {

    private CacheStatisticsImpl statistics;

    @BeforeEach
    void setUp() {
        statistics = new CacheStatisticsImpl(1000L);
    }

    @Test
    @DisplayName("Initial statistics should have zero counts")
    void testInitialState() {
        assertEquals(0L, statistics.getHitCount());
        assertEquals(0L, statistics.getMissCount());
        assertEquals(0L, statistics.getEvictionCount());
        assertEquals(0L, statistics.getCurrentSize());
        assertEquals(1000L, statistics.getMaxSize());
        assertEquals(0L, statistics.getTotalRequests());
        assertEquals(0.0, statistics.getHitRatio(), 0.001);
        assertEquals(1.0, statistics.getMissRatio(), 0.001);
    }

    @Test
    @DisplayName("Hit recording should update counters correctly")
    void testHitRecording() {
        statistics.recordHit();
        statistics.recordHit();
        
        assertEquals(2L, statistics.getHitCount());
        assertEquals(0L, statistics.getMissCount());
        assertEquals(2L, statistics.getTotalRequests());
        assertEquals(1.0, statistics.getHitRatio(), 0.001);
        assertEquals(0.0, statistics.getMissRatio(), 0.001);
    }

    @Test
    @DisplayName("Miss recording should update counters correctly")
    void testMissRecording() {
        statistics.recordMiss();
        statistics.recordMiss();
        statistics.recordMiss();
        
        assertEquals(0L, statistics.getHitCount());
        assertEquals(3L, statistics.getMissCount());
        assertEquals(3L, statistics.getTotalRequests());
        assertEquals(0.0, statistics.getHitRatio(), 0.001);
        assertEquals(1.0, statistics.getMissRatio(), 0.001);
    }

    @Test
    @DisplayName("Mixed hit/miss recording should calculate ratios correctly")
    void testMixedHitMissRatios() {
        // 7 hits, 3 misses = 70% hit ratio, 30% miss ratio
        for (int i = 0; i < 7; i++) {
            statistics.recordHit();
        }
        for (int i = 0; i < 3; i++) {
            statistics.recordMiss();
        }
        
        assertEquals(7L, statistics.getHitCount());
        assertEquals(3L, statistics.getMissCount());
        assertEquals(10L, statistics.getTotalRequests());
        assertEquals(0.7, statistics.getHitRatio(), 0.001);
        assertEquals(0.3, statistics.getMissRatio(), 0.001);
    }

    @Test
    @DisplayName("Eviction recording should increment counter")
    void testEvictionRecording() {
        statistics.recordEviction();
        statistics.recordEviction();
        
        assertEquals(2L, statistics.getEvictionCount());
    }

    @Test
    @DisplayName("Size management should work correctly")
    void testSizeManagement() {
        statistics.setCurrentSize(50L);
        assertEquals(50L, statistics.getCurrentSize());
        
        long newSize = statistics.incrementSize();
        assertEquals(51L, newSize);
        assertEquals(51L, statistics.getCurrentSize());
        
        newSize = statistics.decrementSize();
        assertEquals(50L, newSize);
        assertEquals(50L, statistics.getCurrentSize());
    }

    @Test
    @DisplayName("Reset should clear counters but preserve current size")
    void testReset() {
        statistics.recordHit();
        statistics.recordMiss();
        statistics.recordEviction();
        statistics.setCurrentSize(25L);
        
        statistics.reset();
        
        assertEquals(0L, statistics.getHitCount());
        assertEquals(0L, statistics.getMissCount());
        assertEquals(0L, statistics.getEvictionCount());
        assertEquals(25L, statistics.getCurrentSize()); // Size should not be reset
        assertEquals(0L, statistics.getTotalRequests());
    }

    @Test
    @DisplayName("Unbounded cache should return -1 for max size")
    void testUnboundedCache() {
        CacheStatisticsImpl unbounded = new CacheStatisticsImpl(-1L);
        assertEquals(-1L, unbounded.getMaxSize());
    }

    @Test
    @DisplayName("Thread safety validation with concurrent access")
    void testThreadSafety() throws InterruptedException {
        final int threadCount = 20;
        final int operationsPerThread = 1000;
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch finishLatch = new CountDownLatch(threadCount);
        final AtomicInteger completedThreads = new AtomicInteger(0);

        // Start concurrent threads
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    
                    for (int j = 0; j < operationsPerThread; j++) {
                        if (threadId % 4 == 0) {
                            statistics.recordHit();
                        } else if (threadId % 4 == 1) {
                            statistics.recordMiss();
                        } else if (threadId % 4 == 2) {
                            statistics.recordEviction();
                        } else {
                            statistics.incrementSize();
                        }
                    }
                    
                    completedThreads.incrementAndGet();
                } catch (Exception e) {
                    fail("Thread " + threadId + " failed: " + e.getMessage());
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        // Start all threads simultaneously
        startLatch.countDown();
        
        // Wait for completion with timeout
        boolean completed = finishLatch.await(10, TimeUnit.SECONDS);
        assertTrue(completed, "Threads did not complete within timeout");
        
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        // Verify expected totals (each type has 5 threads * 1000 operations = 5000)
        assertEquals(5000L, statistics.getHitCount(), "Hit count mismatch");
        assertEquals(5000L, statistics.getMissCount(), "Miss count mismatch");
        assertEquals(5000L, statistics.getEvictionCount(), "Eviction count mismatch");
        assertEquals(5000L, statistics.getCurrentSize(), "Size mismatch");
        assertEquals(10000L, statistics.getTotalRequests(), "Total requests mismatch");
        assertEquals(threadCount, completedThreads.get(), "Not all threads completed");

        // Validate ratios
        assertEquals(0.5, statistics.getHitRatio(), 0.001);
        assertEquals(0.5, statistics.getMissRatio(), 0.001);
    }

    @Test
    @DisplayName("toString should provide meaningful representation")
    void testToString() {
        statistics.recordHit();
        statistics.recordMiss();
        statistics.recordEviction();
        statistics.setCurrentSize(50L);
        
        String result = statistics.toString();
        
        assertTrue(result.contains("hits=1"));
        assertTrue(result.contains("misses=1"));
        assertTrue(result.contains("evictions=1"));
        assertTrue(result.contains("size=50"));
        assertTrue(result.contains("1000"));
        assertTrue(result.contains("hitRatio"));
    }

    @Test
    @DisplayName("toString should handle unbounded cache display")
    void testToStringUnbounded() {
        CacheStatisticsImpl unbounded = new CacheStatisticsImpl(-1L);
        String result = unbounded.toString();
        assertTrue(result.contains("∞"), "Should display infinity symbol for unbounded cache");
    }
}