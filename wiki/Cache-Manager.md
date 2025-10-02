# Cache Manager - Query Result Caching

The Cache Manager module provides efficient, configurable caching strategies for query results to reduce database load and improve application performance.

## Table of Contents

1. [Overview](#overview)
2. [Installation](#installation)
3. [Quick Start](#quick-start)
4. [Cache Strategies](#cache-strategies)
5. [Configuration](#configuration)
6. [Advanced Usage](#advanced-usage)
7. [Performance Tuning](#performance-tuning)
8. [Monitoring and Metrics](#monitoring-and-metrics)

---

## Overview

The Cache Manager provides:

- **Multiple Cache Strategies** - LRU (Least Recently Used) and LFU (Least Frequently Used)
- **Thread-Safe Operations** - Concurrent access support
- **Configurable Capacity** - Memory-efficient with size limits
- **TTL Support** - Time-based expiration
- **Hit/Miss Metrics** - Performance monitoring
- **Eviction Policies** - Automatic cache management

### When to Use Caching

✅ **Use caching for:**
- Frequently accessed, rarely changing data
- Expensive queries with predictable results
- Read-heavy workloads
- Pagination of static data sets

❌ **Avoid caching for:**
- Real-time data requirements
- Frequently updated tables
- User-specific queries with high cardinality
- Queries with unpredictable parameters

---

## Installation

### Maven

```xml
<dependency>
    <groupId>com.github.query4j</groupId>
    <artifactId>dynamicquerybuilder-cache</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Gradle

```groovy
dependencies {
    implementation 'com.github.query4j:dynamicquerybuilder-cache:1.0.0-SNAPSHOT'
}
```

---

## Quick Start

### Basic LRU Cache

```java
import com.github.query4j.cache.QueryCacheManager;
import com.github.query4j.cache.CacheConfig;
import com.github.query4j.core.QueryBuilder;

// Create cache with LRU strategy
QueryCacheManager cache = QueryCacheManager.withLRU(100); // Max 100 entries

// Create query
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .orderBy("lastName");

String cacheKey = query.toSQL();

// Try to get from cache
List<User> users = cache.get(cacheKey);

if (users == null) {
    // Cache miss - execute query
    users = executeQuery(query);
    
    // Store in cache
    cache.put(cacheKey, users);
}

// Use cached results
return users;
```

### With TTL (Time To Live)

```java
import java.time.Duration;

CacheConfig config = CacheConfig.builder()
    .strategy(CacheConfig.Strategy.LRU)
    .maxSize(100)
    .ttl(Duration.ofMinutes(5))  // Expire after 5 minutes
    .build();

QueryCacheManager cache = QueryCacheManager.create(config);
```

---

## Cache Strategies

### LRU (Least Recently Used)

Evicts the least recently accessed items when cache is full.

**Best for:**
- General-purpose caching
- Predictable access patterns
- Time-sensitive data

```java
QueryCacheManager lruCache = QueryCacheManager.withLRU(100);

// Items accessed recently are kept
// Oldest unused items are evicted first
```

**Access Pattern Example:**
```
Cache: [A, B, C] (max=3)
Access C → [A, B, C] (C moved to end)
Add D → [B, C, D] (A evicted as LRU)
Access B → [C, D, B] (B moved to end)
```

### LFU (Least Frequently Used)

Evicts items with the lowest access frequency.

**Best for:**
- Hot data identification
- Workloads with clear access frequency patterns
- Long-running caches

```java
QueryCacheManager lfuCache = QueryCacheManager.withLFU(100);

// Items accessed frequently are kept
// Least accessed items are evicted first
```

**Access Pattern Example:**
```
Cache: [A(5), B(3), C(10)] (max=3, number=frequency)
Add D(1) → [A(5), C(10), D(1)] (B evicted as LFU)
Access D → [A(5), C(10), D(2)] (D frequency increases)
```

### Choosing a Strategy

| Factor | LRU | LFU |
|--------|-----|-----|
| Access Pattern | Temporal locality | Frequency-based |
| Best Use Case | Recent data matters | Popular data matters |
| Implementation | Simpler | More complex tracking |
| Memory Overhead | Lower | Higher (frequency counters) |
| Eviction Speed | Faster | Slower |

---

## Configuration

### CacheConfig Builder

```java
import com.github.query4j.cache.CacheConfig;
import java.time.Duration;

CacheConfig config = CacheConfig.builder()
    .strategy(CacheConfig.Strategy.LRU)      // Cache strategy
    .maxSize(500)                             // Max entries
    .ttl(Duration.ofMinutes(10))              // Expiration time
    .enableMetrics(true)                      // Track hit/miss
    .concurrencyLevel(4)                      // Thread concurrency
    .build();

QueryCacheManager cache = QueryCacheManager.create(config);
```

### Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `strategy` | Strategy | LRU | Eviction strategy (LRU/LFU) |
| `maxSize` | int | 100 | Maximum cache entries |
| `ttl` | Duration | null | Time to live (null = no expiration) |
| `enableMetrics` | boolean | false | Track cache statistics |
| `concurrencyLevel` | int | 1 | Thread concurrency hint |

### Environment-Specific Configs

#### Development

```java
CacheConfig devConfig = CacheConfig.builder()
    .strategy(CacheConfig.Strategy.LRU)
    .maxSize(50)                      // Small cache
    .ttl(Duration.ofMinutes(1))       // Short TTL
    .enableMetrics(true)              // Monitoring enabled
    .build();
```

#### Production

```java
CacheConfig prodConfig = CacheConfig.builder()
    .strategy(CacheConfig.Strategy.LFU)
    .maxSize(1000)                    // Large cache
    .ttl(Duration.ofMinutes(30))      // Longer TTL
    .enableMetrics(true)              // Performance tracking
    .concurrencyLevel(8)              // High concurrency
    .build();
```

#### High-Performance

```java
CacheConfig perfConfig = CacheConfig.builder()
    .strategy(CacheConfig.Strategy.LRU)
    .maxSize(5000)                    // Very large cache
    .ttl(Duration.ofHours(1))         // Long TTL
    .enableMetrics(false)             // Disable overhead
    .concurrencyLevel(16)             // Maximum concurrency
    .build();
```

---

## Advanced Usage

### Cache Key Generation

```java
public class CacheKeyGenerator {
    
    public static String generateKey(QueryBuilder<?> query) {
        // Use SQL + parameters as key
        String sql = query.toSQL();
        Map<String, Object> params = query.getParameters();
        
        return sql + "|" + params.toString();
    }
    
    public static String generateKeyWithHash(QueryBuilder<?> query) {
        String key = generateKey(query);
        return Integer.toHexString(key.hashCode());
    }
}
```

### Conditional Caching

```java
public List<User> getUsers(UserSearchCriteria criteria) {
    QueryBuilder<User> query = buildQuery(criteria);
    
    // Only cache if criteria is cacheable
    if (criteria.isCacheable()) {
        String cacheKey = CacheKeyGenerator.generateKey(query);
        
        List<User> cached = cache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        List<User> results = executeQuery(query);
        cache.put(cacheKey, results);
        return results;
    }
    
    // Execute without caching
    return executeQuery(query);
}
```

### Cache Warming

```java
public void warmCache() {
    // Preload frequently accessed queries
    List<QueryBuilder<User>> commonQueries = Arrays.asList(
        QueryBuilder.forEntity(User.class).where("active", true),
        QueryBuilder.forEntity(User.class).where("role", "admin"),
        QueryBuilder.forEntity(User.class).whereIsNotNull("email")
    );
    
    for (QueryBuilder<User> query : commonQueries) {
        String key = query.toSQL();
        List<User> results = executeQuery(query);
        cache.put(key, results);
    }
}
```

### Cache Invalidation

```java
public class UserService {
    private final QueryCacheManager cache;
    
    public void updateUser(User user) {
        // Update database
        userRepository.save(user);
        
        // Invalidate related cache entries
        cache.clear(); // Clear all (aggressive)
        
        // Or selectively invalidate
        cache.remove("SELECT * FROM User WHERE id = :p1");
        cache.remove("SELECT * FROM User WHERE active = :p1");
    }
    
    public void updateUserSelective(User user) {
        userRepository.save(user);
        
        // Pattern-based invalidation
        cache.removeIf((key, value) -> key.contains("User"));
    }
}
```

### Multi-Level Caching

```java
public class MultiLevelCache {
    private final QueryCacheManager l1Cache; // Fast, small
    private final QueryCacheManager l2Cache; // Slower, larger
    
    public MultiLevelCache() {
        l1Cache = QueryCacheManager.withLRU(100);
        l2Cache = QueryCacheManager.withLFU(1000);
    }
    
    public List<User> get(String key) {
        // Try L1 first
        List<User> result = l1Cache.get(key);
        if (result != null) {
            return result;
        }
        
        // Try L2
        result = l2Cache.get(key);
        if (result != null) {
            // Promote to L1
            l1Cache.put(key, result);
            return result;
        }
        
        return null; // Cache miss
    }
    
    public void put(String key, List<User> value) {
        l1Cache.put(key, value);
        l2Cache.put(key, value);
    }
}
```

---

## Performance Tuning

### Sizing Guidelines

**Maximum Cache Size:**
- **Small Application**: 50-100 entries
- **Medium Application**: 500-1000 entries
- **Large Application**: 5000-10000 entries
- **Enterprise**: 10000+ entries (monitor memory)

**Memory Estimation:**
```
Memory ≈ maxSize × averageResultSize × safetyFactor

Example:
  maxSize = 1000
  avgResultSize = 10 KB
  safetyFactor = 2
  Memory ≈ 1000 × 10 KB × 2 = 20 MB
```

### TTL Guidelines

| Data Type | Suggested TTL |
|-----------|--------------|
| Static reference data | 1-24 hours |
| Semi-static data | 5-30 minutes |
| Frequently updated | 1-5 minutes |
| Real-time data | Don't cache |

### Concurrency Settings

```java
// Low concurrency (single-threaded)
.concurrencyLevel(1)

// Medium concurrency (typical web app)
.concurrencyLevel(4)

// High concurrency (high-traffic service)
.concurrencyLevel(8)

// Maximum concurrency (microservices)
.concurrencyLevel(16)
```

---

## Monitoring and Metrics

### Enable Metrics

```java
CacheConfig config = CacheConfig.builder()
    .strategy(CacheConfig.Strategy.LRU)
    .maxSize(500)
    .enableMetrics(true)
    .build();

QueryCacheManager cache = QueryCacheManager.create(config);
```

### Retrieve Statistics

```java
// Get cache metrics
CacheMetrics metrics = cache.getMetrics();

System.out.println("Total requests: " + metrics.getRequestCount());
System.out.println("Cache hits: " + metrics.getHitCount());
System.out.println("Cache misses: " + metrics.getMissCount());
System.out.println("Hit rate: " + (metrics.getHitRate() * 100) + "%");
System.out.println("Eviction count: " + metrics.getEvictionCount());
System.out.println("Current size: " + metrics.getSize());
```

### Performance Logging

```java
public List<User> getUsersWithLogging(QueryBuilder<User> query) {
    String key = query.toSQL();
    long start = System.nanoTime();
    
    List<User> result = cache.get(key);
    boolean cacheHit = (result != null);
    
    if (!cacheHit) {
        result = executeQuery(query);
        cache.put(key, result);
    }
    
    long duration = System.nanoTime() - start;
    
    logger.info("Query cache {} in {}µs: {}", 
                cacheHit ? "HIT" : "MISS", 
                duration / 1000, 
                key);
    
    return result;
}
```

### Health Checks

```java
public boolean isCacheHealthy() {
    CacheMetrics metrics = cache.getMetrics();
    
    // Check hit rate
    double hitRate = metrics.getHitRate();
    if (hitRate < 0.5) { // Less than 50% hit rate
        logger.warn("Low cache hit rate: {}%", hitRate * 100);
        return false;
    }
    
    // Check size
    if (metrics.getSize() > metrics.getMaxSize() * 0.9) {
        logger.warn("Cache nearly full: {}/{}", 
                    metrics.getSize(), 
                    metrics.getMaxSize());
    }
    
    return true;
}
```

---

## Best Practices

### 1. Use Appropriate Cache Keys

```java
// ✅ Good - includes all query parameters
String key = query.toSQL() + "|" + query.getParameters().toString();

// ❌ Bad - might cause collisions
String key = query.toSQL();
```

### 2. Set Realistic TTLs

```java
// ✅ Good - appropriate for data freshness
.ttl(Duration.ofMinutes(10))

// ❌ Bad - too long for dynamic data
.ttl(Duration.ofDays(1))
```

### 3. Monitor Cache Performance

```java
// Log metrics periodically
@Scheduled(fixedRate = 60000) // Every minute
public void logCacheMetrics() {
    CacheMetrics metrics = cache.getMetrics();
    logger.info("Cache hit rate: {}%", metrics.getHitRate() * 100);
}
```

### 4. Handle Cache Misses Gracefully

```java
// ✅ Good - graceful fallback
List<User> users = cache.get(key);
if (users == null) {
    users = executeQuery(query);
    cache.put(key, users);
}

// ❌ Bad - no fallback
List<User> users = cache.get(key); // Might be null!
return users;
```

### 5. Clear Cache on Data Changes

```java
@Transactional
public void updateUser(User user) {
    userRepository.save(user);
    
    // Invalidate cache
    cache.clear(); // Or selective removal
}
```

---

## Troubleshooting

### Problem: Low Hit Rate

**Symptom:** Cache hit rate below 30%

**Causes:**
- Cache too small
- TTL too short
- High query variation
- Inappropriate caching strategy

**Solutions:**
- Increase `maxSize`
- Extend TTL
- Use better cache keys
- Switch between LRU/LFU

### Problem: Memory Issues

**Symptom:** High memory usage or OutOfMemoryError

**Causes:**
- Cache too large
- Result sets too big
- No TTL expiration

**Solutions:**
- Reduce `maxSize`
- Add or reduce TTL
- Cache smaller result sets
- Use projection queries

### Problem: Stale Data

**Symptom:** Cached data doesn't reflect recent changes

**Causes:**
- TTL too long
- Missing cache invalidation

**Solutions:**
- Reduce TTL
- Implement invalidation on updates
- Use event-driven cache clearing

---

## See Also

- **[Core Module](Core-Module)** - Query building fundamentals
- **[Optimizer](Optimizer)** - Query optimization
- **[Configuration](Configuration)** - Configuration guide
- **[Benchmarking](Benchmarking)** - Performance analysis

---

**Last Updated:** December 2024  
**Version:** 1.0.0
