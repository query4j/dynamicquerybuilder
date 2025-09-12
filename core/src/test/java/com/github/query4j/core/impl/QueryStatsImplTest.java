package com.github.query4j.core.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QueryStatsImplTest {

    private QueryStatsImpl stats;

    @BeforeEach
    void setup() {
        stats = new QueryStatsImpl();
    }

    @Test
    void testInitialValues() {
        assertEquals(0L, stats.getExecutionTimeMs());
        assertEquals(0, stats.getResultCount());
        assertEquals("", stats.getGeneratedSQL());
        assertNotNull(stats.getHints());
        assertTrue(stats.getHints().isEmpty());
        assertFalse(stats.wasCacheHit());
        assertTrue(stats.getExecutionTimestamp() > 0);
    }

    @Test
    void testUpdateProperties() {
        stats.update(150L, 5, true);

        assertEquals(150L, stats.getExecutionTimeMs());
        assertEquals(5, stats.getResultCount());
        assertTrue(stats.wasCacheHit());
    }

    @Test
    void testHintsAreImmutable() {
        Map<String, Object> hints = stats.getHints();
        assertThrows(UnsupportedOperationException.class, () -> hints.put("key", "value"));
    }

    @Test
    void testSqlMethod() {
        String sql = "SELECT * FROM users WHERE id = :p1";
        QueryStatsImpl result = stats.sql(sql);
        
        // Should return same instance for method chaining
        assertEquals(stats, result);
        assertEquals(sql, stats.getGeneratedSQL());
    }

    @Test
    void testSqlMethodWithNull() {
        QueryStatsImpl result = stats.sql(null);
        
        assertEquals(stats, result);
        assertEquals("", stats.getGeneratedSQL());
    }

    @Test
    void testSqlMethodWithEmpty() {
        String emptySql = "";
        QueryStatsImpl result = stats.sql(emptySql);
        
        assertEquals(stats, result);
        assertEquals(emptySql, stats.getGeneratedSQL());
    }

    @Test
    void testHintsMethod() {
        Map<String, Object> hintsMap = Map.of(
            "fetchSize", 1000,
            "timeout", 30000,
            "cacheKey", "user_query_123"
        );
        
        QueryStatsImpl result = stats.hints(hintsMap);
        
        // Should return same instance for method chaining
        assertEquals(stats, result);
        
        Map<String, Object> retrievedHints = stats.getHints();
        assertEquals(3, retrievedHints.size());
        assertEquals(1000, retrievedHints.get("fetchSize"));
        assertEquals(30000, retrievedHints.get("timeout"));
        assertEquals("user_query_123", retrievedHints.get("cacheKey"));
        
        // Should be immutable
        assertThrows(UnsupportedOperationException.class, 
            () -> retrievedHints.put("newKey", "newValue"));
    }

    @Test
    void testHintsMethodWithNull() {
        QueryStatsImpl result = stats.hints(null);
        
        assertEquals(stats, result);
        assertTrue(stats.getHints().isEmpty());
    }

    @Test
    void testHintsMethodWithEmpty() {
        QueryStatsImpl result = stats.hints(Map.of());
        
        assertEquals(stats, result);
        assertTrue(stats.getHints().isEmpty());
    }

    @Test
    void testIsCacheHitMethod() {
        // Initially should be false
        assertFalse(stats.isCacheHit());
        
        // After update with cache hit
        stats.update(100L, 5, true);
        assertTrue(stats.isCacheHit());
        
        // After update without cache hit
        stats.update(200L, 10, false);
        assertFalse(stats.isCacheHit());
    }

    @Test
    void testMethodChaining() {
        Map<String, Object> hints = Map.of("fetchSize", 500);
        String sql = "SELECT COUNT(*) FROM orders";
        
        QueryStatsImpl result = stats
            .sql(sql)
            .hints(hints);
        
        assertEquals(stats, result);
        assertEquals(sql, stats.getGeneratedSQL());
        assertEquals(500, stats.getHints().get("fetchSize"));
    }

    @Test
    void testComplexScenario() {
        // Test a realistic usage scenario
        Map<String, Object> queryHints = Map.of(
            "fetchSize", 1000,
            "queryTimeout", 30,
            "enableBatching", true
        );
        
        stats.sql("SELECT u.id, u.name FROM users u WHERE u.active = :p1")
             .hints(queryHints)
             .update(150L, 25, true);
        
        assertEquals("SELECT u.id, u.name FROM users u WHERE u.active = :p1", stats.getGeneratedSQL());
        assertEquals(150L, stats.getExecutionTimeMs());
        assertEquals(25, stats.getResultCount());
        assertTrue(stats.wasCacheHit());
        assertTrue(stats.isCacheHit());
        assertEquals(1000, stats.getHints().get("fetchSize"));
        assertEquals(30, stats.getHints().get("queryTimeout"));
        assertEquals(true, stats.getHints().get("enableBatching"));
    }
}
