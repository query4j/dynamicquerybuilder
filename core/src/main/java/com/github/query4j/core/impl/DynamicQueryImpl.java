package com.github.query4j.core.impl;

import com.github.query4j.core.DynamicQuery;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

package com.github.query4j.core.impl;

import com.github.query4j.core.DynamicQuery;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

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

    public DynamicQueryImpl(List<T> results, String sql) {
        this.results = (results == null)
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(results));
        this.sql = (sql == null) ? "" : sql;
    }

    @Override
    public List<T> execute() {
        return results;
    }

    @Override
    public T executeOne() {
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    @Override
    public long executeCount() {
        return results.size();
    }

    @Override
    public String getSQL() {
        return sql;
    }
}
@RequiredArgsConstructor
public final class DynamicQueryImpl<T> implements DynamicQuery<T> {

    @Getter
    private final List<T> results;

    @Getter
    private final String sql;

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
        return results != null ? results.size() : 0;
    }

    @Override
    public String getSQL() {
        return sql == null ? "" : sql;
    }
}
