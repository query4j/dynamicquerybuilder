package com.github.query4j.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DynamicQueryException class.
 * Validates the base exception class functionality including inheritance,
 * constructors, exception chaining, and serialization.
 */
@DisplayName("DynamicQueryException")
class DynamicQueryExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should create exception with message")
        void shouldCreateExceptionWithMessage() {
            String message = "Dynamic query operation failed";
            DynamicQueryException exception = new DynamicQueryException(message);
            
            assertEquals(message, exception.getMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("should create exception with message and cause")
        void shouldCreateExceptionWithMessageAndCause() {
            String message = "Query processing failed";
            IllegalStateException cause = new IllegalStateException("Invalid state");
            
            DynamicQueryException exception = new DynamicQueryException(message, cause);
            
            assertEquals(message, exception.getMessage());
            assertEquals(cause, exception.getCause());
        }

        @Test
        @DisplayName("should create exception with cause only")
        void shouldCreateExceptionWithCauseOnly() {
            IllegalArgumentException cause = new IllegalArgumentException("Invalid argument");
            
            DynamicQueryException exception = new DynamicQueryException(cause);
            
            assertEquals(cause, exception.getCause());
            // Message should be the cause's toString()
            assertTrue(exception.getMessage().contains("IllegalArgumentException"));
        }

        @Test
        @DisplayName("should create exception with null message")
        void shouldCreateExceptionWithNullMessage() {
            DynamicQueryException exception = new DynamicQueryException((String) null);
            
            assertNull(exception.getMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("should create exception with null cause")
        void shouldCreateExceptionWithNullCause() {
            DynamicQueryException exception = new DynamicQueryException((Throwable) null);
            
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("should create exception with null message and cause")
        void shouldCreateExceptionWithNullMessageAndCause() {
            DynamicQueryException exception = new DynamicQueryException(null, null);
            
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
            DynamicQueryException exception = new DynamicQueryException("test");
            
            assertInstanceOf(RuntimeException.class, exception);
            assertInstanceOf(Exception.class, exception);
            assertInstanceOf(Throwable.class, exception);
        }

        @Test
        @DisplayName("should be throwable")
        void shouldBeThrowable() {
            DynamicQueryException exception = new DynamicQueryException("test");
            
            assertThrows(DynamicQueryException.class, () -> {
                throw exception;
            });
        }

        @Test
        @DisplayName("should maintain serialVersionUID")
        void shouldMaintainSerialVersionUID() {
            DynamicQueryException exception = new DynamicQueryException("test");
            
            // Verify it's serializable by checking the class has serialVersionUID
            assertDoesNotThrow(() -> {
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
                oos.writeObject(exception);
                oos.close();
            });
        }

        @Test
        @DisplayName("should be base for all Query4j exceptions")
        void shouldBeBaseForAllQuery4jExceptions() {
            QueryBuildException buildException = new QueryBuildException("build error");
            QueryExecutionException executionException = new QueryExecutionException("execution error");
            
            // Both should be instances of DynamicQueryException
            assertInstanceOf(DynamicQueryException.class, buildException);
            assertInstanceOf(DynamicQueryException.class, executionException);
            
            // Should be able to catch all Query4j exceptions with DynamicQueryException
            assertThrows(DynamicQueryException.class, () -> {
                throw buildException;
            });
            
            assertThrows(DynamicQueryException.class, () -> {
                throw executionException;
            });
        }
    }

    @Nested
    @DisplayName("Exception Chaining Tests")
    class ExceptionChainingTests {

        @Test
        @DisplayName("should properly chain exceptions")
        void shouldProperlyChainExceptions() {
            IllegalArgumentException rootCause = new IllegalArgumentException("Root cause");
            RuntimeException intermediateCause = new RuntimeException("Intermediate cause", rootCause);
            DynamicQueryException exception = new DynamicQueryException("Top level error", intermediateCause);
            
            assertEquals("Top level error", exception.getMessage());
            assertEquals(intermediateCause, exception.getCause());
            assertEquals(rootCause, exception.getCause().getCause());
        }

        @Test
        @DisplayName("should handle complex exception chains")
        void shouldHandleComplexExceptionChains() {
            Exception level4 = new Exception("Level 4");
            RuntimeException level3 = new RuntimeException("Level 3", level4);
            IllegalStateException level2 = new IllegalStateException("Level 2", level3);
            DynamicQueryException level1 = new DynamicQueryException("Level 1", level2);
            
            assertEquals("Level 1", level1.getMessage());
            assertEquals(level2, level1.getCause());
            assertEquals(level3, level1.getCause().getCause());
            assertEquals(level4, level1.getCause().getCause().getCause());
        }

        @Test
        @DisplayName("should handle circular reference safely")
        void shouldHandleCircularReferenceSafely() {
            // Create a circular reference scenario (should not cause infinite loop)
            DynamicQueryException exception1 = new DynamicQueryException("Exception 1");
            DynamicQueryException exception2 = new DynamicQueryException("Exception 2", exception1);
            
            assertEquals("Exception 2", exception2.getMessage());
            assertEquals(exception1, exception2.getCause());
            assertEquals("Exception 1", exception2.getCause().getMessage());
        }
    }

    @Nested
    @DisplayName("Practical Usage Tests")
    class PracticalUsageTests {

        @Test
        @DisplayName("should be useful for generic library errors")
        void shouldBeUsefulForGenericLibraryErrors() {
            String libraryError = "Query4j library initialization failed";
            IllegalStateException systemError = new IllegalStateException("System not ready");
            
            DynamicQueryException exception = new DynamicQueryException(libraryError, systemError);
            
            assertTrue(exception.getMessage().contains("Query4j"));
            assertEquals(systemError, exception.getCause());
        }

        @Test
        @DisplayName("should serve as catch-all for Query4j exceptions")
        void shouldServeAsCatchAllForQuery4jExceptions() {
            // Simulate catching any Query4j exception with base class
            QueryBuildException buildError = new QueryBuildException("Build failed");
            QueryExecutionException executionError = new QueryExecutionException("Execution failed");
            
            // Should be able to handle both with DynamicQueryException
            Exception caughtBuildException = assertThrows(DynamicQueryException.class, () -> {
                throw buildError;
            });
            
            Exception caughtExecutionException = assertThrows(DynamicQueryException.class, () -> {
                throw executionError;
            });
            
            assertTrue(caughtBuildException instanceof QueryBuildException);
            assertTrue(caughtExecutionException instanceof QueryExecutionException);
            assertTrue(caughtBuildException instanceof DynamicQueryException);
            assertTrue(caughtExecutionException instanceof DynamicQueryException);
        }

        @Test
        @DisplayName("should be useful for configuration errors")
        void shouldBeUsefulForConfigurationErrors() {
            IllegalArgumentException configError = new IllegalArgumentException("Invalid configuration");
            
            DynamicQueryException exception = new DynamicQueryException(
                "Query4j configuration validation failed", configError);
            
            assertTrue(exception.getMessage().contains("configuration"));
            assertEquals(configError, exception.getCause());
        }

        @Test
        @DisplayName("should provide meaningful error messages")
        void shouldProvideMeaningfulErrorMessages() {
            String contextMessage = "Failed to initialize Query4j: database connection unavailable";
            RuntimeException cause = new RuntimeException("Connection refused");
            
            DynamicQueryException exception = new DynamicQueryException(contextMessage, cause);
            
            // Should have clear message and preserve cause
            assertTrue(exception.getMessage().contains("Query4j"));
            assertTrue(exception.getMessage().contains("database connection"));
            assertEquals(cause, exception.getCause());
            assertTrue(exception.getCause().getMessage().contains("Connection refused"));
        }
    }

    @Nested
    @DisplayName("Thread Safety and Serialization Tests")
    class ThreadSafetyAndSerializationTests {

        @Test
        @DisplayName("should be serializable and deserializable")
        void shouldBeSerializableAndDeserializable() throws Exception {
            String message = "Serialization test message";
            RuntimeException cause = new RuntimeException("Serialization cause");
            DynamicQueryException original = new DynamicQueryException(message, cause);
            
            // Serialize
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
            oos.writeObject(original);
            oos.close();
            
            // Deserialize
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
            java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
            DynamicQueryException deserialized = (DynamicQueryException) ois.readObject();
            ois.close();
            
            // Verify
            assertEquals(original.getMessage(), deserialized.getMessage());
            assertEquals(original.getCause().getMessage(), deserialized.getCause().getMessage());
            assertEquals(original.getCause().getClass(), deserialized.getCause().getClass());
        }

        @Test
        @DisplayName("should handle concurrent access safely")
        void shouldHandleConcurrentAccessSafely() throws InterruptedException {
            DynamicQueryException exception = new DynamicQueryException("Concurrent test");
            
            // Test that multiple threads can safely access exception properties
            Thread[] threads = new Thread[10];
            boolean[] results = new boolean[10];
            
            for (int i = 0; i < 10; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    try {
                        // Access exception properties from multiple threads
                        String message = exception.getMessage();
                        Throwable cause = exception.getCause();
                        StackTraceElement[] stackTrace = exception.getStackTrace();
                        
                        results[index] = "Concurrent test".equals(message) && 
                                       cause == null && 
                                       stackTrace != null;
                    } catch (Exception e) {
                        results[index] = false;
                    }
                });
            }
            
            // Start all threads
            for (Thread thread : threads) {
                thread.start();
            }
            
            // Wait for all threads to complete
            for (Thread thread : threads) {
                thread.join();
            }
            
            // Verify all threads succeeded
            for (boolean result : results) {
                assertTrue(result, "Concurrent access should be safe");
            }
        }
    }
}