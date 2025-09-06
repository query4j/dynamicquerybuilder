package com.github.query4j.core.criteria;

import java.util.Map;

/**
 * Represents a query predicate that can be serialized to SQL.
 * All implementations must be immutable and thread-safe.
 * 
 * @since 1.0.0
 */
public interface Predicate {
    /**
     * Converts this predicate to SQL string representation.
     * 
     * @return SQL string
     */
    String toSQL();

    /**
     * Gets parameter names used in this predicate.
     * 
     * @return map of parameter names to values
     */
    Map<String, Object> getParameters();
}
