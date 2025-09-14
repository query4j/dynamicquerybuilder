package com.github.query4j.core.config;

import com.github.query4j.core.DynamicQueryException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for Query4jConfig.
 * Ensures 95%+ coverage by testing all public methods, builders, and edge cases.
 */
public class Query4jConfigTest {

    @Test
    void testDefaultConfigCreation() {
        Query4jConfig config = Query4jConfig.defaultConfig();
        
        assertNotNull(config);
        assertNotNull(config.getCore());
        assertNotNull(config.getCache());
        assertTrue(config.isAutoConfigurationEnabled());
        assertEquals(100, config.getEnvironmentVariablePriority());
        assertEquals(90, config.getSystemPropertyPriority());
        assertEquals(80, config.getYamlFilePriority());
        assertEquals(70, config.getPropertiesFilePriority());
        assertEquals(1, config.getDefaultValuesPriority());
    }

    @Test
    void testHighPerformanceConfigCreation() {
        Query4jConfig config = Query4jConfig.highPerformanceConfig();
        
        assertNotNull(config);
        assertNotNull(config.getCore());
        assertNotNull(config.getCache());
        
        // Verify high performance characteristics
        CoreConfig core = config.getCore();
        assertEquals(15000L, core.getDefaultQueryTimeoutMs());
        assertEquals(20, core.getMaxPredicateDepth());
        assertEquals(200, core.getMaxPredicateCount());
        
        CacheConfig cache = config.getCache();
        assertTrue(cache.isEnabled());
        assertEquals(50000L, cache.getMaxSize());
        assertEquals(7200L, cache.getDefaultTtlSeconds());
    }

    @Test
    void testDevelopmentConfigCreation() {
        Query4jConfig config = Query4jConfig.developmentConfig();
        
        assertNotNull(config);
        assertNotNull(config.getCore());
        assertNotNull(config.getCache());
        
        // Verify development characteristics
        CoreConfig core = config.getCore();
        assertEquals(60000L, core.getDefaultQueryTimeoutMs());
        assertTrue(core.isQueryStatisticsEnabled());
        assertTrue(core.isStrictFieldValidation());
        
        CacheConfig cache = config.getCache();
        assertFalse(cache.isEnabled());
        assertEquals(1000L, cache.getMaxSize());
        assertEquals(300L, cache.getDefaultTtlSeconds());
    }

    @Test
    void testMinimalConfigCreation() {
        Query4jConfig config = Query4jConfig.minimalConfig();
        
        assertNotNull(config);
        assertNotNull(config.getCore());
        assertNotNull(config.getCache());
        
        // Verify minimal characteristics
        CoreConfig core = config.getCore();
        assertEquals(10000L, core.getDefaultQueryTimeoutMs());
        assertEquals(5, core.getMaxPredicateDepth());
        assertEquals(25, core.getMaxPredicateCount());
        assertFalse(core.isQueryStatisticsEnabled());
        
        CacheConfig cache = config.getCache();
        assertFalse(cache.isEnabled());
        assertEquals(100L, cache.getMaxSize());
    }

    @Test
    void testBuilderWithCustomValues() {
        Query4jConfig config = Query4jConfig.builder()
            .core(CoreConfig.builder()
                .defaultQueryTimeoutMs(45000L)
                .maxPredicateDepth(15)
                .queryStatisticsEnabled(true)
                .build())
            .cache(CacheConfig.builder()
                .enabled(true)
                .maxSize(25000L)
                .defaultTtlSeconds(1800L)
                .build())
            .autoConfigurationEnabled(false)
            .environmentVariablePriority(200)
            .systemPropertyPriority(180)
            .yamlFilePriority(160)
            .propertiesFilePriority(140)
            .defaultValuesPriority(50)
            .build();

        assertEquals(45000L, config.getCore().getDefaultQueryTimeoutMs());
        assertEquals(15, config.getCore().getMaxPredicateDepth());
        assertTrue(config.getCore().isQueryStatisticsEnabled());
        
        assertTrue(config.getCache().isEnabled());
        assertEquals(25000L, config.getCache().getMaxSize());
        assertEquals(1800L, config.getCache().getDefaultTtlSeconds());
        
        assertFalse(config.isAutoConfigurationEnabled());
        assertEquals(200, config.getEnvironmentVariablePriority());
        assertEquals(180, config.getSystemPropertyPriority());
        assertEquals(160, config.getYamlFilePriority());
        assertEquals(140, config.getPropertiesFilePriority());
        assertEquals(50, config.getDefaultValuesPriority());
    }

    @Test
    void testBuilderWithDefaultValues() {
        Query4jConfig config = Query4jConfig.builder().build();
        
        assertNotNull(config);
        assertNotNull(config.getCore());
        assertNotNull(config.getCache());
        assertTrue(config.isAutoConfigurationEnabled());
        assertEquals(100, config.getEnvironmentVariablePriority());
        assertEquals(90, config.getSystemPropertyPriority());
        assertEquals(80, config.getYamlFilePriority());
        assertEquals(70, config.getPropertiesFilePriority());
        assertEquals(1, config.getDefaultValuesPriority());
    }

    @Test
    void testValidationWithValidConfiguration() {
        Query4jConfig config = Query4jConfig.builder()
            .core(CoreConfig.defaultConfig())
            .cache(CacheConfig.defaultConfig())
            .build();

        assertDoesNotThrow(() -> config.validate());
    }

    @Test
    void testValidationWithNullCore() {
        assertThrows(DynamicQueryException.class, () -> {
            Query4jConfig.builder()
                .core(null)
                .cache(CacheConfig.defaultConfig())
                .build()
                .validate();
        });
    }

    @Test
    void testValidationWithNullCache() {
        assertThrows(DynamicQueryException.class, () -> {
            Query4jConfig.builder()
                .core(CoreConfig.defaultConfig())
                .cache(null)
                .build()
                .validate();
        });
    }

    @Test
    void testValidationWithInvalidPriorities() {
        assertThrows(DynamicQueryException.class, () -> {
            Query4jConfig.builder()
                .core(CoreConfig.defaultConfig())
                .cache(CacheConfig.defaultConfig())
                .environmentVariablePriority(-1)
                .build()
                .validate();
        });

        assertThrows(DynamicQueryException.class, () -> {
            Query4jConfig.builder()
                .core(CoreConfig.defaultConfig())
                .cache(CacheConfig.defaultConfig())
                .systemPropertyPriority(0)
                .build()
                .validate();
        });
    }

    @Test
    void testToBuilderCreatesNewBuilderWithSameValues() {
        Query4jConfig original = Query4jConfig.builder()
            .core(CoreConfig.highPerformanceConfig())
            .cache(CacheConfig.developmentConfig())
            .autoConfigurationEnabled(false)
            .environmentVariablePriority(150)
            .build();

        Query4jConfig copy = original.toBuilder().build();

        // Should not be the same instance
        assertNotSame(original, copy);

        // But should have same values
        assertEquals(original.getCore().getDefaultQueryTimeoutMs(), 
                    copy.getCore().getDefaultQueryTimeoutMs());
        assertEquals(original.getCache().isEnabled(), 
                    copy.getCache().isEnabled());
        assertEquals(original.isAutoConfigurationEnabled(), 
                    copy.isAutoConfigurationEnabled());
        assertEquals(original.getEnvironmentVariablePriority(), 
                    copy.getEnvironmentVariablePriority());
    }

    @Test
    void testToBuilderAllowsModification() {
        Query4jConfig original = Query4jConfig.defaultConfig();

        Query4jConfig modified = original.toBuilder()
            .autoConfigurationEnabled(false)
            .environmentVariablePriority(50)
            .build();

        assertNotEquals(original.isAutoConfigurationEnabled(), 
                       modified.isAutoConfigurationEnabled());
        assertNotEquals(original.getEnvironmentVariablePriority(), 
                       modified.getEnvironmentVariablePriority());
    }

    @Test
    void testEqualsAndHashCode() {
        Query4jConfig config1 = Query4jConfig.builder()
            .core(CoreConfig.defaultConfig())
            .cache(CacheConfig.defaultConfig())
            .autoConfigurationEnabled(true)
            .build();

        Query4jConfig config2 = Query4jConfig.builder()
            .core(CoreConfig.defaultConfig())
            .cache(CacheConfig.defaultConfig())
            .autoConfigurationEnabled(true)
            .build();

        Query4jConfig config3 = Query4jConfig.builder()
            .core(CoreConfig.defaultConfig())
            .cache(CacheConfig.defaultConfig())
            .autoConfigurationEnabled(false)
            .build();

        assertEquals(config1, config2);
        assertEquals(config1.hashCode(), config2.hashCode());
        assertNotEquals(config1, config3);
        assertNotEquals(config1.hashCode(), config3.hashCode());
    }

    @Test
    void testToString() {
        Query4jConfig config = Query4jConfig.defaultConfig();
        String str = config.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("Query4jConfig"));
        assertTrue(str.contains("core"));
        assertTrue(str.contains("cache"));
        assertTrue(str.contains("autoConfigurationEnabled"));
    }

    @Test
    void testBuilderToString() {
        Query4jConfig.Query4jConfigBuilder builder = Query4jConfig.builder()
            .autoConfigurationEnabled(true);
        
        String str = builder.toString();
        assertNotNull(str);
        assertTrue(str.contains("Query4jConfigBuilder"));
    }

    @Test
    void testAllGettersReturnNonNull() {
        Query4jConfig config = Query4jConfig.defaultConfig();
        
        assertNotNull(config.getCore());
        assertNotNull(config.getCache());
        assertTrue(config.getEnvironmentVariablePriority() > 0);
        assertTrue(config.getSystemPropertyPriority() > 0);
        assertTrue(config.getYamlFilePriority() > 0);
        assertTrue(config.getPropertiesFilePriority() > 0);
        assertTrue(config.getDefaultValuesPriority() > 0);
    }

    @Test
    void testBuilderValidatesOnBuild() {
        // Valid configuration should build successfully
        assertDoesNotThrow(() -> {
            Query4jConfig.builder()
                .core(CoreConfig.defaultConfig())
                .cache(CacheConfig.defaultConfig())
                .build();
        });
    }
}