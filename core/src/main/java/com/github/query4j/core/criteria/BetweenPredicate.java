package com.github.query4j.core.criteria;

import com.github.query4j.core.QueryBuildException;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Immutable predicate for BETWEEN clauses.
 * 
 * <p>
 * Example: {@code field BETWEEN startValue AND endValue}
 * </p>
 * 
 * <p>
 * This predicate is thread-safe and immutable. All constructor parameters are validated
 * to ensure they conform to the required constraints:
 * </p>
 * <ul>
 * <li>Field name must match pattern {@code [A-Za-z0-9_\.]+}</li>
 * <li>Start parameter name must match pattern {@code [A-Za-z][A-Za-z0-9_]*}</li>
 * <li>End parameter name must match pattern {@code [A-Za-z][A-Za-z0-9_]*}</li>
 * </ul>
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
@Value
@EqualsAndHashCode
public class BetweenPredicate implements Predicate {
	
    String field;
    
    Object startValue;
    
    Object endValue;
    
    String startParamName;
    
    String endParamName;

    /**
     * Constructs a new BetweenPredicate with validation.
     * 
     * @param field the field name for the BETWEEN comparison (must match pattern [A-Za-z0-9_\.]+)
     * @param startValue the start value for the BETWEEN clause (may be null)
     * @param endValue the end value for the BETWEEN clause (may be null)
     * @param startParamName the parameter name for the start value (must match pattern [A-Za-z][A-Za-z0-9_]*)
     * @param endParamName the parameter name for the end value (must match pattern [A-Za-z][A-Za-z0-9_]*)
     * @throws QueryBuildException if any parameter is invalid
     */
    public BetweenPredicate(String field, Object startValue, Object endValue, 
                           String startParamName, String endParamName) {
        // Validate field name (this will handle null check and whitespace)
        FieldValidator.validateFieldName(field);
        
        // Validate parameter names (this will handle null check, whitespace, and pattern)
        FieldValidator.validateParameterName(startParamName);
        FieldValidator.validateParameterName(endParamName);
        
        // Validate that parameter names are different to avoid conflicts
        // Check for parameter collision after validation ensures both parameters are non-null
        if (startParamName.trim().equals(endParamName.trim())) {
            throw new QueryBuildException("Start and end parameter names must be different to avoid conflicts");
        }
        
        this.field = field.trim();
        this.startValue = startValue;
        this.endValue = endValue;
        // These trim() calls are safe because validateParameterName ensures non-null
        this.startParamName = startParamName.trim();
        this.endParamName = endParamName.trim();
    }

    @Override
    public String toSQL() {
        return field + " BETWEEN :" + startParamName + " AND :" + endParamName;
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put(startParamName, startValue);
        params.put(endParamName, endValue);
        return Collections.unmodifiableMap(params);
    }
}
