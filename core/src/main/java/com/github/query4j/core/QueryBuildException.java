package com.github.query4j.core;

/**
 * Exception thrown when a query building operation fails due to invalid input,
 * unsupported syntax, or validation problems.
 * 
 * <p>
 * This exception is thrown during the query construction phase when:
 * </p>
 * <ul>
 * <li>Invalid predicate parameters are provided</li>
 * <li>Field names contain invalid characters</li>
 * <li>Unsupported operators or syntax are used</li>
 * <li>Required parameters are null or empty</li>
 * <li>Logical predicate validation fails</li>
 * </ul>
 * 
 * <p>
 * All instances carry full context including input values, offending code,
 * and possible remediation instructions for deterministic debugging.
 * </p>
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
public class QueryBuildException extends DynamicQueryException {

    /**
     * Serialization version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new QueryBuildException with the specified detail message.
     * 
     * @param message the detail message, may be null
     */
    public QueryBuildException(String message) {
        super(message);
    }

    /**
     * Constructs a new QueryBuildException with the specified
     * detail message and cause.
     * 
     * @param message the detail message, may be null
     * @param cause   the cause (which is saved for later retrieval), may be null
     */
    public QueryBuildException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new QueryBuildException with the specified cause.
     * 
     * @param cause the cause (which is saved for later retrieval), may be null
     */
    public QueryBuildException(Throwable cause) {
        super(cause);
    }
}