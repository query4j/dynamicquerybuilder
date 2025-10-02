package com.github.query4j.core.criteria;

import com.github.query4j.core.QueryBuildException;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Collections;
import java.util.Map;

/**
 * Immutable predicate for HAVING clause conditions with aggregation expressions.
 * 
 * <p>
 * This predicate is specifically designed for HAVING clauses which can contain
 * aggregation function expressions like COUNT(field), SUM(field), etc., in addition
 * to regular field names.
 * </p>
 * 
 * <p>
 * Examples: {@code COUNT(id) > 5}, {@code SUM(amount) >= 1000}, {@code department = 'Engineering'}
 * </p>
 * 
 * <p>
 * This predicate is thread-safe and immutable. All constructor parameters are validated
 * to ensure they conform to the required constraints for HAVING clauses:
 * </p>
 * <ul>
 * <li>Aggregated field name can be either a regular field name or aggregation function</li>
 * <li>Operator must be from the predefined whitelist of SQL operators</li>
 * <li>Parameter name must match pattern {@code [A-Za-z][A-Za-z0-9_]*}</li>
 * </ul>
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
@Value
@EqualsAndHashCode
public class HavingPredicate implements Predicate {
	
    String aggregatedField;
    
    String operator;
    
    Object value;
    
    String paramName;

    /**
     * Constructs a new HavingPredicate with validation for HAVING clauses.
     * 
     * @param aggregatedField the field name or aggregation function (e.g., 'department' or 'COUNT(id)')
     * @param operator the comparison operator (must be from the allowed whitelist)
     * @param value the value to compare against (may be null)
     * @param paramName the parameter name for SQL binding (must match pattern [A-Za-z][A-Za-z0-9_]*)
     * @throws QueryBuildException if any parameter is invalid
     */
    public HavingPredicate(String aggregatedField, String operator, Object value, String paramName) {
        // Validate aggregated field name (allows both regular fields and aggregation functions)
        FieldValidator.validateAggregatedFieldName(aggregatedField);
        
        // Validate operator against whitelist
        OperatorValidator.validateOperator(operator);
        
        // Validate parameter name (this will handle null check, whitespace, and pattern)
        FieldValidator.validateParameterName(paramName);
        
        this.aggregatedField = aggregatedField.trim();
        this.operator = operator.trim();
        this.value = value;
        this.paramName = paramName.trim();
    }

    @Override
    public String toSQL() {
        return aggregatedField + " " + operator + " :" + paramName;
    }

    @Override
    public Map<String, Object> getParameters() {
        return Collections.singletonMap(paramName, value);
    }
}