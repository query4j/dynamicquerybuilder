package com.github.query4j.optimizer;

import com.github.query4j.core.DynamicQueryException;

/**
 * Exception thrown when query optimization analysis fails.
 * Extends DynamicQueryException to maintain consistency with the library's
 * exception hierarchy.
 *
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
public class OptimizationException extends DynamicQueryException {
    
    /**
     * Constructs a new OptimizationException with the specified detail message.
     *
     * @param message the detail message
     */
    public OptimizationException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new OptimizationException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public OptimizationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new OptimizationException with the specified cause.
     *
     * @param cause the cause
     */
    public OptimizationException(Throwable cause) {
        super(cause);
    }
}