package com.github.query4j.core.criteria;

import java.util.Collections;
import java.util.Map;

import com.github.query4j.core.QueryBuildException;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * Immutable predicate for NULL checks.
 * 
 * <p>
 * Supports both {@code IS NULL} and {@code IS NOT NULL} operations.
 * </p>
 * 
 * <p>
 * This predicate is thread-safe and immutable. The field name is validated
 * to ensure it conforms to the pattern {@code [A-Za-z0-9_\.]+}.
 * </p>
 * 
 * <p>
 * Examples:
 * </p>
 * <ul>
 * <li>{@code new NullPredicate("email", true)} generates {@code email IS NULL}</li>
 * <li>{@code new NullPredicate("email", false)} generates {@code email IS NOT NULL}</li>
 * </ul>
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
@Value
@EqualsAndHashCode
public class NullPredicate implements Predicate {
	
    String field;
    
    boolean isNull;

    /**
     * Constructs a new NullPredicate with validation.
     * 
     * @param field the field name to check for null (must match pattern [A-Za-z0-9_\.]+)
     * @param isNull true for IS NULL, false for IS NOT NULL
     * @throws QueryBuildException if the field name is invalid
     */
    public NullPredicate(String field, boolean isNull) {
        // Validate field name (this will handle null check)
        FieldValidator.validateFieldName(field);
        
        this.field = field.trim();
        this.isNull = isNull;
    }

    @Override
    public String toSQL() {
        return field + (isNull ? " IS NULL" : " IS NOT NULL");
    }

    @Override
    public Map<String, Object> getParameters() {
        return Collections.emptyMap();
    }
}
