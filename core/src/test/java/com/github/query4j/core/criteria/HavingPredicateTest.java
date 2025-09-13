package com.github.query4j.core.criteria;

import com.github.query4j.core.QueryBuildException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HavingPredicate class.
 */
@DisplayName("HavingPredicate")
class HavingPredicateTest {

    @Nested
    @DisplayName("Constructor Validation")
    class ConstructorValidationTests {

        @Test
        @DisplayName("should create HavingPredicate with regular field name")
        void shouldCreateHavingPredicateWithRegularFieldName() {
            HavingPredicate predicate = new HavingPredicate("department", ">", 5, "param1");
            
            assertEquals("department", predicate.getAggregatedField());
            assertEquals(">", predicate.getOperator());
            assertEquals(5, predicate.getValue());
            assertEquals("param1", predicate.getParamName());
        }

        @Test
        @DisplayName("should create HavingPredicate with aggregation function")
        void shouldCreateHavingPredicateWithAggregationFunction() {
            HavingPredicate predicate = new HavingPredicate("COUNT(id)", ">", 10, "param1");
            
            assertEquals("COUNT(id)", predicate.getAggregatedField());
            assertEquals(">", predicate.getOperator());
            assertEquals(10, predicate.getValue());
            assertEquals("param1", predicate.getParamName());
        }

        @Test
        @DisplayName("should create HavingPredicate with various aggregation functions")
        void shouldCreateHavingPredicateWithVariousAggregationFunctions() {
            assertDoesNotThrow(() -> new HavingPredicate("SUM(amount)", ">=", 1000, "param1"));
            assertDoesNotThrow(() -> new HavingPredicate("AVG(price)", "<", 50.0, "param2"));
            assertDoesNotThrow(() -> new HavingPredicate("MIN(created_date)", "=", "2023-01-01", "param3"));
            assertDoesNotThrow(() -> new HavingPredicate("MAX(updated_date)", "!=", "2023-12-31", "param4"));
            assertDoesNotThrow(() -> new HavingPredicate("COUNT(*)", ">", 0, "param5"));
        }

        @Test
        @DisplayName("should throw for null aggregated field name")
        void shouldThrowForNullAggregatedFieldName() {
            assertThrows(QueryBuildException.class, 
                () -> new HavingPredicate(null, ">", 5, "param1"));
        }

        @Test
        @DisplayName("should throw for empty aggregated field name")
        void shouldThrowForEmptyAggregatedFieldName() {
            assertThrows(QueryBuildException.class, 
                () -> new HavingPredicate("", ">", 5, "param1"));
            assertThrows(QueryBuildException.class, 
                () -> new HavingPredicate("   ", ">", 5, "param1"));
        }

        @Test
        @DisplayName("should throw for invalid aggregated field name")
        void shouldThrowForInvalidAggregatedFieldName() {
            // Invalid characters in regular field names
            assertThrows(QueryBuildException.class, 
                () -> new HavingPredicate("invalid@field", ">", 5, "param1"));
            assertThrows(QueryBuildException.class, 
                () -> new HavingPredicate("invalid-field", ">", 5, "param1"));
            
            // Invalid aggregation function format
            assertThrows(QueryBuildException.class, 
                () -> new HavingPredicate("COUNT(", ">", 5, "param1"));
            assertThrows(QueryBuildException.class, 
                () -> new HavingPredicate("COUNT)", ">", 5, "param1"));
            assertThrows(QueryBuildException.class, 
                () -> new HavingPredicate("COUNT(invalid@field)", ">", 5, "param1"));
        }

        @Test
        @DisplayName("should throw for null operator")
        void shouldThrowForNullOperator() {
            assertThrows(QueryBuildException.class, 
                () -> new HavingPredicate("department", null, 5, "param1"));
        }

        @Test
        @DisplayName("should throw for invalid operator")
        void shouldThrowForInvalidOperator() {
            assertThrows(QueryBuildException.class, 
                () -> new HavingPredicate("department", "INVALID", 5, "param1"));
        }

        @Test
        @DisplayName("should throw for null parameter name")
        void shouldThrowForNullParameterName() {
            assertThrows(QueryBuildException.class, 
                () -> new HavingPredicate("department", ">", 5, null));
        }

        @Test
        @DisplayName("should throw for invalid parameter name")
        void shouldThrowForInvalidParameterName() {
            assertThrows(QueryBuildException.class, 
                () -> new HavingPredicate("department", ">", 5, "123invalid"));
            assertThrows(QueryBuildException.class, 
                () -> new HavingPredicate("department", ">", 5, "invalid@param"));
        }

        @Test
        @DisplayName("should trim whitespace from field and operator")
        void shouldTrimWhitespaceFromFieldAndOperator() {
            HavingPredicate predicate = new HavingPredicate("  COUNT(id)  ", "  >  ", 5, "  param1  ");
            
            assertEquals("COUNT(id)", predicate.getAggregatedField());
            assertEquals(">", predicate.getOperator());
            assertEquals("param1", predicate.getParamName());
        }
    }

    @Nested
    @DisplayName("SQL Generation")
    class SqlGenerationTests {

        @Test
        @DisplayName("should generate correct SQL for regular field")
        void shouldGenerateCorrectSqlForRegularField() {
            HavingPredicate predicate = new HavingPredicate("department", ">", 5, "param1");
            
            assertEquals("department > :param1", predicate.toSQL());
        }

        @Test
        @DisplayName("should generate correct SQL for aggregation functions")
        void shouldGenerateCorrectSqlForAggregationFunctions() {
            HavingPredicate countPredicate = new HavingPredicate("COUNT(id)", ">", 10, "count_param");
            assertEquals("COUNT(id) > :count_param", countPredicate.toSQL());
            
            HavingPredicate sumPredicate = new HavingPredicate("SUM(amount)", ">=", 1000, "sum_param");
            assertEquals("SUM(amount) >= :sum_param", sumPredicate.toSQL());
            
            HavingPredicate avgPredicate = new HavingPredicate("AVG(price)", "<", 50.0, "avg_param");
            assertEquals("AVG(price) < :avg_param", avgPredicate.toSQL());
        }

        @Test
        @DisplayName("should generate SQL with all supported operators")
        void shouldGenerateSqlWithAllSupportedOperators() {
            assertEquals("COUNT(id) = :p1", 
                new HavingPredicate("COUNT(id)", "=", 5, "p1").toSQL());
            assertEquals("COUNT(id) != :p2", 
                new HavingPredicate("COUNT(id)", "!=", 5, "p2").toSQL());
            assertEquals("COUNT(id) <> :p3", 
                new HavingPredicate("COUNT(id)", "<>", 5, "p3").toSQL());
            assertEquals("COUNT(id) < :p4", 
                new HavingPredicate("COUNT(id)", "<", 5, "p4").toSQL());
            assertEquals("COUNT(id) <= :p5", 
                new HavingPredicate("COUNT(id)", "<=", 5, "p5").toSQL());
            assertEquals("COUNT(id) > :p6", 
                new HavingPredicate("COUNT(id)", ">", 5, "p6").toSQL());
            assertEquals("COUNT(id) >= :p7", 
                new HavingPredicate("COUNT(id)", ">=", 5, "p7").toSQL());
        }
    }

    @Nested
    @DisplayName("Parameter Mapping")
    class ParameterMappingTests {

        @Test
        @DisplayName("should return correct parameter mapping")
        void shouldReturnCorrectParameterMapping() {
            HavingPredicate predicate = new HavingPredicate("COUNT(id)", ">", 42, "count_param");
            
            Map<String, Object> params = predicate.getParameters();
            
            assertEquals(1, params.size());
            assertEquals(42, params.get("count_param"));
        }

        @Test
        @DisplayName("should handle null values")
        void shouldHandleNullValues() {
            HavingPredicate predicate = new HavingPredicate("SUM(amount)", "=", null, "sum_param");
            
            Map<String, Object> params = predicate.getParameters();
            
            assertEquals(1, params.size());
            assertNull(params.get("sum_param"));
        }

        @Test
        @DisplayName("should return immutable parameter map")
        void shouldReturnImmutableParameterMap() {
            HavingPredicate predicate = new HavingPredicate("AVG(price)", "<", 100.0, "avg_param");
            
            Map<String, Object> params = predicate.getParameters();
            
            assertThrows(UnsupportedOperationException.class, 
                () -> params.put("newKey", "newValue"));
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("should be equal for identical predicates")
        void shouldBeEqualForIdenticalPredicates() {
            HavingPredicate predicate1 = new HavingPredicate("COUNT(id)", ">", 5, "param1");
            HavingPredicate predicate2 = new HavingPredicate("COUNT(id)", ">", 5, "param1");
            
            assertEquals(predicate1, predicate2);
            assertEquals(predicate1.hashCode(), predicate2.hashCode());
        }

        @Test
        @DisplayName("should not be equal for different field names")
        void shouldNotBeEqualForDifferentFieldNames() {
            HavingPredicate predicate1 = new HavingPredicate("COUNT(id)", ">", 5, "param1");
            HavingPredicate predicate2 = new HavingPredicate("SUM(amount)", ">", 5, "param1");
            
            assertNotEquals(predicate1, predicate2);
        }

        @Test
        @DisplayName("should not be equal for different operators")
        void shouldNotBeEqualForDifferentOperators() {
            HavingPredicate predicate1 = new HavingPredicate("COUNT(id)", ">", 5, "param1");
            HavingPredicate predicate2 = new HavingPredicate("COUNT(id)", "<", 5, "param1");
            
            assertNotEquals(predicate1, predicate2);
        }

        @Test
        @DisplayName("should not be equal for different values")
        void shouldNotBeEqualForDifferentValues() {
            HavingPredicate predicate1 = new HavingPredicate("COUNT(id)", ">", 5, "param1");
            HavingPredicate predicate2 = new HavingPredicate("COUNT(id)", ">", 10, "param1");
            
            assertNotEquals(predicate1, predicate2);
        }

        @Test
        @DisplayName("should not be equal for different parameter names")
        void shouldNotBeEqualForDifferentParameterNames() {
            HavingPredicate predicate1 = new HavingPredicate("COUNT(id)", ">", 5, "param1");
            HavingPredicate predicate2 = new HavingPredicate("COUNT(id)", ">", 5, "param2");
            
            assertNotEquals(predicate1, predicate2);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("should work with complex aggregation expressions")
        void shouldWorkWithComplexAggregationExpressions() {
            // Test with qualified field names in aggregation functions
            assertDoesNotThrow(() -> 
                new HavingPredicate("COUNT(user.profile_id)", ">", 0, "param1"));
            assertDoesNotThrow(() -> 
                new HavingPredicate("SUM(order_details.amount)", ">=", 1000, "param2"));
            assertDoesNotThrow(() -> 
                new HavingPredicate("AVG(product.price)", "BETWEEN", 10.0, "param3"));
        }
    }
}