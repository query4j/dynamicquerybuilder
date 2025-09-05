package com.github.query4j.core;

import java.util.List;

/**
 * Interface representing a paginated result set.
 *
 * @param <T> the entity type
 * @since 1.0.0
 */
public interface Page<T> {
    List<T> getContent();

    int getNumber();

    int getSize();

    long getTotalElements();

    int getTotalPages();

    boolean hasNext();

    boolean hasPrevious();

    boolean isFirst();

    boolean isLast();
}
