package com.github.query4j.core.criteria;

import com.github.query4j.core.QueryBuildException;
import java.util.regex.Pattern;

/**
 * Utility class for validating field names and parameter names in predicates.
 * 
 * <p>
 * This class provides centralized validation logic for field names and parameter names
 * used in predicate construction, ensuring they conform to the required patterns and 
 * are not null or empty. This validation prevents SQL injection attacks through 
 * malformed field names and parameter names.
 * </p>
 * 
 * <p>
 * Valid field names must match the pattern {@code [A-Za-z0-9_\.]+}, supporting:
 * </p>
 * <ul>
 * <li>Alphanumeric characters (a-z, A-Z, 0-9)</li>
 * <li>Underscores (_)</li>
 * <li>Dots (.) for qualified field names</li>
 * </ul>
 * 
 * <p>
 * Valid parameter names must match the pattern {@code [A-Za-z][A-Za-z0-9_]*}, supporting:
 * </p>
 * <ul>
 * <li>Must start with a letter (a-z, A-Z)</li>
 * <li>Can contain alphanumeric characters and underscores after the first character</li>
 * <li>Cannot start with numbers or special characters</li>
 * </ul>
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class FieldValidator {
    
    /**
     * Precompiled pattern for valid field names.
     * Allows alphanumeric characters, underscores, and dots.
     */
    private static final Pattern FIELD_PATTERN = Pattern.compile("^[A-Za-z0-9_\\.]+$");
    
    /**
     * Precompiled pattern for valid parameter names.
     * Must start with a letter, then allows letters, numbers, and underscores.
     */
    private static final Pattern PARAM_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z0-9_]*$");
    
    /**
     * Precompiled pattern for valid aggregation expressions in HAVING clauses.
     * Allows aggregation functions like COUNT(), SUM(), AVG(), MIN(), MAX() with field names.
     */
    private static final Pattern AGGREGATION_PATTERN = Pattern.compile("^[A-Za-z]+\\([A-Za-z0-9_\\.\\*]+\\)$");
    
    /**
     * Private constructor to prevent instantiation.
     */
    private FieldValidator() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Validates that the given field name is valid for use in predicates.
     * 
     * <p>
     * A valid field name must:
     * </p>
     * <ul>
     * <li>Not be null</li>
     * <li>Not be empty or only whitespace</li>
     * <li>Match the pattern {@code [A-Za-z0-9_\.]+}</li>
     * </ul>
     * 
     * @param fieldName the field name to validate
     * @throws QueryBuildException if the field name is null, empty, or contains invalid characters
     */
    public static void validateFieldName(String fieldName) {
        if (fieldName == null) {
            throw new QueryBuildException("Field name must not be null");
        }
        
        // Normalize by trimming leading/trailing whitespace
        String trimmed = fieldName.trim();
        if (trimmed.isEmpty()) {
            throw new QueryBuildException("Field name must not be empty");
        }
        
        if (!FIELD_PATTERN.matcher(trimmed).matches()) {
            throw new QueryBuildException(
                "Field name contains invalid characters: '" + fieldName + 
                "'. Valid pattern is [A-Za-z0-9_\\.]+"
            );
        }
    }
    
    /**
     * Validates that the given parameter name is valid for use in SQL placeholders.
     * 
     * <p>
     * A valid parameter name must:
     * </p>
     * <ul>
     * <li>Not be null</li>
     * <li>Not be empty or only whitespace</li>
     * <li>Start with a letter (a-z, A-Z)</li>
     * <li>Contain only letters, numbers, and underscores after the first character</li>
     * <li>Match the pattern {@code [A-Za-z][A-Za-z0-9_]*}</li>
     * </ul>
     * 
     * <p>
     * This validation prevents SQL injection attacks through malformed parameter names
     * and ensures compatibility with SQL parameter binding mechanisms.
     * </p>
     * 
     * @param paramName the parameter name to validate
     * @throws QueryBuildException if the parameter name is null, empty, or contains invalid characters
     */
    public static void validateParameterName(String paramName) {
        if (paramName == null) {
            throw new QueryBuildException("Parameter name must not be null");
        }
        
        // Normalize by trimming leading/trailing whitespace
        String trimmed = paramName.trim();
        if (trimmed.isEmpty()) {
            throw new QueryBuildException("Parameter name must not be empty");
        }
        
        if (!PARAM_PATTERN.matcher(trimmed).matches()) {
            throw new QueryBuildException(
                "Parameter name contains invalid characters: '" + paramName + 
                "'. Parameter names must start with a letter and contain only letters, numbers, and underscores"
            );
        }
    }
    
    /**
     * Validates that the given aggregated field name is valid for use in HAVING clauses.
     * 
     * <p>
     * A valid aggregated field name can be either:
     * </p>
     * <ul>
     * <li>A regular field name (matching {@code [A-Za-z0-9_\.]+})</li>
     * <li>An aggregation function expression (e.g., COUNT(id), SUM(amount), AVG(price))</li>
     * </ul>
     * 
     * <p>
     * This allows HAVING clauses to work with both simple field references and
     * aggregation function expressions commonly used in SQL.
     * </p>
     * 
     * @param aggregatedFieldName the aggregated field name to validate
     * @throws QueryBuildException if the field name is null, empty, or contains invalid characters
     * @since 1.0.0
     */
    public static void validateAggregatedFieldName(String aggregatedFieldName) {
        if (aggregatedFieldName == null) {
            throw new QueryBuildException("Aggregated field name must not be null");
        }
        
        // Normalize by trimming leading/trailing whitespace
        String trimmed = aggregatedFieldName.trim();
        if (trimmed.isEmpty()) {
            throw new QueryBuildException("Aggregated field name must not be empty");
        }
        
        // Check if it's a regular field name or an aggregation function
        boolean isValidFieldName = FIELD_PATTERN.matcher(trimmed).matches();
        boolean isValidAggregation = AGGREGATION_PATTERN.matcher(trimmed).matches();
        
        if (!isValidFieldName && !isValidAggregation) {
            throw new QueryBuildException(
                "Aggregated field name contains invalid characters: '" + aggregatedFieldName + 
                "'. Must be either a valid field name [A-Za-z0-9_\\.]+, or an aggregation function like COUNT(field)"
            );
        }
    }
}