package com.github.query4j.core;

import java.util.List;

/**
 * Represents a fully built query that can be executed.
 *
 * @param <T> the entity type
 * @since 1.0.0
 */
public interface DynamicQuery<T> {
    List<T> execute();

    T executeOne();

    long executeCount();

    String getSQL();
}
