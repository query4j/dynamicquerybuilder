package com.github.query4j.core;

/**
 * Exception thrown when a query execution fails unexpectedly.
 * 
 * <p>
 * This unchecked exception wraps any lower level exceptions thrown
 * during query building or execution phases. It extends DynamicQueryException
 * to maintain consistency with the Query4j exception hierarchy.
 * </p>
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
     * @param message the detail message, may be null
     */
    public QueryExecutionException(String message) {
        super(message);
    }

    /**
     * Constructs a new QueryExecutionException with the specified
     * detail message and cause.
     * 
     * @param message the detail message, may be null
     * @param cause   the cause (which is saved for later retrieval), may be null
     */
    public QueryExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new QueryExecutionException with the specified cause.
     * 
     * @param cause the cause (which is saved for later retrieval), may be null
     */
    public QueryExecutionException(Throwable cause) {
        super(cause);
    }
}
