package com.github.query4j.core.criteria;

import java.util.Collections;
import java.util.Map;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

/**
 * Immutable predicate for NULL checks.
 * Supports both IS NULL and IS NOT NULL.
 * 
 * @since 1.0.0
 */
@Value
@EqualsAndHashCode
public class NullPredicate implements Predicate {
	
    @NonNull
    String field;
    
    boolean isNull;

    @Override
    public String toSQL() {
        return field + (isNull ? " IS NULL" : " IS NOT NULL");
    }

    @Override
    public Map<String, Object> getParameters() {
        return Collections.emptyMap();
    }
}
