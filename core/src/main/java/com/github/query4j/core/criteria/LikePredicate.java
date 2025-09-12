package com.github.query4j.core.criteria;

import com.github.query4j.core.QueryBuildException;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Collections;
import java.util.Map;

/**
 * Immutable predicate for LIKE clauses.
 * 
 * <p>
 * Example: {@code field LIKE pattern}
 * </p>
 * 
 * <p>
 * This predicate is thread-safe and immutable. All constructor parameters are validated
 * to ensure they conform to the required constraints:
 * </p>
 * <ul>
 * <li>Field name must match pattern {@code [A-Za-z0-9_\.]+}</li>
 * <li>Pattern must not be null</li>
 * <li>Parameter name must match pattern {@code [A-Za-z][A-Za-z0-9_]*}</li>
 * </ul>
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
@Value
@EqualsAndHashCode
public class LikePredicate implements Predicate {
	
    String field;
    
    String pattern;
    
    String paramName;

    /**
     * Constructs a new LikePredicate with validation.
     * 
     * @param field the field name for the LIKE comparison (must match pattern [A-Za-z0-9_\.]+)
     * @param pattern the LIKE pattern (must not be null)
     * @param paramName the parameter name for SQL binding (must match pattern [A-Za-z][A-Za-z0-9_]*)
     * @throws QueryBuildException if any parameter is invalid
     */
    public LikePredicate(String field, String pattern, String paramName) {
        // Validate field name (this will handle null check and whitespace)
        FieldValidator.validateFieldName(field);
        
        // Validate pattern
        if (pattern == null) {
            throw new QueryBuildException("Pattern must not be null");
        }
        
        // Validate parameter name (this will handle null check, whitespace, and pattern)
        FieldValidator.validateParameterName(paramName);
        
        this.field = field.trim();
        this.pattern = pattern;
        this.paramName = paramName.trim();
    }

    @Override
    public String toSQL() {
        return field + " LIKE :" + paramName;
    }

    @Override
    public Map<String, Object> getParameters() {
        return Collections.singletonMap(paramName, pattern);
    }
}
