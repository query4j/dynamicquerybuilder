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

import com.github.query4j.core.QueryBuildException;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * Immutable predicate for logical operations (AND, OR, NOT). 
 * 
 * <p>
 * Combines multiple predicates with logical operators to create complex query conditions.
 * </p>
 * 
 * <p>
 * This predicate is thread-safe and immutable. All constructor parameters are validated
 * to ensure they conform to the required constraints:
 * </p>
 * <ul>
 * <li>Operator must be one of: AND, OR, NOT (case-insensitive)</li>
 * <li>Children list must not be null or empty</li>
 * <li>Children list must not contain any null predicates</li>
 * <li>NOT operator must have exactly one child predicate</li>
 * <li>AND and OR operators must have at least one child predicate</li>
 * </ul>
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
@Value
@EqualsAndHashCode
public class LogicalPredicate implements Predicate {

	private static final Set<String> ALLOWED_OPERATORS = new HashSet<>(Arrays.asList("AND", "OR", "NOT"));

	String operator;

	List<Predicate> children;

	/**
	 * Constructs a new LogicalPredicate with validation.
	 * 
	 * @param operator the logical operator (AND, OR, NOT - case insensitive)
	 * @param children the child predicates to combine (must not be null, empty, or contain null predicates)
	 * @throws QueryBuildException if the operator is invalid or children constraints are violated
	 */
	public LogicalPredicate(String operator, List<Predicate> children) {
		if (operator == null) {
			throw new QueryBuildException("Operator must not be null");
		}
		if (children == null) {
			throw new QueryBuildException("Children list must not be null");
		}
		if (children.isEmpty()) {
			throw new QueryBuildException("Children list must not be empty");
		}
		
		// Prevent null children to avoid NPEs later
		for (int i = 0; i < children.size(); i++) {
			if (children.get(i) == null) {
				throw new QueryBuildException("Child predicate at index " + i + " must not be null");
			}
		}
		
		String op = operator.trim().toUpperCase();
		if (!ALLOWED_OPERATORS.contains(op)) {
			throw new QueryBuildException("Invalid logical operator: '" + operator + "'. Allowed operators are: AND, OR, NOT");
		}
		if("NOT".equals(op) && children.size() != 1) {
			throw new QueryBuildException("NOT operator must have exactly one child predicate, but got " + children.size());
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
