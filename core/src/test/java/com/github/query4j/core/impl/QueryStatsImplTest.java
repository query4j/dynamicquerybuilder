package com.github.query4j.core.impl;

import com.github.query4j.core.QueryStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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
    void testSetters() {
        stats = new QueryStatsImpl();

        // Directly set using reflection or create additional setters if needed
        // Currently update() manages state; test only that update works as expected.
        stats.update(200L, 10, false);

        assertEquals(200L, stats.getExecutionTimeMs());
        assertEquals(10, stats.getResultCount());
        assertFalse(stats.wasCacheHit());
    }

    @Test
    void testHintsAreImmutable() {
        Map<String, Object> hints = stats.getHints();
        assertThrows(UnsupportedOperationException.class, () -> hints.put("key", "value"));
    }
}
