package com.github.query4j.core.criteria;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Immutable predicate for IN clauses.
 * Example: field IN (value1, value2, value3)
 * 
 * @since 1.0.0
 */
@Value
@EqualsAndHashCode
public class InPredicate implements Predicate {
	
    @NonNull
    String field;
    
    @NonNull
    List<Object> values;
    
    @NonNull
    String baseParamName;

    public InPredicate(@NonNull String field, @NonNull List<Object> values, @NonNull String baseParamName) {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("values must not be empty");
        }
        this.field = field;
        this.values = Collections.unmodifiableList(new ArrayList<>(values));
        this.baseParamName = baseParamName;
    }

    @Override
    public String toSQL() {
        String placeholders = IntStream.range(0, values.size())
                .mapToObj(i -> ":" + baseParamName + "_" + i)
                .collect(Collectors.joining(", "));
        return field + " IN (" + placeholders + ")";
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> params = new HashMap<>();
        for (int i = 0; i < values.size(); i++) {
            params.put(baseParamName + "_" + i, values.get(i));
        }
        return Collections.unmodifiableMap(params);
    }
}
