package com.github.query4j.cache.impl;

import com.github.query4j.cache.CacheStatistics;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe implementation of cache statistics tracking.
 * 
 * <p>
 * Uses atomic operations for all counters to ensure thread-safety without
 * synchronization overhead. Provides consistent snapshot views of cache
 * performance metrics for monitoring and optimization.
 * </p>
 * 
 * <p>
 * All operations are lock-free and optimized for high-throughput concurrent
 * access patterns typical in query caching scenarios.
 * </p>
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class CacheStatisticsImpl implements CacheStatistics {

    private final AtomicLong hitCount = new AtomicLong(0);
    private final AtomicLong missCount = new AtomicLong(0);
    private final AtomicLong evictionCount = new AtomicLong(0);
    private final AtomicLong currentSize = new AtomicLong(0);
    private final long maxSize;

    /**
     * Creates a new statistics tracker for the given maximum cache size.
     * 
     * @param maxSize the maximum cache size, or -1 for unbounded
     */
    public CacheStatisticsImpl(long maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public long getHitCount() {
        return hitCount.get();
    }

    @Override
    public long getMissCount() {
        return missCount.get();
    }

    @Override
    public long getEvictionCount() {
        return evictionCount.get();
    }

    @Override
    public long getCurrentSize() {
        return currentSize.get();
    }

    @Override
    public long getMaxSize() {
        return maxSize;
    }

    @Override
    public double getHitRatio() {
        long hits = getHitCount();
        long total = getTotalRequests();
        return total == 0 ? 0.0 : (double) hits / total;
    }

    @Override
    public double getMissRatio() {
        long misses = getMissCount();
        long total = getTotalRequests();
        return total == 0 ? 1.0 : (double) misses / total;
    }

    @Override
    public void reset() {
        hitCount.set(0);
        missCount.set(0);
        evictionCount.set(0);
        // Note: currentSize is not reset as it reflects actual cache state
    }

    /**
     * Increments the hit counter atomically.
     * Package-private for use by cache implementation.
     */
    void recordHit() {
        hitCount.incrementAndGet();
    }

    /**
     * Increments the miss counter atomically.
     * Package-private for use by cache implementation.
     */
    void recordMiss() {
        missCount.incrementAndGet();
    }

    /**
     * Increments the eviction counter atomically.
     * Package-private for use by cache implementation.
     */
    void recordEviction() {
        evictionCount.incrementAndGet();
    }

    /**
     * Sets the current cache size atomically.
     * Package-private for use by cache implementation.
     * 
     * @param size the new cache size
     */
    void setCurrentSize(long size) {
        currentSize.set(size);
    }

    /**
     * Increments the current cache size atomically.
     * Package-private for use by cache implementation.
     * 
     * @return the new size after increment
     */
    long incrementSize() {
        return currentSize.incrementAndGet();
    }

    /**
     * Decrements the current cache size atomically.
     * Package-private for use by cache implementation.
     * 
     * @return the new size after decrement
     */
    long decrementSize() {
        return currentSize.decrementAndGet();
    }

    @Override
    public String toString() {
        return String.format(
            "CacheStatistics{hits=%d, misses=%d, evictions=%d, size=%d/%s, hitRatio=%.3f}",
            getHitCount(), getMissCount(), getEvictionCount(), 
            getCurrentSize(), maxSize == -1 ? "∞" : String.valueOf(maxSize), 
            getHitRatio()
        );
    }
}