package com.github.query4j.core.impl;

import com.github.query4j.core.QueryBuilder;
import com.github.query4j.core.QueryStats;
import com.github.query4j.core.DynamicQueryException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Additional tests to achieve 95%+ code coverage for DynamicQueryBuilder.
 * Focuses on testing methods not covered by main test suite.
 */
@DisplayName("DynamicQueryBuilder Coverage Tests")
class DynamicQueryBuilderCoverageTest {

    private DynamicQueryBuilder<TestEntity> builder;

    @BeforeEach
    void setUp() {
        builder = new DynamicQueryBuilder<>(TestEntity.class);
    }

    @Nested
    @DisplayName("Aggregation Methods")
    class AggregationMethodTests {

        @Test
        @DisplayName("should support count with field")
        void shouldSupportCountWithField() {
            QueryBuilder<TestEntity> result = builder.count("id");
            assertNotNull(result);
            String sql = result.toSQL();
            assertTrue(sql.contains("COUNT(id)"));
        }

        @Test
        @DisplayName("should support sum aggregation")
        void shouldSupportSumAggregation() {
            QueryBuilder<TestEntity> result = builder.sum("amount");
            assertNotNull(result);
            String sql = result.toSQL();
            assertTrue(sql.contains("SUM(amount)"));
        }

        @Test
        @DisplayName("should support avg aggregation")
        void shouldSupportAvgAggregation() {
            QueryBuilder<TestEntity> result = builder.avg("amount");
            assertNotNull(result);
            String sql = result.toSQL();
            assertTrue(sql.contains("AVG(amount)"));
        }

        @Test
        @DisplayName("should support min aggregation")
        void shouldSupportMinAggregation() {
            QueryBuilder<TestEntity> result = builder.min("amount");
            assertNotNull(result);
            String sql = result.toSQL();
            assertTrue(sql.contains("MIN(amount)"));
        }

        @Test
        @DisplayName("should support max aggregation")
        void shouldSupportMaxAggregation() {
            QueryBuilder<TestEntity> result = builder.max("amount");
            assertNotNull(result);
            String sql = result.toSQL();
            assertTrue(sql.contains("MAX(amount)"));
        }

        @Test
        @DisplayName("should support countAll aggregation")
        void shouldSupportCountAllAggregation() {
            QueryBuilder<TestEntity> result = builder.countAll();
            assertNotNull(result);
            String sql = result.toSQL();
            assertTrue(sql.contains("COUNT(*)"));
        }
    }

    @Nested
    @DisplayName("Join Methods")
    class JoinMethodTests {

        @Test
        @DisplayName("should support right join")
        void shouldSupportRightJoin() {
            QueryBuilder<TestEntity> result = builder.rightJoin("orders");
            assertNotNull(result);
            String sql = result.toSQL();
            assertTrue(sql.contains("RIGHT JOIN"));
            assertTrue(sql.contains("orders"));
        }

        @Test
        @DisplayName("should support fetch join")
        void shouldSupportFetchJoin() {
            QueryBuilder<TestEntity> result = builder.fetch("orders");
            assertNotNull(result);
            String sql = result.toSQL();
            assertTrue(sql.contains("FETCH"));
            assertTrue(sql.contains("orders"));
        }
    }

    @Nested
    @DisplayName("Having Clause Methods")
    class HavingMethodTests {

        @Test
        @DisplayName("should support having conditions")
        void shouldSupportHavingConditions() {
            QueryBuilder<TestEntity> result = builder
                .groupBy("category")
                .having("COUNT(*)", ">", 5);
            assertNotNull(result);
            String sql = result.toSQL();
            assertTrue(sql.contains("HAVING"));
            assertTrue(sql.contains("COUNT(*)"));
        }

        @Test
        @DisplayName("should get having predicates")
        void shouldGetHavingPredicates() {
            QueryBuilder<TestEntity> result = builder
                .groupBy("category")
                .having("COUNT(*)", ">", 5);
            
            // Test the getHavingPredicates method
            DynamicQueryBuilder<TestEntity> dynBuilder = (DynamicQueryBuilder<TestEntity>) result;
            assertNotNull(dynBuilder.getHavingPredicates());
        }
    }

    @Nested
    @DisplayName("Cache Methods")
    class CacheMethodTests {

        @Test
        @DisplayName("should enable caching")
        void shouldEnableCaching() {
            QueryBuilder<TestEntity> result = builder.cached();
            assertNotNull(result);
            assertNotSame(builder, result);
        }

        @Test
        @DisplayName("should enable caching with region name")
        void shouldEnableCachingWithRegionName() {
            QueryBuilder<TestEntity> result = builder.cached("userCache");
            assertNotNull(result);
            assertNotSame(builder, result);
        }

        @Test
        @DisplayName("should enable caching with TTL")
        void shouldEnableCachingWithTTL() {
            QueryBuilder<TestEntity> result = builder.cached(3600L);
            assertNotNull(result);
            assertNotSame(builder, result);
        }
    }

    @Nested
    @DisplayName("Parameter Management")
    class ParameterManagementTests {

        @Test
        @DisplayName("should get parameters map")
        void shouldGetParametersMap() {
            QueryBuilder<TestEntity> result = builder
                .where("name", "John")
                .whereIn("status", Arrays.asList("ACTIVE", "PENDING"));
            
            DynamicQueryBuilder<TestEntity> dynBuilder = (DynamicQueryBuilder<TestEntity>) result;
            Map<String, Object> params = dynBuilder.getParameters();
            assertNotNull(params);
            assertFalse(params.isEmpty());
        }

        @Test
        @DisplayName("should handle empty parameters map")
        void shouldHandleEmptyParametersMap() {
            QueryBuilder<TestEntity> result = builder.parameters(Collections.emptyMap());
            assertNotNull(result);
            assertSame(builder, result); // Should return same instance for empty params
        }

        @Test
        @DisplayName("should validate parameter names")
        void shouldValidateParameterNames() {
            assertThrows(IllegalArgumentException.class, () -> {
                builder.parameter("", "value");
            });
            
            assertThrows(IllegalArgumentException.class, () -> {
                builder.parameter("   ", "value");
            });
        }

        @Test
        @DisplayName("should validate hint names")
        void shouldValidateHintNames() {
            assertThrows(IllegalArgumentException.class, () -> {
                builder.hint("", "value");
            });
            
            assertThrows(IllegalArgumentException.class, () -> {
                builder.hint("   ", "value");
            });
        }
    }

    @Nested
    @DisplayName("Validation Methods")
    class ValidationMethodTests {

        @Test
        @DisplayName("should validate field names")
        void shouldValidateFieldNames() {
            // Valid field names should work
            assertDoesNotThrow(() -> builder.where("validField", "value"));
            assertDoesNotThrow(() -> builder.where("valid_field", "value"));
            assertDoesNotThrow(() -> builder.where("validField123", "value"));
            assertDoesNotThrow(() -> builder.where("table.field", "value"));
            
            // Invalid field names should throw
            assertThrows(IllegalArgumentException.class, () -> {
                builder.where("", "value");
            });
        }

        @Test
        @DisplayName("should validate operators")
        void shouldValidateOperators() {
            // Valid operators should work
            assertDoesNotThrow(() -> builder.where("field", "=", "value"));
            assertDoesNotThrow(() -> builder.where("field", ">", 10));
            assertDoesNotThrow(() -> builder.where("field", "LIKE", "pattern%"));
            assertDoesNotThrow(() -> builder.where("field", "!=", "value"));
            assertDoesNotThrow(() -> builder.where("field", ">=", 10));
            
            // Invalid operators should throw
            assertThrows(IllegalArgumentException.class, () -> {
                builder.where("field", "", "value");
            });
            
            assertThrows(IllegalArgumentException.class, () -> {
                builder.where("field", "INVALID_OP", "value");
            });
        }

        @Test
        @DisplayName("should validate fetch size")
        void shouldValidateFetchSize() {
            // Valid fetch sizes should work
            assertDoesNotThrow(() -> builder.fetchSize(100));
            assertDoesNotThrow(() -> builder.fetchSize(1000));
            
            // Invalid fetch sizes should throw
            assertThrows(IllegalArgumentException.class, () -> {
                builder.fetchSize(-1);
            });
            
            assertThrows(IllegalArgumentException.class, () -> {
                builder.fetchSize(0);
            });
        }

        @Test
        @DisplayName("should validate timeout")
        void shouldValidateTimeout() {
            // Valid timeouts should work
            assertDoesNotThrow(() -> builder.timeout(30));
            assertDoesNotThrow(() -> builder.timeout(300));
            
            // Invalid timeouts should throw
            assertThrows(IllegalArgumentException.class, () -> {
                builder.timeout(-1);
            });
            
            assertThrows(IllegalArgumentException.class, () -> {
                builder.timeout(0);
            });
        }
    }

    @Nested
    @DisplayName("SQL Generation Edge Cases")
    class SQLGenerationEdgeCaseTests {

        @Test
        @DisplayName("should handle complex query with all features")
        void shouldHandleComplexQueryWithAllFeatures() {
            QueryBuilder<TestEntity> complexQuery = builder
                .select("name", "email")
                .join("orders")
                .leftJoin("profiles")
                .where("active", true)
                .whereIn("status", Arrays.asList("ACTIVE", "PENDING"))
                .whereLike("name", "John%")
                .whereBetween("age", 18, 65)
                .whereIsNotNull("email")
                .groupBy("name", "email")
                .having("COUNT(*)", ">", 1)
                .orderBy("name")
                .orderByDescending("email")
                .page(1, 20)
                .fetchSize(100)
                .timeout(30)
                .cached()
                .hint("USE_INDEX", "idx_name");
            
            String sql = complexQuery.toSQL();
            assertNotNull(sql);
            assertFalse(sql.trim().isEmpty());
            
            // Verify main clauses are present
            assertTrue(sql.contains("SELECT"));
            assertTrue(sql.contains("FROM"));
        }

        @Test
        @DisplayName("should handle native SQL with parameters")
        void shouldHandleNativeSQLWithParameters() {
            QueryBuilder<TestEntity> nativeQuery = builder
                .nativeQuery("SELECT * FROM users WHERE name = :name AND age > :minAge")
                .parameter("name", "John")
                .parameter("minAge", 18);
            
            String sql = nativeQuery.toSQL();
            assertEquals("SELECT * FROM users WHERE name = :name AND age > :minAge", sql);
            
            Map<String, Object> params = ((DynamicQueryBuilder<TestEntity>) nativeQuery).getParameters();
            assertTrue(params.containsKey("name"));
            assertTrue(params.containsKey("minAge"));
            assertEquals("John", params.get("name"));
            assertEquals(18, params.get("minAge"));
        }
    }

    /**
     * Test entity class for testing purposes
     */
    static class TestEntity {
        private Long id;
        private String name;
        private String email;
        private boolean active;
        private String status;
        private int age;
        private String category;
        private double amount;
        
        // Getters and setters omitted for brevity
    }
}