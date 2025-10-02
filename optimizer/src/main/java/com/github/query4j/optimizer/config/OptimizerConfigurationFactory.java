package com.github.query4j.optimizer.config;

import com.github.query4j.core.DynamicQueryException;
import com.github.query4j.optimizer.OptimizerConfig;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Factory for creating and managing optimizer configuration instances.
 * 
 * <p>
 * This factory provides singleton access to optimizer configuration instances and supports
 * both auto-loading from external sources and programmatic configuration.
 * It ensures thread-safe initialization and provides caching for performance.
 * </p>
 * 
 * <p>
 * Usage examples:
 * <pre>
 * // Get default auto-configured instance
 * OptimizerConfig config = OptimizerConfigurationFactory.getDefault();
 * 
 * // Load from specific file
 * OptimizerConfig config = OptimizerConfigurationFactory.loadFromFile("myconfig.properties");
 * 
 * // Programmatic configuration
 * OptimizerConfig config = OptimizerConfigurationFactory.builder()
 *     .indexSuggestionsEnabled(true)
 *     .verboseOutput(true)
 *     .build();
 * </pre>
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class OptimizerConfigurationFactory {
    
    private static volatile OptimizerConfig defaultConfig;
    private static final ConcurrentMap<String, OptimizerConfig> configCache = new ConcurrentHashMap<>();
    private static final Object INIT_LOCK = new Object();
    
    // Prevent instantiation
    private OptimizerConfigurationFactory() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Gets the default optimizer configuration instance with auto-loading.
     * 
     * <p>
     * This method loads configuration from all available sources using the standard
     * priority order. The configuration is cached and subsequent calls return the
     * same instance for performance.
     * </p>
     * 
     * @return the default optimizer configuration instance, never null
     * @throws DynamicQueryException if configuration loading fails
     */
    public static OptimizerConfig getDefault() {
        if (defaultConfig == null) {
            synchronized (INIT_LOCK) {
                if (defaultConfig == null) {
                    defaultConfig = new OptimizerConfigurationLoader().load().build();
                }
            }
        }
        return defaultConfig;
    }
    
    /**
     * Sets the default optimizer configuration instance.
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
    public static void setDefault(OptimizerConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration must not be null");
        }
        synchronized (INIT_LOCK) {
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
     * Loads optimizer configuration from a specific file and caches it.
     * 
     * @param configFile path to configuration file
     * @return loaded optimizer configuration instance
     * @throws DynamicQueryException if file loading fails
     */
    public static OptimizerConfig loadFromFile(String configFile) {
        return configCache.computeIfAbsent(configFile, file -> {
            return new OptimizerConfigurationLoader()
                    .loadFromFile(file)
                    .build();
        });
    }
    
    /**
     * Creates an optimizer configuration builder for programmatic configuration.
     * 
     * <p>
     * This method returns a builder that can be used to create custom
     * configuration instances without loading from external sources.
     * </p>
     * 
     * @return a new optimizer configuration builder
     * @hidden
     */
    @SuppressWarnings("javadoc")
    public static OptimizerConfig.OptimizerConfigBuilder builder() {
        return OptimizerConfig.builder();
    }
    
    /**
     * Creates an optimizer configuration builder initialized with default values.
     * 
     * <p>
     * This method returns a builder pre-populated with the current default
     * configuration. Useful for creating variations of the default config.
     * </p>
     * 
     * @return a builder with default optimizer configuration values
     * @hidden
     */
    @SuppressWarnings("javadoc")
    public static OptimizerConfig.OptimizerConfigBuilder builderWithDefaults() {
        OptimizerConfig defaultConfig = getDefault();
        return OptimizerConfig.builder()
                .indexSuggestionsEnabled(defaultConfig.isIndexSuggestionsEnabled())
                .predicatePushdownEnabled(defaultConfig.isPredicatePushdownEnabled())
                .joinReorderingEnabled(defaultConfig.isJoinReorderingEnabled())
                .indexSelectivityThreshold(defaultConfig.getIndexSelectivityThreshold())
                .predicateReorderingThreshold(defaultConfig.getPredicateReorderingThreshold())
                .joinReorderingThreshold(defaultConfig.getJoinReorderingThreshold())
                .maxAnalysisTimeMs(defaultConfig.getMaxAnalysisTimeMs())
                .verboseOutput(defaultConfig.isVerboseOutput())
                .maxCompositeIndexColumns(defaultConfig.getMaxCompositeIndexColumns())
                .targetDatabase(defaultConfig.getTargetDatabase());
    }
    
    /**
     * Creates a new OptimizerConfigurationLoader instance.
     * 
     * <p>
     * This method provides direct access to the configuration loading mechanism
     * for advanced use cases that require custom loading behavior.
     * </p>
     * 
     * @return a new OptimizerConfigurationLoader instance
     */
    public static OptimizerConfigurationLoader loader() {
        return new OptimizerConfigurationLoader();
    }
    
    /**
     * Creates an optimizer configuration instance from properties programmatically.
     * 
     * <p>
     * This method allows creating configuration from a map of string properties,
     * similar to how external configuration files are processed.
     * </p>
     * 
     * @param properties configuration properties map
     * @return optimizer configuration instance created from properties
     * @throws DynamicQueryException if properties are invalid
     */
    public static OptimizerConfig fromProperties(java.util.Map<String, String> properties) {
        OptimizerConfigurationLoader loader = new OptimizerConfigurationLoader();
        
        // Manually populate system properties with optimizer-specific values
        properties.forEach((key, value) -> {
            if (key.startsWith("query4j.optimizer.")) {
                System.setProperty(key, value);
            }
        });
        
        try {
            return loader.load().build();
        } finally {
            // Clean up system properties
            properties.keySet().forEach(key -> {
                if (key.startsWith("query4j.optimizer.")) {
                    System.clearProperty(key);
                }
            });
        }
    }
}