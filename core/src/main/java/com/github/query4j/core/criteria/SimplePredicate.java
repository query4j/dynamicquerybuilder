package com.github.query4j.core.criteria;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.Collections;
import java.util.Map;

/**
 * Immutable predicate for simple field comparisons.
 * Examples: field = value, field > value, etc.
 * 
 * @since 1.0.0
 */
@Value
@RequiredArgsConstructor
@EqualsAndHashCode
public class SimplePredicate implements Predicate {
	
    @NonNull
    String field;
    
    @NonNull
    String operator;
    
    Object value;
    
    @NonNull
    String paramName;

    @Override
    public String toSQL() {
        return field + " " + operator + " :" + paramName;
    }

    @Override
    public Map<String, Object> getParameters() {
        return Collections.singletonMap(paramName, value);
    }
}
