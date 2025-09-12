package com.github.query4j.core.criteria;

import com.github.query4j.core.QueryBuildException;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Collections;
import java.util.Map;

/**
 * Immutable predicate for simple field comparisons.
 * 
 * <p>
 * Examples: {@code field = value}, {@code field > value}, {@code field <= value}, etc.
 * </p>
 * 
 * <p>
 * This predicate is thread-safe and immutable. All constructor parameters are validated
 * to ensure they conform to the required constraints:
 * </p>
 * <ul>
 * <li>Field name must match pattern {@code [A-Za-z0-9_\.]+}</li>
 * <li>Operator must not be null or empty</li>
 * <li>Parameter name must not be null or empty</li>
 * </ul>
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
@Value
@EqualsAndHashCode
public class SimplePredicate implements Predicate {
	
    String field;
    
    String operator;
    
    Object value;
    
    String paramName;

    /**
     * Constructs a new SimplePredicate with validation.
     * 
     * @param field the field name to compare (must match pattern [A-Za-z0-9_\.]+)
     * @param operator the comparison operator (must not be null or empty)
     * @param value the value to compare against (may be null)
     * @param paramName the parameter name for SQL binding (must not be null or empty)
     * @throws QueryBuildException if any parameter is invalid
     */
    public SimplePredicate(String field, String operator, Object value, String paramName) {
        // Validate field name (this will handle null check)
        FieldValidator.validateFieldName(field);
        
        // Validate operator
        if (operator == null) {
            throw new QueryBuildException("Operator must not be null");
        }
        if (operator.trim().isEmpty()) {
            throw new QueryBuildException("Operator must not be empty");
        }
        
        // Validate parameter name
        if (paramName == null) {
            throw new QueryBuildException("Parameter name must not be null");
        }
        if (paramName.trim().isEmpty()) {
            throw new QueryBuildException("Parameter name must not be empty");
        }
        
        this.field = field.trim();
        this.operator = operator.trim();
        this.value = value;
        this.paramName = paramName.trim();
    }

    @Override
    public String toSQL() {
        return field + " " + operator + " :" + paramName;
    }

    @Override
    public Map<String, Object> getParameters() {
        return Collections.singletonMap(paramName, value);
    }
}
