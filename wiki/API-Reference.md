# API Reference - Complete Query4j API Documentation

Complete reference for all Query4j public APIs across core, cache, and optimizer modules.

## Table of Contents

1. [Core Module APIs](#core-module-apis)
2. [Cache Module APIs](#cache-module-apis)
3. [Optimizer Module APIs](#optimizer-module-apis)
4. [Configuration APIs](#configuration-apis)
5. [Exception Hierarchy](#exception-hierarchy)

---

## Core Module APIs

### QueryBuilder<T>

Main query builder interface for constructing dynamic queries.

#### Factory Methods

```java
// Create with entity class (type-safe)
QueryBuilder<User> QueryBuilder.forEntity(Class<User> entityClass)

// Create with table name
QueryBuilder<?> QueryBuilder.forEntity(String tableName)
```

#### Predicate Methods

```java
// Simple equality predicate
QueryBuilder<T> where(String field, Object value)

// IN predicate
QueryBuilder<T> whereIn(String field, Collection<?> values)

// LIKE predicate
QueryBuilder<T> whereLike(String field, String pattern)

// BETWEEN predicate
QueryBuilder<T> whereBetween(String field, Object min, Object max)

// NULL check predicates
QueryBuilder<T> whereIsNull(String field)
QueryBuilder<T> whereIsNotNull(String field)
```

#### Logical Operators

```java
// Logical AND
QueryBuilder<T> and()

// Logical OR  
QueryBuilder<T> or()

// Logical NOT
QueryBuilder<T> not()

// Grouping
QueryBuilder<T> openGroup()
QueryBuilder<T> closeGroup()
```

#### JOIN Operations

```java
// INNER JOIN
QueryBuilder<T> innerJoin(String table, String localKey, String foreignKey)

// LEFT JOIN
QueryBuilder<T> leftJoin(String table, String localKey, String foreignKey)

// RIGHT JOIN
QueryBuilder<T> rightJoin(String table, String localKey, String foreignKey)

// FETCH JOIN (for eager loading)
QueryBuilder<T> fetchJoin(String table, String localKey, String foreignKey)
```

#### Aggregations and Grouping

```java
// SELECT specific columns/aggregations
QueryBuilder<T> select(String... columns)

// GROUP BY clause
QueryBuilder<T> groupBy(String... fields)

// HAVING clause
QueryBuilder<T> having(String condition)
```

#### Sorting and Pagination

```java
// ORDER BY clause
QueryBuilder<T> orderBy(String field)
QueryBuilder<T> orderBy(String field, String direction)

// LIMIT clause
QueryBuilder<T> limit(int limit)

// OFFSET clause
QueryBuilder<T> offset(int offset)
```

#### Query Execution

```java
// Generate SQL string
String toSQL()

// Get parameter map
Map<String, Object> getParameters()

// Execute query (requires executor)
List<T> findAll()
Optional<T> findOne()
long count()
```

---

## Cache Module APIs

### QueryCacheManager

Main cache manager for storing and retrieving query results.

#### Factory Methods

```java
// Create with LRU strategy
QueryCacheManager QueryCacheManager.withLRU(int maxSize)

// Create with LFU strategy
QueryCacheManager QueryCacheManager.withLFU(int maxSize)

// Create with custom configuration
QueryCacheManager QueryCacheManager.create(CacheConfig config)
```

#### Core Operations

```java
// Get cached value
<V> V get(String key)

// Put value in cache
<V> void put(String key, V value)

// Remove from cache
void remove(String key)

// Clear all entries
void clear()

// Check if key exists
boolean containsKey(String key)

// Get cache size
int size()
```

#### Conditional Operations

```java
// Remove entries matching predicate
void removeIf(BiPredicate<String, Object> predicate)

// Get or compute if absent
<V> V computeIfAbsent(String key, Function<String, V> mappingFunction)
```

#### Metrics

```java
// Get cache metrics (if enabled)
CacheMetrics getMetrics()
```

### CacheConfig

Configuration builder for cache settings.

```java
CacheConfig.Builder builder()
    .strategy(CacheConfig.Strategy strategy)       // LRU or LFU
    .maxSize(int maxSize)                          // Maximum entries
    .ttl(Duration ttl)                             // Time to live
    .enableMetrics(boolean enable)                 // Track statistics
    .concurrencyLevel(int level)                   // Thread concurrency
    .build()
```

### CacheMetrics

Cache performance metrics.

```java
// Get total request count
long getRequestCount()

// Get cache hit count
long getHitCount()

// Get cache miss count
long getMissCount()

// Calculate hit rate (0.0 - 1.0)
double getHitRate()

// Get eviction count
long getEvictionCount()

// Get current cache size
int getSize()

// Get maximum cache size
int getMaxSize()
```

---

## Optimizer Module APIs

### QueryOptimizer

Main optimizer for analyzing and suggesting query improvements.

#### Factory Methods

```java
// Create with default configuration
QueryOptimizer QueryOptimizer.create()

// Create with custom configuration
QueryOptimizer QueryOptimizer.create(OptimizerConfig config)

// Create no-op optimizer (disabled)
QueryOptimizer QueryOptimizer.noOp()
```

#### Analysis Methods

```java
// Analyze QueryBuilder instance
OptimizationResult analyze(QueryBuilder<?> query)

// Analyze SQL string
OptimizationResult analyzeSQL(String sql)

// Analyze with table statistics
OptimizationResult analyze(QueryBuilder<?> query, TableStatistics stats)
```

### OptimizationResult

Results of query optimization analysis.

```java
// Get index suggestions
List<IndexSuggestion> getIndexSuggestions()

// Get predicate pushdown suggestions
List<PredicatePushdownSuggestion> getPredicatePushdownSuggestions()

// Get join reordering suggestions
List<JoinReorderSuggestion> getJoinReorderSuggestions()

// Get all suggestions
List<OptimizationSuggestion> getAllSuggestions()

// Check if any suggestions available
boolean hasSuggestions()

// Get optimization score (0.0 - 1.0)
double getOptimizationScore()

// Get analysis duration
Duration getAnalysisTime()

// Get summary
String getSummary()
```

### IndexSuggestion

Index recommendation details.

```java
// Get table name
String getTableName()

// Get column names
List<String> getColumns()

// Get index type (BTREE, HASH, etc.)
String getIndexType()

// Get index DDL statement
String getIndexDefinition()

// Get reasoning
String getReason()

// Get estimated improvement (0.0 - 1.0)
double getEstimatedImprovement()

// Get priority (1=high, 2=medium, 3=low)
int getPriority()
```

### OptimizerConfig

Configuration for query optimizer.

```java
OptimizerConfig.Builder builder()
    .indexSuggestionsEnabled(boolean enable)
    .predicatePushdownEnabled(boolean enable)
    .joinReorderingEnabled(boolean enable)
    .indexSelectivityThreshold(double threshold)
    .maxAnalysisTimeMs(long timeoutMs)
    .verboseOutput(boolean verbose)
    .targetDatabase(DatabaseType type)
    .build()

// Predefined configurations
OptimizerConfig.defaultConfig()
OptimizerConfig.highPerformanceConfig()
OptimizerConfig.developmentConfig()
```

---

## Configuration APIs

### CoreConfig

Core module configuration.

```java
CoreConfig.Builder builder()
    .defaultQueryTimeoutMs(long timeout)
    .maxPredicateDepth(int depth)
    .maxPredicateCount(int count)
    .strictFieldValidation(boolean strict)
    .queryStatisticsEnabled(boolean enable)
    .build()

// Load from configuration sources
CoreConfig CoreConfig.load()

// Get current configuration
CoreConfig CoreConfig.current()
```

### CacheConfig

Cache module configuration (see Cache Module APIs above).

### OptimizerConfig

Optimizer module configuration (see Optimizer Module APIs above).

---

## Exception Hierarchy

### DynamicQueryException

Base exception for all Query4j errors.

```
DynamicQueryException (base - RuntimeException)
├── QueryBuildException (construction/validation errors)
├── QueryExecutionException (runtime/database errors)
└── OptimizationException (optimization analysis errors)
```

### QueryBuildException

Thrown during query construction for invalid inputs.

**Common Causes:**
- Invalid field names
- Empty IN collections
- Mismatched group open/close
- Exceeding predicate limits

```java
try {
    QueryBuilder.forEntity(User.class)
        .where("", "value");  // Invalid: empty field
} catch (QueryBuildException e) {
    // Handle build error
}
```

### QueryExecutionException

Thrown during query execution.

**Common Causes:**
- Database connection errors
- SQL syntax errors (from generated SQL)
- Timeout errors
- Permission issues

```java
try {
    List<User> users = query.findAll();
} catch (QueryExecutionException e) {
    // Handle execution error
}
```

### OptimizationException

Thrown during optimizer analysis.

**Common Causes:**
- Analysis timeout
- Invalid SQL parsing
- Missing table metadata

```java
try {
    OptimizationResult result = optimizer.analyze(query);
} catch (OptimizationException e) {
    // Handle optimization error
}
```

### ConfigurationException

Thrown for invalid configuration.

**Common Causes:**
- Invalid property values
- Missing required configuration
- Type conversion errors

```java
try {
    CoreConfig config = CoreConfig.load();
} catch (ConfigurationException e) {
    // Handle configuration error
}
```

---

## Usage Examples

### Complete Query Building Example

```java
import com.github.query4j.core.QueryBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class QueryExample {
    public void buildComplexQuery() {
        QueryBuilder<Order> query = QueryBuilder.forEntity(Order.class)
            .select("customer_id", "SUM(amount) as total")
            .innerJoin("Customer", "customer_id", "id")
            .openGroup()
                .where("Order.status", "completed")
                .or()
                .where("Order.status", "shipped")
            .closeGroup()
            .and()
            .whereBetween("Order.order_date", startDate, endDate)
            .and()
            .whereIn("Customer.country", Arrays.asList("USA", "Canada"))
            .groupBy("customer_id")
            .having("SUM(amount) > 1000")
            .orderBy("total", "DESC")
            .limit(100);
        
        String sql = query.toSQL();
        Map<String, Object> params = query.getParameters();
        
        // Execute query...
    }
}
```

### Cache with Optimizer Example

```java
import com.github.query4j.cache.QueryCacheManager;
import com.github.query4j.optimizer.QueryOptimizer;

public class OptimizedCachedQuery {
    private final QueryCacheManager cache;
    private final QueryOptimizer optimizer;
    
    public List<User> searchUsers(UserCriteria criteria) {
        QueryBuilder<User> query = buildQuery(criteria);
        
        // Check cache first
        String cacheKey = query.toSQL();
        List<User> cached = cache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // Optimize query (in dev/staging)
        if (shouldOptimize()) {
            OptimizationResult result = optimizer.analyze(query);
            logOptimizations(result);
        }
        
        // Execute and cache
        List<User> results = executeQuery(query);
        cache.put(cacheKey, results);
        
        return results;
    }
}
```

---

## See Also

For detailed API documentation with examples:
- **[Core Module](Core-Module)** - Query building APIs
- **[Cache Manager](Cache-Manager)** - Caching APIs
- **[Optimizer](Optimizer)** - Optimization APIs
- **[Error Handling](Error-Handling)** - Exception handling guide
- **[JavaDoc](https://query4j.github.io/dynamicquerybuilder/javadoc/)** - Generated API documentation

For complete source code and examples:
- **[GitHub Repository](https://github.com/query4j/dynamicquerybuilder)**
- **[Examples Module](https://github.com/query4j/dynamicquerybuilder/tree/master/examples)**

---

**Last Updated:** October 2025  
**Version:** 1.0.0
