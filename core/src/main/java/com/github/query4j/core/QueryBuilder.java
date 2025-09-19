package com.github.query4j.core;

import com.github.query4j.core.impl.DynamicQueryBuilder;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Core interface for dynamic query building with fluent API design.
 * 
 * <p>
 * Provides comprehensive support for JPA query construction with advanced
 * features
 * including complex filtering, joins, aggregations, pagination, and caching.
 * </p>
 * 
 * <p>
 * This interface follows the Builder pattern, allowing method chaining for
 * readable and maintainable query construction. All implementations must be
 * immutable to ensure thread safety.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * List<User> users = QueryBuilder.forEntity(User.class)
 *         .where("active", true)
 *         .and()
 *         .where("department", "Engineering")
 *         .orderBy("lastName")
 *         .limit(50)
 *         .cached()
 *         .findAll();
 * }</pre>
 * 
 * @param <T> the entity type this query builder operates on
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface QueryBuilder<T> {

    /**
     * Creates a new QueryBuilder instance for the specified entity class.
     * 
     * @param <T>         the entity type
     * @param entityClass the JPA entity class, must not be null
     * @return a new QueryBuilder instance
     * @throws IllegalArgumentException if entityClass is null
     * @since 1.0.0
     */
    static <T> QueryBuilder<T> forEntity(Class<T> entityClass) {
        if (entityClass == null) {
            throw new IllegalArgumentException("Entity class must not be null");
        }
        return new DynamicQueryBuilder<>(entityClass);
    }

    // ========== WHERE Conditions ==========

    /**
     * Adds an equality condition to the query.
     * 
     * @param fieldName the field name, must not be null or empty
     * @param value     the expected value, may be null
     * @return a new QueryBuilder instance with the condition added
     * @throws IllegalArgumentException if fieldName is null or empty
     * @since 1.0.0
     */
    QueryBuilder<T> where(String fieldName, Object value);

    /**
     * Adds a condition with a specific operator.
     * 
     * @param fieldName the field name, must not be null or empty
     * @param operator  the comparison operator (=, !=, &lt;, &lt;=, &gt;, &gt;=, LIKE), must not be null
     *                  not be null
     * @param value     the comparison value, may be null for IS NULL/IS NOT NULL
     *                  operators
     * @return a new QueryBuilder instance with the condition added
     * @throws IllegalArgumentException if fieldName or operator is null or empty,
     *                                  or operator is invalid
     * @since 1.0.0
     */
    QueryBuilder<T> where(String fieldName, String operator, Object value);

    /**
     * Adds an IN condition for the specified field.
     * 
     * @param fieldName the field name, must not be null or empty
     * @param values    the list of possible values, must not be null or empty
     * @return a new QueryBuilder instance with the condition added
     * @throws IllegalArgumentException if fieldName is null/empty or values is
     *                                  null/empty
     * @since 1.0.0
     */
    QueryBuilder<T> whereIn(String fieldName, List<Object> values);

    /**
     * Adds a NOT IN condition for the specified field.
     * 
     * @param fieldName the field name, must not be null or empty
     * @param values    the list of excluded values, must not be null or empty
     * @return a new QueryBuilder instance with the condition added
     * @throws IllegalArgumentException if fieldName is null/empty or values is
     *                                  null/empty
     * @since 1.0.0
     */
    QueryBuilder<T> whereNotIn(String fieldName, List<Object> values);

    /**
     * Adds a LIKE pattern matching condition.
     * 
     * @param fieldName the field name, must not be null or empty
     * @param pattern   the pattern to match (supports % and _ wildcards), must not
     *                  be null
     * @return a new QueryBuilder instance with the condition added
     * @throws IllegalArgumentException if fieldName or pattern is null or empty
     * @since 1.0.0
     */
    QueryBuilder<T> whereLike(String fieldName, String pattern);

    /**
     * Adds a NOT LIKE pattern matching condition.
     * 
     * @param fieldName the field name, must not be null or empty
     * @param pattern   the pattern to exclude, must not be null
     * @return a new QueryBuilder instance with the condition added
     * @throws IllegalArgumentException if fieldName or pattern is null or empty
     * @since 1.0.0
     */
    QueryBuilder<T> whereNotLike(String fieldName, String pattern);

    /**
     * Adds a BETWEEN range condition.
     * 
     * @param fieldName  the field name, must not be null or empty
     * @param startValue the start value (inclusive), must not be null
     * @param endValue   the end value (inclusive), must not be null
     * @return a new QueryBuilder instance with the condition added
     * @throws IllegalArgumentException if any parameter is null or fieldName is
     *                                  empty
     * @since 1.0.0
     */
    QueryBuilder<T> whereBetween(String fieldName, Object startValue, Object endValue);

    /**
     * Adds an IS NULL condition.
     * 
     * @param fieldName the field name, must not be null or empty
     * @return a new QueryBuilder instance with the condition added
     * @throws IllegalArgumentException if fieldName is null or empty
     * @since 1.0.0
     */
    QueryBuilder<T> whereIsNull(String fieldName);

    /**
     * Adds an IS NOT NULL condition.
     * 
     * @param fieldName the field name, must not be null or empty
     * @return a new QueryBuilder instance with the condition added
     * @throws IllegalArgumentException if fieldName is null or empty
     * @since 1.0.0
     */
    QueryBuilder<T> whereIsNotNull(String fieldName);

    // ========== Logical Operators ==========

    /**
     * Adds an AND logical operator between conditions.
     * 
     * @return a new QueryBuilder instance
     * @since 1.0.0
     */
    QueryBuilder<T> and();

    /**
     * Adds an OR logical operator between conditions.
     * 
     * @return a new QueryBuilder instance
     * @since 1.0.0
     */
    QueryBuilder<T> or();

    /**
     * Adds a NOT logical operator to negate the next condition.
     * 
     * @return a new QueryBuilder instance
     * @since 1.0.0
     */
    QueryBuilder<T> not();

    // ========== Condition Grouping ==========

    /**
     * Opens a new group of conditions (equivalent to opening parentheses).
     * 
     * @return a new QueryBuilder instance
     * @since 1.0.0
     */
    QueryBuilder<T> openGroup();

    /**
     * Closes the current group of conditions (equivalent to closing parentheses).
     * 
     * @return a new QueryBuilder instance
     * @throws IllegalStateException if no group is currently open
     * @since 1.0.0
     */
    QueryBuilder<T> closeGroup();

    // ========== JOIN Operations ==========

    /**
     * Adds an INNER JOIN on the specified association field.
     * 
     * @param associationFieldName the association field name, must not be null or
     *                             empty
     * @return a new QueryBuilder instance
     * @throws IllegalArgumentException if associationFieldName is null or empty
     * @since 1.0.0
     */
    QueryBuilder<T> join(String associationFieldName);

    /**
     * Adds a LEFT JOIN on the specified association field.
     * 
     * @param associationFieldName the association field name, must not be null or
     *                             empty
     * @return a new QueryBuilder instance
     * @throws IllegalArgumentException if associationFieldName is null or empty
     * @since 1.0.0
     */
    QueryBuilder<T> leftJoin(String associationFieldName);

    /**
     * Adds a RIGHT JOIN on the specified association field.
     * 
     * @param associationFieldName the association field name, must not be null or
     *                             empty
     * @return a new QueryBuilder instance
     * @throws IllegalArgumentException if associationFieldName is null or empty
     * @since 1.0.0
     */
    QueryBuilder<T> rightJoin(String associationFieldName);

    /**
     * Adds an INNER JOIN on the specified association field.
     * This is an alias for {@link #join(String)} for improved readability.
     * 
     * @param associationFieldName the association field name, must not be null or
     *                             empty
     * @return a new QueryBuilder instance
     * @throws IllegalArgumentException if associationFieldName is null or empty
     * @since 1.0.0
     */
    QueryBuilder<T> innerJoin(String associationFieldName);

    /**
     * Adds a fetch join to eagerly load the specified association.
     * 
     * @param associationFieldName the association field name, must not be null or
     *                             empty
     * @return a new QueryBuilder instance
     * @throws IllegalArgumentException if associationFieldName is null or empty
     * @since 1.0.0
     */
    QueryBuilder<T> fetch(String associationFieldName);

    // ========== Selection and Aggregation ==========

    /**
     * Specifies which fields to select in the query result.
     * 
     * @param fieldNames the field names to select, must not be null or empty array
     * @return a new QueryBuilder instance
     * @throws IllegalArgumentException if fieldNames array is null, empty, or
     *                                  contains null/empty elements
     * @since 1.0.0
     */
    QueryBuilder<T> select(String... fieldNames);

    /**
     * Adds a COUNT(*) aggregation function.
     * 
     * @return a new QueryBuilder instance
     * @since 1.0.0
     */
    QueryBuilder<T> countAll();

    /**
     * Adds a COUNT(fieldName) aggregation function.
     * 
     * @param fieldName the field to count, must not be null or empty
     * @return a new QueryBuilder instance
     * @throws IllegalArgumentException if fieldName is null or empty
     * @since 1.0.0
     */
    QueryBuilder<T> count(String fieldName);

    /**
     * Adds a SUM aggregation function for numeric fields.
     * 
     * @param numericFieldName the numeric field to sum, must not be null or empty
     * @return a new QueryBuilder instance
     * @throws IllegalArgumentException if numericFieldName is null or empty
     * @since 1.0.0
     */
    QueryBuilder<T> sum(String numericFieldName);

    /**
     * Adds an AVG (average) aggregation function for numeric fields.
     * 
     * @param numericFieldName the numeric field to average, must not be null or
     *                         empty
     * @return a new QueryBuilder instance
     * @throws IllegalArgumentException if numericFieldName is null or empty
     * @since 1.0.0
     */
    QueryBuilder<T> avg(String numericFieldName);

    /**
     * Adds a MIN aggregation function to find minimum value.
     * 
     * @param fieldName the field to find minimum value, must not be null or empty
     * @return a new QueryBuilder instance
     * @throws IllegalArgumentException if fieldName is null or empty
     * @since 1.0.0
     */
    QueryBuilder<T> min(String fieldName);

    /**
     * Adds a MAX aggregation function to find maximum value.
     * 
     * @param fieldName the field to find maximum value, must not be null or empty
     * @return a new QueryBuilder instance
     * @throws IllegalArgumentException if fieldName is null or empty
     * @since 1.0.0
     */
    QueryBuilder<T> max(String fieldName);

    // ========== GROUP BY and HAVING ==========

    /**
     * Adds GROUP BY clause with specified fields.
     * 
     * @param fieldNames the fields to group by, must not be null or empty array
     * @return a new QueryBuilder instance
     * @throws IllegalArgumentException if fieldNames array is null, empty, or
     *                                  contains null/empty elements
     * @since 1.0.0
     */
    QueryBuilder<T> groupBy(String... fieldNames);

    /**
     * Adds a HAVING condition for aggregated results.
     * 
     * @param aggregatedFieldName the aggregated field name, must not be null or
     *                            empty
     * @param operator            the comparison operator, must not be null or empty
     * @param value               the comparison value, may be null
     * @return a new QueryBuilder instance
     * @throws IllegalArgumentException if aggregatedFieldName or operator is null
     *                                  or empty
     * @since 1.0.0
     */
    QueryBuilder<T> having(String aggregatedFieldName, String operator, Object value);

    // ========== ORDER BY ==========

    /**
     * Adds ascending ORDER BY for the specified field.
     * 
     * @param fieldName the field name to sort by, must not be null or empty
     * @return a new QueryBuilder instance
     * @throws IllegalArgumentException if fieldName is null or empty
     * @since 1.0.0
     */
    QueryBuilder<T> orderBy(String fieldName);

    /**
     * Adds descending ORDER BY for the specified field.
     * 
     * @param fieldName the field name to sort by, must not be null or empty
     * @return a new QueryBuilder instance
     * @throws IllegalArgumentException if fieldName is null or empty
     * @since 1.0.0
     */
    QueryBuilder<T> orderByDescending(String fieldName);

    /**
     * Adds ORDER BY with specified direction.
     * 
     * @param fieldName the field name to sort by, must not be null or empty
     * @param ascending true for ASC, false for DESC
     * @return a new QueryBuilder instance
     * @throws IllegalArgumentException if fieldName is null or empty
     * @since 1.0.0
     */
    QueryBuilder<T> orderBy(String fieldName, boolean ascending);

    // ========== Pagination ==========

    /**
     * Sets the maximum number of results to return.
     * 
     * @param maxResults the maximum number of results, must be positive
     * @return a new QueryBuilder instance
     * @throws IllegalArgumentException if maxResults is not positive
     * @since 1.0.0
     */
    QueryBuilder<T> limit(int maxResults);

    /**
     * Sets the number of results to skip.
     * 
     * @param skipCount the number of results to skip, must not be negative
     * @return a new QueryBuilder instance
     * @throws IllegalArgumentException if skipCount is negative
     * @since 1.0.0
     */
    QueryBuilder<T> offset(int skipCount);

    /**
     * Sets pagination using page number and page size.
     * 
     * @param pageNumber the page number (1-based), must be positive
     * @param pageSize   the page size, must be positive
     * @return a new QueryBuilder instance
     * @throws IllegalArgumentException if pageNumber is not positive or pageSize is not
     *                                  positive
     * @since 1.0.0
     */
    QueryBuilder<T> page(int pageNumber, int pageSize);

    // ========== Subqueries ==========

    /**
     * Adds an EXISTS subquery condition.
     * 
     * @param subquery the subquery builder, must not be null
     * @return a new QueryBuilder instance
     * @throws IllegalArgumentException if subquery is null
     * @since 1.0.0
     */
    QueryBuilder<T> exists(QueryBuilder<?> subquery);

    /**
     * Adds a NOT EXISTS subquery condition.
     * 
     * @param subquery the subquery builder, must not be null
     * @return a new QueryBuilder instance
     * @throws IllegalArgumentException if subquery is null
     * @since 1.0.0
     */
    QueryBuilder<T> notExists(QueryBuilder<?> subquery);

    /**
     * Adds an IN subquery condition.
     * 
     * @param fieldName the field name, must not be null or empty
     * @param subquery  the subquery builder, must not be null
     * @return a new QueryBuilder instance
     * @throws IllegalArgumentException if fieldName is null/empty or subquery is
     *                                  null
     * @since 1.0.0
     */
    QueryBuilder<T> in(String fieldName, QueryBuilder<?> subquery);

    /**
     * Adds a NOT IN subquery condition.
     * 
     * @param fieldName the field name, must not be null or empty
     * @param subquery  the subquery builder, must not be null
     * @return a new QueryBuilder instance
     * @throws IllegalArgumentException if fieldName is null/empty or subquery is
     *                                  null
     * @since 1.0.0
     */
    QueryBuilder<T> notIn(String fieldName, QueryBuilder<?> subquery);

    // ========== Custom Functions ==========

    /**
     * Adds a custom database function call.
     * 
     * @param functionName the name of the database function, must not be null or
     *                     empty
     * @param fieldName    the field to apply the function to, must not be null or
     *                     empty
     * @param parameters   additional function parameters, may be empty but not null
     * @return a new QueryBuilder instance
     * @throws IllegalArgumentException if functionName or fieldName is null or
     *                                  empty, or parameters is null
     * @since 1.0.0
     */
    QueryBuilder<T> customFunction(String functionName, String fieldName, Object... parameters);

    // ========== Native Query Support ==========

    /**
     * Executes a native SQL query instead of JPA criteria.
     * 
     * @param sqlQuery the native SQL query string, must not be null or empty
     * @return a new QueryBuilder instance
     * @throws IllegalArgumentException if sqlQuery is null or empty
     * @since 1.0.0
     */
    QueryBuilder<T> nativeQuery(String sqlQuery);

    /**
     * Sets a named parameter for native queries.
     * 
     * @param parameterName  the parameter name, must not be null or empty
     * @param parameterValue the parameter value, may be null
     * @return a new QueryBuilder instance
     * @throws IllegalArgumentException if parameterName is null or empty
     * @since 1.0.0
     */
    QueryBuilder<T> parameter(String parameterName, Object parameterValue);

    /**
     * Sets multiple named parameters for native queries.
     * 
     * @param parameterMap map of parameter names and values, must not be null
     * @return a new QueryBuilder instance
     * @throws IllegalArgumentException if parameterMap is null
     * @since 1.0.0
     */
    QueryBuilder<T> parameters(Map<String, Object> parameterMap);

    // ========== Caching ==========

    /**
     * Enables caching for this query with default settings.
     * 
     * @return a new QueryBuilder instance
     * @since 1.0.0
     */
    QueryBuilder<T> cached();

    /**
     * Enables caching for this query in the specified cache region.
     * 
     * @param cacheRegionName the cache region name, must not be null or empty
     * @return a new QueryBuilder instance
     * @throws IllegalArgumentException if cacheRegionName is null or empty
     * @since 1.0.0
     */
    QueryBuilder<T> cached(String cacheRegionName);

    /**
     * Enables caching for this query with specified time-to-live.
     * 
     * @param timeToLiveSeconds the cache entry TTL in seconds, must not be negative
     * @return a new QueryBuilder instance
     * @throws IllegalArgumentException if timeToLiveSeconds is negative
     * @since 1.0.0
     */
    QueryBuilder<T> cached(long timeToLiveSeconds);

    // ========== Query Hints ==========

    /**
     * Sets a query hint for performance tuning.
     * 
     * @param hintName  the hint name, must not be null or empty
     * @param hintValue the hint value, may be null
     * @return a new QueryBuilder instance
     * @throws IllegalArgumentException if hintName is null or empty
     * @since 1.0.0
     */
    QueryBuilder<T> hint(String hintName, Object hintValue);

    /**
     * Sets the fetch size hint for result processing.
     * 
     * @param fetchSize the fetch size, must be positive
     * @return a new QueryBuilder instance
     * @throws IllegalArgumentException if fetchSize is not positive
     * @since 1.0.0
     */
    QueryBuilder<T> fetchSize(int fetchSize);

    /**
     * Sets the query timeout in seconds.
     * 
     * @param timeoutSeconds the timeout in seconds, must not be negative
     * @return a new QueryBuilder instance
     * @throws IllegalArgumentException if timeoutSeconds is negative
     * @since 1.0.0
     */
    QueryBuilder<T> timeout(int timeoutSeconds);

    // ========== Query Execution ==========

    /**
     * Executes the query and returns all matching results.
     * 
     * @return list of matching entities, never null but may be empty
     * @throws QueryExecutionException if query execution fails
     * @since 1.0.0
     */
    List<T> findAll();

    /**
     * Executes the query and returns the first result, or null if no results.
     * 
     * @return the first matching entity or null if no results found
     * @throws QueryExecutionException if query execution fails
     * @since 1.0.0
     */
    T findOne();

    /**
     * Executes a count query and returns the number of matching results.
     * 
     * @return the count of matching entities, always non-negative
     * @throws QueryExecutionException if query execution fails
     * @since 1.0.0
     */
    long count();

    /**
     * Checks if any results exist for this query.
     * 
     * @return true if at least one result exists, false otherwise
     * @throws QueryExecutionException if query execution fails
     * @since 1.0.0
     */
    boolean exists();

    // ========== Asynchronous Execution ==========

    /**
     * Executes the query asynchronously and returns all matching results.
     * 
     * @return CompletableFuture containing list of matching entities, never null
     * @since 1.0.0
     */
    CompletableFuture<List<T>> findAllAsync();

    /**
     * Executes the query asynchronously and returns the first result.
     * 
     * @return CompletableFuture containing the first matching entity or null
     * @since 1.0.0
     */
    CompletableFuture<T> findOneAsync();

    /**
     * Executes a count query asynchronously.
     * 
     * @return CompletableFuture containing the count of matching entities
     * @since 1.0.0
     */
    CompletableFuture<Long> countAsync();

    // ========== Pagination Results ==========

    /**
     * Executes the query and returns paginated results with metadata.
     * 
     * @return a Page containing results and pagination information, never null
     * @throws QueryExecutionException if query execution fails
     * @since 1.0.0
     */
    Page<T> findPage();

    // ========== Query Building ==========

    /**
     * Builds and returns a DynamicQuery instance without executing it.
     * This allows for query reuse and performance optimization.
     * 
     * @return a DynamicQuery instance representing this builder's state, never null
     * @since 1.0.0
     */
    DynamicQuery<T> build();

    // ========== Debugging and Diagnostics ==========

    /**
     * Generates the SQL representation of this query for debugging purposes.
     * 
     * @return the generated SQL string, never null but may be empty if not
     *         available
     * @since 1.0.0
     */
    String toSQL();

    /**
     * Returns the parameters map for this query.
     * 
     * @return Map containing parameter names and values, never null
     * @since 1.0.0
     */
    Map<String, Object> getParameters();

    /**
     * Returns query execution statistics and performance metrics.
     * 
     * @return QueryStats containing execution information, never null
     * @since 1.0.0
     */
    QueryStats getExecutionStats();
}