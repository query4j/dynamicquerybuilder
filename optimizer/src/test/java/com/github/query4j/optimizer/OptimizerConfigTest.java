package com.github.query4j.optimizer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for OptimizerConfig.
 */
public class OptimizerConfigTest {

    @Test
    void testDefaultConfig() {
        OptimizerConfig config = OptimizerConfig.defaultConfig();
        
        assertNotNull(config);
        assertTrue(config.isIndexSuggestionsEnabled());
        assertTrue(config.isPredicatePushdownEnabled());
        assertTrue(config.isJoinReorderingEnabled());
        assertEquals(0.1, config.getIndexSelectivityThreshold(), 0.001);
        assertEquals(0.05, config.getPredicateReorderingThreshold(), 0.001);
        assertEquals(0.1, config.getJoinReorderingThreshold(), 0.001);
        assertEquals(5000, config.getMaxAnalysisTimeMs());
        assertFalse(config.isVerboseOutput());
        assertEquals(3, config.getMaxCompositeIndexColumns());
        assertEquals(OptimizerConfig.DatabaseType.GENERIC, config.getTargetDatabase());
    }

    @Test
    void testHighPerformanceConfig() {
        OptimizerConfig config = OptimizerConfig.highPerformanceConfig();
        
        assertNotNull(config);
        assertEquals(0.05, config.getIndexSelectivityThreshold(), 0.001);
        assertEquals(0.02, config.getPredicateReorderingThreshold(), 0.001);
        assertEquals(0.05, config.getJoinReorderingThreshold(), 0.001);
        assertEquals(4, config.getMaxCompositeIndexColumns());
        assertFalse(config.isVerboseOutput());
    }

    @Test
    void testDevelopmentConfig() {
        OptimizerConfig config = OptimizerConfig.developmentConfig();
        
        assertNotNull(config);
        assertTrue(config.isVerboseOutput());
        assertEquals(10000, config.getMaxAnalysisTimeMs());
    }

    @Test
    void testBuilderCustomValues() {
        OptimizerConfig config = OptimizerConfig.builder()
            .indexSuggestionsEnabled(false)
            .predicatePushdownEnabled(false)
            .joinReorderingEnabled(false)
            .indexSelectivityThreshold(0.2)
            .predicateReorderingThreshold(0.1)
            .joinReorderingThreshold(0.15)
            .maxAnalysisTimeMs(8000)
            .verboseOutput(true)
            .maxCompositeIndexColumns(5)
            .targetDatabase(OptimizerConfig.DatabaseType.POSTGRESQL)
            .build();

        assertFalse(config.isIndexSuggestionsEnabled());
        assertFalse(config.isPredicatePushdownEnabled());
        assertFalse(config.isJoinReorderingEnabled());
        assertEquals(0.2, config.getIndexSelectivityThreshold(), 0.001);
        assertEquals(0.1, config.getPredicateReorderingThreshold(), 0.001);
        assertEquals(0.15, config.getJoinReorderingThreshold(), 0.001);
        assertEquals(8000, config.getMaxAnalysisTimeMs());
        assertTrue(config.isVerboseOutput());
        assertEquals(5, config.getMaxCompositeIndexColumns());
        assertEquals(OptimizerConfig.DatabaseType.POSTGRESQL, config.getTargetDatabase());
    }

    @Test
    void testDatabaseTypeEnum() {
        OptimizerConfig.DatabaseType[] types = OptimizerConfig.DatabaseType.values();
        
        assertTrue(types.length >= 6);
        assertNotNull(OptimizerConfig.DatabaseType.GENERIC.getDisplayName());
        assertNotNull(OptimizerConfig.DatabaseType.POSTGRESQL.getDisplayName());
        assertNotNull(OptimizerConfig.DatabaseType.MYSQL.getDisplayName());
        assertNotNull(OptimizerConfig.DatabaseType.H2.getDisplayName());
        assertNotNull(OptimizerConfig.DatabaseType.ORACLE.getDisplayName());
        assertNotNull(OptimizerConfig.DatabaseType.SQL_SERVER.getDisplayName());
        
        assertEquals("Generic SQL", OptimizerConfig.DatabaseType.GENERIC.getDisplayName());
        assertEquals("PostgreSQL", OptimizerConfig.DatabaseType.POSTGRESQL.getDisplayName());
    }

    @Test
    void testEqualsAndHashCode() {
        OptimizerConfig config1 = OptimizerConfig.builder()
            .verboseOutput(true)
            .maxAnalysisTimeMs(5000)
            .build();

        OptimizerConfig config2 = OptimizerConfig.builder()
            .verboseOutput(true)
            .maxAnalysisTimeMs(5000)
            .build();

        OptimizerConfig config3 = OptimizerConfig.builder()
            .verboseOutput(false)
            .maxAnalysisTimeMs(5000)
            .build();

        assertEquals(config1, config2);
        assertEquals(config1.hashCode(), config2.hashCode());
        assertNotEquals(config1, config3);
        assertNotEquals(config1.hashCode(), config3.hashCode());
    }

    @Test
    void testToString() {
        OptimizerConfig config = OptimizerConfig.defaultConfig();
        String str = config.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("OptimizerConfig"));
    }
}