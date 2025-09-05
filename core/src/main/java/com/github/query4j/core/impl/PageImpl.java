package com.github.query4j.core.impl;

import com.github.query4j.core.Page;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * Immutable implementation of Page interface.
 * Represents a paginated set of results with metadata.
 *
 * @param <T> the entity type
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class PageImpl<T> implements Page<T> {

    @Getter
    private final List<T> content;

    @Getter
    private final int number;  // zero-based page index

    @Getter
    private final int size;    // page size

    @Getter
    private final long totalElements;

    @Override
    public int getTotalPages() {
        if (size <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalElements / size);
    }

    @Override
    public boolean hasNext() {
        return number + 1 < getTotalPages();
    }

    @Override
    public boolean hasPrevious() {
        return number > 0;
    }

    @Override
    public boolean isFirst() {
        return number == 0;
    }

    @Override
    public boolean isLast() {
        return !hasNext();
    }

    /**
     * Creates an empty page instance.
     *
     * @param <T> the entity type
     * @return an empty page
     */
    public static <T> Page<T> empty() {
        return new PageImpl<>(Collections.emptyList(), 0, 0, 0);
    }
}
