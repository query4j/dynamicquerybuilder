package com.github.query4j.cache.config;

import com.github.query4j.cache.CacheManager;
import com.github.query4j.cache.CacheStatistics;
import com.github.query4j.cache.impl.CacheStatisticsImpl;

/**
 * No-operation cache manager implementation used when caching is disabled.
 * 
 * <p>
 * This implementation provides all the CacheManager interface methods but
 * performs no actual caching operations. All get operations return null,
 * and all put/invalidate operations are ignored. This allows the application
 * to use the same caching API whether caching is enabled or disabled.
 * </p>
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
final class NoOpCacheManager implements CacheManager {
    
    private final String cacheRegion;
    private final CacheStatistics statistics;
    
    /**
     * Creates a new no-op cache manager for the specified region.
     * 
     * @param cacheRegion the cache region name
     */
    NoOpCacheManager(String cacheRegion) {
        this.cacheRegion = cacheRegion != null ? cacheRegion : "disabled";
        this.statistics = new CacheStatisticsImpl(0); // Zero capacity
    }
    
    @Override
    public Object get(String key) {
        validateKey(key);
        // Always return null (cache miss) since caching is disabled
        return null;
    }
    
    @Override
    public void put(String key, Object value) {
        validateKey(key);
        // No-op - ignore put operations when caching is disabled
    }
    
    @Override
    public void put(String key, Object value, long ttlSeconds) {
        validateKey(key);
        if (ttlSeconds < 0) {
            throw new IllegalArgumentException("TTL seconds must not be negative, got: " + ttlSeconds);
        }
        // No-op - ignore put operations when caching is disabled
    }
    
    @Override
    public void invalidate(String key) {
        validateKey(key);
        // No-op - nothing to invalidate when caching is disabled
    }
    
    @Override
    public void clear() {
        // No-op - nothing to clear when caching is disabled
    }
    
    @Override
    public CacheStatistics stats() {
        return statistics;
    }
    
    @Override
    public boolean containsKey(String key) {
        validateKey(key);
        return false; // Never contains any keys when caching is disabled
    }
    
    @Override
    public String getCacheRegion() {
        return cacheRegion;
    }
    
    @Override
    public long getMaxSize() {
        return 0; // No capacity when caching is disabled
    }
    
    @Override
    public long getDefaultTtlSeconds() {
        return -1; // No TTL when caching is disabled
    }
    
    @Override
    public void maintenance() {
        // No-op - no maintenance needed when caching is disabled
    }
    
    private void validateKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Cache key must not be null or empty");
        }
    }
    
    @Override
    public String toString() {
        return String.format(
            "NoOpCacheManager{region='%s', enabled=false}",
            cacheRegion
        );
    }
}