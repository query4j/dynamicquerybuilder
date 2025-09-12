package com.github.query4j.core.criteria;

import com.github.query4j.core.QueryBuildException;

/**
 * Utility class for validating field names in predicates.
 * 
 * <p>
 * This class provides centralized validation logic for field names used in 
 * predicate construction, ensuring they conform to the required pattern and 
 * are not null or empty.
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
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
final class FieldValidator {
    
    /**
     * Regular expression pattern for valid field names.
     * Allows alphanumeric characters, underscores, and dots.
     */
    private static final String VALID_FIELD_PATTERN = "[A-Za-z0-9_\\.]+";
    
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
    static void validateFieldName(String fieldName) {
        if (fieldName == null) {
            throw new QueryBuildException("Field name must not be null");
        }
        
        String trimmed = fieldName.trim();
        if (trimmed.isEmpty()) {
            throw new QueryBuildException("Field name must not be empty");
        }
        
        if (!trimmed.matches(VALID_FIELD_PATTERN)) {
            throw new QueryBuildException(
                "Field name contains invalid characters: '" + fieldName + 
                "'. Valid pattern is [A-Za-z0-9_\\.]+"
            );
        }
    }
}