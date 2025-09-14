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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simplified integration test to verify basic Query4j functionality 
 * without complex service layer dependencies.
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Simple Dynamic Query Integration Tests")
class SimpleDynamicQueryTest {
    
    @TestConfiguration
    static class TestConfig {
        
        @Bean
        public CacheManager cacheManager() {
            return CaffeineCacheManager.create(100L, 300L);
        }
        
        @Bean
        public QueryOptimizer queryOptimizer() {
            OptimizerConfig config = OptimizerConfig.builder()
                .indexSuggestionsEnabled(true)
                .predicatePushdownEnabled(true)
                .joinReorderingEnabled(true)
                .verboseOutput(true)
                .maxAnalysisTimeMs(5000L)
                .build();
            
            return new QueryOptimizerImpl(config);
        }
    }
    
    @Autowired
    private CacheManager cacheManager;
    
    @Autowired
    private QueryOptimizer queryOptimizer;
    
    @BeforeEach
    void setUp() {
        // Simple setup
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
        assertNotNull(cacheManager);
        
        // Test basic cache operations
        cacheManager.put("test-key", "test-value");
        Object retrieved = cacheManager.get("test-key");
        
        assertEquals("test-value", retrieved);
        assertTrue(cacheManager.containsKey("test-key"));
        
        // Test statistics
        assertNotNull(cacheManager.stats());
    }
    
    @Test
    @DisplayName("should have functioning query optimizer")
    void shouldHaveFunctioningQueryOptimizer() {
        assertNotNull(queryOptimizer);
        
        // Create a simple query to optimize
        DynamicQueryBuilder<Customer> queryBuilder = new DynamicQueryBuilder<>(Customer.class);
        QueryBuilder<Customer> builder = queryBuilder.where("region", "=", "North");
        final DynamicQueryBuilder<Customer> finalBuilder = (DynamicQueryBuilder<Customer>) builder;
        
        // Test optimization (this should not throw exceptions)
        assertDoesNotThrow(() -> {
            var result = queryOptimizer.optimize(finalBuilder);
            assertNotNull(result);
        });
    }
}