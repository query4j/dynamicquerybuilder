package com.github.query4j.core.impl;

import com.github.query4j.core.QueryBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for pagination and sorting features as specified in issue #16.
 * Validates all acceptance criteria for pagination (page, limit, offset) and sorting functionality.
 */
@DisplayName("Pagination and Sorting Features (Issue #16)")
class PaginationAndSortingTest {

    private QueryBuilder<TestEntity> builder;

    @BeforeEach
    void setUp() {
        builder = QueryBuilder.forEntity(TestEntity.class);
    }

    @Nested
    @DisplayName("Pagination API Methods")
    class PaginationApiTests {

        @Test
        @DisplayName("limit() adds LIMIT clause")
        void limitAddsLimitClause() {
            String sql = builder.limit(25).toSQL();
            assertTrue(sql.contains("LIMIT 25"), 
                "SQL should contain LIMIT clause: " + sql);
        }

        @Test
        @DisplayName("offset() adds OFFSET clause")
        void offsetAddsOffsetClause() {
            String sql = builder.offset(10).toSQL();
            assertTrue(sql.contains("OFFSET 10"), 
                "SQL should contain OFFSET clause: " + sql);
        }

        @Test
        @DisplayName("page() calculates correct offset and limit")
        void pageCalculatesCorrectOffsetAndLimit() {
            // page(1, 20) → OFFSET 0 LIMIT 20 (but OFFSET 0 might be omitted)
            String sql1 = builder.page(1, 20).toSQL();
            assertTrue(sql1.contains("LIMIT 20"), 
                "Page 1 should have LIMIT 20: " + sql1);
            // OFFSET 0 is typically omitted in SQL generation

            // page(2, 20) → OFFSET 20 LIMIT 20
            String sql2 = builder.page(2, 20).toSQL();
            assertTrue(sql2.contains("LIMIT 20"), 
                "Page 2 should have LIMIT 20: " + sql2);
            assertTrue(sql2.contains("OFFSET 20"), 
                "Page 2 should have OFFSET 20: " + sql2);

            // page(3, 15) → OFFSET 30 LIMIT 15
            String sql3 = builder.page(3, 15).toSQL();
            assertTrue(sql3.contains("LIMIT 15"), 
                "Page 3 should have LIMIT 15: " + sql3);
            assertTrue(sql3.contains("OFFSET 30"), 
                "Page 3 should have OFFSET 30: " + sql3);
        }
    }

    @Nested
    @DisplayName("Sorting API Methods")
    class SortingApiTests {

        @Test
        @DisplayName("orderBy() with field name adds default ascending sort")
        void orderByDefaultAscending() {
            String sql = builder.orderBy("name").toSQL();
            assertTrue(sql.contains("ORDER BY name ASC"), 
                "Should add ascending order by default: " + sql);
        }

        @Test
        @DisplayName("orderBy() with explicit direction")
        void orderByExplicitDirection() {
            String sqlAsc = builder.orderBy("age", true).toSQL();
            assertTrue(sqlAsc.contains("ORDER BY age ASC"), 
                "Should add ascending order: " + sqlAsc);

            String sqlDesc = builder.orderBy("age", false).toSQL();
            assertTrue(sqlDesc.contains("ORDER BY age DESC"), 
                "Should add descending order: " + sqlDesc);
        }

        @Test
        @DisplayName("orderByDescending() adds descending sort")
        void orderByDescending() {
            String sql = builder.orderByDescending("created_date").toSQL();
            assertTrue(sql.contains("ORDER BY created_date DESC"), 
                "Should add descending order: " + sql);
        }

        @Test
        @DisplayName("Multiple orderBy calls accumulate into comma-separated ORDER BY clause")
        void multipleOrderByAccumulate() {
            String sql = builder
                .orderBy("field1")
                .orderBy("field2", false)
                .orderByDescending("field3")
                .toSQL();
            
            assertTrue(sql.contains("ORDER BY field1 ASC, field2 DESC, field3 DESC"), 
                "Should accumulate multiple order clauses: " + sql);
        }
    }

    @Nested
    @DisplayName("SQL Generation Order")
    class SqlGenerationOrderTests {

        @Test
        @DisplayName("ORDER BY appears after WHERE, GROUP BY, and HAVING clauses")
        void orderByAppearsInCorrectPosition() {
            String sql = builder
                .where("active", true)
                .groupBy("department")
                .having("COUNT(*)", ">", 5)
                .orderBy("name")
                .toSQL();
            
            // Find positions
            int wherePos = sql.indexOf("WHERE");
            int groupByPos = sql.indexOf("GROUP BY");
            int havingPos = sql.indexOf("HAVING");
            int orderByPos = sql.indexOf("ORDER BY");
            
            assertTrue(wherePos < orderByPos, 
                "ORDER BY should come after WHERE: " + sql);
            assertTrue(groupByPos < orderByPos, 
                "ORDER BY should come after GROUP BY: " + sql);
            assertTrue(havingPos < orderByPos, 
                "ORDER BY should come after HAVING: " + sql);
        }

        @Test
        @DisplayName("LIMIT and OFFSET appear at the end of SQL statement")
        void limitOffsetAppearAtEnd() {
            String sql = builder
                .where("active", true)
                .orderBy("name")
                .limit(10)
                .offset(5)
                .toSQL();
            
            int orderByPos = sql.indexOf("ORDER BY");
            int limitPos = sql.indexOf("LIMIT");
            int offsetPos = sql.indexOf("OFFSET");
            
            assertTrue(orderByPos < limitPos, 
                "LIMIT should come after ORDER BY: " + sql);
            assertTrue(limitPos < offsetPos, 
                "OFFSET should come after LIMIT: " + sql);
        }
    }

    @Nested
    @DisplayName("Immutability & Thread Safety")
    class ImmutabilityTests {

        @Test
        @DisplayName("Each pagination method returns new builder instance")
        void paginationMethodsReturnNewInstance() {
            QueryBuilder<TestEntity> withLimit = builder.limit(10);
            QueryBuilder<TestEntity> withOffset = builder.offset(5);
            QueryBuilder<TestEntity> withPage = builder.page(2, 10);

            assertNotSame(builder, withLimit, "limit() should return new instance");
            assertNotSame(builder, withOffset, "offset() should return new instance");
            assertNotSame(builder, withPage, "page() should return new instance");
        }

        @Test
        @DisplayName("Each sorting method returns new builder instance")
        void sortingMethodsReturnNewInstance() {
            QueryBuilder<TestEntity> withOrder1 = builder.orderBy("name");
            QueryBuilder<TestEntity> withOrder2 = builder.orderByDescending("age");
            QueryBuilder<TestEntity> withOrder3 = builder.orderBy("created_date", true);

            assertNotSame(builder, withOrder1, "orderBy() should return new instance");
            assertNotSame(builder, withOrder2, "orderByDescending() should return new instance");
            assertNotSame(builder, withOrder3, "orderBy(field, direction) should return new instance");
        }

        @Test
        @DisplayName("Original builder remains unchanged after operations")
        void originalBuilderRemainsUnchanged() {
            String originalSQL = builder.toSQL();

            // Perform operations that should not modify original
            builder.limit(10);
            builder.offset(5);
            builder.orderBy("name");
            builder.page(2, 20);

            assertEquals(originalSQL, builder.toSQL(), 
                "Original builder should remain unchanged");
        }
    }

    @Nested
    @DisplayName("Validation")
    class ValidationTests {

        @Test
        @DisplayName("limit() validates maxResults > 0")
        void limitValidation() {
            // Valid cases
            assertDoesNotThrow(() -> builder.limit(1));
            assertDoesNotThrow(() -> builder.limit(100));

            // Invalid cases
            assertThrows(IllegalArgumentException.class, () -> builder.limit(0),
                "limit(0) should throw IllegalArgumentException");
            assertThrows(IllegalArgumentException.class, () -> builder.limit(-1),
                "limit(-1) should throw IllegalArgumentException");
        }

        @Test
        @DisplayName("offset() validates skipCount >= 0")
        void offsetValidation() {
            // Valid cases
            assertDoesNotThrow(() -> builder.offset(0));
            assertDoesNotThrow(() -> builder.offset(1));
            assertDoesNotThrow(() -> builder.offset(100));

            // Invalid cases
            assertThrows(IllegalArgumentException.class, () -> builder.offset(-1),
                "offset(-1) should throw IllegalArgumentException");
        }

        @Test
        @DisplayName("page() validates pageNumber > 0 and pageSize > 0")
        void pageValidation() {
            // Valid cases
            assertDoesNotThrow(() -> builder.page(1, 1));
            assertDoesNotThrow(() -> builder.page(1, 10));
            assertDoesNotThrow(() -> builder.page(5, 20));

            // Invalid page numbers (should be >= 1, not 0-based)
            assertThrows(IllegalArgumentException.class, () -> builder.page(0, 10),
                "page(0, 10) should throw IllegalArgumentException - pages start from 1");
            assertThrows(IllegalArgumentException.class, () -> builder.page(-1, 10),
                "page(-1, 10) should throw IllegalArgumentException");

            // Invalid page sizes
            assertThrows(IllegalArgumentException.class, () -> builder.page(1, 0),
                "page(1, 0) should throw IllegalArgumentException");
            assertThrows(IllegalArgumentException.class, () -> builder.page(1, -1),
                "page(1, -1) should throw IllegalArgumentException");
        }

        @Test
        @DisplayName("orderBy field names must match [A-Za-z0-9_\\.]+")
        void orderByFieldNameValidation() {
            // Valid field names
            assertDoesNotThrow(() -> builder.orderBy("name"));
            assertDoesNotThrow(() -> builder.orderBy("field_name"));
            assertDoesNotThrow(() -> builder.orderBy("table.field"));
            assertDoesNotThrow(() -> builder.orderBy("field123"));

            // Invalid field names
            assertThrows(IllegalArgumentException.class, () -> builder.orderBy(""),
                "Empty field name should throw IllegalArgumentException");
            assertThrows(IllegalArgumentException.class, () -> builder.orderBy("field-name"),
                "Hyphen in field name should throw IllegalArgumentException");
            assertThrows(IllegalArgumentException.class, () -> builder.orderBy("field name"),
                "Space in field name should throw IllegalArgumentException");
        }
    }

    @Nested
    @DisplayName("Page Calculation Logic")
    class PageCalculationTests {

        @Test
        @DisplayName("Page numbering starts from 1 (not 0)")
        void pageNumberingStartsFromOne() {
            // This should be valid - page 1
            assertDoesNotThrow(() -> builder.page(1, 10));
            
            // This should be invalid - page 0
            assertThrows(IllegalArgumentException.class, () -> builder.page(0, 10),
                "Page numbering should start from 1, not 0");
        }

        @Test
        @DisplayName("Page calculation examples from issue")
        void pageCalculationExamples() {
            // page(1, 20) → OFFSET 0 LIMIT 20 (OFFSET 0 might be omitted)
            String sql1 = builder.page(1, 20).toSQL();
            assertTrue(sql1.contains("LIMIT 20"));
            // Don't check for OFFSET 0 as it might be omitted for optimization

            // page(2, 20) → OFFSET 20 LIMIT 20
            String sql2 = builder.page(2, 20).toSQL();
            assertTrue(sql2.contains("LIMIT 20"));
            assertTrue(sql2.contains("OFFSET 20"));

            // page(3, 15) → OFFSET 30 LIMIT 15
            String sql3 = builder.page(3, 15).toSQL();
            assertTrue(sql3.contains("LIMIT 15"));
            assertTrue(sql3.contains("OFFSET 30"));
        }
    }

    @Nested
    @DisplayName("Complex Combinations")
    class ComplexCombinationTests {

        @Test
        @DisplayName("Combining pagination with sorting")
        void combiningPaginationWithSorting() {
            String sql = builder
                .orderBy("name")
                .orderByDescending("created_date")
                .page(2, 10)
                .toSQL();
            
            assertTrue(sql.contains("ORDER BY name ASC, created_date DESC"));
            assertTrue(sql.contains("LIMIT 10"));
            assertTrue(sql.contains("OFFSET 10"));
        }

        @Test
        @DisplayName("Combining with WHERE conditions")
        void combiningWithWhereConditions() {
            String sql = builder
                .where("active", true)
                .orderBy("name")
                .limit(20)
                .offset(5)
                .toSQL();
            
            assertTrue(sql.contains("WHERE"));
            assertTrue(sql.contains("active ="));
            assertTrue(sql.contains("ORDER BY name ASC"));
            assertTrue(sql.contains("LIMIT 20"));
            assertTrue(sql.contains("OFFSET 5"));
        }

        @Test
        @DisplayName("Full query with all clauses in correct order")
        void fullQueryCorrectOrder() {
            String sql = builder
                .select("name", "age")
                .where("active", true)
                .groupBy("department")
                .having("COUNT(*)", ">", 1)
                .orderBy("name")
                .orderBy("age", false)
                .limit(25)
                .offset(50)
                .toSQL();
            
            // Verify all clauses are present
            assertTrue(sql.contains("SELECT name, age"));
            assertTrue(sql.contains("WHERE active ="));
            assertTrue(sql.contains("GROUP BY department"));
            assertTrue(sql.contains("HAVING COUNT(*) >"));
            assertTrue(sql.contains("ORDER BY name ASC, age DESC"));
            assertTrue(sql.contains("LIMIT 25"));
            assertTrue(sql.contains("OFFSET 50"));
            
            // Verify order is correct
            int selectPos = sql.indexOf("SELECT");
            int wherePos = sql.indexOf("WHERE");
            int groupByPos = sql.indexOf("GROUP BY");
            int havingPos = sql.indexOf("HAVING");
            int orderByPos = sql.indexOf("ORDER BY");
            int limitPos = sql.indexOf("LIMIT");
            int offsetPos = sql.indexOf("OFFSET");
            
            assertTrue(selectPos < wherePos);
            assertTrue(wherePos < groupByPos);
            assertTrue(groupByPos < havingPos);
            assertTrue(havingPos < orderByPos);
            assertTrue(orderByPos < limitPos);
            assertTrue(limitPos < offsetPos);
        }
    }

    // Test entity for type safety
    static class TestEntity {
        private String name;
        private int age;
        private boolean active;
        private String department;
    }
}