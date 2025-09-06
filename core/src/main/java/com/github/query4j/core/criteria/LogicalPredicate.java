package com.github.query4j.core.criteria;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Immutable predicate for logical operations (AND, OR, NOT).
 * Combines multiple predicates with logical operators.
 * 
 * @since 1.0.0
 */
@Value
@EqualsAndHashCode
public class LogicalPredicate implements Predicate {
	
    @NonNull
    String operator;
    
    @NonNull
    List<Predicate> children;

    public LogicalPredicate(@NonNull String operator, @NonNull List<Predicate> children) {
        if (children.isEmpty()) {
            throw new IllegalArgumentException("children must not be empty");
        }
        this.operator = operator.toUpperCase();
        this.children = Collections.unmodifiableList(new ArrayList<>(children));
    }

    @Override
    public String toSQL() {
        if ("NOT".equals(operator) && children.size() == 1) {
            return "NOT (" + children.get(0).toSQL() + ")";
        }

        String joined = children.stream()
                .map(Predicate::toSQL)
                .collect(Collectors.joining(" " + operator + " "));
        return "(" + joined + ")";
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> allParams = new HashMap<>();
        for (Predicate child : children) {
            allParams.putAll(child.getParameters());
        }
        return Collections.unmodifiableMap(allParams);
    }
}
