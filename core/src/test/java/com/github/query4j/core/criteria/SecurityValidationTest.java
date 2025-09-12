package com.github.query4j.core.criteria;

import com.github.query4j.core.QueryBuildException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Security-focused validation tests to ensure robust input validation 
 * and protection against injection attacks through malformed inputs.
 *
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
@DisplayName("Security Validation Tests")
class SecurityValidationTest {

    @Nested
    @DisplayName("Parameter Name Injection Tests")
    class ParameterNameInjectionTests {

        @Test
        @DisplayName("should reject parameter names starting with numbers")
        void shouldRejectParameterNamesStartingWithNumbers() {
            assertThrows(QueryBuildException.class, () -> 
                new SimplePredicate("field", "=", "value", "123param"));
            
            assertThrows(QueryBuildException.class, () -> 
                new SimplePredicate("field", "=", "value", "0"));
            
            assertThrows(QueryBuildException.class, () -> 
                new SimplePredicate("field", "=", "value", "9abc"));
        }

        @Test
        @DisplayName("should reject parameter names starting with special characters")
        void shouldRejectParameterNamesStartingWithSpecialCharacters() {
            assertThrows(QueryBuildException.class, () -> 
                new SimplePredicate("field", "=", "value", "_param"));
            
            assertThrows(QueryBuildException.class, () -> 
                new SimplePredicate("field", "=", "value", "$param"));
            
            assertThrows(QueryBuildException.class, () -> 
                new SimplePredicate("field", "=", "value", "@param"));
            
            assertThrows(QueryBuildException.class, () -> 
                new SimplePredicate("field", "=", "value", "-param"));
        }

        @Test
        @DisplayName("should reject parameter names with invalid characters")
        void shouldRejectParameterNamesWithInvalidCharacters() {
            assertThrows(QueryBuildException.class, () -> 
                new SimplePredicate("field", "=", "value", "param-name"));
            
            assertThrows(QueryBuildException.class, () -> 
                new SimplePredicate("field", "=", "value", "param@name"));
            
            assertThrows(QueryBuildException.class, () -> 
                new SimplePredicate("field", "=", "value", "param name"));
            
            assertThrows(QueryBuildException.class, () -> 
                new SimplePredicate("field", "=", "value", "param.name"));
            
            assertThrows(QueryBuildException.class, () -> 
                new SimplePredicate("field", "=", "value", "param$name"));
        }

        @Test
        @DisplayName("should reject SQL injection attempts in parameter names")
        void shouldRejectSQLInjectionAttemptsInParameterNames() {
            assertThrows(QueryBuildException.class, () -> 
                new SimplePredicate("field", "=", "value", "param'; DROP TABLE users; --"));
            
            assertThrows(QueryBuildException.class, () -> 
                new SimplePredicate("field", "=", "value", "param OR 1=1"));
            
            assertThrows(QueryBuildException.class, () -> 
                new SimplePredicate("field", "=", "value", "param) UNION SELECT * FROM secrets--"));
        }

        @Test
        @DisplayName("should accept valid parameter names")
        void shouldAcceptValidParameterNames() {
            assertDoesNotThrow(() -> 
                new SimplePredicate("field", "=", "value", "validParam"));
            
            assertDoesNotThrow(() -> 
                new SimplePredicate("field", "=", "value", "param123"));
            
            assertDoesNotThrow(() -> 
                new SimplePredicate("field", "=", "value", "param_name"));
            
            assertDoesNotThrow(() -> 
                new SimplePredicate("field", "=", "value", "P"));
            
            assertDoesNotThrow(() -> 
                new SimplePredicate("field", "=", "value", "a"));
        }
    }

    @Nested
    @DisplayName("Operator Injection Tests")
    class OperatorInjectionTests {

        @Test
        @DisplayName("should reject SQL injection attempts in operators")
        void shouldRejectSQLInjectionAttemptsInOperators() {
            assertThrows(QueryBuildException.class, () -> 
                new SimplePredicate("field", "= OR 1=1 --", "value", "param"));
            
            assertThrows(QueryBuildException.class, () -> 
                new SimplePredicate("field", "; DROP TABLE users; --", "value", "param"));
            
            assertThrows(QueryBuildException.class, () -> 
                new SimplePredicate("field", "= UNION SELECT * FROM secrets", "value", "param"));
        }

        @Test
        @DisplayName("should reject invalid operators")
        void shouldRejectInvalidOperators() {
            assertThrows(QueryBuildException.class, () -> 
                new SimplePredicate("field", "INVALID_OP", "value", "param"));
            
            assertThrows(QueryBuildException.class, () -> 
                new SimplePredicate("field", ">>", "value", "param"));
            
            assertThrows(QueryBuildException.class, () -> 
                new SimplePredicate("field", "~~", "value", "param"));
        }

        @Test
        @DisplayName("should accept valid operators")
        void shouldAcceptValidOperators() {
            assertDoesNotThrow(() -> 
                new SimplePredicate("field", "=", "value", "param"));
            
            assertDoesNotThrow(() -> 
                new SimplePredicate("field", "!=", "value", "param"));
            
            assertDoesNotThrow(() -> 
                new SimplePredicate("field", "<>", "value", "param"));
            
            assertDoesNotThrow(() -> 
                new SimplePredicate("field", "<", "value", "param"));
            
            assertDoesNotThrow(() -> 
                new SimplePredicate("field", "<=", "value", "param"));
            
            assertDoesNotThrow(() -> 
                new SimplePredicate("field", ">", "value", "param"));
            
            assertDoesNotThrow(() -> 
                new SimplePredicate("field", ">=", "value", "param"));
            
            assertDoesNotThrow(() -> 
                new SimplePredicate("field", "LIKE", "value", "param"));
        }
    }

    @Nested
    @DisplayName("Field Name Security Tests")
    class FieldNameSecurityTests {

        @Test
        @DisplayName("should reject field names with potential SQL injection")
        void shouldRejectFieldNamesWithPotentialSQLInjection() {
            assertThrows(QueryBuildException.class, () -> 
                new SimplePredicate("field'; DROP TABLE users; --", "=", "value", "param"));
            
            assertThrows(QueryBuildException.class, () -> 
                new SimplePredicate("field OR 1=1", "=", "value", "param"));
            
            assertThrows(QueryBuildException.class, () -> 
                new SimplePredicate("field) UNION SELECT * FROM secrets--", "=", "value", "param"));
        }

        @Test
        @DisplayName("should reject field names with invalid characters")
        void shouldRejectFieldNamesWithInvalidCharacters() {
            assertThrows(QueryBuildException.class, () -> 
                new SimplePredicate("field-name", "=", "value", "param"));
            
            assertThrows(QueryBuildException.class, () -> 
                new SimplePredicate("field@domain", "=", "value", "param"));
            
            assertThrows(QueryBuildException.class, () -> 
                new SimplePredicate("field name", "=", "value", "param"));
            
            assertThrows(QueryBuildException.class, () -> 
                new SimplePredicate("field$name", "=", "value", "param"));
        }
    }

    @Nested
    @DisplayName("Null Safety Tests")
    class NullSafetyTests {

        @Test
        @DisplayName("should reject null children in LogicalPredicate")
        void shouldRejectNullChildrenInLogicalPredicate() {
            SimplePredicate validPredicate = new SimplePredicate("field", "=", "value", "param");
            
            assertThrows(QueryBuildException.class, () -> 
                new LogicalPredicate("AND", Arrays.asList(validPredicate, null)));
            
            assertThrows(QueryBuildException.class, () -> 
                new LogicalPredicate("OR", Arrays.asList(null, validPredicate)));
            
            assertThrows(QueryBuildException.class, () -> 
                new LogicalPredicate("NOT", Collections.singletonList(null)));
        }

        @Test
        @DisplayName("should accept non-null children in LogicalPredicate")
        void shouldAcceptNonNullChildrenInLogicalPredicate() {
            SimplePredicate pred1 = new SimplePredicate("field1", "=", "value1", "param1");
            SimplePredicate pred2 = new SimplePredicate("field2", "=", "value2", "param2");
            
            assertDoesNotThrow(() -> 
                new LogicalPredicate("AND", Arrays.asList(pred1, pred2)));
            
            assertDoesNotThrow(() -> 
                new LogicalPredicate("OR", Arrays.asList(pred1, pred2)));
            
            assertDoesNotThrow(() -> 
                new LogicalPredicate("NOT", Collections.singletonList(pred1)));
        }
    }

    @Nested
    @DisplayName("Cross-Predicate Parameter Name Tests")
    class CrossPredicateParameterNameTests {

        @Test
        @DisplayName("should validate parameter names consistently across all predicate types")
        void shouldValidateParameterNamesConsistentlyAcrossAllPredicateTypes() {
            String invalidParamName = "123invalid";
            
            // SimplePredicate
            assertThrows(QueryBuildException.class, () -> 
                new SimplePredicate("field", "=", "value", invalidParamName));
            
            // InPredicate
            assertThrows(QueryBuildException.class, () -> 
                new InPredicate("field", Arrays.asList("value1", "value2"), invalidParamName));
            
            // LikePredicate
            assertThrows(QueryBuildException.class, () -> 
                new LikePredicate("field", "%pattern%", invalidParamName));
            
            // BetweenPredicate
            assertThrows(QueryBuildException.class, () -> 
                new BetweenPredicate("field", 1, 10, invalidParamName, "validParam"));
            
            assertThrows(QueryBuildException.class, () -> 
                new BetweenPredicate("field", 1, 10, "validParam", invalidParamName));
        }

        @Test
        @DisplayName("should accept valid parameter names consistently across all predicate types")
        void shouldAcceptValidParameterNamesConsistentlyAcrossAllPredicateTypes() {
            String validParamName = "validParam";
            
            // SimplePredicate
            assertDoesNotThrow(() -> 
                new SimplePredicate("field", "=", "value", validParamName));
            
            // InPredicate
            assertDoesNotThrow(() -> 
                new InPredicate("field", Arrays.asList("value1", "value2"), validParamName));
            
            // LikePredicate
            assertDoesNotThrow(() -> 
                new LikePredicate("field", "%pattern%", validParamName));
            
            // BetweenPredicate
            assertDoesNotThrow(() -> 
                new BetweenPredicate("field", 1, 10, "startParam", "endParam"));
        }
    }
}