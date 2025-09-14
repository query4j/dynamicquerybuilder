package com.github.query4j.optimizer.config;

import com.github.query4j.core.DynamicQueryException;
import com.github.query4j.core.config.ConfigurationLoader;
import com.github.query4j.optimizer.OptimizerConfig;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Configuration loader specifically for the optimizer module.
 * Extends the base configuration loading capabilities to handle optimizer-specific settings.
 * 
 * <p>
 * This loader can be used independently or in combination with the core configuration loader
 * to provide complete Query4j configuration management.
 * </p>
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class OptimizerConfigurationLoader {
    
    private static final String OPTIMIZER_PREFIX = "query4j.optimizer";
    
    // Common configuration file names to search for
    private static final List<String> DEFAULT_CONFIG_FILES = Arrays.asList(
        "query4j.properties",
        "query4j.yml", 
        "query4j.yaml",
        "application.properties",
        "application.yml",
        "application.yaml"
    );
    
    private static final List<String> DEFAULT_CONFIG_PATHS = Arrays.asList(
        ".",
        "config",
        "src/main/resources",
        "src/test/resources",
        System.getProperty("user.home") + "/.query4j",
        "/etc/query4j"
    );
    
    private final Map<String, String> configProperties = new HashMap<>();
    private boolean loaded = false;
    
    /**
     * Creates a new optimizer configuration loader instance.
     */
    public OptimizerConfigurationLoader() {
        // Empty constructor
    }
    
    /**
     * Loads optimizer configuration from all available sources.
     * This method is idempotent - calling it multiple times has no additional effect.
     * 
     * @return this loader for method chaining
     * @throws DynamicQueryException if configuration loading fails
     */
    public OptimizerConfigurationLoader load() {
        if (loaded) {
            return this;
        }
        
        try {
            // Load in priority order (lowest to highest)
            loadDefaultConfigFiles();
            loadSystemProperties();
            loadEnvironmentVariables();
            
            loaded = true;
            return this;
        } catch (Exception e) {
            throw new DynamicQueryException("Failed to load optimizer configuration", e);
        }
    }
    
    /**
     * Loads configuration from a specific file.
     * 
     * @param configFile path to configuration file
     * @return this loader for method chaining
     * @throws DynamicQueryException if file loading fails
     */
    public OptimizerConfigurationLoader loadFromFile(String configFile) {
        return loadFromFile(Paths.get(configFile));
    }
    
    /**
     * Loads configuration from a specific file path.
     * 
     * @param configPath path to configuration file
     * @return this loader for method chaining
     * @throws DynamicQueryException if file loading fails
     */
    public OptimizerConfigurationLoader loadFromFile(Path configPath) {
        if (!Files.exists(configPath)) {
            throw new DynamicQueryException("Configuration file not found: " + configPath);
        }
        
        try {
            if (configPath.toString().endsWith(".properties")) {
                loadPropertiesFile(configPath);
            } else if (configPath.toString().matches(".*\\.(yml|yaml)$")) {
                loadYamlFile(configPath);
            } else {
                throw new DynamicQueryException("Unsupported configuration file format: " + configPath);
            }
            return this;
        } catch (IOException e) {
            throw new DynamicQueryException("Failed to load configuration file: " + configPath, e);
        }
    }
    
    /**
     * Builds an OptimizerConfig instance from loaded properties.
     * 
     * @return configured OptimizerConfig instance
     * @throws DynamicQueryException if configuration is invalid
     */
    public OptimizerConfig build() {
        if (!loaded) {
            load(); // Auto-load if not already loaded
        }
        
        try {
            return buildOptimizerConfig();
        } catch (Exception e) {
            throw new DynamicQueryException("Failed to build optimizer configuration", e);
        }
    }
    
    /**
     * Gets a configuration property value by key.
     * 
     * @param key property key
     * @return property value, or null if not found
     */
    public String getProperty(String key) {
        return configProperties.get(key);
    }
    
    /**
     * Gets a configuration property value with default.
     * 
     * @param key property key
     * @param defaultValue default value if property not found
     * @return property value or default
     */
    public String getProperty(String key, String defaultValue) {
        return configProperties.getOrDefault(key, defaultValue);
    }
    
    private void loadDefaultConfigFiles() {
        for (String configPath : DEFAULT_CONFIG_PATHS) {
            for (String configFile : DEFAULT_CONFIG_FILES) {
                Path filePath = Paths.get(configPath, configFile);
                if (Files.exists(filePath)) {
                    try {
                        loadFromFile(filePath);
                    } catch (DynamicQueryException e) {
                        // Log warning but continue with other files
                        System.err.println("Warning: Failed to load config file " + filePath + ": " + e.getMessage());
                    }
                }
            }
        }
    }
    
    private void loadPropertiesFile(Path filePath) throws IOException {
        Properties props = new Properties();
        try (InputStream input = Files.newInputStream(filePath)) {
            props.load(input);
            props.forEach((key, value) -> {
                String keyStr = key.toString();
                if (keyStr.startsWith("query4j.optimizer") || keyStr.startsWith("query4j.") && keyStr.contains("optimizer")) {
                    configProperties.put(keyStr, value.toString());
                }
            });
        }
    }
    
    private void loadYamlFile(Path filePath) throws IOException {
        // Basic YAML parsing for simple key-value pairs
        List<String> lines = Files.readAllLines(filePath);
        String currentSection = "";
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            
            if (line.endsWith(":") && !line.contains(" ")) {
                // Section header
                currentSection = line.substring(0, line.length() - 1);
            } else if (line.contains(":")) {
                // Key-value pair
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    
                    String fullKey;
                    if (currentSection.isEmpty()) {
                        fullKey = key;
                    } else {
                        fullKey = currentSection + "." + key;
                    }
                    
                    if (fullKey.contains("optimizer") || fullKey.startsWith("query4j.optimizer")) {
                        configProperties.put(fullKey, value);
                    }
                }
            }
        }
    }
    
    private void loadSystemProperties() {
        System.getProperties().forEach((key, value) -> {
            String keyStr = key.toString();
            if (keyStr.startsWith(OPTIMIZER_PREFIX)) {
                configProperties.put(keyStr, value.toString());
            }
        });
    }
    
    private void loadEnvironmentVariables() {
        System.getenv().forEach((key, value) -> {
            if (key.startsWith("QUERY4J_OPTIMIZER_")) {
                // Convert QUERY4J_OPTIMIZER_INDEX_SUGGESTIONS to query4j.optimizer.indexSuggestions
                String normalizedKey = key.toLowerCase()
                        .replace("query4j_optimizer_", "query4j.optimizer.")
                        .replace("_", ".");
                configProperties.put(normalizedKey, value);
            }
        });
    }
    
    private OptimizerConfig buildOptimizerConfig() {
        return OptimizerConfig.builder()
                .indexSuggestionsEnabled(getBooleanProperty(OPTIMIZER_PREFIX + ".indexSuggestionsEnabled", true))
                .predicatePushdownEnabled(getBooleanProperty(OPTIMIZER_PREFIX + ".predicatePushdownEnabled", true))
                .joinReorderingEnabled(getBooleanProperty(OPTIMIZER_PREFIX + ".joinReorderingEnabled", true))
                .indexSelectivityThreshold(getDoubleProperty(OPTIMIZER_PREFIX + ".indexSelectivityThreshold", 0.1))
                .predicateReorderingThreshold(getDoubleProperty(OPTIMIZER_PREFIX + ".predicateReorderingThreshold", 0.05))
                .joinReorderingThreshold(getDoubleProperty(OPTIMIZER_PREFIX + ".joinReorderingThreshold", 0.1))
                .maxAnalysisTimeMs(getLongProperty(OPTIMIZER_PREFIX + ".maxAnalysisTimeMs", 5000L))
                .verboseOutput(getBooleanProperty(OPTIMIZER_PREFIX + ".verboseOutput", false))
                .maxCompositeIndexColumns(getIntProperty(OPTIMIZER_PREFIX + ".maxCompositeIndexColumns", 3))
                .targetDatabase(getEnumProperty(OPTIMIZER_PREFIX + ".targetDatabase", 
                        OptimizerConfig.DatabaseType.class, OptimizerConfig.DatabaseType.GENERIC))
                .build();
    }
    
    private boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = configProperties.get(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }
    
    private int getIntProperty(String key, int defaultValue) {
        String value = configProperties.get(key);
        try {
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            throw new DynamicQueryException("Invalid integer value for property " + key + ": " + value);
        }
    }
    
    private long getLongProperty(String key, long defaultValue) {
        String value = configProperties.get(key);
        try {
            return value != null ? Long.parseLong(value) : defaultValue;
        } catch (NumberFormatException e) {
            throw new DynamicQueryException("Invalid long value for property " + key + ": " + value);
        }
    }
    
    private double getDoubleProperty(String key, double defaultValue) {
        String value = configProperties.get(key);
        try {
            return value != null ? Double.parseDouble(value) : defaultValue;
        } catch (NumberFormatException e) {
            throw new DynamicQueryException("Invalid double value for property " + key + ": " + value);
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T extends Enum<T>> T getEnumProperty(String key, Class<T> enumClass, T defaultValue) {
        String value = configProperties.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DynamicQueryException("Invalid enum value for property " + key + ": " + value + 
                    ". Valid values: " + Arrays.toString(enumClass.getEnumConstants()));
        }
    }
}