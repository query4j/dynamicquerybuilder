package com.github.query4j.core;

import com.github.query4j.core.impl.PageImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for {@link Page} interface and its implementations.
 * 
 * <p>Tests cover all pagination scenarios, edge cases, and boundary conditions
 * to ensure robust behavior across different use cases.</p>
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
class PageSimpleTest {

    @Test
    @DisplayName("Should create page with valid parameters")
    void shouldCreatePageWithValidParameters() {
        List<String> content = Arrays.asList("item1", "item2", "item3");
        
        Page<String> page = new PageImpl<>(content, 0, 10, 25L);
        
        assertEquals(content, page.getContent());
        assertEquals(0, page.getNumber());
        assertEquals(10, page.getSize());
        assertEquals(25L, page.getTotalElements());
    }

    @Test
    @DisplayName("Should create empty page")
    void shouldCreateEmptyPage() {
        Page<String> page = new PageImpl<>(Collections.emptyList(), 0, 10, 0L);
        
        assertTrue(page.getContent().isEmpty());
        assertEquals(0, page.getNumber());
        assertEquals(10, page.getSize());
        assertEquals(0L, page.getTotalElements());
    }

    @Test
    @DisplayName("Should create page with null content")
    void shouldCreatePageWithNullContent() {
        Page<String> page = new PageImpl<>(null, 0, 10, 0L);
        
        // PageImpl converts null to empty list
        assertNotNull(page.getContent());
        assertTrue(page.getContent().isEmpty());
    }

    @Test
    @DisplayName("Should calculate total pages correctly")
    void shouldCalculateTotalPagesCorrectly() {
        // Test case 1: 25 elements, 10 per page = 3 pages
        Page<String> page1 = new PageImpl<>(Arrays.asList("item"), 0, 10, 25L);
        assertEquals(3, page1.getTotalPages());
        
        // Test case 2: 25 elements, 5 per page = 5 pages
        Page<String> page2 = new PageImpl<>(Arrays.asList("item"), 1, 5, 25L);
        assertEquals(5, page2.getTotalPages());
        
        // Test case 3: Exact division - 20 elements, 10 per page = 2 pages
        Page<String> page3 = new PageImpl<>(Arrays.asList("item"), 0, 10, 20L);
        assertEquals(2, page3.getTotalPages());
    }

    @Test
    @DisplayName("Should identify first page correctly")
    void shouldIdentifyFirstPageCorrectly() {
        Page<String> firstPage = new PageImpl<>(Arrays.asList("item"), 0, 10, 25L);
        Page<String> secondPage = new PageImpl<>(Arrays.asList("item"), 1, 10, 25L);
        
        assertTrue(firstPage.isFirst());
        assertFalse(secondPage.isFirst());
    }

    @Test
    @DisplayName("Should identify last page correctly")
    void shouldIdentifyLastPageCorrectly() {
        Page<String> firstPage = new PageImpl<>(Arrays.asList("item"), 0, 10, 25L);  // 3 total pages (0, 1, 2)
        Page<String> lastPage = new PageImpl<>(Arrays.asList("item"), 2, 10, 25L);   // Last page
        Page<String> singlePage = new PageImpl<>(Arrays.asList("item"), 0, 10, 5L);  // Only one page
        
        assertFalse(firstPage.isLast());
        assertTrue(lastPage.isLast());
        assertTrue(singlePage.isLast()); // Single page is both first and last
    }

    @Test
    @DisplayName("Should detect presence of next page")
    void shouldDetectPresenceOfNextPage() {
        Page<String> firstPage = new PageImpl<>(Arrays.asList("item"), 0, 10, 25L);
        Page<String> lastPage = new PageImpl<>(Arrays.asList("item"), 2, 10, 25L);
        
        assertTrue(firstPage.hasNext());
        assertFalse(lastPage.hasNext());
    }

    @Test
    @DisplayName("Should detect presence of previous page")
    void shouldDetectPresenceOfPreviousPage() {
        Page<String> firstPage = new PageImpl<>(Arrays.asList("item"), 0, 10, 25L);
        Page<String> secondPage = new PageImpl<>(Arrays.asList("item"), 1, 10, 25L);
        
        assertFalse(firstPage.hasPrevious());
        assertTrue(secondPage.hasPrevious());
    }

    @Test
    @DisplayName("Should handle single element per page")
    void shouldHandleSingleElementPerPage() {
        Page<String> page = new PageImpl<>(Arrays.asList("item"), 5, 1, 10L);
        
        assertEquals(1, page.getSize());
        assertEquals(10, page.getTotalPages());
        assertEquals(5, page.getNumber());
        assertFalse(page.isFirst());
        assertFalse(page.isLast());
        assertTrue(page.hasNext());
        assertTrue(page.hasPrevious());
    }

    @Test
    @DisplayName("Should handle large page sizes")
    void shouldHandleLargePageSizes() {
        List<String> content = Arrays.asList("item1", "item2");
        Page<String> page = new PageImpl<>(content, 0, 1000, 2L);
        
        assertEquals(1, page.getTotalPages()); // All items fit in one page
        assertTrue(page.isFirst());
        assertTrue(page.isLast());
        assertFalse(page.hasNext());
        assertFalse(page.hasPrevious());
    }

    @Test
    @DisplayName("Should handle page number larger than total pages")
    void shouldHandlePageNumberLargerThanTotalPages() {
        Page<String> page = new PageImpl<>(Collections.emptyList(), 10, 10, 25L);
        
        // Should still work, representing a page beyond the data
        assertEquals(10, page.getNumber());
        assertEquals(3, page.getTotalPages()); // Actual total pages
        assertTrue(page.getContent().isEmpty());
    }

    @Test
    @DisplayName("Should handle exact division of total elements")
    void shouldHandleExactDivisionOfTotalElements() {
        Page<String> page = new PageImpl<>(Arrays.asList("item1", "item2"), 1, 10, 20L);
        
        assertEquals(2, page.getTotalPages()); // Exactly 20 elements / 10 per page
        assertEquals(1, page.getNumber());
        assertTrue(page.isLast()); // Page 1 is the last page (0-indexed)
    }

    @Test
    @DisplayName("Should work with different generic types")
    void shouldWorkWithDifferentGenericTypes() {
        // Test with Integer
        Page<Integer> intPage = new PageImpl<>(Arrays.asList(1, 2, 3), 0, 5, 10L);
        assertEquals(3, intPage.getContent().size());
        
        // Test with String
        Page<String> stringPage = new PageImpl<>(Arrays.asList("a", "b", "c"), 0, 5, 10L);
        assertEquals(3, stringPage.getContent().size());
        
        // Test with custom objects
        Page<TestObject> objectPage = new PageImpl<>(Arrays.asList(new TestObject("test")), 0, 5, 1L);
        assertEquals(1, objectPage.getContent().size());
    }

    @Test
    @DisplayName("Should handle zero total pages edge case")
    void shouldHandleZeroTotalPagesEdgeCase() {
        Page<String> page = new PageImpl<>(Collections.emptyList(), 0, 10, 0L);
        
        // With 0 total elements and size 10, getTotalPages() returns 0 in PageImpl
        assertEquals(0, page.getTotalPages());
        assertEquals(0, page.getNumber());
        assertTrue(page.isFirst());
        assertTrue(page.isLast()); // No pages means this is both first and last
    }

    /**
     * Simple test object for generic type testing.
     */
    private static class TestObject {
        private final String value;
        
        public TestObject(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
}