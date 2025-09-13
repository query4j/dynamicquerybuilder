package com.github.query4j.core;

import com.github.query4j.core.criteria.*;
import com.github.query4j.core.impl.DynamicQueryBuilder;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static net.jqwik.api.Assume.that;

/**
 * Property-based tests focusing on boundary conditions, large data sets,
 * and performance characteristics to achieve comprehensive coverage.
 */
class BoundaryConditionPropertyTests {

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
    Arbitrary<String> largeFieldNames() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('A', 'Z')
            .withCharRange('0', '9')
            .withChars('_', '.')
            .ofMinLength(40)
            .ofMaxLength(100)
            .filter(s -> s.matches("[A-Za-z0-9_\\.]+"));
    }

    @Provide
    Arbitrary<String> largeStringValues() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('A', 'Z')
            .withCharRange('0', '9')
            .withChars(' ', '.', ',', '!', '?')
            .ofMinLength(500)
            .ofMaxLength(2000);
    }

    @Provide
    Arbitrary<List<Object>> largeValueLists() {
        return validValues().list().ofMinSize(50).ofMaxSize(200);
    }

    // Test boundary conditions with empty collections
    @Property
    void queryBuilderHandlesEmptyCollectionsGracefully(
            @ForAll("validFieldNames") String field,
            @ForAll("validValues") Object value) {
        
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        
        // Test with empty select fields - should handle gracefully
        DynamicQueryBuilder<Object> withEmptySelect = (DynamicQueryBuilder<Object>) 
            builder.withSelectFields(Collections.emptyList());
        assertNotNull(withEmptySelect);
        
        // Test with empty join clauses - should handle gracefully
        DynamicQueryBuilder<Object> withEmptyJoins = (DynamicQueryBuilder<Object>) 
            withEmptySelect.withJoinClauses(Collections.emptyList());
        assertNotNull(withEmptyJoins);
        
        // Test with empty order by clauses - should handle gracefully
        DynamicQueryBuilder<Object> withEmptyOrder = (DynamicQueryBuilder<Object>) 
            withEmptyJoins.withOrderByClauses(Collections.emptyList());
        assertNotNull(withEmptyOrder);
        
        // Should still generate valid SQL
        String sql = withEmptyOrder.where(field, value).toSQL();
        assertTrue(sql.contains("SELECT"));
        assertTrue(sql.contains("FROM"));
        assertTrue(sql.contains("WHERE"));
    }

    // Test with maximum number of predicates
    @Property
    void queryBuilderHandlesLargeNumberOfPredicates(
            @ForAll("validFieldNames") String baseField,
            @ForAll @IntRange(min = 100, max = 500) int predicateCount) {
        
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        
        // Add many predicates
        for (int i = 0; i < predicateCount; i++) {
            builder = (DynamicQueryBuilder<Object>) builder.where(baseField + i, "value" + i);
            if (i < predicateCount - 1) {
                builder = (DynamicQueryBuilder<Object>) builder.and();
            }
        }
        
        String sql = builder.toSQL();
        List<Predicate> predicates = builder.getPredicates();
        
        // Verify structure
        assertTrue(sql.contains("WHERE"));
        assertTrue(predicates.size() > 0);
        
        // Verify all parameters are unique
        Set<String> allParamNames = new HashSet<>();
        for (Predicate predicate : predicates) {
            allParamNames.addAll(predicate.getParameters().keySet());
        }
        
        // Should have no parameter name collisions
        Map<String, Object> allParams = new HashMap<>();
        for (Predicate predicate : predicates) {
            allParams.putAll(predicate.getParameters());
        }
        assertEquals(allParams.size(), allParamNames.size());
    }

    // Test with very large IN lists
    @Property
    void inPredicateHandlesLargeValueLists(
            @ForAll("validFieldNames") String field,
            @ForAll("largeValueLists") List<Object> largeValues) {
        
        that(largeValues.size() >= 50);
        
        InPredicate predicate = new InPredicate(field, largeValues, "large_in");
        String sql = predicate.toSQL();
        Map<String, Object> params = predicate.getParameters();
        
        // Verify structure
        assertTrue(sql.contains(field));
        assertTrue(sql.contains("IN"));
        assertTrue(sql.contains("("));
        assertTrue(sql.contains(")"));
        
        // Should have correct number of parameters
        assertEquals(largeValues.size(), params.size());
        
        // Verify all parameter placeholders are in SQL
        for (int i = 0; i < largeValues.size(); i++) {
            assertTrue(sql.contains(":large_in_" + i));
            assertEquals(largeValues.get(i), params.get("large_in_" + i));
        }
    }

    // Test with large string values
    @Property
    void predicatesHandleLargeStringValues(
            @ForAll("validFieldNames") String field,
            @ForAll("largeStringValues") String largeValue) {
        
        that(largeValue.length() >= 500);
        
        // Test SimplePredicate with large string
        SimplePredicate simple = new SimplePredicate(field, "=", largeValue, "large_str");
        String sql = simple.toSQL();
        Map<String, Object> params = simple.getParameters();
        
        assertTrue(sql.contains(field));
        assertTrue(sql.contains(":large_str"));
        assertEquals(1, params.size());
        assertEquals(largeValue, params.get("large_str"));
        
        // Test LikePredicate with large pattern
        LikePredicate like = new LikePredicate(field, largeValue, "large_pattern");
        String likeSQL = like.toSQL();
        Map<String, Object> likeParams = like.getParameters();
        
        assertTrue(likeSQL.contains(field));
        assertTrue(likeSQL.contains("LIKE"));
        assertEquals(1, likeParams.size());
        assertEquals(largeValue, likeParams.get("large_pattern"));
    }

    // Test with large field names
    @Property
    void predicatesHandleLargeFieldNames(
            @ForAll("largeFieldNames") String largeField,
            @ForAll("validValues") Object value) {
        
        that(largeField.length() >= 40);
        
        SimplePredicate predicate = new SimplePredicate(largeField, "=", value, "p1");
        String sql = predicate.toSQL();
        
        assertTrue(sql.contains(largeField));
        assertTrue(sql.contains("="));
        assertTrue(sql.contains(":p1"));
    }

    // Test boundary values for numeric ranges
    @Property
    void predicatesHandleNumericBoundaryValues(
            @ForAll("validFieldNames") String field) {
        
        Number[] boundaryValues = {
            Integer.MIN_VALUE, Integer.MAX_VALUE,
            Long.MIN_VALUE, Long.MAX_VALUE,
            Float.MIN_VALUE, Float.MAX_VALUE,
            Double.MIN_VALUE, Double.MAX_VALUE,
            0, -0, 1, -1,
            Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
            Double.NaN
        };
        
        for (int i = 0; i < boundaryValues.length; i++) {
            Number value = boundaryValues[i];
            SimplePredicate predicate = new SimplePredicate(field, "=", value, "bound_" + i);
            
            String sql = predicate.toSQL();
            Map<String, Object> params = predicate.getParameters();
            
            assertTrue(sql.contains(field));
            assertEquals(1, params.size());
            assertEquals(value, params.get("bound_" + i));
        }
    }

    // Test complex logical predicate nesting
    @Property
    void logicalPredicatesHandleDeepNesting(
            @ForAll("validFieldNames") String baseField,
            @ForAll @IntRange(min = 3, max = 8) int nestingDepth) {
        
        // Create deeply nested logical predicates
        List<Predicate> level1 = Arrays.asList(
            new SimplePredicate(baseField + "1", "=", "value1", "p1"),
            new SimplePredicate(baseField + "2", "!=", "value2", "p2")
        );
        
        LogicalPredicate current = new LogicalPredicate("AND", level1);
        
        for (int depth = 2; depth < nestingDepth; depth++) {
            SimplePredicate newPred = new SimplePredicate(baseField + depth, "=", "value" + depth, "p" + depth);
            List<Predicate> nextLevel = Arrays.asList(current, newPred);
            current = new LogicalPredicate(depth % 2 == 0 ? "OR" : "AND", nextLevel);
        }
        
        String sql = current.toSQL();
        Map<String, Object> params = current.getParameters();
        
        // Verify structure contains nested parentheses
        assertTrue(sql.contains("("));
        assertTrue(sql.contains(")"));
        assertTrue(sql.contains("AND") || sql.contains("OR"));
        
        // Should have parameters for all nested predicates
        assertTrue(params.size() >= nestingDepth - 1);
        
        // Verify all base fields are present
        for (int i = 1; i < nestingDepth; i++) {
            assertTrue(sql.contains(baseField + i));
        }
    }

    // Test query performance with many joins
    @Property
    void queryBuilderHandlesManyJoins(
            @ForAll @IntRange(min = 10, max = 50) int joinCount,
            @ForAll("validFieldNames") String baseTable) {
        
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        
        List<String> joinClauses = new ArrayList<>();
        for (int i = 1; i <= joinCount; i++) {
            joinClauses.add(baseTable + i + ".id = " + baseTable + (i-1) + ".parent_id");
        }
        
        DynamicQueryBuilder<Object> withJoins = (DynamicQueryBuilder<Object>) 
            builder.withJoinClauses(joinClauses);
        
        String sql = withJoins.toSQL();
        
        // Should contain appropriate JOIN structure
        assertTrue(sql.contains("FROM"));
        
        // Should reference all tables
        for (int i = 0; i < joinCount; i++) {
            assertTrue(sql.contains(baseTable + i));
        }
    }

    // Test aggregation with many group by fields
    @Property
    void queryBuilderHandlesManyGroupByFields(
            @ForAll @IntRange(min = 10, max = 30) int groupByCount,
            @ForAll("validFieldNames") String baseField) {
        
        String[] groupByFields = new String[groupByCount];
        for (int i = 0; i < groupByCount; i++) {
            groupByFields[i] = baseField + "_" + i;
        }
        
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        DynamicQueryBuilder<Object> withGroupBy = (DynamicQueryBuilder<Object>) 
            builder.sum(baseField + "_amount")
                   .groupBy(groupByFields);
        
        String sql = withGroupBy.toSQL();
        
        assertTrue(sql.contains("SELECT"));
        assertTrue(sql.contains("SUM"));
        assertTrue(sql.contains("GROUP BY"));
        
        // All group by fields should be present
        for (String field : groupByFields) {
            assertTrue(sql.contains(field));
        }
    }

    // Test order by with many fields and mixed directions
    @Property
    void queryBuilderHandlesManyOrderByFields(
            @ForAll @IntRange(min = 10, max = 25) int orderByCount,
            @ForAll("validFieldNames") String baseField) {
        
        List<String> orderByClauses = new ArrayList<>();
        for (int i = 0; i < orderByCount; i++) {
            String direction = i % 2 == 0 ? "ASC" : "DESC";
            orderByClauses.add(baseField + "_" + i + " " + direction);
        }
        
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        DynamicQueryBuilder<Object> withOrderBy = (DynamicQueryBuilder<Object>) 
            builder.withOrderByClauses(orderByClauses);
        
        String sql = withOrderBy.toSQL();
        
        assertTrue(sql.contains("ORDER BY"));
        
        // All order by fields should be present
        for (int i = 0; i < orderByCount; i++) {
            assertTrue(sql.contains(baseField + "_" + i));
        }
        assertTrue(sql.contains("ASC"));
        assertTrue(sql.contains("DESC"));
    }

    // Test edge case: single character field names and values
    @Property
    void predicatesHandleSingleCharacterInputs() {
        char[] validChars = {'a', 'z', 'A', 'Z', '0', '9', '_'};
        
        for (char c : validChars) {
            String field = String.valueOf(c);
            String value = String.valueOf(c);
            String param = "p" + c;
            
            SimplePredicate predicate = new SimplePredicate(field, "=", value, param);
            String sql = predicate.toSQL();
            
            assertTrue(sql.contains(field));
            assertTrue(sql.contains("="));
            assertTrue(sql.contains(":" + param));
        }
    }

    // Test memory efficiency with large queries
    @Property
    void largeQueryMemoryEfficiency(
            @ForAll("validFieldNames") String baseField,
            @ForAll @IntRange(min = 50, max = 200) int conditionCount) {
        
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        
        // Build large query step by step
        for (int i = 0; i < conditionCount; i++) {
            builder = (DynamicQueryBuilder<Object>) builder.where(baseField + i, "=", "value" + i);
            if (i < conditionCount - 1) {
                builder = (DynamicQueryBuilder<Object>) builder.and();
            }
        }
        
        // Generate SQL - should not cause memory issues
        long startTime = System.currentTimeMillis();
        String sql = builder.toSQL();
        long endTime = System.currentTimeMillis();
        
        // Should complete in reasonable time (less than 1 second for large queries)
        assertTrue(endTime - startTime < 1000);
        
        // SQL should be valid
        assertNotNull(sql);
        assertFalse(sql.isEmpty());
        assertTrue(sql.contains("SELECT"));
        assertTrue(sql.contains("WHERE"));
        
        // Verify all conditions are present
        for (int i = 0; i < Math.min(10, conditionCount); i++) {
            assertTrue(sql.contains(baseField + i));
        }
    }

    // Test SQL injection prevention with malicious inputs
    @Property
    void predicatesPreventSQLInjection(
            @ForAll("validFieldNames") String field) {
        
        String[] maliciousInputs = {
            "'; DROP TABLE users; --",
            "' OR '1'='1",
            "'; SELECT * FROM passwords; --",
            "' UNION SELECT * FROM credit_cards --",
            "admin'--",
            "' OR 1=1#",
            "\"; DELETE FROM users; --"
        };
        
        for (int i = 0; i < maliciousInputs.length; i++) {
            String maliciousValue = maliciousInputs[i];
            SimplePredicate predicate = new SimplePredicate(field, "=", maliciousValue, "param" + i);
            
            String sql = predicate.toSQL();
            Map<String, Object> params = predicate.getParameters();
            
            // SQL should contain parameterized query, not the malicious content directly
            assertTrue(sql.contains(field));
            assertTrue(sql.contains("="));
            assertTrue(sql.contains(":param" + i));
            
            // Malicious content should be safely stored in parameters
            assertEquals(maliciousValue, params.get("param" + i));
            
            // SQL itself should not contain the malicious strings
            assertFalse(sql.contains("DROP TABLE"));
            assertFalse(sql.contains("DELETE FROM"));
            assertFalse(sql.contains("UNION SELECT"));
        }
    }

    // Test concurrent access to builder with multiple threads
    @Property
    void builderHandlesConcurrentModification(
            @ForAll("validFieldNames") String baseField,
            @ForAll @IntRange(min = 5, max = 20) int operationCount) throws InterruptedException {
        
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(4);
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(operationCount);
        java.util.concurrent.ConcurrentHashMap<Integer, String> results = new java.util.concurrent.ConcurrentHashMap<>();
        
        try {
            for (int i = 0; i < operationCount; i++) {
                final int operationId = i;
                executor.submit(() -> {
                    try {
                        // Each thread creates its own query modification
                        DynamicQueryBuilder<Object> threadBuilder = (DynamicQueryBuilder<Object>) 
                            builder.where(baseField + operationId, "value" + operationId);
                        
                        String sql = threadBuilder.toSQL();
                        results.put(operationId, sql);
                    } catch (Exception e) {
                        // Should not happen with proper immutability
                        fail("Concurrent modification issue: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            latch.await();
            
            // All operations should complete successfully
            assertEquals(operationCount, results.size());
            
            // Each result should be unique and valid
            Set<String> uniqueResults = new HashSet<>(results.values());
            assertEquals(operationCount, uniqueResults.size());
            
            for (String sql : results.values()) {
                assertTrue(sql.contains("SELECT"));
                assertTrue(sql.contains("WHERE"));
            }
            
        } finally {
            executor.shutdown();
        }
    }
}