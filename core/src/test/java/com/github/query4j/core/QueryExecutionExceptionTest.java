package com.github.query4j.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for QueryExecutionException class.
 */
@DisplayName("QueryExecutionException")
class QueryExecutionExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should create exception with message")
        void shouldCreateExceptionWithMessage() {
            String message = "Query execution failed";
            QueryExecutionException exception = new QueryExecutionException(message);
            
            assertEquals(message, exception.getMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("should create exception with message and cause")
        void shouldCreateExceptionWithMessageAndCause() {
            String message = "Query execution failed";
            RuntimeException cause = new RuntimeException("Database connection lost");
            
            QueryExecutionException exception = new QueryExecutionException(message, cause);
            
            assertEquals(message, exception.getMessage());
            assertEquals(cause, exception.getCause());
        }

        @Test
        @DisplayName("should create exception with cause only")
        void shouldCreateExceptionWithCauseOnly() {
            RuntimeException cause = new RuntimeException("Database connection lost");
            
            QueryExecutionException exception = new QueryExecutionException(cause);
            
            assertEquals(cause, exception.getCause());
            // Message should be the cause's toString()
            assertTrue(exception.getMessage().contains("RuntimeException"));
        }

        @Test
        @DisplayName("should create exception with null message")
        void shouldCreateExceptionWithNullMessage() {
            QueryExecutionException exception = new QueryExecutionException((String) null);
            
            assertNull(exception.getMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("should create exception with null cause")
        void shouldCreateExceptionWithNullCause() {
            QueryExecutionException exception = new QueryExecutionException((Throwable) null);
            
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("should create exception with null message and cause")
        void shouldCreateExceptionWithNullMessageAndCause() {
            QueryExecutionException exception = new QueryExecutionException(null, null);
            
            assertNull(exception.getMessage());
            assertNull(exception.getCause());
        }
    }

    @Nested
    @DisplayName("Inheritance and Type Tests")
    class InheritanceTests {

        @Test
        @DisplayName("should be a RuntimeException")
        void shouldBeRuntimeException() {
            QueryExecutionException exception = new QueryExecutionException("test");
            
            assertInstanceOf(RuntimeException.class, exception);
            assertInstanceOf(Exception.class, exception);
            assertInstanceOf(Throwable.class, exception);
        }

        @Test
        @DisplayName("should be throwable")
        void shouldBeThrowable() {
            QueryExecutionException exception = new QueryExecutionException("test");
            
            assertThrows(QueryExecutionException.class, () -> {
                throw exception;
            });
        }

        @Test
        @DisplayName("should maintain serialVersionUID")
        void shouldMaintainSerialVersionUID() {
            QueryExecutionException exception = new QueryExecutionException("test");
            
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
            RuntimeException rootCause = new RuntimeException("Root cause");
            IllegalStateException intermediateCause = new IllegalStateException("Intermediate", rootCause);
            QueryExecutionException exception = new QueryExecutionException("Top level", intermediateCause);
            
            assertEquals("Top level", exception.getMessage());
            assertEquals(intermediateCause, exception.getCause());
            assertEquals(rootCause, exception.getCause().getCause());
        }

        @Test
        @DisplayName("should handle complex exception chains")
        void shouldHandleComplexExceptionChains() {
            Exception level4 = new Exception("Level 4");
            RuntimeException level3 = new RuntimeException("Level 3", level4);
            IllegalArgumentException level2 = new IllegalArgumentException("Level 2", level3);
            QueryExecutionException level1 = new QueryExecutionException("Level 1", level2);
            
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
        @DisplayName("should be useful for SQL exceptions")
        void shouldBeUsefulForSqlExceptions() {
            String sqlError = "ORA-00942: table or view does not exist";
            RuntimeException sqlException = new RuntimeException(sqlError);
            
            QueryExecutionException exception = new QueryExecutionException(
                "Failed to execute query against USERS table", sqlException);
            
            assertTrue(exception.getMessage().contains("USERS table"));
            assertEquals(sqlException, exception.getCause());
            assertTrue(exception.getCause().getMessage().contains("ORA-00942"));
        }

        @Test
        @DisplayName("should be useful for connection timeouts")
        void shouldBeUsefulForConnectionTimeouts() {
            RuntimeException timeoutException = new RuntimeException("Connection timeout after 30 seconds");
            
            QueryExecutionException exception = new QueryExecutionException(
                "Query timed out while waiting for database response", timeoutException);
            
            assertTrue(exception.getMessage().contains("timed out"));
            assertEquals(timeoutException, exception.getCause());
        }

        @Test
        @DisplayName("should be useful for parameter binding errors")
        void shouldBeUsefulForParameterBindingErrors() {
            IllegalArgumentException bindingError = new IllegalArgumentException("Parameter index out of range");
            
            QueryExecutionException exception = new QueryExecutionException(
                "Failed to bind parameters to prepared statement", bindingError);
            
            assertTrue(exception.getMessage().contains("bind parameters"));
            assertEquals(bindingError, exception.getCause());
        }
    }
}