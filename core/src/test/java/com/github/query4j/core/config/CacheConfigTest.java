package com.github.query4j.core.config;

import com.github.query4j.core.DynamicQueryException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for CacheConfig.
 * Ensures 95%+ coverage by testing all public methods, builders, and validation.
 */
public class CacheConfigTest {

    @Test
    void testDefaultConfigCreation() {
        CacheConfig config = CacheConfig.defaultConfig();
        
        assertTrue(config.isEnabled());
        assertEquals(3600L, config.getDefaultTtlSeconds());
        assertEquals(10_000L, config.getMaxSize());
        assertEquals("default", config.getDefaultRegion());
        assertFalse(config.isStatisticsEnabled());
        assertEquals(60L, config.getMaintenanceIntervalSeconds());
        assertTrue(config.isKeyValidationEnabled());
        assertEquals(512, config.getMaxKeyLength()); // Actual default is 512
        assertEquals(16, config.getConcurrencyLevel());
        assertFalse(config.isAutoWarmupEnabled());
        assertEquals(100, config.getWarmupSize()); // Actual default is 100
    }

    @Test
    void testHighPerformanceConfigCreation() {
        CacheConfig config = CacheConfig.highPerformanceConfig();
        
        assertTrue(config.isEnabled());
        assertEquals(7200L, config.getDefaultTtlSeconds());
        assertEquals(50000L, config.getMaxSize());
        assertEquals("highperf", config.getDefaultRegion());
        assertTrue(config.isStatisticsEnabled());
        assertEquals(30L, config.getMaintenanceIntervalSeconds());
        assertFalse(config.isKeyValidationEnabled());
        assertEquals(32, config.getConcurrencyLevel());
        assertTrue(config.isAutoWarmupEnabled());
        assertEquals(5000, config.getWarmupSize());
    }

    @Test
    void testDevelopmentConfigCreation() {
        CacheConfig config = CacheConfig.developmentConfig();
        
        assertFalse(config.isEnabled());
        assertEquals(300L, config.getDefaultTtlSeconds());
        assertEquals(1000L, config.getMaxSize());
        assertEquals("dev", config.getDefaultRegion());
        assertTrue(config.isStatisticsEnabled());
        assertEquals(120L, config.getMaintenanceIntervalSeconds());
        assertTrue(config.isKeyValidationEnabled());
        assertEquals(128, config.getMaxKeyLength());
        assertEquals(4, config.getConcurrencyLevel());
        assertFalse(config.isAutoWarmupEnabled());
        assertEquals(100, config.getWarmupSize());
    }

    @Test
    void testDisabledConfigCreation() {
        CacheConfig config = CacheConfig.disabledConfig();
        
        // Only test what disabledConfig() specifically sets
        assertFalse(config.isEnabled());
        
        // Other values should use defaults, not specific values
        assertTrue(config.getDefaultTtlSeconds() > 0); // Uses default
        assertTrue(config.getMaxSize() > 0); // Uses default
    }

    @Test
    void testBuilderWithAllCustomValues() {
        CacheConfig config = CacheConfig.builder()
            .enabled(true)
            .defaultTtlSeconds(5400L)
            .maxSize(25000L)
            .defaultRegion("custom")
            .statisticsEnabled(true)
            .maintenanceIntervalSeconds(45L)
            .keyValidationEnabled(false)
            .maxKeyLength(512)
            .concurrencyLevel(24)
            .autoWarmupEnabled(true)
            .warmupSize(2500)
            .build();

        assertTrue(config.isEnabled());
        assertEquals(5400L, config.getDefaultTtlSeconds());
        assertEquals(25000L, config.getMaxSize());
        assertEquals("custom", config.getDefaultRegion());
        assertTrue(config.isStatisticsEnabled());
        assertEquals(45L, config.getMaintenanceIntervalSeconds());
        assertFalse(config.isKeyValidationEnabled());
        assertEquals(512, config.getMaxKeyLength());
        assertEquals(24, config.getConcurrencyLevel());
        assertTrue(config.isAutoWarmupEnabled());
        assertEquals(2500, config.getWarmupSize());
    }

    @Test
    void testBuilderWithDefaultValues() {
        CacheConfig config = CacheConfig.builder().build();
        
        assertTrue(config.isEnabled());
        assertEquals(3600L, config.getDefaultTtlSeconds());
        assertEquals(10000L, config.getMaxSize());
        assertEquals("default", config.getDefaultRegion());
        assertFalse(config.isStatisticsEnabled());
        assertEquals(60L, config.getMaintenanceIntervalSeconds());
        assertTrue(config.isKeyValidationEnabled());
        assertEquals(256, config.getMaxKeyLength());
        assertEquals(16, config.getConcurrencyLevel());
        assertFalse(config.isAutoWarmupEnabled());
        assertEquals(1000, config.getWarmupSize());
    }

    @Test
    void testValidationWithValidConfiguration() {
        CacheConfig config = CacheConfig.builder()
            .enabled(true)
            .defaultTtlSeconds(1800L)
            .maxSize(5000L)
            .build();

        assertDoesNotThrow(() -> config.validate());
    }

    @Test
    void testValidationWithNegativeTtl() {
        assertThrows(IllegalStateException.class, () -> {
            CacheConfig.builder()
                .defaultTtlSeconds(-100L)
                .build()
                .validate();
        });
    }

    @Test
    void testValidationWithNegativeMaxSize() {
        assertThrows(IllegalStateException.class, () -> {
            CacheConfig.builder()
                .maxSize(-1000L)
                .build()
                .validate();
        });
    }

    @Test
    void testValidationWithNullDefaultRegion() {
        assertThrows(IllegalStateException.class, () -> {
            CacheConfig.builder()
                .defaultRegion(null)
                .build()
                .validate();
        });
    }

    @Test
    void testValidationWithEmptyDefaultRegion() {
        assertThrows(IllegalStateException.class, () -> {
            CacheConfig.builder()
                .defaultRegion("")
                .build()
                .validate();
        });
    }

    @Test
    void testValidationWithNegativeMaintenanceInterval() {
        assertThrows(IllegalStateException.class, () -> {
            CacheConfig.builder()
                .maintenanceIntervalSeconds(-10L)
                .build()
                .validate();
        });
    }

    @Test
    void testValidationWithInvalidMaxKeyLength() {
        assertThrows(IllegalStateException.class, () -> {
            CacheConfig.builder()
                .maxKeyLength(0)
                .build()
                .validate();
        });

        assertThrows(IllegalStateException.class, () -> {
            CacheConfig.builder()
                .maxKeyLength(-50)
                .build()
                .validate();
        });
    }

    @Test
    void testValidationWithInvalidConcurrencyLevel() {
        assertThrows(IllegalStateException.class, () -> {
            CacheConfig.builder()
                .concurrencyLevel(0)
                .build()
                .validate();
        });

        assertThrows(IllegalStateException.class, () -> {
            CacheConfig.builder()
                .concurrencyLevel(-5)
                .build()
                .validate();
        });
    }

    @Test
    void testValidationWithInvalidWarmupSize() {
        assertThrows(IllegalStateException.class, () -> {
            CacheConfig.builder()
                .warmupSize(-100)
                .build()
                .validate();
        });
    }

    @Test
    void testValidationRequiresPositiveValues() {
        // Validation requires positive values even for disabled cache
        assertThrows(IllegalStateException.class, () -> {
            CacheConfig.builder()
                .enabled(false)
                .maxSize(0L) // Invalid: must be positive
                .build()
                .validate();
        });
        
        // Valid configuration with positive values
        assertDoesNotThrow(() -> {
            CacheConfig.builder()
                .enabled(false)
                .defaultTtlSeconds(1L)
                .maxSize(1L)
                .maintenanceIntervalSeconds(0L) // Zero is allowed for maintenance interval
                .warmupSize(0) // Zero is allowed for warmup size
                .build()
                .validate();
        });
    }

    @Test
    void testEqualsAndHashCode() {
        CacheConfig config1 = CacheConfig.builder()
            .enabled(true)
            .maxSize(5000L)
            .defaultTtlSeconds(1800L)
            .build();

        CacheConfig config2 = CacheConfig.builder()
            .enabled(true)
            .maxSize(5000L)
            .defaultTtlSeconds(1800L)
            .build();

        CacheConfig config3 = CacheConfig.builder()
            .enabled(false)
            .maxSize(5000L)
            .defaultTtlSeconds(1800L)
            .build();

        assertEquals(config1, config2);
        assertEquals(config1.hashCode(), config2.hashCode());
        assertNotEquals(config1, config3);
        assertNotEquals(config1.hashCode(), config3.hashCode());
    }

    @Test
    void testToString() {
        CacheConfig config = CacheConfig.defaultConfig();
        String str = config.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("CacheConfig"));
        assertTrue(str.contains("enabled"));
        assertTrue(str.contains("maxSize"));
        assertTrue(str.contains("defaultTtlSeconds"));
    }

    @Test
    void testBuilderToString() {
        CacheConfig.CacheConfigBuilder builder = CacheConfig.builder()
            .enabled(true)
            .maxSize(5000L);
        
        String str = builder.toString();
        assertNotNull(str);
        assertTrue(str.contains("CacheConfigBuilder"));
    }

    @Test
    void testAllGettersReturnExpectedValues() {
        CacheConfig config = CacheConfig.builder()
            .enabled(false)
            .defaultTtlSeconds(7200L)
            .maxSize(15000L)
            .defaultRegion("test")
            .statisticsEnabled(true)
            .maintenanceIntervalSeconds(90L)
            .keyValidationEnabled(false)
            .maxKeyLength(1024)
            .concurrencyLevel(8)
            .autoWarmupEnabled(true)
            .warmupSize(500)
            .build();

        assertFalse(config.isEnabled());
        assertEquals(7200L, config.getDefaultTtlSeconds());
        assertEquals(15000L, config.getMaxSize());
        assertEquals("test", config.getDefaultRegion());
        assertTrue(config.isStatisticsEnabled());
        assertEquals(90L, config.getMaintenanceIntervalSeconds());
        assertFalse(config.isKeyValidationEnabled());
        assertEquals(1024, config.getMaxKeyLength());
        assertEquals(8, config.getConcurrencyLevel());
        assertTrue(config.isAutoWarmupEnabled());
        assertEquals(500, config.getWarmupSize());
    }

    @Test
    void testBuilderReusability() {
        CacheConfig.CacheConfigBuilder builder = CacheConfig.builder()
            .enabled(true)
            .maxSize(5000L);

        CacheConfig config1 = builder.build();
        CacheConfig config2 = builder.defaultTtlSeconds(7200L).build();

        // Both should be valid but different
        assertTrue(config1.isEnabled());
        assertTrue(config2.isEnabled());
        assertEquals(5000L, config1.getMaxSize());
        assertEquals(5000L, config2.getMaxSize());
        assertEquals(3600L, config1.getDefaultTtlSeconds()); // default
        assertEquals(7200L, config2.getDefaultTtlSeconds()); // custom
    }

    @Test
    void testBuilderValidatesOnBuild() {
        // Valid configuration should build successfully
        assertDoesNotThrow(() -> {
            CacheConfig.builder()
                .enabled(true)
                .maxSize(1000L)
                .build();
        });

        // Invalid configuration should build but fail during validation
        assertDoesNotThrow(() -> {
            CacheConfig config = CacheConfig.builder()
                .maxSize(-1L)
                .build();
            
            // Should fail during validation
            assertThrows(IllegalStateException.class, config::validate);
        });
    }
}