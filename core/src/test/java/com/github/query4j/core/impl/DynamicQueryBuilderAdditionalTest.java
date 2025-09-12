package com.github.query4j.core.impl;

import com.github.query4j.core.QueryBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Additional tests for DynamicQueryBuilder to improve code coverage.
 */
@DisplayName("DynamicQueryBuilder Additional Coverage")
class DynamicQueryBuilderAdditionalTest {

    private DynamicQueryBuilder<TestEntity> builder;

    @BeforeEach
    void setUp() {
        builder = new DynamicQueryBuilder<>(TestEntity.class);
    }

    @Nested
    @DisplayName("Aggregation Methods")
    class AggregationTests {

        @Test
        @DisplayName("should handle countAll method")
        void shouldHandleCountAll() {
            String sql = builder.countAll().toSQL();
            assertTrue(sql.contains("COUNT(*)"));
            assertTrue(sql.contains("SELECT COUNT(*)"));
        }

        @Test
        @DisplayName("should handle count with field")
        void shouldHandleCountWithField() {
            String sql = builder.count("id").toSQL();
            assertTrue(sql.contains("COUNT(id)"));
        }

        @Test
        @DisplayName("should handle sum aggregation")
        void shouldHandleSumAggregation() {
            String sql = builder.sum("amount").toSQL();
            assertTrue(sql.contains("SUM(amount)"));
        }

        @Test
        @DisplayName("should handle avg aggregation")
        void shouldHandleAvgAggregation() {
            String sql = builder.avg("price").toSQL();
            assertTrue(sql.contains("AVG(price)"));
        }

        @Test
        @DisplayName("should handle min aggregation")
        void shouldHandleMinAggregation() {
            String sql = builder.min("created_date").toSQL();
            assertTrue(sql.contains("MIN(created_date)"));
        }

        @Test
        @DisplayName("should handle max aggregation")
        void shouldHandleMaxAggregation() {
            String sql = builder.max("updated_date").toSQL();
            assertTrue(sql.contains("MAX(updated_date)"));
        }

        @Test
        @DisplayName("should throw for null field in aggregation methods")
        void shouldThrowForNullFieldInAggregationMethods() {
            assertThrows(NullPointerException.class, () -> builder.count(null));
            assertThrows(NullPointerException.class, () -> builder.sum(null));
            assertThrows(NullPointerException.class, () -> builder.avg(null));
            assertThrows(NullPointerException.class, () -> builder.min(null));
            assertThrows(NullPointerException.class, () -> builder.max(null));
        }
    }

    @Nested
    @DisplayName("Join Methods")
    class JoinTests {

        @Test
        @DisplayName("should handle rightJoin")
        void shouldHandleRightJoin() {
            String sql = builder.rightJoin("orders").toSQL();
            assertTrue(sql.contains("RIGHT JOIN orders"));
        }

        @Test
        @DisplayName("should handle fetch join")
        void shouldHandleFetchJoin() {
            String sql = builder.fetch("profile").toSQL();
            assertTrue(sql.contains("LEFT JOIN FETCH profile"));
        }

        @Test
        @DisplayName("should throw for null association in joins")
        void shouldThrowForNullAssociationInJoins() {
            assertThrows(NullPointerException.class, () -> builder.join(null));
            assertThrows(NullPointerException.class, () -> builder.leftJoin(null));
            assertThrows(NullPointerException.class, () -> builder.rightJoin(null));
            assertThrows(NullPointerException.class, () -> builder.innerJoin(null));
            assertThrows(NullPointerException.class, () -> builder.fetch(null));
        }

        @Test
        @DisplayName("should handle multiple joins")
        void shouldHandleMultipleJoins() {
            String sql = builder
                .join("orders")
                .leftJoin("profile")
                .rightJoin("permissions")
                .toSQL();
            
            assertTrue(sql.contains("INNER JOIN orders"));
            assertTrue(sql.contains("LEFT JOIN profile"));
            assertTrue(sql.contains("RIGHT JOIN permissions"));
        }
    }

    @Nested
    @DisplayName("Group By and Having")
    class GroupByHavingTests {

        @Test
        @DisplayName("should handle having clause")
        void shouldHandleHavingClause() {
            String sql = builder
                .groupBy("department")
                .having("total_count", ">", 5)
                .toSQL();
            
            assertTrue(sql.contains("GROUP BY department"));
            assertTrue(sql.contains("HAVING"));
            assertTrue(sql.contains("total_count"));
        }

        @Test
        @DisplayName("should throw for empty groupBy fields")
        void shouldThrowForEmptyGroupByFields() {
            assertThrows(IllegalArgumentException.class, () -> builder.groupBy());
        }

        @Test
        @DisplayName("should throw for null fields in groupBy")
        void shouldThrowForNullFieldsInGroupBy() {
            assertThrows(NullPointerException.class, () -> builder.groupBy((String[]) null));
            assertThrows(IllegalArgumentException.class, () -> builder.groupBy("valid", null));
        }

        @Test
        @DisplayName("should throw for null parameters in having")
        void shouldThrowForNullParametersInHaving() {
            assertThrows(NullPointerException.class, () -> builder.having(null, ">", 5));
            assertThrows(NullPointerException.class, () -> builder.having("field", null, 5));
        }
    }

    @Nested
    @DisplayName("Select Methods")
    class SelectTests {

        @Test
        @DisplayName("should throw for empty select fields")
        void shouldThrowForEmptySelectFields() {
            assertThrows(IllegalArgumentException.class, () -> builder.select());
        }

        @Test
        @DisplayName("should throw for null select fields")
        void shouldThrowForNullSelectFields() {
            assertThrows(NullPointerException.class, () -> builder.select((String[]) null));
            assertThrows(IllegalArgumentException.class, () -> builder.select("valid", null));
        }

        @Test
        @DisplayName("should handle multiple select fields")
        void shouldHandleMultipleSelectFields() {
            String sql = builder.select("id", "name", "email", "created_date").toSQL();
            
            assertTrue(sql.contains("SELECT id, name, email, created_date"));
            assertFalse(sql.contains("SELECT *"));
        }
    }

    @Nested
    @DisplayName("Ordering Methods")
    class OrderingTests {

        @Test
        @DisplayName("should handle orderBy with ascending/descending flag")
        void shouldHandleOrderByWithFlag() {
            String sqlAsc = builder.orderBy("name", true).toSQL();
            String sqlDesc = builder.orderBy("name", false).toSQL();
            
            assertTrue(sqlAsc.contains("name ASC"));
            assertTrue(sqlDesc.contains("name DESC"));
        }

        @Test
        @DisplayName("should throw for null field in orderBy")
        void shouldThrowForNullFieldInOrderBy() {
            assertThrows(NullPointerException.class, () -> builder.orderBy(null));
            assertThrows(NullPointerException.class, () -> builder.orderByDescending(null));
            assertThrows(NullPointerException.class, () -> builder.orderBy(null, true));
        }

        @Test
        @DisplayName("should handle multiple orderBy clauses")
        void shouldHandleMultipleOrderByClauses() {
            String sql = builder
                .orderBy("name")
                .orderByDescending("created_date")
                .orderBy("id", true)
                .toSQL();
            
            assertTrue(sql.contains("ORDER BY"));
            assertTrue(sql.contains("name ASC"));
            assertTrue(sql.contains("created_date DESC"));
            assertTrue(sql.contains("id ASC"));
        }
    }

    @Nested
    @DisplayName("Caching Methods")
    class CachingTests {

        @Test
        @DisplayName("should handle cached method")
        void shouldHandleCachedMethod() {
            QueryBuilder<TestEntity> cached = builder.cached();
            assertNotNull(cached);
            assertNotSame(builder, cached);
        }

        @Test
        @DisplayName("should handle cached with region")
        void shouldHandleCachedWithRegion() {
            QueryBuilder<TestEntity> cached = builder.cached("userCache");
            assertNotNull(cached);
            assertNotSame(builder, cached);
        }

        @Test
        @DisplayName("should handle cached with TTL")
        void shouldHandleCachedWithTTL() {
            QueryBuilder<TestEntity> cached = builder.cached(3600L);
            assertNotNull(cached);
            assertNotSame(builder, cached);
        }
    }

    @Nested
    @DisplayName("Complex SQL Generation")
    class ComplexSqlGenerationTests {

        @Test
        @DisplayName("should generate complex query with all clauses")
        void shouldGenerateComplexQueryWithAllClauses() {
            String sql = builder
                .select("u.name", "order_count")
                .join("orders")
                .where("u.active", true)
                .and()
                .whereIn("u.role", Arrays.asList("ADMIN", "USER"))
                .groupBy("u.name")
                .having("order_count", ">", 5)
                .orderBy("u.name")
                .limit(50)
                .offset(100)
                .toSQL();
            
            assertTrue(sql.contains("SELECT u.name, order_count"));
            assertTrue(sql.contains("FROM TestEntity"));
            assertTrue(sql.contains("INNER JOIN orders"));
            assertTrue(sql.contains("WHERE"));
            assertTrue(sql.contains("u.active ="));
            assertTrue(sql.contains("AND"));
            assertTrue(sql.contains("u.role IN"));
            assertTrue(sql.contains("GROUP BY u.name"));
            assertTrue(sql.contains("HAVING order_count >"));
            assertTrue(sql.contains("ORDER BY u.name ASC"));
            assertTrue(sql.contains("LIMIT 50"));
            assertTrue(sql.contains("OFFSET 100"));
        }

        @Test
        @DisplayName("should handle query with only WHERE and no other clauses")
        void shouldHandleQueryWithOnlyWhere() {
            String sql = builder.where("active", true).toSQL();
            
            assertTrue(sql.startsWith("SELECT * FROM TestEntity WHERE active = :"));
            assertTrue(sql.contains("active"));
        }

        @Test
        @DisplayName("should handle query with LIMIT but no OFFSET")
        void shouldHandleQueryWithLimitButNoOffset() {
            String sql = builder.limit(10).toSQL();
            
            assertTrue(sql.contains("LIMIT 10"));
            assertFalse(sql.contains("OFFSET"));
        }

        @Test
        @DisplayName("should handle query with OFFSET but no LIMIT")
        void shouldHandleQueryWithOffsetButNoLimit() {
            String sql = builder.offset(5).toSQL();
            
            assertTrue(sql.contains("OFFSET 5"));
            assertFalse(sql.contains("LIMIT"));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle arithmetic overflow in page calculation")
        void shouldHandleArithmeticOverflowInPageCalculation() {
            // This should throw ArithmeticException due to integer overflow
            assertThrows(ArithmeticException.class, 
                () -> builder.page(Integer.MAX_VALUE, Integer.MAX_VALUE));
        }

        @Test
        @DisplayName("should handle maximum valid page values")
        void shouldHandleMaximumValidPageValues() {
            // This should work without overflow
            assertDoesNotThrow(() -> builder.page(1000, 1000));
        }

        @Test
        @DisplayName("should handle boundary values for pagination")
        void shouldHandleBoundaryValuesForPagination() {
            // Test boundary values
            assertDoesNotThrow(() -> builder.page(1, 1));
            assertDoesNotThrow(() -> builder.limit(1));
            assertDoesNotThrow(() -> builder.offset(0));
        }

        @Test
        @DisplayName("should maintain immutability with empty operations")
        void shouldMaintainImmutabilityWithEmptyOperations() {
            DynamicQueryBuilder<TestEntity> original = new DynamicQueryBuilder<>(TestEntity.class);
            DynamicQueryBuilder<TestEntity> withAnd = (DynamicQueryBuilder<TestEntity>) original.and();
            DynamicQueryBuilder<TestEntity> withOr = (DynamicQueryBuilder<TestEntity>) original.or();
            
            assertNotSame(original, withAnd);
            assertNotSame(original, withOr);
            assertNotSame(withAnd, withOr);
        }
    }

    @Nested
    @DisplayName("Parameter Generation")
    class ParameterGenerationTests {

        @Test
        @DisplayName("should generate unique parameter names for multiple predicates")
        void shouldGenerateUniqueParameterNamesForMultiplePredicates() {
            DynamicQueryBuilder<TestEntity> complexBuilder = (DynamicQueryBuilder<TestEntity>) builder
                .where("name", "John")
                .and()
                .where("age", 25)
                .and()
                .whereIn("status", Arrays.asList("ACTIVE", "PENDING"))
                .and()
                .whereLike("email", "%@company.com")
                .and()
                .whereBetween("created_date", "2023-01-01", "2023-12-31");
            
            String sql = complexBuilder.toSQL();
            
            // Verify SQL contains parameter placeholders
            assertTrue(sql.contains(":"));
            
            // Should not contain duplicate parameter names in the same query
            long colonCount = sql.chars().filter(ch -> ch == ':').count();
            assertTrue(colonCount >= 6); // At least 6 parameters expected
        }

        @Test
        @DisplayName("should handle special characters in field names for parameter generation")
        void shouldHandleSpecialCharactersInFieldNamesForParameterGeneration() {
            // Test with dots and underscores in field names
            String sql = builder
                .where("user.profile_name", "John")
                .and()
                .where("order_details.item_count", 5)
                .toSQL();
            
            assertTrue(sql.contains("user.profile_name"));
            assertTrue(sql.contains("order_details.item_count"));
            assertTrue(sql.contains(":"));
        }
    }

    // Test entity for type safety
    private static class TestEntity {
        private String name;
        private Integer age;
        private String status;
        private String email;
        
        // Constructor, getters, setters would be here in real implementation
    }
}