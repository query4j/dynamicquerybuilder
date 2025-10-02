package com.github.query4j.core.impl;

import com.github.query4j.core.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PageImpl")
class PageImplTest {

    @Nested
    @DisplayName("Basic Page Properties")
    class BasicPagePropertiesTests {

        @Test
        @DisplayName("should create page with valid properties")
        void testPageProperties() {
            List<String> content = Arrays.asList("a", "b", "c");
            int pageNumber = 1;
            int pageSize = 3;
            long totalElements = 10;

            Page<String> page = new PageImpl<>(content, pageNumber, pageSize, totalElements);

            assertEquals(content, page.getContent());
            assertEquals(pageNumber, page.getNumber());
            assertEquals(pageSize, page.getSize());
            assertEquals(totalElements, page.getTotalElements());

            int expectedTotalPages = (int) Math.ceil((double) totalElements / pageSize);
            assertEquals(expectedTotalPages, page.getTotalPages());

            assertTrue(page.hasNext());
            assertTrue(page.hasPrevious());
            assertFalse(page.isFirst());
            assertFalse(page.isLast());
        }

        @Test
        @DisplayName("should handle first and last page indicators")
        void testFirstAndLastPageIndicators() {
            Page<String> firstPage = new PageImpl<>(Arrays.asList("x"), 0, 3, 3);
            assertTrue(firstPage.isFirst());
            assertTrue(firstPage.isLast()); // changed from false to true
            assertFalse(firstPage.hasPrevious());
            assertFalse(firstPage.hasNext());

            Page<String> lastPage = new PageImpl<>(Arrays.asList("z"), 2, 3, 7);
            assertFalse(lastPage.isFirst());
            assertTrue(lastPage.isLast());
            assertTrue(lastPage.hasPrevious());
            assertFalse(lastPage.hasNext());
        }

        @Test
        @DisplayName("should create empty page")
        void testEmptyPage() {
            Page<String> emptyPage = PageImpl.empty();
            assertNotNull(emptyPage);
            assertTrue(emptyPage.getContent().isEmpty());
            assertEquals(0, emptyPage.getNumber());
            assertEquals(0, emptyPage.getSize());
            assertEquals(0, emptyPage.getTotalElements());
            assertEquals(0, emptyPage.getTotalPages());
            assertTrue(emptyPage.isFirst());
            assertTrue(emptyPage.isLast());
            assertFalse(emptyPage.hasNext());
            assertFalse(emptyPage.hasPrevious());
        }
    }

    @Nested
    @DisplayName("Constructor Edge Cases")
    class ConstructorEdgeCasesTests {

        @Test
        @DisplayName("should handle null content with defensive copying")
        void shouldHandleNullContent() {
            Page<String> page = new PageImpl<>(null, 0, 10, 0);
            
            assertNotNull(page.getContent());
            assertTrue(page.getContent().isEmpty());
            assertEquals(0, page.getNumber());
            assertEquals(10, page.getSize());
            assertEquals(0, page.getTotalElements());
        }

        @Test
        @DisplayName("should handle empty content list")
        void shouldHandleEmptyContent() {
            List<String> emptyList = Collections.emptyList();
            Page<String> page = new PageImpl<>(emptyList, 0, 10, 100);
            
            assertNotNull(page.getContent());
            assertTrue(page.getContent().isEmpty());
            assertEquals(0, page.getNumber());
            assertEquals(10, page.getSize());
            assertEquals(100, page.getTotalElements());
            assertEquals(10, page.getTotalPages());
        }

        @Test
        @DisplayName("should throw exception for negative page number")
        void shouldThrowForNegativePageNumber() {
            List<String> content = Arrays.asList("item1", "item2");
            
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                new PageImpl<>(content, -1, 10, 20);
            });
            
            assertTrue(exception.getMessage().contains("Page number cannot be negative"));
        }

        @Test
        @DisplayName("should handle zero page size")
        void shouldHandleZeroPageSize() {
            List<String> content = Arrays.asList("item1");
            Page<String> page = new PageImpl<>(content, 0, 0, 1);
            
            assertEquals(0, page.getNumber());
            assertEquals(0, page.getSize());
            assertEquals(1, page.getTotalElements());
            assertEquals(0, page.getTotalPages()); // Zero page size should result in 0 total pages
        }

        @Test
        @DisplayName("should throw exception for negative page size")
        void shouldThrowForNegativePageSize() {
            List<String> content = Arrays.asList("item1");
            
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                new PageImpl<>(content, 0, -5, 10);
            });
            
            assertTrue(exception.getMessage().contains("Page size cannot be negative"));
        }

        @Test
        @DisplayName("should handle zero total elements")
        void shouldHandleZeroTotalElements() {
            List<String> content = Collections.emptyList();
            Page<String> page = new PageImpl<>(content, 0, 10, 0);
            
            assertEquals(0, page.getTotalElements());
            assertEquals(0, page.getTotalPages());
            assertTrue(page.isFirst());
            assertTrue(page.isLast());
            assertFalse(page.hasNext());
            assertFalse(page.hasPrevious());
        }

        @Test
        @DisplayName("should throw exception for negative total elements")
        void shouldThrowForNegativeTotalElements() {
            List<String> content = Arrays.asList("item1");
            
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                new PageImpl<>(content, 0, 10, -5);
            });
            
            assertTrue(exception.getMessage().contains("Total elements cannot be negative"));
        }

        @Test
        @DisplayName("should create immutable content copy")
        void shouldCreateImmutableContentCopy() {
            List<String> originalList = Arrays.asList("item1", "item2", "item3");
            Page<String> page = new PageImpl<>(originalList, 0, 10, 3);
            
            List<String> pageContent = page.getContent();
            assertNotSame(originalList, pageContent);
            assertEquals(originalList, pageContent);
            
            // Verify the content is immutable
            assertThrows(UnsupportedOperationException.class, () -> {
                pageContent.add("newItem");
            });
            assertThrows(UnsupportedOperationException.class, () -> {
                pageContent.remove(0);
            });
        }
    }

    @Nested
    @DisplayName("Page Navigation Logic")
    class PageNavigationLogicTests {

        @Test
        @DisplayName("should calculate total pages correctly")
        void shouldCalculateTotalPagesCorrectly() {
            // Exact division
            Page<String> page1 = new PageImpl<>(Collections.emptyList(), 0, 5, 10);
            assertEquals(2, page1.getTotalPages());
            
            // With remainder
            Page<String> page2 = new PageImpl<>(Collections.emptyList(), 0, 3, 10);
            assertEquals(4, page2.getTotalPages());
            
            // Single element
            Page<String> page3 = new PageImpl<>(Collections.emptyList(), 0, 10, 1);
            assertEquals(1, page3.getTotalPages());
        }

        @Test
        @DisplayName("should handle hasNext logic correctly")
        void shouldHandleHasNextLogicCorrectly() {
            // Has next page
            Page<String> page1 = new PageImpl<>(Collections.emptyList(), 0, 5, 15);
            assertTrue(page1.hasNext());
            
            // Last page
            Page<String> page2 = new PageImpl<>(Collections.emptyList(), 2, 5, 15);
            assertFalse(page2.hasNext());
            
            // Beyond last page
            Page<String> page3 = new PageImpl<>(Collections.emptyList(), 5, 5, 15);
            assertFalse(page3.hasNext());
        }

        @Test
        @DisplayName("should handle hasPrevious logic correctly")
        void shouldHandleHasPreviousLogicCorrectly() {
            // First page
            Page<String> page1 = new PageImpl<>(Collections.emptyList(), 0, 5, 15);
            assertFalse(page1.hasPrevious());
            
            // Second page
            Page<String> page2 = new PageImpl<>(Collections.emptyList(), 1, 5, 15);
            assertTrue(page2.hasPrevious());
        }

        @Test
        @DisplayName("should handle isFirst logic correctly")
        void shouldHandleIsFirstLogicCorrectly() {
            Page<String> firstPage = new PageImpl<>(Collections.emptyList(), 0, 5, 15);
            assertTrue(firstPage.isFirst());
            
            Page<String> notFirstPage = new PageImpl<>(Collections.emptyList(), 1, 5, 15);
            assertFalse(notFirstPage.isFirst());
        }

        @Test
        @DisplayName("should handle isLast logic correctly")
        void shouldHandleIsLastLogicCorrectly() {
            // Last page
            Page<String> lastPage = new PageImpl<>(Collections.emptyList(), 2, 5, 15);
            assertTrue(lastPage.isLast());
            
            // Not last page
            Page<String> notLastPage = new PageImpl<>(Collections.emptyList(), 0, 5, 15);
            assertFalse(notLastPage.isLast());
            
            // Single page scenario
            Page<String> singlePage = new PageImpl<>(Collections.emptyList(), 0, 5, 3);
            assertTrue(singlePage.isLast());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Conditions")
    class EdgeCasesTests {

        @Test
        @DisplayName("should handle very large total elements")
        void shouldHandleVeryLargeTotalElements() {
            long largeTotal = Long.MAX_VALUE;
            Page<String> page = new PageImpl<>(Collections.emptyList(), 0, 1000, largeTotal);
            
            assertEquals(largeTotal, page.getTotalElements());
            // Should not overflow
            assertTrue(page.getTotalPages() > 0);
        }

        @Test
        @DisplayName("should handle maximum integer page size")
        void shouldHandleMaximumIntegerPageSize() {
            Page<String> page = new PageImpl<>(Collections.emptyList(), 0, Integer.MAX_VALUE, 100);
            
            assertEquals(Integer.MAX_VALUE, page.getSize());
            assertEquals(1, page.getTotalPages());
        }

        @Test
        @DisplayName("should maintain consistency across multiple calls")
        void shouldMaintainConsistencyAcrossMultipleCalls() {
            List<String> content = Arrays.asList("a", "b", "c");
            Page<String> page = new PageImpl<>(content, 1, 3, 10);
            
            // Call methods multiple times to ensure consistency
            for (int i = 0; i < 10; i++) {
                assertEquals(content, page.getContent());
                assertEquals(1, page.getNumber());
                assertEquals(3, page.getSize());
                assertEquals(10, page.getTotalElements());
                assertEquals(4, page.getTotalPages());
                assertTrue(page.hasNext());
                assertTrue(page.hasPrevious());
                assertFalse(page.isFirst());
                assertFalse(page.isLast());
            }
        }
    }
}
