package com.github.query4j.core;

import java.util.Map;

/**
 * Holds execution metadata and performance diagnostics for query operations.
 * 
 * <p>
 * Provides comprehensive performance and diagnostic information about query
 * execution, including timing, result counts, generated SQL, caching status,
 * and custom hints. This interface is essential for monitoring, debugging,
 * and optimizing query performance in production environments.
 * </p>
 * 
 * <p>
 * All QueryStats implementations are immutable snapshots captured at query
 * execution time. They are thread-safe and can be safely shared or stored
 * for historical analysis.
 * </p>
 * 
 * <p>
 * Performance targets:
 * </p>
 * <ul>
 * <li>Basic queries: &lt; 1ms execution time</li>
 * <li>Moderate queries: &lt; 2ms execution time</li>
 * <li>Complex queries: &lt; 5ms execution time</li>
 * <li>Cached queries: &lt; 0.1ms for cache hits</li>
 * </ul>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * // Execute query with stats tracking
 * QueryBuilder<User> builder = QueryBuilder.forEntity(User.class)
 *     .where("active", true)
 *     .orderBy("lastName");
 * 
 * List<User> users = builder.findAll();
 * QueryStats stats = builder.getStats();
 * 
 * // Analyze performance
 * System.out.println("Execution time: " + stats.getExecutionTimeMs() + "ms");
 * System.out.println("Results: " + stats.getResultCount());
 * System.out.println("Cache hit: " + stats.wasCacheHit());
 * System.out.println("SQL: " + stats.getGeneratedSQL());
 * 
 * // Check performance targets
 * if (stats.getExecutionTimeMs() > 5) {
 *     logger.warn("Slow query detected: {}", stats.getGeneratedSQL());
 * }
 * }</pre>
 *
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface QueryStats {
    
    /**
     * Returns the total execution time in milliseconds.
     * 
     * <p>
     * Measures the complete query execution time from invocation to result
     * retrieval, including database round-trip time, result set processing,
     * and object mapping. Does not include time spent in application code
     * before or after the query execution.
     * </p>
     * 
     * <p>
     * For cached queries, this represents the cache lookup time (typically
     * sub-millisecond). For non-cached queries, this includes full database
     * execution time.
     * </p>
     * 
     * @return execution time in milliseconds, always &gt;= 0
     */
    long getExecutionTimeMs();

    /**
     * Returns the number of entities returned by the query.
     * 
     * <p>
     * For list queries, this is the size of the result list. For single-entity
     * queries, this is 1 if an entity was found, or 0 if no entity matched.
     * For count queries, this represents the count result.
     * </p>
     * 
     * @return the number of results, always &gt;= 0
     */
    int getResultCount();

    /**
     * Returns the SQL query that was executed.
     * 
     * <p>
     * This is the actual SQL sent to the database, including all WHERE clauses,
     * JOINs, ORDER BY, and pagination. Parameter placeholders are included but
     * not replaced with actual values for security and privacy reasons.
     * </p>
     * 
     * <p>
     * The exact format depends on the JPA provider and database dialect. This
     * is primarily useful for debugging and query analysis.
     * </p>
     * 
     * @return the generated SQL query, never null
     */
    String getGeneratedSQL();

    /**
     * Returns custom hints or metadata associated with this query execution.
     * 
     * <p>
     * Hints provide additional context about query execution, such as optimizer
     * suggestions, cache configuration, or custom application metadata. The
     * specific hints available depend on the query configuration and execution
     * context.
     * </p>
     * 
     * <p>
     * Common hint keys include:
     * </p>
     * <ul>
     * <li>{@code "optimizer.suggestions"} - List of optimization suggestions</li>
     * <li>{@code "cache.ttl"} - Cache time-to-live in seconds</li>
     * <li>{@code "query.complexity"} - Estimated query complexity score</li>
     * </ul>
     * 
     * @return map of hints, never null but may be empty
     */
    Map<String, Object> getHints();

    /**
     * Returns whether this query result was served from cache.
     * 
     * <p>
     * When true, indicates that the query did not hit the database and was
     * served from the in-memory cache. Cache hits are typically 10-100x faster
     * than database queries.
     * </p>
     * 
     * <p>
     * Cache hits are only possible when caching is explicitly enabled via
     * {@link QueryBuilder#cached()} or similar methods.
     * </p>
     * 
     * @return true if this was a cache hit, false if database was queried
     */
    boolean wasCacheHit();

    /**
     * Returns the timestamp when this query was executed.
     * 
     * <p>
     * The timestamp is in milliseconds since the Unix epoch (January 1, 1970,
     * 00:00:00 GMT), suitable for use with {@link java.time.Instant#ofEpochMilli(long)}.
     * </p>
     * 
     * <p>
     * This timestamp represents the start of query execution and can be used
     * for time-series analysis, monitoring, and correlation with other system
     * events.
     * </p>
     * 
     * @return execution timestamp in milliseconds since epoch, always &gt; 0
     */
    long getExecutionTimestamp();
}