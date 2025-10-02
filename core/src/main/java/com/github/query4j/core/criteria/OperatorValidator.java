package com.github.query4j.core.criteria;

import com.github.query4j.core.QueryBuildException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for validating SQL operators in predicates.
 * 
 * <p>
 * This class provides centralized validation logic for SQL operators used in 
 * predicate construction, ensuring they are from a predefined whitelist and 
 * preventing operator injection attacks.
 * </p>
 * 
 * <p>
 * Valid operators include common SQL comparison operators:
 * </p>
 * <ul>
 * <li>=, !=, &lt;&gt; (equality and inequality)</li>
 * <li>&lt;, &lt;=, &gt;, &gt;= (comparison operators)</li>
 * <li>LIKE, NOT LIKE (pattern matching)</li>
 * <li>IS, IS NOT (null comparison)</li>
 * <li>IN, NOT IN (set membership)</li>
 * </ul>
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class OperatorValidator {
    
    /**
     * Set of allowed SQL operators to prevent operator injection.
     */
    private static final Set<String> ALLOWED_OPERATORS = new HashSet<>(Arrays.asList(
        "=", "!=", "<>", "<", "<=", ">", ">=",
        "LIKE", "NOT LIKE", "ILIKE", "NOT ILIKE",
        "IS", "IS NOT", "IN", "NOT IN",
        "BETWEEN", "NOT BETWEEN",
        "EXISTS", "NOT EXISTS"
    ));
    
    /**
     * Private constructor to prevent instantiation.
     */
    private OperatorValidator() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Validates that the given operator is in the allowed whitelist.
     * 
     * <p>
     * A valid operator must:
     * </p>
     * <ul>
     * <li>Not be null</li>
     * <li>Not be empty or only whitespace</li>
     * <li>Be in the predefined whitelist of SQL operators</li>
     * </ul>
     * 
     * <p>
     * This validation prevents SQL injection attacks through malformed operators
     * and ensures only legitimate SQL operators are used in queries.
     * </p>
     * 
     * @param operator the operator to validate (case-insensitive)
     * @throws QueryBuildException if the operator is null, empty, or not in the whitelist
     */
    public static void validateOperator(String operator) {
        if (operator == null) {
            throw new QueryBuildException("Operator must not be null");
        }
        
        // Normalize by trimming leading/trailing whitespace
        String trimmed = operator.trim();
        if (trimmed.isEmpty()) {
            throw new QueryBuildException("Operator must not be empty");
        }
        
        // Check against whitelist (case-insensitive)
        String upperCaseOperator = trimmed.toUpperCase();
        if (!ALLOWED_OPERATORS.contains(upperCaseOperator)) {
            throw new QueryBuildException(
                "Invalid operator: '" + operator + "'. Allowed operators are: " + ALLOWED_OPERATORS
            );
        }
    }
}