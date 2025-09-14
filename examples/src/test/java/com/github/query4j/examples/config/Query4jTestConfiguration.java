package com.github.query4j.examples.config;

import com.github.query4j.cache.CacheManager;
import com.github.query4j.cache.impl.CaffeineCacheManager;
import com.github.query4j.core.config.CoreConfig;
import com.github.query4j.optimizer.QueryOptimizer;
import com.github.query4j.optimizer.QueryOptimizerImpl;
import com.github.query4j.optimizer.OptimizerConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Test configuration for Query4j components in Spring Boot context.
 * This configuration provides optimized beans for integration testing
 * with caching enabled and development-friendly settings.
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
@TestConfiguration
public class Query4jTestConfiguration {
    
    /**
     * Core configuration optimized for testing with extended timeouts
     * and comprehensive validation enabled.
     */
    @Bean
    @Primary
    public CoreConfig coreConfig() {
        return CoreConfig.developmentConfig();
    }
    
    /**
     * Caffeine-based cache manager for testing with small cache size
     * to enable easy cache eviction testing.
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        return CaffeineCacheManager.create(
            100L,   // maxSize - small for testing
            300L    // defaultTtlSeconds - 5 minutes
        );
    }
    
    /**
     * Query optimizer configured for H2 database with verbose output
     * for testing and debugging.
     */
    @Bean
    @Primary
    public QueryOptimizer queryOptimizer() {
        OptimizerConfig config = OptimizerConfig.builder()
            .indexSuggestionsEnabled(true)
            .predicatePushdownEnabled(true)
            .joinReorderingEnabled(true)
            .verboseOutput(true)
            .maxAnalysisTimeMs(5000L) // 5 seconds for testing
            .build();
        
        return new QueryOptimizerImpl(config);
    }
}