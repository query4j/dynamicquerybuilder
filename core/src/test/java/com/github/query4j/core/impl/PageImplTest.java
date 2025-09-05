package com.github.query4j.core.impl;

import com.github.query4j.core.Page;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageImplTest {

    @Test
    void testPageProperties() {
        List<String> content = List.of("a", "b", "c");
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
    void testFirstAndLastPageIndicators() {
        Page<String> firstPage = new PageImpl<>(List.of("x"), 0, 3, 3);
        assertTrue(firstPage.isFirst());
        assertFalse(firstPage.isLast());
        assertFalse(firstPage.hasPrevious());
        assertTrue(firstPage.hasNext());

        Page<String> lastPage = new PageImpl<>(List.of("z"), 2, 3, 7);
        assertFalse(lastPage.isFirst());
        assertTrue(lastPage.isLast());
        assertTrue(lastPage.hasPrevious());
        assertFalse(lastPage.hasNext());
    }

    @Test
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
