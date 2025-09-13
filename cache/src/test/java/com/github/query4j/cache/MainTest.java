package com.github.query4j.cache;

import com.github.query4j.cache.impl.CaffeineCacheManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for cache module setup and basic functionality.
 */
public class MainTest {

    @Test
    @DisplayName("Cache module context loads correctly")
    public void contextLoads() {
        // Verify basic cache manager creation works
        CacheManager cache = CaffeineCacheManager.create();
        assertNotNull(cache, "Cache manager should be created successfully");
        assertEquals("default", cache.getCacheRegion());
        assertNotNull(cache.stats(), "Statistics should be available");
    }

    @Test
    @DisplayName("Multiple cache regions work independently")
    public void multipleRegionsWork() {
        CacheManager cache1 = CaffeineCacheManager.forRegion("region1");
        CacheManager cache2 = CaffeineCacheManager.forRegion("region2");
        
        assertNotSame(cache1, cache2, "Different regions should be different instances");
        assertEquals("region1", cache1.getCacheRegion());
        assertEquals("region2", cache2.getCacheRegion());
        
        // Operations should be independent
        cache1.put("key", "value1");
        cache2.put("key", "value2");
        
        assertEquals("value1", cache1.get("key"));
        assertEquals("value2", cache2.get("key"));
    }
}
