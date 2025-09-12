package com.github.query4j.core.impl;

import com.github.query4j.core.DynamicQuery;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

class DynamicQueryImplTest {

    @Test
    void testExecuteReturnsResults() {
        List<String> sampleResults = Arrays.asList("elem1", "elem2");
        DynamicQuery<String> dq = new DynamicQueryImpl<>(sampleResults, "SELECT * FROM SampleTable");
        List<String> results = dq.execute();

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("elem1", results.get(0));
    }

    @Test
    void testExecuteOneReturnsFirstElement() {
        List<String> sampleResults = Arrays.asList("first", "second");
        DynamicQuery<String> dq = new DynamicQueryImpl<>(sampleResults, "SELECT * FROM SampleTable");
        String first = dq.executeOne();

        assertEquals("first", first);
    }

    @Test
    void testExecuteOneReturnsNullWhenEmpty() {
        DynamicQuery<String> dq = new DynamicQueryImpl<>(Collections.emptyList(), "SELECT * FROM SampleTable");
        assertNull(dq.executeOne());
    }

    @Test
    void testExecuteCountReturnsSize() {
        List<Integer> results = Arrays.asList(1, 2, 3, 4);
        DynamicQuery<Integer> dq = new DynamicQueryImpl<>(results, "SELECT * FROM SampleTable");
        long count = dq.executeCount();

        assertEquals(4, count);
    }

    @Test
    void testGetSQL() {
        String sql = "SELECT name FROM SampleTable";
        DynamicQuery<Object> dq = new DynamicQueryImpl<>(Collections.emptyList(), sql);
        assertEquals(sql, dq.getSQL());
    }

    @Test
    void testNullSafeSQL() {
        DynamicQuery<Object> dq = new DynamicQueryImpl<>(null, null);
        assertNotNull(dq.getSQL());
        assertEquals("", dq.getSQL());
    }

    @Test
    void testGetResultsMethod() {
        List<String> sampleResults = Arrays.asList("result1", "result2", "result3");
        DynamicQueryImpl<String> dq = new DynamicQueryImpl<>(sampleResults, "SELECT * FROM test");
        
        List<String> results = dq.getResults();
        
        assertNotNull(results);
        assertEquals(3, results.size());
        assertEquals("result1", results.get(0));
        assertEquals("result2", results.get(1));
        assertEquals("result3", results.get(2));
    }

    @Test
    void testGetResultsWithEmptyList() {
        DynamicQueryImpl<String> dq = new DynamicQueryImpl<>(Collections.emptyList(), "SELECT * FROM test");
        
        List<String> results = dq.getResults();
        
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testGetResultsWithNullList() {
        DynamicQueryImpl<String> dq = new DynamicQueryImpl<>(null, "SELECT * FROM test");
        
        List<String> results = dq.getResults();
        
        assertNull(results);
    }
}
