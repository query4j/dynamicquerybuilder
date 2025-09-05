package com.github.query4j.core;

import java.util.Map;

/**
 * Holds execution metadata for queries.
 * Provides performance and diagnostic information.
 *
 * @since 1.0.0
 */
public interface QueryStats {
    long getExecutionTimeMs();
    int getResultCount();
    String getGeneratedSQL();
    Map<String, Object> getHints();
    boolean wasCacheHit();
    long getExecutionTimestamp();
}