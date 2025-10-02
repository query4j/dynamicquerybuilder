# Query4j Cache Module

High-performance, thread-safe caching module for Query4j providing fast in-memory query result caching with configurable size-based eviction, TTL (time-to-live), and comprehensive statistics tracking.

## Features

- **High Performance**: Sub-millisecond cache operations optimized for 20+ concurrent threads
- **Configurable Eviction**: Size-based LRU eviction and TTL-based expiration
- **Thread Safety**: Fully thread-safe for concurrent access patterns
- **Named Regions**: Logical separation of different query types
- **Comprehensive Statistics**: Hit/miss tracking, eviction metrics, and performance monitoring
- **Memory Efficiency**: Automatic cleanup and memory leak prevention

## Architecture

The cache module is built on [Caffeine](https://github.com/ben-manes/caffeine), a high-performance caching library that provides:

- **LRU Eviction**: Least Recently Used entries are evicted when maximum size is exceeded
- **TTL Support**: Time-based expiration with automatic cleanup
- **Statistics Tracking**: Built-in performance metrics and diagnostics
- **Lazy Eviction**: Optimized eviction processing for better performance

## Usage

### Basic Cache Operations

```java
// Create cache manager with default settings
CacheManager cache = CaffeineCacheManager.create();

// Store and retrieve values
cache.put("query-key", queryResult);
Object result = cache.get("query-key");

// Check if key exists
if (cache.containsKey("query-key")) {
    // Handle cache hit
}

// Remove specific entry
cache.invalidate("query-key");

// Clear all entries
cache.clear();
```

### Custom Configuration

```java
// Create cache with custom size and TTL
CacheManager cache = CaffeineCacheManager.create(1000L, 1800L); // 1000 entries, 30 min TTL

// Create named cache region
CacheManager queryCache = CaffeineCacheManager.forRegion("query-results");
CacheManager metadataCache = CaffeineCacheManager.forRegion("metadata", 500L, 300L);
```

### TTL-Specific Operations

```java
// Store with custom TTL
cache.put("short-lived-key", value, 60L); // 60 seconds TTL

// Store with default TTL
cache.put("normal-key", value);
```

### Statistics and Monitoring

```java
CacheStatistics stats = cache.stats();

// Basic metrics
long hits = stats.getHitCount();
long misses = stats.getMissCount();
long evictions = stats.getEvictionCount();
long currentSize = stats.getCurrentSize();

// Performance metrics
double hitRatio = stats.getHitRatio();
double missRatio = stats.getMissRatio();
long totalRequests = stats.getTotalRequests();

System.out.println("Cache performance: " + stats.toString());
```

## Cache Behavior and Testing

### Eviction Policy

The cache implements **Least Recently Used (LRU)** eviction when the maximum size is exceeded:

- Recently accessed entries are retained
- Least recently used entries are evicted first
- Eviction is lazy and processed during maintenance operations
- Manual `cache.maintenance()` can be called to force cleanup

### TTL Expiration

Time-to-Live (TTL) expiration works as follows:

- Entries expire after the configured TTL duration
- Expired entries are removed during maintenance or access
- Accessing expired entries results in cache misses
- TTL is configured per cache manager (not per entry)

### Memory Management

The cache module implements several memory efficiency strategies:

- **Size Constraints**: Cache size never exceeds configured maximum
- **Automatic Cleanup**: Expired and evicted entries are cleaned up automatically
- **Memory Monitoring**: Runtime memory usage is tracked during testing
- **Leak Prevention**: Proper cleanup prevents memory leaks under load

## Testing Coverage

The cache module includes comprehensive test coverage for all critical behaviors:

### Cache Hit and Miss Tracking

- ✅ **Hit Counting**: Accessing existing cached entries increments hit count
- ✅ **Miss Counting**: Accessing non-existent keys increments miss count  
- ✅ **Statistics Accuracy**: Ratios and counters are accurate during concurrent operations
- ✅ **Edge Cases**: Zero requests, only hits, only misses scenarios

### Eviction Policy Testing

- ✅ **LRU Eviction**: When max size exceeded, least recently used entries are evicted
- ✅ **Eviction Counting**: Eviction count increments appropriately with multiple evictions
- ✅ **Manual Invalidation**: Entries are removed and statistics updated correctly
- ✅ **Size Constraints**: Cache size never exceeds configured maximum

### TTL (Time-To-Live) Expiration

- ✅ **Time-Based Expiration**: Entries expire after configured TTL duration (tested with 1-2s TTL)
- ✅ **Miss After Expiration**: Expired entries cause misses and increment miss count
- ✅ **Cache Reload**: Cache can be reloaded with new values after expiration
- ✅ **Mixed Eviction**: Both size-based and TTL-based eviction work together

### Memory Efficiency

- ✅ **Memory Leak Prevention**: Heap usage monitoring ensures no memory leaks
- ✅ **Large Dataset Handling**: Cache handles thousands of entries with proper eviction
- ✅ **Memory Stability**: Memory usage remains stable under continuous operations
- ✅ **Size Reporting**: Cache size reporting is accurate during all operations

### Concurrency & Thread Safety

- ✅ **Concurrent Access**: 20+ threads can safely access cache simultaneously
- ✅ **Statistics Consistency**: Hit, miss, and eviction counters remain consistent under load
- ✅ **Performance Requirements**: Average operations complete in < 1ms under concurrent load
- ✅ **Data Integrity**: No corruption occurs during concurrent read/write operations

## Test Execution

Run the comprehensive cache tests:

```bash
# Run all cache tests
./gradlew cache:test

# Run specific test categories
./gradlew cache:test --tests "*Eviction*"
./gradlew cache:test --tests "*TTL*"
./gradlew cache:test --tests "*Memory*"
./gradlew cache:test --tests "*Statistics*"

# Run with verbose output
./gradlew cache:test --info
```

### Test Reliability Features

- **CI-Ready**: Tests use short TTL values (1-2s) with Thread.sleep for reliable timing
- **Lazy Eviction Handling**: Tests call `maintenance()` to process Caffeine's lazy eviction
- **Memory Monitoring**: Runtime memory usage tracked with GC calls for accurate measurements  
- **Concurrent Safety**: Proper synchronization with CountDownLatch and ExecutorService
- **Timeout Handling**: Generous timeouts (30s) for concurrent operations in CI environments

## Performance Characteristics

### Benchmarks

- **Average Operation Time**: < 1ms for get/put operations under 20+ concurrent threads
- **Memory Overhead**: Minimal memory footprint with efficient cleanup
- **Scalability**: Handles 1000+ concurrent operations without degradation
- **Cache Hit Performance**: Sub-millisecond response for cache hits

### Memory Usage

- **Baseline**: Cache manager has minimal memory footprint when empty
- **Growth**: Linear memory growth with dataset size, constrained by max size
- **Cleanup**: Significant memory reduction after clear() and maintenance()
- **Stability**: Memory usage remains stable during continuous operation cycles

## Configuration Options

### Cache Manager Settings

| Parameter | Default | Description |
|-----------|---------|-------------|
| `maxSize` | 10,000 | Maximum number of entries |
| `defaultTtlSeconds` | 3600 (1 hour) | Default TTL for entries |
| `cacheRegion` | "default" | Named cache region |

### Best Practices

1. **Size Planning**: Set `maxSize` based on expected query volume and memory constraints
2. **TTL Selection**: Choose TTL based on data freshness requirements
3. **Region Usage**: Use different regions for different types of cached data
4. **Maintenance**: Call `maintenance()` periodically for optimal cleanup
5. **Monitoring**: Monitor statistics for performance tuning

## Integration

The cache module integrates with the Query4j ecosystem:

- **Core Module**: Uses exception hierarchy from `query4j-core`
- **Optimizer Module**: Can be used by query optimizers for plan caching
- **Examples Module**: Demonstrates cache usage patterns

## Dependencies

- **Caffeine**: High-performance caching library (3.1.8)
- **Query4j Core**: Exception handling and base interfaces
- **JUnit 5**: Testing framework
- **jqwik**: Property-based testing support

## Troubleshooting

### Common Issues

**Memory Usage Growing**: 
- Check if `maxSize` is set appropriately
- Verify TTL is reasonable for your use case
- Call `maintenance()` periodically

**Poor Hit Ratios**:
- Review cache key generation for consistency
- Check if TTL is too short for your access patterns
- Monitor eviction count for size-related evictions

**Performance Issues**:
- Verify concurrent access patterns
- Check for excessive maintenance() calls
- Monitor GC pressure from large cached values

### Debugging

Enable detailed logging and use statistics for performance analysis:

```java
CacheStatistics stats = cache.stats();
System.out.println("Cache Stats: " + stats.toString());
System.out.println("Hit Ratio: " + stats.getHitRatio());
System.out.println("Evictions: " + stats.getEvictionCount());
```

## Documentation

For comprehensive documentation and API references:

- **[Main README](../README.md)** - Library overview, installation, and quick start
- **[API Reference Guide](../docs/API_GUIDE.md)** - Complete API documentation with examples
- **[Configuration Guide](../docs/Configuration.md)** - Cache configuration options and best practices
- **[JavaDoc Generation](../docs/JAVADOC_GENERATION.md)** - Generate and access API documentation
- **[Troubleshooting Guide](../README.md#troubleshooting)** - Common issues and solutions

### API Documentation (JavaDoc)

Generate JavaDoc for the cache module:

```bash
# Generate JavaDoc
./gradlew cache:javadoc

# View generated documentation
open cache/build/docs/javadoc/index.html
```

The cache module JavaDoc includes comprehensive documentation for:
- `CacheManager` - High-performance cache interface with TTL support
- `CacheStatistics` - Performance metrics with hit ratio targets (>80%)
- Implementation classes with usage examples

## Related Modules

- **[Core Module](../core/README.md)** - Query building and execution
- **[Optimizer Module](../optimizer/README.md)** - Query optimization with caching strategies
- **[Examples Module](../examples/README.md)** - Working examples with cache integration

## Contributing

When contributing to the cache module:

1. Add tests for new functionality following existing patterns
2. Ensure thread safety for all operations
3. Update this README with new features or behavior changes
4. Verify memory efficiency with appropriate test coverage
5. Maintain performance requirements (< 1ms average operations)
6. Follow [documentation standards](../docs/API_DOCUMENTATION_GUIDE.md)

See the [Contributing Guide](../CONTRIBUTING.md) for complete guidelines.

## Support

- **Issues**: [GitHub Issues](https://github.com/query4j/dynamicquerybuilder/issues)
- **Discussions**: [GitHub Discussions](https://github.com/query4j/dynamicquerybuilder/discussions)

## License

This module is part of the Query4j project and follows the same licensing terms.