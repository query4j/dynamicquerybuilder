package com.github.query4j.optimizer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for OptimizationException to improve code coverage.
 */
@DisplayName("OptimizationException Tests")
class OptimizationExceptionTest {
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create exception with message")
        void shouldCreateExceptionWithMessage() {
            String message = "Test optimization error";
            
            OptimizationException exception = new OptimizationException(message);
            
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isNull();
        }
        
        @Test
        @DisplayName("Should create exception with message and cause")
        void shouldCreateExceptionWithMessageAndCause() {
            String message = "Test optimization error";
            RuntimeException cause = new RuntimeException("Root cause");
            
            OptimizationException exception = new OptimizationException(message, cause);
            
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isEqualTo(cause);
        }
        
        @Test
        @DisplayName("Should create exception with cause only")
        void shouldCreateExceptionWithCauseOnly() {
            RuntimeException cause = new RuntimeException("Root cause");
            
            OptimizationException exception = new OptimizationException(cause);
            
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getMessage()).contains("Root cause");
        }
        
        @Test
        @DisplayName("Should handle null message")
        void shouldHandleNullMessage() {
            OptimizationException exception = new OptimizationException((String) null);
            
            assertThat(exception.getMessage()).isNull();
            assertThat(exception.getCause()).isNull();
        }
        
        @Test
        @DisplayName("Should handle null cause")
        void shouldHandleNullCause() {
            OptimizationException exception = new OptimizationException((Throwable) null);
            
            assertThat(exception.getCause()).isNull();
        }
        
        @Test
        @DisplayName("Should handle null message and cause")
        void shouldHandleNullMessageAndCause() {
            OptimizationException exception = new OptimizationException(null, null);
            
            assertThat(exception.getMessage()).isNull();
            assertThat(exception.getCause()).isNull();
        }
    }
    
    @Nested
    @DisplayName("Exception Hierarchy Tests")
    class ExceptionHierarchyTests {
        
        @Test
        @DisplayName("Should extend RuntimeException")
        void shouldExtendRuntimeException() {
            OptimizationException exception = new OptimizationException("Test message");
            
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }
        
        @Test
        @DisplayName("Should be throwable")
        void shouldBeThrowable() {
            assertThatThrownBy(() -> {
                throw new OptimizationException("Test exception");
            }).isInstanceOf(OptimizationException.class)
              .hasMessage("Test exception");
        }
        
        @Test
        @DisplayName("Should be catchable as RuntimeException")
        void shouldBeCatchableAsRuntimeException() {
            try {
                throw new OptimizationException("Test exception");
            } catch (RuntimeException e) {
                assertThat(e).isInstanceOf(OptimizationException.class);
                assertThat(e.getMessage()).isEqualTo("Test exception");
            }
        }
    }
    
    @Nested
    @DisplayName("Serialization Tests")
    class SerializationTests {
        
        @Test
        @DisplayName("Should have stack trace")
        void shouldHaveStackTrace() {
            OptimizationException exception = new OptimizationException("Test message");
            
            StackTraceElement[] stackTrace = exception.getStackTrace();
            assertThat(stackTrace).isNotEmpty();
        }
        
        @Test
        @DisplayName("Should preserve stack trace when wrapping cause")
        void shouldPreserveStackTraceWhenWrappingCause() {
            RuntimeException originalException = new RuntimeException("Original error");
            OptimizationException wrappedException = new OptimizationException("Wrapped error", originalException);
            
            assertThat(wrappedException.getCause()).isEqualTo(originalException);
            assertThat(wrappedException.getStackTrace()).isNotEmpty();
        }
    }
    
    @Nested
    @DisplayName("Usage Scenarios Tests")
    class UsageScenariosTests {
        
        @Test
        @DisplayName("Should be used for optimization timeout")
        void shouldBeUsedForOptimizationTimeout() {
            assertThatThrownBy(() -> {
                throw new OptimizationException("Analysis timeout exceeded after 5000ms");
            }).isInstanceOf(OptimizationException.class)
              .hasMessageContaining("timeout")
              .hasMessageContaining("5000ms");
        }
        
        @Test
        @DisplayName("Should be used for configuration errors")
        void shouldBeUsedForConfigurationErrors() {
            assertThatThrownBy(() -> {
                throw new OptimizationException("Invalid optimizer configuration: threshold must be positive");
            }).isInstanceOf(OptimizationException.class)
              .hasMessageContaining("configuration")
              .hasMessageContaining("threshold");
        }
        
        @Test
        @DisplayName("Should be used for wrapping database exceptions")
        void shouldBeUsedForWrappingDatabaseExceptions() {
            RuntimeException sqlException = new RuntimeException("Connection timeout");
            
            assertThatThrownBy(() -> {
                throw new OptimizationException("Failed to analyze query statistics", sqlException);
            }).isInstanceOf(OptimizationException.class)
              .hasMessageContaining("analyze")
              .hasCause(sqlException);
        }
        
        @Test
        @DisplayName("Should be used for analysis failures")
        void shouldBeUsedForAnalysisFailures() {
            assertThatThrownBy(() -> {
                throw new OptimizationException("Unable to parse query structure for optimization");
            }).isInstanceOf(OptimizationException.class)
              .hasMessageContaining("parse")
              .hasMessageContaining("optimization");
        }
    }
}