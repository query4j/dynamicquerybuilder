package com.github.query4j.core.config;

import com.github.query4j.core.DynamicQueryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for Query4jConfigurationFactory.
 * Ensures 95%+ coverage by testing all public methods and edge cases.
 */
public class Query4jConfigurationFactoryTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // Reset factory state before each test
        Query4jConfigurationFactory.resetDefault();
    }

    @Test
    void testConstructorThrowsException() {
        // Test that constructor is private and throws exception
        assertThrows(InvocationTargetException.class, () -> {
            java.lang.reflect.Constructor<Query4jConfigurationFactory> constructor = 
                Query4jConfigurationFactory.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });
    }

    @Test
    void testGetDefaultInitializedOnFirstCall() {
        Query4jConfig config = Query4jConfigurationFactory.getDefault();
        assertNotNull(config);
        assertNotNull(config.getCore());
        assertNotNull(config.getCache());
        assertTrue(config.isAutoConfigurationEnabled());
    }

    @Test
    void testGetDefaultReturnsSameInstanceOnSubsequentCalls() {
        Query4jConfig config1 = Query4jConfigurationFactory.getDefault();
        Query4jConfig config2 = Query4jConfigurationFactory.getDefault();
        assertSame(config1, config2);
    }

    @Test
    void testSetDefaultOverridesConfiguration() {
        Query4jConfig customConfig = Query4jConfig.builder()
            .core(CoreConfig.builder()
                .defaultQueryTimeoutMs(5000L)
                .maxPredicateDepth(15)
                .build())
            .build();

        Query4jConfigurationFactory.setDefault(customConfig);
        Query4jConfig retrieved = Query4jConfigurationFactory.getDefault();
        
        assertSame(customConfig, retrieved);
        assertEquals(5000L, retrieved.getCore().getDefaultQueryTimeoutMs());
        assertEquals(15, retrieved.getCore().getMaxPredicateDepth());
    }

    @Test
    void testSetDefaultWithNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            Query4jConfigurationFactory.setDefault(null);
        });
    }

    @Test
    void testResetDefaultClearsConfiguration() {
        // Set custom config first
        Query4jConfig customConfig = Query4jConfig.builder()
            .core(CoreConfig.highPerformanceConfig())
            .build();
        Query4jConfigurationFactory.setDefault(customConfig);
        
        // Reset and verify new config is created
        Query4jConfigurationFactory.resetDefault();
        Query4jConfig newConfig = Query4jConfigurationFactory.getDefault();
        
        assertNotSame(customConfig, newConfig);
        // Should have default values
        assertEquals(30000L, newConfig.getCore().getDefaultQueryTimeoutMs());
    }

    @Test
    void testLoadFromFileWithValidPath() throws IOException {
        // Create test properties file
        Path configFile = tempDir.resolve("test.properties");
        Properties props = new Properties();
        props.setProperty("query4j.core.defaultQueryTimeoutMs", "15000");
        props.setProperty("query4j.core.maxPredicateDepth", "25");
        props.setProperty("query4j.cache.enabled", "false");
        
        try (var writer = Files.newBufferedWriter(configFile)) {
            props.store(writer, "Test config");
        }

        Query4jConfig config = Query4jConfigurationFactory.loadFromFile(configFile.toString());
        
        assertNotNull(config);
        assertEquals(15000L, config.getCore().getDefaultQueryTimeoutMs());
        assertEquals(25, config.getCore().getMaxPredicateDepth());
        assertFalse(config.getCache().isEnabled());
    }

    @Test
    void testLoadFromFileWithInvalidPath() {
        assertThrows(DynamicQueryException.class, () -> {
            Query4jConfigurationFactory.loadFromFile("/nonexistent/path/config.properties");
        });
    }

    @Test
    void testLoadFromFileWithNullPath() {
        assertThrows(IllegalArgumentException.class, () -> {
            Query4jConfigurationFactory.loadFromFile(null);
        });
    }

    @Test
    void testLoadFromFileWithEmptyPath() {
        assertThrows(IllegalArgumentException.class, () -> {
            Query4jConfigurationFactory.loadFromFile("");
        });
    }

    @Test
    void testBuilderReturnsNewInstance() {
        Query4jConfig.Query4jConfigBuilder builder1 = Query4jConfigurationFactory.builder();
        Query4jConfig.Query4jConfigBuilder builder2 = Query4jConfigurationFactory.builder();
        
        assertNotNull(builder1);
        assertNotNull(builder2);
        assertNotSame(builder1, builder2);
    }

    @Test
    void testBuilderWithDefaultsIncludesDefaultConfig() {
        Query4jConfig.Query4jConfigBuilder builder = Query4jConfigurationFactory.builderWithDefaults();
        Query4jConfig config = builder.build();
        
        assertNotNull(config);
        assertNotNull(config.getCore());
        assertNotNull(config.getCache());
        assertEquals(30000L, config.getCore().getDefaultQueryTimeoutMs());
        assertEquals(10, config.getCore().getMaxPredicateDepth());
    }

    @Test
    void testLoaderReturnsNewInstance() {
        ConfigurationLoader loader1 = Query4jConfigurationFactory.loader();
        ConfigurationLoader loader2 = Query4jConfigurationFactory.loader();
        
        assertNotNull(loader1);
        assertNotNull(loader2);
        assertNotSame(loader1, loader2);
    }

    @Test
    void testValidateWithValidConfig() {
        Query4jConfig validConfig = Query4jConfig.builder()
            .core(CoreConfig.defaultConfig())
            .cache(CacheConfig.defaultConfig())
            .build();

        assertDoesNotThrow(() -> {
            Query4jConfigurationFactory.validate(validConfig);
        });
    }

    @Test
    void testValidateWithNullConfig() {
        assertThrows(IllegalArgumentException.class, () -> {
            Query4jConfigurationFactory.validate(null);
        });
    }

    @Test
    void testFromPropertiesWithValidMap() {
        Map<String, String> properties = new HashMap<>();
        properties.put("query4j.core.defaultQueryTimeoutMs", "20000");
        properties.put("query4j.core.maxPredicateDepth", "20");
        properties.put("query4j.cache.enabled", "true");
        properties.put("query4j.cache.maxSize", "5000");

        Query4jConfig config = Query4jConfigurationFactory.fromProperties(properties);
        
        assertNotNull(config);
        assertEquals(20000L, config.getCore().getDefaultQueryTimeoutMs());
        assertEquals(20, config.getCore().getMaxPredicateDepth());
        assertTrue(config.getCache().isEnabled());
        assertEquals(5000L, config.getCache().getMaxSize());
    }

    @Test
    void testFromPropertiesWithEmptyMap() {
        Map<String, String> properties = new HashMap<>();
        Query4jConfig config = Query4jConfigurationFactory.fromProperties(properties);
        
        assertNotNull(config);
        // Should have default values
        assertEquals(30000L, config.getCore().getDefaultQueryTimeoutMs());
        assertTrue(config.getCache().isEnabled());
    }

    @Test
    void testFromPropertiesWithNullMap() {
        assertThrows(IllegalArgumentException.class, () -> {
            Query4jConfigurationFactory.fromProperties(null);
        });
    }

    @Test
    void testFromPropertiesWithInvalidPropertyName() {
        Map<String, String> properties = new HashMap<>();
        properties.put("invalid.property.name", "value");
        properties.put("query4j.core.defaultQueryTimeoutMs", "15000");

        // Should still work but ignore invalid properties
        Query4jConfig config = Query4jConfigurationFactory.fromProperties(properties);
        assertNotNull(config);
        assertEquals(15000L, config.getCore().getDefaultQueryTimeoutMs());
    }

    @Test
    void testThreadSafetyOfGetDefault() throws InterruptedException {
        final int threadCount = 10;
        final Query4jConfig[] configs = new Query4jConfig[threadCount];
        final Thread[] threads = new Thread[threadCount];

        // Create multiple threads that call getDefault() simultaneously
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                configs[index] = Query4jConfigurationFactory.getDefault();
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Verify all threads got the same instance
        for (int i = 1; i < threadCount; i++) {
            assertSame(configs[0], configs[i]);
        }
    }
}