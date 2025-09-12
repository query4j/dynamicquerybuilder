package com.github.query4j.core.criteria;

import com.github.query4j.core.QueryBuildException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import com.github.query4j.core.QueryBuildException;

/**
 * Comprehensive unit tests for LogicalPredicate class.
 * Tests SQL generation, parameter mapping, immutability, and validation.
 */
@DisplayName("LogicalPredicate")
class LogicalPredicateTest {

    @Nested
    @DisplayName("Constructor and Basic Properties")
    class ConstructorTests {

        @Test
        @DisplayName("should create valid AND predicate")
        void shouldCreateValidAndPredicate() {
            Predicate pred1 = new SimplePredicate("name", "=", "John", "p1");
            Predicate pred2 = new SimplePredicate("age", ">", 18, "p2");
            List<Predicate> children = Arrays.asList(pred1, pred2);
            
            LogicalPredicate predicate = new LogicalPredicate("AND", children);
            
            assertEquals("AND", predicate.getOperator());
            assertEquals(children, predicate.getChildren());
        }

        @Test
        @DisplayName("should create valid OR predicate")
        void shouldCreateValidOrPredicate() {
            Predicate pred1 = new SimplePredicate("status", "=", "ACTIVE", "p1");
            Predicate pred2 = new SimplePredicate("status", "=", "PENDING", "p2");
            List<Predicate> children = Arrays.asList(pred1, pred2);
            
            LogicalPredicate predicate = new LogicalPredicate("OR", children);
            
            assertEquals("OR", predicate.getOperator());
            assertEquals(children, predicate.getChildren());
        }

        @Test
        @DisplayName("should create valid NOT predicate with single child")
        void shouldCreateValidNotPredicate() {
            Predicate child = new SimplePredicate("active", "=", true, "p1");
            List<Predicate> children = Arrays.asList(child);
            
            LogicalPredicate predicate = new LogicalPredicate("NOT", children);
            
            assertEquals("NOT", predicate.getOperator());
            assertEquals(children, predicate.getChildren());
        }

        @Test
        @DisplayName("should normalize operator to uppercase")
        void shouldNormalizeOperatorToUppercase() {
            Predicate pred1 = new SimplePredicate("name", "=", "John", "p1");
            Predicate pred2 = new SimplePredicate("age", ">", 18, "p2");
            List<Predicate> children = Arrays.asList(pred1, pred2);
            
            LogicalPredicate andPredicate = new LogicalPredicate("and", children);
            LogicalPredicate orPredicate = new LogicalPredicate("or", children);
            LogicalPredicate notPredicate = new LogicalPredicate("not", Arrays.asList(pred1));
            
            assertEquals("AND", andPredicate.getOperator());
            assertEquals("OR", orPredicate.getOperator());
            assertEquals("NOT", notPredicate.getOperator());
        }

        @Test
        @DisplayName("should throw NullPointerException for null operator")
        void shouldThrowForNullOperator() {
            List<Predicate> children = Arrays.asList(new SimplePredicate("field", "=", "value", "p1"));
            assertThrows(QueryBuildException.class, 
                () -> new LogicalPredicate(null, children));
        }

        @Test
        @DisplayName("should throw NullPointerException for null children")
        void shouldThrowForNullChildren() {
            assertThrows(QueryBuildException.class,
                () -> new LogicalPredicate("AND", null));
        }

        @Test
        @DisplayName("should throw QueryBuildException for empty children list")
        void shouldThrowForEmptyChildrenList() {
            List<Predicate> emptyChildren = Collections.emptyList();
            assertThrows(QueryBuildException.class,
                () -> new LogicalPredicate("AND", emptyChildren));
        }

        @Test
        @DisplayName("should throw QueryBuildException for invalid operator")
        void shouldThrowForInvalidOperator() {
            List<Predicate> children = Arrays.asList(new SimplePredicate("field", "=", "value", "p1"));
            assertThrows(QueryBuildException.class,
                () -> new LogicalPredicate("INVALID", children));
        }

        @Test
        @DisplayName("should throw QueryBuildException for NOT with multiple children")
        void shouldThrowForNotWithMultipleChildren() {
            Predicate pred1 = new SimplePredicate("name", "=", "John", "p1");
            Predicate pred2 = new SimplePredicate("age", ">", 18, "p2");
            List<Predicate> children = Arrays.asList(pred1, pred2);
            
            assertThrows(QueryBuildException.class,
                () -> new LogicalPredicate("NOT", children));
        }

        @Test
        @DisplayName("should create immutable copy of children list")
        void shouldCreateImmutableCopyOfChildrenList() {
            Predicate pred1 = new SimplePredicate("name", "=", "John", "p1");
            Predicate pred2 = new SimplePredicate("age", ">", 18, "p2");
            List<Predicate> originalChildren = new ArrayList<>(Arrays.asList(pred1, pred2));
            
            LogicalPredicate predicate = new LogicalPredicate("AND", originalChildren);
            
            // Modify original list
            originalChildren.add(new SimplePredicate("status", "=", "ACTIVE", "p3"));
            
            // Predicate should not be affected
            assertEquals(2, predicate.getChildren().size());
        }
    }

    @Nested
    @DisplayName("SQL Generation")
    class SqlGenerationTests {

        @Test
        @DisplayName("should generate correct SQL for AND with two children")
        void shouldGenerateAndSQLWithTwoChildren() {
            Predicate pred1 = new SimplePredicate("name", "=", "John", "p1");
            Predicate pred2 = new SimplePredicate("age", ">", 18, "p2");
            List<Predicate> children = Arrays.asList(pred1, pred2);
            
            LogicalPredicate predicate = new LogicalPredicate("AND", children);
            assertEquals("(name = :p1 AND age > :p2)", predicate.toSQL());
        }

        @Test
        @DisplayName("should generate correct SQL for OR with two children")
        void shouldGenerateOrSQLWithTwoChildren() {
            Predicate pred1 = new SimplePredicate("status", "=", "ACTIVE", "p1");
            Predicate pred2 = new SimplePredicate("status", "=", "PENDING", "p2");
            List<Predicate> children = Arrays.asList(pred1, pred2);
            
            LogicalPredicate predicate = new LogicalPredicate("OR", children);
            assertEquals("(status = :p1 OR status = :p2)", predicate.toSQL());
        }

        @Test
        @DisplayName("should generate correct SQL for NOT with single child")
        void shouldGenerateNotSQLWithSingleChild() {
            Predicate child = new SimplePredicate("active", "=", true, "p1");
            List<Predicate> children = Arrays.asList(child);
            
            LogicalPredicate predicate = new LogicalPredicate("NOT", children);
            assertEquals("NOT (active = :p1)", predicate.toSQL());
        }

        @Test
        @DisplayName("should generate correct SQL for AND with multiple children")
        void shouldGenerateAndSQLWithMultipleChildren() {
            Predicate pred1 = new SimplePredicate("name", "=", "John", "p1");
            Predicate pred2 = new SimplePredicate("age", ">", 18, "p2");
            Predicate pred3 = new SimplePredicate("status", "=", "ACTIVE", "p3");
            List<Predicate> children = Arrays.asList(pred1, pred2, pred3);
            
            LogicalPredicate predicate = new LogicalPredicate("AND", children);
            assertEquals("(name = :p1 AND age > :p2 AND status = :p3)", predicate.toSQL());
        }

        @Test
        @DisplayName("should generate correct SQL for OR with multiple children")
        void shouldGenerateOrSQLWithMultipleChildren() {
            Predicate pred1 = new SimplePredicate("type", "=", "A", "p1");
            Predicate pred2 = new SimplePredicate("type", "=", "B", "p2");
            Predicate pred3 = new SimplePredicate("type", "=", "C", "p3");
            List<Predicate> children = Arrays.asList(pred1, pred2, pred3);
            
            LogicalPredicate predicate = new LogicalPredicate("OR", children);
            assertEquals("(type = :p1 OR type = :p2 OR type = :p3)", predicate.toSQL());
        }

        @Test
        @DisplayName("should generate correct SQL for nested logical predicates")
        void shouldGenerateNestedLogicalSQL() {
            // Create inner predicates
            Predicate name1 = new SimplePredicate("name", "=", "John", "p1");
            Predicate name2 = new SimplePredicate("name", "=", "Jane", "p2");
            LogicalPredicate nameOr = new LogicalPredicate("OR", Arrays.asList(name1, name2));
            
            Predicate age = new SimplePredicate("age", ">", 18, "p3");
            
            // Create outer AND predicate
            LogicalPredicate outerAnd = new LogicalPredicate("AND", Arrays.asList(nameOr, age));
            
            assertEquals("((name = :p1 OR name = :p2) AND age > :p3)", outerAnd.toSQL());
        }

        @Test
        @DisplayName("should generate correct SQL with mixed predicate types")
        void shouldGenerateSQLWithMixedPredicateTypes() {
            Predicate simple = new SimplePredicate("name", "=", "John", "p1");
            Predicate in = new InPredicate("status", Arrays.asList("ACTIVE", "PENDING"), "p2");
            Predicate nullPred = new NullPredicate("deleted_at", true);
            
            List<Predicate> children = Arrays.asList(simple, in, nullPred);
            LogicalPredicate predicate = new LogicalPredicate("AND", children);
            
            String expected = "(name = :p1 AND status IN (:p2_0, :p2_1) AND deleted_at IS NULL)";
            assertEquals(expected, predicate.toSQL());
        }
    }

    @Nested
    @DisplayName("Parameter Mapping")
    class ParameterMappingTests {

        @Test
        @DisplayName("should return combined parameters from all children")
        void shouldReturnCombinedParametersFromChildren() {
            Predicate pred1 = new SimplePredicate("name", "=", "John", "p1");
            Predicate pred2 = new SimplePredicate("age", ">", 25, "p2");
            List<Predicate> children = Arrays.asList(pred1, pred2);
            
            LogicalPredicate predicate = new LogicalPredicate("AND", children);
            Map<String, Object> params = predicate.getParameters();
            
            assertEquals(2, params.size());
            assertEquals("John", params.get("p1"));
            assertEquals(25, params.get("p2"));
        }

        @Test
        @DisplayName("should return parameters from single child for NOT")
        void shouldReturnParametersFromSingleChildForNot() {
            Predicate child = new SimplePredicate("active", "=", true, "p1");
            List<Predicate> children = Arrays.asList(child);
            
            LogicalPredicate predicate = new LogicalPredicate("NOT", children);
            Map<String, Object> params = predicate.getParameters();
            
            assertEquals(1, params.size());
            assertEquals(true, params.get("p1"));
        }

        @Test
        @DisplayName("should return combined parameters from complex predicates")
        void shouldReturnCombinedParametersFromComplexPredicates() {
            Predicate simple = new SimplePredicate("name", "=", "John", "p1");
            Predicate in = new InPredicate("status", Arrays.asList("ACTIVE", "PENDING"), "p2");
            Predicate between = new BetweenPredicate("age", 18, 65, "p3_start", "p3_end");
            Predicate like = new LikePredicate("email", "%@company.com", "p4");
            
            List<Predicate> children = Arrays.asList(simple, in, between, like);
            LogicalPredicate predicate = new LogicalPredicate("AND", children);
            Map<String, Object> params = predicate.getParameters();
            
            assertEquals(6, params.size());
            assertEquals("John", params.get("p1"));
            assertEquals("ACTIVE", params.get("p2_0"));
            assertEquals("PENDING", params.get("p2_1"));
            assertEquals(18, params.get("p3_start"));
            assertEquals(65, params.get("p3_end"));
            assertEquals("%@company.com", params.get("p4"));
        }

        @Test
        @DisplayName("should handle empty parameters from NullPredicate")
        void shouldHandleEmptyParametersFromNullPredicate() {
            Predicate simple = new SimplePredicate("name", "=", "John", "p1");
            Predicate nullPred = new NullPredicate("deleted_at", true);
            
            List<Predicate> children = Arrays.asList(simple, nullPred);
            LogicalPredicate predicate = new LogicalPredicate("AND", children);
            Map<String, Object> params = predicate.getParameters();
            
            assertEquals(1, params.size());
            assertEquals("John", params.get("p1"));
        }

        @Test
        @DisplayName("should return immutable parameter map")
        void shouldReturnImmutableParameterMap() {
            Predicate pred1 = new SimplePredicate("name", "=", "John", "p1");
            Predicate pred2 = new SimplePredicate("age", ">", 25, "p2");
            List<Predicate> children = Arrays.asList(pred1, pred2);
            
            LogicalPredicate predicate = new LogicalPredicate("AND", children);
            Map<String, Object> params = predicate.getParameters();
            
            assertThrows(UnsupportedOperationException.class, 
                () -> params.put("p3", "value"));
            assertThrows(UnsupportedOperationException.class,
                () -> params.remove("p1"));
        }

        @Test
        @DisplayName("should return consistent parameter mapping")
        void shouldReturnConsistentParameterMapping() {
            Predicate pred1 = new SimplePredicate("name", "=", "John", "p1");
            Predicate pred2 = new SimplePredicate("age", ">", 25, "p2");
            List<Predicate> children = Arrays.asList(pred1, pred2);
            
            LogicalPredicate predicate = new LogicalPredicate("AND", children);
            
            Map<String, Object> params1 = predicate.getParameters();
            Map<String, Object> params2 = predicate.getParameters();
            
            assertEquals(params1, params2);
        }

        @Test
        @DisplayName("should handle nested logical predicates parameters")
        void shouldHandleNestedLogicalPredicatesParameters() {
            // Inner predicates
            Predicate name1 = new SimplePredicate("name", "=", "John", "p1");
            Predicate name2 = new SimplePredicate("name", "=", "Jane", "p2");
            LogicalPredicate nameOr = new LogicalPredicate("OR", Arrays.asList(name1, name2));
            
            Predicate age = new SimplePredicate("age", ">", 18, "p3");
            
            // Outer predicate
            LogicalPredicate outerAnd = new LogicalPredicate("AND", Arrays.asList(nameOr, age));
            Map<String, Object> params = outerAnd.getParameters();
            
            assertEquals(3, params.size());
            assertEquals("John", params.get("p1"));
            assertEquals("Jane", params.get("p2"));
            assertEquals(18, params.get("p3"));
        }
    }

    @Nested
    @DisplayName("Immutability")
    class ImmutabilityTests {

        @Test
        @DisplayName("should be immutable - children list cannot be modified")
        void shouldBeImmutable() {
            Predicate pred1 = new SimplePredicate("name", "=", "John", "p1");
            Predicate pred2 = new SimplePredicate("age", ">", 18, "p2");
            List<Predicate> children = Arrays.asList(pred1, pred2);
            
            LogicalPredicate predicate = new LogicalPredicate("AND", children);
            
            List<Predicate> retrievedChildren = predicate.getChildren();
            assertThrows(UnsupportedOperationException.class,
                () -> retrievedChildren.add(new SimplePredicate("status", "=", "ACTIVE", "p3")));
            assertThrows(UnsupportedOperationException.class,
                () -> retrievedChildren.remove(0));
        }

        @Test
        @DisplayName("should maintain consistent SQL and parameters")
        void shouldMaintainConsistentOutput() {
            Predicate pred1 = new SimplePredicate("name", "=", "John", "p1");
            Predicate pred2 = new SimplePredicate("age", ">", 18, "p2");
            List<Predicate> children = Arrays.asList(pred1, pred2);
            
            LogicalPredicate predicate = new LogicalPredicate("AND", children);
            
            String sql1 = predicate.toSQL();
            String sql2 = predicate.toSQL();
            Map<String, Object> params1 = predicate.getParameters();
            Map<String, Object> params2 = predicate.getParameters();
            
            assertEquals(sql1, sql2);
            assertEquals(params1, params2);
        }

        @Test
        @DisplayName("should create independent instances")
        void shouldCreateIndependentInstances() {
            Predicate pred1 = new SimplePredicate("name", "=", "John", "p1");
            Predicate pred2 = new SimplePredicate("age", ">", 18, "p2");
            List<Predicate> children = Arrays.asList(pred1, pred2);
            
            LogicalPredicate predicate1 = new LogicalPredicate("AND", children);
            LogicalPredicate predicate2 = new LogicalPredicate("AND", children);
            
            assertEquals(predicate1, predicate2);
            assertNotSame(predicate1, predicate2);
            assertNotSame(predicate1.getChildren(), predicate2.getChildren());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("should be equal when all fields are equal")
        void shouldBeEqualWhenFieldsEqual() {
            Predicate pred1 = new SimplePredicate("name", "=", "John", "p1");
            Predicate pred2 = new SimplePredicate("age", ">", 18, "p2");
            List<Predicate> children = Arrays.asList(pred1, pred2);
            
            LogicalPredicate predicate1 = new LogicalPredicate("AND", children);
            LogicalPredicate predicate2 = new LogicalPredicate("AND", children);
            
            assertEquals(predicate1, predicate2);
            assertEquals(predicate1.hashCode(), predicate2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when operator differs")
        void shouldNotBeEqualWhenOperatorDiffers() {
            Predicate pred1 = new SimplePredicate("name", "=", "John", "p1");
            Predicate pred2 = new SimplePredicate("age", ">", 18, "p2");
            List<Predicate> children = Arrays.asList(pred1, pred2);
            
            LogicalPredicate andPredicate = new LogicalPredicate("AND", children);
            LogicalPredicate orPredicate = new LogicalPredicate("OR", children);
            
            assertNotEquals(andPredicate, orPredicate);
        }

        @Test
        @DisplayName("should not be equal when children differ")
        void shouldNotBeEqualWhenChildrenDiffer() {
            Predicate pred1 = new SimplePredicate("name", "=", "John", "p1");
            Predicate pred2 = new SimplePredicate("age", ">", 18, "p2");
            Predicate pred3 = new SimplePredicate("status", "=", "ACTIVE", "p3");
            
            LogicalPredicate predicate1 = new LogicalPredicate("AND", Arrays.asList(pred1, pred2));
            LogicalPredicate predicate2 = new LogicalPredicate("AND", Arrays.asList(pred1, pred3));
            
            assertNotEquals(predicate1, predicate2);
        }

        @Test
        @DisplayName("should not be equal when children order differs")
        void shouldNotBeEqualWhenChildrenOrderDiffers() {
            Predicate pred1 = new SimplePredicate("name", "=", "John", "p1");
            Predicate pred2 = new SimplePredicate("age", ">", 18, "p2");
            
            LogicalPredicate predicate1 = new LogicalPredicate("AND", Arrays.asList(pred1, pred2));
            LogicalPredicate predicate2 = new LogicalPredicate("AND", Arrays.asList(pred2, pred1));
            
            assertNotEquals(predicate1, predicate2);
        }

        @Test
        @DisplayName("should not be equal to null")
        void shouldNotBeEqualToNull() {
            Predicate pred = new SimplePredicate("name", "=", "John", "p1");
            LogicalPredicate predicate = new LogicalPredicate("AND", Arrays.asList(pred));
            assertNotEquals(predicate, null);
        }

        @Test
        @DisplayName("should not be equal to different class")
        void shouldNotBeEqualToDifferentClass() {
            Predicate pred = new SimplePredicate("name", "=", "John", "p1");
            LogicalPredicate logicalPredicate = new LogicalPredicate("AND", Arrays.asList(pred));
            SimplePredicate simplePredicate = new SimplePredicate("name", "=", "John", "p1");
            assertNotEquals(logicalPredicate, simplePredicate);
        }

        @Test
        @DisplayName("should be equal to itself")
        void shouldBeEqualToItself() {
            Predicate pred = new SimplePredicate("name", "=", "John", "p1");
            LogicalPredicate predicate = new LogicalPredicate("AND", Arrays.asList(pred));
            assertEquals(predicate, predicate);
            assertEquals(predicate.hashCode(), predicate.hashCode());
        }

        @Test
        @DisplayName("should provide meaningful toString representation")
        void shouldProvideMeaningfulToString() {
            Predicate pred1 = new SimplePredicate("name", "=", "John", "p1");
            Predicate pred2 = new SimplePredicate("age", ">", 18, "p2");
            LogicalPredicate predicate = new LogicalPredicate("AND", Arrays.asList(pred1, pred2));
            String toString = predicate.toString();
            
            assertNotNull(toString);
            assertTrue(toString.contains("LogicalPredicate"));
            assertTrue(toString.contains("AND"));
            assertTrue(toString.contains("name"));
            assertTrue(toString.contains("age"));
        }

        @Test
        @DisplayName("should handle nested predicates in toString")
        void shouldHandleNestedPredicatesInToString() {
            Predicate pred1 = new SimplePredicate("name", "=", "John", "p1");
            Predicate pred2 = new SimplePredicate("age", ">", 18, "p2");
            LogicalPredicate inner = new LogicalPredicate("OR", Arrays.asList(pred1, pred2));
            
            Predicate pred3 = new SimplePredicate("status", "=", "ACTIVE", "p3");
            LogicalPredicate outer = new LogicalPredicate("AND", Arrays.asList(inner, pred3));
            
            String toString = outer.toString();
            
            assertNotNull(toString);
            assertTrue(toString.contains("LogicalPredicate"));
            assertTrue(toString.contains("AND"));
            assertTrue(toString.contains("OR"));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle deeply nested logical predicates")
        void shouldHandleDeeplyNestedLogicalPredicates() {
            // Level 1
            Predicate name = new SimplePredicate("name", "=", "John", "p1");
            Predicate age = new SimplePredicate("age", ">", 18, "p2");
            LogicalPredicate level1 = new LogicalPredicate("AND", Arrays.asList(name, age));
            
            // Level 2
            Predicate status = new SimplePredicate("status", "=", "ACTIVE", "p3");
            LogicalPredicate level2 = new LogicalPredicate("OR", Arrays.asList(level1, status));
            
            // Level 3
            Predicate deleted = new NullPredicate("deleted_at", true);
            LogicalPredicate level3 = new LogicalPredicate("AND", Arrays.asList(level2, deleted));
            
            String expectedSQL = "(((name = :p1 AND age > :p2) OR status = :p3) AND deleted_at IS NULL)";
            assertEquals(expectedSQL, level3.toSQL());
            
            Map<String, Object> params = level3.getParameters();
            assertEquals(3, params.size());
            assertEquals("John", params.get("p1"));
            assertEquals(18, params.get("p2"));
            assertEquals("ACTIVE", params.get("p3"));
        }

        @Test
        @DisplayName("should handle large number of children")
        void shouldHandleLargeNumberOfChildren() {
            List<Predicate> children = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                children.add(new SimplePredicate("field" + i, "=", "value" + i, "p" + i));
            }
            
            LogicalPredicate predicate = new LogicalPredicate("OR", children);
            
            String sql = predicate.toSQL();
            assertTrue(sql.startsWith("(field0 = :p0 OR"));
            assertTrue(sql.endsWith("field99 = :p99)"));
            
            Map<String, Object> params = predicate.getParameters();
            assertEquals(100, params.size());
            assertEquals("value0", params.get("p0"));
            assertEquals("value99", params.get("p99"));
        }

        @Test
        @DisplayName("should handle operator case variations")
        void shouldHandleOperatorCaseVariations() {
            Predicate pred1 = new SimplePredicate("name", "=", "John", "p1");
            Predicate pred2 = new SimplePredicate("age", ">", 18, "p2");
            List<Predicate> children = Arrays.asList(pred1, pred2);
            
            LogicalPredicate andUpper = new LogicalPredicate("AND", children);
            LogicalPredicate andLower = new LogicalPredicate("and", children);
            LogicalPredicate andMixed = new LogicalPredicate("And", children);
            
            assertEquals("AND", andUpper.getOperator());
            assertEquals("AND", andLower.getOperator());
            assertEquals("AND", andMixed.getOperator());
            
            String expectedSQL = "(name = :p1 AND age > :p2)";
            assertEquals(expectedSQL, andUpper.toSQL());
            assertEquals(expectedSQL, andLower.toSQL());
            assertEquals(expectedSQL, andMixed.toSQL());
        }

        @Test
        @DisplayName("should maintain consistency across repeated calls")
        void shouldMaintainConsistencyAcrossRepeatedCalls() {
            Predicate pred1 = new SimplePredicate("name", "=", "John", "p1");
            Predicate pred2 = new SimplePredicate("age", ">", 18, "p2");
            List<Predicate> children = Arrays.asList(pred1, pred2);
            
            LogicalPredicate predicate = new LogicalPredicate("AND", children);
            
            for (int i = 0; i < 100; i++) {
                assertEquals("(name = :p1 AND age > :p2)", predicate.toSQL());
                Map<String, Object> params = predicate.getParameters();
                assertEquals(2, params.size());
                assertEquals("John", params.get("p1"));
                assertEquals(18, params.get("p2"));
                assertEquals("AND", predicate.getOperator());
                assertEquals(2, predicate.getChildren().size());
            }
        }
    }
}