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
}
