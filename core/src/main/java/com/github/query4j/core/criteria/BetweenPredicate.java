package com.github.query4j.core.criteria;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Immutable predicate for BETWEEN clauses.
 * Example: field BETWEEN startValue AND endValue
 * 
 * @since 1.0.0
 */
@Value
@RequiredArgsConstructor
@EqualsAndHashCode
public class BetweenPredicate implements Predicate {
	
    @NonNull
    String field;
    
    Object startValue;
    
    Object endValue;
    
    @NonNull
    String startParamName;
    
    @NonNull
    String endParamName;

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
