package com.github.query4j.core.impl;

import java.util.Collections;
import java.util.List;

import com.github.query4j.core.DynamicQuery;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Immutable implementation of DynamicQuery.
 * Encapsulates the built query and provides execution methods.
 *
 * @param <T> the entity type
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class DynamicQueryImpl<T> implements DynamicQuery<T> {

    @Getter
    private final List<T> results;

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
        return results != null ? results.size() : 0L;
    }

    @Override
    public String getSQL() {
        return sql != null ? sql : "";
    }
}
