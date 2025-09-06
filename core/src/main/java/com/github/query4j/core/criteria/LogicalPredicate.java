package com.github.query4j.core.criteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

/**
 * Immutable predicate for logical operations (AND, OR, NOT). Combines multiple
 * predicates with logical operators.
 * 
 * @since 1.0.0
 */
@Value
@EqualsAndHashCode
public class LogicalPredicate implements Predicate {

	private static final Set<String> ALLOWED_OPERATORS = new HashSet<>(Arrays.asList("AND", "OR", "NOT"));

	@NonNull
	String operator;

	@NonNull
	List<Predicate> children;

	public LogicalPredicate(@NonNull String operator, @NonNull List<Predicate> children) {
		if (children.isEmpty()) {
			throw new IllegalArgumentException("children must not be empty");
		}
		String op = operator.trim().toUpperCase();
		if (!ALLOWED_OPERATORS.contains(op)) {
			throw new IllegalArgumentException("Invalid logical operator: " + operator);
		}else if("NOT".equals(op) && children.size() != 1) {
			throw new IllegalArgumentException("NOT must have exactly one child predicate");
		}
		this.operator = op;
		this.children = Collections.unmodifiableList(new ArrayList<>(children));
	}

	@Override
	public String toSQL() {
		if ("NOT".equals(operator) && children.size() == 1) {
			return "NOT (" + children.get(0).toSQL() + ")";
		}

		String joined = children.stream().map(Predicate::toSQL).collect(Collectors.joining(" " + operator + " "));
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
