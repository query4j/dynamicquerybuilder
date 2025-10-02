package com.github.query4j.core.config;

import com.github.query4j.core.DynamicQueryException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConfigurationLoader class.
 */
class ConfigurationLoaderTest {

    @TempDir
    Path tempDir;
    
    private Map<String, String> originalSystemProperties;
    private Map<String, String> originalEnvironmentVariables;
    
    @BeforeEach
    void setUp() {
        // Save original system properties
        originalSystemProperties = new HashMap<>();
        System.getProperties().forEach((key, value) -> {
            String keyStr = key.toString();
            if (keyStr.startsWith("query4j.")) {
                originalSystemProperties.put(keyStr, value.toString());
            }
        });
    }
    
    @AfterEach
    void tearDown() {
        // Restore original system properties
        System.getProperties().forEach((key, value) -> {
            String keyStr = key.toString();
            if (keyStr.startsWith("query4j.")) {
                System.clearProperty(keyStr);
            }
        });
        originalSystemProperties.forEach(System::setProperty);
    }
    
    @Test
    void load_shouldCreateDefaultConfiguration() {
        ConfigurationLoader loader = new ConfigurationLoader();
        Query4jConfig config = loader.load().build();
        
        assertNotNull(config);
        assertNotNull(config.getCore());
        assertNotNull(config.getCache());
    }
    
    @Test
    void loadFromPropertiesFile_shouldParseCorrectly() throws IOException {
        // Create test properties file
        Path configFile = tempDir.resolve("test.properties");
        String propertiesContent = 
            "query4j.core.defaultQueryTimeoutMs=45000\n" +
            "query4j.core.maxPredicateDepth=15\n" +
            "query4j.cache.enabled=false\n" +
            "query4j.cache.maxSize=5000\n";
        Files.write(configFile, propertiesContent.getBytes());
        
        ConfigurationLoader loader = new ConfigurationLoader();
        Query4jConfig config = loader.loadFromFile(configFile).build();
        
        // Debug: print all loaded properties
        System.out.println("Loaded properties:");
        System.out.println("core timeout: " + config.getCore().getDefaultQueryTimeoutMs());
        System.out.println("core depth: " + config.getCore().getMaxPredicateDepth());
        System.out.println("cache enabled: " + config.getCache().isEnabled());
        System.out.println("cache size: " + config.getCache().getMaxSize());
        
        assertEquals(45_000L, config.getCore().getDefaultQueryTimeoutMs());
        assertEquals(15, config.getCore().getMaxPredicateDepth());
        assertFalse(config.getCache().isEnabled());
        assertEquals(5000L, config.getCache().getMaxSize());
    }
    
    @Test
    void loadFromYamlFile_shouldParseCorrectly() throws IOException {
        // Create test YAML file
        Path configFile = tempDir.resolve("test.yml");
        String yamlContent = 
            "query4j:\n" +
            "  core:\n" +
            "    defaultQueryTimeoutMs: 25000\n" +
            "    maxPredicateDepth: 8\n" +
            "  cache:\n" +
            "    enabled: true\n" +
            "    maxSize: 8000\n";
        Files.write(configFile, yamlContent.getBytes());
        
        ConfigurationLoader loader = new ConfigurationLoader();
        Query4jConfig config = loader.loadFromFile(configFile).build();
        
        assertEquals(25_000L, config.getCore().getDefaultQueryTimeoutMs());
        assertEquals(8, config.getCore().getMaxPredicateDepth());
        assertTrue(config.getCache().isEnabled());
        assertEquals(8000L, config.getCache().getMaxSize());
    }
    
    @Test
    void systemProperties_shouldOverrideDefaults() {
        System.setProperty("query4j.core.maxPredicateDepth", "25");
        System.setProperty("query4j.cache.defaultTtlSeconds", "7200");
        
        // Create new loader to pick up system properties
        ConfigurationLoader loader = new ConfigurationLoader();
        Query4jConfig config = loader.load().build();
        
        assertEquals(25, config.getCore().getMaxPredicateDepth());
        assertEquals(7200L, config.getCache().getDefaultTtlSeconds());
    }
    
    @Test
    void getProperty_shouldReturnCorrectValues() {
        // Set system property first
        System.setProperty("query4j.test.property", "testValue");
        
        // Create new loader to pick up the system property
        ConfigurationLoader loader = new ConfigurationLoader();
        loader.load(); 
        
        assertEquals("testValue", loader.getProperty("query4j.test.property"));
        assertNull(loader.getProperty("nonexistent.property"));
        assertEquals("defaultValue", loader.getProperty("nonexistent.property", "defaultValue"));
    }
    
    @Test
    void loadFromFile_shouldThrowExceptionForNonexistentFile() {
        ConfigurationLoader loader = new ConfigurationLoader();
        Path nonexistentFile = tempDir.resolve("nonexistent.properties");
        
        DynamicQueryException exception = assertThrows(DynamicQueryException.class, 
            () -> loader.loadFromFile(nonexistentFile));
        assertTrue(exception.getMessage().contains("Configuration file not found"));
    }
    
    @Test
    void loadFromFile_shouldThrowExceptionForUnsupportedFormat() throws IOException {
        Path unsupportedFile = tempDir.resolve("config.xml");
        Files.write(unsupportedFile, "<config></config>".getBytes());
        
        ConfigurationLoader loader = new ConfigurationLoader();
        DynamicQueryException exception = assertThrows(DynamicQueryException.class, 
            () -> loader.loadFromFile(unsupportedFile));
        assertTrue(exception.getMessage().contains("Unsupported configuration file format"));
    }
    
    @Test
    void build_shouldValidateConfiguration() throws IOException {
        // Create invalid configuration
        Path configFile = tempDir.resolve("invalid.properties");
        String propertiesContent = 
            "query4j.core.maxPredicateDepth=-1\n" +
            "query4j.cache.maxSize=0\n";
        Files.write(configFile, propertiesContent.getBytes());
        
        ConfigurationLoader loader = new ConfigurationLoader();
        assertThrows(DynamicQueryException.class, 
            () -> loader.loadFromFile(configFile).build());
    }
    
    @Test
    void multipleLoads_shouldBeIdempotent() {
        ConfigurationLoader loader = new ConfigurationLoader();
        Query4jConfig config1 = loader.load().build();
        Query4jConfig config2 = loader.load().build();
        
        assertEquals(config1.getCore().getDefaultQueryTimeoutMs(), config2.getCore().getDefaultQueryTimeoutMs());
        assertEquals(config1.getCache().getMaxSize(), config2.getCache().getMaxSize());
    }
}