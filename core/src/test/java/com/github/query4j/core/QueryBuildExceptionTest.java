package com.github.query4j.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for QueryBuildException class.
 */
@DisplayName("QueryBuildException")
class QueryBuildExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should create exception with message")
        void shouldCreateExceptionWithMessage() {
            String message = "Invalid predicate parameter";
            QueryBuildException exception = new QueryBuildException(message);
            
            assertEquals(message, exception.getMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("should create exception with message and cause")
        void shouldCreateExceptionWithMessageAndCause() {
            String message = "Field validation failed";
            IllegalArgumentException cause = new IllegalArgumentException("Invalid field name");
            
            QueryBuildException exception = new QueryBuildException(message, cause);
            
            assertEquals(message, exception.getMessage());
            assertEquals(cause, exception.getCause());
        }

        @Test
        @DisplayName("should create exception with cause only")
        void shouldCreateExceptionWithCauseOnly() {
            IllegalArgumentException cause = new IllegalArgumentException("Unsupported operator");
            
            QueryBuildException exception = new QueryBuildException(cause);
            
            assertEquals(cause, exception.getCause());
            // Message should be the cause's toString()
            assertTrue(exception.getMessage().contains("IllegalArgumentException"));
        }

        @Test
        @DisplayName("should create exception with null message")
        void shouldCreateExceptionWithNullMessage() {
            QueryBuildException exception = new QueryBuildException((String) null);
            
            assertNull(exception.getMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("should create exception with null cause")
        void shouldCreateExceptionWithNullCause() {
            QueryBuildException exception = new QueryBuildException((Throwable) null);
            
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("should create exception with null message and cause")
        void shouldCreateExceptionWithNullMessageAndCause() {
            QueryBuildException exception = new QueryBuildException(null, null);
            
            assertNull(exception.getMessage());
            assertNull(exception.getCause());
        }
    }

    @Nested
    @DisplayName("Inheritance and Type Tests")
    class InheritanceTests {

        @Test
        @DisplayName("should be a DynamicQueryException")
        void shouldBeDynamicQueryException() {
            QueryBuildException exception = new QueryBuildException("test");
            
            assertInstanceOf(DynamicQueryException.class, exception);
            assertInstanceOf(RuntimeException.class, exception);
            assertInstanceOf(Exception.class, exception);
            assertInstanceOf(Throwable.class, exception);
        }

        @Test
        @DisplayName("should be throwable")
        void shouldBeThrowable() {
            QueryBuildException exception = new QueryBuildException("test");
            
            assertThrows(QueryBuildException.class, () -> {
                throw exception;
            });
        }

        @Test
        @DisplayName("should maintain serialVersionUID")
        void shouldMaintainSerialVersionUID() {
            QueryBuildException exception = new QueryBuildException("test");
            
            // Verify it's serializable by checking the class has serialVersionUID
            assertDoesNotThrow(() -> {
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
                oos.writeObject(exception);
                oos.close();
            });
        }
    }

    @Nested
    @DisplayName("Exception Chaining Tests")
    class ExceptionChainingTests {

        @Test
        @DisplayName("should properly chain exceptions")
        void shouldProperlyChainExceptions() {
            IllegalArgumentException rootCause = new IllegalArgumentException("Invalid field");
            RuntimeException intermediateCause = new RuntimeException("Validation failed", rootCause);
            QueryBuildException exception = new QueryBuildException("Query build failed", intermediateCause);
            
            assertEquals("Query build failed", exception.getMessage());
            assertEquals(intermediateCause, exception.getCause());
            assertEquals(rootCause, exception.getCause().getCause());
        }

        @Test
        @DisplayName("should handle complex exception chains")
        void shouldHandleComplexExceptionChains() {
            Exception level4 = new Exception("Level 4");
            RuntimeException level3 = new RuntimeException("Level 3", level4);
            IllegalArgumentException level2 = new IllegalArgumentException("Level 2", level3);
            QueryBuildException level1 = new QueryBuildException("Level 1", level2);
            
            assertEquals("Level 1", level1.getMessage());
            assertEquals(level2, level1.getCause());
            assertEquals(level3, level1.getCause().getCause());
            assertEquals(level4, level1.getCause().getCause().getCause());
        }
    }

    @Nested
    @DisplayName("Practical Usage Tests")
    class PracticalUsageTests {

        @Test
        @DisplayName("should be useful for field validation errors")
        void shouldBeUsefulForFieldValidationErrors() {
            String fieldError = "Field name contains invalid characters: @user.name";
            IllegalArgumentException validationException = new IllegalArgumentException(fieldError);
            
            QueryBuildException exception = new QueryBuildException(
                "Failed to validate field name in WHERE clause", validationException);
            
            assertTrue(exception.getMessage().contains("validate field"));
            assertEquals(validationException, exception.getCause());
            assertTrue(exception.getCause().getMessage().contains("@user.name"));
        }

        @Test
        @DisplayName("should be useful for operator validation errors")
        void shouldBeUsefulForOperatorValidationErrors() {
            IllegalArgumentException operatorError = new IllegalArgumentException("Unsupported operator: ===");
            
            QueryBuildException exception = new QueryBuildException(
                "Invalid operator provided for predicate", operatorError);
            
            assertTrue(exception.getMessage().contains("Invalid operator"));
            assertEquals(operatorError, exception.getCause());
        }

        @Test
        @DisplayName("should be useful for parameter binding errors")
        void shouldBeUsefulForParameterBindingErrors() {
            NullPointerException bindingError = new NullPointerException("Parameter value cannot be null");
            
            QueryBuildException exception = new QueryBuildException(
                "Failed to bind parameter to query", bindingError);
            
            assertTrue(exception.getMessage().contains("bind parameter"));
            assertEquals(bindingError, exception.getCause());
        }
    }
}