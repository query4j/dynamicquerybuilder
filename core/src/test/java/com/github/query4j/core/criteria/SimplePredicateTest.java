package com.github.query4j.core.criteria;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import com.github.query4j.core.QueryBuildException;

/**
 * Comprehensive unit tests for SimplePredicate class.
 * Tests SQL generation, parameter mapping, immutability, and validation.
 */
@DisplayName("SimplePredicate")
class SimplePredicateTest {

    @Nested
    @DisplayName("Constructor and Basic Properties")
    class ConstructorTests {

        @Test
        @DisplayName("should create valid predicate with required parameters")
        void shouldCreateValidPredicate() {
            SimplePredicate predicate = new SimplePredicate("name", "=", "John", "p1");
            
            assertEquals("name", predicate.getField());
            assertEquals("=", predicate.getOperator());
            assertEquals("John", predicate.getValue());
            assertEquals("p1", predicate.getParamName());
        }

        @Test
        @DisplayName("should throw NullPointerException for null field")
        void shouldThrowForNullField() {
            assertThrows(QueryBuildException.class, 
                () -> new SimplePredicate(null, "=", "value", "p1"));
        }

        @Test
        @DisplayName("should throw NullPointerException for null operator")
        void shouldThrowForNullOperator() {
            assertThrows(QueryBuildException.class,
                () -> new SimplePredicate("field", null, "value", "p1"));
        }

        @Test
        @DisplayName("should throw NullPointerException for null paramName")
        void shouldThrowForNullParamName() {
            assertThrows(QueryBuildException.class,
                () -> new SimplePredicate("field", "=", "value", null));
        }

        @Test
        @DisplayName("should accept null value")
        void shouldAcceptNullValue() {
            SimplePredicate predicate = new SimplePredicate("field", "IS", null, "p1");
            assertNull(predicate.getValue());
        }
    }

    @Nested
    @DisplayName("SQL Generation")
    class SqlGenerationTests {

        @Test
        @DisplayName("should generate correct SQL for equality comparison")
        void shouldGenerateEqualitySQL() {
            SimplePredicate predicate = new SimplePredicate("name", "=", "John", "p1");
            assertEquals("name = :p1", predicate.toSQL());
        }

        @Test
        @DisplayName("should generate correct SQL for comparison operators")
        void shouldGenerateComparisonSQL() {
            String[] operators = {">", ">=", "<", "<=", "!=", "<>"};
            
            for (String op : operators) {
                SimplePredicate predicate = new SimplePredicate("age", op, 25, "p1");
                String expectedSQL = "age " + op + " :p1";
                assertEquals(expectedSQL, predicate.toSQL());
            }
        }

        @Test
        @DisplayName("should generate correct SQL for LIKE operator")
        void shouldGenerateLikeSQL() {
            SimplePredicate predicate = new SimplePredicate("name", "LIKE", "%John%", "p1");
            assertEquals("name LIKE :p1", predicate.toSQL());
        }

        @Test
        @DisplayName("should generate SQL with field containing dots")
        void shouldGenerateSQLWithDottedField() {
            SimplePredicate predicate = new SimplePredicate("user.name", "=", "John", "p1");
            assertEquals("user.name = :p1", predicate.toSQL());
        }

        @Test
        @DisplayName("should generate SQL with field containing underscores")
        void shouldGenerateSQLWithUnderscoreField() {
            SimplePredicate predicate = new SimplePredicate("first_name", "=", "John", "p1");
            assertEquals("first_name = :p1", predicate.toSQL());
        }

        @Test
        @DisplayName("should handle complex parameter names")
        void shouldHandleComplexParamNames() {
            SimplePredicate predicate = new SimplePredicate("field", "=", "value", "param_name_123");
            assertEquals("field = :param_name_123", predicate.toSQL());
        }
    }

    @Nested
    @DisplayName("Parameter Mapping")
    class ParameterMappingTests {

        @Test
        @DisplayName("should return correct single parameter mapping")
        void shouldReturnCorrectParameterMapping() {
            SimplePredicate predicate = new SimplePredicate("name", "=", "John", "p1");
            Map<String, Object> params = predicate.getParameters();
            
            assertEquals(1, params.size());
            assertEquals("John", params.get("p1"));
        }

        @Test
        @DisplayName("should return parameter mapping with null value")
        void shouldReturnParameterMappingWithNull() {
            SimplePredicate predicate = new SimplePredicate("field", "IS", null, "p1");
            Map<String, Object> params = predicate.getParameters();
            
            assertEquals(1, params.size());
            assertNull(params.get("p1"));
            assertTrue(params.containsKey("p1"));
        }

        @Test
        @DisplayName("should return parameter mapping with different value types")
        void shouldReturnParameterMappingWithDifferentTypes() {
            // String value
            SimplePredicate stringPredicate = new SimplePredicate("name", "=", "John", "p1");
            assertEquals("John", stringPredicate.getParameters().get("p1"));
            
            // Integer value
            SimplePredicate intPredicate = new SimplePredicate("age", "=", 25, "p2");
            assertEquals(25, intPredicate.getParameters().get("p2"));
            
            // Boolean value
            SimplePredicate boolPredicate = new SimplePredicate("active", "=", true, "p3");
            assertEquals(true, boolPredicate.getParameters().get("p3"));
            
            // Double value
            SimplePredicate doublePredicate = new SimplePredicate("salary", ">", 50000.50, "p4");
            assertEquals(50000.50, doublePredicate.getParameters().get("p4"));
        }

        @Test
        @DisplayName("should return immutable parameter map")
        void shouldReturnImmutableParameterMap() {
            SimplePredicate predicate = new SimplePredicate("name", "=", "John", "p1");
            Map<String, Object> params = predicate.getParameters();
            
            assertThrows(UnsupportedOperationException.class, 
                () -> params.put("p2", "value"));
            assertThrows(UnsupportedOperationException.class,
                () -> params.remove("p1"));
        }
    }

    @Nested
    @DisplayName("Immutability")
    class ImmutabilityTests {

        @Test
        @DisplayName("should be immutable - fields cannot be modified")
        void shouldBeImmutable() {
            SimplePredicate predicate = new SimplePredicate("name", "=", "John", "p1");
            
            // Verify all getters return the same values
            assertEquals("name", predicate.getField());
            assertEquals("=", predicate.getOperator());
            assertEquals("John", predicate.getValue());
            assertEquals("p1", predicate.getParamName());
            
            // Create another predicate with same values
            SimplePredicate predicate2 = new SimplePredicate("name", "=", "John", "p1");
            
            // Should be equal but not same instance
            assertEquals(predicate, predicate2);
            assertNotSame(predicate, predicate2);
        }

        @Test
        @DisplayName("should maintain consistent SQL and parameters")
        void shouldMaintainConsistentOutput() {
            SimplePredicate predicate = new SimplePredicate("name", "=", "John", "p1");
            
            String sql1 = predicate.toSQL();
            String sql2 = predicate.toSQL();
            Map<String, Object> params1 = predicate.getParameters();
            Map<String, Object> params2 = predicate.getParameters();
            
            assertEquals(sql1, sql2);
            assertEquals(params1, params2);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("should be equal when all fields are equal")
        void shouldBeEqualWhenFieldsEqual() {
            SimplePredicate predicate1 = new SimplePredicate("name", "=", "John", "p1");
            SimplePredicate predicate2 = new SimplePredicate("name", "=", "John", "p1");
            
            assertEquals(predicate1, predicate2);
            assertEquals(predicate1.hashCode(), predicate2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when field differs")
        void shouldNotBeEqualWhenFieldDiffers() {
            SimplePredicate predicate1 = new SimplePredicate("name", "=", "John", "p1");
            SimplePredicate predicate2 = new SimplePredicate("age", "=", "John", "p1");
            
            assertNotEquals(predicate1, predicate2);
        }

        @Test
        @DisplayName("should not be equal when operator differs")
        void shouldNotBeEqualWhenOperatorDiffers() {
            SimplePredicate predicate1 = new SimplePredicate("name", "=", "John", "p1");
            SimplePredicate predicate2 = new SimplePredicate("name", "!=", "John", "p1");
            
            assertNotEquals(predicate1, predicate2);
        }

        @Test
        @DisplayName("should not be equal when value differs")
        void shouldNotBeEqualWhenValueDiffers() {
            SimplePredicate predicate1 = new SimplePredicate("name", "=", "John", "p1");
            SimplePredicate predicate2 = new SimplePredicate("name", "=", "Jane", "p1");
            
            assertNotEquals(predicate1, predicate2);
        }

        @Test
        @DisplayName("should not be equal when paramName differs")
        void shouldNotBeEqualWhenParamNameDiffers() {
            SimplePredicate predicate1 = new SimplePredicate("name", "=", "John", "p1");
            SimplePredicate predicate2 = new SimplePredicate("name", "=", "John", "p2");
            
            assertNotEquals(predicate1, predicate2);
        }

        @Test
        @DisplayName("should handle null values in equals comparison")
        void shouldHandleNullValuesInEquals() {
            SimplePredicate predicate1 = new SimplePredicate("field", "IS", null, "p1");
            SimplePredicate predicate2 = new SimplePredicate("field", "IS", null, "p1");
            
            assertEquals(predicate1, predicate2);
            assertEquals(predicate1.hashCode(), predicate2.hashCode());
        }

        @Test
        @DisplayName("should not be equal to null object")
        void shouldNotBeEqualToNullObject() {
            SimplePredicate predicate = new SimplePredicate("field", "=", "value", "p1");
            
            assertNotEquals(predicate, null);
        }

        @Test
        @DisplayName("should not be equal to different object type")
        void shouldNotBeEqualToDifferentObjectType() {
            SimplePredicate predicate = new SimplePredicate("field", "=", "value", "p1");
            String different = "different type";
            
            assertNotEquals(predicate, different);
        }

        @Test
        @DisplayName("should be equal to itself always")
        void shouldBeEqualToItselfAlways() {
            SimplePredicate predicate = new SimplePredicate("field", "=", "value", "p1");
            
            assertEquals(predicate, predicate);
        }

        @Test
        @DisplayName("should have consistent hashCode for equal objects")
        void shouldHaveConsistentHashCodeForEqualObjects() {
            SimplePredicate predicate1 = new SimplePredicate("field", "=", "value", "p1");
            SimplePredicate predicate2 = new SimplePredicate("field", "=", "value", "p1");
            
            assertEquals(predicate1.hashCode(), predicate2.hashCode());
            
            // Multiple calls should return same hash code
            int hash1 = predicate1.hashCode();
            int hash2 = predicate1.hashCode();
            assertEquals(hash1, hash2);
        }

        @Test
        @DisplayName("should have different hashCode for different objects")
        void shouldHaveDifferentHashCodeForDifferentObjects() {
            SimplePredicate predicate1 = new SimplePredicate("field1", "=", "value", "p1");
            SimplePredicate predicate2 = new SimplePredicate("field2", "=", "value", "p1");
            
            assertNotEquals(predicate1.hashCode(), predicate2.hashCode());
        }

        @Test
        @DisplayName("should provide meaningful toString representation")
        void shouldProvideMeaningfulToString() {
            SimplePredicate predicate = new SimplePredicate("name", "=", "John", "p1");
            String toString = predicate.toString();
            
            assertNotNull(toString);
            assertTrue(toString.contains("SimplePredicate"));
            assertTrue(toString.contains("name"));
            assertTrue(toString.contains("="));
            assertTrue(toString.contains("John"));
            assertTrue(toString.contains("p1"));
        }

        @Test
        @DisplayName("should handle null values in toString")
        void shouldHandleNullValuesInToString() {
            SimplePredicate predicate = new SimplePredicate("field", "IS", null, "p1");
            String toString = predicate.toString();
            
            assertNotNull(toString);
            assertTrue(toString.contains("SimplePredicate"));
            assertTrue(toString.contains("field"));
            assertTrue(toString.contains("IS"));
            assertTrue(toString.contains("null"));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle empty string values")
        void shouldHandleEmptyStringValues() {
            SimplePredicate predicate = new SimplePredicate("field", "=", "", "p1");
            
            assertEquals("field = :p1", predicate.toSQL());
            assertEquals("", predicate.getParameters().get("p1"));
        }

        @Test
        @DisplayName("should handle special characters in values")
        void shouldHandleSpecialCharactersInValues() {
            String specialValue = "O'Malley & Co. (\"Special\" chars) - 100%";
            SimplePredicate predicate = new SimplePredicate("company", "=", specialValue, "p1");
            
            assertEquals("company = :p1", predicate.toSQL());
            assertEquals(specialValue, predicate.getParameters().get("p1"));
        }

        @Test
        @DisplayName("should handle very long values")
        void shouldHandleVeryLongValues() {
            // Java 8 compatible alternative to String.repeat()
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 10000; i++) {
                sb.append("A");
            }
            String longValue = sb.toString(); // Very long string
            SimplePredicate predicate = new SimplePredicate("description", "=", longValue, "p1");
            
            assertEquals("description = :p1", predicate.toSQL());
            assertEquals(longValue, predicate.getParameters().get("p1"));
        }

        @Test
        @DisplayName("should handle numeric field names")
        void shouldHandleNumericFieldNames() {
            SimplePredicate predicate = new SimplePredicate("field123", "=", "value", "p1");
            assertEquals("field123 = :p1", predicate.toSQL());
        }
        
        @Test
        @DisplayName("should handle multiple predicates with same field names")
        void shouldHandleMultiplePredicatesWithSameFieldNames() {
            // Multiple predicates with same field should work independently
            SimplePredicate predicate1 = new SimplePredicate("name", "=", "John", "p1");
            SimplePredicate predicate2 = new SimplePredicate("name", "!=", "Jane", "p2");
            SimplePredicate predicate3 = new SimplePredicate("name", ">", "A", "p3");
            
            // All should generate correct SQL independently
            assertEquals("name = :p1", predicate1.toSQL());
            assertEquals("name != :p2", predicate2.toSQL());
            assertEquals("name > :p3", predicate3.toSQL());
            
            // Parameters should be distinct
            Map<String, Object> params1 = predicate1.getParameters();
            Map<String, Object> params2 = predicate2.getParameters();
            Map<String, Object> params3 = predicate3.getParameters();
            
            assertEquals("John", params1.get("p1"));
            assertEquals("Jane", params2.get("p2"));
            assertEquals("A", params3.get("p3"));
            
            // Parameter names should not conflict
            assertFalse(params1.containsKey("p2"));
            assertFalse(params1.containsKey("p3"));
            assertFalse(params2.containsKey("p1"));
            assertFalse(params2.containsKey("p3"));
            assertFalse(params3.containsKey("p1"));
            assertFalse(params3.containsKey("p2"));
        }
    }
}