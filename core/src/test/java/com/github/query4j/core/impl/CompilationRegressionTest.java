package com.github.query4j.core.impl;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for compilation issues identified in GitHub issue #63.
 * These tests ensure that the fixes for Lombok @Getter conflicts and constructor 
 * parameter alignment work correctly.
 */
@DisplayName("Compilation Regression Tests")
class CompilationRegressionTest {

    @Test
    @DisplayName("DynamicQueryImpl should instantiate without compilation errors")
    void dynamicQueryImplInstantiatesCorrectly() {
        // Test that DynamicQueryImpl can be instantiated with final fields
        // This would fail if @RequiredArgsConstructor wasn't working properly
        DynamicQueryImpl<String> query = new DynamicQueryImpl<>(
            Arrays.asList("result1", "result2"), 
            "SELECT * FROM test"
        );
        
        assertNotNull(query);
        assertEquals(2, query.execute().size());
        assertEquals("result1", query.executeOne());
        assertEquals(2L, query.executeCount());
        assertEquals("SELECT * FROM test", query.getSQL());
        
        // Test the manual getters that replaced @Getter annotations
        assertEquals(Arrays.asList("result1", "result2"), query.getResults());
        assertEquals("SELECT * FROM test", query.getSql());
    }

    @Test
    @DisplayName("DynamicQueryImpl should handle null values correctly")
    void dynamicQueryImplHandlesNulls() {
        // Test null handling (important for final field initialization)
        DynamicQueryImpl<String> query = new DynamicQueryImpl<>(null, null);
        
        assertNotNull(query);
        assertTrue(query.execute().isEmpty());
        assertNull(query.executeOne());
        assertEquals(0L, query.executeCount());
        assertEquals("", query.getSQL());
        
        // Test manual getters with nulls
        assertNull(query.getResults());
        assertNull(query.getSql());
    }

    @Test
    @DisplayName("DynamicQueryBuilder should instantiate without constructor parameter errors")
    void dynamicQueryBuilderConstructorWorks() {
        // Test that DynamicQueryBuilder constructor parameter count is correct
        // This would fail if the parameter count in the public constructor 
        // didn't match the @RequiredArgsConstructor generated constructor
        DynamicQueryBuilder<String> builder = new DynamicQueryBuilder<>(String.class);
        
        assertNotNull(builder);
        assertTrue(builder.getPredicates().isEmpty());
        
        // Test that the builder can create chains without issues
        var newBuilder = builder.where("name", "test");
        assertNotNull(newBuilder);
        assertNotSame(builder, newBuilder); // Verify immutability
    }

    @Test
    @DisplayName("Final fields should be properly initialized in both classes")
    void finalFieldsAreInitialized() {
        // Verify that final fields don't cause "variable not initialized" errors
        
        // Test DynamicQueryImpl
        DynamicQueryImpl<Integer> query = new DynamicQueryImpl<>(
            Collections.singletonList(42), 
            "SELECT 42"
        );
        
        // These calls would fail if final fields weren't properly initialized
        assertNotNull(query.getResults());
        assertNotNull(query.getSql());
        
        // Test DynamicQueryBuilder
        DynamicQueryBuilder<Integer> builder = new DynamicQueryBuilder<>(Integer.class);
        
        // These calls would fail if final fields weren't properly initialized by constructor
        assertNotNull(builder.getPredicates());
        // Note: we can't access other final fields directly, but their initialization
        // is tested implicitly by the successful construction and method calls
    }
}