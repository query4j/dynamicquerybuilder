package com.github.query4j.core.criteria;

import com.github.query4j.core.QueryBuildException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for field name validation across all predicate types.
 * Verifies that all predicates properly validate field names according to the
 * pattern [A-Za-z0-9_\.]+
 */
@DisplayName("Field Validation")
class FieldValidationTest {

    @Nested
    @DisplayName("Valid Field Names")
    class ValidFieldNamesTests {

        @Test
        @DisplayName("should accept simple alphanumeric field names")
        void shouldAcceptSimpleAlphanumericFieldNames() {
            assertDoesNotThrow(() -> new SimplePredicate("field123", "=", "value", "p1"));
            assertDoesNotThrow(() -> new NullPredicate("field123", true));
            assertDoesNotThrow(() -> new LikePredicate("field123", "%pattern%", "p1"));
            assertDoesNotThrow(() -> new InPredicate("field123", Arrays.asList("A", "B"), "p1"));
            assertDoesNotThrow(() -> new BetweenPredicate("field123", 1, 10, "p1", "p2"));
        }

        @Test
        @DisplayName("should accept field names with underscores")
        void shouldAcceptFieldNamesWithUnderscores() {
            assertDoesNotThrow(() -> new SimplePredicate("first_name", "=", "John", "p1"));
            assertDoesNotThrow(() -> new NullPredicate("last_name", false));
            assertDoesNotThrow(() -> new LikePredicate("user_email", "%@example.com", "p1"));
        }

        @Test
        @DisplayName("should accept field names with dots (qualified names)")
        void shouldAcceptFieldNamesWithDots() {
            assertDoesNotThrow(() -> new SimplePredicate("user.email", "=", "test@example.com", "p1"));
            assertDoesNotThrow(() -> new NullPredicate("order.createdAt", true));
            assertDoesNotThrow(() -> new LikePredicate("product.name", "%widget%", "p1"));
        }

        @Test
        @DisplayName("should accept mixed valid characters")
        void shouldAcceptMixedValidCharacters() {
            String validField = "User123.profile_data";
            assertDoesNotThrow(() -> new SimplePredicate(validField, "=", "value", "p1"));
            assertDoesNotThrow(() -> new NullPredicate(validField, false));
        }
    }

    @Nested
    @DisplayName("Invalid Field Names")
    class InvalidFieldNamesTests {

        @Test
        @DisplayName("should reject null field names")
        void shouldRejectNullFieldNames() {
            assertThrows(QueryBuildException.class, () -> new SimplePredicate(null, "=", "value", "p1"));
            assertThrows(QueryBuildException.class, () -> new NullPredicate(null, true));
            assertThrows(QueryBuildException.class, () -> new LikePredicate(null, "pattern", "p1"));
            assertThrows(QueryBuildException.class, () -> new InPredicate(null, Arrays.asList("A"), "p1"));
            assertThrows(QueryBuildException.class, () -> new BetweenPredicate(null, 1, 10, "p1", "p2"));
        }

        @Test
        @DisplayName("should reject empty field names")
        void shouldRejectEmptyFieldNames() {
            assertThrows(QueryBuildException.class, () -> new SimplePredicate("", "=", "value", "p1"));
            assertThrows(QueryBuildException.class, () -> new NullPredicate("", true));
            assertThrows(QueryBuildException.class, () -> new LikePredicate("", "pattern", "p1"));
            assertThrows(QueryBuildException.class, () -> new InPredicate("", Arrays.asList("A"), "p1"));
            assertThrows(QueryBuildException.class, () -> new BetweenPredicate("", 1, 10, "p1", "p2"));
        }

        @Test
        @DisplayName("should reject whitespace-only field names")
        void shouldRejectWhitespaceOnlyFieldNames() {
            assertThrows(QueryBuildException.class, () -> new SimplePredicate("   ", "=", "value", "p1"));
            assertThrows(QueryBuildException.class, () -> new NullPredicate("\t\n", true));
            assertThrows(QueryBuildException.class, () -> new LikePredicate("  ", "pattern", "p1"));
        }

        @Test
        @DisplayName("should reject field names with special characters")
        void shouldRejectFieldNamesWithSpecialCharacters() {
            String[] invalidFields = {
                "field-name",     // hyphen
                "field@domain",   // at symbol
                "field name",     // space
                "field+extra",    // plus
                "field*wild",     // asterisk
                "field$var",      // dollar
                "field#hash",     // hash
                "field!bang",     // exclamation
                "field?query",    // question mark
                "field|pipe",     // pipe
                "field&and",      // ampersand
                "field%percent",  // percent
                "field^hat",      // caret
                "field(paren",    // parenthesis
                "field[bracket",  // bracket
                "field{brace",    // brace
                "field\"quote",   // quote
                "field'apostrophe", // apostrophe
                "field;semicolon", // semicolon
                "field:colon",    // colon
                "field,comma",    // comma
                "field<less",     // less than
                "field>greater",  // greater than
                "field=equals",   // equals
                "field\\backslash", // backslash
                "field/slash",    // forward slash
                "field~tilde"     // tilde
            };

            for (String invalidField : invalidFields) {
                QueryBuildException ex = assertThrows(QueryBuildException.class, 
                    () -> new SimplePredicate(invalidField, "=", "value", "p1"),
                    "Field name should be rejected: " + invalidField);
                
                assertTrue(ex.getMessage().contains("invalid characters"), 
                    "Exception message should mention invalid characters for field: " + invalidField);
            }
        }

        @Test
        @DisplayName("should reject field names starting with numbers followed by invalid chars")
        void shouldRejectFieldNamesStartingWithNumbersFollowedByInvalidChars() {
            assertThrows(QueryBuildException.class, () -> new SimplePredicate("123field-name", "=", "value", "p1"));
            assertThrows(QueryBuildException.class, () -> new SimplePredicate("456field@domain", "=", "value", "p1"));
        }
    }

    @Nested
    @DisplayName("Parameter Validation")
    class ParameterValidationTests {

        @Test
        @DisplayName("should reject empty parameter names")
        void shouldRejectEmptyParameterNames() {
            assertThrows(QueryBuildException.class, () -> new SimplePredicate("field", "=", "value", ""));
            assertThrows(QueryBuildException.class, () -> new SimplePredicate("field", "=", "value", "   "));
            assertThrows(QueryBuildException.class, () -> new LikePredicate("field", "pattern", ""));
            assertThrows(QueryBuildException.class, () -> new InPredicate("field", Arrays.asList("A"), ""));
            assertThrows(QueryBuildException.class, () -> new BetweenPredicate("field", 1, 10, "", "p2"));
            assertThrows(QueryBuildException.class, () -> new BetweenPredicate("field", 1, 10, "p1", ""));
        }

        @Test
        @DisplayName("should reject empty operators")
        void shouldRejectEmptyOperators() {
            assertThrows(QueryBuildException.class, () -> new SimplePredicate("field", "", "value", "p1"));
            assertThrows(QueryBuildException.class, () -> new SimplePredicate("field", "   ", "value", "p1"));
        }
    }

    @Nested
    @DisplayName("Error Messages")
    class ErrorMessageTests {

        @Test
        @DisplayName("should provide meaningful error messages for invalid field names")
        void shouldProvideMeaningfulErrorMessagesForInvalidFieldNames() {
            QueryBuildException ex = assertThrows(QueryBuildException.class, 
                () -> new SimplePredicate("invalid-field", "=", "value", "p1"));
            
            assertTrue(ex.getMessage().contains("invalid characters"));
            assertTrue(ex.getMessage().contains("invalid-field"));
            assertTrue(ex.getMessage().contains("[A-Za-z0-9_\\.]"));
        }

        @Test
        @DisplayName("should provide meaningful error messages for null field names")
        void shouldProvideMeaningfulErrorMessagesForNullFieldNames() {
            QueryBuildException ex = assertThrows(QueryBuildException.class, 
                () -> new SimplePredicate(null, "=", "value", "p1"));
            
            assertTrue(ex.getMessage().contains("must not be null"));
        }

        @Test
        @DisplayName("should provide meaningful error messages for empty field names")
        void shouldProvideMeaningfulErrorMessagesForEmptyFieldNames() {
            QueryBuildException ex = assertThrows(QueryBuildException.class, 
                () -> new SimplePredicate("", "=", "value", "p1"));
            
            assertTrue(ex.getMessage().contains("must not be empty"));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("should handle field names with leading and trailing whitespace")
        void shouldHandleFieldNamesWithLeadingAndTrailingWhitespace() {
            // Valid field names should be trimmed and accepted
            SimplePredicate predicate = new SimplePredicate("  validField  ", "=", "value", "p1");
            assertEquals("validField", predicate.getField());
        }

        @Test
        @DisplayName("should validate field names in LogicalPredicate children")
        void shouldValidateFieldNamesInLogicalPredicateChildren() {
            // This should work with valid field names
            SimplePredicate validPredicate = new SimplePredicate("validField", "=", "value", "p1");
            assertDoesNotThrow(() -> new LogicalPredicate("AND", Arrays.asList(validPredicate)));
            
            // Invalid field names in children should still be caught during child construction
            assertThrows(QueryBuildException.class, () -> {
                SimplePredicate invalidPredicate = new SimplePredicate("invalid-field", "=", "value", "p1");
            });
        }
    }

    @Nested
    @DisplayName("Validator Static Method Coverage")
    class ValidatorStaticMethodCoverage {

        @Test
        @DisplayName("should verify FieldValidator static method functionality")
        void shouldVerifyFieldValidatorStaticMethodFunctionality() {
            // Test validateFieldName static method
            assertDoesNotThrow(() -> FieldValidator.validateFieldName("valid_field"));
            assertThrows(QueryBuildException.class, () -> FieldValidator.validateFieldName("invalid@field"));

            // Test validateParameterName static method
            assertDoesNotThrow(() -> FieldValidator.validateParameterName("valid_param"));
            assertThrows(QueryBuildException.class, () -> FieldValidator.validateParameterName("invalid@param"));
        }

        @Test
        @DisplayName("should verify OperatorValidator static method functionality")
        void shouldVerifyOperatorValidatorStaticMethodFunctionality() {
            // Test validateOperator static method
            assertDoesNotThrow(() -> OperatorValidator.validateOperator("="));
            assertDoesNotThrow(() -> OperatorValidator.validateOperator("LIKE"));
            assertThrows(QueryBuildException.class, () -> OperatorValidator.validateOperator("INVALID_OP"));
        }

        @Test
        @DisplayName("should test additional FieldValidator edge cases")
        void shouldTestAdditionalFieldValidatorEdgeCases() {
            // Test empty field name
            assertThrows(QueryBuildException.class, () -> FieldValidator.validateFieldName(""));
            
            // Test null field name
            assertThrows(QueryBuildException.class, () -> FieldValidator.validateFieldName(null));
            
            // Test valid complex field names
            assertDoesNotThrow(() -> FieldValidator.validateFieldName("user.profile.firstName"));
            assertDoesNotThrow(() -> FieldValidator.validateFieldName("table123.column_name"));
        }

        @Test
        @DisplayName("should test additional OperatorValidator edge cases")
        void shouldTestAdditionalOperatorValidatorEdgeCases() {
            // Test all valid operators
            assertDoesNotThrow(() -> OperatorValidator.validateOperator("="));
            assertDoesNotThrow(() -> OperatorValidator.validateOperator("!="));
            assertDoesNotThrow(() -> OperatorValidator.validateOperator("<>"));
            assertDoesNotThrow(() -> OperatorValidator.validateOperator("<"));
            assertDoesNotThrow(() -> OperatorValidator.validateOperator(">"));
            assertDoesNotThrow(() -> OperatorValidator.validateOperator("<="));
            assertDoesNotThrow(() -> OperatorValidator.validateOperator(">="));
            assertDoesNotThrow(() -> OperatorValidator.validateOperator("LIKE"));
            assertDoesNotThrow(() -> OperatorValidator.validateOperator("NOT LIKE"));
            assertDoesNotThrow(() -> OperatorValidator.validateOperator("IN"));
            assertDoesNotThrow(() -> OperatorValidator.validateOperator("NOT IN"));
            assertDoesNotThrow(() -> OperatorValidator.validateOperator("IS"));
            assertDoesNotThrow(() -> OperatorValidator.validateOperator("IS NOT"));
            assertDoesNotThrow(() -> OperatorValidator.validateOperator("EXISTS"));
            assertDoesNotThrow(() -> OperatorValidator.validateOperator("NOT EXISTS"));
            assertDoesNotThrow(() -> OperatorValidator.validateOperator("BETWEEN"));
            
            // Test invalid operators
            assertThrows(QueryBuildException.class, () -> OperatorValidator.validateOperator("INVALID"));
            assertThrows(QueryBuildException.class, () -> OperatorValidator.validateOperator("==="));
            assertThrows(QueryBuildException.class, () -> OperatorValidator.validateOperator(""));
            assertThrows(QueryBuildException.class, () -> OperatorValidator.validateOperator(null));
        }
    }
}