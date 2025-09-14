package com.github.query4j.examples.integration;

import com.github.query4j.cache.CacheManager;
import com.github.query4j.cache.impl.CaffeineCacheManager;
import com.github.query4j.core.impl.DynamicQueryBuilder;
import com.github.query4j.examples.entity.Customer;
import com.github.query4j.optimizer.QueryOptimizer;
import com.github.query4j.optimizer.QueryOptimizerImpl;
import com.github.query4j.optimizer.OptimizerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic integration test for Query4j components without Spring Boot context.
 * Tests core functionality of dynamic query building, caching, and optimization.
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
@DisplayName("Basic Dynamic Query Integration Tests")
class BasicDynamicQueryTest {
    
    private CacheManager cacheManager;
    private QueryOptimizer queryOptimizer;
    
    @BeforeEach
    void setUp() {
        // Create cache manager
        cacheManager = CaffeineCacheManager.create(100L, 300L);
        
        // Create query optimizer
        OptimizerConfig config = OptimizerConfig.builder()
            .indexSuggestionsEnabled(true)
            .predicatePushdownEnabled(true)
            .joinReorderingEnabled(true)
            .verboseOutput(true)
            .maxAnalysisTimeMs(5000L)
            .build();
        
        queryOptimizer = new QueryOptimizerImpl(config);
    }
    
    @Test
    @DisplayName("should create dynamic query builder and generate SQL")
    void shouldCreateDynamicQueryBuilderAndGenerateSQL() {
        // Create a query builder for Customer entity
        DynamicQueryBuilder<Customer> builder = new DynamicQueryBuilder<>(Customer.class);
        
        // Build a query with multiple conditions
        builder = (DynamicQueryBuilder<Customer>) builder
            .where("region", "=", "North")
            .and()
            .where("active", "=", true)
            .and()
            .where("creditLimit", ">=", 5000.0);
        
        // Generate SQL
        String sql = builder.toSQL();
        
        // Verify the SQL structure
        assertNotNull(sql, "Generated SQL should not be null");
        assertTrue(sql.contains("SELECT"), "SQL should contain SELECT clause");
        assertTrue(sql.contains("Customer"), "SQL should reference Customer table");
        assertTrue(sql.contains("WHERE"), "SQL should contain WHERE clause");
        
        // Verify predicates were created
        assertEquals(3, builder.getPredicates().size(), "Should have 3 predicates");
        
        // Verify each predicate has parameters
        builder.getPredicates().forEach(predicate -> {
            assertNotNull(predicate.getParameters(), "Predicate should have parameters");
            assertFalse(predicate.getParameters().isEmpty(), "Predicate parameters should not be empty");
        });
        
        System.out.println("Generated SQL: " + sql);
    }
    
    @Test
    @DisplayName("should support complex query building with joins and aggregations")
    void shouldSupportComplexQueryBuildingWithJoinsAndAggregations() {
        // Create a query with joins and aggregations
        DynamicQueryBuilder<Customer> builder = new DynamicQueryBuilder<>(Customer.class);
        
        builder = (DynamicQueryBuilder<Customer>) builder
            .select("customers.name", "customers.region", "COUNT(orders.id) as orderCount")
            .join("orders")
            .where("customers.active", "=", true)
            .groupBy("customers.id", "customers.name", "customers.region")
            .having("COUNT(orders.id)", ">", 2);
        
        String sql = builder.toSQL();
        
        // Verify complex query structure
        assertNotNull(sql);
        assertTrue(sql.contains("SELECT"));
        assertTrue(sql.contains("JOIN"));
        assertTrue(sql.contains("WHERE"));
        assertTrue(sql.contains("GROUP BY"));
        assertTrue(sql.contains("HAVING"));
        
        // Verify both WHERE and HAVING predicates exist
        assertFalse(builder.getPredicates().isEmpty(), "Should have WHERE predicates");
        assertFalse(builder.getHavingPredicates().isEmpty(), "Should have HAVING predicates");
        
        System.out.println("Complex query SQL: " + sql);
    }
    
    @Test
    @DisplayName("should support pagination and ordering")
    void shouldSupportPaginationAndOrdering() {
        DynamicQueryBuilder<Customer> builder = new DynamicQueryBuilder<>(Customer.class);
        
        builder = (DynamicQueryBuilder<Customer>) builder
            .where("active", "=", true)
            .orderBy("name")
            .page(1, 10); // Second page, 10 items per page
        
        String sql = builder.toSQL();
        
        assertNotNull(sql);
        assertTrue(sql.contains("SELECT"));
        assertTrue(sql.contains("ORDER BY"));
        
        System.out.println("Paginated query SQL: " + sql);
    }
    
    @Test
    @DisplayName("should work with cache manager for query result caching")
    void shouldWorkWithCacheManagerForQueryResultCaching() {
        // Test basic cache operations
        String cacheKey = "test-query-results";
        String testData = "Sample query results";
        
        // Put data in cache
        cacheManager.put(cacheKey, testData, 600L); // 10 minutes TTL
        
        // Retrieve from cache
        Object cached = cacheManager.get(cacheKey);
        assertEquals(testData, cached, "Cached data should match");
        
        // Verify cache statistics
        assertNotNull(cacheManager.stats(), "Cache statistics should be available");
        assertTrue(cacheManager.stats().getHitCount() > 0, "Should have cache hits");
        
        // Test cache key existence
        assertTrue(cacheManager.containsKey(cacheKey), "Cache should contain the key");
        
        // Test cache invalidation
        cacheManager.invalidate(cacheKey);
        assertFalse(cacheManager.containsKey(cacheKey), "Key should be invalidated");
    }
    
    @Test
    @DisplayName("should work with query optimizer for performance suggestions")
    void shouldWorkWithQueryOptimizerForPerformanceSuggestions() {
        // Create a query that can be optimized
        DynamicQueryBuilder<Customer> builder = new DynamicQueryBuilder<>(Customer.class);
        
        final DynamicQueryBuilder<Customer> finalBuilder = (DynamicQueryBuilder<Customer>) builder
            .select("customers.name", "customers.region")
            .join("orders")
            .where("customers.region", "=", "North")
            .and()
            .where("orders.total", ">", 100.0)
            .orderBy("customers.name");
        
        // Test optimization
        assertDoesNotThrow(() -> {
            var result = queryOptimizer.optimize(finalBuilder);
            assertNotNull(result, "Optimization result should not be null");
            
            // Verify the result has suggestions
            assertTrue(result.getTotalSuggestionCount() >= 0, "Should handle suggestions count");
            
            System.out.println("Optimization summary: " + result.getSummary());
            System.out.println("Total suggestions: " + result.getTotalSuggestionCount());
            System.out.println("Index suggestions: " + result.getIndexSuggestions().size());
            System.out.println("Predicate pushdown suggestions: " + result.getPredicatePushdownSuggestions().size());
            System.out.println("Join reorder suggestions: " + result.getJoinReorderSuggestions().size());
            
        }, "Query optimization should not throw exceptions");
    }
    
    @Test
    @DisplayName("should handle query parameters correctly")
    void shouldHandleQueryParametersCorrectly() {
        DynamicQueryBuilder<Customer> builder = new DynamicQueryBuilder<>(Customer.class);
        
        builder = (DynamicQueryBuilder<Customer>) builder
            .where("name", "=", "Alice Johnson")
            .and()
            .where("creditLimit", ">=", 5000.0)
            .and()
            .whereIn("region", java.util.List.of("North", "South", "East"));
        
        // Verify all predicates have parameters
        assertEquals(3, builder.getPredicates().size());
        
        // Collect all parameters
        java.util.Map<String, Object> allParameters = new java.util.HashMap<>();
        builder.getPredicates().forEach(predicate -> {
            allParameters.putAll(predicate.getParameters());
        });
        
        // Verify parameters are present
        assertFalse(allParameters.isEmpty(), "Should have query parameters");
        assertTrue(allParameters.containsValue("Alice Johnson"));
        assertTrue(allParameters.containsValue(5000.0));
        
        System.out.println("Query parameters: " + allParameters);
    }
}