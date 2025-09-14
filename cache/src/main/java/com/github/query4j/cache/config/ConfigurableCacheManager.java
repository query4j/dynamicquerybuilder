package com.github.query4j.cache.config;

import com.github.query4j.cache.CacheManager;
import com.github.query4j.cache.impl.CaffeineCacheManager;
import com.github.query4j.core.config.CacheConfig;
import com.github.query4j.core.config.Query4jConfigurationFactory;

/**
 * Factory for creating cache managers with configuration support.
 * 
 * <p>
 * This factory creates cache managers configured according to the application's
 * configuration settings. It supports both auto-configuration from external sources
 * and programmatic configuration overrides.
 * </p>
 * 
 * <p>
 * Usage examples:
 * <pre>
 * // Create cache manager with auto-configuration
 * CacheManager cacheManager = ConfigurableCacheManager.create();
 * 
 * // Create cache manager with specific configuration
 * CacheConfig config = CacheConfig.highPerformanceConfig();
 * CacheManager cacheManager = ConfigurableCacheManager.create(config);
 * 
 * // Create named cache manager with auto-configuration
 * CacheManager cacheManager = ConfigurableCacheManager.forRegion("myregion");
 * </pre>
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class ConfigurableCacheManager {
    
    // Prevent instantiation
    private ConfigurableCacheManager() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Creates a cache manager using auto-loaded configuration.
     * 
     * <p>
     * This method loads configuration from all available sources and creates
     * a cache manager with those settings. If caching is disabled in the
     * configuration, returns a no-op cache manager.
     * </p>
     * 
     * @return configured cache manager instance
     */
    public static CacheManager create() {
        CacheConfig config = Query4jConfigurationFactory.getDefault().getCache();
        return createWithConfig(config);
    }
    
    /**
     * Creates a cache manager with specific configuration.
     * 
     * @param config the cache configuration to use
     * @return configured cache manager instance
     * @throws IllegalArgumentException if config is null
     */
    public static CacheManager create(CacheConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Cache configuration must not be null");
        }
        return createWithConfig(config);
    }
    
    /**
     * Creates a named cache manager using auto-loaded configuration.
     * 
     * @param regionName the cache region name
     * @return configured cache manager instance for the specified region
     * @throws IllegalArgumentException if regionName is null or empty
     */
    public static CacheManager forRegion(String regionName) {
        CacheConfig config = Query4jConfigurationFactory.getDefault().getCache();
        return createWithConfig(config, regionName);
    }
    
    /**
     * Creates a named cache manager with specific configuration.
     * 
     * @param regionName the cache region name
     * @param config the cache configuration to use
     * @return configured cache manager instance for the specified region
     * @throws IllegalArgumentException if regionName is null/empty or config is null
     */
    public static CacheManager forRegion(String regionName, CacheConfig config) {
        if (regionName == null || regionName.trim().isEmpty()) {
            throw new IllegalArgumentException("Region name must not be null or empty");
        }
        if (config == null) {
            throw new IllegalArgumentException("Cache configuration must not be null");
        }
        return createWithConfig(config, regionName);
    }
    
    private static CacheManager createWithConfig(CacheConfig config) {
        return createWithConfig(config, config.getDefaultRegion());
    }
    
    private static CacheManager createWithConfig(CacheConfig config, String regionName) {
        config.validate(); // Ensure configuration is valid
        
        if (!config.isEnabled()) {
            // Return a no-op cache manager when caching is disabled
            return new NoOpCacheManager(regionName);
        }
        
        // Create Caffeine cache manager with configuration
        return CaffeineCacheManager.forRegion(
            regionName, 
            config.getMaxSize(), 
            config.getDefaultTtlSeconds()
        );
    }
}