package com.github.query4j.core;

import com.github.query4j.core.criteria.*;
import com.github.query4j.core.impl.DynamicQueryBuilder;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.NotEmpty;
import net.jqwik.api.constraints.Size;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static net.jqwik.api.Assume.that;

/**
 * Comprehensive property-based tests for QueryBuilder focusing on untested methods,
 * edge cases, and complex scenarios to achieve 95%+ coverage.
 */
class QueryBuilderPropertyTests {

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
    Arbitrary<String> validPatterns() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('A', 'Z')
            .withCharRange('0', '9')
            .withChars('%', '_', ' ', '-')
            .ofMaxLength(100);
    }

    @Provide
    Arbitrary<String> validJoinClauses() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('A', 'Z')
            .withCharRange('0', '9')
            .withChars('_', '.', ' ', '=')
            .ofMinLength(5)
            .ofMaxLength(100)
            .filter(s -> s.contains("=") && s.contains("."));
    }

    // Test withEntityClass method (currently 0% coverage)
    @Property
    void withEntityClassCreatesNewInstanceWithCorrectEntityClass(
            @ForAll("validFieldNames") String field,
            @ForAll("validValues") Object value) {
        
        DynamicQueryBuilder<String> original = new DynamicQueryBuilder<>(String.class);
        DynamicQueryBuilder<String> modified = original.withEntityClass(String.class);
        
        // Should be different instance
        assertNotSame(original, modified);
        
        // Original should be unchanged
        String originalSQL = original.toSQL();
        assertTrue(originalSQL.contains("String"));
        
        // Modified should work with same entity class
        String modifiedSQL = ((DynamicQueryBuilder<String>) modified.where(field, value)).toSQL();
        assertTrue(modifiedSQL.contains("String"));
    }

    // Test withPredicates method (currently 0% coverage)
    @Property
    void withPredicatesCreatesBuilderWithGivenPredicates(
            @ForAll("validFieldNames") String field1,
            @ForAll("validFieldNames") String field2,
            @ForAll("validValues") Object value1,
            @ForAll("validValues") Object value2) {
        
        that(!field1.equals(field2));
        
        List<Predicate> predicates = Arrays.asList(
            new SimplePredicate(field1, "=", value1, "p1"),
            new SimplePredicate(field2, "!=", value2, "p2")
        );
        
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        DynamicQueryBuilder<Object> withPredicates = (DynamicQueryBuilder<Object>) builder.withPredicates(predicates);
        
        // Should be different instance
        assertNotSame(builder, withPredicates);
        
        // Should contain the predicates
        String sql = withPredicates.toSQL();
        assertTrue(sql.contains(field1));
        assertTrue(sql.contains(field2));
        assertTrue(sql.contains("WHERE"));
    }

    // Test customFunction method (54% coverage - improve)
    @Property
    void customFunctionGeneratesValidSQLWithParameters(
            @ForAll("validFieldNames") String functionName,
            @ForAll("validFieldNames") String sqlExpression,
            @ForAll("validValueLists") List<Object> parameters) {
        
        Object[] paramArray = parameters.toArray();
        
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        DynamicQueryBuilder<Object> withFunction = (DynamicQueryBuilder<Object>) 
            builder.customFunction(functionName, sqlExpression, paramArray);
        
        // Should be different instance
        assertNotSame(builder, withFunction);
        
        // SQL should contain the function
        String sql = withFunction.toSQL();
        assertTrue(sql.contains(functionName) || sql.contains(sqlExpression));
    }

    // Test builder methods with collections (82% coverage - improve)
    @Property
    void builderMethodsWithCollectionsPreservePreviousState(
            @ForAll("validFieldNames") String selectField,
            @ForAll("validJoinClauses") String joinClause,
            @ForAll("validFieldNames") String orderField,
            @ForAll("validFieldNames") String groupField) {
        
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        
        // Add select fields
        DynamicQueryBuilder<Object> withSelect = (DynamicQueryBuilder<Object>) 
            builder.withSelectFields(Arrays.asList(selectField));
        assertNotSame(builder, withSelect);
        
        // Add join clauses 
        DynamicQueryBuilder<Object> withJoin = (DynamicQueryBuilder<Object>) 
            withSelect.withJoinClauses(Arrays.asList(joinClause));
        assertNotSame(withSelect, withJoin);
        
        // Add order by clauses
        DynamicQueryBuilder<Object> withOrder = (DynamicQueryBuilder<Object>) 
            withJoin.withOrderByClauses(Arrays.asList(orderField + " ASC"));
        assertNotSame(withJoin, withOrder);
        
        // Add group by clauses
        DynamicQueryBuilder<Object> withGroup = (DynamicQueryBuilder<Object>) 
            withOrder.withGroupByClauses(Arrays.asList(groupField));
        assertNotSame(withOrder, withGroup);
        
        // Final SQL should contain all elements
        String finalSQL = withGroup.toSQL();
        assertTrue(finalSQL.contains(selectField));
        assertTrue(finalSQL.contains("ORDER BY"));
        assertTrue(finalSQL.contains("GROUP BY"));
    }

    // Test having predicates with collections (82% coverage - improve)
    @Property
    void withHavingPredicatesPreservesImmutability(
            @ForAll("validFieldNames") String aggregatedField,
            @ForAll("validOperators") String operator,
            @ForAll("validValues") Object value) {
        
        HavingPredicate havingPredicate = new HavingPredicate(
            "SUM(" + aggregatedField + ")", operator, value, "h1"
        );
        
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        DynamicQueryBuilder<Object> withHaving = (DynamicQueryBuilder<Object>) 
            builder.withHavingPredicates(Arrays.asList(havingPredicate));
        
        // Should be different instance
        assertNotSame(builder, withHaving);
        
        // Original should not have HAVING clause
        String originalSQL = builder.toSQL();
        assertFalse(originalSQL.contains("HAVING"));
        
        // Modified should have HAVING clause
        String modifiedSQL = withHaving.toSQL();
        assertTrue(modifiedSQL.contains("HAVING"));
    }

    // Test complex query composition with all builder methods
    @Property
    void complexQueryCompositionWithAllMethods(
            @ForAll("validFieldNames") String field,
            @ForAll("validValues") Object value,
            @ForAll @IntRange(min = 1, max = 100) int limit,
            @ForAll @IntRange(min = 0, max = 50) int offset) {
        
        DynamicQueryBuilder<Object> builder = (DynamicQueryBuilder<Object>) new DynamicQueryBuilder<>(Object.class)
            .where(field, value)
            .and()
            .whereIsNotNull(field + "_status")
            .orderBy(field)
            .limit(limit)
            .offset(offset);
        
        String sql = builder.toSQL();
        
        // Verify complete SQL structure
        assertTrue(sql.contains("SELECT"));
        assertTrue(sql.contains("FROM"));
        assertTrue(sql.contains("WHERE"));
        assertTrue(sql.contains("ORDER BY"));
        assertTrue(sql.contains("LIMIT"));
        assertTrue(sql.contains("OFFSET"));
        assertTrue(sql.contains(field));
        assertTrue(sql.contains("IS NOT NULL"));
    }

    // Test whereNotIn method (90% coverage - improve)
    @Property
    void whereNotInGeneratesCorrectSQLAndParameters(
            @ForAll("validFieldNames") String field,
            @ForAll("validValueLists") List<Object> values) {
        
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        DynamicQueryBuilder<Object> withNotIn = (DynamicQueryBuilder<Object>) 
            builder.whereNotIn(field, values);
        
        String sql = withNotIn.toSQL();
        
        // Should contain NOT IN clause
        assertTrue(sql.contains(field));
        assertTrue(sql.contains("NOT IN"));
        assertTrue(sql.contains("("));
        assertTrue(sql.contains(")"));
        
        // Should have correct number of parameter placeholders
        long paramCount = sql.chars().filter(ch -> ch == ':').count();
        assertEquals(values.size(), paramCount);
    }

    // Test whereNotLike method (88% coverage - improve)
    @Property
    void whereNotLikeGeneratesCorrectSQLAndParameters(
            @ForAll("validFieldNames") String field,
            @ForAll("validPatterns") String pattern) {
        
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        DynamicQueryBuilder<Object> withNotLike = (DynamicQueryBuilder<Object>) 
            builder.whereNotLike(field, pattern);
        
        String sql = withNotLike.toSQL();
        
        // Should contain NOT LIKE clause
        assertTrue(sql.contains(field));
        assertTrue(sql.contains("NOT LIKE"));
        assertTrue(sql.contains(":"));
    }

    // Test parameter generation uniqueness
    @Property
    void parameterGenerationIsUnique(
            @ForAll("validFieldNames") String baseField,
            @ForAll @Size(min = 5, max = 20) List<@NotEmpty String> suffixes) {
        
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        
        // Add multiple conditions with similar field names
        for (int i = 0; i < suffixes.size(); i++) {
            String field = baseField + "_" + suffixes.get(i);
            builder = (DynamicQueryBuilder<Object>) builder.where(field, "value" + i);
            if (i < suffixes.size() - 1) {
                builder = (DynamicQueryBuilder<Object>) builder.and();
            }
        }
        
        String sql = builder.toSQL();
        List<Predicate> predicates = builder.getPredicates();
        
        // Collect all parameter names
        Set<String> paramNames = new HashSet<>();
        for (Predicate predicate : predicates) {
            paramNames.addAll(predicate.getParameters().keySet());
        }
        
        // All parameter names should be unique
        Map<String, Object> allParams = new HashMap<>();
        for (Predicate predicate : predicates) {
            allParams.putAll(predicate.getParameters());
        }
        
        assertEquals(allParams.size(), paramNames.size());
        assertTrue(paramNames.size() >= suffixes.size());
    }

    // Test subquery methods (in/notIn with QueryBuilder - 73% coverage)
    @Property
    void subQueryMethodsGenerateValidSQL(
            @ForAll("validFieldNames") String mainField,
            @ForAll("validFieldNames") String subField,
            @ForAll("validValues") Object subValue) {
        
        // Create subquery
        DynamicQueryBuilder<Object> subQuery = (DynamicQueryBuilder<Object>) new DynamicQueryBuilder<>(Object.class)
            .select(subField)
            .where(subField, subValue);
        
        // Test IN with subquery
        DynamicQueryBuilder<Object> mainQueryIn = (DynamicQueryBuilder<Object>) new DynamicQueryBuilder<>(Object.class)
            .where(mainField, "!=", null)
            .and()
            .in(mainField, subQuery);
        
        String sqlIn = mainQueryIn.toSQL();
        assertTrue(sqlIn.contains(mainField));
        assertTrue(sqlIn.contains("IN"));
        assertTrue(sqlIn.contains("SELECT"));
        
        // Test NOT IN with subquery  
        DynamicQueryBuilder<Object> mainQueryNotIn = (DynamicQueryBuilder<Object>) new DynamicQueryBuilder<>(Object.class)
            .where(mainField, "!=", null)
            .and()
            .notIn(mainField, subQuery);
        
        String sqlNotIn = mainQueryNotIn.toSQL();
        assertTrue(sqlNotIn.contains(mainField));
        assertTrue(sqlNotIn.contains("NOT IN"));
        assertTrue(sqlNotIn.contains("SELECT"));
    }

    // Test advanced query configuration methods (58% coverage - improve)
    @Property
    void advancedConfigurationMethodsPreserveImmutability(
            @ForAll("validParameterNames") String paramName,
            @ForAll("validValues") Object paramValue,
            @ForAll("validFieldNames") String hintName,
            @ForAll @IntRange(min = 1, max = 10000) int fetchSize,
            @ForAll @IntRange(min = 1, max = 300) int timeout) {
        
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        
        // Test parameter method
        DynamicQueryBuilder<Object> withParam = (DynamicQueryBuilder<Object>) 
            builder.parameter(paramName, paramValue);
        assertNotSame(builder, withParam);
        
        // Test parameters method
        Map<String, Object> params = new HashMap<>();
        params.put(paramName, paramValue);
        DynamicQueryBuilder<Object> withParams = (DynamicQueryBuilder<Object>) 
            builder.parameters(params);
        assertNotSame(builder, withParams);
        
        // Test hint method
        DynamicQueryBuilder<Object> withHint = (DynamicQueryBuilder<Object>) 
            builder.hint(hintName, paramValue);
        assertNotSame(builder, withHint);
        
        // Test fetchSize method
        DynamicQueryBuilder<Object> withFetchSize = (DynamicQueryBuilder<Object>) 
            builder.fetchSize(fetchSize);
        assertNotSame(builder, withFetchSize);
        
        // Test timeout method
        DynamicQueryBuilder<Object> withTimeout = (DynamicQueryBuilder<Object>) 
            builder.timeout(timeout);
        assertNotSame(builder, withTimeout);
    }

    // Test native query method (58% coverage - improve)
    @Property
    void nativeQueryMethodPreservesImmutability(
            @ForAll("validFieldNames") String tableName,
            @ForAll("validFieldNames") String condition) {
        
        String nativeSQL = "SELECT * FROM " + tableName + " WHERE " + condition + " = :value";
        
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        DynamicQueryBuilder<Object> withNative = (DynamicQueryBuilder<Object>) 
            builder.nativeQuery(nativeSQL);
        
        // Should be different instance
        assertNotSame(builder, withNative);
        
        // Should generate SQL (though may be modified)
        String sql = withNative.toSQL();
        assertNotNull(sql);
        assertFalse(sql.isEmpty());
    }

    // Test thread safety with concurrent access
    @Property
    void builderIsThreadSafe(
            @ForAll("validFieldNames") String baseField,
            @ForAll @IntRange(min = 2, max = 10) int threadCount) throws InterruptedException {
        
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        Map<Integer, String> results = new ConcurrentHashMap<>();
        
        try {
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    try {
                        DynamicQueryBuilder<Object> threadBuilder = (DynamicQueryBuilder<Object>) 
                            builder.where(baseField + threadId, "value" + threadId);
                        String sql = threadBuilder.toSQL();
                        results.put(threadId, sql);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        // Ignore - testing thread safety
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            latch.await();
            
            // All threads should have completed successfully
            assertEquals(threadCount, successCount.get());
            assertEquals(threadCount, results.size());
            
            // Each result should be unique and valid
            Set<String> uniqueResults = new HashSet<>(results.values());
            assertEquals(threadCount, uniqueResults.size());
            
        } finally {
            executor.shutdown();
        }
    }

    // Test exists and notExists methods
    @Property
    void existsAndNotExistsGenerateValidSQL(
            @ForAll("validFieldNames") String mainField,
            @ForAll("validFieldNames") String subField,
            @ForAll("validValues") Object subValue) {
        
        // Create subquery for EXISTS
        DynamicQueryBuilder<Object> subQuery = (DynamicQueryBuilder<Object>) new DynamicQueryBuilder<>(Object.class)
            .select(subField)
            .where(subField, subValue);
        
        // Test EXISTS
        DynamicQueryBuilder<Object> withExists = (DynamicQueryBuilder<Object>) new DynamicQueryBuilder<>(Object.class)
            .where(mainField, "!=", null)
            .and()
            .exists(subQuery);
        
        String sqlExists = withExists.toSQL();
        assertTrue(sqlExists.contains("EXISTS"));
        assertTrue(sqlExists.contains("SELECT"));
        
        // Test NOT EXISTS
        DynamicQueryBuilder<Object> withNotExists = (DynamicQueryBuilder<Object>) new DynamicQueryBuilder<>(Object.class)
            .where(mainField, "!=", null)
            .and()
            .notExists(subQuery);
        
        String sqlNotExists = withNotExists.toSQL();
        assertTrue(sqlNotExists.contains("NOT EXISTS"));
        assertTrue(sqlNotExists.contains("SELECT"));
    }

    // Test caching methods
    @Property
    void cachingMethodsPreserveImmutability(
            @ForAll("validParameterNames") String cacheKey,
            @ForAll @IntRange(min = 1, max = 3600) long cacheDuration) {
        
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        
        // Test cached() method
        DynamicQueryBuilder<Object> cached1 = (DynamicQueryBuilder<Object>) builder.cached();
        assertNotSame(builder, cached1);
        
        // Test cached(String) method
        DynamicQueryBuilder<Object> cached2 = (DynamicQueryBuilder<Object>) builder.cached(cacheKey);
        assertNotSame(builder, cached2);
        
        // Test cached(long) method
        DynamicQueryBuilder<Object> cached3 = (DynamicQueryBuilder<Object>) builder.cached(cacheDuration);
        assertNotSame(builder, cached3);
    }

    // Test method chaining complexity with grouping
    @Property
    void complexMethodChainingWithGrouping(
            @ForAll("validFieldNames") String field1,
            @ForAll("validFieldNames") String field2,
            @ForAll("validValues") Object value1,
            @ForAll("validValues") Object value2) {
        
        that(!field1.equals(field2));
        
        DynamicQueryBuilder<Object> complexQuery = (DynamicQueryBuilder<Object>) new DynamicQueryBuilder<>(Object.class)
            .where(field1, value1)
            .and()
            .openGroup()
                .where(field2, "!=", value2)
                .or()
                .whereIsNull(field2)
            .closeGroup()
            .orderBy(field1)
            .limit(100);
        
        String sql = complexQuery.toSQL();
        
        // Verify complex structure
        assertTrue(sql.contains("WHERE"));
        assertTrue(sql.contains("AND"));
        assertTrue(sql.contains("("));
        assertTrue(sql.contains(")"));
        assertTrue(sql.contains("OR"));
        assertTrue(sql.contains("ORDER BY"));
        assertTrue(sql.contains("LIMIT"));
        
        // Verify fields are present
        assertTrue(sql.contains(field1));
        assertTrue(sql.contains(field2));
    }
}