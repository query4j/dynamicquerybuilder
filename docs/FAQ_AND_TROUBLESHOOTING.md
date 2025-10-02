# Query4j FAQ and Troubleshooting Guide

A comprehensive guide to frequently asked questions and troubleshooting common issues with the Query4j Dynamic Query Builder library.

## Table of Contents

1. [Frequently Asked Questions (FAQ)](#frequently-asked-questions-faq)
   - [Installation and Setup](#installation-and-setup)
   - [Configuration and Caching](#configuration-and-caching)
   - [Query Building and Predicates](#query-building-and-predicates)
   - [Database Compatibility](#database-compatibility)
   - [Error Handling](#error-handling)
   - [Performance Optimization](#performance-optimization)
   - [Version Compatibility](#version-compatibility)
2. [Troubleshooting Guide](#troubleshooting-guide)
   - [Query Build Exceptions](#query-build-exceptions)
   - [Runtime and Execution Errors](#runtime-and-execution-errors)
   - [Cache Issues](#cache-issues)
   - [Optimizer Problems](#optimizer-problems)
   - [Async and Batch Processing](#async-and-batch-processing)
   - [Debugging and Logging](#debugging-and-logging)
3. [Community Contributions](#community-contributions)

---

## Frequently Asked Questions (FAQ)

### Installation and Setup

#### Q: What are the minimum requirements to use Query4j?

**A:** Query4j requires:
- **Java 17** or higher
- **Gradle 8.5+** or **Maven 3.6+** for building from source
- Compatible with major SQL databases (H2, PostgreSQL, MySQL, Oracle, SQL Server)

```gradle
// Gradle dependency
dependencies {
    implementation 'com.github.query4j:dynamicquerybuilder-core:1.0.0'
    implementation 'com.github.query4j:dynamicquerybuilder-cache:1.0.0'  // Optional
    implementation 'com.github.query4j:dynamicquerybuilder-optimizer:1.0.0'  // Optional
}
```

```xml
<!-- Maven dependency -->
<dependency>
    <groupId>com.github.query4j</groupId>
    <artifactId>dynamicquerybuilder-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### Q: How do I get started with a basic query?

**A:** Start with the `QueryBuilder.forEntity()` factory method:

```java
import com.github.query4j.core.QueryBuilder;

// Simple query
List<User> users = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .findAll();

// Complex query with multiple conditions
List<User> filteredUsers = QueryBuilder.forEntity(User.class)
    .where("department", "Engineering")
    .and()
    .whereIn("role", Arrays.asList("admin", "developer"))
    .orderBy("lastName")
    .limit(50)
    .findAll();
```

#### Q: Can I use Query4j without Spring Boot?

**A:** Yes! Query4j is framework-agnostic and works with any Java application. It doesn't require Spring Boot, Spring Framework, or any other specific framework. You can use it with plain JDBC, JPA/Hibernate, or any other data access technology.

```java
// Standalone usage (no framework required)
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .where("status", "active");

// Get generated SQL and parameters
String sql = query.toSQL();
Map<String, Object> params = query.getParameters();

// Execute with your preferred data access method
```

---

### Configuration and Caching

#### Q: How do I enable caching for my queries?

**A:** Caching can be enabled in two ways:

**1. Programmatically (per-query basis):**
```java
Page<User> users = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .cached(3600) // Cache for 1 hour (3600 seconds)
    .findPage();
```

**2. Via Configuration (global settings):**
```properties
# query4j.properties
query4j.cache.enabled=true
query4j.cache.defaultTtlSeconds=3600
query4j.cache.maxSize=10000
query4j.cache.defaultRegion=default
```

Or using YAML:
```yaml
query4j:
  cache:
    enabled: true
    defaultTtlSeconds: 3600
    maxSize: 10000
    defaultRegion: "default"
```

**3. Programmatic Configuration:**
```java
import com.github.query4j.core.config.CacheConfig;

// Create custom cache configuration
CacheConfig cacheConfig = CacheConfig.builder()
    .enabled(true)
    .defaultTtlSeconds(3600)
    .maxSize(10000)
    .statisticsEnabled(true)
    .build();

// Use with QueryBuilder (when cache module is available)
// QueryBuilder will use the configured cache settings
```

#### Q: How can I disable caching for specific queries?

**A:** Even if caching is enabled globally, you can disable it for specific queries:

```java
// Explicitly disable caching for this query
List<User> users = QueryBuilder.forEntity(User.class)
    .where("id", 123)
    .cached(0)  // 0 or negative TTL disables caching
    .findAll();
```

#### Q: What caching strategies does Query4j support?

**A:** Query4j uses an LRU (Least Recently Used) cache with TTL (Time To Live) support:
- **Maximum size-based eviction**: Automatically removes least recently used entries when cache is full
- **TTL-based expiration**: Entries expire after configured time period
- **Manual invalidation**: You can programmatically clear cache entries or entire regions
- **Statistics tracking**: Monitor cache hit rates, miss rates, and eviction metrics

#### Q: How do I configure different cache regions for different entity types?

**A:** Cache regions help organize cached queries by entity type or use case:

```java
// Configure cache with regions
CacheConfig config = CacheConfig.builder()
    .enabled(true)
    .defaultRegion("users")  // Default region for user queries
    .maxSize(10000)
    .build();

// Queries will use the configured region
QueryBuilder.forEntity(User.class)
    .where("active", true)
    .cached(1800)  // Uses "users" region
    .findAll();
```

---

### Query Building and Predicates

#### Q: How do I define custom predicates?

**A:** Query4j provides several ways to create custom predicates:

**1. Using Custom Function Predicates:**
```java
import com.github.query4j.core.criteria.CustomFunctionPredicate;

// Custom database function
QueryBuilder.forEntity(User.class)
    .where(new CustomFunctionPredicate(
        "UPPER",           // Function name
        "email",           // Field name
        new Object[]{},    // Additional parameters
        "p"                // Parameter prefix
    ))
    .findAll();

// Generates SQL: WHERE UPPER(email) = :p
```

**2. Implementing the Predicate Interface:**
```java
import com.github.query4j.core.criteria.Predicate;
import java.util.Map;
import java.util.HashMap;

public class CustomPredicate implements Predicate {
    private final String fieldName;
    private final Object value;
    
    public CustomPredicate(String fieldName, Object value) {
        this.fieldName = fieldName;
        this.value = value;
    }
    
    @Override
    public String toSQL() {
        return "CUSTOM_FUNCTION(" + fieldName + ") = :customValue";
    }
    
    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put("customValue", value);
        return params;
    }
}
```

**3. Using Built-in Predicate Types:**
```java
// Simple equality
.where("status", "active")

// Custom operator
.where("age", ">", 18)

// IN clause
.whereIn("role", Arrays.asList("admin", "user"))

// LIKE pattern matching
.whereLike("name", "John%")

// BETWEEN range
.whereBetween("salary", 50000, 100000)

// NULL checks
.whereIsNull("deletedAt")
.whereIsNotNull("email")
```

#### Q: How do I build complex queries with AND/OR logic?

**A:** Use logical operators and grouping:

```java
// Complex query with grouping
List<User> users = QueryBuilder.forEntity(User.class)
    .where("department", "Engineering")
    .and()
    .openGroup()  // Start parenthetical group
        .where("role", "admin")
        .or()
        .where("role", "developer")
    .closeGroup()  // End parenthetical group
    .and()
    .where("active", true)
    .findAll();

// Generates SQL:
// WHERE department = :p0 
// AND (role = :p1 OR role = :p2) 
// AND active = :p3
```

**Multiple levels of nesting:**
```java
QueryBuilder.forEntity(Order.class)
    .where("status", "pending")
    .and()
    .openGroup()
        .where("priority", "high")
        .or()
        .openGroup()
            .where("priority", "medium")
            .and()
            .where("amount", ">", 1000)
        .closeGroup()
    .closeGroup()
    .findAll();
```

#### Q: Can I reuse query builders?

**A:** Yes! Query4j uses an immutable builder pattern. Each method call returns a new instance:

```java
// Base query (reusable)
QueryBuilder<User> baseQuery = QueryBuilder.forEntity(User.class)
    .where("active", true);

// Derive specific queries without modifying base
List<User> admins = baseQuery
    .and()
    .where("role", "admin")
    .findAll();

List<User> developers = baseQuery
    .and()
    .where("role", "developer")
    .findAll();

// baseQuery remains unchanged
```

#### Q: How do I handle dynamic query conditions (optional filters)?

**A:** Build queries conditionally:

```java
public List<User> searchUsers(String name, String department, Boolean active) {
    QueryBuilder<User> query = QueryBuilder.forEntity(User.class);
    boolean hasCondition = false;
    
    if (name != null && !name.isEmpty()) {
        query = query.whereLike("name", "%" + name + "%");
        hasCondition = true;
    }
    
    if (department != null) {
        if (hasCondition) {
            query = query.and();
        }
        query = query.where("department", department);
        hasCondition = true;
    }
    
    if (active != null) {
        if (hasCondition) {
            query = query.and();
        }
        query = query.where("active", active);
    }
    
    return query.findAll();
}
```

---

### Database Compatibility

#### Q: Which SQL databases are supported?

**A:** Query4j generates standard SQL that works with all major databases:

| Database | Version | Status | Notes |
|----------|---------|--------|-------|
| **H2** | 2.x | ✅ Fully Supported | Primary test database |
| **PostgreSQL** | 12+ | ✅ Fully Supported | Production tested |
| **MySQL** | 8.0+ | ✅ Fully Supported | MariaDB compatible |
| **Oracle** | 12c+ | ✅ Compatible | Standard SQL features |
| **SQL Server** | 2017+ | ✅ Compatible | T-SQL compatible |
| **SQLite** | 3.x | ⚠️ Basic Support | Limited feature set |

#### Q: Are database-specific functions supported?

**A:** Yes, use `CustomFunctionPredicate` for database-specific functions:

```java
// PostgreSQL-specific
new CustomFunctionPredicate("DATE_TRUNC", "created_at", 
    new Object[]{"'day'"}, "p")

// MySQL-specific
new CustomFunctionPredicate("DATE_FORMAT", "created_at",
    new Object[]{"'%Y-%m-%d'"}, "p")

// Oracle-specific
new CustomFunctionPredicate("TO_CHAR", "created_at",
    new Object[]{"'YYYY-MM-DD'"}, "p")
```

#### Q: Does Query4j handle database-specific pagination?

**A:** Yes, Query4j generates appropriate SQL for each database:

```java
// Works across all databases
Page<User> page = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .page(0, 20)  // Page number, page size
    .findPage();

// PostgreSQL: LIMIT 20 OFFSET 0
// MySQL: LIMIT 20 OFFSET 0
// SQL Server: OFFSET 0 ROWS FETCH NEXT 20 ROWS ONLY
// Oracle: OFFSET 0 ROWS FETCH NEXT 20 ROWS ONLY
```

---

### Error Handling

#### Q: What exceptions should I catch when using Query4j?

**A:** Query4j uses a structured exception hierarchy:

```java
import com.github.query4j.core.DynamicQueryException;
import com.github.query4j.core.QueryBuildException;
import com.github.query4j.core.QueryExecutionException;

try {
    List<User> users = QueryBuilder.forEntity(User.class)
        .where("status", "active")
        .findAll();
        
} catch (QueryBuildException e) {
    // Handle query construction errors
    // - Invalid field names
    // - Unsupported operators
    // - Validation failures
    log.error("Query build failed: " + e.getMessage(), e);
    
} catch (QueryExecutionException e) {
    // Handle runtime/database errors
    // - SQL execution failures
    // - Connection issues
    // - Deadlocks/timeouts
    log.error("Query execution failed: " + e.getMessage(), e);
    
} catch (DynamicQueryException e) {
    // Catch-all for other Query4j errors
    log.error("Query4j error: " + e.getMessage(), e);
}
```

#### Q: How do I handle validation errors for field names?

**A:** Query4j validates field names automatically. Valid field names must:
- Not be null or empty
- Match pattern: `[A-Za-z0-9_\.]+`
- Contain only alphanumeric characters, underscores, and dots

```java
// ✅ Valid field names
.where("username", "john")
.where("user_id", 123)
.where("account.balance", 1000)

// ❌ Invalid - will throw QueryBuildException
.where("user-name", "john")     // Contains hyphen
.where("user name", "john")     // Contains space
.where("", "value")             // Empty field name
.where(null, "value")           // Null field name
```

#### Q: What should I do when I get an "Invalid operator" error?

**A:** Ensure you're using supported SQL operators. See the [ERRORS.md](../ERRORS.md) document for the complete list:

```java
// Supported operators
"=", "!=", "<>", "<", "<=", ">", ">="        // Comparison
"LIKE", "NOT LIKE", "ILIKE", "NOT ILIKE"     // Pattern matching
"IS", "IS NOT"                                // Null checking
"IN", "NOT IN", "BETWEEN", "NOT BETWEEN"     // Set operations
"EXISTS", "NOT EXISTS"                        // Existence checking

// ❌ Unsupported operator
.where("age", "BETWEEN", 18)  // Wrong - use whereBetween()

// ✅ Correct usage
.whereBetween("age", 18, 65)
```

---

### Performance Optimization

#### Q: How do I optimize query performance?

**A:** Follow these best practices:

**1. Use the Query Optimizer:**
```java
import com.github.query4j.optimizer.QueryOptimizer;
import com.github.query4j.optimizer.OptimizerConfig;

// Create optimizer with custom configuration
OptimizerConfig config = OptimizerConfig.highPerformanceConfig();
QueryOptimizer optimizer = QueryOptimizer.create(config);

// Analyze query for optimization suggestions
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .where("department", "Engineering")
    .and()
    .where("salary", ">", 50000);

OptimizationResult result = optimizer.analyze(query);

// Review suggestions
result.getIndexSuggestions().forEach(suggestion -> {
    System.out.println("Suggested index: " + suggestion.getFieldNames());
    System.out.println("Expected impact: " + suggestion.getExpectedImpact());
});
```

**2. Enable Caching:**
```java
// Cache frequently accessed queries
Page<User> users = QueryBuilder.forEntity(User.class)
    .where("status", "active")
    .orderBy("lastName")
    .page(0, 20)
    .cached(3600)  // Cache for 1 hour
    .findPage();
```

**3. Use Pagination:**
```java
// Don't load all results at once
Page<User> page = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .page(0, 100)  // Limit results
    .findPage();
```

**4. Optimize Predicates:**
```java
// ✅ Good - specific predicates first
.where("user_id", 123)           // High selectivity
.and()
.where("status", "active")       // Lower selectivity

// ⚠️ Less optimal - broad predicates first
.where("status", "active")       // Many matches
.and()
.where("user_id", 123)           // Single match
```

#### Q: What are the performance benchmarks?

**A:** Query4j is highly optimized:

| Operation | Performance | Notes |
|-----------|-------------|-------|
| Basic Query Build | ~1.7 μs | Simple WHERE clause |
| Moderate Query Build | ~6.7 μs | Multiple predicates with AND/OR |
| Complex Query Build | ~17.1 μs | Nested groups, joins, aggregations |
| Optimizer Analysis | ~0.4 μs | Query analysis overhead |

All operations target sub-millisecond performance for typical use cases.

#### Q: How can I monitor query performance?

**A:** Use query statistics:

```java
// Enable statistics in configuration
query4j.core.queryStatisticsEnabled=true

// Statistics are tracked automatically (when available)
QueryStats stats = query.getStats();
System.out.println("Execution time: " + stats.getExecutionTimeMs() + "ms");
System.out.println("Rows returned: " + stats.getRowCount());
```

---

### Version Compatibility

#### Q: What Java versions are supported?

**A:** Query4j requires **Java 17** or higher. It uses modern Java features including:
- Records and sealed classes (Java 17)
- Pattern matching enhancements
- Text blocks for better SQL readability
- Modern concurrency APIs

#### Q: How do I upgrade from an older version?

**A:** Check the [Migration Guide](API_GUIDE.md#migration-guide) in the API documentation. General steps:

1. Review the [CHANGELOG](../CHANGELOG.md) for breaking changes
2. Update your dependency version
3. Run tests to identify compatibility issues
4. Update deprecated API usages
5. Verify configuration changes

```gradle
// Update Gradle dependency
dependencies {
    implementation 'com.github.query4j:dynamicquerybuilder-core:1.0.0'
}
```

#### Q: Is Query4j compatible with Spring Boot 3.x?

**A:** Yes, Query4j works with Spring Boot 3.x and Spring Framework 6.x. It's framework-agnostic and integrates seamlessly with Spring's dependency injection:

```java
@Service
public class UserService {
    
    public List<User> findActiveUsers() {
        return QueryBuilder.forEntity(User.class)
            .where("active", true)
            .findAll();
    }
}
```

#### Q: Can I use Query4j with JPA/Hibernate?

**A:** Yes! Query4j complements JPA/Hibernate by providing dynamic query building:

```java
@Repository
public class UserRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public List<User> dynamicSearch(Map<String, Object> filters) {
        QueryBuilder<User> query = QueryBuilder.forEntity(User.class);
        
        // Build query from filters
        // ... add conditions dynamically
        
        String sql = query.toSQL();
        Map<String, Object> params = query.getParameters();
        
        // Execute with JPA
        TypedQuery<User> jpaQuery = entityManager.createQuery(sql, User.class);
        params.forEach(jpaQuery::setParameter);
        
        return jpaQuery.getResultList();
    }
}
```

---

## Troubleshooting Guide

### Query Build Exceptions

#### Problem: `QueryBuildException: Field name must not be empty`

**Cause:** Empty or whitespace-only field name passed to a predicate method.

**Solution:**
```java
// ❌ Wrong - empty field name
.where("", "value")
.where("   ", "value")

// ✅ Correct
.where("username", "value")
```

**Diagnosis Steps:**
1. Check for null or empty strings in field name variables
2. Verify string trimming doesn't result in empty values
3. Ensure field names are properly initialized from configuration/constants

---

#### Problem: `QueryBuildException: Field name contains invalid characters`

**Cause:** Field name contains characters outside the allowed pattern `[A-Za-z0-9_\.]+`.

**Solution:**
```java
// ❌ Wrong - invalid characters
.where("user-name", "john")      // Hyphen not allowed
.where("user name", "john")      // Space not allowed
.where("user@email", "value")    // @ not allowed

// ✅ Correct
.where("user_name", "john")
.where("userName", "john")
.where("user.name", "john")      // Dot allowed for nested fields
```

**Diagnosis Steps:**
1. Review field names for special characters
2. Use only alphanumeric, underscore, and dot characters
3. Consider using constants for field names to avoid typos

---

#### Problem: `QueryBuildException: Invalid operator`

**Cause:** Unsupported or misspelled SQL operator.

**Solution:**
```java
// ❌ Wrong - unsupported operators
.where("age", "GREATER_THAN", 18)
.where("name", "EQUALS", "John")

// ✅ Correct - use supported operators
.where("age", ">", 18)
.where("name", "=", "John")

// Or use convenience methods
.whereBetween("age", 18, 65)
.whereIn("role", Arrays.asList("admin", "user"))
```

**Supported Operators:**
- Comparison: `=`, `!=`, `<>`, `<`, `<=`, `>`, `>=`
- Pattern: `LIKE`, `NOT LIKE`, `ILIKE`, `NOT ILIKE`
- Null: `IS`, `IS NOT`
- Set: `IN`, `NOT IN`, `BETWEEN`, `NOT BETWEEN`
- Existence: `EXISTS`, `NOT EXISTS`

**See Also:** [ERRORS.md - Operator Validation Errors](../ERRORS.md#operator-validation-errors)

---

#### Problem: `QueryBuildException: Values list must not be empty`

**Cause:** Empty list passed to `whereIn()` or `whereNotIn()` method.

**Solution:**
```java
// ❌ Wrong - empty list
List<String> roles = new ArrayList<>();
.whereIn("role", roles)

// ✅ Correct - check before using
List<String> roles = Arrays.asList("admin", "user", "developer");
if (!roles.isEmpty()) {
    query = query.whereIn("role", roles);
}

// ✅ Alternative - handle optional IN clause
List<String> roles = getUserRoles();
if (roles != null && !roles.isEmpty()) {
    query = query.whereIn("role", roles);
}
```

---

#### Problem: `QueryBuildException: NOT operator must have exactly one child predicate`

**Cause:** Using `not()` with multiple predicates or without a following condition.

**Solution:**
```java
// ❌ Wrong - multiple predicates after NOT
.not()
.where("status", "deleted")
.where("archived", true)

// ✅ Correct - single predicate after NOT
.not()
.where("status", "deleted")
.and()
.where("active", true)

// ✅ Alternative - group multiple conditions
.not()
.openGroup()
    .where("status", "deleted")
    .or()
    .where("archived", true)
.closeGroup()
```

---

### Runtime and Execution Errors

#### Problem: `QueryExecutionException` during query execution

**Cause:** Database connection issues, SQL syntax errors, or constraint violations.

**Solution:**
```java
try {
    List<User> users = QueryBuilder.forEntity(User.class)
        .where("status", "active")
        .findAll();
        
} catch (QueryExecutionException e) {
    // Log the full error with context
    log.error("Query execution failed", e);
    
    // Check the generated SQL
    String sql = query.toSQL();
    log.debug("Generated SQL: {}", sql);
    log.debug("Parameters: {}", query.getParameters());
    
    // Handle specific database errors
    if (e.getCause() instanceof SQLException) {
        SQLException sqlEx = (SQLException) e.getCause();
        log.error("SQL Error Code: {}", sqlEx.getErrorCode());
        log.error("SQL State: {}", sqlEx.getSQLState());
    }
}
```

**Diagnosis Steps:**
1. Review the generated SQL using `query.toSQL()`
2. Check parameter bindings with `query.getParameters()`
3. Test the SQL directly in your database client
4. Verify database connectivity and permissions
5. Check for constraint violations or data type mismatches
6. Review database-specific error codes

---

#### Problem: Connection timeouts or slow queries

**Cause:** Network issues, missing indexes, or inefficient query structure.

**Solution:**

**1. Configure query timeout:**
```properties
query4j.core.defaultQueryTimeoutMs=30000
```

**2. Optimize the query:**
```java
// Add appropriate indexes based on WHERE clauses
// Use pagination to limit result sets
Page<User> users = QueryBuilder.forEntity(User.class)
    .where("department", "Engineering")
    .page(0, 100)
    .findPage();

// Use caching for frequently accessed data
.cached(3600)
```

**3. Analyze with optimizer:**
```java
OptimizerConfig config = OptimizerConfig.developmentConfig()
    .withVerboseOutput(true);
QueryOptimizer optimizer = QueryOptimizer.create(config);
OptimizationResult result = optimizer.analyze(query);

// Review index suggestions
result.getIndexSuggestions().forEach(suggestion -> {
    log.info("Consider adding index on: {}", suggestion.getFieldNames());
});
```

**4. Enable query logging:**
```properties
# Log all SQL queries
logging.level.com.github.query4j=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

---

### Cache Issues

#### Problem: Cache not working or cache misses

**Cause:** Cache disabled, incorrect configuration, or cache key collisions.

**Solution:**

**1. Verify cache is enabled:**
```properties
query4j.cache.enabled=true
query4j.cache.defaultTtlSeconds=3600
```

**2. Check cache statistics:**
```java
// Enable cache statistics
CacheConfig config = CacheConfig.builder()
    .enabled(true)
    .statisticsEnabled(true)
    .build();

// Monitor cache performance (when statistics API is available)
// log.info("Cache hit rate: {}", cacheStats.getHitRate());
// log.info("Cache miss rate: {}", cacheStats.getMissRate());
```

**3. Verify TTL configuration:**
```java
// Ensure TTL is positive
.cached(3600)  // ✅ 1 hour cache

.cached(0)     // ❌ Disables caching
.cached(-1)    // ❌ Disables caching
```

**4. Check cache size:**
```properties
# Increase cache size if needed
query4j.cache.maxSize=20000
```

---

#### Problem: `IllegalArgumentException: TTL seconds must not be negative`

**Cause:** Negative TTL value passed to cache configuration.

**Solution:**
```java
// ❌ Wrong - negative TTL
.cached(-100)

// ✅ Correct - positive TTL or zero to disable
.cached(3600)  // Cache for 1 hour
.cached(0)     // Disable caching for this query
```

---

#### Problem: Memory issues with large cache

**Cause:** Cache size too large for available memory.

**Solution:**

**1. Reduce cache size:**
```properties
query4j.cache.maxSize=5000
```

**2. Reduce TTL:**
```properties
query4j.cache.defaultTtlSeconds=1800  # 30 minutes
```

**3. Enable cache maintenance:**
```properties
query4j.cache.maintenanceIntervalSeconds=300  # 5 minutes
```

**4. Monitor JVM memory:**
```bash
# Start application with memory settings
java -Xms512m -Xmx2g -XX:+UseG1GC -jar application.jar
```

---

### Optimizer Problems

#### Problem: Optimizer analysis takes too long

**Cause:** Complex query with many predicates or joins.

**Solution:**

**1. Configure analysis timeout:**
```java
OptimizerConfig config = OptimizerConfig.builder()
    .maxAnalysisTimeMs(5000)  // 5 second timeout
    .build();
```

**2. Disable unnecessary optimizations:**
```java
OptimizerConfig config = OptimizerConfig.builder()
    .indexSuggestionsEnabled(true)
    .predicatePushdownEnabled(false)    // Disable if not needed
    .joinReorderingEnabled(false)       // Disable if not needed
    .build();
```

**3. Use development config for verbose output:**
```java
OptimizerConfig config = OptimizerConfig.developmentConfig();
```

---

#### Problem: Optimizer suggestions not applicable

**Cause:** Database-specific features or constraints not detected.

**Solution:**

**1. Configure target database:**
```java
OptimizerConfig config = OptimizerConfig.builder()
    .targetDatabase(OptimizerConfig.DatabaseType.POSTGRESQL)
    .build();
```

**2. Review suggestions carefully:**
```java
OptimizationResult result = optimizer.analyze(query);

result.getIndexSuggestions().forEach(suggestion -> {
    // Consider database-specific factors
    log.info("Suggested index: {}", suggestion.getFieldNames());
    log.info("Impact: {}", suggestion.getExpectedImpact());
    log.info("Applicable to {}: {}", dbType, isApplicable(suggestion));
});
```

**3. Provide custom optimization hints:**
```java
// Add database hints (when hint API is available)
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .where("status", "active")
    .hint("USE_INDEX", "idx_status");  // Database-specific hint
```

---

### Async and Batch Processing

#### Problem: Deadlocks or race conditions in concurrent query execution

**Cause:** Query4j is thread-safe, but database connection management may not be.

**Solution:**

**1. Use proper connection pooling:**
```properties
# HikariCP configuration
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
```

**2. Implement retry logic:**
```java
import java.util.concurrent.CompletableFuture;

public CompletableFuture<List<User>> asyncQuery() {
    return CompletableFuture.supplyAsync(() -> {
        int retries = 3;
        while (retries > 0) {
            try {
                return QueryBuilder.forEntity(User.class)
                    .where("status", "active")
                    .findAll();
            } catch (QueryExecutionException e) {
                if (isDeadlock(e) && retries > 1) {
                    retries--;
                    Thread.sleep(100 * (4 - retries));  // Exponential backoff
                } else {
                    throw e;
                }
            }
        }
        throw new RuntimeException("Failed after retries");
    });
}
```

**3. Use appropriate isolation levels:**
```java
@Transactional(isolation = Isolation.READ_COMMITTED)
public List<User> findUsers() {
    return QueryBuilder.forEntity(User.class)
        .where("active", true)
        .findAll();
}
```

---

#### Problem: Memory issues with batch processing

**Cause:** Loading too many records at once.

**Solution:**

**1. Use pagination:**
```java
public void processBatch() {
    int pageSize = 1000;
    int pageNumber = 0;
    Page<User> page;
    
    do {
        page = QueryBuilder.forEntity(User.class)
            .where("processed", false)
            .page(pageNumber++, pageSize)
            .findPage();
        
        // Process page
        processUsers(page.getContent());
        
    } while (page.hasNext());
}
```

**2. Stream processing (when available):**
```java
// Process records in batches without loading all into memory
QueryBuilder.forEntity(User.class)
    .where("status", "pending")
    .stream()
    .forEach(user -> processUser(user));
```

**3. Clear cache between batches:**
```java
// If using JPA/Hibernate
entityManager.clear();

// Query4j cache clearing (when cache API is available)
// cacheManager.clear();
```

See [examples/README-CONSUMER-APPS.md](../examples/README-CONSUMER-APPS.md) for complete batch processing examples.

---

### Debugging and Logging

#### Problem: Need to see generated SQL for debugging

**Solution:**

**1. Use `toSQL()` method:**
```java
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .where("status", "active")
    .and()
    .whereLike("name", "John%");

// Log the generated SQL
String sql = query.toSQL();
Map<String, Object> params = query.getParameters();

log.debug("Generated SQL: {}", sql);
log.debug("Parameters: {}", params);
```

**2. Enable SQL logging:**
```properties
# Query4j logging
logging.level.com.github.query4j=DEBUG
logging.level.com.github.query4j.core=TRACE

# JPA/Hibernate SQL logging
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# Show SQL with parameters
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

**3. Use logback configuration:**
```xml
<!-- logback-spring.xml -->
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <!-- Query4j logging -->
    <logger name="com.github.query4j" level="DEBUG"/>
    <logger name="com.github.query4j.core" level="TRACE"/>
    
    <!-- SQL logging -->
    <logger name="org.hibernate.SQL" level="DEBUG"/>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
```

---

#### Problem: Understanding optimizer suggestions

**Solution:**

**1. Enable verbose output:**
```java
OptimizerConfig config = OptimizerConfig.builder()
    .verboseOutput(true)
    .build();

QueryOptimizer optimizer = QueryOptimizer.create(config);
OptimizationResult result = optimizer.analyze(query);
```

**2. Interpret suggestions:**
```java
result.getIndexSuggestions().forEach(suggestion -> {
    System.out.println("=== Index Suggestion ===");
    System.out.println("Fields: " + suggestion.getFieldNames());
    System.out.println("Type: " + suggestion.getOptimizationType());
    System.out.println("Impact: " + suggestion.getExpectedImpact());
    System.out.println("Reason: " + suggestion.getRationale());
});

result.getPredicatePushdownSuggestions().forEach(suggestion -> {
    System.out.println("=== Predicate Pushdown ===");
    System.out.println("Original: " + suggestion.getOriginalPredicate());
    System.out.println("Optimized: " + suggestion.getOptimizedPredicate());
});

result.getJoinReorderSuggestions().forEach(suggestion -> {
    System.out.println("=== Join Reorder ===");
    System.out.println("Original: " + suggestion.getOriginalJoinSequence());
    System.out.println("Suggested: " + suggestion.getSuggestedJoinSequence());
    System.out.println("Improvement: " + (suggestion.getEstimatedImprovement() * 100) + "%");
});
```

See [optimizer/README.md](../optimizer/README.md) for detailed optimizer documentation.

---

#### Problem: Performance profiling and benchmarking

**Solution:**

**1. Use built-in benchmarks:**
```bash
# Run all benchmarks
./gradlew benchmark:benchmark

# Run specific benchmark
./gradlew benchmark:optimizerBenchmark
```

**2. Enable query statistics:**
```properties
query4j.core.queryStatisticsEnabled=true
```

**3. Profile with JMH:**
```java
// See benchmark module for complete examples
@Benchmark
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public void benchmarkComplexQuery(Blackhole blackhole) {
    QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
        .where("department", "Engineering")
        .and()
        .whereIn("role", roles)
        .orderBy("lastName")
        .limit(100);
    
    blackhole.consume(query.toSQL());
}
```

See [benchmark/README.md](../benchmark/README.md) for complete benchmarking guide.

---

#### Problem: Debugging test failures

**Solution:**

**1. Use detailed assertions:**
```java
@Test
void testComplexQuery() {
    QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
        .where("status", "active")
        .and()
        .where("age", ">", 18);
    
    String sql = query.toSQL();
    Map<String, Object> params = query.getParameters();
    
    // Detailed assertions
    assertThat(sql)
        .contains("WHERE")
        .contains("status = :p0")
        .contains("AND age > :p1");
    
    assertThat(params)
        .containsEntry("p0", "active")
        .containsEntry("p1", 18);
}
```

**2. Use test fixtures:**
```java
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QueryBuilderTest {
    
    private QueryBuilder<User> baseQuery;
    
    @BeforeAll
    void setUp() {
        baseQuery = QueryBuilder.forEntity(User.class)
            .where("active", true);
    }
    
    @Test
    void testDerivedQuery() {
        QueryBuilder<User> derived = baseQuery
            .and()
            .where("role", "admin");
        
        // Test derived query
        assertThat(derived.toSQL()).contains("active = :p0", "role = :p1");
    }
}
```

---

## Community Contributions

### How to Contribute FAQ Entries

We welcome community contributions to this FAQ! If you've encountered a problem and found a solution, please share it with others.

#### Steps to Contribute:

1. **Fork the repository:**
   ```bash
   git clone https://github.com/query4j/dynamicquerybuilder.git
   cd dynamicquerybuilder
   ```

2. **Create a feature branch:**
   ```bash
   git checkout -b faq/your-question-topic
   ```

3. **Add your FAQ entry:**
   - Add your question and answer to the appropriate section
   - Use clear, concise language
   - Include code examples where helpful
   - Link to relevant documentation
   - Test all code examples

4. **Follow the format:**
   ```markdown
   #### Q: Your question here?
   
   **A:** Your answer here.
   
   ```java
   // Code example
   ```
   
   **See Also:** [Link to related docs]
   ```

5. **Submit a pull request:**
   - Ensure all code examples work
   - Verify links are correct
   - Update table of contents if needed
   - Follow the [Contributing Guide](../CONTRIBUTING.md)

### Reporting Issues Not Covered Here

If you encounter an issue not covered in this guide:

1. **Search existing issues:** Check [GitHub Issues](https://github.com/query4j/dynamicquerybuilder/issues) for similar problems

2. **Create a new issue:**
   - Use issue template
   - Include Query4j version
   - Provide minimal reproducible example
   - Include error messages and stack traces
   - Describe expected vs actual behavior

3. **Join discussions:** Participate in [GitHub Discussions](https://github.com/query4j/dynamicquerybuilder/discussions)

### Suggesting Documentation Improvements

To suggest improvements to this guide:

1. Open an issue with label `documentation`
2. Describe what's unclear or missing
3. Suggest specific improvements
4. Submit a pull request if you'd like to implement it

---

## Additional Resources

- **[README.md](../README.md)** - Project overview and quick start
- **[API Guide](API_GUIDE.md)** - Comprehensive API documentation
- **[Configuration Guide](Configuration.md)** - Detailed configuration options
- **[Error Reference](../ERRORS.md)** - Complete error message catalog
- **[Optimizer Guide](../optimizer/README.md)** - Query optimization documentation
- **[Examples](../examples/README-CONSUMER-APPS.md)** - Real-world usage examples
- **[Contributing Guide](../CONTRIBUTING.md)** - How to contribute
- **[GitHub Issues](https://github.com/query4j/dynamicquerybuilder/issues)** - Report bugs
- **[GitHub Discussions](https://github.com/query4j/dynamicquerybuilder/discussions)** - Ask questions

---

**Last Updated:** October 2025  
**Version:** 1.0.0  
**Maintainers:** Query4j Development Team

For urgent issues or security concerns, please email: security@query4j.org
