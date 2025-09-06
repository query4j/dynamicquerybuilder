package com.github.query4j.core.criteria;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.Collections;
import java.util.Map;

/**
 * Immutable predicate for LIKE clauses.
 * Example: field LIKE pattern
 * 
 * @since 1.0.0
 */
@Value
@RequiredArgsConstructor
@EqualsAndHashCode
public class LikePredicate implements Predicate {
	
    @NonNull
    String field;
    
    @NonNull
    String pattern;
    
    @NonNull
    String paramName;

    @Override
    public String toSQL() {
        return field + " LIKE :" + paramName;
    }

    @Override
    public Map<String, Object> getParameters() {
        return Collections.singletonMap(paramName, pattern);
    }
}
