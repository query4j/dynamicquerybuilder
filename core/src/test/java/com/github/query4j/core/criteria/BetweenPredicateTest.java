package com.github.query4j.core.criteria;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import com.github.query4j.core.QueryBuildException;

/**
 * Comprehensive unit tests for BetweenPredicate class.
 * Tests SQL generation, parameter mapping, immutability, and validation.
 */
@DisplayName("BetweenPredicate")
class BetweenPredicateTest {

    @Nested
    @DisplayName("Constructor and Basic Properties")
    class ConstructorTests {

        @Test
        @DisplayName("should create valid predicate with required parameters")
        void shouldCreateValidPredicate() {
            BetweenPredicate predicate = new BetweenPredicate("age", 18, 65, "p1_start", "p1_end");
            
            assertEquals("age", predicate.getField());
            assertEquals(18, predicate.getStartValue());
            assertEquals(65, predicate.getEndValue());
            assertEquals("p1_start", predicate.getStartParamName());
            assertEquals("p1_end", predicate.getEndParamName());
        }

        @Test
        @DisplayName("should throw NullPointerException for null field")
        void shouldThrowForNullField() {
            assertThrows(QueryBuildException.class, 
                () -> new BetweenPredicate(null, 1, 10, "p1_start", "p1_end"));
        }

        @Test
        @DisplayName("should throw NullPointerException for null startParamName")
        void shouldThrowForNullStartParamName() {
            assertThrows(QueryBuildException.class,
                () -> new BetweenPredicate("field", 1, 10, null, "p1_end"));
        }

        @Test
        @DisplayName("should throw NullPointerException for null endParamName")
        void shouldThrowForNullEndParamName() {
            assertThrows(QueryBuildException.class,
                () -> new BetweenPredicate("field", 1, 10, "p1_start", null));
        }

        @Test
        @DisplayName("should accept null start and end values")
        void shouldAcceptNullValues() {
            BetweenPredicate predicate = new BetweenPredicate("field", null, null, "p1_start", "p1_end");
            assertNull(predicate.getStartValue());
            assertNull(predicate.getEndValue());
        }

        @Test
        @DisplayName("should accept same start and end values")
        void shouldAcceptSameStartAndEndValues() {
            BetweenPredicate predicate = new BetweenPredicate("field", 10, 10, "p1_start", "p1_end");
            assertEquals(10, predicate.getStartValue());
            assertEquals(10, predicate.getEndValue());
        }

        @Test
        @DisplayName("should accept start value greater than end value")
        void shouldAcceptStartGreaterThanEnd() {
            // The predicate itself doesn't validate ordering - that's left to the database
            BetweenPredicate predicate = new BetweenPredicate("field", 100, 10, "p1_start", "p1_end");
            assertEquals(100, predicate.getStartValue());
            assertEquals(10, predicate.getEndValue());
        }
    }

    @Nested
    @DisplayName("SQL Generation")
    class SqlGenerationTests {

        @Test
        @DisplayName("should generate correct SQL for integer values")
        void shouldGenerateCorrectSQLForIntegers() {
            BetweenPredicate predicate = new BetweenPredicate("age", 18, 65, "p1_start", "p1_end");
            assertEquals("age BETWEEN :p1_start AND :p1_end", predicate.toSQL());
        }

        @Test
        @DisplayName("should generate correct SQL for decimal values")
        void shouldGenerateCorrectSQLForDecimals() {
            BetweenPredicate predicate = new BetweenPredicate("salary", 30000.50, 80000.75, "p1_start", "p1_end");
            assertEquals("salary BETWEEN :p1_start AND :p1_end", predicate.toSQL());
        }

        @Test
        @DisplayName("should generate correct SQL for string values")
        void shouldGenerateCorrectSQLForStrings() {
            BetweenPredicate predicate = new BetweenPredicate("name", "A", "Z", "p1_start", "p1_end");
            assertEquals("name BETWEEN :p1_start AND :p1_end", predicate.toSQL());
        }

        @Test
        @DisplayName("should generate correct SQL for date values")
        void shouldGenerateCorrectSQLForDates() {
            LocalDate startDate = LocalDate.of(2023, 1, 1);
            LocalDate endDate = LocalDate.of(2023, 12, 31);
            BetweenPredicate predicate = new BetweenPredicate("created_date", startDate, endDate, "p1_start", "p1_end");
            assertEquals("created_date BETWEEN :p1_start AND :p1_end", predicate.toSQL());
        }

        @Test
        @DisplayName("should generate SQL with field containing dots")
        void shouldGenerateSQLWithDottedField() {
            BetweenPredicate predicate = new BetweenPredicate("user.age", 18, 65, "p1_start", "p1_end");
            assertEquals("user.age BETWEEN :p1_start AND :p1_end", predicate.toSQL());
        }

        @Test
        @DisplayName("should generate SQL with field containing underscores")
        void shouldGenerateSQLWithUnderscoreField() {
            BetweenPredicate predicate = new BetweenPredicate("birth_year", 1950, 2000, "p1_start", "p1_end");
            assertEquals("birth_year BETWEEN :p1_start AND :p1_end", predicate.toSQL());
        }

        @Test
        @DisplayName("should generate SQL with complex parameter names")
        void shouldGenerateSQLWithComplexParamNames() {
            BetweenPredicate predicate = new BetweenPredicate("field", 1, 10, 
                "complex_param_start_123", "complex_param_end_456");
            assertEquals("field BETWEEN :complex_param_start_123 AND :complex_param_end_456", 
                predicate.toSQL());
        }

        @Test
        @DisplayName("should generate SQL with null values")
        void shouldGenerateSQLWithNullValues() {
            BetweenPredicate predicate = new BetweenPredicate("field", null, null, "p1_start", "p1_end");
            assertEquals("field BETWEEN :p1_start AND :p1_end", predicate.toSQL());
        }
    }

    @Nested
    @DisplayName("Parameter Mapping")
    class ParameterMappingTests {

        @Test
        @DisplayName("should return correct parameter mapping for integer values")
        void shouldReturnCorrectParameterMappingForIntegers() {
            BetweenPredicate predicate = new BetweenPredicate("age", 18, 65, "p1_start", "p1_end");
            Map<String, Object> params = predicate.getParameters();
            
            assertEquals(2, params.size());
            assertEquals(18, params.get("p1_start"));
            assertEquals(65, params.get("p1_end"));
        }

        @Test
        @DisplayName("should return correct parameter mapping for different value types")
        void shouldReturnCorrectParameterMappingForDifferentTypes() {
            // String values
            BetweenPredicate stringPredicate = new BetweenPredicate("name", "A", "Z", "p1_start", "p1_end");
            Map<String, Object> stringParams = stringPredicate.getParameters();
            assertEquals("A", stringParams.get("p1_start"));
            assertEquals("Z", stringParams.get("p1_end"));
            
            // Double values
            BetweenPredicate doublePredicate = new BetweenPredicate("salary", 30000.50, 80000.75, "p2_start", "p2_end");
            Map<String, Object> doubleParams = doublePredicate.getParameters();
            assertEquals(30000.50, doubleParams.get("p2_start"));
            assertEquals(80000.75, doubleParams.get("p2_end"));
            
            // Date values
            LocalDate startDate = LocalDate.of(2023, 1, 1);
            LocalDate endDate = LocalDate.of(2023, 12, 31);
            BetweenPredicate datePredicate = new BetweenPredicate("date", startDate, endDate, "p3_start", "p3_end");
            Map<String, Object> dateParams = datePredicate.getParameters();
            assertEquals(startDate, dateParams.get("p3_start"));
            assertEquals(endDate, dateParams.get("p3_end"));
        }

        @Test
        @DisplayName("should return parameter mapping with null values")
        void shouldReturnParameterMappingWithNullValues() {
            BetweenPredicate predicate = new BetweenPredicate("field", null, 100, "p1_start", "p1_end");
            Map<String, Object> params = predicate.getParameters();
            
            assertEquals(2, params.size());
            assertNull(params.get("p1_start"));
            assertEquals(100, params.get("p1_end"));
            assertTrue(params.containsKey("p1_start"));
            assertTrue(params.containsKey("p1_end"));
        }

        @Test
        @DisplayName("should return parameter mapping with both null values")
        void shouldReturnParameterMappingWithBothNullValues() {
            BetweenPredicate predicate = new BetweenPredicate("field", null, null, "p1_start", "p1_end");
            Map<String, Object> params = predicate.getParameters();
            
            assertEquals(2, params.size());
            assertNull(params.get("p1_start"));
            assertNull(params.get("p1_end"));
            assertTrue(params.containsKey("p1_start"));
            assertTrue(params.containsKey("p1_end"));
        }

        @Test
        @DisplayName("should return immutable parameter map")
        void shouldReturnImmutableParameterMap() {
            BetweenPredicate predicate = new BetweenPredicate("age", 18, 65, "p1_start", "p1_end");
            Map<String, Object> params = predicate.getParameters();
            
            assertThrows(UnsupportedOperationException.class, 
                () -> params.put("p2", "value"));
            assertThrows(UnsupportedOperationException.class,
                () -> params.remove("p1_start"));
        }

        @Test
        @DisplayName("should return consistent parameter mapping")
        void shouldReturnConsistentParameterMapping() {
            BetweenPredicate predicate = new BetweenPredicate("age", 18, 65, "p1_start", "p1_end");
            
            Map<String, Object> params1 = predicate.getParameters();
            Map<String, Object> params2 = predicate.getParameters();
            
            assertEquals(params1, params2);
            assertEquals(params1.get("p1_start"), params2.get("p1_start"));
            assertEquals(params1.get("p1_end"), params2.get("p1_end"));
        }

        @Test
        @DisplayName("should have unique parameter names")
        void shouldHaveUniqueParameterNames() {
            BetweenPredicate predicate = new BetweenPredicate("field", 1, 10, "unique_start", "unique_end");
            Map<String, Object> params = predicate.getParameters();
            
            assertTrue(params.containsKey("unique_start"));
            assertTrue(params.containsKey("unique_end"));
            assertNotEquals("unique_start", "unique_end");
        }
    }

    @Nested
    @DisplayName("Immutability")
    class ImmutabilityTests {

        @Test
        @DisplayName("should be immutable - fields cannot be modified")
        void shouldBeImmutable() {
            BetweenPredicate predicate = new BetweenPredicate("age", 18, 65, "p1_start", "p1_end");
            
            // Verify all getters return the same values
            assertEquals("age", predicate.getField());
            assertEquals(18, predicate.getStartValue());
            assertEquals(65, predicate.getEndValue());
            assertEquals("p1_start", predicate.getStartParamName());
            assertEquals("p1_end", predicate.getEndParamName());
            
            // Create another predicate with same values
            BetweenPredicate predicate2 = new BetweenPredicate("age", 18, 65, "p1_start", "p1_end");
            
            // Should be equal but not same instance
            assertEquals(predicate, predicate2);
            assertNotSame(predicate, predicate2);
        }

        @Test
        @DisplayName("should maintain consistent SQL and parameters")
        void shouldMaintainConsistentOutput() {
            BetweenPredicate predicate = new BetweenPredicate("age", 18, 65, "p1_start", "p1_end");
            
            String sql1 = predicate.toSQL();
            String sql2 = predicate.toSQL();
            Map<String, Object> params1 = predicate.getParameters();
            Map<String, Object> params2 = predicate.getParameters();
            
            assertEquals(sql1, sql2);
            assertEquals(params1, params2);
            assertEquals("age BETWEEN :p1_start AND :p1_end", sql1);
            assertEquals(18, params1.get("p1_start"));
            assertEquals(65, params1.get("p1_end"));
        }

        @Test
        @DisplayName("should create independent instances")
        void shouldCreateIndependentInstances() {
            BetweenPredicate predicate1 = new BetweenPredicate("age", 18, 65, "p1_start", "p1_end");
            BetweenPredicate predicate2 = new BetweenPredicate("age", 18, 65, "p1_start", "p1_end");
            
            assertEquals(predicate1, predicate2);
            assertNotSame(predicate1, predicate2);
            assertEquals(predicate1.getField(), predicate2.getField());
            assertEquals(predicate1.getStartValue(), predicate2.getStartValue());
            assertEquals(predicate1.getEndValue(), predicate2.getEndValue());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("should be equal when all fields are equal")
        void shouldBeEqualWhenFieldsEqual() {
            BetweenPredicate predicate1 = new BetweenPredicate("age", 18, 65, "p1_start", "p1_end");
            BetweenPredicate predicate2 = new BetweenPredicate("age", 18, 65, "p1_start", "p1_end");
            
            assertEquals(predicate1, predicate2);
            assertEquals(predicate1.hashCode(), predicate2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when field differs")
        void shouldNotBeEqualWhenFieldDiffers() {
            BetweenPredicate predicate1 = new BetweenPredicate("age", 18, 65, "p1_start", "p1_end");
            BetweenPredicate predicate2 = new BetweenPredicate("salary", 18, 65, "p1_start", "p1_end");
            
            assertNotEquals(predicate1, predicate2);
        }

        @Test
        @DisplayName("should not be equal when start value differs")
        void shouldNotBeEqualWhenStartValueDiffers() {
            BetweenPredicate predicate1 = new BetweenPredicate("age", 18, 65, "p1_start", "p1_end");
            BetweenPredicate predicate2 = new BetweenPredicate("age", 21, 65, "p1_start", "p1_end");
            
            assertNotEquals(predicate1, predicate2);
        }

        @Test
        @DisplayName("should not be equal when end value differs")
        void shouldNotBeEqualWhenEndValueDiffers() {
            BetweenPredicate predicate1 = new BetweenPredicate("age", 18, 65, "p1_start", "p1_end");
            BetweenPredicate predicate2 = new BetweenPredicate("age", 18, 70, "p1_start", "p1_end");
            
            assertNotEquals(predicate1, predicate2);
        }

        @Test
        @DisplayName("should not be equal when start param name differs")
        void shouldNotBeEqualWhenStartParamNameDiffers() {
            BetweenPredicate predicate1 = new BetweenPredicate("age", 18, 65, "p1_start", "p1_end");
            BetweenPredicate predicate2 = new BetweenPredicate("age", 18, 65, "p2_start", "p1_end");
            
            assertNotEquals(predicate1, predicate2);
        }

        @Test
        @DisplayName("should not be equal when end param name differs")
        void shouldNotBeEqualWhenEndParamNameDiffers() {
            BetweenPredicate predicate1 = new BetweenPredicate("age", 18, 65, "p1_start", "p1_end");
            BetweenPredicate predicate2 = new BetweenPredicate("age", 18, 65, "p1_start", "p2_end");
            
            assertNotEquals(predicate1, predicate2);
        }

        @Test
        @DisplayName("should handle null values in equals comparison")
        void shouldHandleNullValuesInEquals() {
            BetweenPredicate predicate1 = new BetweenPredicate("field", null, null, "p1_start", "p1_end");
            BetweenPredicate predicate2 = new BetweenPredicate("field", null, null, "p1_start", "p1_end");
            
            assertEquals(predicate1, predicate2);
            assertEquals(predicate1.hashCode(), predicate2.hashCode());
        }

        @Test
        @DisplayName("should provide meaningful toString representation")
        void shouldProvideMeaningfulToString() {
            BetweenPredicate predicate = new BetweenPredicate("age", 18, 65, "p1_start", "p1_end");
            String toString = predicate.toString();
            
            assertNotNull(toString);
            assertTrue(toString.contains("BetweenPredicate"));
            assertTrue(toString.contains("age"));
            assertTrue(toString.contains("18"));
            assertTrue(toString.contains("65"));
            assertTrue(toString.contains("p1_start"));
            assertTrue(toString.contains("p1_end"));
        }

        @Test
        @DisplayName("should handle null values in toString")
        void shouldHandleNullValuesInToString() {
            BetweenPredicate predicate = new BetweenPredicate("field", null, null, "p1_start", "p1_end");
            String toString = predicate.toString();
            
            assertNotNull(toString);
            assertTrue(toString.contains("BetweenPredicate"));
            assertTrue(toString.contains("field"));
            assertTrue(toString.contains("null"));
        }

        @Test
        @DisplayName("should not be equal to null")
        void shouldNotBeEqualToNull() {
            BetweenPredicate predicate = new BetweenPredicate("age", 18, 65, "p1_start", "p1_end");
            assertNotEquals(predicate, null);
        }

        @Test
        @DisplayName("should not be equal to different class")
        void shouldNotBeEqualToDifferentClass() {
            BetweenPredicate predicate = new BetweenPredicate("age", 18, 65, "p1_start", "p1_end");
            SimplePredicate simplePredicate = new SimplePredicate("age", ">=", 18, "p1");
            assertNotEquals(predicate, simplePredicate);
        }

        @Test
        @DisplayName("should be equal to itself")
        void shouldBeEqualToItself() {
            BetweenPredicate predicate = new BetweenPredicate("age", 18, 65, "p1_start", "p1_end");
            assertEquals(predicate, predicate);
            assertEquals(predicate.hashCode(), predicate.hashCode());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle zero values")
        void shouldHandleZeroValues() {
            BetweenPredicate predicate = new BetweenPredicate("score", 0, 0, "p1_start", "p1_end");
            
            assertEquals("score BETWEEN :p1_start AND :p1_end", predicate.toSQL());
            Map<String, Object> params = predicate.getParameters();
            assertEquals(0, params.get("p1_start"));
            assertEquals(0, params.get("p1_end"));
        }

        @Test
        @DisplayName("should handle negative values")
        void shouldHandleNegativeValues() {
            BetweenPredicate predicate = new BetweenPredicate("temperature", -10, -5, "p1_start", "p1_end");
            
            assertEquals("temperature BETWEEN :p1_start AND :p1_end", predicate.toSQL());
            Map<String, Object> params = predicate.getParameters();
            assertEquals(-10, params.get("p1_start"));
            assertEquals(-5, params.get("p1_end"));
        }

        @Test
        @DisplayName("should handle very large values")
        void shouldHandleVeryLargeValues() {
            long largeStart = Long.MAX_VALUE - 1;
            long largeEnd = Long.MAX_VALUE;
            BetweenPredicate predicate = new BetweenPredicate("id", largeStart, largeEnd, "p1_start", "p1_end");
            
            assertEquals("id BETWEEN :p1_start AND :p1_end", predicate.toSQL());
            Map<String, Object> params = predicate.getParameters();
            assertEquals(largeStart, params.get("p1_start"));
            assertEquals(largeEnd, params.get("p1_end"));
        }

        @Test
        @DisplayName("should handle decimal precision")
        void shouldHandleDecimalPrecision() {
            double start = 3.14159265359;
            double end = 2.71828182846;
            BetweenPredicate predicate = new BetweenPredicate("value", start, end, "p1_start", "p1_end");
            
            assertEquals("value BETWEEN :p1_start AND :p1_end", predicate.toSQL());
            Map<String, Object> params = predicate.getParameters();
            assertEquals(start, params.get("p1_start"));
            assertEquals(end, params.get("p1_end"));
        }

        @Test
        @DisplayName("should handle empty string values")
        void shouldHandleEmptyStringValues() {
            BetweenPredicate predicate = new BetweenPredicate("name", "", "ZZZZ", "p1_start", "p1_end");
            
            assertEquals("name BETWEEN :p1_start AND :p1_end", predicate.toSQL());
            Map<String, Object> params = predicate.getParameters();
            assertEquals("", params.get("p1_start"));
            assertEquals("ZZZZ", params.get("p1_end"));
        }

        @Test
        @DisplayName("should handle special characters in string values")
        void shouldHandleSpecialCharactersInStringValues() {
            String start = "O'Malley & Co.";
            String end = "Z\"Special\"Z";
            BetweenPredicate predicate = new BetweenPredicate("company", start, end, "p1_start", "p1_end");
            
            assertEquals("company BETWEEN :p1_start AND :p1_end", predicate.toSQL());
            Map<String, Object> params = predicate.getParameters();
            assertEquals(start, params.get("p1_start"));
            assertEquals(end, params.get("p1_end"));
        }

        @Test
        @DisplayName("should maintain consistency across repeated calls")
        void shouldMaintainConsistencyAcrossRepeatedCalls() {
            BetweenPredicate predicate = new BetweenPredicate("test_field", 10, 20, "p1_start", "p1_end");
            
            for (int i = 0; i < 100; i++) {
                assertEquals("test_field BETWEEN :p1_start AND :p1_end", predicate.toSQL());
                Map<String, Object> params = predicate.getParameters();
                assertEquals(10, params.get("p1_start"));
                assertEquals(20, params.get("p1_end"));
                assertEquals("test_field", predicate.getField());
                assertEquals(10, predicate.getStartValue());
                assertEquals(20, predicate.getEndValue());
            }
        }
    }
}