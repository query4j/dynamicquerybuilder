package com.github.query4j.core.impl;

import com.github.query4j.core.QueryStats;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Map;

/**
 * Basic implementation of QueryStats interface.
 * Provides execution timing, result count, SQL and hints info.
 * 
 * @since 1.0.0
 */
@NoArgsConstructor
public final class QueryStatsImpl implements QueryStats {

    @Getter
    private long executionTimeMs;

    @Getter
    private int resultCount;

    @Getter
    private String generatedSQL = "";

    @Getter
    private Map<String, Object> hints = Collections.emptyMap();

    @Getter
    private boolean cacheHit;

    @Getter
    private long executionTimestamp = System.currentTimeMillis();

    /**
     * Updates the statistics data.
     * 
     * @param executionTimeMs execution time in milliseconds
     * @param resultCount number of results returned
     * @param cacheHit whether cache was hit
     */
    public void update(long executionTimeMs, int resultCount, boolean cacheHit) {
        this.executionTimeMs = executionTimeMs;
        this.resultCount = resultCount;
        this.cacheHit = cacheHit;
    }

	@Override
	public boolean wasCacheHit() {
		return cacheHit;
	}
}
