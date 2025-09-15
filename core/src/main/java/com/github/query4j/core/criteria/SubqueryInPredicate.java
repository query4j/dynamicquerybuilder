package com.github.query4j.core.criteria;

import com.github.query4j.core.QueryBuilder;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Map;

/**
 * Immutable predicate for IN/NOT IN subquery operations.
 * 
 * <p>
 * Examples: {@code field IN (subquery)}, {@code field NOT IN (subquery)}
 * </p>
 * 
 * <p>
 * This predicate is thread-safe and immutable.
 * </p>
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
@Value
@EqualsAndHashCode
public class SubqueryInPredicate implements Predicate {
    
    String fieldName;
    
    String operator; // "IN" or "NOT IN"
    
    QueryBuilder<?> subquery;

    /**
     * Constructs a new SubqueryInPredicate with validation.
     * 
     * @param fieldName the field name
     * @param operator the subquery operator (IN or NOT IN)
     * @param subquery the subquery builder
     */
    public SubqueryInPredicate(String fieldName, String operator, QueryBuilder<?> subquery) {
        // Validate field name
        FieldValidator.validateFieldName(fieldName);
        
        if (operator == null || operator.trim().isEmpty()) {
            throw new IllegalArgumentException("operator must not be null or empty");
        }
        if (subquery == null) {
            throw new IllegalArgumentException("subquery must not be null");
        }
        
        String normalizedOperator = operator.trim().toUpperCase();
        if (!"IN".equals(normalizedOperator) && !"NOT IN".equals(normalizedOperator)) {
            throw new IllegalArgumentException("operator must be 'IN' or 'NOT IN'");
        }
        
        this.fieldName = fieldName.trim();
        this.operator = normalizedOperator;
        this.subquery = subquery;
    }

    @Override
    public String toSQL() {
        return fieldName + " " + operator + " (" + subquery.toSQL() + ")";
    }

    @Override
    public Map<String, Object> getParameters() {
        return subquery.getParameters();
    }
}