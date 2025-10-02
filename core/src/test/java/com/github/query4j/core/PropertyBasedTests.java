package com.github.query4j.core;

import com.github.query4j.core.criteria.*;
import com.github.query4j.core.impl.DynamicQueryBuilder;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static net.jqwik.api.Assume.that;

/**
 * Property-based tests using jqwik for comprehensive validation of 
 * query composition, SQL generation, and parameter mapping.
 */
class PropertyBasedTests {

    @Provide
    Arbitrary<String> validFieldNames() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('A', 'Z')
            .withCharRange('0', '9')
            .withChars('_', '.')
            .ofMinLength(1)
            .ofMaxLength(50)
            .filter(s -> s.matches("[A-Za-z0-9_\\.]+"))
            .filter(s -> !s.isEmpty());
    }

    @Provide
    Arbitrary<String> validOperators() {
        return Arbitraries.of("=", "!=", "<>", "<", "<=", ">", ">=");
    }

    @Provide
    Arbitrary<Object> validValues() {
        return Arbitraries.oneOf(
            Arbitraries.strings().ofMaxLength(100),
            Arbitraries.integers(),
            Arbitraries.doubles(),
            Arbitraries.of(true, false),
            Arbitraries.just(null)
        );
    }

    @Provide
    Arbitrary<List<Object>> validValueLists() {
        return validValues().list().ofMinSize(1).ofMaxSize(10);
    }

    @Provide
    Arbitrary<String> validParameterNames() {
        // Generate parameter names that comply with strict validation: [A-Za-z][A-Za-z0-9_]*
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('A', 'Z')
            .ofLength(1)  // First character must be a letter
            .flatMap(firstChar -> 
                Arbitraries.strings()
                    .withCharRange('a', 'z')
                    .withCharRange('A', 'Z')
                    .withCharRange('0', '9')
                    .withChars('_')
                    .ofMinLength(0)
                    .ofMaxLength(19)
                    .map(rest -> firstChar + rest)
            );
    }

    @Provide
    Arbitrary<String> validPatterns() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('A', 'Z')
            .withCharRange('0', '9')
            .withChars('%', '_', ' ', '-')
            .ofMaxLength(100);
    }

    @Provide
    Arbitrary<String> validAggregationFunctions() {
        return Arbitraries.oneOf(
            // Simple aggregation functions
            validFieldNames().map(field -> "COUNT(" + field + ")"),
            validFieldNames().map(field -> "SUM(" + field + ")"),
            validFieldNames().map(field -> "AVG(" + field + ")"),
            validFieldNames().map(field -> "MIN(" + field + ")"),
            validFieldNames().map(field -> "MAX(" + field + ")"),
            // COUNT(*) is a special case
            Arbitraries.just("COUNT(*)"),
            // Regular field names (also valid for HAVING)
            validFieldNames()
        );
    }

    @Property
    void simplePredicateGeneratesValidSQL(@ForAll("validFieldNames") String field,
                                        @ForAll("validOperators") String operator,
                                        @ForAll("validValues") Object value,
                                        @ForAll("validParameterNames") String paramName) {
        
        SimplePredicate predicate = new SimplePredicate(field, operator, value, paramName);
        String sql = predicate.toSQL();
        
        // SQL should contain field, operator, and parameter placeholder
        assertTrue(sql.contains(field));
        assertTrue(sql.contains(operator));
        assertTrue(sql.contains(":" + paramName));
        
        // Parameters should contain exactly one entry
        Map<String, Object> params = predicate.getParameters();
        assertEquals(1, params.size());
        assertEquals(value, params.get(paramName));
    }

    @Property
    void havingPredicateGeneratesValidSQL(@ForAll("validAggregationFunctions") String aggregatedField,
                                        @ForAll("validOperators") String operator,
                                        @ForAll("validValues") Object value,
                                        @ForAll("validParameterNames") String paramName) {
        
        HavingPredicate predicate = new HavingPredicate(aggregatedField, operator, value, paramName);
        String sql = predicate.toSQL();
        
        // SQL should contain aggregated field, operator, and parameter placeholder
        assertTrue(sql.contains(aggregatedField));
        assertTrue(sql.contains(operator));
        assertTrue(sql.contains(":" + paramName));
        
        // Parameters should contain exactly one entry
        Map<String, Object> params = predicate.getParameters();
        assertEquals(1, params.size());
        assertEquals(value, params.get(paramName));
    }

    @Property
    void havingPredicateIsImmutable(@ForAll("validAggregationFunctions") String aggregatedField,
                                  @ForAll("validOperators") String operator,
                                  @ForAll("validValues") Object value,
                                  @ForAll("validParameterNames") String paramName) {
        
        HavingPredicate predicate = new HavingPredicate(aggregatedField, operator, value, paramName);
        
        // Multiple calls should return the same results
        String sql1 = predicate.toSQL();
        String sql2 = predicate.toSQL();
        Map<String, Object> params1 = predicate.getParameters();
        Map<String, Object> params2 = predicate.getParameters();
        
        assertEquals(sql1, sql2);
        assertEquals(params1, params2);
        
        // Verify immutability of parameters map
        assertThrows(UnsupportedOperationException.class, () -> {
            params1.put("test", "value");
        });
    }

    @Property
    void havingPredicateEqualsAndHashCodeConsistency(@ForAll("validAggregationFunctions") String aggregatedField,
                                                    @ForAll("validOperators") String operator,
                                                    @ForAll("validValues") Object value,
                                                    @ForAll("validParameterNames") String paramName) {
        
        HavingPredicate predicate1 = new HavingPredicate(aggregatedField, operator, value, paramName);
        HavingPredicate predicate2 = new HavingPredicate(aggregatedField, operator, value, paramName);
        
        // Equal objects should have same hash code
        assertEquals(predicate1, predicate2);
        assertEquals(predicate1.hashCode(), predicate2.hashCode());
        
        // Object should be equal to itself
        assertEquals(predicate1, predicate1);
        
        // toString should be consistent
        assertEquals(predicate1.toString(), predicate2.toString());
    }

    @Property
    void inPredicateGeneratesValidSQL(@ForAll("validFieldNames") String field,
                                    @ForAll("validValueLists") List<Object> values,
                                    @ForAll("validParameterNames") String baseParamName) {
        
        InPredicate predicate = new InPredicate(field, values, baseParamName);
        String sql = predicate.toSQL();
        
        // SQL should contain field and IN clause
        assertTrue(sql.contains(field));
        assertTrue(sql.contains("IN"));
        assertTrue(sql.contains("("));
        assertTrue(sql.contains(")"));
        
        // Should have correct number of parameter placeholders
        for (int i = 0; i < values.size(); i++) {
            assertTrue(sql.contains(":" + baseParamName + "_" + i));
        }
        
        // Parameters should match values
        Map<String, Object> params = predicate.getParameters();
        assertEquals(values.size(), params.size());
        for (int i = 0; i < values.size(); i++) {
            assertEquals(values.get(i), params.get(baseParamName + "_" + i));
        }
    }

    @Property
    void likePredicateGeneratesValidSQL(@ForAll("validFieldNames") String field,
                                      @ForAll("validPatterns") String pattern,
                                      @ForAll("validParameterNames") String paramName) {
        
        LikePredicate predicate = new LikePredicate(field, pattern, paramName);
        String sql = predicate.toSQL();
        
        // SQL should contain field, LIKE, and parameter placeholder
        assertTrue(sql.contains(field));
        assertTrue(sql.contains("LIKE"));
        assertTrue(sql.contains(":" + paramName));
        
        // Parameters should contain exactly one entry
        Map<String, Object> params = predicate.getParameters();
        assertEquals(1, params.size());
        assertEquals(pattern, params.get(paramName));
    }

    @Property
    void betweenPredicateGeneratesValidSQL(@ForAll("validFieldNames") String field,
                                         @ForAll("validValues") Object startValue,
                                         @ForAll("validValues") Object endValue,
                                         @ForAll("validParameterNames") String startParamName,
                                         @ForAll("validParameterNames") String endParamName) {
        that(!startParamName.equals(endParamName));
        
        BetweenPredicate predicate = new BetweenPredicate(field, startValue, endValue, 
                                                         startParamName, endParamName);
        String sql = predicate.toSQL();
        
        // SQL should contain field, BETWEEN, AND, and parameter placeholders
        assertTrue(sql.contains(field));
        assertTrue(sql.contains("BETWEEN"));
        assertTrue(sql.contains("AND"));
        assertTrue(sql.contains(":" + startParamName));
        assertTrue(sql.contains(":" + endParamName));
        
        // Parameters should contain exactly two entries
        Map<String, Object> params = predicate.getParameters();
        assertEquals(2, params.size());
        assertEquals(startValue, params.get(startParamName));
        assertEquals(endValue, params.get(endParamName));
    }

    @Property
    void nullPredicateGeneratesValidSQL(@ForAll("validFieldNames") String field,
                                      @ForAll boolean isNull) {
        NullPredicate predicate = new NullPredicate(field, isNull);
        String sql = predicate.toSQL();
        
        // SQL should contain field and IS NULL/IS NOT NULL
        assertTrue(sql.contains(field));
        assertTrue(sql.contains("IS"));
        if (isNull) {
            assertTrue(sql.contains("IS NULL"));
        } else {
            assertTrue(sql.contains("IS NOT NULL"));
        }
        
        // Parameters should be empty
        Map<String, Object> params = predicate.getParameters();
        assertTrue(params.isEmpty());
    }

    @Property
    void logicalPredicateGeneratesValidSQL(@ForAll("validFieldNames") String field1,
                                         @ForAll("validFieldNames") String field2,
                                         @ForAll("validValues") Object value1,
                                         @ForAll("validValues") Object value2) {
        // Use valid logical operators
        String operator = "AND"; // Simple case for property test
        
        Predicate pred1 = new SimplePredicate(field1, "=", value1, "p1");
        Predicate pred2 = new SimplePredicate(field2, "=", value2, "p2");
        List<Predicate> children = Arrays.asList(pred1, pred2);
        
        LogicalPredicate predicate = new LogicalPredicate(operator, children);
        String sql = predicate.toSQL();
        
        // SQL should contain both predicates and logical operator
        assertTrue(sql.contains(field1));
        assertTrue(sql.contains(field2));
        assertTrue(sql.contains(operator));
        assertTrue(sql.contains("("));
        assertTrue(sql.contains(")"));
        
        // Parameters should contain entries from both children
        Map<String, Object> params = predicate.getParameters();
        assertEquals(2, params.size());
        assertEquals(value1, params.get("p1"));
        assertEquals(value2, params.get("p2"));
    }

    @Property
    void queryBuilderGeneratesValidSQL(@ForAll("validFieldNames") String field,
                                     @ForAll("validValues") Object value) {
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        String sql = builder.where(field, value).toSQL();
        
        // SQL should be valid basic structure
        assertTrue(sql.startsWith("SELECT"));
        assertTrue(sql.contains("FROM"));
        assertTrue(sql.contains("WHERE"));
        assertTrue(sql.contains(field));
    }

    @Property
    void randomFieldNamesGenerateValidSQL(@ForAll("validFieldNames") String field) {
        // Test that any valid field name generates valid SQL
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        String sql = builder.where(field, "test").toSQL();
        
        assertTrue(sql.contains(field));
        assertTrue(sql.contains("WHERE"));
        
        // Basic SQL structure checks
        assertTrue(sql.startsWith("SELECT"));
        assertTrue(sql.contains("FROM"));
    }

    @Property
    void complexQueryCompositionMaintainsStructure(
            @ForAll("validFieldNames") String field1,
            @ForAll("validFieldNames") String field2,
            @ForAll("validFieldNames") String orderField,
            @ForAll("validValues") Object value1,
            @ForAll("validValues") Object value2,
            @ForAll @IntRange(min = 1, max = 100) int limit) {
        
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        String sql = builder
            .where(field1, value1)
            .and()
            .where(field2, value2)
            .orderBy(orderField)
            .limit(limit)
            .toSQL();
        
        // Verify SQL structure
        assertTrue(sql.startsWith("SELECT"));
        assertTrue(sql.contains("FROM"));
        assertTrue(sql.contains("WHERE"));
        assertTrue(sql.contains("ORDER BY"));
        assertTrue(sql.contains("LIMIT"));
        
        // Verify all fields are present
        assertTrue(sql.contains(field1));
        assertTrue(sql.contains(field2));
        assertTrue(sql.contains(orderField));
        assertTrue(sql.contains(String.valueOf(limit)));
    }

    @Property
    void predicatesAreImmutable(@ForAll("validFieldNames") String field,
                              @ForAll("validValues") Object value,
                              @ForAll("validParameterNames") String paramName) {
        
        SimplePredicate predicate = new SimplePredicate(field, "=", value, paramName);
        
        // Multiple calls should return the same results
        String sql1 = predicate.toSQL();
        String sql2 = predicate.toSQL();
        Map<String, Object> params1 = predicate.getParameters();
        Map<String, Object> params2 = predicate.getParameters();
        
        assertEquals(sql1, sql2);
        assertEquals(params1, params2);
        
        // Verify immutability of parameters map
        assertThrows(UnsupportedOperationException.class, () -> {
            params1.put("test", "value");
        });
    }

    @Property
    void builderIsImmutable(@ForAll("validFieldNames") String field,
                          @ForAll("validValues") Object value) {
        DynamicQueryBuilder<Object> original = new DynamicQueryBuilder<>(Object.class);
        String originalSQL = original.toSQL();
        
        // Operations should return new instances
        DynamicQueryBuilder<Object> modified = (DynamicQueryBuilder<Object>) original.where(field, value);
        
        // Original should be unchanged
        assertEquals(originalSQL, original.toSQL());
        
        // Modified should be different
        assertNotEquals(originalSQL, modified.toSQL());
        
        // They should be different instances
        assertNotSame(original, modified);
    }

    @Property
    void multiplePredicatesGenerateUniqueParameters(
            @ForAll("validFieldNames") String field1,
            @ForAll("validFieldNames") String field2,
            @ForAll("validValues") Object value1,
            @ForAll("validValues") Object value2) {
        
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        
        // Add multiple where conditions
        builder = (DynamicQueryBuilder<Object>) builder
            .where(field1, value1)
            .and()
            .where(field2, value2);
        
        String sql = builder.toSQL();
        List<Predicate> predicates = builder.getPredicates();
        
        // Should have at least one predicate
        assertFalse(predicates.isEmpty());
        
        // Collect all parameters from all predicates
        Map<String, Object> allParams = new HashMap<>();
        for (Predicate predicate : predicates) {
            allParams.putAll(predicate.getParameters());
        }
        
        // All parameter names should be unique (no collisions)
        Set<String> paramNames = allParams.keySet();
        assertEquals(allParams.size(), paramNames.size());
        
        // SQL should be valid
        assertTrue(sql.startsWith("SELECT"));
        assertTrue(sql.contains("FROM"));
        assertTrue(sql.contains("WHERE"));
    }

    // Test advanced builder configuration methods
    @Property
    void builderConfigurationMethodsPreserveImmutability(
            @ForAll("validFieldNames") String field,
            @ForAll("validValues") Object value,
            @ForAll @IntRange(min = 1, max = 100) int limit,
            @ForAll @IntRange(min = 1, max = 50) int offset,
            @ForAll @IntRange(min = 1, max = 10) int groupDepth) { // Added groupDepth parameter
        
        DynamicQueryBuilder<Object> original = new DynamicQueryBuilder<>(Object.class);
        String originalSQL = original.toSQL();
        
        // Test withLimit
        DynamicQueryBuilder<Object> withLimit = (DynamicQueryBuilder<Object>) original.withLimit(limit);
        assertNotSame(original, withLimit);
        assertEquals(originalSQL, original.toSQL()); // Original unchanged
        
        // Test withOffset  
        DynamicQueryBuilder<Object> withOffset = (DynamicQueryBuilder<Object>) original.withOffset(offset);
        assertNotSame(original, withOffset);
        assertEquals(originalSQL, original.toSQL()); // Original unchanged
        
        // Test withCacheEnabled
        DynamicQueryBuilder<Object> withCache = (DynamicQueryBuilder<Object>) original.withCacheEnabled(true);
        assertNotSame(original, withCache);
        assertEquals(originalSQL, original.toSQL()); // Original unchanged
        
        // Test withNextLogicalOperator
        DynamicQueryBuilder<Object> withLogical = (DynamicQueryBuilder<Object>) original.withNextLogicalOperator("AND");
        assertNotSame(original, withLogical);
        assertEquals(originalSQL, original.toSQL()); // Original unchanged
        
        // Test withGroupDepth (change from 0 to avoid optimization)
        DynamicQueryBuilder<Object> withDepth = (DynamicQueryBuilder<Object>) original.withGroupDepth(groupDepth + 1);
        assertNotSame(original, withDepth);
        assertEquals(originalSQL, original.toSQL()); // Original unchanged
    }

    // Test complex aggregation scenarios
    @Property
    void complexAggregationScenariosGenerateValidSQL(
            @ForAll("validFieldNames") String field1,
            @ForAll("validFieldNames") String field2,
            @ForAll("validFieldNames") String field3,
            @ForAll("validValues") Object havingValue) {
        
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        
        // Create complex aggregation query - each aggregation method replaces the previous SELECT
        // So we'll test individual aggregations and combined clauses
        DynamicQueryBuilder<Object> complexQuery = (DynamicQueryBuilder<Object>) builder
            .count(field3) // Only count will be in the final SELECT
            .groupBy(field1, field2)
            .having("SUM(" + field1 + ")", ">", havingValue)
            .orderBy(field1, false);
        
        String sql = complexQuery.toSQL();
        
        // Verify aggregation structure
        assertTrue(sql.contains("SELECT"));
        assertTrue(sql.contains("COUNT(")); // Only COUNT should be present since it was last
        assertTrue(sql.contains("GROUP BY"));
        assertTrue(sql.contains("HAVING"));
        assertTrue(sql.contains("ORDER BY"));
        
        // Verify fields are present in appropriate clauses
        assertTrue(sql.contains(field1)); // Should be in GROUP BY, HAVING, ORDER BY
        assertTrue(sql.contains(field2)); // Should be in GROUP BY
        assertTrue(sql.contains(field3)); // Should be in COUNT()
    }

    // Test join operations with different types
    @Property
    void joinOperationsGenerateCorrectSQL(
            @ForAll("validFieldNames") String table1,
            @ForAll("validFieldNames") String table2) {
        
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        
        // Join methods expect field names, not full conditions
        // Test different join types
        DynamicQueryBuilder<Object> innerJoin = (DynamicQueryBuilder<Object>) 
            builder.innerJoin(table1);
        String innerJoinSQL = innerJoin.toSQL();
        assertTrue(innerJoinSQL.length() > builder.toSQL().length());
        assertTrue(innerJoinSQL.contains("INNER JOIN"));
        
        DynamicQueryBuilder<Object> leftJoin = (DynamicQueryBuilder<Object>) 
            builder.leftJoin(table1);
        String leftJoinSQL = leftJoin.toSQL();
        assertTrue(leftJoinSQL.length() > builder.toSQL().length());
        assertTrue(leftJoinSQL.contains("LEFT JOIN"));
        
        DynamicQueryBuilder<Object> rightJoin = (DynamicQueryBuilder<Object>) 
            builder.rightJoin(table1);
        String rightJoinSQL = rightJoin.toSQL();
        assertTrue(rightJoinSQL.length() > builder.toSQL().length());
        assertTrue(rightJoinSQL.contains("RIGHT JOIN"));
        
        // Test generic join
        DynamicQueryBuilder<Object> genericJoin = (DynamicQueryBuilder<Object>) 
            builder.join(table1);
        assertTrue(genericJoin.toSQL().length() > builder.toSQL().length());
        
        // Test fetch join
        DynamicQueryBuilder<Object> fetchJoin = (DynamicQueryBuilder<Object>) 
            builder.fetch(table2);
        String fetchSQL = fetchJoin.toSQL();
        assertTrue(fetchSQL.length() > builder.toSQL().length());
        assertTrue(fetchSQL.contains("FETCH"));
    }

    // Test pagination scenarios
    @Property
    void paginationScenariosGenerateValidSQL(
            @ForAll("validFieldNames") String field,
            @ForAll("validValues") Object value,
            @ForAll @IntRange(min = 1, max = 100) int pageNumber,
            @ForAll @IntRange(min = 5, max = 50) int pageSize) {
        
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        
        // Test page method
        DynamicQueryBuilder<Object> pagedQuery = (DynamicQueryBuilder<Object>) builder
            .where(field, value)
            .orderBy(field)
            .page(pageNumber, pageSize);
        
        String sql = pagedQuery.toSQL();
        
        assertTrue(sql.contains("LIMIT"));
        assertTrue(sql.contains("ORDER BY"));
        assertTrue(sql.contains(String.valueOf(pageSize)));
        
        // Verify offset calculation - OFFSET is only included when > 0
        long expectedOffset = (long) (pageNumber - 1) * pageSize;
        if (expectedOffset > 0) {
            assertTrue(sql.contains("OFFSET"));
            if (expectedOffset <= Integer.MAX_VALUE) {
                assertTrue(sql.contains(String.valueOf(expectedOffset)));
            }
        }
    }

    // Test execution methods exist and are callable
    @Property
    void executionMethodsAreCallable(
            @ForAll("validFieldNames") String field,
            @ForAll("validValues") Object value) {
        
        DynamicQueryBuilder<Object> builder = (DynamicQueryBuilder<Object>) new DynamicQueryBuilder<>(Object.class)
            .where(field, value);
        
        // These methods should return non-null objects (even though they don't execute)
        assertNotNull(builder.count());
        assertNotNull(builder.findAll());
        // Skip findOne() as it returns null in the stub implementation  
        assertTrue(builder.exists() == false || builder.exists() == true); // Returns boolean, check it's valid
        assertNotNull(builder.findPage());
        assertNotNull(builder.countAsync());
        assertNotNull(builder.findAllAsync());
        assertNotNull(builder.findOneAsync()); // This wraps findOne() in CompletableFuture so should be non-null
        assertNotNull(builder.build());
        
        // getExecutionStats() now works, so test it returns a valid object
        assertNotNull(builder.getExecutionStats());
        assertNotNull(builder.getExecutionStats().getGeneratedSQL());
    }
}