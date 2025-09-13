package com.github.query4j.core.criteria;

import com.github.query4j.core.QueryBuildException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import com.github.query4j.core.QueryBuildException;

/**
 * Comprehensive unit tests for InPredicate class.
 * Tests SQL generation, parameter mapping, immutability, and validation.
 */
@DisplayName("InPredicate")
class InPredicateTest {

    @Nested
    @DisplayName("Constructor and Basic Properties")
    class ConstructorTests {

        @Test
        @DisplayName("should create valid predicate with valid parameters")
        void shouldCreateValidPredicate() {
            List<Object> values = Arrays.asList("A", "B", "C");
            InPredicate predicate = new InPredicate("status", values, "p1");
            
            assertEquals("status", predicate.getField());
            assertEquals(values, predicate.getValues());
            assertEquals("p1", predicate.getBaseParamName());
        }

        @Test
        @DisplayName("should throw NullPointerException for null field")
        void shouldThrowForNullField() {
            List<Object> values = Arrays.asList("A", "B");
            assertThrows(QueryBuildException.class, 
                () -> new InPredicate(null, values, "p1"));
        }

        @Test
        @DisplayName("should throw NullPointerException for null values")
        void shouldThrowForNullValues() {
            assertThrows(QueryBuildException.class,
                () -> new InPredicate("field", null, "p1"));
        }

        @Test
        @DisplayName("should throw NullPointerException for null baseParamName")
        void shouldThrowForNullBaseParamName() {
            List<Object> values = Arrays.asList("A", "B");
            assertThrows(QueryBuildException.class,
                () -> new InPredicate("field", values, null));
        }

        @Test
        @DisplayName("should throw QueryBuildException for empty values list")
        void shouldThrowForEmptyValuesList() {
            List<Object> emptyValues = Collections.emptyList();
            assertThrows(QueryBuildException.class,
                () -> new InPredicate("field", emptyValues, "p1"));
        }

        @Test
        @DisplayName("should create immutable copy of values list")
        void shouldCreateImmutableCopyOfValuesList() {
            List<Object> originalValues = new ArrayList<>(Arrays.asList("A", "B", "C"));
            InPredicate predicate = new InPredicate("field", originalValues, "p1");
            
            // Modify original list
            originalValues.add("D");
            
            // Predicate should not be affected
            assertEquals(3, predicate.getValues().size());
            assertEquals(Arrays.asList("A", "B", "C"), predicate.getValues());
        }
    }

    @Nested
    @DisplayName("SQL Generation")
    class SqlGenerationTests {

        @Test
        @DisplayName("should generate correct SQL for single value")
        void shouldGenerateSQLForSingleValue() {
            List<Object> values = Arrays.asList("ACTIVE");
            InPredicate predicate = new InPredicate("status", values, "p1");
            assertEquals("status IN (:p1_0)", predicate.toSQL());
        }

        @Test
        @DisplayName("should generate correct SQL for multiple values")
        void shouldGenerateSQLForMultipleValues() {
            List<Object> values = Arrays.asList("ACTIVE", "PENDING", "COMPLETED");
            InPredicate predicate = new InPredicate("status", values, "p1");
            assertEquals("status IN (:p1_0, :p1_1, :p1_2)", predicate.toSQL());
        }

        @Test
        @DisplayName("should generate SQL with field containing dots")
        void shouldGenerateSQLWithDottedField() {
            List<Object> values = Arrays.asList(1, 2, 3);
            InPredicate predicate = new InPredicate("user.id", values, "p1");
            assertEquals("user.id IN (:p1_0, :p1_1, :p1_2)", predicate.toSQL());
        }

        @Test
        @DisplayName("should generate SQL with field containing underscores")
        void shouldGenerateSQLWithUnderscoreField() {
            List<Object> values = Arrays.asList("John", "Jane");
            InPredicate predicate = new InPredicate("first_name", values, "p1");
            assertEquals("first_name IN (:p1_0, :p1_1)", predicate.toSQL());
        }

        @Test
        @DisplayName("should generate SQL with complex base parameter name")
        void shouldGenerateSQLWithComplexBaseParamName() {
            List<Object> values = Arrays.asList("A", "B");
            InPredicate predicate = new InPredicate("field", values, "complex_param_name_123");
            assertEquals("field IN (:complex_param_name_123_0, :complex_param_name_123_1)", 
                predicate.toSQL());
        }

        @Test
        @DisplayName("should generate SQL for many values")
        void shouldGenerateSQLForManyValues() {
            List<Object> values = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                values.add(i);
            }
            InPredicate predicate = new InPredicate("id", values, "p1");
            
            String expected = "id IN (:p1_0, :p1_1, :p1_2, :p1_3, :p1_4, :p1_5, :p1_6, :p1_7, :p1_8, :p1_9)";
            assertEquals(expected, predicate.toSQL());
        }
    }

    @Nested
    @DisplayName("Parameter Mapping")
    class ParameterMappingTests {

        @Test
        @DisplayName("should return correct parameter mapping for single value")
        void shouldReturnCorrectParameterMappingForSingleValue() {
            List<Object> values = Arrays.asList("ACTIVE");
            InPredicate predicate = new InPredicate("status", values, "p1");
            Map<String, Object> params = predicate.getParameters();
            
            assertEquals(1, params.size());
            assertEquals("ACTIVE", params.get("p1_0"));
        }

        @Test
        @DisplayName("should return correct parameter mapping for multiple values")
        void shouldReturnCorrectParameterMappingForMultipleValues() {
            List<Object> values = Arrays.asList("ACTIVE", "PENDING", "COMPLETED");
            InPredicate predicate = new InPredicate("status", values, "p1");
            Map<String, Object> params = predicate.getParameters();
            
            assertEquals(3, params.size());
            assertEquals("ACTIVE", params.get("p1_0"));
            assertEquals("PENDING", params.get("p1_1"));
            assertEquals("COMPLETED", params.get("p1_2"));
        }

        @Test
        @DisplayName("should return parameter mapping with different value types")
        void shouldReturnParameterMappingWithDifferentTypes() {
            List<Object> values = Arrays.asList("String", 123, true, 45.67, null);
            InPredicate predicate = new InPredicate("field", values, "p1");
            Map<String, Object> params = predicate.getParameters();
            
            assertEquals(5, params.size());
            assertEquals("String", params.get("p1_0"));
            assertEquals(123, params.get("p1_1"));
            assertEquals(true, params.get("p1_2"));
            assertEquals(45.67, params.get("p1_3"));
            assertNull(params.get("p1_4"));
            assertTrue(params.containsKey("p1_4"));
        }

        @Test
        @DisplayName("should return parameter mapping with null values")
        void shouldReturnParameterMappingWithNullValues() {
            List<Object> values = Arrays.asList(null, "value", null);
            InPredicate predicate = new InPredicate("field", values, "p1");
            Map<String, Object> params = predicate.getParameters();
            
            assertEquals(3, params.size());
            assertNull(params.get("p1_0"));
            assertEquals("value", params.get("p1_1"));
            assertNull(params.get("p1_2"));
            assertTrue(params.containsKey("p1_0"));
            assertTrue(params.containsKey("p1_2"));
        }

        @Test
        @DisplayName("should return immutable parameter map")
        void shouldReturnImmutableParameterMap() {
            List<Object> values = Arrays.asList("A", "B");
            InPredicate predicate = new InPredicate("field", values, "p1");
            Map<String, Object> params = predicate.getParameters();
            
            assertThrows(UnsupportedOperationException.class, 
                () -> params.put("p1_2", "value"));
            assertThrows(UnsupportedOperationException.class,
                () -> params.remove("p1_0"));
        }

        @Test
        @DisplayName("should generate unique parameter names")
        void shouldGenerateUniqueParameterNames() {
            List<Object> values = Arrays.asList(1, 2, 3, 4, 5);
            InPredicate predicate = new InPredicate("field", values, "p1");
            Map<String, Object> params = predicate.getParameters();
            
            Set<String> paramNames = params.keySet();
            assertEquals(5, paramNames.size());
            assertTrue(paramNames.contains("p1_0"));
            assertTrue(paramNames.contains("p1_1"));
            assertTrue(paramNames.contains("p1_2"));
            assertTrue(paramNames.contains("p1_3"));
            assertTrue(paramNames.contains("p1_4"));
        }
    }

    @Nested
    @DisplayName("Immutability")
    class ImmutabilityTests {

        @Test
        @DisplayName("should be immutable - values list cannot be modified")
        void shouldBeImmutable() {
            List<Object> values = Arrays.asList("A", "B", "C");
            InPredicate predicate = new InPredicate("field", values, "p1");
            
            List<Object> retrievedValues = predicate.getValues();
            assertThrows(UnsupportedOperationException.class,
                () -> retrievedValues.add("D"));
            assertThrows(UnsupportedOperationException.class,
                () -> retrievedValues.remove(0));
        }

        @Test
        @DisplayName("should maintain consistent SQL and parameters")
        void shouldMaintainConsistentOutput() {
            List<Object> values = Arrays.asList("A", "B", "C");
            InPredicate predicate = new InPredicate("field", values, "p1");
            
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
            List<Object> values = Arrays.asList("A", "B", "C");
            InPredicate predicate1 = new InPredicate("field", values, "p1");
            InPredicate predicate2 = new InPredicate("field", values, "p1");
            
            assertEquals(predicate1, predicate2);
            assertNotSame(predicate1, predicate2);
            assertNotSame(predicate1.getValues(), predicate2.getValues());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("should be equal when all fields are equal")
        void shouldBeEqualWhenFieldsEqual() {
            List<Object> values = Arrays.asList("A", "B", "C");
            InPredicate predicate1 = new InPredicate("field", values, "p1");
            InPredicate predicate2 = new InPredicate("field", values, "p1");
            
            assertEquals(predicate1, predicate2);
            assertEquals(predicate1.hashCode(), predicate2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when field differs")
        void shouldNotBeEqualWhenFieldDiffers() {
            List<Object> values = Arrays.asList("A", "B");
            InPredicate predicate1 = new InPredicate("field1", values, "p1");
            InPredicate predicate2 = new InPredicate("field2", values, "p1");
            
            assertNotEquals(predicate1, predicate2);
        }

        @Test
        @DisplayName("should not be equal when values differ")
        void shouldNotBeEqualWhenValuesDiffer() {
            InPredicate predicate1 = new InPredicate("field", Arrays.asList("A", "B"), "p1");
            InPredicate predicate2 = new InPredicate("field", Arrays.asList("A", "C"), "p1");
            
            assertNotEquals(predicate1, predicate2);
        }

        @Test
        @DisplayName("should not be equal when baseParamName differs")
        void shouldNotBeEqualWhenBaseParamNameDiffers() {
            List<Object> values = Arrays.asList("A", "B");
            InPredicate predicate1 = new InPredicate("field", values, "p1");
            InPredicate predicate2 = new InPredicate("field", values, "p2");
            
            assertNotEquals(predicate1, predicate2);
        }

        @Test
        @DisplayName("should handle null values in equals comparison")
        void shouldHandleNullValuesInEquals() {
            List<Object> values = Arrays.asList(null, "value", null);
            InPredicate predicate1 = new InPredicate("field", values, "p1");
            InPredicate predicate2 = new InPredicate("field", values, "p1");
            
            assertEquals(predicate1, predicate2);
            assertEquals(predicate1.hashCode(), predicate2.hashCode());
        }

        @Test
        @DisplayName("should not be equal to null object")
        void shouldNotBeEqualToNullObject() {
            List<Object> values = Arrays.asList("A", "B");
            InPredicate predicate = new InPredicate("field", values, "p1");
            
            assertNotEquals(predicate, null);
        }

        @Test
        @DisplayName("should not be equal to different object type")
        void shouldNotBeEqualToDifferentObjectType() {
            List<Object> values = Arrays.asList("A", "B");
            InPredicate predicate = new InPredicate("field", values, "p1");
            String different = "different type";
            
            assertNotEquals(predicate, different);
        }

        @Test
        @DisplayName("should be equal to itself always")
        void shouldBeEqualToItselfAlways() {
            List<Object> values = Arrays.asList("A", "B");
            InPredicate predicate = new InPredicate("field", values, "p1");
            
            assertEquals(predicate, predicate);
        }

        @Test
        @DisplayName("should have consistent hashCode for equal objects")
        void shouldHaveConsistentHashCodeForEqualObjects() {
            List<Object> values = Arrays.asList("A", "B");
            InPredicate predicate1 = new InPredicate("field", values, "p1");
            InPredicate predicate2 = new InPredicate("field", values, "p1");
            
            assertEquals(predicate1.hashCode(), predicate2.hashCode());
            
            // Multiple calls should return same hash code
            int hash1 = predicate1.hashCode();
            int hash2 = predicate1.hashCode();
            assertEquals(hash1, hash2);
        }

        @Test
        @DisplayName("should have different hashCode for different objects")
        void shouldHaveDifferentHashCodeForDifferentObjects() {
            InPredicate predicate1 = new InPredicate("field1", Arrays.asList("A", "B"), "p1");
            InPredicate predicate2 = new InPredicate("field2", Arrays.asList("A", "B"), "p1");
            
            assertNotEquals(predicate1.hashCode(), predicate2.hashCode());
        }

        @Test
        @DisplayName("should provide meaningful toString representation")
        void shouldProvideMeaningfulToString() {
            List<Object> values = Arrays.asList("A", "B", "C");
            InPredicate predicate = new InPredicate("status", values, "p1");
            String toString = predicate.toString();
            
            assertNotNull(toString);
            assertTrue(toString.contains("InPredicate"));
            assertTrue(toString.contains("status"));
            assertTrue(toString.contains("A"));
            assertTrue(toString.contains("B"));
            assertTrue(toString.contains("C"));
            assertTrue(toString.contains("p1"));
        }

        @Test
        @DisplayName("should handle null values in toString")
        void shouldHandleNullValuesInToString() {
            List<Object> values = Arrays.asList(null, "value", null);
            InPredicate predicate = new InPredicate("field", values, "p1");
            String toString = predicate.toString();
            
            assertNotNull(toString);
            assertTrue(toString.contains("InPredicate"));
            assertTrue(toString.contains("field"));
            assertTrue(toString.contains("null"));
            assertTrue(toString.contains("value"));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle empty string values")
        void shouldHandleEmptyStringValues() {
            List<Object> values = Arrays.asList("", "value", "");
            InPredicate predicate = new InPredicate("field", values, "p1");
            
            assertEquals("field IN (:p1_0, :p1_1, :p1_2)", predicate.toSQL());
            Map<String, Object> params = predicate.getParameters();
            assertEquals("", params.get("p1_0"));
            assertEquals("value", params.get("p1_1"));
            assertEquals("", params.get("p1_2"));
        }

        @Test
        @DisplayName("should handle special characters in values")
        void shouldHandleSpecialCharactersInValues() {
            List<Object> values = Arrays.asList("O'Malley", "Co. & Sons", "\"Special\"");
            InPredicate predicate = new InPredicate("company", values, "p1");
            
            assertEquals("company IN (:p1_0, :p1_1, :p1_2)", predicate.toSQL());
            Map<String, Object> params = predicate.getParameters();
            assertEquals("O'Malley", params.get("p1_0"));
            assertEquals("Co. & Sons", params.get("p1_1"));
            assertEquals("\"Special\"", params.get("p1_2"));
        }

        @Test
        @DisplayName("should handle large number of values")
        void shouldHandleLargeNumberOfValues() {
            List<Object> values = new ArrayList<>();
            for (int i = 1; i <= 1000; i++) {
                values.add(i);
            }
            InPredicate predicate = new InPredicate("id", values, "p1");
            
            String sql = predicate.toSQL();
            assertTrue(sql.startsWith("id IN (:p1_0, :p1_1"));
            assertTrue(sql.endsWith(":p1_999)"));
            
            Map<String, Object> params = predicate.getParameters();
            assertEquals(1000, params.size());
            assertEquals(1, params.get("p1_0"));
            assertEquals(1000, params.get("p1_999"));
        }

        @Test
        @DisplayName("should handle mixed numeric and string values")
        void shouldHandleMixedValues() {
            List<Object> values = Arrays.asList(1, "string", 3.14, true);
            InPredicate predicate = new InPredicate("mixed_field", values, "p1");
            
            assertEquals("mixed_field IN (:p1_0, :p1_1, :p1_2, :p1_3)", predicate.toSQL());
            Map<String, Object> params = predicate.getParameters();
            assertEquals(1, params.get("p1_0"));
            assertEquals("string", params.get("p1_1"));
            assertEquals(3.14, params.get("p1_2"));
            assertEquals(true, params.get("p1_3"));
        }
        
        @Test
        @DisplayName("should handle multiple InPredicates with different base parameter names")
        void shouldHandleMultipleInPredicatesWithDifferentBaseNames() {
            List<Object> values1 = Arrays.asList("A", "B");
            List<Object> values2 = Arrays.asList("X", "Y", "Z");
            
            InPredicate predicate1 = new InPredicate("status", values1, "p1");
            InPredicate predicate2 = new InPredicate("category", values2, "p2");
            
            // SQL should be generated correctly for each
            assertEquals("status IN (:p1_0, :p1_1)", predicate1.toSQL());
            assertEquals("category IN (:p2_0, :p2_1, :p2_2)", predicate2.toSQL());
            
            // Parameters should not conflict
            Map<String, Object> params1 = predicate1.getParameters();
            Map<String, Object> params2 = predicate2.getParameters();
            
            assertEquals("A", params1.get("p1_0"));
            assertEquals("B", params1.get("p1_1"));
            assertEquals("X", params2.get("p2_0"));
            assertEquals("Y", params2.get("p2_1"));
            assertEquals("Z", params2.get("p2_2"));
            
            // No parameter name conflicts
            Set<String> allParamNames = new HashSet<>();
            allParamNames.addAll(params1.keySet());
            allParamNames.addAll(params2.keySet());
            assertEquals(5, allParamNames.size()); // Should be exactly 5 unique names
        }
    }
}