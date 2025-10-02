package com.github.query4j.core.criteria;

import com.github.query4j.core.QueryBuilder;
import com.github.query4j.core.impl.DynamicQueryBuilder;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for SubqueryInPredicate.
 */
class SubqueryInPredicateTest {

    // Simple test entities
    static class TestUser {
        private Long id;
        private String name;
        // Getters and setters omitted for brevity
    }

    static class TestOrder {
        private Long id;
        private String status;
        private Long userId;
        // Getters and setters omitted for brevity
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create IN predicate with valid parameters")
        void shouldCreateInPredicateWithValidParameters() {
            QueryBuilder<TestOrder> subquery = QueryBuilder.forEntity(TestOrder.class)
                .select("userId");

            SubqueryInPredicate predicate = new SubqueryInPredicate("id", "IN", subquery);

            assertEquals("id", predicate.getFieldName());
            assertEquals("IN", predicate.getOperator());
            assertEquals(subquery, predicate.getSubquery());
        }

        @Test
        @DisplayName("Should create NOT IN predicate with valid parameters")
        void shouldCreateNotInPredicateWithValidParameters() {
            QueryBuilder<TestOrder> subquery = QueryBuilder.forEntity(TestOrder.class)
                .select("userId");

            SubqueryInPredicate predicate = new SubqueryInPredicate("id", "NOT IN", subquery);

            assertEquals("id", predicate.getFieldName());
            assertEquals("NOT IN", predicate.getOperator());
            assertEquals(subquery, predicate.getSubquery());
        }

        @Test
        @DisplayName("Should normalize operator to uppercase")
        void shouldNormalizeOperatorToUppercase() {
            QueryBuilder<TestOrder> subquery = QueryBuilder.forEntity(TestOrder.class);

            SubqueryInPredicate predicate1 = new SubqueryInPredicate("id", "in", subquery);
            SubqueryInPredicate predicate2 = new SubqueryInPredicate("id", "not in", subquery);

            assertEquals("IN", predicate1.getOperator());
            assertEquals("NOT IN", predicate2.getOperator());
        }

        @Test
        @DisplayName("Should handle operator with whitespace")
        void shouldHandleOperatorWithWhitespace() {
            QueryBuilder<TestOrder> subquery = QueryBuilder.forEntity(TestOrder.class);

            SubqueryInPredicate predicate = new SubqueryInPredicate("id", "  IN  ", subquery);

            assertEquals("IN", predicate.getOperator());
        }

        @Test
        @DisplayName("Should trim field name whitespace")
        void shouldTrimFieldNameWhitespace() {
            QueryBuilder<TestOrder> subquery = QueryBuilder.forEntity(TestOrder.class);

            SubqueryInPredicate predicate = new SubqueryInPredicate("  fieldName  ", "IN", subquery);

            assertEquals("fieldName", predicate.getFieldName());
        }

        @Test
        @DisplayName("Should reject null field name")
        void shouldRejectNullFieldName() {
            QueryBuilder<TestOrder> subquery = QueryBuilder.forEntity(TestOrder.class);

            assertThrows(com.github.query4j.core.QueryBuildException.class, () ->
                new SubqueryInPredicate(null, "IN", subquery)
            );
        }

        @Test
        @DisplayName("Should reject empty field name")
        void shouldRejectEmptyFieldName() {
            QueryBuilder<TestOrder> subquery = QueryBuilder.forEntity(TestOrder.class);

            assertThrows(com.github.query4j.core.QueryBuildException.class, () ->
                new SubqueryInPredicate("", "IN", subquery)
            );

            assertThrows(com.github.query4j.core.QueryBuildException.class, () ->
                new SubqueryInPredicate("   ", "IN", subquery)
            );
        }

        @Test
        @DisplayName("Should reject invalid field name")
        void shouldRejectInvalidFieldName() {
            QueryBuilder<TestOrder> subquery = QueryBuilder.forEntity(TestOrder.class);

            assertThrows(com.github.query4j.core.QueryBuildException.class, () ->
                new SubqueryInPredicate("invalid-field!", "IN", subquery)
            );
        }

        @Test
        @DisplayName("Should reject null operator")
        void shouldRejectNullOperator() {
            QueryBuilder<TestOrder> subquery = QueryBuilder.forEntity(TestOrder.class);

            assertThrows(IllegalArgumentException.class, () ->
                new SubqueryInPredicate("id", null, subquery)
            );
        }

        @Test
        @DisplayName("Should reject empty operator")
        void shouldRejectEmptyOperator() {
            QueryBuilder<TestOrder> subquery = QueryBuilder.forEntity(TestOrder.class);

            assertThrows(IllegalArgumentException.class, () ->
                new SubqueryInPredicate("id", "", subquery)
            );

            assertThrows(IllegalArgumentException.class, () ->
                new SubqueryInPredicate("id", "   ", subquery)
            );
        }

        @Test
        @DisplayName("Should reject invalid operator")
        void shouldRejectInvalidOperator() {
            QueryBuilder<TestOrder> subquery = QueryBuilder.forEntity(TestOrder.class);

            assertThrows(IllegalArgumentException.class, () ->
                new SubqueryInPredicate("id", "INVALID", subquery)
            );

            assertThrows(IllegalArgumentException.class, () ->
                new SubqueryInPredicate("id", "EXISTS", subquery)
            );
        }

        @Test
        @DisplayName("Should reject null subquery")
        void shouldRejectNullSubquery() {
            assertThrows(IllegalArgumentException.class, () ->
                new SubqueryInPredicate("id", "IN", null)
            );
        }
    }

    @Nested
    @DisplayName("SQL Generation Tests")
    class SqlGenerationTests {

        @Test
        @DisplayName("Should generate SQL for IN predicate")
        void shouldGenerateSqlForInPredicate() {
            QueryBuilder<TestOrder> subquery = new DynamicQueryBuilder<>(TestOrder.class)
                .select("userId");

            SubqueryInPredicate predicate = new SubqueryInPredicate("id", "IN", subquery);

            String expectedSql = "id IN (" + subquery.toSQL() + ")";
            assertEquals(expectedSql, predicate.toSQL());
        }

        @Test
        @DisplayName("Should generate SQL for NOT IN predicate")
        void shouldGenerateSqlForNotInPredicate() {
            QueryBuilder<TestOrder> subquery = new DynamicQueryBuilder<>(TestOrder.class)
                .select("userId");

            SubqueryInPredicate predicate = new SubqueryInPredicate("id", "NOT IN", subquery);

            String expectedSql = "id NOT IN (" + subquery.toSQL() + ")";
            assertEquals(expectedSql, predicate.toSQL());
        }

        @Test
        @DisplayName("Should generate SQL with complex subquery")
        void shouldGenerateSqlWithComplexSubquery() {
            QueryBuilder<TestOrder> subquery = new DynamicQueryBuilder<>(TestOrder.class)
                .select("userId")
                .where("status", "ACTIVE")
                .and()
                .where("amount", ">", 100)
                .orderBy("orderDate");

            SubqueryInPredicate predicate = new SubqueryInPredicate("userId", "IN", subquery);

            String sql = predicate.toSQL();
            assertTrue(sql.startsWith("userId IN ("));
            assertTrue(sql.endsWith(")"));
            assertTrue(sql.contains("status = :p1"));
            assertTrue(sql.contains("amount > :p2"));
        }
    }

    @Nested
    @DisplayName("Parameter Mapping Tests")
    class ParameterMappingTests {

        @Test
        @DisplayName("Should return parameters from DynamicQueryBuilder subquery")
        void shouldReturnParametersFromDynamicQueryBuilder() {
            QueryBuilder<TestOrder> subquery = new DynamicQueryBuilder<>(TestOrder.class)
                .select("userId")
                .where("status", "ACTIVE")
                .and()
                .where("amount", ">", 100);

            SubqueryInPredicate predicate = new SubqueryInPredicate("userId", "IN", subquery);

            Map<String, Object> parameters = predicate.getParameters();
            assertFalse(parameters.isEmpty());
            assertTrue(parameters.containsValue("ACTIVE"));
            assertTrue(parameters.containsValue(100));
        }

        @Test
        @DisplayName("Should return empty parameters for subquery with no predicates")
        void shouldReturnEmptyParametersForSubqueryWithNoPredicates() {
            QueryBuilder<TestOrder> subquery = new DynamicQueryBuilder<>(TestOrder.class)
                .select("userId");

            SubqueryInPredicate predicate = new SubqueryInPredicate("userId", "IN", subquery);

            Map<String, Object> parameters = predicate.getParameters();
            assertTrue(parameters.isEmpty());
        }
    }

    @Nested
    @DisplayName("Equality and Immutability Tests")
    class EqualityAndImmutabilityTests {

        @Test
        @DisplayName("Should be equal when all properties match")
        void shouldBeEqualWhenPropertiesMatch() {
            QueryBuilder<TestOrder> subquery = new DynamicQueryBuilder<>(TestOrder.class)
                .select("userId")
                .where("status", "ACTIVE");

            SubqueryInPredicate predicate1 = new SubqueryInPredicate("id", "IN", subquery);
            SubqueryInPredicate predicate2 = new SubqueryInPredicate("id", "IN", subquery);

            assertEquals(predicate1, predicate2);
            assertEquals(predicate1.hashCode(), predicate2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when field names differ")
        void shouldNotBeEqualWhenFieldNamesDiffer() {
            QueryBuilder<TestOrder> subquery = new DynamicQueryBuilder<>(TestOrder.class)
                .select("userId");

            SubqueryInPredicate predicate1 = new SubqueryInPredicate("id", "IN", subquery);
            SubqueryInPredicate predicate2 = new SubqueryInPredicate("userId", "IN", subquery);

            assertNotEquals(predicate1, predicate2);
        }

        @Test
        @DisplayName("Should not be equal when operators differ")
        void shouldNotBeEqualWhenOperatorsDiffer() {
            QueryBuilder<TestOrder> subquery = new DynamicQueryBuilder<>(TestOrder.class)
                .select("userId");

            SubqueryInPredicate predicate1 = new SubqueryInPredicate("id", "IN", subquery);
            SubqueryInPredicate predicate2 = new SubqueryInPredicate("id", "NOT IN", subquery);

            assertNotEquals(predicate1, predicate2);
        }

        @Test
        @DisplayName("Should not be equal when subqueries differ")
        void shouldNotBeEqualWhenSubqueriesDiffer() {
            QueryBuilder<TestOrder> subquery1 = new DynamicQueryBuilder<>(TestOrder.class)
                .select("userId")
                .where("status", "ACTIVE");
            QueryBuilder<TestOrder> subquery2 = new DynamicQueryBuilder<>(TestOrder.class)
                .select("userId")
                .where("status", "CANCELLED");

            SubqueryInPredicate predicate1 = new SubqueryInPredicate("id", "IN", subquery1);
            SubqueryInPredicate predicate2 = new SubqueryInPredicate("id", "IN", subquery2);

            assertNotEquals(predicate1, predicate2);
        }
    }

    @Nested
    @DisplayName("Field Validation Tests")
    class FieldValidationTests {

        @Test
        @DisplayName("Should accept valid field names")
        void shouldAcceptValidFieldNames() {
            QueryBuilder<TestOrder> subquery = QueryBuilder.forEntity(TestOrder.class);

            assertDoesNotThrow(() ->
                new SubqueryInPredicate("fieldName", "IN", subquery)
            );

            assertDoesNotThrow(() ->
                new SubqueryInPredicate("field_name", "IN", subquery)
            );

            assertDoesNotThrow(() ->
                new SubqueryInPredicate("field123", "IN", subquery)
            );

            assertDoesNotThrow(() ->
                new SubqueryInPredicate("table.field", "IN", subquery)
            );
        }

        @Test
        @DisplayName("Should reject field names with special characters")
        void shouldRejectFieldNamesWithSpecialCharacters() {
            QueryBuilder<TestOrder> subquery = QueryBuilder.forEntity(TestOrder.class);

            assertThrows(com.github.query4j.core.QueryBuildException.class, () ->
                new SubqueryInPredicate("field-name", "IN", subquery)
            );

            assertThrows(com.github.query4j.core.QueryBuildException.class, () ->
                new SubqueryInPredicate("field@name", "IN", subquery)
            );

            assertThrows(com.github.query4j.core.QueryBuildException.class, () ->
                new SubqueryInPredicate("field name", "IN", subquery)
            );
        }
    }
}