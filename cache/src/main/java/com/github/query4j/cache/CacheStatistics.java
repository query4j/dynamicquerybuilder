package com.github.query4j.cache;

/**
 * Interface providing cache performance and diagnostic metrics.
 * 
 * <p>
 * This interface exposes essential cache statistics including hit/miss ratios,
 * eviction counts, and performance metrics. All implementations must provide
 * thread-safe access to these statistics for monitoring and optimization.
 * </p>
 * 
 * <p>
 * Statistics are collected continuously during cache operations and can be
 * used for:
 * </p>
 * <ul>
 * <li>Performance monitoring and tuning</li>
 * <li>Cache configuration optimization</li>
 * <li>Memory usage analysis</li>
 * <li>Hit ratio analysis for query patterns</li>
 * </ul>
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface CacheStatistics {

    /**
     * Returns the total number of cache hits since creation or last reset.
     * 
     * @return the hit count, always non-negative
     * @since 1.0.0
     */
    long getHitCount();

    /**
     * Returns the total number of cache misses since creation or last reset.
     * 
     * @return the miss count, always non-negative
     * @since 1.0.0
     */
    long getMissCount();

    /**
     * Returns the total number of cache evictions due to size or TTL limits.
     * 
     * @return the eviction count, always non-negative
     * @since 1.0.0
     */
    long getEvictionCount();

    /**
     * Returns the current number of entries in the cache.
     * 
     * @return the current cache size, always non-negative
     * @since 1.0.0
     */
    long getCurrentSize();

    /**
     * Returns the maximum configured size of the cache.
     * 
     * @return the maximum size, or -1 if unbounded
     * @since 1.0.0
     */
    long getMaxSize();

    /**
     * Calculates and returns the cache hit ratio as a percentage.
     * 
     * @return the hit ratio between 0.0 and 1.0, or 0.0 if no requests yet
     * @since 1.0.0
     */
    double getHitRatio();

    /**
     * Calculates and returns the cache miss ratio as a percentage.
     * 
     * @return the miss ratio between 0.0 and 1.0, or 1.0 if no hits yet
     * @since 1.0.0
     */
    double getMissRatio();

    /**
     * Returns the total number of requests (hits + misses) since creation.
     * 
     * @return the total request count, always non-negative
     * @since 1.0.0
     */
    default long getTotalRequests() {
        return getHitCount() + getMissCount();
    }

    /**
     * Resets all statistics counters to zero.
     * This operation is thread-safe but may cause temporary inconsistencies
     * in statistics during concurrent operations.
     * 
     * @since 1.0.0
     */
    void reset();
}