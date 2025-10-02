package com.github.query4j.core;

import java.util.List;

/**
 * Represents a fully built, reusable query that can be executed multiple times.
 * 
 * <p>
 * This interface represents the result of calling {@link QueryBuilder#build()},
 * providing a compiled query object that can be executed efficiently. Unlike
 * the builder interface, DynamicQuery instances are designed for reuse and
 * can be stored, cached, and executed multiple times with consistent behavior.
 * </p>
 * 
 * <p>
 * All DynamicQuery implementations are immutable and thread-safe, allowing
 * safe concurrent execution from multiple threads. This makes them ideal for
 * scenarios where the same query needs to be executed repeatedly, such as in
 * batch processing or scheduled tasks.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * // Build a reusable query
 * DynamicQuery<User> activeUsersQuery = QueryBuilder.forEntity(User.class)
 *     .where("active", true)
 *     .orderBy("lastName")
 *     .build();
 * 
 * // Execute multiple times
 * List<User> batch1 = activeUsersQuery.execute();
 * // ... process batch1 ...
 * 
 * List<User> batch2 = activeUsersQuery.execute(); // Executes same query again
 * 
 * // Get single result
 * User firstActive = activeUsersQuery.executeOne();
 * 
 * // Count without loading entities
 * long totalActive = activeUsersQuery.executeCount();
 * 
 * // Inspect generated SQL for debugging
 * System.out.println("SQL: " + activeUsersQuery.getSQL());
 * }</pre>
 *
 * @param <T> the entity type this query returns
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface DynamicQuery<T> {
    
    /**
     * Executes the query and returns all matching entities.
     * 
     * <p>
     * This method executes the complete query and loads all matching entities
     * into memory. For large result sets, consider using pagination via
     * {@link QueryBuilder#page(int, int)} instead.
     * </p>
     * 
     * <p>
     * The returned list is immutable and contains all entities matching the
     * query criteria. The list may be empty if no entities match, but will
     * never be null.
     * </p>
     * 
     * @return list of all matching entities, never null but may be empty
     * @throws QueryExecutionException if query execution fails
     */
    List<T> execute();

    /**
     * Executes the query and returns exactly one result.
     * 
     * <p>
     * This method is intended for queries that are expected to return exactly
     * one result, such as queries with unique constraints or single-entity
     * lookups. It will throw an exception if zero or multiple results are found.
     * </p>
     * 
     * <p>
     * For queries that may return zero or one result, consider using
     * {@code execute()} and checking the list size, or catching the exception.
     * </p>
     * 
     * @return the single matching entity, never null
     * @throws QueryExecutionException if query execution fails, or if zero or
     *                                  multiple results are found
     */
    T executeOne();

    /**
     * Executes a count query and returns the number of matching entities.
     * 
     * <p>
     * This method executes a COUNT query without loading any entities into memory,
     * making it efficient for checking result set sizes. This is particularly
     * useful for pagination total counts or validation logic.
     * </p>
     * 
     * <p>
     * The generated SQL transforms the query into a {@code SELECT COUNT(*)}
     * variant, stripping out unnecessary SELECT clauses while preserving
     * WHERE, JOIN, and other filtering logic.
     * </p>
     * 
     * @return the count of matching entities, always &gt;= 0
     * @throws QueryExecutionException if query execution fails
     */
    long executeCount();

    /**
     * Returns the SQL representation of this query for debugging purposes.
     * 
     * <p>
     * The returned SQL string is the actual query that will be executed,
     * including all WHERE clauses, JOINs, ORDER BY, and pagination. Parameter
     * placeholders are included but not replaced with actual values.
     * </p>
     * 
     * <p>
     * This method is primarily intended for debugging, logging, and query
     * analysis. The exact SQL format may vary depending on the underlying
     * JPA provider and database dialect.
     * </p>
     * 
     * @return the SQL query string, never null
     */
    String getSQL();
}
