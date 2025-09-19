package com.github.query4j.core.criteria;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for CustomFunctionPredicate.
 */
class CustomFunctionPredicateTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create predicate with valid parameters")
        void shouldCreateWithValidParameters() {
            CustomFunctionPredicate predicate = new CustomFunctionPredicate(
                "UPPER", "name", new Object[]{"test"}, "param"
            );

            assertEquals("UPPER", predicate.getFunctionName());
            assertEquals("name", predicate.getFieldName());
            assertEquals("param", predicate.getParamPrefix());
            
            // Verify parameters are properly mapped in getParameters() map
            Map<String, Object> paramMap = predicate.getParameters();
            assertEquals(1, paramMap.size());
            assertEquals("test", paramMap.get("param_0"));
        }

        @Test
        @DisplayName("Should handle null function name")
        void shouldRejectNullFunctionName() {
            assertThrows(IllegalArgumentException.class, () ->
                new CustomFunctionPredicate(null, "field", new Object[0], "param")
            );
        }

        @Test
        @DisplayName("Should handle empty function name")
        void shouldRejectEmptyFunctionName() {
            assertThrows(IllegalArgumentException.class, () ->
                new CustomFunctionPredicate("", "field", new Object[0], "param")
            );
            
            assertThrows(IllegalArgumentException.class, () ->
                new CustomFunctionPredicate("   ", "field", new Object[0], "param")
            );
        }

        @Test
        @DisplayName("Should handle null field name")
        void shouldRejectNullFieldName() {
            assertThrows(com.github.query4j.core.QueryBuildException.class, () ->
                new CustomFunctionPredicate("FUNC", null, new Object[0], "param")
            );
        }

        @Test
        @DisplayName("Should handle null parameter prefix")
        void shouldRejectNullParamPrefix() {
            assertThrows(IllegalArgumentException.class, () ->
                new CustomFunctionPredicate("FUNC", "field", new Object[0], null)
            );
        }

        @Test
        @DisplayName("Should handle null parameters array")
        void shouldHandleNullParameters() {
            CustomFunctionPredicate predicate = new CustomFunctionPredicate(
                "FUNC", "field", null, "param"
            );

            // Verify empty parameter map for null parameters
            Map<String, Object> paramMap = predicate.getParameters();
            assertTrue(paramMap.isEmpty());
        }

        @Test
        @DisplayName("Should normalize function name to uppercase")
        void shouldNormalizeFunctionName() {
            CustomFunctionPredicate predicate = new CustomFunctionPredicate(
                "lower", "field", new Object[0], "param"
            );

            assertEquals("LOWER", predicate.getFunctionName());
        }

        @Test
        @DisplayName("Should trim whitespace from inputs")
        void shouldTrimWhitespace() {
            CustomFunctionPredicate predicate = new CustomFunctionPredicate(
                "  UPPER  ", "  field  ", new Object[0], "  param  "
            );

            assertEquals("UPPER", predicate.getFunctionName());
            assertEquals("field", predicate.getFieldName());
            assertEquals("param", predicate.getParamPrefix());
        }
    }

    @Nested
    @DisplayName("SQL Generation Tests")
    class SqlGenerationTests {

        @Test
        @DisplayName("Should generate SQL for function with no parameters")
        void shouldGenerateSqlWithoutParameters() {
            CustomFunctionPredicate predicate = new CustomFunctionPredicate(
                "UPPER", "name", new Object[0], "param"
            );

            assertEquals("UPPER(name)", predicate.toSQL());
        }

        @Test
        @DisplayName("Should generate SQL for function with single parameter")
        void shouldGenerateSqlWithSingleParameter() {
            CustomFunctionPredicate predicate = new CustomFunctionPredicate(
                "SUBSTRING", "name", new Object[]{1}, "param"
            );

            assertEquals("SUBSTRING(name, :param_0)", predicate.toSQL());
        }

        @Test
        @DisplayName("Should generate SQL for function with multiple parameters")
        void shouldGenerateSqlWithMultipleParameters() {
            CustomFunctionPredicate predicate = new CustomFunctionPredicate(
                "SUBSTRING", "name", new Object[]{1, 5}, "param"
            );

            assertEquals("SUBSTRING(name, :param_0, :param_1)", predicate.toSQL());
        }
    }

    @Nested
    @DisplayName("Parameter Mapping Tests")
    class ParameterMappingTests {

        @Test
        @DisplayName("Should return empty map for no parameters")
        void shouldReturnEmptyMapForNoParameters() {
            CustomFunctionPredicate predicate = new CustomFunctionPredicate(
                "UPPER", "name", new Object[0], "param"
            );

            Map<String, Object> params = predicate.getParameters();
            assertTrue(params.isEmpty());
        }

        @Test
        @DisplayName("Should map single parameter correctly")
        void shouldMapSingleParameter() {
            CustomFunctionPredicate predicate = new CustomFunctionPredicate(
                "FUNC", "field", new Object[]{"value"}, "test"
            );

            Map<String, Object> params = predicate.getParameters();
            assertEquals(1, params.size());
            assertEquals("value", params.get("test_0"));
        }

        @Test
        @DisplayName("Should map multiple parameters correctly")
        void shouldMapMultipleParameters() {
            CustomFunctionPredicate predicate = new CustomFunctionPredicate(
                "FUNC", "field", new Object[]{"val1", 42, true}, "prefix"
            );

            Map<String, Object> params = predicate.getParameters();
            assertEquals(3, params.size());
            assertEquals("val1", params.get("prefix_0"));
            assertEquals(42, params.get("prefix_1"));
            assertEquals(true, params.get("prefix_2"));
        }

        @Test
        @DisplayName("Should return immutable parameter map")
        void shouldReturnImmutableParameterMap() {
            CustomFunctionPredicate predicate = new CustomFunctionPredicate(
                "FUNC", "field", new Object[]{"test"}, "param"
            );

            Map<String, Object> params = predicate.getParameters();
            assertThrows(UnsupportedOperationException.class, () ->
                params.put("new", "value")
            );
        }
    }

    @Nested
    @DisplayName("Equality and Immutability Tests")
    class EqualityAndImmutabilityTests {

        @Test
        @DisplayName("Should be equal when all properties match")
        void shouldBeEqualWhenPropertiesMatch() {
            CustomFunctionPredicate predicate1 = new CustomFunctionPredicate(
                "UPPER", "name", new Object[]{"test"}, "param"
            );
            CustomFunctionPredicate predicate2 = new CustomFunctionPredicate(
                "UPPER", "name", new Object[]{"test"}, "param"
            );

            assertEquals(predicate1, predicate2);
            assertEquals(predicate1.hashCode(), predicate2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when properties differ")
        void shouldNotBeEqualWhenPropertiesDiffer() {
            CustomFunctionPredicate predicate1 = new CustomFunctionPredicate(
                "UPPER", "name", new Object[]{"test"}, "param"
            );
            CustomFunctionPredicate predicate2 = new CustomFunctionPredicate(
                "LOWER", "name", new Object[]{"test"}, "param"
            );

            assertNotEquals(predicate1, predicate2);
        }

        @Test
        @DisplayName("Should be immutable - parameter array changes don't affect predicate")
        void shouldBeImmutable() {
            Object[] originalParams = {"test", 42};
            CustomFunctionPredicate predicate = new CustomFunctionPredicate(
                "FUNC", "field", originalParams, "param"
            );

            // Modify original array
            originalParams[0] = "modified";

            // Predicate should retain original values
            Map<String, Object> params = predicate.getParameters();
            assertEquals("test", params.get("param_0"));
            assertEquals(42, params.get("param_1"));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null parameter values")
        void shouldHandleNullParameterValues() {
            CustomFunctionPredicate predicate = new CustomFunctionPredicate(
                "FUNC", "field", new Object[]{null, "value", null}, "param"
            );

            Map<String, Object> params = predicate.getParameters();
            assertEquals(3, params.size());
            assertNull(params.get("param_0"));
            assertEquals("value", params.get("param_1"));
            assertNull(params.get("param_2"));
        }

        @Test
        @DisplayName("Should handle complex parameter types")
        void shouldHandleComplexParameterTypes() {
            java.time.LocalDate date = java.time.LocalDate.now();
            java.math.BigDecimal decimal = new java.math.BigDecimal("123.45");

            CustomFunctionPredicate predicate = new CustomFunctionPredicate(
                "FUNC", "field", new Object[]{date, decimal}, "param"
            );

            Map<String, Object> params = predicate.getParameters();
            assertEquals(date, params.get("param_0"));
            assertEquals(decimal, params.get("param_1"));
        }
    }
}