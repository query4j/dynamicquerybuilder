package com.github.query4j.core.criteria;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

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
        @DisplayName("should throw QueryBuildException for duplicate parameter names")
        void shouldThrowForDuplicateParameterNames() {
            assertThrows(QueryBuildException.class,
                () -> new BetweenPredicate("field", 1, 10, "sameName", "sameName"));
        }

        @Test
        @DisplayName("should throw QueryBuildException for trimmed duplicate parameter names")
        void shouldThrowForTrimmedDuplicateParameterNames() {
            assertThrows(QueryBuildException.class,
                () -> new BetweenPredicate("field", 1, 10, " sameName ", "sameName "));
        }

        @Test
        @DisplayName("should throw QueryBuildException for duplicate names with extra whitespace")
        void shouldThrowForDuplicateNamesWithWhitespace() {
            assertThrows(QueryBuildException.class,
                () -> new BetweenPredicate("field", 1, 10, "param1  ", "  param1"));
        }

        @Test
        @DisplayName("should throw QueryBuildException for identical trimmed parameter names with different whitespace")
        void shouldThrowForIdenticalTrimmedNamesWithDifferentWhitespace() {
            assertThrows(QueryBuildException.class,
                () -> new BetweenPredicate("field", 1, 10, "\tparam1\n", " param1 "));
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
        @DisplayName("should not be equal to null object")
        void shouldNotBeEqualToNullObject() {
            BetweenPredicate predicate = new BetweenPredicate("field", 1, 10, "p1_start", "p1_end");
            
            assertNotEquals(predicate, null);
        }

        @Test
        @DisplayName("should not be equal to different object type")
        void shouldNotBeEqualToDifferentObjectType() {
            BetweenPredicate predicate = new BetweenPredicate("field", 1, 10, "p1_start", "p1_end");
            String different = "different type";
            
            assertNotEquals(predicate, different);
        }

        @Test
        @DisplayName("should be equal to itself always")
        void shouldBeEqualToItselfAlways() {
            BetweenPredicate predicate = new BetweenPredicate("field", 1, 10, "p1_start", "p1_end");
            
            assertEquals(predicate, predicate);
        }

        @Test
        @DisplayName("should have consistent hashCode for equal objects")
        void shouldHaveConsistentHashCodeForEqualObjects() {
            BetweenPredicate predicate1 = new BetweenPredicate("field", 1, 10, "p1_start", "p1_end");
            BetweenPredicate predicate2 = new BetweenPredicate("field", 1, 10, "p1_start", "p1_end");
            
            assertEquals(predicate1.hashCode(), predicate2.hashCode());
            
            // Multiple calls should return same hash code
            int hash1 = predicate1.hashCode();
            int hash2 = predicate1.hashCode();
            assertEquals(hash1, hash2);
        }

        @Test
        @DisplayName("should have different hashCode for different objects")
        void shouldHaveDifferentHashCodeForDifferentObjects() {
            BetweenPredicate predicate1 = new BetweenPredicate("field1", 1, 10, "p1_start", "p1_end");
            BetweenPredicate predicate2 = new BetweenPredicate("field2", 1, 10, "p1_start", "p1_end");
            
            assertNotEquals(predicate1.hashCode(), predicate2.hashCode());
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

    @Nested
    @DisplayName("Branch Coverage for Parameter Validation")
    class BranchCoverageTests {

        @Test
        @DisplayName("should cover all branches in parameter collision check")
        void shouldCoverAllBranchesInParameterCollisionCheck() {
            // This test is designed to improve branch coverage for the conditional:
            // if (startParamName != null && endParamName != null && 
            //     startParamName.trim().equals(endParamName.trim()))
            
            // Branch 1: Successful construction with different parameter names (no collision)
            // This exercises the FALSE branch where collision check passes
            BetweenPredicate successPredicate = new BetweenPredicate("field", 1, 10, "start", "end");
            assertNotNull(successPredicate);
            assertEquals("start", successPredicate.getStartParamName());
            assertEquals("end", successPredicate.getEndParamName());
            
            // Branch 2: Collision detected with identical names (TRUE branch)
            QueryBuildException collisionEx = assertThrows(QueryBuildException.class,
                () -> new BetweenPredicate("field", 1, 10, "duplicate", "duplicate"));
            assertTrue(collisionEx.getMessage().contains("Start and end parameter names must be different"));
            
            // Branch 3: Collision detected after trimming (TRUE branch with whitespace)
            QueryBuildException trimCollisionEx = assertThrows(QueryBuildException.class,
                () -> new BetweenPredicate("field", 1, 10, "  name  ", "name"));
            assertTrue(trimCollisionEx.getMessage().contains("Start and end parameter names must be different"));
        }

        @Test
        @DisplayName("should verify parameter validation precedence over collision check")
        void shouldVerifyParameterValidationPrecedenceOverCollisionCheck() {
            // Ensure that parameter validation (which can throw for null) occurs before
            // the collision check, confirming the logical flow
            
            // Test null start parameter - should fail with parameter validation error
            QueryBuildException nullStartEx = assertThrows(QueryBuildException.class,
                () -> new BetweenPredicate("field", 1, 10, null, "valid"));
            assertTrue(nullStartEx.getMessage().contains("Parameter name must not be null"));
            
            // Test null end parameter - should fail with parameter validation error  
            QueryBuildException nullEndEx = assertThrows(QueryBuildException.class,
                () -> new BetweenPredicate("field", 1, 10, "valid", null));
            assertTrue(nullEndEx.getMessage().contains("Parameter name must not be null"));
            
            // This confirms that the defensive null checks in the collision logic
            // are currently unreachable but represent good defensive programming
        }

        @Test
        @DisplayName("should test complex whitespace scenarios in collision detection")
        void shouldTestComplexWhitespaceInCollisionDetection() {
            // Test various whitespace combinations to ensure trim() logic is properly covered
            
            // Case 1: Leading whitespace that becomes identical after trim
            assertThrows(QueryBuildException.class,
                () -> new BetweenPredicate("field", 1, 10, "   param", "param"));
                
            // Case 2: Trailing whitespace that becomes identical after trim
            assertThrows(QueryBuildException.class,
                () -> new BetweenPredicate("field", 1, 10, "param   ", "param"));
                
            // Case 3: Both leading and trailing whitespace
            assertThrows(QueryBuildException.class,
                () -> new BetweenPredicate("field", 1, 10, "  param  ", "param"));
                
            // Case 4: Different types of whitespace (tabs, newlines) - should become same after trim
            assertThrows(QueryBuildException.class,
                () -> new BetweenPredicate("field", 1, 10, "\tparam\n", " param "));
                
            // Case 5: Parameters that remain different after trimming - should succeed
            BetweenPredicate differentPredicate = new BetweenPredicate("field", 1, 10, "  param1  ", "  param2  ");
            assertEquals("param1", differentPredicate.getStartParamName());
            assertEquals("param2", differentPredicate.getEndParamName());
        }

        @Test
        @DisplayName("should ensure defensive null check branches are documented") 
        void shouldEnsureDefensiveNullCheckBranchesAreDocumented() {
            // While the null checks in the collision detection are currently unreachable
            // due to prior validation, they represent defensive programming.
            // This test documents their intended behavior if they were ever reached.
            
            // The intended behavior would be:
            // - If startParamName is null, skip collision check (short-circuit AND)
            // - If endParamName is null, skip collision check (short-circuit AND) 
            // - Only check for collision if both are non-null
            
            // Since we cannot reach these branches with null values due to validation,
            // we verify that validation properly prevents null values from reaching that point
            
            assertThrows(QueryBuildException.class, () -> {
                new BetweenPredicate("field", 1, 10, null, "param");
            });
            
            assertThrows(QueryBuildException.class, () -> {
                new BetweenPredicate("field", 1, 10, "param", null);
            });
            
            // This test serves as documentation that the null checks exist for defensive purposes
            // even though they are not currently reachable in normal execution flow
        }

        @Test
        @DisplayName("should achieve complete branch coverage for parameter collision detection")
        void shouldAchieveCompleteBranchCoverageForParameterCollisionDetection() {
            // After refactoring, the collision check is simplified to:
            // if (startParamName.trim().equals(endParamName.trim()))
            // This is more testable and covers the essential logic without unreachable defensive null checks
            
            // Test collision detection with various scenarios
            
            // Branch 1: Parameters are different after trimming (FALSE - no collision)
            BetweenPredicate neverCollision = new BetweenPredicate("field", 1, 10, "start", "end");
            assertNotNull(neverCollision);
            assertEquals("start", neverCollision.getStartParamName());
            assertEquals("end", neverCollision.getEndParamName());
            
            // Branch 2: Parameters are identical after trimming (TRUE - collision detected)
            QueryBuildException directCollision = assertThrows(QueryBuildException.class,
                () -> new BetweenPredicate("field", 1, 10, "collision", "collision"));
            assertTrue(directCollision.getMessage().contains("Start and end parameter names must be different"));
            
            // Branch 3: Parameters become identical after trimming whitespace (TRUE - collision)
            QueryBuildException trimCollision = assertThrows(QueryBuildException.class,
                () -> new BetweenPredicate("field", 1, 10, " trimmed ", "trimmed"));
            assertTrue(trimCollision.getMessage().contains("Start and end parameter names must be different"));
            
            // Branch 4: Complex whitespace scenarios that result in collision (TRUE)
            QueryBuildException complexTrimCollision = assertThrows(QueryBuildException.class,
                () -> new BetweenPredicate("field", 1, 10, "\tparam\n", " param "));
            assertTrue(complexTrimCollision.getMessage().contains("Start and end parameter names must be different"));
            
            // Verify that validation still prevents null values appropriately
            assertThrows(QueryBuildException.class,
                () -> new BetweenPredicate("field", 1, 10, null, "valid"));
            assertThrows(QueryBuildException.class,
                () -> new BetweenPredicate("field", 1, 10, "valid", null));
        }
        
        @Test
        @DisplayName("should verify comprehensive collision detection with edge cases")
        void shouldVerifyComprehensiveCollisionDetectionWithEdgeCases() {
            // Test additional edge cases for collision detection to ensure robustness
            
            // Case 1: Parameters that look similar but are different after trim
            BetweenPredicate similarButDifferent = new BetweenPredicate("field", 1, 10, "param1", "param2");
            assertEquals("param1", similarButDifferent.getStartParamName());
            assertEquals("param2", similarButDifferent.getEndParamName());
            
            // Case 2: Parameters with different amounts of whitespace but same core content
            QueryBuildException whitespacEnzCollision = assertThrows(QueryBuildException.class,
                () -> new BetweenPredicate("field", 1, 10, "   core   ", "\tcore\n"));
            assertTrue(whitespacEnzCollision.getMessage().contains("Start and end parameter names must be different"));
            
            // Case 3: Empty strings after trimming would be invalid anyway due to validation
            QueryBuildException emptyParam = assertThrows(QueryBuildException.class,
                () -> new BetweenPredicate("field", 1, 10, "   ", "valid"));
            assertTrue(emptyParam.getMessage().contains("Parameter name must not be empty"));
            
            // Case 4: Parameters with only case differences - should NOT collide (case sensitive)
            BetweenPredicate caseTeidifference = new BetweenPredicate("field", 1, 10, "Param", "param");
            assertEquals("Param", caseTeidifference.getStartParamName());
            assertEquals("param", caseTeidifference.getEndParamName());
            
            // Case 5: Parameters with underscore differences - should NOT collide
            BetweenPredicate underscoreHydifference = new BetweenPredicate("field", 1, 10, "param_1", "param1");
            assertEquals("param_1", underscoreHydifference.getStartParamName());
            assertEquals("param1", underscoreHydifference.getEndParamName());
        }

        @Test
        @DisplayName("should document defensive programming rationale for null checks")
        void shouldDocumentDefensiveProgrammingRationaleForNullChecks() {
            // This test documents why the defensive null checks exist in the collision detection
            // even though they are currently unreachable due to validation
            
            // The defensive null checks serve several purposes:
            // 1. Future-proofing: If validation logic changes, the null checks prevent NPE
            // 2. Code readability: Makes the intention clear about handling null parameters
            // 3. Robustness: Provides additional safety layer
            
            // Current behavior: Validation prevents null values from reaching collision check
            assertThrows(QueryBuildException.class,
                () -> new BetweenPredicate("field", 1, 10, null, "valid"),
                "Null start parameter should be caught by validation");
                
            assertThrows(QueryBuildException.class, 
                () -> new BetweenPredicate("field", 1, 10, "valid", null),
                "Null end parameter should be caught by validation");
            
            // The defensive null checks in the collision detection represent good programming practice
            // even if they're not currently reachable through normal execution paths
            
            // Test all reachable collision detection scenarios
            BetweenPredicate nontCollision = new BetweenPredicate("field", 1, 10, "start", "end");
            assertEquals("start", nontCollision.getStartParamName());
            assertEquals("end", nontCollision.getEndParamName());
            
            QueryBuildException collisionEx = assertThrows(QueryBuildException.class,
                () -> new BetweenPredicate("field", 1, 10, "collision", "collision"));
            assertTrue(collisionEx.getMessage().contains("Start and end parameter names must be different"));
        }
    }
}