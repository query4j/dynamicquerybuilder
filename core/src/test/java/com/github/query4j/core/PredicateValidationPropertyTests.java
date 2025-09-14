package com.github.query4j.core;

import com.github.query4j.core.criteria.*;
import com.github.query4j.core.impl.DynamicQueryBuilder;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.NotEmpty;
import net.jqwik.api.constraints.Size;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static net.jqwik.api.Assume.that;

/**
 * Property-based tests focusing on predicate validation, edge cases,
 * and error handling to improve branch coverage.
 */
class PredicateValidationPropertyTests {

    @Provide
    Arbitrary<String> invalidFieldNames() {
        return Arbitraries.oneOf(
            Arbitraries.just(""),           // Empty string
            Arbitraries.just("   "),       // Whitespace only
            Arbitraries.strings().withChars('!', '@', '#', '$', '%', '^', '&', '*', '-', ' ', '(', ')', '[', ']')
                .ofMinLength(1).ofMaxLength(20),  // Invalid characters
            Arbitraries.just("field-name"), // Hyphen (invalid)
            Arbitraries.just("field name"), // Space (invalid)
            Arbitraries.just("field@name"), // At symbol (invalid)
            Arbitraries.just("field#name"), // Hash symbol (invalid)
            Arbitraries.just("field$name")  // Dollar symbol (invalid)
        );
    }

    @Provide
    Arbitrary<String> invalidOperators() {
        return Arbitraries.oneOf(
            Arbitraries.just(""),           // Empty string
            Arbitraries.just("   "),       // Whitespace only
            Arbitraries.just("==="),       // Invalid triple equals
            Arbitraries.just("EQUALS"),    // Invalid word operator
            Arbitraries.just("??"),        // Invalid question marks
            Arbitraries.just("<<>>"),      // Invalid brackets
            Arbitraries.just("@="),        // Invalid symbol
            Arbitraries.just("!=!"),       // Invalid combination
            Arbitraries.just("INVALID")    // Invalid word
        );
    }

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
    Arbitrary<String> validParameterNames() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('A', 'Z')
            .ofLength(1)
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
    Arbitrary<String> invalidParameterNames() {
        return Arbitraries.oneOf(
            Arbitraries.just(""),
            Arbitraries.just("123param"),
            Arbitraries.just("param-name"),
            Arbitraries.just("param name"),
            Arbitraries.strings().withChars('!', '@', '#').ofMinLength(1).ofMaxLength(10)
        );
    }

    @Provide
    Arbitrary<List<Object>> emptyOrNullValueLists() {
        return Arbitraries.oneOf(
            Arbitraries.just(Collections.emptyList()),
            Arbitraries.just(null)
        );
    }

    @Provide
    Arbitrary<String> validLogicalOperators() {
        return Arbitraries.of("AND", "OR", "NOT");
    }

    // Test SimplePredicate validation edge cases
    @Property
    void simplePredicateThrowsOnInvalidFieldName(
            @ForAll("invalidFieldNames") String invalidField,
            @ForAll("validOperators") String operator,
            @ForAll("validValues") Object value,
            @ForAll("validParameterNames") String paramName) {
        
        assertThrows(QueryBuildException.class, () -> 
            new SimplePredicate(invalidField, operator, value, paramName)
        );
    }

    @Property
    void simplePredicateThrowsOnInvalidOperator(
            @ForAll("validFieldNames") String field,
            @ForAll("invalidOperators") String invalidOperator,
            @ForAll("validValues") Object value,
            @ForAll("validParameterNames") String paramName) {
        
        assertThrows(QueryBuildException.class, () -> 
            new SimplePredicate(field, invalidOperator, value, paramName)
        );
    }

    @Property
    void simplePredicateThrowsOnInvalidParameterName(
            @ForAll("validFieldNames") String field,
            @ForAll("validOperators") String operator,
            @ForAll("validValues") Object value,
            @ForAll("invalidParameterNames") String invalidParamName) {
        
        assertThrows(QueryBuildException.class, () -> 
            new SimplePredicate(field, operator, value, invalidParamName)
        );
    }

    // Test InPredicate validation edge cases  
    @Property
    void inPredicateThrowsOnEmptyValueList(
            @ForAll("validFieldNames") String field,
            @ForAll("emptyOrNullValueLists") List<Object> emptyValues,
            @ForAll("validParameterNames") String paramName) {
        
        assertThrows(QueryBuildException.class, () -> 
            new InPredicate(field, emptyValues, paramName)
        );
    }

    @Property
    void inPredicateThrowsOnInvalidFieldName(
            @ForAll("invalidFieldNames") String invalidField,
            @ForAll("validParameterNames") String paramName) {
        
        List<Object> values = Arrays.asList("value1", "value2");
        
        assertThrows(QueryBuildException.class, () -> 
            new InPredicate(invalidField, values, paramName)
        );
    }

    // Test LikePredicate validation edge cases
    @Property
    void likePredicateThrowsOnInvalidFieldName(
            @ForAll("invalidFieldNames") String invalidField,
            @ForAll("validParameterNames") String paramName) {
        
        assertThrows(QueryBuildException.class, () -> 
            new LikePredicate(invalidField, "pattern%", paramName)
        );
    }

    @Property
    void likePredicateThrowsOnNullPattern(
            @ForAll("validFieldNames") String field,
            @ForAll("validParameterNames") String paramName) {
        
        assertThrows(QueryBuildException.class, () -> 
            new LikePredicate(field, null, paramName)
        );
    }

    // Test BetweenPredicate validation edge cases
    @Property
    void betweenPredicateThrowsOnInvalidFieldName(
            @ForAll("invalidFieldNames") String invalidField,
            @ForAll("validValues") Object startValue,
            @ForAll("validValues") Object endValue,
            @ForAll("validParameterNames") String startParam,
            @ForAll("validParameterNames") String endParam) {
        
        that(!startParam.equals(endParam));
        
        assertThrows(QueryBuildException.class, () -> 
            new BetweenPredicate(invalidField, startValue, endValue, startParam, endParam)
        );
    }

    @Property
    void betweenPredicateThrowsOnSameParameterNames(
            @ForAll("validFieldNames") String field,
            @ForAll("validValues") Object startValue,
            @ForAll("validValues") Object endValue,
            @ForAll("validParameterNames") String paramName) {
        
        assertThrows(QueryBuildException.class, () -> 
            new BetweenPredicate(field, startValue, endValue, paramName, paramName)
        );
    }

    @Property
    void betweenPredicateHandlesNullValues(
            @ForAll("validFieldNames") String field,
            @ForAll("validParameterNames") String startParam,
            @ForAll("validParameterNames") String endParam) {
        
        that(!startParam.equals(endParam));
        
        BetweenPredicate predicate = new BetweenPredicate(field, null, null, startParam, endParam);
        String sql = predicate.toSQL();
        Map<String, Object> params = predicate.getParameters();
        
        assertTrue(sql.contains(field));
        assertTrue(sql.contains("BETWEEN"));
        assertEquals(2, params.size());
        assertNull(params.get(startParam));
        assertNull(params.get(endParam));
    }

    // Test NullPredicate validation
    @Property
    void nullPredicateThrowsOnInvalidFieldName(
            @ForAll("invalidFieldNames") String invalidField,
            @ForAll boolean isNull) {
        
        assertThrows(QueryBuildException.class, () -> 
            new NullPredicate(invalidField, isNull)
        );
    }

    @Property
    void nullPredicateGeneratesCorrectSQLForBothConditions(
            @ForAll("validFieldNames") String field,
            @ForAll boolean isNull) {
        
        NullPredicate predicate = new NullPredicate(field, isNull);
        String sql = predicate.toSQL();
        
        assertTrue(sql.contains(field));
        assertTrue(sql.contains("IS"));
        
        if (isNull) {
            assertTrue(sql.contains("IS NULL"));
            assertFalse(sql.contains("IS NOT NULL"));
        } else {
            assertTrue(sql.contains("IS NOT NULL"));
        }
        
        // Parameters should always be empty for null predicates
        assertTrue(predicate.getParameters().isEmpty());
    }

    // Test HavingPredicate validation edge cases
    @Property
    void havingPredicateThrowsOnInvalidAggregatedField(
            @ForAll("invalidFieldNames") String invalidField,
            @ForAll("validOperators") String operator,
            @ForAll("validValues") Object value,
            @ForAll("validParameterNames") String paramName) {
        
        assertThrows(QueryBuildException.class, () -> 
            new HavingPredicate(invalidField, operator, value, paramName)
        );
    }

    @Property
    void havingPredicateAcceptsValidAggregationFunctions(
            @ForAll("validFieldNames") String field,
            @ForAll("validOperators") String operator,
            @ForAll("validValues") Object value,
            @ForAll("validParameterNames") String paramName) {
        
        // Test various aggregation function formats
        String[] aggregationFormats = {
            "COUNT(" + field + ")",
            "SUM(" + field + ")",
            "AVG(" + field + ")",
            "MIN(" + field + ")",
            "MAX(" + field + ")",
            "COUNT(*)",
            field // Plain field name should also work
        };
        
        for (String aggregatedField : aggregationFormats) {
            HavingPredicate predicate = new HavingPredicate(aggregatedField, operator, value, paramName);
            String sql = predicate.toSQL();
            
            assertTrue(sql.contains(aggregatedField));
            assertTrue(sql.contains(operator));
            assertTrue(sql.contains(":" + paramName));
            
            Map<String, Object> params = predicate.getParameters();
            assertEquals(1, params.size());
            assertEquals(value, params.get(paramName));
        }
    }

    // Test LogicalPredicate validation edge cases
    @Property
    void logicalPredicateThrowsOnInvalidOperator(
            @ForAll("validFieldNames") String field1,
            @ForAll("validFieldNames") String field2,
            @ForAll("validValues") Object value1,
            @ForAll("validValues") Object value2) {
        
        Predicate pred1 = new SimplePredicate(field1, "=", value1, "p1");
        Predicate pred2 = new SimplePredicate(field2, "=", value2, "p2");
        List<Predicate> children = Arrays.asList(pred1, pred2);
        
        // Invalid logical operators
        String[] invalidLogicalOps = {"", "INVALID", "&&", "||", "XOR"};
        
        for (String invalidOp : invalidLogicalOps) {
            assertThrows(QueryBuildException.class, () -> 
                new LogicalPredicate(invalidOp, children)
            );
        }
    }

    @Property
    void logicalPredicateThrowsOnEmptyChildren(
            @ForAll("validLogicalOperators") String logicalOperator) {
        
        assertThrows(QueryBuildException.class, () -> 
            new LogicalPredicate(logicalOperator, Collections.emptyList())
        );
        
        assertThrows(QueryBuildException.class, () -> 
            new LogicalPredicate(logicalOperator, null)
        );
    }

    @Property
    void logicalPredicateHandlesSingleChildForNOT(
            @ForAll("validFieldNames") String field,
            @ForAll("validValues") Object value) {
        
        Predicate child = new SimplePredicate(field, "=", value, "p1");
        LogicalPredicate notPredicate = new LogicalPredicate("NOT", Arrays.asList(child));
        
        String sql = notPredicate.toSQL();
        assertTrue(sql.contains("NOT"));
        assertTrue(sql.contains("("));
        assertTrue(sql.contains(")"));
        assertTrue(sql.contains(field));
    }

    // Test QueryBuilder validation methods
    @Property
    void queryBuilderThrowsOnInvalidFieldNames(
            @ForAll("invalidFieldNames") String invalidField,
            @ForAll("validValues") Object value) {
        
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        
        // Ensure the field name is actually invalid before testing
        that(!invalidField.trim().isEmpty() && !invalidField.matches("[A-Za-z0-9_\\.]+"));
        
        // Test various methods that should validate field names
        assertThrows(IllegalArgumentException.class, () -> builder.where(invalidField, value));
        assertThrows(IllegalArgumentException.class, () -> builder.whereIsNull(invalidField));
        assertThrows(IllegalArgumentException.class, () -> builder.whereIsNotNull(invalidField));
        assertThrows(IllegalArgumentException.class, () -> builder.orderBy(invalidField));
        assertThrows(IllegalArgumentException.class, () -> builder.groupBy(invalidField));
    }

    @Property
    void queryBuilderThrowsOnInvalidOperators(
            @ForAll("validFieldNames") String field,
            @ForAll("invalidOperators") String invalidOperator,
            @ForAll("validValues") Object value) {
        
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        
        assertThrows(IllegalArgumentException.class, () -> 
            builder.where(field, invalidOperator, value)
        );
    }

    // Test parameter name generation and collision avoidance
    @Property
    void parameterNameGenerationAvoidsCollisions(
            @ForAll("validFieldNames") String baseField,
            @ForAll @Size(min = 10, max = 50) List<@NotEmpty String> values) {
        
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        
        // Add many conditions with the same base field name
        for (int i = 0; i < values.size(); i++) {
            builder = (DynamicQueryBuilder<Object>) builder.where(baseField, values.get(i));
            if (i < values.size() - 1) {
                builder = (DynamicQueryBuilder<Object>) builder.and();
            }
        }
        
        List<Predicate> predicates = builder.getPredicates();
        
        // Collect all parameter names from all predicates
        Set<String> allParamNames = new HashSet<>();
        for (Predicate predicate : predicates) {
            allParamNames.addAll(predicate.getParameters().keySet());
        }
        
        // All parameter names should be unique (no collisions)
        Map<String, Object> allParams = new HashMap<>(); 
        for (Predicate predicate : predicates) {
            Map<String, Object> predicateParams = predicate.getParameters();
            for (Map.Entry<String, Object> entry : predicateParams.entrySet()) {
                assertFalse(allParams.containsKey(entry.getKey()), 
                    "Parameter name collision detected: " + entry.getKey());
                allParams.put(entry.getKey(), entry.getValue());
            }
        }
        
        assertEquals(allParams.size(), allParamNames.size());
    }

    // Test edge cases with special values
    @Property
    void predicatesHandleSpecialValues(
            @ForAll("validFieldNames") String field,
            @ForAll("validParameterNames") String paramName) {
        
        Object[] specialValues = {
            null, "", 0, 0.0, false, Double.NaN, Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY, Long.MAX_VALUE, Long.MIN_VALUE,
            Integer.MAX_VALUE, Integer.MIN_VALUE
        };
        
        for (Object specialValue : specialValues) {
            // Test SimplePredicate with special values
            SimplePredicate simple = new SimplePredicate(field, "=", specialValue, paramName + "_s");
            String sql = simple.toSQL();
            assertNotNull(sql);
            assertFalse(sql.isEmpty());
            
            // Test BetweenPredicate with special values
            BetweenPredicate between = new BetweenPredicate(field, specialValue, specialValue, 
                paramName + "_b1", paramName + "_b2");
            String betweenSQL = between.toSQL();
            assertNotNull(betweenSQL);
            assertFalse(betweenSQL.isEmpty());
        }
    }

    // Test predicate immutability with concurrent access
    @Property
    void predicatesAreThreadSafeAndImmutable(
            @ForAll("validFieldNames") String field,
            @ForAll("validValues") Object value,
            @ForAll("validParameterNames") String paramName,
            @ForAll @IntRange(min = 2, max = 10) int threadCount) throws InterruptedException {
        
        SimplePredicate predicate = new SimplePredicate(field, "=", value, paramName);
        
        // Test concurrent access to predicate methods
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(threadCount);
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(threadCount);
        java.util.concurrent.atomic.AtomicInteger successCount = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.ConcurrentHashMap<Integer, String> sqlResults = new java.util.concurrent.ConcurrentHashMap<>();
        java.util.concurrent.ConcurrentHashMap<Integer, Map<String, Object>> paramResults = new java.util.concurrent.ConcurrentHashMap<>();
        
        try {
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    try {
                        // Multiple calls from different threads
                        String sql1 = predicate.toSQL();
                        String sql2 = predicate.toSQL();
                        Map<String, Object> params1 = predicate.getParameters();
                        Map<String, Object> params2 = predicate.getParameters();
                        
                        // Results should be consistent
                        assertEquals(sql1, sql2);
                        assertEquals(params1, params2);
                        
                        sqlResults.put(threadId, sql1);
                        paramResults.put(threadId, params1);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        // Should not happen for thread-safe operations
                        fail("Thread safety violation: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            latch.await();
            
            // All threads should have succeeded
            assertEquals(threadCount, successCount.get());
            
            // All results should be identical (immutable)
            Set<String> uniqueSQLResults = new HashSet<>(sqlResults.values());
            assertEquals(1, uniqueSQLResults.size());
            
            Set<Map<String, Object>> uniqueParamResults = new HashSet<>(paramResults.values());
            assertEquals(1, uniqueParamResults.size());
            
        } finally {
            executor.shutdown();
        }
    }
}