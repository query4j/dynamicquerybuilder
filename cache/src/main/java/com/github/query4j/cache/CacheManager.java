package com.github.query4j.cache;

import com.github.query4j.core.DynamicQueryException;

/**
 * High-performance, thread-safe cache manager interface for Query4j.
 * 
 * <p>
 * Provides fast in-memory caching for query results with configurable
 * size-based eviction, TTL (time-to-live), and concurrency control.
 * All implementations must be fully thread-safe for concurrent access
 * and optimized for high-throughput scenarios.
 * </p>
 * 
 * <p>
 * Cache keys should be generated based on query SQL and parameters to ensure
 * proper cache invalidation and collision avoidance. The cache manager supports
 * named cache regions for logical separation of different query types.
 * </p>
 * 
 * <p>
 * Performance requirements:
 * </p>
 * <ul>
 * <li>Average get/put operations must complete in &lt; 1ms under 20+ concurrent threads</li>
 * <li>Support for configurable maximum size and TTL settings</li>
 * <li>LRU or size-based eviction with TTL support</li>
 * <li>Comprehensive statistics tracking for monitoring</li>
 * </ul>
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface CacheManager {

    /**
     * Retrieves a value from the cache by key.
     * 
     * <p>
     * This operation is thread-safe and should complete in sub-millisecond time
     * under normal conditions. Returns null if the key is not found or has expired.
     * </p>
     * 
     * @param key the cache key, must not be null or empty
     * @return the cached value, or null if not found or expired
     * @throws IllegalArgumentException if key is null or empty
     * @throws DynamicQueryException if cache operation fails
     * @since 1.0.0
     */
    Object get(String key);

    /**
     * Stores a value in the cache with the default TTL.
     * 
     * <p>
     * If the cache is at maximum capacity, this operation will trigger eviction
     * according to the configured eviction policy (typically LRU).
     * </p>
     * 
     * @param key   the cache key, must not be null or empty
     * @param value the value to cache, may be null
     * @throws IllegalArgumentException if key is null or empty
     * @throws DynamicQueryException if cache operation fails
     * @since 1.0.0
     */
    void put(String key, Object value);

    /**
     * Stores a value in the cache with a specific TTL.
     * 
     * <p>
     * The value will be automatically evicted after the specified TTL expires,
     * regardless of usage frequency. A TTL of 0 or negative value uses the
     * default TTL setting.
     * </p>
     * 
     * @param key        the cache key, must not be null or empty
     * @param value      the value to cache, may be null
     * @param ttlSeconds the time-to-live in seconds, must not be negative
     * @throws IllegalArgumentException if key is null/empty or ttlSeconds is negative
     * @throws DynamicQueryException if cache operation fails
     * @since 1.0.0
     */
    void put(String key, Object value, long ttlSeconds);

    /**
     * Removes a specific entry from the cache.
     * 
     * @param key the cache key to invalidate, must not be null or empty
     * @throws IllegalArgumentException if key is null or empty
     * @throws DynamicQueryException if cache operation fails
     * @since 1.0.0
     */
    void invalidate(String key);

    /**
     * Removes all entries from the cache immediately.
     * 
     * <p>
     * This operation is thread-safe but may cause temporary performance impact
     * during concurrent access. All statistics are preserved.
     * </p>
     * 
     * @throws DynamicQueryException if cache operation fails
     * @since 1.0.0
     */
    void clear();

    /**
     * Returns current cache performance and diagnostic statistics.
     * 
     * <p>
     * Statistics include hit/miss counts, eviction metrics, and current cache size.
     * The returned statistics are thread-safe and provide a consistent snapshot
     * at the time of the call.
     * </p>
     * 
     * @return cache statistics, never null
     * @since 1.0.0
     */
    CacheStatistics stats();

    /**
     * Checks if the cache contains a valid (non-expired) entry for the given key.
     * 
     * <p>
     * This method does not count as a cache hit or miss in statistics.
     * It only verifies the presence of a valid entry.
     * </p>
     * 
     * @param key the cache key to check, must not be null or empty
     * @return true if the key exists and is not expired, false otherwise
     * @throws IllegalArgumentException if key is null or empty
     * @throws DynamicQueryException if cache operation fails
     * @since 1.0.0
     */
    boolean containsKey(String key);

    /**
     * Returns the name of the cache region this manager operates on.
     * 
     * @return the cache region name, never null
     * @since 1.0.0
     */
    String getCacheRegion();

    /**
     * Returns the maximum number of entries this cache can hold.
     * 
     * @return the maximum size, or -1 if unbounded
     * @since 1.0.0
     */
    long getMaxSize();

    /**
     * Returns the default TTL for cache entries in seconds.
     * 
     * @return the default TTL in seconds, or -1 if no default TTL
     * @since 1.0.0
     */
    long getDefaultTtlSeconds();

    /**
     * Performs cache maintenance operations like expired entry cleanup.
     * 
     * <p>
     * This method is typically called automatically by the cache implementation
     * but can be invoked manually for immediate cleanup. It's thread-safe and
     * non-blocking.
     * </p>
     * 
     * @since 1.0.0
     */
    void maintenance();
}