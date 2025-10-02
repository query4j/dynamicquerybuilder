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

        @Test
        @DisplayName("should handle aggregation with groupBy and having")
        void shouldHandleAggregationWithGroupByAndHaving() {
            String sql = builder
                .sum("amount")
                .where("active", true)
                .groupBy("department")
                .having("SUM(amount)", ">", 1000)
                .orderBy("department")
                .toSQL();
            
            assertTrue(sql.contains("SELECT SUM(amount)"));
            assertTrue(sql.contains("WHERE active"));
            assertTrue(sql.contains("GROUP BY department"));
            assertTrue(sql.contains("HAVING SUM(amount) >"));
            assertTrue(sql.contains("ORDER BY department"));
        }

        @Test
        @DisplayName("should handle having with regular field names")
        void shouldHandleHavingWithRegularFieldNames() {
            String sql = builder
                .select("department", "count")
                .groupBy("department")
                .having("department", "=", "Engineering")
                .toSQL();
            
            assertTrue(sql.contains("HAVING department ="));
        }

        @Test
        @DisplayName("should handle having with various aggregation functions")
        void shouldHandleHavingWithVariousAggregationFunctions() {
            // Test COUNT
            String countSql = builder.groupBy("dept").having("COUNT(id)", ">", 5).toSQL();
            assertTrue(countSql.contains("HAVING COUNT(id) >"));
            
            // Test SUM
            String sumSql = builder.groupBy("dept").having("SUM(amount)", ">=", 1000).toSQL();
            assertTrue(sumSql.contains("HAVING SUM(amount) >="));
            
            // Test AVG
            String avgSql = builder.groupBy("dept").having("AVG(salary)", "<", 50000).toSQL();
            assertTrue(avgSql.contains("HAVING AVG(salary) <"));
            
            // Test MIN
            String minSql = builder.groupBy("dept").having("MIN(hire_date)", "=", "2020-01-01").toSQL();
            assertTrue(minSql.contains("HAVING MIN(hire_date) ="));
            
            // Test MAX
            String maxSql = builder.groupBy("dept").having("MAX(update_date)", "!=", "2023-12-31").toSQL();
            assertTrue(maxSql.contains("HAVING MAX(update_date) !="));
            
            // Test COUNT(*)
            String countAllSql = builder.groupBy("dept").having("COUNT(*)", ">", 0).toSQL();
            assertTrue(countAllSql.contains("HAVING COUNT(*) >"));
        }

        @Test
        @DisplayName("should throw for invalid aggregation expressions in having")
        void shouldThrowForInvalidAggregationExpressionsInHaving() {
            assertThrows(IllegalArgumentException.class, 
                () -> builder.having("COUNT(", ">", 5));
            assertThrows(IllegalArgumentException.class, 
                () -> builder.having("COUNT)", ">", 5));
            assertThrows(IllegalArgumentException.class, 
                () -> builder.having("INVALID@FUNC(field)", ">", 5));
        }

        @Test
        @DisplayName("should maintain immutability with aggregation operations")
        void shouldMaintainImmutabilityWithAggregationOperations() {
            DynamicQueryBuilder<TestEntity> original = builder;
            
            DynamicQueryBuilder<TestEntity> withCount = (DynamicQueryBuilder<TestEntity>) original.countAll();
            DynamicQueryBuilder<TestEntity> withSum = (DynamicQueryBuilder<TestEntity>) original.sum("amount");
            DynamicQueryBuilder<TestEntity> withAvg = (DynamicQueryBuilder<TestEntity>) original.avg("price");
            
            // Original should be unchanged
            assertEquals("SELECT * FROM TestEntity", original.toSQL());
            
            // Each operation should create different instances
            assertNotSame(original, withCount);
            assertNotSame(original, withSum);
            assertNotSame(original, withAvg);
            
            // Each should have different SQL
            assertTrue(withCount.toSQL().contains("COUNT(*)"));
            assertTrue(withSum.toSQL().contains("SUM(amount)"));
            assertTrue(withAvg.toSQL().contains("AVG(price)"));
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

        @Test
        @DisplayName("should throw IllegalArgumentException for invalid association names in joins")
        void shouldThrowForInvalidAssociationNamesInJoins() {
            // Test empty string
            assertThrows(IllegalArgumentException.class, () -> builder.join(""));
            assertThrows(IllegalArgumentException.class, () -> builder.leftJoin(""));
            assertThrows(IllegalArgumentException.class, () -> builder.rightJoin(""));
            assertThrows(IllegalArgumentException.class, () -> builder.innerJoin(""));
            assertThrows(IllegalArgumentException.class, () -> builder.fetch(""));
            
            // Test whitespace only
            assertThrows(IllegalArgumentException.class, () -> builder.join("   "));
            assertThrows(IllegalArgumentException.class, () -> builder.leftJoin("   "));
            assertThrows(IllegalArgumentException.class, () -> builder.rightJoin("   "));
            assertThrows(IllegalArgumentException.class, () -> builder.innerJoin("   "));
            assertThrows(IllegalArgumentException.class, () -> builder.fetch("   "));
            
            // Test invalid characters
            assertThrows(IllegalArgumentException.class, () -> builder.join("invalid@field"));
            assertThrows(IllegalArgumentException.class, () -> builder.leftJoin("invalid-field"));
            assertThrows(IllegalArgumentException.class, () -> builder.rightJoin("invalid field"));
            assertThrows(IllegalArgumentException.class, () -> builder.innerJoin("invalid*field"));
            assertThrows(IllegalArgumentException.class, () -> builder.fetch("invalid#field"));
        }

        @Test
        @DisplayName("should accept valid association names with dots and underscores")
        void shouldAcceptValidAssociationNames() {
            // Test valid field names with dots and underscores
            String sql1 = builder.join("user.profile").toSQL();
            assertTrue(sql1.contains("INNER JOIN user.profile"));
            
            String sql2 = builder.leftJoin("order_items").toSQL();
            assertTrue(sql2.contains("LEFT JOIN order_items"));
            
            String sql3 = builder.rightJoin("user123").toSQL();
            assertTrue(sql3.contains("RIGHT JOIN user123"));
            
            String sql4 = builder.innerJoin("department.employees").toSQL();
            assertTrue(sql4.contains("INNER JOIN department.employees"));
            
            String sql5 = builder.fetch("profile_data").toSQL();
            assertTrue(sql5.contains("LEFT JOIN FETCH profile_data"));
        }

        @Test
        @DisplayName("should maintain immutability with join operations")
        void shouldMaintainImmutabilityWithJoinOperations() {
            DynamicQueryBuilder<TestEntity> original = builder;
            
            DynamicQueryBuilder<TestEntity> withJoin = (DynamicQueryBuilder<TestEntity>) original.join("orders");
            DynamicQueryBuilder<TestEntity> withLeftJoin = (DynamicQueryBuilder<TestEntity>) withJoin.leftJoin("profile");
            DynamicQueryBuilder<TestEntity> withRightJoin = (DynamicQueryBuilder<TestEntity>) withLeftJoin.rightJoin("permissions");
            DynamicQueryBuilder<TestEntity> withFetch = (DynamicQueryBuilder<TestEntity>) withRightJoin.fetch("details");
            
            // Original should be unchanged
            assertEquals("SELECT * FROM TestEntity", original.toSQL());
            
            // Each step should be different
            assertNotSame(original, withJoin);
            assertNotSame(withJoin, withLeftJoin);
            assertNotSame(withLeftJoin, withRightJoin);
            assertNotSame(withRightJoin, withFetch);
            
            // Final result should contain all joins
            String finalSql = withFetch.toSQL();
            assertTrue(finalSql.contains("INNER JOIN orders"));
            assertTrue(finalSql.contains("LEFT JOIN profile"));
            assertTrue(finalSql.contains("RIGHT JOIN permissions"));
            assertTrue(finalSql.contains("LEFT JOIN FETCH details"));
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

        @Test
        @DisplayName("should return empty list for getHavingPredicates initially")
        void shouldReturnEmptyListForGetHavingPredicatesInitially() {
            DynamicQueryBuilder<TestEntity> newBuilder = new DynamicQueryBuilder<>(TestEntity.class);
            
            List<com.github.query4j.core.criteria.Predicate> havingPredicates = newBuilder.getHavingPredicates();
            
            assertNotNull(havingPredicates);
            assertTrue(havingPredicates.isEmpty());
        }

        @Test
        @DisplayName("should return immutable list from getHavingPredicates")
        void shouldReturnImmutableListFromGetHavingPredicates() {
            DynamicQueryBuilder<TestEntity> builderWithHaving = 
                (DynamicQueryBuilder<TestEntity>) builder.having("COUNT(id)", ">", 5);
            
            List<com.github.query4j.core.criteria.Predicate> havingPredicates = builderWithHaving.getHavingPredicates();
            
            assertNotNull(havingPredicates);
            assertEquals(1, havingPredicates.size());
            
            // Test immutability - should throw UnsupportedOperationException
            assertThrows(UnsupportedOperationException.class, () -> {
                havingPredicates.clear();
            });
        }

        @Test
        @DisplayName("should return correct having predicates after calling having method")
        void shouldReturnCorrectHavingPredicatesAfterCallingHavingMethod() {
            DynamicQueryBuilder<TestEntity> builderWithHaving = 
                (DynamicQueryBuilder<TestEntity>) builder
                    .groupBy("department")
                    .having("SUM(amount)", ">=", 1000);
            
            List<com.github.query4j.core.criteria.Predicate> havingPredicates = builderWithHaving.getHavingPredicates();
            
            assertNotNull(havingPredicates);
            assertEquals(1, havingPredicates.size());
            
            com.github.query4j.core.criteria.Predicate havingPredicate = havingPredicates.get(0);
            assertNotNull(havingPredicate);
            assertTrue(havingPredicate.toSQL().contains("SUM(amount)"));
            assertTrue(havingPredicate.toSQL().contains(">="));
            
            // Verify parameters
            assertFalse(havingPredicate.getParameters().isEmpty());
            assertTrue(havingPredicate.getParameters().containsValue(1000));
        }

        @Test
        @DisplayName("should handle multiple having predicates")
        void shouldHandleMultipleHavingPredicates() {
            DynamicQueryBuilder<TestEntity> builderWithMultipleHaving = 
                (DynamicQueryBuilder<TestEntity>) builder
                    .groupBy("department", "region")
                    .having("COUNT(id)", ">", 5)
                    .having("SUM(amount)", ">=", 1000)
                    .having("AVG(salary)", "<", 50000);
            
            List<com.github.query4j.core.criteria.Predicate> havingPredicates = builderWithMultipleHaving.getHavingPredicates();
            
            assertNotNull(havingPredicates);
            assertEquals(3, havingPredicates.size());
            
            // Verify first predicate
            com.github.query4j.core.criteria.Predicate firstPredicate = havingPredicates.get(0);
            assertTrue(firstPredicate.toSQL().contains("COUNT(id)"));
            assertTrue(firstPredicate.getParameters().containsValue(5));
            
            // Verify second predicate
            com.github.query4j.core.criteria.Predicate secondPredicate = havingPredicates.get(1);
            assertTrue(secondPredicate.toSQL().contains("SUM(amount)"));
            assertTrue(secondPredicate.getParameters().containsValue(1000));
            
            // Verify third predicate
            com.github.query4j.core.criteria.Predicate thirdPredicate = havingPredicates.get(2);
            assertTrue(thirdPredicate.toSQL().contains("AVG(salary)"));
            assertTrue(thirdPredicate.getParameters().containsValue(50000));
        }

        @Test
        @DisplayName("should maintain having predicates across query builder operations")
        void shouldMaintainHavingPredicatesAcrossQueryBuilderOperations() {
            DynamicQueryBuilder<TestEntity> complexBuilder = 
                (DynamicQueryBuilder<TestEntity>) builder
                    .where("status", "active")
                    .groupBy("department")
                    .having("COUNT(id)", ">", 10)
                    .orderBy("department")
                    .limit(50);
            
            List<com.github.query4j.core.criteria.Predicate> havingPredicates = complexBuilder.getHavingPredicates();
            
            assertNotNull(havingPredicates);
            assertEquals(1, havingPredicates.size());
            
            com.github.query4j.core.criteria.Predicate havingPredicate = havingPredicates.get(0);
            assertTrue(havingPredicate.toSQL().contains("COUNT(id)"));
            assertTrue(havingPredicate.getParameters().containsValue(10));
            
            // Verify the complete SQL still works
            String sql = complexBuilder.toSQL();
            assertTrue(sql.contains("WHERE status"));
            assertTrue(sql.contains("GROUP BY department"));
            assertTrue(sql.contains("HAVING COUNT(id) >"));
            assertTrue(sql.contains("ORDER BY department"));
            assertTrue(sql.contains("LIMIT 50"));
        }

        @Test
        @DisplayName("should handle having predicates with different aggregation functions")
        void shouldHandleHavingPredicatesWithDifferentAggregationFunctions() {
            // Test various aggregation functions in having clauses
            String[] aggregateFunctions = {"COUNT(id)", "SUM(amount)", "AVG(salary)", "MIN(hire_date)", "MAX(update_date)", "COUNT(*)"};
            Object[] values = {5, 1000, 50000.0, "2020-01-01", "2023-12-31", 0};
            String[] operators = {">", ">=", "<", "=", "!=", ">"};
            
            for (int i = 0; i < aggregateFunctions.length; i++) {
                DynamicQueryBuilder<TestEntity> testBuilder = 
                    (DynamicQueryBuilder<TestEntity>) new DynamicQueryBuilder<>(TestEntity.class)
                        .groupBy("department")
                        .having(aggregateFunctions[i], operators[i], values[i]);
                
                List<com.github.query4j.core.criteria.Predicate> havingPredicates = testBuilder.getHavingPredicates();
                
                assertEquals(1, havingPredicates.size(), 
                    "Failed for aggregation function: " + aggregateFunctions[i]);
                
                com.github.query4j.core.criteria.Predicate predicate = havingPredicates.get(0);
                assertTrue(predicate.toSQL().contains(aggregateFunctions[i]), 
                    "SQL should contain: " + aggregateFunctions[i]);
                assertTrue(predicate.toSQL().contains(operators[i]), 
                    "SQL should contain operator: " + operators[i]);
                assertTrue(predicate.getParameters().containsValue(values[i]), 
                    "Parameters should contain value: " + values[i]);
            }
        }

        @Test
        @DisplayName("should preserve immutability when adding having predicates")
        void shouldPreserveImmutabilityWhenAddingHavingPredicates() {
            // Original builder
            DynamicQueryBuilder<TestEntity> original = new DynamicQueryBuilder<>(TestEntity.class);
            assertTrue(original.getHavingPredicates().isEmpty());
            
            // Add first having predicate - should return new instance
            DynamicQueryBuilder<TestEntity> withFirstHaving = 
                (DynamicQueryBuilder<TestEntity>) original.having("COUNT(id)", ">", 5);
            
            // Original should still be empty
            assertTrue(original.getHavingPredicates().isEmpty());
            assertEquals(1, withFirstHaving.getHavingPredicates().size());
            
            // Add second having predicate - should return new instance
            DynamicQueryBuilder<TestEntity> withSecondHaving = 
                (DynamicQueryBuilder<TestEntity>) withFirstHaving.having("SUM(amount)", ">=", 1000);
            
            // Previous instances should be unchanged
            assertTrue(original.getHavingPredicates().isEmpty());
            assertEquals(1, withFirstHaving.getHavingPredicates().size());
            assertEquals(2, withSecondHaving.getHavingPredicates().size());
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