package com.github.query4j.core.criteria;

import java.util.Map;

/**
 * Represents a query predicate that can be serialized to SQL. All
 * implementations must be immutable and thread-safe.
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
	 * Returns an immutable map of parameter bindings for this predicate. Keys are
	 * the named placeholders (without the ':' prefix) used by {@link #toSQL()} and
	 * must be unique within the entire composed predicate tree. Implementations
	 * must never return null.
	 *
	 * @return immutable map of parameter names to bound values
	 */
	Map<String, Object> getParameters();
}
