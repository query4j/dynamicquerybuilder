package com.github.query4j.optimizer;

import com.github.query4j.core.DynamicQueryException;

/**
 * Exception thrown when query optimization analysis fails unexpectedly.
 * 
 * <p>
 * This exception indicates that the optimizer encountered an error during
 * query analysis that prevented it from generating optimization suggestions.
 * Unlike query execution exceptions, this occurs during the analysis phase
 * before any database interaction.
 * </p>
 * 
 * <p>
 * Common causes include:
 * </p>
 * <ul>
 * <li>Invalid or unparseable SQL syntax in raw SQL analysis</li>
 * <li>Null or invalid query builder instances</li>
 * <li>Internal optimizer configuration errors</li>
 * <li>Timeout during complex query analysis</li>
 * <li>Resource exhaustion during optimization</li>
 * </ul>
 * 
 * <p>
 * This exception extends {@link DynamicQueryException} to maintain consistency
 * with the library's exception hierarchy. The original cause is always preserved
 * for debugging purposes.
 * </p>
 * 
 * <p>
 * Example handling:
 * </p>
 * 
 * <pre>{@code
 * try {
 *     QueryOptimizer optimizer = QueryOptimizer.create();
 *     OptimizationResult result = optimizer.optimize(queryBuilder);
 * } catch (OptimizationException e) {
 *     logger.warn("Query optimization failed: {}", e.getMessage());
 *     // Proceed without optimization or use fallback strategy
 * }
 * }</pre>
 *
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
public class OptimizationException extends DynamicQueryException {
    
    /**
     * Serialization version UID.
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructs a new OptimizationException with the specified detail message.
     *
     * @param message the detail message describing the optimization failure, may be null
     */
    public OptimizationException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new OptimizationException with the specified detail message and cause.
     * 
     * <p>
     * This is the most commonly used constructor as it preserves both the
     * descriptive message and the underlying cause of the failure.
     * </p>
     *
     * @param message the detail message describing the optimization failure, may be null
     * @param cause the underlying cause of the failure, may be null
     */
    public OptimizationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new OptimizationException with the specified cause.
     * 
     * <p>
     * The detail message will be derived from the cause's message.
     * </p>
     *
     * @param cause the underlying cause of the failure, may be null
     */
    public OptimizationException(Throwable cause) {
        super(cause);
    }
}