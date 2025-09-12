package com.github.query4j.core.criteria;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import com.github.query4j.core.QueryBuildException;

/**
 * Comprehensive unit tests for LikePredicate class.
 * Tests SQL generation, parameter mapping, immutability, and validation.
 */
@DisplayName("LikePredicate")
class LikePredicateTest {

    @Nested
    @DisplayName("Constructor and Basic Properties")
    class ConstructorTests {

        @Test
        @DisplayName("should create valid predicate with required parameters")
        void shouldCreateValidPredicate() {
            LikePredicate predicate = new LikePredicate("name", "%John%", "p1");
            
            assertEquals("name", predicate.getField());
            assertEquals("%John%", predicate.getPattern());
            assertEquals("p1", predicate.getParamName());
        }

        @Test
        @DisplayName("should throw NullPointerException for null field")
        void shouldThrowForNullField() {
            assertThrows(QueryBuildException.class, 
                () -> new LikePredicate(null, "%pattern%", "p1"));
        }

        @Test
        @DisplayName("should throw NullPointerException for null pattern")
        void shouldThrowForNullPattern() {
            assertThrows(QueryBuildException.class,
                () -> new LikePredicate("field", null, "p1"));
        }

        @Test
        @DisplayName("should throw NullPointerException for null paramName")
        void shouldThrowForNullParamName() {
            assertThrows(QueryBuildException.class,
                () -> new LikePredicate("field", "%pattern%", null));
        }

        @Test
        @DisplayName("should accept empty string pattern")
        void shouldAcceptEmptyStringPattern() {
            LikePredicate predicate = new LikePredicate("field", "", "p1");
            assertEquals("", predicate.getPattern());
        }

        @Test
        @DisplayName("should accept pattern without wildcards")
        void shouldAcceptPatternWithoutWildcards() {
            LikePredicate predicate = new LikePredicate("field", "exact", "p1");
            assertEquals("exact", predicate.getPattern());
        }
    }

    @Nested
    @DisplayName("SQL Generation")
    class SqlGenerationTests {

        @Test
        @DisplayName("should generate correct SQL for basic LIKE pattern")
        void shouldGenerateBasicLikeSQL() {
            LikePredicate predicate = new LikePredicate("name", "%John%", "p1");
            assertEquals("name LIKE :p1", predicate.toSQL());
        }

        @Test
        @DisplayName("should generate SQL for prefix pattern")
        void shouldGeneratePrefixPatternSQL() {
            LikePredicate predicate = new LikePredicate("name", "John%", "p1");
            assertEquals("name LIKE :p1", predicate.toSQL());
        }

        @Test
        @DisplayName("should generate SQL for suffix pattern")
        void shouldGenerateSuffixPatternSQL() {
            LikePredicate predicate = new LikePredicate("name", "%John", "p1");
            assertEquals("name LIKE :p1", predicate.toSQL());
        }

        @Test
        @DisplayName("should generate SQL for single character wildcard")
        void shouldGenerateSingleCharWildcardSQL() {
            LikePredicate predicate = new LikePredicate("code", "A_C", "p1");
            assertEquals("code LIKE :p1", predicate.toSQL());
        }

        @Test
        @DisplayName("should generate SQL with field containing dots")
        void shouldGenerateSQLWithDottedField() {
            LikePredicate predicate = new LikePredicate("user.name", "%John%", "p1");
            assertEquals("user.name LIKE :p1", predicate.toSQL());
        }

        @Test
        @DisplayName("should generate SQL with field containing underscores")
        void shouldGenerateSQLWithUnderscoreField() {
            LikePredicate predicate = new LikePredicate("first_name", "%John%", "p1");
            assertEquals("first_name LIKE :p1", predicate.toSQL());
        }

        @Test
        @DisplayName("should generate SQL with complex parameter names")
        void shouldGenerateSQLWithComplexParamNames() {
            LikePredicate predicate = new LikePredicate("field", "%pattern%", "complex_param_name_123");
            assertEquals("field LIKE :complex_param_name_123", predicate.toSQL());
        }

        @Test
        @DisplayName("should generate SQL for exact match pattern")
        void shouldGenerateSQLForExactMatch() {
            LikePredicate predicate = new LikePredicate("name", "John", "p1");
            assertEquals("name LIKE :p1", predicate.toSQL());
        }
    }

    @Nested
    @DisplayName("Parameter Mapping")
    class ParameterMappingTests {

        @Test
        @DisplayName("should return correct single parameter mapping")
        void shouldReturnCorrectParameterMapping() {
            LikePredicate predicate = new LikePredicate("name", "%John%", "p1");
            Map<String, Object> params = predicate.getParameters();
            
            assertEquals(1, params.size());
            assertEquals("%John%", params.get("p1"));
        }

        @Test
        @DisplayName("should return parameter mapping with various patterns")
        void shouldReturnParameterMappingWithVariousPatterns() {
            // Prefix pattern
            LikePredicate prefixPredicate = new LikePredicate("name", "John%", "p1");
            assertEquals("John%", prefixPredicate.getParameters().get("p1"));
            
            // Suffix pattern
            LikePredicate suffixPredicate = new LikePredicate("name", "%John", "p2");
            assertEquals("%John", suffixPredicate.getParameters().get("p2"));
            
            // Contains pattern
            LikePredicate containsPredicate = new LikePredicate("name", "%John%", "p3");
            assertEquals("%John%", containsPredicate.getParameters().get("p3"));
            
            // Single char wildcard
            LikePredicate singleCharPredicate = new LikePredicate("code", "A_C", "p4");
            assertEquals("A_C", singleCharPredicate.getParameters().get("p4"));
        }

        @Test
        @DisplayName("should return parameter mapping with empty pattern")
        void shouldReturnParameterMappingWithEmptyPattern() {
            LikePredicate predicate = new LikePredicate("field", "", "p1");
            Map<String, Object> params = predicate.getParameters();
            
            assertEquals(1, params.size());
            assertEquals("", params.get("p1"));
        }

        @Test
        @DisplayName("should return parameter mapping with special characters")
        void shouldReturnParameterMappingWithSpecialChars() {
            String specialPattern = "%O'Malley & Co.%";
            LikePredicate predicate = new LikePredicate("company", specialPattern, "p1");
            Map<String, Object> params = predicate.getParameters();
            
            assertEquals(1, params.size());
            assertEquals(specialPattern, params.get("p1"));
        }

        @Test
        @DisplayName("should return immutable parameter map")
        void shouldReturnImmutableParameterMap() {
            LikePredicate predicate = new LikePredicate("name", "%John%", "p1");
            Map<String, Object> params = predicate.getParameters();
            
            assertThrows(UnsupportedOperationException.class, 
                () -> params.put("p2", "value"));
            assertThrows(UnsupportedOperationException.class,
                () -> params.remove("p1"));
        }

        @Test
        @DisplayName("should return consistent parameter mapping")
        void shouldReturnConsistentParameterMapping() {
            LikePredicate predicate = new LikePredicate("name", "%John%", "p1");
            
            Map<String, Object> params1 = predicate.getParameters();
            Map<String, Object> params2 = predicate.getParameters();
            
            assertEquals(params1, params2);
            assertEquals(params1.get("p1"), params2.get("p1"));
        }
    }

    @Nested
    @DisplayName("Immutability")
    class ImmutabilityTests {

        @Test
        @DisplayName("should be immutable - fields cannot be modified")
        void shouldBeImmutable() {
            LikePredicate predicate = new LikePredicate("name", "%John%", "p1");
            
            // Verify all getters return the same values
            assertEquals("name", predicate.getField());
            assertEquals("%John%", predicate.getPattern());
            assertEquals("p1", predicate.getParamName());
            
            // Create another predicate with same values
            LikePredicate predicate2 = new LikePredicate("name", "%John%", "p1");
            
            // Should be equal but not same instance
            assertEquals(predicate, predicate2);
            assertNotSame(predicate, predicate2);
        }

        @Test
        @DisplayName("should maintain consistent SQL and parameters")
        void shouldMaintainConsistentOutput() {
            LikePredicate predicate = new LikePredicate("name", "%John%", "p1");
            
            String sql1 = predicate.toSQL();
            String sql2 = predicate.toSQL();
            Map<String, Object> params1 = predicate.getParameters();
            Map<String, Object> params2 = predicate.getParameters();
            
            assertEquals(sql1, sql2);
            assertEquals(params1, params2);
            assertEquals("name LIKE :p1", sql1);
            assertEquals("%John%", params1.get("p1"));
        }

        @Test
        @DisplayName("should create independent instances")
        void shouldCreateIndependentInstances() {
            LikePredicate predicate1 = new LikePredicate("name", "%John%", "p1");
            LikePredicate predicate2 = new LikePredicate("name", "%John%", "p1");
            
            assertEquals(predicate1, predicate2);
            assertNotSame(predicate1, predicate2);
            assertEquals(predicate1.getField(), predicate2.getField());
            assertEquals(predicate1.getPattern(), predicate2.getPattern());
            assertEquals(predicate1.getParamName(), predicate2.getParamName());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("should be equal when all fields are equal")
        void shouldBeEqualWhenFieldsEqual() {
            LikePredicate predicate1 = new LikePredicate("name", "%John%", "p1");
            LikePredicate predicate2 = new LikePredicate("name", "%John%", "p1");
            
            assertEquals(predicate1, predicate2);
            assertEquals(predicate1.hashCode(), predicate2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when field differs")
        void shouldNotBeEqualWhenFieldDiffers() {
            LikePredicate predicate1 = new LikePredicate("name", "%John%", "p1");
            LikePredicate predicate2 = new LikePredicate("email", "%John%", "p1");
            
            assertNotEquals(predicate1, predicate2);
        }

        @Test
        @DisplayName("should not be equal when pattern differs")
        void shouldNotBeEqualWhenPatternDiffers() {
            LikePredicate predicate1 = new LikePredicate("name", "%John%", "p1");
            LikePredicate predicate2 = new LikePredicate("name", "%Jane%", "p1");
            
            assertNotEquals(predicate1, predicate2);
        }

        @Test
        @DisplayName("should not be equal when paramName differs")
        void shouldNotBeEqualWhenParamNameDiffers() {
            LikePredicate predicate1 = new LikePredicate("name", "%John%", "p1");
            LikePredicate predicate2 = new LikePredicate("name", "%John%", "p2");
            
            assertNotEquals(predicate1, predicate2);
        }

        @Test
        @DisplayName("should not be equal to null")
        void shouldNotBeEqualToNull() {
            LikePredicate predicate = new LikePredicate("name", "%John%", "p1");
            assertNotEquals(predicate, null);
        }

        @Test
        @DisplayName("should not be equal to different class")
        void shouldNotBeEqualToDifferentClass() {
            LikePredicate predicate = new LikePredicate("name", "%John%", "p1");
            SimplePredicate simplePredicate = new SimplePredicate("name", "LIKE", "%John%", "p1");
            assertNotEquals(predicate, simplePredicate);
        }

        @Test
        @DisplayName("should be equal to itself")
        void shouldBeEqualToItself() {
            LikePredicate predicate = new LikePredicate("name", "%John%", "p1");
            assertEquals(predicate, predicate);
            assertEquals(predicate.hashCode(), predicate.hashCode());
        }

        @Test
        @DisplayName("should provide meaningful toString representation")
        void shouldProvideMeaningfulToString() {
            LikePredicate predicate = new LikePredicate("name", "%John%", "p1");
            String toString = predicate.toString();
            
            assertNotNull(toString);
            assertTrue(toString.contains("LikePredicate"));
            assertTrue(toString.contains("name"));
            assertTrue(toString.contains("%John%"));
            assertTrue(toString.contains("p1"));
        }

        @Test
        @DisplayName("should handle special characters in toString")
        void shouldHandleSpecialCharactersInToString() {
            LikePredicate predicate = new LikePredicate("field", "%O'Malley%", "p1");
            String toString = predicate.toString();
            
            assertNotNull(toString);
            assertTrue(toString.contains("LikePredicate"));
            assertTrue(toString.contains("field"));
            assertTrue(toString.contains("%O'Malley%"));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle patterns with only wildcards")
        void shouldHandlePatternsWithOnlyWildcards() {
            LikePredicate predicate1 = new LikePredicate("field", "%", "p1");
            assertEquals("field LIKE :p1", predicate1.toSQL());
            assertEquals("%", predicate1.getParameters().get("p1"));
            
            LikePredicate predicate2 = new LikePredicate("field", "_", "p2");
            assertEquals("field LIKE :p2", predicate2.toSQL());
            assertEquals("_", predicate2.getParameters().get("p2"));
        }

        @Test
        @DisplayName("should handle patterns with multiple wildcards")
        void shouldHandlePatternsWithMultipleWildcards() {
            LikePredicate predicate = new LikePredicate("field", "%test_%pattern%", "p1");
            assertEquals("field LIKE :p1", predicate.toSQL());
            assertEquals("%test_%pattern%", predicate.getParameters().get("p1"));
        }

        @Test
        @DisplayName("should handle patterns with special SQL characters")
        void shouldHandlePatternsWithSpecialSQLChars() {
            String specialPattern = "%'test\"[pattern]%";
            LikePredicate predicate = new LikePredicate("field", specialPattern, "p1");
            
            assertEquals("field LIKE :p1", predicate.toSQL());
            assertEquals(specialPattern, predicate.getParameters().get("p1"));
        }

        @Test
        @DisplayName("should handle very long patterns")
        void shouldHandleVeryLongPatterns() {
            // Java 8 compatible alternative to String.repeat()
            StringBuilder sb = new StringBuilder();
            sb.append("%");
            for (int i = 0; i < 10000; i++) {
                sb.append("a");
            }
            sb.append("%");
            String longPattern = sb.toString();
            LikePredicate predicate = new LikePredicate("field", longPattern, "p1");
            
            assertEquals("field LIKE :p1", predicate.toSQL());
            assertEquals(longPattern, predicate.getParameters().get("p1"));
        }

        @Test
        @DisplayName("should handle case sensitivity in patterns")
        void shouldHandleCaseSensitivityInPatterns() {
            LikePredicate upperPredicate = new LikePredicate("name", "%JOHN%", "p1");
            LikePredicate lowerPredicate = new LikePredicate("name", "%john%", "p2");
            LikePredicate mixedPredicate = new LikePredicate("name", "%JoHn%", "p3");
            
            assertEquals("name LIKE :p1", upperPredicate.toSQL());
            assertEquals("name LIKE :p2", lowerPredicate.toSQL());
            assertEquals("name LIKE :p3", mixedPredicate.toSQL());
            
            assertEquals("%JOHN%", upperPredicate.getParameters().get("p1"));
            assertEquals("%john%", lowerPredicate.getParameters().get("p2"));
            assertEquals("%JoHn%", mixedPredicate.getParameters().get("p3"));
        }

        @Test
        @DisplayName("should handle unicode characters in patterns")
        void shouldHandleUnicodeCharactersInPatterns() {
            String unicodePattern = "%José_Müller%";
            LikePredicate predicate = new LikePredicate("name", unicodePattern, "p1");
            
            assertEquals("name LIKE :p1", predicate.toSQL());
            assertEquals(unicodePattern, predicate.getParameters().get("p1"));
        }

        @Test
        @DisplayName("should maintain consistency across repeated calls")
        void shouldMaintainConsistencyAcrossRepeatedCalls() {
            LikePredicate predicate = new LikePredicate("test_field", "%pattern%", "p1");
            
            for (int i = 0; i < 100; i++) {
                assertEquals("test_field LIKE :p1", predicate.toSQL());
                assertEquals("%pattern%", predicate.getParameters().get("p1"));
                assertEquals("test_field", predicate.getField());
                assertEquals("%pattern%", predicate.getPattern());
                assertEquals("p1", predicate.getParamName());
            }
        }
    }
}