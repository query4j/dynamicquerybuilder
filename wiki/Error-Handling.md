# Error Handling - Exception Catalog and Troubleshooting

Complete guide to Query4j exception handling, error messages, and troubleshooting steps.

## Table of Contents

1. [Exception Hierarchy](#exception-hierarchy)
2. [Core Module Errors](#core-module-errors)
3. [Cache Module Errors](#cache-module-errors)
4. [Optimizer Module Errors](#optimizer-module-errors)
5. [Configuration Errors](#configuration-errors)
6. [Common Issues and Solutions](#common-issues-and-solutions)

---

## Exception Hierarchy

Query4j uses a structured exception hierarchy for consistent error handling:

```
DynamicQueryException (RuntimeException)
├── QueryBuildException        (construction/validation errors)
├── QueryExecutionException    (runtime/database errors)
├── OptimizationException      (optimizer analysis errors)
└── ConfigurationException     (configuration errors)
```

### DynamicQueryException

**Base exception** for all Query4j library errors.

**Package:** `com.github.query4j.core.DynamicQueryException`  
**Type:** Unchecked (RuntimeException)  
**Purpose:** Root exception providing common error handling infrastructure

```java
try {
    // Query4j operations
} catch (DynamicQueryException e) {
    logger.error("Query4j error: {}", e.getMessage(), e);
    // Handle any Query4j error
}
```

---

## Core Module Errors

### QueryBuildException

Thrown during query construction for **invalid inputs or validation failures**.

#### ERROR-001: Invalid Field Name

**Message:** `Field name cannot be null or empty`

**Cause:** Attempting to use null or empty string as field name.

**Solution:**
```java
// ❌ Invalid
.where("", "value")           // Empty field name
.where(null, "value")         // Null field name

// ✅ Valid
.where("username", "value")   // Proper field name
```

#### ERROR-002: Invalid Field Name Format

**Message:** `Field name '{field}' does not match required pattern [A-Za-z0-9_\.]+`

**Cause:** Field name contains invalid characters.

**Solution:**
```java
// ❌ Invalid
.where("user-name", "value")      // Contains hyphen
.where("user name", "value")      // Contains space
.where("user@email", "value")     // Contains @

// ✅ Valid
.where("user_name", "value")      // Underscore
.where("userName", "value")       // CamelCase
.where("table.column", "value")   // Qualified name
```

#### ERROR-003: Empty IN Collection

**Message:** `IN predicate requires non-empty collection of values`

**Cause:** Calling `whereIn()` with null or empty collection.

**Solution:**
```java
// ❌ Invalid
.whereIn("role", null)                     // Null collection
.whereIn("role", Collections.emptyList())  // Empty collection

// ✅ Valid
.whereIn("role", Arrays.asList("admin", "user"))  // Non-empty
if (!roles.isEmpty()) {
    query = query.whereIn("role", roles);
}
```

#### ERROR-004: Mismatched Group Operations

**Message:** `Unclosed logical group: missing closeGroup() call`

**Cause:** `openGroup()` called without corresponding `closeGroup()`.

**Solution:**
```java
// ❌ Invalid
QueryBuilder.forEntity(User.class)
    .openGroup()
        .where("a", 1)
        .or()
        .where("b", 2)
    // Missing closeGroup()
    .and()
    .where("c", 3);

// ✅ Valid
QueryBuilder.forEntity(User.class)
    .openGroup()
        .where("a", 1)
        .or()
        .where("b", 2)
    .closeGroup()  // Properly closed
    .and()
    .where("c", 3);
```

#### ERROR-005: Maximum Predicate Depth Exceeded

**Message:** `Maximum predicate nesting depth ({max}) exceeded`

**Cause:** Too many nested logical groups.

**Solution:**
```java
// Increase limit in configuration
CoreConfig config = CoreConfig.builder()
    .maxPredicateDepth(20)  // Increase from default 10
    .build();

// Or flatten query structure
```

#### ERROR-006: Maximum Predicate Count Exceeded

**Message:** `Maximum predicate count ({max}) exceeded`

**Cause:** Too many predicates in single query.

**Solution:**
```java
// Increase limit in configuration
CoreConfig config = CoreConfig.builder()
    .maxPredicateCount(100)  // Increase from default 50
    .build();

// Or split into multiple queries
```

#### ERROR-007: Invalid Operator

**Message:** `Unsupported operator: {operator}`

**Cause:** Using unsupported comparison operator.

**Solution:**
```java
// Use supported predicate methods:
.where(field, value)              // Equality
.whereIn(field, values)           // IN
.whereLike(field, pattern)        // LIKE
.whereBetween(field, min, max)    // BETWEEN
.whereIsNull(field)               // IS NULL
.whereIsNotNull(field)            // IS NOT NULL
```

---

## Cache Module Errors

### CacheException

Thrown for cache-related errors.

#### ERROR-101: Invalid Cache Key

**Message:** `Cache key cannot be null or empty`

**Cause:** Attempting to use null/empty cache key.

**Solution:**
```java
// ❌ Invalid
cache.get(null);
cache.get("");

// ✅ Valid
String key = query.toSQL();
cache.get(key);
```

#### ERROR-102: Cache Size Exceeded

**Message:** `Cannot add entry: cache size limit ({maxSize}) reached`

**Cause:** Cache full and eviction failed.

**Solution:**
```java
// Increase cache size
CacheConfig config = CacheConfig.builder()
    .maxSize(1000)  // Increase capacity
    .build();

// Or implement aggressive eviction
cache.clear();  // Clear old entries
```

#### ERROR-103: Invalid TTL

**Message:** `TTL must be positive: {ttl}`

**Cause:** Negative or zero TTL value.

**Solution:**
```java
// ❌ Invalid
.ttl(Duration.ofSeconds(-1))
.ttl(Duration.ZERO)

// ✅ Valid
.ttl(Duration.ofMinutes(10))
.ttl(Duration.ofHours(1))
```

---

## Optimizer Module Errors

### OptimizationException

Thrown during query optimization analysis.

#### ERROR-201: Analysis Timeout

**Message:** `Query analysis exceeded maximum time: {timeout}ms`

**Cause:** Complex query analysis took too long.

**Solution:**
```java
// Increase timeout
OptimizerConfig config = OptimizerConfig.builder()
    .maxAnalysisTimeMs(30000)  // 30 seconds
    .build();

// Or simplify query
```

#### ERROR-202: Invalid SQL Syntax

**Message:** `Cannot parse SQL: {sql}`

**Cause:** Malformed SQL string for analysis.

**Solution:**
```java
// Validate SQL before analysis
String sql = query.toSQL();
System.out.println("SQL: " + sql);

// Ensure SQL is syntactically valid
OptimizationResult result = optimizer.analyzeSQL(sql);
```

#### ERROR-203: Missing Table Metadata

**Message:** `Table statistics not available for: {table}`

**Cause:** Optimizer requires table statistics that aren't provided.

**Solution:**
```java
// Provide table statistics
TableStatistics stats = TableStatistics.builder()
    .tableName("users")
    .rowCount(100000)
    .indexedColumns(Arrays.asList("id", "email"))
    .build();

OptimizationResult result = optimizer.analyze(query, stats);
```

---

## Configuration Errors

### ConfigurationException

Thrown for configuration-related errors.

#### ERROR-301: Invalid Configuration Property

**Message:** `Invalid value for property '{property}': {value}`

**Cause:** Configuration value doesn't meet validation rules.

**Solution:**
```java
// Check valid ranges
CoreConfig config = CoreConfig.builder()
    .maxPredicateDepth(10)      // Must be > 0
    .maxPredicateCount(50)      // Must be > 0
    .defaultQueryTimeoutMs(30000) // Must be >= 0
    .build();
```

#### ERROR-302: Missing Required Configuration

**Message:** `Required configuration property not found: {property}`

**Cause:** Required configuration missing from all sources.

**Solution:**
```java
// Provide via environment variable
export QUERY4J_REQUIRED_PROPERTY=value

// Or properties file
query4j.required.property=value

// Or programmatically
config.setProperty("required.property", value);
```

#### ERROR-303: Configuration File Not Found

**Message:** `Configuration file not found: {file}`

**Cause:** Referenced configuration file doesn't exist.

**Solution:**
```java
// Verify file location
// Should be in: src/main/resources/query4j.properties
// Or: src/main/resources/query4j.yml

// Check classpath
System.out.println(System.getProperty("java.class.path"));
```

---

## Common Issues and Solutions

### Issue: Query Returns Unexpected Results

**Symptoms:**
- Wrong number of results
- Missing expected data
- Duplicate records

**Debugging Steps:**

1. **Inspect generated SQL:**
```java
String sql = query.toSQL();
System.out.println("SQL: " + sql);
```

2. **Check parameters:**
```java
Map<String, Object> params = query.getParameters();
System.out.println("Parameters: " + params);
```

3. **Validate field names:**
```java
// Ensure field names match database columns
// Check for typos and case sensitivity
```

4. **Test SQL directly:**
```sql
-- Execute generated SQL in database client
-- Manually substitute parameters to verify logic
```

### Issue: Performance Degradation

**Symptoms:**
- Slow query execution
- High memory usage
- Timeout errors

**Solutions:**

1. **Use Query Optimizer:**
```java
OptimizationResult result = optimizer.analyze(query);
result.getIndexSuggestions().forEach(System.out::println);
```

2. **Enable query caching:**
```java
QueryCacheManager cache = QueryCacheManager.withLRU(500);
// Cache frequently accessed queries
```

3. **Limit result size:**
```java
query = query.limit(100);  // Paginate large results
```

4. **Review predicate complexity:**
```java
// Reduce number of predicates
// Flatten nested logical groups
// Use indexes on filtered columns
```

### Issue: Memory Leaks

**Symptoms:**
- Growing memory usage
- OutOfMemoryError
- GC pressure

**Solutions:**

1. **Limit cache size:**
```java
CacheConfig config = CacheConfig.builder()
    .maxSize(1000)  // Set reasonable limit
    .ttl(Duration.ofMinutes(10))  // Add expiration
    .build();
```

2. **Clear cache periodically:**
```java
@Scheduled(fixedRate = 3600000)  // Every hour
public void clearCache() {
    cache.clear();
}
```

3. **Profile memory usage:**
```java
// Monitor cache metrics
CacheMetrics metrics = cache.getMetrics();
logger.info("Cache size: {}/{}", metrics.getSize(), metrics.getMaxSize());
```

### Issue: Stale Cached Data

**Symptoms:**
- Cached data doesn't reflect recent updates
- Changes not visible immediately

**Solutions:**

1. **Reduce TTL:**
```java
.ttl(Duration.ofMinutes(5))  // Shorter expiration
```

2. **Invalidate on updates:**
```java
@Transactional
public void updateUser(User user) {
    userRepository.save(user);
    cache.clear();  // Or selective removal
}
```

3. **Use event-driven invalidation:**
```java
@EventListener
public void onUserUpdated(UserUpdatedEvent event) {
    cache.removeIf((key, value) -> 
        key.contains("User") && key.contains(event.getUserId()));
}
```

### Issue: Thread Safety Concerns

**Symptoms:**
- Concurrent modification exceptions
- Inconsistent query results
- Race conditions

**Solutions:**

1. **Leverage immutability:**
```java
// QueryBuilder is immutable - safe to share
QueryBuilder<User> baseQuery = QueryBuilder.forEntity(User.class);

// Each thread gets its own instance
CompletableFuture.supplyAsync(() -> {
    return baseQuery.where("role", "admin").findAll();
});
```

2. **Use thread-safe cache:**
```java
CacheConfig config = CacheConfig.builder()
    .concurrencyLevel(8)  // High concurrency
    .build();
```

3. **Avoid shared mutable state:**
```java
// ❌ Bad - shared mutable builder
private QueryBuilder<User> sharedQuery;

// ✅ Good - create per request
public List<User> getUsers() {
    QueryBuilder<User> query = QueryBuilder.forEntity(User.class);
    return query.where("active", true).findAll();
}
```

---

## Troubleshooting Workflow

### Step 1: Enable Verbose Logging

```java
// Enable detailed logging
OptimizerConfig config = OptimizerConfig.builder()
    .verboseOutput(true)
    .build();

// Or via logging configuration
logging.level.com.github.query4j=DEBUG
```

### Step 2: Inspect Query Components

```java
QueryBuilder<User> query = buildQuery();

// SQL
System.out.println("SQL: " + query.toSQL());

// Parameters
System.out.println("Parameters: " + query.getParameters());

// Predicate count
System.out.println("Predicates: " + query.getPredicateCount());
```

### Step 3: Test Incrementally

```java
// Build query step by step
QueryBuilder<User> query = QueryBuilder.forEntity(User.class);
System.out.println("Base: " + query.toSQL());

query = query.where("active", true);
System.out.println("With filter: " + query.toSQL());

query = query.orderBy("name");
System.out.println("With ordering: " + query.toSQL());
```

### Step 4: Validate Against Database

```sql
-- Run generated SQL directly in database
-- Verify results match expectations
-- Check execution plan for performance
```

---

## Getting Help

If you can't resolve an issue:

1. **Check Documentation:**
   - [FAQ and Troubleshooting](FAQ-and-Troubleshooting)
   - [API Reference](API-Reference)
   - [GitHub README](https://github.com/query4j/dynamicquerybuilder)

2. **Search Existing Issues:**
   - [GitHub Issues](https://github.com/query4j/dynamicquerybuilder/issues)

3. **Ask the Community:**
   - [GitHub Discussions](https://github.com/query4j/dynamicquerybuilder/discussions)
   - [Stack Overflow](https://stackoverflow.com/questions/tagged/query4j) (tag: `query4j`)

4. **Report a Bug:**
   - [Create Issue](https://github.com/query4j/dynamicquerybuilder/issues/new)
   - Include: Query4j version, error message, minimal reproduction code

---

## See Also

- **[ERRORS.md](https://github.com/query4j/dynamicquerybuilder/blob/master/ERRORS.md)** - Complete error catalog
- **[FAQ and Troubleshooting](FAQ-and-Troubleshooting)** - Common questions
- **[API Reference](API-Reference)** - Complete API documentation

---

**Last Updated:** October 2025  
**Version:** 1.0.0
