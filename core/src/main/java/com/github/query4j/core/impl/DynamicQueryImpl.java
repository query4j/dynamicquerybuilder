package com.github.query4j.core.impl;

import java.util.Collections;
import java.util.List;

import com.github.query4j.core.DynamicQuery;

/**
 * Immutable implementation of DynamicQuery.
 * Encapsulates the built query and provides execution methods.
 *
 * @param <T> the entity type
 * @since 1.0.0
 */
public final class DynamicQueryImpl<T> implements DynamicQuery<T> {

    private final List<T> results;

    private final String sql;

    /**
     * Constructs a new DynamicQueryImpl with the given results and SQL.
     *
     * @param results the query results
     * @param sql the generated SQL query
     */
    public DynamicQueryImpl(List<T> results, String sql) {
        this.results = results;
        this.sql = sql;
    }

    @Override
    public List<T> execute() {
        return results != null ? results : Collections.emptyList();
    }

    @Override
    public T executeOne() {
        if (results == null || results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    @Override
    public long executeCount() {
        return results != null ? results.size() : 0L;
    }

    @Override
    public String getSQL() {
        return sql != null ? sql : "";
    }

    /**
     * Gets the results list.
     * @return the results list
     */
    public List<T> getResults() {
        return results;
    }

    /**
     * Gets the SQL string.
     * @return the SQL string
     */
    public String getSql() {
        return sql;
    }
}
