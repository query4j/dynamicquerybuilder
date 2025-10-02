package com.github.query4j.core.config;

import com.github.query4j.core.DynamicQueryException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Factory for creating and managing Query4j configuration instances.
 * 
 * <p>
 * This factory provides singleton access to configuration instances and supports
 * both auto-loading from external sources and programmatic configuration.
 * It ensures thread-safe initialization and provides caching for performance.
 * </p>
 * 
 * <p>
 * Usage examples:
 * <pre>
 * // Get default auto-configured instance
 * Query4jConfig config = Query4jConfigurationFactory.getDefault();
 * 
 * // Load from specific file
 * Query4jConfig config = Query4jConfigurationFactory.loadFromFile("myconfig.properties");
 * 
 * // Programmatic configuration
 * Query4jConfig config = Query4jConfigurationFactory.builder()
 *     .core(CoreConfig.highPerformanceConfig())
 *     .build();
 * </pre>
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class Query4jConfigurationFactory {
    
    private static volatile Query4jConfig defaultConfig;
    private static final ConcurrentMap<String, Query4jConfig> configCache = new ConcurrentHashMap<>();
    private static final Object INIT_LOCK = new Object();
    
    // Prevent instantiation
    private Query4jConfigurationFactory() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Gets the default configuration instance with auto-loading.
     * 
     * <p>
     * This method loads configuration from all available sources using the standard
     * priority order. The configuration is cached and subsequent calls return the
     * same instance for performance.
     * </p>
     * 
     * @return the default configuration instance, never null
     * @throws DynamicQueryException if configuration loading fails
     */
    public static Query4jConfig getDefault() {
        if (defaultConfig == null) {
            synchronized (INIT_LOCK) {
                if (defaultConfig == null) {
                    defaultConfig = new ConfigurationLoader().load().build();
                }
            }
        }
        return defaultConfig;
    }
    
    /**
     * Sets the default configuration instance.
     * 
     * <p>
     * This method allows overriding the auto-loaded configuration with a
     * programmatically created instance. Useful for testing or when external
     * configuration sources are not available.
     * </p>
     * 
     * @param config the configuration to use as default, must not be null
     * @throws IllegalArgumentException if config is null
     */
    public static void setDefault(Query4jConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration must not be null");
        }
        synchronized (INIT_LOCK) {
            config.validate(); // Validate before setting
            defaultConfig = config;
        }
    }
    
    /**
     * Resets the default configuration, forcing reload on next access.
     * 
     * <p>
     * This method clears the cached default configuration. The next call to
     * {@link #getDefault()} will reload from external sources.
     * </p>
     */
    public static void resetDefault() {
        synchronized (INIT_LOCK) {
            defaultConfig = null;
            configCache.clear();
        }
    }
    
    /**
     * Loads configuration from a specific file and caches it.
     * 
     * @param configFile path to configuration file
     * @return loaded configuration instance
     * @throws IllegalArgumentException if configFile is null or empty
     * @throws DynamicQueryException if file loading fails
     */
    public static Query4jConfig loadFromFile(String configFile) {
        if (configFile == null) {
            throw new IllegalArgumentException("Configuration file path must not be null");
        }
        if (configFile.trim().isEmpty()) {
            throw new IllegalArgumentException("Configuration file path must not be empty");
        }
        return configCache.computeIfAbsent(configFile, file -> {
            return new ConfigurationLoader()
                    .loadFromFile(file)
                    .build();
        });
    }
    
    /**
     * Creates a configuration builder for programmatic configuration.
     * 
     * <p>
     * This method returns a builder that can be used to create custom
     * configuration instances without loading from external sources.
     * </p>
     * 
     * @return a new configuration builder
     * @hidden
     */
    @SuppressWarnings("javadoc")
    public static Query4jConfig.Query4jConfigBuilder builder() {
        return Query4jConfig.builder();
    }
    
    /**
     * Creates a configuration builder initialized with default values.
     * 
     * <p>
     * This method returns a builder pre-populated with the current default
     * configuration. Useful for creating variations of the default config.
     * </p>
     * 
     * @return a builder with default configuration values
     * @hidden
     */
    @SuppressWarnings("javadoc")
    public static Query4jConfig.Query4jConfigBuilder builderWithDefaults() {
        return getDefault().toBuilder();
    }
    
    /**
     * Creates a new ConfigurationLoader instance.
     * 
     * <p>
     * This method provides direct access to the configuration loading mechanism
     * for advanced use cases that require custom loading behavior.
     * </p>
     * 
     * @return a new ConfigurationLoader instance
     */
    public static ConfigurationLoader loader() {
        return new ConfigurationLoader();
    }
    
    /**
     * Validates a configuration instance and throws if invalid.
     * 
     * @param config the configuration to validate
     * @throws IllegalArgumentException if config is null
     * @throws DynamicQueryException if configuration is invalid
     */
    public static void validate(Query4jConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration must not be null");
        }
        config.validate();
    }
    
    /**
     * Creates a configuration instance from properties programmatically.
     * 
     * <p>
     * This method allows creating configuration from a map of string properties,
     * similar to how external configuration files are processed.
     * </p>
     * 
     * @param properties configuration properties map
     * @return configuration instance created from properties
     * @throws IllegalArgumentException if properties map is null
     * @throws DynamicQueryException if properties are invalid
     */
    public static Query4jConfig fromProperties(java.util.Map<String, String> properties) {
        if (properties == null) {
            throw new IllegalArgumentException("Properties map must not be null");
        }
        
        ConfigurationLoader loader = new ConfigurationLoader();
        
        // Manually populate the loader's properties
        properties.forEach((key, value) -> {
            if (key.startsWith("query4j.")) {
                loader.getClass(); // Access to package-private methods would require reflection
                // For now, we'll use the simpler approach of system properties
                System.setProperty(key, value);
            }
        });
        
        try {
            return loader.load().build();
        } finally {
            // Clean up system properties
            properties.keySet().forEach(key -> {
                if (key.startsWith("query4j.")) {
                    System.clearProperty(key);
                }
            });
        }
    }
}