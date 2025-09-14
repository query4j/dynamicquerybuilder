package com.github.query4j.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the complete custom exception hierarchy.
 * Validates that the exception hierarchy works correctly together,
 * including catch-all behavior and proper inheritance.
 */
@DisplayName("Exception Hierarchy Integration")
class ExceptionHierarchyIntegrationTest {

    @Test
    @DisplayName("should catch all Query4j exceptions with DynamicQueryException")
    void shouldCatchAllQuery4jExceptionsWithBaseException() {
        // Test that DynamicQueryException can catch both build and execution exceptions
        
        // Test QueryBuildException
        try {
            throw new QueryBuildException("Build failed");
        } catch (DynamicQueryException e) {
            assertTrue(e instanceof QueryBuildException);
            assertEquals("Build failed", e.getMessage());
        }
        
        // Test QueryExecutionException
        try {
            throw new QueryExecutionException("Execution failed");
        } catch (DynamicQueryException e) {
            assertTrue(e instanceof QueryExecutionException);
            assertEquals("Execution failed", e.getMessage());
        }
        
        // Test base DynamicQueryException
        try {
            throw new DynamicQueryException("Generic error");
        } catch (DynamicQueryException e) {
            assertEquals(DynamicQueryException.class, e.getClass());
            assertEquals("Generic error", e.getMessage());
        }
    }

    @Test
    @DisplayName("should maintain proper exception hierarchy")
    void shouldMaintainProperExceptionHierarchy() {
        QueryBuildException buildException = new QueryBuildException("test");
        QueryExecutionException executionException = new QueryExecutionException("test");
        DynamicQueryException baseException = new DynamicQueryException("test");
        
        // Verify inheritance hierarchy
        assertTrue(buildException instanceof DynamicQueryException);
        assertTrue(buildException instanceof RuntimeException);
        assertTrue(buildException instanceof Exception);
        assertTrue(buildException instanceof Throwable);
        
        assertTrue(executionException instanceof DynamicQueryException);
        assertTrue(executionException instanceof RuntimeException);
        assertTrue(executionException instanceof Exception);
        assertTrue(executionException instanceof Throwable);
        
        assertTrue(baseException instanceof RuntimeException);
        assertTrue(baseException instanceof Exception);
        assertTrue(baseException instanceof Throwable);
        
        // Verify specific type checks
        assertFalse(baseException instanceof QueryBuildException);
        assertFalse(baseException instanceof QueryExecutionException);
        // QueryBuildException and QueryExecutionException are siblings, not related
    }

    @Test
    @DisplayName("should handle chained exceptions across hierarchy")
    void shouldHandleChainedExceptionsAcrossHierarchy() {
        // Create exception chain using different types
        IllegalArgumentException rootCause = new IllegalArgumentException("Invalid input");
        QueryBuildException buildCause = new QueryBuildException("Build failed", rootCause);
        QueryExecutionException topLevel = new QueryExecutionException("Execution failed", buildCause);
        
        // Verify the chain
        assertEquals("Execution failed", topLevel.getMessage());
        assertEquals(buildCause, topLevel.getCause());
        assertEquals(rootCause, topLevel.getCause().getCause());
        
        // Should be able to catch with base exception
        try {
            throw topLevel;
        } catch (DynamicQueryException e) {
            assertTrue(e instanceof QueryExecutionException);
            assertTrue(e.getCause() instanceof QueryBuildException);
            assertTrue(e.getCause() instanceof DynamicQueryException);
            assertTrue(e.getCause().getCause() instanceof IllegalArgumentException);
        }
    }

    @Test
    @DisplayName("should preserve exception details in multi-level hierarchy")
    void shouldPreserveExceptionDetailsInMultiLevelHierarchy() {
        // Test with realistic scenario
        Exception sqlError = new RuntimeException("ORA-00942: table or view does not exist");
        QueryExecutionException executionError = new QueryExecutionException(
            "Failed to execute query on USERS table", sqlError);
        QueryBuildException wrapperError = new QueryBuildException(
            "Query validation failed during execution", executionError);
        
        // Verify all details are preserved
        assertEquals("Query validation failed during execution", wrapperError.getMessage());
        assertTrue(wrapperError.getCause() instanceof QueryExecutionException);
        assertTrue(wrapperError.getCause().getMessage().contains("USERS table"));
        assertTrue(wrapperError.getCause().getCause().getMessage().contains("ORA-00942"));
        
        // Should be catchable at any level
        try {
            throw wrapperError;
        } catch (DynamicQueryException e) {
            // Can catch with base class
            assertTrue(e instanceof QueryBuildException);
        }
    }

    @Test
    @DisplayName("should support polymorphic exception handling")
    void shouldSupportPolymorphicExceptionHandling() {
        DynamicQueryException[] exceptions = {
            new DynamicQueryException("Base error"),
            new QueryBuildException("Build error"),
            new QueryExecutionException("Execution error")
        };
        
        // Should be able to handle all uniformly
        for (DynamicQueryException exception : exceptions) {
            assertNotNull(exception.getMessage());
            assertTrue(exception instanceof DynamicQueryException);
            assertTrue(exception instanceof RuntimeException);
            
            // Verify can be thrown and caught
            try {
                throw exception;
            } catch (DynamicQueryException caught) {
                assertEquals(exception.getClass(), caught.getClass());
                assertEquals(exception.getMessage(), caught.getMessage());
            }
        }
    }
}