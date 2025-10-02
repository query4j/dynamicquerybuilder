package com.github.query4j.core.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CoreConfig class.
 */
class CoreConfigTest {

    @Test
    void defaultConfig_shouldHaveSafeDefaults() {
        CoreConfig config = CoreConfig.defaultConfig();
        
        assertNotNull(config);
        assertEquals(30_000L, config.getDefaultQueryTimeoutMs());
        assertEquals(10, config.getMaxPredicateDepth());
        assertEquals(50, config.getMaxPredicateCount());
        assertTrue(config.isLikePredacatesEnabled());
        assertTrue(config.isInPredicatesEnabled());
        assertTrue(config.isBetweenPredicatesEnabled());
        assertTrue(config.isNullPredicatesEnabled());
        assertEquals(1000, config.getMaxInPredicateSize());
        assertTrue(config.isStrictFieldValidation());
        assertTrue(config.isParameterCollisionDetection());
        assertEquals(20, config.getDefaultPageSize());
        assertEquals(1000, config.getMaxPageSize());
        assertTrue(config.isQueryStatisticsEnabled());
    }
    
    @Test
    void highPerformanceConfig_shouldOptimizeForPerformance() {
        CoreConfig config = CoreConfig.highPerformanceConfig();
        
        assertNotNull(config);
        assertEquals(10_000L, config.getDefaultQueryTimeoutMs());
        assertEquals(8, config.getMaxPredicateDepth());
        assertEquals(30, config.getMaxPredicateCount());
        assertEquals(500, config.getMaxInPredicateSize());
        assertFalse(config.isQueryStatisticsEnabled());
    }
    
    @Test
    void developmentConfig_shouldOptimizeForDebugging() {
        CoreConfig config = CoreConfig.developmentConfig();
        
        assertNotNull(config);
        assertEquals(60_000L, config.getDefaultQueryTimeoutMs());
        assertEquals(15, config.getMaxPredicateDepth());
        assertEquals(100, config.getMaxPredicateCount());
        assertTrue(config.isStrictFieldValidation());
        assertTrue(config.isParameterCollisionDetection());
        assertTrue(config.isQueryStatisticsEnabled());
    }
    
    @Test
    void validate_shouldPassForValidConfiguration() {
        CoreConfig config = CoreConfig.defaultConfig();
        
        assertDoesNotThrow(config::validate);
    }
    
    @Test
    void validate_shouldFailForInvalidMaxPredicateDepth() {
        CoreConfig config = CoreConfig.builder()
                .maxPredicateDepth(0)
                .build();
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, config::validate);
        assertTrue(exception.getMessage().contains("maxPredicateDepth must be at least 1"));
    }
    
    @Test
    void validate_shouldFailForInvalidMaxPredicateCount() {
        CoreConfig config = CoreConfig.builder()
                .maxPredicateCount(-1)
                .build();
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, config::validate);
        assertTrue(exception.getMessage().contains("maxPredicateCount must be at least 1"));
    }
    
    @Test
    void validate_shouldFailForInvalidMaxInPredicateSize() {
        CoreConfig config = CoreConfig.builder()
                .maxInPredicateSize(0)
                .build();
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, config::validate);
        assertTrue(exception.getMessage().contains("maxInPredicateSize must be at least 1"));
    }
    
    @Test
    void validate_shouldFailForInvalidPageSizes() {
        CoreConfig config = CoreConfig.builder()
                .defaultPageSize(100)
                .maxPageSize(50)
                .build();
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, config::validate);
        assertTrue(exception.getMessage().contains("maxPageSize") && 
                   exception.getMessage().contains("defaultPageSize"));
    }
    
    @Test
    void builderPattern_shouldAllowCustomization() {
        CoreConfig config = CoreConfig.builder()
                .defaultQueryTimeoutMs(15_000L)
                .maxPredicateDepth(5)
                .likePredacatesEnabled(false)
                .queryStatisticsEnabled(false)
                .build();
        
        assertEquals(15_000L, config.getDefaultQueryTimeoutMs());
        assertEquals(5, config.getMaxPredicateDepth());
        assertFalse(config.isLikePredacatesEnabled());
        assertFalse(config.isQueryStatisticsEnabled());
    }
}