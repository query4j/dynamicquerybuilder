package com.github.query4j.cache;

/**
 * Cache module for Query4j dynamic query builder.
 * 
 * <p>
 * This module provides high-performance, thread-safe caching capabilities
 * using Caffeine as the underlying cache implementation. It supports:
 * </p>
 * <ul>
 * <li>Configurable maximum size and TTL settings</li>
 * <li>Named cache regions for logical separation</li>
 * <li>Comprehensive statistics tracking</li>
 * <li>Thread-safe concurrent access for 20+ threads</li>
 * <li>Sub-millisecond operation performance</li>
 * </ul>
 * 
 * <p>
 * Main entry points:
 * </p>
 * <ul>
 * <li>{@link com.github.query4j.cache.CacheManager} - Core caching interface</li>
 * <li>{@link com.github.query4j.cache.impl.CaffeineCacheManager} - High-performance implementation</li>
 * <li>{@link com.github.query4j.cache.CacheStatistics} - Performance metrics interface</li>
 * </ul>
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class Main {
    
    private Main() {
        // Utility class - no instantiation
    }
    
    /**
     * Module information and usage examples.
     */
    public static void main(String[] args) {
        System.out.println("Query4j Cache Module v1.0.0");
        System.out.println("High-performance caching with Caffeine backend");
        System.out.println();
        System.out.println("Example usage:");
        System.out.println("CacheManager cache = CaffeineCacheManager.create(10_000L, 3600L);");
        System.out.println("cache.put(\"query-key\", resultSet);");
        System.out.println("Object cached = cache.get(\"query-key\");");
        System.out.println("CacheStatistics stats = cache.stats();");
    }
}
