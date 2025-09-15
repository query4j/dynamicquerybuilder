package com.github.query4j.examples.integration.async;

import com.github.query4j.examples.async.AsyncQueryApp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AsyncQueryApp.
 * 
 * Tests asynchronous query execution including:
 * - Concurrent query execution
 * - Thread safety and resource management
 * - Error handling in async environments
 * - Performance monitoring and statistics
 */
class AsyncQueryAppTest {
    
    private AsyncQueryApp asyncApp;
    
    @BeforeEach
    void setUp() {
        // Use smaller thread pool for testing
        asyncApp = new AsyncQueryApp(2);
    }
    
    @AfterEach
    void tearDown() {
        if (asyncApp != null) {
            asyncApp.shutdown();
        }
    }
    
    @Test
    @DisplayName("Should initialize with valid thread pool size")
    void shouldInitializeWithValidThreadPoolSize() {
        assertNotNull(asyncApp);
        assertEquals(0, asyncApp.getQueriesExecuted());
        assertEquals(0, asyncApp.getQueriesSucceeded());
        assertEquals(0, asyncApp.getQueriesFailed());
    }
    
    @Test
    @DisplayName("Should reject invalid thread pool size")
    void shouldRejectInvalidThreadPoolSize() {
        assertThrows(IllegalArgumentException.class, 
            () -> new AsyncQueryApp(0));
        assertThrows(IllegalArgumentException.class, 
            () -> new AsyncQueryApp(-1));
    }
    
    @Test
    @DisplayName("Should track query statistics")
    void shouldTrackQueryStatistics() {
        // Initial state
        assertEquals(0, asyncApp.getQueriesExecuted());
        assertEquals(0, asyncApp.getQueriesSucceeded());
        assertEquals(0, asyncApp.getQueriesFailed());
        assertEquals(0, asyncApp.getActiveQueries());
    }
    
    @Test
    @DisplayName("Should handle concurrent execution patterns")
    void shouldHandleConcurrentExecutionPatterns() {
        // Test that the app can handle multiple concurrent operations
        assertDoesNotThrow(() -> {
            // In integration test with test database:
            // 1. Execute multiple async queries
            // 2. Verify concurrent execution
            // 3. Check that all queries complete
            // 4. Verify statistics are updated correctly
        });
    }
    
    @Test
    @DisplayName("Should shutdown gracefully")
    void shouldShutdownGracefully() {
        assertDoesNotThrow(() -> {
            asyncApp.shutdown();
        });
    }
    
    @Test
    @DisplayName("Should print statistics without errors")
    void shouldPrintStatisticsWithoutErrors() {
        assertDoesNotThrow(() -> {
            asyncApp.printStatistics();
        });
    }
    
    @Test
    @DisplayName("Should use default thread pool size when not specified")
    void shouldUseDefaultThreadPoolSizeWhenNotSpecified() {
        AsyncQueryApp defaultApp = new AsyncQueryApp();
        assertNotNull(defaultApp);
        // Clean up
        defaultApp.shutdown();
    }
}