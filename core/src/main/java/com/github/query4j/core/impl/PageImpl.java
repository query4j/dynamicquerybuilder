package com.github.query4j.core.impl;

import com.github.query4j.core.Page;

import java.util.Collections;
import java.util.List;

/**
 * Immutable implementation of Page interface using Java 17 record.
 * Represents a paginated set of results with metadata.
 *
 * @param <T> the entity type
 * @param content the list of entities in this page
 * @param number the page number (zero-based)
 * @param size the size of the page
 * @param totalElements the total number of elements across all pages
 * @since 1.0.0
 */
public record PageImpl<T>(
    List<T> content,
    int number,
    int size,
    long totalElements
) implements Page<T> {

    /**
     * Constructs a PageImpl with validation.
     * 
     * @param content the list of entities in this page
     * @param number the page number (zero-based)
     * @param size the size of the page
     * @param totalElements the total number of elements across all pages
     */
    public PageImpl {
        // Defensive copy to ensure immutability
        content = content == null ? Collections.emptyList() : List.copyOf(content);
        
        // Basic validation
        if (number < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size < 0) {
            throw new IllegalArgumentException("Page size cannot be negative");
        }
        if (totalElements < 0) {
            throw new IllegalArgumentException("Total elements cannot be negative");
        }
    }

    @Override
    public List<T> getContent() {
        return content;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public long getTotalElements() {
        return totalElements;
    }

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
