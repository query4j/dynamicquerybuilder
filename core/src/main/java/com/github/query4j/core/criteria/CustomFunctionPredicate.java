package com.github.query4j.core.criteria;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Immutable predicate for custom database function calls.
 * 
 * <p>
 * Examples: {@code UPPER(field) = value}, {@code DATE_TRUNC('day', field) > value}
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
public class CustomFunctionPredicate implements Predicate {
    
    String functionName;
    
    String fieldName;
    
    Object[] parameters;
    
    String paramPrefix;

    /**
     * Constructs a new CustomFunctionPredicate with validation.
     * 
     * @param functionName the database function name
     * @param fieldName the field to apply the function to
     * @param parameters additional function parameters
     * @param paramPrefix prefix for parameter naming
     */
    public CustomFunctionPredicate(String functionName, String fieldName, Object[] parameters, String paramPrefix) {
        if (functionName == null || functionName.trim().isEmpty()) {
            throw new IllegalArgumentException("functionName must not be null or empty");
        }
        
        // Validate field name
        FieldValidator.validateFieldName(fieldName);
        
        if (paramPrefix == null || paramPrefix.trim().isEmpty()) {
            throw new IllegalArgumentException("paramPrefix must not be null or empty");
        }
        
        this.functionName = functionName.trim().toUpperCase();
        this.fieldName = fieldName.trim();
        this.parameters = parameters != null ? parameters.clone() : new Object[0];
        this.paramPrefix = paramPrefix.trim();
    }

    @Override
    public String toSQL() {
        if (parameters.length == 0) {
            return functionName + "(" + fieldName + ")";
        }
        
        StringBuilder sql = new StringBuilder();
        sql.append(functionName).append("(").append(fieldName);
        
        for (int i = 0; i < parameters.length; i++) {
            sql.append(", :").append(paramPrefix).append("_").append(i);
        }
        
        sql.append(")");
        return sql.toString();
    }

    @Override
    public Map<String, Object> getParameters() {
        if (parameters.length == 0) {
            return Collections.emptyMap();
        }
        
        Map<String, Object> paramMap = new HashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            paramMap.put(paramPrefix + "_" + i, parameters[i]);
        }
        
        return Collections.unmodifiableMap(paramMap);
    }
}