package com.github.query4j.core.criteria;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import com.github.query4j.core.QueryBuildException;

/**
 * Comprehensive unit tests for NullPredicate class.
 * Tests SQL generation, parameter mapping, immutability, and validation.
 */
@DisplayName("NullPredicate")
class NullPredicateTest {

    @Nested
    @DisplayName("Constructor and Basic Properties")
    class ConstructorTests {

        @Test
        @DisplayName("should create valid IS NULL predicate")
        void shouldCreateValidIsNullPredicate() {
            NullPredicate predicate = new NullPredicate("field", true);
            
            assertEquals("field", predicate.getField());
            assertTrue(predicate.isNull());
        }

        @Test
        @DisplayName("should create valid IS NOT NULL predicate")
        void shouldCreateValidIsNotNullPredicate() {
            NullPredicate predicate = new NullPredicate("field", false);
            
            assertEquals("field", predicate.getField());
            assertFalse(predicate.isNull());
        }

        @Test
        @DisplayName("should throw NullPointerException for null field")
        void shouldThrowForNullField() {
            assertThrows(QueryBuildException.class, 
                () -> new NullPredicate(null, true));
        }

        @Test
        @DisplayName("should accept field with dots")
        void shouldAcceptFieldWithDots() {
            NullPredicate predicate = new NullPredicate("user.email", true);
            assertEquals("user.email", predicate.getField());
        }

        @Test
        @DisplayName("should accept field with underscores")
        void shouldAcceptFieldWithUnderscores() {
            NullPredicate predicate = new NullPredicate("first_name", true);
            assertEquals("first_name", predicate.getField());
        }

        @Test
        @DisplayName("should accept alphanumeric field names")
        void shouldAcceptAlphanumericFieldNames() {
            NullPredicate predicate = new NullPredicate("field123", true);
            assertEquals("field123", predicate.getField());
        }
    }

    @Nested
    @DisplayName("SQL Generation")
    class SqlGenerationTests {

        @Test
        @DisplayName("should generate correct SQL for IS NULL")
        void shouldGenerateIsNullSQL() {
            NullPredicate predicate = new NullPredicate("email", true);
            assertEquals("email IS NULL", predicate.toSQL());
        }

        @Test
        @DisplayName("should generate correct SQL for IS NOT NULL")
        void shouldGenerateIsNotNullSQL() {
            NullPredicate predicate = new NullPredicate("email", false);
            assertEquals("email IS NOT NULL", predicate.toSQL());
        }

        @Test
        @DisplayName("should generate SQL with field containing dots")
        void shouldGenerateSQLWithDottedField() {
            NullPredicate predicate = new NullPredicate("user.email", true);
            assertEquals("user.email IS NULL", predicate.toSQL());
        }

        @Test
        @DisplayName("should generate SQL with field containing underscores")
        void shouldGenerateSQLWithUnderscoreField() {
            NullPredicate predicate = new NullPredicate("first_name", false);
            assertEquals("first_name IS NOT NULL", predicate.toSQL());
        }

        @Test
        @DisplayName("should generate SQL with numeric field names")
        void shouldGenerateSQLWithNumericFieldNames() {
            NullPredicate predicate = new NullPredicate("field123", true);
            assertEquals("field123 IS NULL", predicate.toSQL());
        }

        @Test
        @DisplayName("should generate SQL with complex field names")
        void shouldGenerateSQLWithComplexFieldNames() {
            NullPredicate predicate = new NullPredicate("user_profile.address_line_1", false);
            assertEquals("user_profile.address_line_1 IS NOT NULL", predicate.toSQL());
        }
    }

    @Nested
    @DisplayName("Parameter Mapping")
    class ParameterMappingTests {

        @Test
        @DisplayName("should return empty parameter map for IS NULL")
        void shouldReturnEmptyParameterMapForIsNull() {
            NullPredicate predicate = new NullPredicate("field", true);
            Map<String, Object> params = predicate.getParameters();
            
            assertTrue(params.isEmpty());
            assertEquals(0, params.size());
        }

        @Test
        @DisplayName("should return empty parameter map for IS NOT NULL")
        void shouldReturnEmptyParameterMapForIsNotNull() {
            NullPredicate predicate = new NullPredicate("field", false);
            Map<String, Object> params = predicate.getParameters();
            
            assertTrue(params.isEmpty());
            assertEquals(0, params.size());
        }

        @Test
        @DisplayName("should return immutable parameter map")
        void shouldReturnImmutableParameterMap() {
            NullPredicate predicate = new NullPredicate("field", true);
            Map<String, Object> params = predicate.getParameters();
            
            assertThrows(UnsupportedOperationException.class, 
                () -> params.put("key", "value"));
        }

        @Test
        @DisplayName("should return consistent empty map")
        void shouldReturnConsistentEmptyMap() {
            NullPredicate predicate = new NullPredicate("field", true);
            
            Map<String, Object> params1 = predicate.getParameters();
            Map<String, Object> params2 = predicate.getParameters();
            
            assertEquals(params1, params2);
            assertTrue(params1.isEmpty());
            assertTrue(params2.isEmpty());
        }
    }

    @Nested
    @DisplayName("Immutability")
    class ImmutabilityTests {

        @Test
        @DisplayName("should be immutable - fields cannot be modified")
        void shouldBeImmutable() {
            NullPredicate predicate = new NullPredicate("field", true);
            
            // Verify getters return the same values
            assertEquals("field", predicate.getField());
            assertTrue(predicate.isNull());
            
            // Create another predicate with same values
            NullPredicate predicate2 = new NullPredicate("field", true);
            
            // Should be equal but not same instance
            assertEquals(predicate, predicate2);
            assertNotSame(predicate, predicate2);
        }

        @Test
        @DisplayName("should maintain consistent SQL and parameters")
        void shouldMaintainConsistentOutput() {
            NullPredicate predicate = new NullPredicate("field", true);
            
            String sql1 = predicate.toSQL();
            String sql2 = predicate.toSQL();
            Map<String, Object> params1 = predicate.getParameters();
            Map<String, Object> params2 = predicate.getParameters();
            
            assertEquals(sql1, sql2);
            assertEquals(params1, params2);
            assertEquals("field IS NULL", sql1);
            assertTrue(params1.isEmpty());
        }

        @Test
        @DisplayName("should create independent instances")
        void shouldCreateIndependentInstances() {
            NullPredicate predicate1 = new NullPredicate("field", true);
            NullPredicate predicate2 = new NullPredicate("field", true);
            
            assertEquals(predicate1, predicate2);
            assertNotSame(predicate1, predicate2);
            assertEquals(predicate1.getField(), predicate2.getField());
            assertEquals(predicate1.isNull(), predicate2.isNull());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("should be equal when all fields are equal - IS NULL")
        void shouldBeEqualWhenFieldsEqualIsNull() {
            NullPredicate predicate1 = new NullPredicate("field", true);
            NullPredicate predicate2 = new NullPredicate("field", true);
            
            assertEquals(predicate1, predicate2);
            assertEquals(predicate1.hashCode(), predicate2.hashCode());
        }

        @Test
        @DisplayName("should be equal when all fields are equal - IS NOT NULL")
        void shouldBeEqualWhenFieldsEqualIsNotNull() {
            NullPredicate predicate1 = new NullPredicate("field", false);
            NullPredicate predicate2 = new NullPredicate("field", false);
            
            assertEquals(predicate1, predicate2);
            assertEquals(predicate1.hashCode(), predicate2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when field differs")
        void shouldNotBeEqualWhenFieldDiffers() {
            NullPredicate predicate1 = new NullPredicate("field1", true);
            NullPredicate predicate2 = new NullPredicate("field2", true);
            
            assertNotEquals(predicate1, predicate2);
        }

        @Test
        @DisplayName("should not be equal when isNull flag differs")
        void shouldNotBeEqualWhenIsNullFlagDiffers() {
            NullPredicate predicate1 = new NullPredicate("field", true);
            NullPredicate predicate2 = new NullPredicate("field", false);
            
            assertNotEquals(predicate1, predicate2);
        }

        @Test
        @DisplayName("should not be equal to null")
        void shouldNotBeEqualToNull() {
            NullPredicate predicate = new NullPredicate("field", true);
            assertNotEquals(predicate, null);
        }

        @Test
        @DisplayName("should not be equal to different class")
        void shouldNotBeEqualToDifferentClass() {
            NullPredicate predicate = new NullPredicate("field", true);
            SimplePredicate simplePredicate = new SimplePredicate("field", "=", null, "p1");
            assertNotEquals(predicate, simplePredicate);
        }

        @Test
        @DisplayName("should be equal to itself")
        void shouldBeEqualToItself() {
            NullPredicate predicate = new NullPredicate("field", true);
            assertEquals(predicate, predicate);
            assertEquals(predicate.hashCode(), predicate.hashCode());
        }

        @Test
        @DisplayName("should provide meaningful toString representation for IS NULL")
        void shouldProvideMeaningfulToStringForIsNull() {
            NullPredicate predicate = new NullPredicate("status", true);
            String toString = predicate.toString();
            
            assertNotNull(toString);
            assertTrue(toString.contains("NullPredicate"));
            assertTrue(toString.contains("status"));
            assertTrue(toString.contains("true"));
        }

        @Test
        @DisplayName("should provide meaningful toString representation for IS NOT NULL")
        void shouldProvideMeaningfulToStringForIsNotNull() {
            NullPredicate predicate = new NullPredicate("name", false);
            String toString = predicate.toString();
            
            assertNotNull(toString);
            assertTrue(toString.contains("NullPredicate"));
            assertTrue(toString.contains("name"));
            assertTrue(toString.contains("false"));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle field with single character")
        void shouldHandleFieldWithSingleCharacter() {
            NullPredicate predicate = new NullPredicate("x", true);
            assertEquals("x IS NULL", predicate.toSQL());
        }

        @Test
        @DisplayName("should handle field with maximum allowed characters")
        void shouldHandleFieldWithMaxCharacters() {
            // Java 8 compatible alternative to String.repeat()
            StringBuilder sb = new StringBuilder();
            sb.append("field_");
            for (int i = 0; i < 100; i++) {
                sb.append("a");
            }
            String longField = sb.toString();
            NullPredicate predicate = new NullPredicate(longField, false);
            assertEquals(longField + " IS NOT NULL", predicate.toSQL());
        }

        @Test
        @DisplayName("should handle field with numbers and mixed case")
        void shouldHandleFieldWithNumbersAndMixedCase() {
            NullPredicate predicate = new NullPredicate("Field123_Test", true);
            assertEquals("Field123_Test IS NULL", predicate.toSQL());
        }

        @Test
        @DisplayName("should handle repeated calls consistently")
        void shouldHandleRepeatedCallsConsistently() {
            NullPredicate predicate = new NullPredicate("test_field", false);
            
            for (int i = 0; i < 100; i++) {
                assertEquals("test_field IS NOT NULL", predicate.toSQL());
                assertTrue(predicate.getParameters().isEmpty());
                assertFalse(predicate.isNull());
                assertEquals("test_field", predicate.getField());
            }
        }

        @Test
        @DisplayName("should maintain contract across different boolean values")
        void shouldMaintainContractAcrossBooleanValues() {
            NullPredicate isNullPredicate = new NullPredicate("field", true);
            NullPredicate isNotNullPredicate = new NullPredicate("field", false);
            
            assertEquals("field IS NULL", isNullPredicate.toSQL());
            assertEquals("field IS NOT NULL", isNotNullPredicate.toSQL());
            
            assertTrue(isNullPredicate.isNull());
            assertFalse(isNotNullPredicate.isNull());
            
            assertTrue(isNullPredicate.getParameters().isEmpty());
            assertTrue(isNotNullPredicate.getParameters().isEmpty());
        }
    }
}