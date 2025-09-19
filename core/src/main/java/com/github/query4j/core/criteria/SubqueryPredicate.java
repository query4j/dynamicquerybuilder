package com.github.query4j.core.criteria;

import com.github.query4j.core.QueryBuilder;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Map;

/**
 * Immutable predicate for subquery operations (EXISTS, NOT EXISTS).
 * 
 * <p>
 * Examples: {@code EXISTS (subquery)}, {@code NOT EXISTS (subquery)}
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
public class SubqueryPredicate implements Predicate {
    
    String operator; // "EXISTS" or "NOT EXISTS"
    
    QueryBuilder<?> subquery;

    /**
     * Constructs a new SubqueryPredicate with validation.
     * 
     * @param operator the subquery operator (EXISTS or NOT EXISTS)
     * @param subquery the subquery builder
     */
    public SubqueryPredicate(String operator, QueryBuilder<?> subquery) {
        if (operator == null || operator.trim().isEmpty()) {
            throw new IllegalArgumentException("operator must not be null or empty");
        }
        if (subquery == null) {
            throw new IllegalArgumentException("subquery must not be null");
        }
        
        String normalizedOperator = operator.trim().toUpperCase();
        if (!"EXISTS".equals(normalizedOperator) && !"NOT EXISTS".equals(normalizedOperator)) {
            throw new IllegalArgumentException("operator must be 'EXISTS' or 'NOT EXISTS'");
        }
        
        this.operator = normalizedOperator;
        this.subquery = subquery;
    }

    @Override
    public String toSQL() {
        return operator + " (" + subquery.toSQL() + ")";
    }

    @Override
    public Map<String, Object> getParameters() {
        return subquery.getParameters();
    }
}