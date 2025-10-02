package com.github.query4j.core;

/**
 * Base exception for all dynamic query builder errors.
 * This unchecked exception serves as the root of the custom exception hierarchy
 * for the Query4j library, providing a common base for all query-related exceptions.
 * 
 * <p>
 * This exception follows the Query4j error handling conventions and provides
 * full context for debugging with meaningful error messages and cause chaining.
 * </p>
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
public class DynamicQueryException extends RuntimeException {

    /**
     * Serialization version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new DynamicQueryException with the specified detail message.
     * 
     * @param message the detail message, may be null
     */
    public DynamicQueryException(String message) {
        super(message);
    }

    /**
     * Constructs a new DynamicQueryException with the specified
     * detail message and cause.
     * 
     * @param message the detail message, may be null
     * @param cause   the cause (which is saved for later retrieval), may be null
     */
    public DynamicQueryException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new DynamicQueryException with the specified cause.
     * 
     * @param cause the cause (which is saved for later retrieval), may be null
     */
    public DynamicQueryException(Throwable cause) {
        super(cause);
    }
}