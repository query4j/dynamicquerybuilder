package com.github.query4j.cache;

/**
 * Interface providing comprehensive cache performance and diagnostic metrics.
 * 
 * <p>
 * This interface exposes essential cache statistics including hit/miss ratios,
 * eviction counts, and performance metrics. All implementations must provide
 * thread-safe access to these statistics for monitoring and optimization in
 * high-concurrency environments.
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
 * <li>Capacity planning and scaling decisions</li>
 * </ul>
 * 
 * <p>
 * Target metrics for well-tuned caches:
 * </p>
 * <ul>
 * <li>Hit ratio: &gt; 0.80 (80%) for frequently accessed queries</li>
 * <li>Eviction rate: &lt; 10% of total requests for stable workloads</li>
 * <li>Current size: 60-80% of max size for optimal performance</li>
 * </ul>
 * 
 * <p>
 * Example usage for monitoring:
 * </p>
 * 
 * <pre>{@code
 * CacheManager cache = CaffeineCacheManager.create();
 * CacheStatistics stats = cache.stats();
 * 
 * // Monitor cache effectiveness
 * System.out.println("Cache hit ratio: " + 
 *     String.format("%.2f%%", stats.getHitRatio() * 100));
 * System.out.println("Total requests: " + stats.getTotalRequests());
 * System.out.println("Evictions: " + stats.getEvictionCount());
 * 
 * // Check if cache needs tuning
 * if (stats.getHitRatio() < 0.5) {
 *     logger.warn("Low cache hit ratio - consider increasing cache size or TTL");
 * }
 * 
 * // Capacity monitoring
 * double capacityUsed = (double) stats.getCurrentSize() / stats.getMaxSize();
 * if (capacityUsed > 0.9) {
 *     logger.warn("Cache near capacity - evictions may increase");
 * }
 * }</pre>
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface CacheStatistics {

    /**
     * Returns the total number of cache hits since creation or last reset.
     * 
     * <p>
     * A cache hit occurs when a requested key is found in the cache and has
     * not expired. This metric indicates how often the cache successfully
     * serves requests without database access.
     * </p>
     * 
     * @return the hit count, always &gt;= 0
     * @since 1.0.0
     */
    long getHitCount();

    /**
     * Returns the total number of cache misses since creation or last reset.
     * 
     * <p>
     * A cache miss occurs when a requested key is not found in the cache or
     * has expired. This metric indicates how often requests must fall through
     * to the database.
     * </p>
     * 
     * @return the miss count, always &gt;= 0
     * @since 1.0.0
     */
    long getMissCount();

    /**
     * Returns the total number of cache evictions due to size or TTL limits.
     * 
     * <p>
     * Evictions occur when:
     * </p>
     * <ul>
     * <li>The cache reaches its maximum size (size-based eviction)</li>
     * <li>Entries expire based on their TTL (time-based eviction)</li>
     * <li>Manual invalidation via {@link CacheManager#invalidate(String)}</li>
     * </ul>
     * 
     * <p>
     * High eviction rates may indicate that cache size is too small or TTL
     * is too short for the workload.
     * </p>
     * 
     * @return the eviction count, always &gt;= 0
     * @since 1.0.0
     */
    long getEvictionCount();

    /**
     * Returns the current number of entries in the cache.
     * 
     * <p>
     * This is a snapshot of the cache size at the time of the call and may
     * change immediately in concurrent environments. The count includes all
     * non-expired entries currently stored in memory.
     * </p>
     * 
     * @return the current cache size, always &gt;= 0
     * @since 1.0.0
     */
    long getCurrentSize();

    /**
     * Returns the maximum configured size of the cache.
     * 
     * <p>
     * This is the upper limit of entries the cache can hold before eviction
     * begins. When the cache reaches this size, the least recently used (LRU)
     * entries are evicted to make room for new entries.
     * </p>
     * 
     * @return the maximum size, or -1 if unbounded
     * @since 1.0.0
     */
    long getMaxSize();

    /**
     * Calculates and returns the cache hit ratio.
     * 
     * <p>
     * The hit ratio is calculated as {@code hits / (hits + misses)}, providing
     * a value between 0.0 (no hits) and 1.0 (all hits). This is the primary
     * metric for cache effectiveness.
     * </p>
     * 
     * <p>
     * Target ratios:
     * </p>
     * <ul>
     * <li>&gt; 0.80: Excellent - cache is highly effective</li>
     * <li>0.60-0.80: Good - cache is working well</li>
     * <li>0.40-0.60: Fair - consider tuning cache size or TTL</li>
     * <li>&lt; 0.40: Poor - cache may not be beneficial</li>
     * </ul>
     * 
     * @return the hit ratio between 0.0 and 1.0, or 0.0 if no requests yet
     * @since 1.0.0
     */
    double getHitRatio();

    /**
     * Calculates and returns the cache miss ratio.
     * 
     * <p>
     * The miss ratio is calculated as {@code misses / (hits + misses)}, providing
     * a value between 0.0 (no misses) and 1.0 (all misses). This is the complement
     * of the hit ratio: {@code missRatio = 1.0 - hitRatio}.
     * </p>
     * 
     * @return the miss ratio between 0.0 and 1.0, or 1.0 if no hits yet
     * @since 1.0.0
     */
    double getMissRatio();

    /**
     * Returns the total number of cache requests (hits + misses) since creation.
     * 
     * <p>
     * This is a convenience method that combines hit and miss counts to provide
     * the total number of cache lookups performed. Useful for calculating
     * request rates and overall cache usage.
     * </p>
     * 
     * @return the total request count, always &gt;= 0
     * @since 1.0.0
     */
    default long getTotalRequests() {
        return getHitCount() + getMissCount();
    }

    /**
     * Resets all statistics counters to zero.
     * 
     * <p>
     * This operation is thread-safe but may cause temporary inconsistencies
     * in statistics during concurrent operations. Cache entries themselves
     * are not affected - only the statistics counters are reset.
     * </p>
     * 
     * <p>
     * Use this method when:
     * </p>
     * <ul>
     * <li>Starting a new monitoring period</li>
     * <li>After configuration changes to establish new baselines</li>
     * <li>During testing to isolate metrics for specific scenarios</li>
     * </ul>
     * 
     * @since 1.0.0
     */
    void reset();
}