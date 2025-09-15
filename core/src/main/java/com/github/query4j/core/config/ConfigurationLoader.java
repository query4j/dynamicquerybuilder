package com.github.query4j.core.config;

import com.github.query4j.core.DynamicQueryException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Loads Query4j configuration from various sources with priority-based overrides.
 * 
 * <p>
 * Supports loading configuration for core and cache modules from:
 * <ul>
 * <li>Properties files (.properties)</li>
 * <li>YAML files (.yml, .yaml)</li>
 * <li>System properties</li>
 * <li>Environment variables</li>
 * </ul>
 * 
 * <p>
 * Configuration priority (highest to lowest):
 * <ol>
 * <li>Environment variables</li>
 * <li>System properties</li>
 * <li>YAML files</li>
 * <li>Properties files</li>
 * <li>Default values</li>
 * </ol>
 * 
 * <p>
 * Note: Optimizer configuration is handled separately to avoid circular dependencies.
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class ConfigurationLoader {
    
    private static final String CONFIG_PREFIX = "query4j";
    private static final String CORE_PREFIX = CONFIG_PREFIX + ".core";
    private static final String CACHE_PREFIX = CONFIG_PREFIX + ".cache";
    
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
     * Creates a new configuration loader instance.
     */
    public ConfigurationLoader() {
        // Empty constructor
    }
    
    /**
     * Loads configuration from all available sources.
     * This method is idempotent - calling it multiple times has no additional effect.
     * 
     * @return this loader for method chaining
     * @throws DynamicQueryException if configuration loading fails
     */
    public ConfigurationLoader load() {
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
            throw new DynamicQueryException("Failed to load configuration", e);
        }
    }
    
    /**
     * Loads configuration from a specific file.
     * 
     * @param configFile path to configuration file
     * @return this loader for method chaining
     * @throws IllegalArgumentException if configFile is null or empty
     * @throws DynamicQueryException if file loading fails
     */
    public ConfigurationLoader loadFromFile(String configFile) {
        if (configFile == null) {
            throw new IllegalArgumentException("Configuration file path must not be null");
        }
        if (configFile.trim().isEmpty()) {
            throw new IllegalArgumentException("Configuration file path must not be empty");
        }
        return loadFromFile(Paths.get(configFile));
    }
    
    /**
     * Loads configuration from a specific file path.
     * 
     * @param configPath path to configuration file
     * @return this loader for method chaining
     * @throws DynamicQueryException if file loading fails
     */
    public ConfigurationLoader loadFromFile(Path configPath) {
        if (!Files.exists(configPath)) {
            throw new DynamicQueryException("Configuration file not found: " + configPath);
        }
        
        if (Files.isDirectory(configPath)) {
            throw new DynamicQueryException("Expected configuration file but found directory: " + configPath);
        }
        
        try {
            if (configPath.toString().endsWith(".properties")) {
                loadPropertiesFile(configPath);
            } else if (configPath.toString().matches(".*\\.(yml|yaml)$")) {
                loadYamlFile(configPath);
            } else {
                throw new DynamicQueryException("Unsupported configuration file format: " + configPath);
            }
            // Mark as loaded to prevent auto-loading of default files
            loaded = true;
            return this;
        } catch (IOException e) {
            throw new DynamicQueryException("Failed to load configuration file: " + configPath, e);
        }
    }
    
    /**
     * Builds a Query4jConfig instance from loaded properties.
     * 
     * @return configured Query4jConfig instance
     * @throws DynamicQueryException if configuration is invalid
     */
    public Query4jConfig build() {
        if (!loaded) {
            load(); // Auto-load if not already loaded
        }
        
        try {
            Query4jConfig config = Query4jConfig.builder()
                    .core(buildCoreConfig())
                    .cache(buildCacheConfig())
                    .build();
            
            config.validate();
            return config;
        } catch (Exception e) {
            throw new DynamicQueryException("Failed to build configuration", e);
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
                if (keyStr.startsWith(CONFIG_PREFIX)) {
                    configProperties.put(keyStr, value.toString());
                }
            });
        }
    }
    
    private void loadYamlFile(Path filePath) throws IOException {
        // Basic YAML parsing for simple key-value pairs with nested sections
        // For production use, consider using a proper YAML library like SnakeYAML
        List<String> lines = Files.readAllLines(filePath);
        Stack<String> sectionStack = new Stack<>();
        
        for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
            String originalLine = lines.get(lineNumber);
            String line = originalLine.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            
            // Basic YAML syntax validation
            if (line.contains("[") && !line.contains("]")) {
                throw new IOException("Invalid YAML syntax at line " + (lineNumber + 1) + ": unclosed bracket");
            }
            if (!line.contains(":") && !line.isEmpty()) {
                // Non-empty lines should either be section headers or key-value pairs
                throw new IOException("Invalid YAML syntax at line " + (lineNumber + 1) + ": missing colon in '" + line + "'");
            }
            
            // Count indentation to determine nesting level
            int indent = getIndentLevel(originalLine);
            
            if (line.endsWith(":") && !line.contains(" ")) {
                // Section header
                String section = line.substring(0, line.length() - 1);
                
                // Adjust stack based on indentation
                while (!sectionStack.isEmpty() && indent <= getStackIndentLevel(sectionStack.size())) {
                    sectionStack.pop();
                }
                sectionStack.push(section);
                
            } else if (line.contains(":")) {
                // Key-value pair
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    // Remove quotes if present
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    
                    // Build full key from section stack
                    StringBuilder fullKey = new StringBuilder();
                    for (int i = 0; i < sectionStack.size(); i++) {
                        if (fullKey.length() > 0) fullKey.append(".");
                        fullKey.append(sectionStack.get(i));
                    }
                    if (fullKey.length() > 0) fullKey.append(".");
                    fullKey.append(key);
                    
                    String fullKeyStr = fullKey.toString();
                    if (fullKeyStr.startsWith(CONFIG_PREFIX)) {
                        configProperties.put(fullKeyStr, value);
                    }
                }
            }
        }
    }
    
    private int getIndentLevel(String line) {
        int spaces = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ') spaces++;
            else break;
        }
        return spaces;
    }
    
    private int getStackIndentLevel(int stackSize) {
        // Assume 2 spaces per level (typical YAML)
        return (stackSize - 1) * 2;
    }
    
    private void loadSystemProperties() {
        System.getProperties().forEach((key, value) -> {
            String keyStr = key.toString();
            if (keyStr.startsWith(CONFIG_PREFIX)) {
                configProperties.put(keyStr, value.toString());
            }
        });
    }
    
    private void loadEnvironmentVariables() {
        System.getenv().forEach((key, value) -> {
            if (key.startsWith("QUERY4J_")) {
                // Convert QUERY4J_CORE_MAX_DEPTH to query4j.core.maxDepth
                String normalizedKey = key.toLowerCase()
                        .replace("query4j_", "query4j.")
                        .replace("_", ".");
                configProperties.put(normalizedKey, value);
            }
        });
    }
    
    private CoreConfig buildCoreConfig() {
        return CoreConfig.builder()
                .defaultQueryTimeoutMs(getLongProperty(CORE_PREFIX + ".defaultQueryTimeoutMs", 30_000L))
                .maxPredicateDepth(getIntProperty(CORE_PREFIX + ".maxPredicateDepth", 10))
                .maxPredicateCount(getIntProperty(CORE_PREFIX + ".maxPredicateCount", 50))
                .likePredacatesEnabled(getBooleanProperty(CORE_PREFIX + ".likePredicatesEnabled", true))
                .inPredicatesEnabled(getBooleanProperty(CORE_PREFIX + ".inPredicatesEnabled", true))
                .betweenPredicatesEnabled(getBooleanProperty(CORE_PREFIX + ".betweenPredicatesEnabled", true))
                .nullPredicatesEnabled(getBooleanProperty(CORE_PREFIX + ".nullPredicatesEnabled", true))
                .maxInPredicateSize(getIntProperty(CORE_PREFIX + ".maxInPredicateSize", 1000))
                .strictFieldValidation(getBooleanProperty(CORE_PREFIX + ".strictFieldValidation", true))
                .parameterCollisionDetection(getBooleanProperty(CORE_PREFIX + ".parameterCollisionDetection", true))
                .defaultPageSize(getIntProperty(CORE_PREFIX + ".defaultPageSize", 20))
                .maxPageSize(getIntProperty(CORE_PREFIX + ".maxPageSize", 1000))
                .queryStatisticsEnabled(getBooleanProperty(CORE_PREFIX + ".queryStatisticsEnabled", true))
                .build();
    }
    
    private CacheConfig buildCacheConfig() {
        return CacheConfig.builder()
                .enabled(getBooleanProperty(CACHE_PREFIX + ".enabled", true))
                .defaultTtlSeconds(getLongProperty(CACHE_PREFIX + ".defaultTtlSeconds", 3600L))
                .maxSize(getLongProperty(CACHE_PREFIX + ".maxSize", 10_000L))
                .defaultRegion(getProperty(CACHE_PREFIX + ".defaultRegion", "default"))
                .statisticsEnabled(getBooleanProperty(CACHE_PREFIX + ".statisticsEnabled", true))
                .maintenanceIntervalSeconds(getLongProperty(CACHE_PREFIX + ".maintenanceIntervalSeconds", 300L))
                .keyValidationEnabled(getBooleanProperty(CACHE_PREFIX + ".keyValidationEnabled", true))
                .maxKeyLength(getIntProperty(CACHE_PREFIX + ".maxKeyLength", 512))
                .concurrencyLevel(getIntProperty(CACHE_PREFIX + ".concurrencyLevel", 16))
                .autoWarmupEnabled(getBooleanProperty(CACHE_PREFIX + ".autoWarmupEnabled", false))
                .warmupSize(getIntProperty(CACHE_PREFIX + ".warmupSize", 100))
                .build();
    }
    
    private boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = configProperties.get(key);
        if (value == null) {
            return defaultValue;
        }
        // Handle common boolean values, fall back to default for invalid values
        String lowerValue = value.toLowerCase().trim();
        if ("true".equals(lowerValue) || "yes".equals(lowerValue) || "1".equals(lowerValue)) {
            return true;
        } else if ("false".equals(lowerValue) || "no".equals(lowerValue) || "0".equals(lowerValue)) {
            return false;
        } else {
            // Fall back to default for invalid boolean values like "maybe"
            return defaultValue;
        }
    }
    
    private int getIntProperty(String key, int defaultValue) {
        String value = configProperties.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            int parsedValue = Integer.parseInt(value);
            // Normalize negative values for certain properties to positive defaults
            if (parsedValue <= 0 && (key.contains("maxPredicateDepth") || key.contains("maxPredicateCount") || 
                                    key.contains("maxInPredicateSize") || key.contains("defaultPageSize") || 
                                    key.contains("maxPageSize") || key.contains("maxKeyLength") || 
                                    key.contains("concurrencyLevel"))) {
                return defaultValue; // Use safe default for invalid values
            }
            return parsedValue;
        } catch (NumberFormatException e) {
            // Fall back to default value for invalid numbers rather than throwing exception
            return defaultValue;
        }
    }
    
    private long getLongProperty(String key, long defaultValue) {
        String value = configProperties.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            long parsedValue = Long.parseLong(value);
            // Normalize negative values for certain properties
            if (parsedValue < 0 && (key.contains("defaultQueryTimeoutMs") || key.contains("defaultTtlSeconds") ||
                                   key.contains("maxSize") || key.contains("maintenanceIntervalSeconds"))) {
                return defaultValue; // Use safe default for negative values  
            }
            return parsedValue;
        } catch (NumberFormatException e) {
            // Fall back to default value for invalid numbers rather than throwing exception
            return defaultValue;
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
}