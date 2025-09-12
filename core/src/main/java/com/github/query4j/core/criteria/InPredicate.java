package com.github.query4j.core.criteria;

import com.github.query4j.core.QueryBuildException;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Immutable predicate for IN clauses.
 * 
 * <p>
 * Example: {@code field IN (value1, value2, value3)}
 * </p>
 * 
 * <p>
 * This predicate is thread-safe and immutable. All constructor parameters are validated
 * to ensure they conform to the required constraints:
 * </p>
 * <ul>
 * <li>Field name must match pattern {@code [A-Za-z0-9_\.]+}</li>
 * <li>Values list must not be null or empty</li>
 * <li>Base parameter name must match pattern {@code [A-Za-z][A-Za-z0-9_]*}</li>
 * </ul>
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
@Value
@EqualsAndHashCode
public class InPredicate implements Predicate {
	
    String field;
    
    List<Object> values;
    
    String baseParamName;

    /**
     * Constructs a new InPredicate with validation.
     * 
     * @param field the field name for the IN comparison (must match pattern [A-Za-z0-9_\.]+)
     * @param values the list of values for the IN clause (must not be null or empty)
     * @param baseParamName the base parameter name for SQL binding (must match pattern [A-Za-z][A-Za-z0-9_]*)
     * @throws QueryBuildException if any parameter is invalid
     */
    public InPredicate(String field, List<Object> values, String baseParamName) {
        // Validate field name (this will handle null check and whitespace)
        FieldValidator.validateFieldName(field);
        
        // Validate values list
        if (values == null) {
            throw new QueryBuildException("Values list must not be null");
        }
        if (values.isEmpty()) {
            throw new QueryBuildException("Values list must not be empty");
        }
        
        // Validate base parameter name (this will handle null check, whitespace, and pattern)
        FieldValidator.validateParameterName(baseParamName);
        
        this.field = field.trim();
        this.values = Collections.unmodifiableList(new ArrayList<>(values));
        this.baseParamName = baseParamName.trim();
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
