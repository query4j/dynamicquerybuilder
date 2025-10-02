package com.github.query4j.core;

/**
 * Exception thrown when query execution fails due to runtime or database errors.
 * 
 * <p>
 * This exception is thrown during the query execution phase when:
 * </p>
 * <ul>
 * <li>Database connectivity issues occur</li>
 * <li>SQL syntax errors are detected at runtime</li>
 * <li>Constraint violations occur (unique, foreign key, etc.)</li>
 * <li>Transaction deadlocks or timeouts happen</li>
 * <li>Entity mapping failures occur</li>
 * <li>Permission or security violations are detected</li>
 * </ul>
 * 
 * <p>
 * Unlike {@link QueryBuildException}, which indicates problems during query
 * construction, QueryExecutionException wraps runtime failures that occur
 * when attempting to execute a valid query against the database.
 * </p>
 * 
 * <p>
 * This unchecked exception wraps any lower-level exceptions thrown during
 * query execution, such as {@code SQLException}, {@code PersistenceException},
 * or JPA provider-specific exceptions. The original cause is always preserved
 * via {@link #getCause()} for detailed debugging.
 * </p>
 * 
 * <p>
 * Example handling:
 * </p>
 * 
 * <pre>{@code
 * try {
 *     List<User> users = QueryBuilder.forEntity(User.class)
 *         .where("active", true)
 *         .findAll();
 * } catch (QueryExecutionException e) {
 *     logger.error("Query execution failed: {}", e.getMessage(), e);
 *     // Handle database error appropriately
 *     if (e.getCause() instanceof SQLException) {
 *         // Specific handling for SQL errors
 *     }
 * }
 * }</pre>
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
public class QueryExecutionException extends DynamicQueryException {

    /**
     * Serialization version UID.
     */
    private static final long serialVersionUID = 2036606562622219299L;

    /**
     * Constructs a new QueryExecutionException with the specified detail message.
     * 
     * @param message the detail message describing the execution failure, may be null
     */
    public QueryExecutionException(String message) {
        super(message);
    }

    /**
     * Constructs a new QueryExecutionException with the specified
     * detail message and cause.
     * 
     * <p>
     * This is the most commonly used constructor as it preserves the original
     * exception that caused the query execution failure.
     * </p>
     * 
     * @param message the detail message describing the execution failure, may be null
     * @param cause   the underlying cause of the failure (e.g., SQLException), may be null
     */
    public QueryExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new QueryExecutionException with the specified cause.
     * 
     * <p>
     * The detail message will be derived from the cause's message.
     * </p>
     * 
     * @param cause the underlying cause of the failure, may be null
     */
    public QueryExecutionException(Throwable cause) {
        super(cause);
    }
}