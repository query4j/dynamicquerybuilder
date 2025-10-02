# Query4j Dynamic Query Builder - API Reference Guide

## Overview

Query4j provides a comprehensive, thread-safe, and high-performance API for building dynamic SQL queries with fluent method chaining. This guide documents all public API entry points, their usage patterns, and practical examples.

## Table of Contents

1. [API Entry Points Summary](#api-entry-points-summary)
2. [Core Module APIs](#core-module-apis)
3. [Cache Module APIs](#cache-module-apis)
4. [Optimizer Module APIs](#optimizer-module-apis)
5. [Configuration APIs](#configuration-apis)
6. [Exception Hierarchy](#exception-hierarchy)
7. [Usage Examples](#usage-examples)
8. [Performance Considerations](#performance-considerations)
9. [Migration Guide](#migration-guide)

## API Entry Points Summary

| Entry Point | Module | Description | Example Usage |
|-------------|---------|-------------|---------------|
| `QueryBuilder.forEntity()` | Core | Primary entry point for fluent query building | `QueryBuilder.forEntity(User.class).where("name", "John").findAll()` |
| `DynamicQuery` | Core | Represents a built, reusable query | `query.execute()` |
| `Page<T>` | Core | Paginated result container | `page.getContent()` |
| `QueryStats` | Core | Query execution metadata and performance metrics | `stats.getExecutionTimeMs()` |
| `CacheManager` | Cache | Query result caching interface *(Planned)* | `cacheManager.put(key, value, ttl)` |
| `QueryOptimizer` | Optimizer | Query analysis and optimization *(Planned)* | `optimizer.analyze(query)` |
| `Configuration` | Config | Application-wide settings *(Planned)* | `Configuration.builder().build()` |

## Core Module APIs

### QueryBuilder&lt;T&gt; Interface

**Primary Entry Point**: `QueryBuilder.forEntity(Class<T> entityClass)`

The `QueryBuilder` interface is the main entry point for all dynamic query operations. It provides a fluent API for constructing complex SQL queries with type safety and immutability.

#### Factory Method
```java
/**
 * Creates a new QueryBuilder instance for the specified entity class.
 */
static <T> QueryBuilder<T> forEntity(Class<T> entityClass)
```

#### Core Feature Categories

##### 1. WHERE Conditions
- `where(String fieldName, Object value)` - Equality condition
- `where(String fieldName, String operator, Object value)` - Custom operator condition  
- `whereIn(String fieldName, List<Object> values)` - IN condition
- `whereNotIn(String fieldName, List<Object> values)` - NOT IN condition
- `whereLike(String fieldName, String pattern)` - Pattern matching
- `whereNotLike(String fieldName, String pattern)` - Negative pattern matching
- `whereBetween(String fieldName, Object start, Object end)` - Range condition
- `whereIsNull(String fieldName)` - NULL check
- `whereIsNotNull(String fieldName)` - NOT NULL check

##### 2. Logical Operators
- `and()` - AND logical operator
- `or()` - OR logical operator  
- `not()` - NOT logical operator

##### 3. Condition Grouping
- `openGroup()` - Opens parenthetical grouping
- `closeGroup()` - Closes parenthetical grouping

##### 4. JOIN Operations
- `join(String associationField)` - Inner join
- `leftJoin(String associationField)` - Left outer join
- `rightJoin(String associationField)` - Right outer join
- `innerJoin(String associationField)` - Explicit inner join
- `fetch(String associationField)` - Fetch join for eager loading

##### 5. Selection and Aggregation
- `select(String... fieldNames)` - Field selection
- `countAll()` - COUNT(*) aggregation
- `count(String fieldName)` - COUNT(field) aggregation
- `sum(String fieldName)` - SUM aggregation
- `avg(String fieldName)` - AVG aggregation
- `min(String fieldName)` - MIN aggregation
- `max(String fieldName)` - MAX aggregation

##### 6. Grouping and Having
- `groupBy(String... fieldNames)` - GROUP BY clause
- `having(String field, String operator, Object value)` - HAVING condition

##### 7. Sorting
- `orderBy(String fieldName)` - Ascending sort
- `orderByDescending(String fieldName)` - Descending sort
- `orderBy(String fieldName, boolean ascending)` - Directional sort

##### 8. Pagination
- `limit(int maxResults)` - Result limit
- `offset(int skipCount)` - Result offset
- `page(int pageNumber, int pageSize)` - Page-based pagination

##### 9. Subqueries
- `exists(QueryBuilder<?> subquery)` - EXISTS condition
- `notExists(QueryBuilder<?> subquery)` - NOT EXISTS condition
- `in(String fieldName, QueryBuilder<?> subquery)` - IN subquery
- `notIn(String fieldName, QueryBuilder<?> subquery)` - NOT IN subquery

##### 10. Native SQL Support
- `nativeQuery(String sqlQuery)` - Native SQL execution
- `parameter(String name, Object value)` - Named parameter
- `parameters(Map<String, Object> params)` - Multiple parameters

##### 11. Caching
- `cached()` - Enable default caching
- `cached(String regionName)` - Region-specific caching
- `cached(long ttlSeconds)` - TTL-based caching

##### 12. Performance Hints
- `hint(String hintName, Object hintValue)` - Query hint
- `fetchSize(int fetchSize)` - Fetch size hint
- `timeout(int timeoutSeconds)` - Query timeout

##### 13. Query Execution
- `findAll()` - Execute and return all results
- `findOne()` - Execute and return first result
- `count()` - Execute count query
- `exists()` - Check if results exist

##### 14. Asynchronous Execution
- `findAllAsync()` - Async execution returning all results
- `findOneAsync()` - Async execution returning first result
- `countAsync()` - Async count query

##### 15. Pagination Results
- `findPage()` - Execute with pagination metadata

##### 16. Query Building
- `build()` - Build reusable DynamicQuery instance

##### 17. Debugging and Diagnostics
- `toSQL()` - Generate SQL string for debugging
- `getExecutionStats()` - Get performance statistics

### DynamicQuery&lt;T&gt; Interface

Represents a fully built query that can be executed multiple times with optimal performance.

```java
public interface DynamicQuery<T> {
    List<T> execute();           // Execute the query
    T executeOne();              // Execute and return single result
    long executeCount();         // Execute as count query
    String getSQL();             // Get generated SQL
}
```

### Page&lt;T&gt; Interface

Container for paginated results with metadata.

```java
public interface Page<T> {
    List<T> getContent();        // Current page content
    int getNumber();             // Current page number (0-based)
    int getSize();               // Page size
    long getTotalElements();     // Total number of elements
    int getTotalPages();         // Total number of pages
    boolean hasNext();           // Has next page
    boolean hasPrevious();       // Has previous page
    boolean isFirst();           // Is first page
    boolean isLast();            // Is last page
}
```

### QueryStats Interface

Provides execution metadata and performance metrics.

```java
public interface QueryStats {
    long getExecutionTimeMs();      // Execution time in milliseconds
    int getResultCount();           // Number of results returned
    String getGeneratedSQL();       // Generated SQL query
    Map<String, Object> getHints(); // Applied query hints
    boolean wasCacheHit();          // Whether result came from cache
    long getExecutionTimestamp();   // Execution timestamp
}
```

## Cache Module APIs

> **Note**: Cache module is currently in development. The following APIs represent the planned interface.

### CacheManager Interface *(Planned)*

```java
public interface CacheManager {
    void put(String key, Object value, long ttlSeconds);
    <T> T get(String key, Class<T> type);
    void evict(String key);
    void clear();
    void clearRegion(String regionName);
    CacheStatistics getStatistics();
}
```

### CacheConfiguration *(Planned)*

```java
public class CacheConfiguration {
    public static Builder builder() { /* */ }
    
    public static class Builder {
        public Builder maxSize(long maxSize);
        public Builder ttl(Duration ttl);
        public Builder region(String name);
        public CacheConfiguration build();
    }
}
```

## Optimizer Module APIs

> **Note**: Optimizer module is currently in development. The following APIs represent the planned interface.

### QueryOptimizer Interface *(Planned)*

```java
public interface QueryOptimizer {
    OptimizationResult analyze(QueryBuilder<?> query);
    QueryBuilder<?> optimize(QueryBuilder<?> query);
    List<OptimizationSuggestion> suggest(QueryBuilder<?> query);
}
```

### OptimizationResult *(Planned)*

```java
public interface OptimizationResult {
    List<OptimizationSuggestion> getSuggestions();
    QueryComplexityMetrics getComplexity();
    EstimatedPerformance getEstimatedPerformance();
}
```

## Configuration APIs

> **Note**: Configuration module is currently in development. The following APIs represent the planned interface.

### Configuration *(Planned)*

```java
public class Configuration {
    public static Builder builder() { /* */ }
    public static Configuration load(String configFile) { /* */ }
    
    public static class Builder {
        public Builder caching(CacheConfiguration cache);
        public Builder optimization(OptimizationConfiguration opt);
        public Builder database(DatabaseConfiguration db);
        public Configuration build();
    }
}
```

## Exception Hierarchy

Query4j uses a structured exception hierarchy for clear error handling:

```
DynamicQueryException (base)
├── QueryBuildException (query construction errors)
└── QueryExecutionException (runtime execution errors)
```

### DynamicQueryException
Base exception for all Query4j-related errors.

### QueryBuildException
Thrown during query construction for validation errors, invalid operators, or malformed queries.

### QueryExecutionException  
Thrown during query execution for database errors, connection issues, or runtime failures.

## Usage Examples

### Basic Query Construction

```java
// Simple equality query
List<User> users = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .findAll();

// Multiple conditions with logical operators
List<User> engineeringUsers = QueryBuilder.forEntity(User.class)
    .where("department", "Engineering")
    .and()
    .where("active", true)
    .orderBy("lastName")
    .findAll();
```

### Advanced Filtering

```java
// Complex conditions with grouping
List<Order> orders = QueryBuilder.forEntity(Order.class)
    .openGroup()
        .where("status", "PENDING")
        .or()
        .where("status", "PROCESSING")
    .closeGroup()
    .and()
    .whereBetween("orderDate", startDate, endDate)
    .whereIn("customerId", customerIds)
    .findAll();

// Pattern matching and null checks
List<Product> products = QueryBuilder.forEntity(Product.class)
    .whereLike("name", "%laptop%")
    .and()
    .whereIsNotNull("price")
    .and()
    .where("price", ">", 500.0)
    .findAll();
```

### Joins and Associations

```java
// Join with related entities
List<Order> ordersWithCustomers = QueryBuilder.forEntity(Order.class)
    .join("customer")
    .where("customer.country", "USA")
    .and()
    .where("status", "SHIPPED")
    .findAll();

// Fetch join for eager loading
List<User> usersWithRoles = QueryBuilder.forEntity(User.class)
    .fetch("roles")
    .where("active", true)
    .findAll();
```

### Aggregation and Grouping

```java
// Count queries
long activeUserCount = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .count();

// Aggregation with grouping
List<Object[]> salesByRegion = QueryBuilder.forEntity(Sale.class)
    .select("region", "SUM(amount)")
    .groupBy("region")
    .having("SUM(amount)", ">", 10000.0)
    .findAll();
```

### Pagination

```java
// Page-based pagination
Page<User> userPage = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .orderBy("lastName")
    .page(0, 20)  // First page, 20 items per page
    .findPage();

// Manual pagination with offset/limit
List<User> users = QueryBuilder.forEntity(User.class)
    .where("department", "Engineering")
    .orderBy("joinDate")
    .offset(50)
    .limit(25)
    .findAll();
```

### Caching

```java
// Enable default caching
List<User> cachedUsers = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .cached()
    .findAll();

// Region-specific caching with TTL
List<Product> cachedProducts = QueryBuilder.forEntity(Product.class)
    .where("featured", true)
    .cached("featured-products", 3600) // 1 hour TTL
    .findAll();
```

### Asynchronous Execution

```java
// Async query execution
CompletableFuture<List<User>> futureUsers = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .findAllAsync();

futureUsers.thenAccept(users -> {
    // Process results asynchronously
    users.forEach(System.out::println);
});
```

### Native SQL Queries

```java
// Native SQL with parameters
List<User> users = QueryBuilder.forEntity(User.class)
    .nativeQuery("SELECT * FROM users WHERE department = :dept AND salary > :minSalary")
    .parameter("dept", "Engineering")
    .parameter("minSalary", 75000)
    .findAll();
```

### Query Building and Reuse

```java
// Build reusable query
DynamicQuery<User> activeUsersQuery = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .orderBy("lastName")
    .build();

// Execute multiple times
List<User> batch1 = activeUsersQuery.execute();
List<User> batch2 = activeUsersQuery.execute();

// Get performance stats
QueryStats stats = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .findAll(); // This executes the query

System.out.println("Execution time: " + stats.getExecutionTimeMs() + "ms");
System.out.println("Results: " + stats.getResultCount());
```

## Performance Considerations

### Query Construction Performance
- All `QueryBuilder` methods return new instances (immutable pattern)
- Minimal object allocation during query construction  
- Sub-millisecond query building for typical queries
- Thread-safe concurrent usage

### Execution Performance
- Parameter placeholder generation (`:p1`, `:p2`, etc.)
- SQL string caching and reuse
- Connection pooling ready
- Batch query support

### Caching Strategy
- Query result caching with configurable TTL
- Cache region support for organized cache management
- Cache hit/miss statistics via `QueryStats`
- Memory-efficient cache eviction policies

### Optimization Guidelines
- Use `build()` for frequently executed queries
- Enable caching for read-heavy operations
- Leverage fetch joins to reduce N+1 queries
- Use pagination for large result sets
- Monitor performance via `QueryStats`

## Migration Guide

### From Raw JPA/Hibernate
```java
// Before: Raw JPA Criteria API
CriteriaBuilder cb = em.getCriteriaBuilder();
CriteriaQuery<User> cq = cb.createQuery(User.class);
Root<User> user = cq.from(User.class);
cq.select(user).where(cb.equal(user.get("active"), true));

// After: Query4j
List<User> users = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .findAll();
```

### From String-based Queries
```java
// Before: String concatenation
String sql = "SELECT * FROM users WHERE active = " + active + 
             " AND department = '" + department + "'";

// After: Query4j with type safety
List<User> users = QueryBuilder.forEntity(User.class)
    .where("active", active)
    .and()
    .where("department", department)
    .findAll();
```

### Version Compatibility

**Current Stable Release: v1.0.0** 🎉

#### Version History
- **1.0.0** (2025-10-02): First stable release - Production ready
  - Core query building with comprehensive predicate support
  - Cache module with LRU and TTL support
  - Optimizer module with query analysis and suggestions
  - 95%+ test coverage
  - Complete documentation and examples

#### Planned Releases
- **1.1.x** *(Planned)*: Advanced caching, distributed cache support
- **1.2.x** *(Planned)*: Query rewrite engine, advanced optimization
- **2.0.x** *(Planned)*: Multi-database transactions, async execution

#### Compatibility Matrix

| Query4j Version | Java Version | Spring Boot | Hibernate | Status |
|-----------------|--------------|-------------|-----------|--------|
| 1.0.0 | 17-21 | 2.7.x, 3.x | 5.6.x, 6.x | ✅ Stable |
| 1.0.0-SNAPSHOT | 17+ | 2.7.x, 3.x | 5.6.x, 6.x | ⚠️ Superseded |

For upgrade instructions, see [UPGRADE_GUIDE.md](../UPGRADE_GUIDE.md)

## Contributing to API Documentation

### Documentation Standards
1. **JavaDoc Completeness**: All public methods must have comprehensive JavaDoc
2. **Example Coverage**: Include practical examples for all major features  
3. **Error Documentation**: Document all thrown exceptions with context
4. **Performance Notes**: Include performance characteristics and recommendations

### Updating Documentation
1. **API Changes**: Update this guide when adding/modifying public APIs
2. **Example Validation**: Ensure all code examples compile and execute correctly
3. **Cross-References**: Maintain links between related APIs and concepts
4. **Version Tracking**: Document API changes in release notes

### Review Process
1. **Peer Review**: All documentation changes require code review
2. **Example Testing**: Validate examples in integration tests
3. **Performance Impact**: Document performance implications of new APIs
4. **Backward Compatibility**: Ensure migration guidance for breaking changes

---

For additional support and examples, see:
- [README.md](../README.md) - Quick start guide  
- [CONTRIBUTING.md](../CONTRIBUTING.md) - Contribution guidelines
- [GitHub Issues](https://github.com/query4j/dynamicquerybuilder/issues) - Bug reports and feature requests
- [GitHub Discussions](https://github.com/query4j/dynamicquerybuilder/discussions) - Community support