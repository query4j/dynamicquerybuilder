package com.github.query4j.core.config;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * Immutable configuration properties for Query4j cache module.
 * Provides control over caching behavior, TTL settings, and performance parameters.
 * 
 * <p>
 * This configuration is used by cache managers to initialize and configure
 * cache instances. All values are validated and provide safe defaults for
 * production use.
 * </p>
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
@Value
@Builder
public class CacheConfig {
    
    /**
     * Enable or disable caching globally.
     * When disabled, all cache operations become no-ops.
     */
    @Builder.Default
    boolean enabled = true;
    
    /**
     * Default cache TTL (time-to-live) in seconds.
     * Cached entries will be evicted after this time.
     */
    @Builder.Default
    long defaultTtlSeconds = 3600L; // 1 hour
    
    /**
     * Maximum cache size (number of entries).
     * When exceeded, LRU eviction policy applies.
     */
    @Builder.Default
    long maxSize = 10_000L;
    
    /**
     * Default cache region name for unnamed cache instances.
     */
    @NonNull
    @Builder.Default
    String defaultRegion = "default";
    
    /**
     * Enable detailed cache statistics collection.
     * When disabled, improves performance but reduces observability.
     */
    @Builder.Default
    boolean statisticsEnabled = true;
    
    /**
     * Cache maintenance interval in seconds.
     * How often to run cleanup operations for expired entries.
     */
    @Builder.Default
    long maintenanceIntervalSeconds = 300L; // 5 minutes
    
    /**
     * Enable cache key validation.
     * When enabled, validates cache keys match expected patterns.
     */
    @Builder.Default
    boolean keyValidationEnabled = true;
    
    /**
     * Maximum cache key length in characters.
     * Prevents memory issues with excessively long keys.
     */
    @Builder.Default
    int maxKeyLength = 512;
    
    /**
     * Cache concurrency level for internal data structures.
     * Higher values improve concurrent access performance.
     */
    @Builder.Default
    int concurrencyLevel = 16;
    
    /**
     * Enable automatic cache warming on startup.
     * When enabled, cache will be pre-populated with common queries.
     */
    @Builder.Default
    boolean autoWarmupEnabled = false;
    
    /**
     * Number of entries to pre-load during cache warming.
     */
    @Builder.Default
    int warmupSize = 100;
    
    /**
     * Creates a default configuration instance.
     *
     * @return default CacheConfig, never null
     */
    public static CacheConfig defaultConfig() {
        return CacheConfig.builder().build();
    }
    
    /**
     * Creates a configuration optimized for high-performance scenarios.
     *
     * @return high-performance CacheConfig, never null
     */
    public static CacheConfig highPerformanceConfig() {
        return CacheConfig.builder()
                .maxSize(50_000L) // Larger cache
                .defaultTtlSeconds(7200L) // Longer TTL
                .concurrencyLevel(32) // Higher concurrency
                .statisticsEnabled(false) // Disable for max performance
                .maintenanceIntervalSeconds(600L) // Less frequent maintenance
                .build();
    }
    
    /**
     * Creates a configuration optimized for development/testing.
     *
     * @return development-friendly CacheConfig, never null
     */
    public static CacheConfig developmentConfig() {
        return CacheConfig.builder()
                .maxSize(1_000L) // Smaller cache for testing
                .defaultTtlSeconds(300L) // Shorter TTL for faster feedback
                .statisticsEnabled(true)
                .keyValidationEnabled(true)
                .autoWarmupEnabled(false)
                .build();
    }
    
    /**
     * Creates a configuration with caching disabled.
     * Useful for testing or environments where caching is not desired.
     *
     * @return disabled CacheConfig, never null
     */
    public static CacheConfig disabledConfig() {
        return CacheConfig.builder()
                .enabled(false)
                .build();
    }
    
    /**
     * Validates this configuration and throws an exception if invalid.
     * 
     * @throws IllegalStateException if configuration is invalid
     */
    public void validate() {
        if (defaultTtlSeconds < 0) {
            throw new IllegalStateException("defaultTtlSeconds must not be negative, got: " + defaultTtlSeconds);
        }
        if (maxSize <= 0) {
            throw new IllegalStateException("maxSize must be positive, got: " + maxSize);
        }
        if (defaultRegion == null || defaultRegion.trim().isEmpty()) {
            throw new IllegalStateException("defaultRegion must not be null or empty");
        }
        if (maintenanceIntervalSeconds < 0) {
            throw new IllegalStateException("maintenanceIntervalSeconds must not be negative, got: " + maintenanceIntervalSeconds);
        }
        if (maxKeyLength <= 0) {
            throw new IllegalStateException("maxKeyLength must be positive, got: " + maxKeyLength);
        }
        if (concurrencyLevel <= 0) {
            throw new IllegalStateException("concurrencyLevel must be positive, got: " + concurrencyLevel);
        }
        if (warmupSize < 0) {
            throw new IllegalStateException("warmupSize must not be negative, got: " + warmupSize);
        }
    }
}