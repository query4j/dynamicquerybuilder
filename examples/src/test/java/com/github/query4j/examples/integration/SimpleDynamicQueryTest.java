package com.github.query4j.examples.integration;

import com.github.query4j.cache.CacheManager;
import com.github.query4j.cache.impl.CaffeineCacheManager;
import com.github.query4j.core.QueryBuilder;
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
 * Simplified integration test to verify basic Query4j functionality 
 * without complex service layer dependencies.
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
@DisplayName("Simple Dynamic Query Integration Tests")
class SimpleDynamicQueryTest {
    
    private CacheManager simpleCacheManager;
    private QueryOptimizer simpleQueryOptimizer;
    
    @BeforeEach
    void setUp() {
        // Create cache manager
        simpleCacheManager = CaffeineCacheManager.create(100L, 300L);
        
        // Create query optimizer
        OptimizerConfig config = OptimizerConfig.builder()
            .indexSuggestionsEnabled(true)
            .predicatePushdownEnabled(true)
            .joinReorderingEnabled(true)
            .verboseOutput(true)
            .maxAnalysisTimeMs(5000L)
            .build();
        
        simpleQueryOptimizer = new QueryOptimizerImpl(config);
    }
    
    @Test
    @DisplayName("should create dynamic query builder successfully")
    void shouldCreateDynamicQueryBuilderSuccessfully() {
        // Test basic DynamicQueryBuilder creation and SQL generation
        DynamicQueryBuilder<Customer> queryBuilder = new DynamicQueryBuilder<>(Customer.class);
        QueryBuilder<Customer> builder = queryBuilder;
        
        builder = builder
            .where("region", "=", "North")
            .where("active", "=", true);
        
        DynamicQueryBuilder<Customer> finalBuilder = (DynamicQueryBuilder<Customer>) builder;
        String sql = finalBuilder.toSQL();
        
        assertNotNull(sql);
        assertTrue(sql.contains("SELECT"));
        assertTrue(sql.contains("Customer"));
        assertTrue(sql.contains("WHERE"));
        
        // Test that we have predicates
        assertFalse(finalBuilder.getPredicates().isEmpty());
        assertEquals(2, finalBuilder.getPredicates().size());
    }
    
    @Test
    @DisplayName("should have functioning cache manager")
    void shouldHaveFunctioningCacheManager() {
        assertNotNull(simpleCacheManager);
        
        // Test basic cache operations
        simpleCacheManager.put("test-key", "test-value");
        Object retrieved = simpleCacheManager.get("test-key");
        
        assertEquals("test-value", retrieved);
        assertTrue(simpleCacheManager.containsKey("test-key"));
        
        // Test statistics
        assertNotNull(simpleCacheManager.stats());
    }
    
    @Test
    @DisplayName("should have functioning query optimizer")
    void shouldHaveFunctioningQueryOptimizer() {
        assertNotNull(simpleQueryOptimizer);
        
        // Create a simple query to optimize
        DynamicQueryBuilder<Customer> queryBuilder = new DynamicQueryBuilder<>(Customer.class);
        QueryBuilder<Customer> builder = queryBuilder.where("region", "=", "North");
        final DynamicQueryBuilder<Customer> finalBuilder = (DynamicQueryBuilder<Customer>) builder;
        
        // Test optimization (this should not throw exceptions)
        assertDoesNotThrow(() -> {
            var result = simpleQueryOptimizer.optimize(finalBuilder);
            assertNotNull(result);
        });
    }
}