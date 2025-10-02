# FAQ and Troubleshooting

Frequently asked questions and troubleshooting guide for Query4j Dynamic Query Builder.

## Table of Contents

1. [General Questions](#general-questions)
2. [Usage Questions](#usage-questions)
3. [Performance Questions](#performance-questions)
4. [Troubleshooting](#troubleshooting)
5. [Integration Questions](#integration-questions)

---

## General Questions

### What is Query4j?

Query4j is a high-performance, thread-safe Java library for building dynamic SQL queries with a fluent API. It provides immutable query builders, comprehensive predicate support, and built-in optimization capabilities.

### Why use Query4j instead of JPA or Hibernate?

**Use Query4j when:**
- You need dynamic query construction based on runtime conditions
- Performance is critical (minimal overhead vs raw JDBC)
- You want type safety without full ORM complexity
- You need fine-grained control over SQL generation
- You're building reporting or analytics applications

**Use JPA/Hibernate when:**
- You want full ORM features (entity lifecycle, lazy loading, etc.)
- You primarily work with CRUD operations
- You benefit from automatic schema generation
- Object-relational mapping is your primary need

### What databases does Query4j support?

Query4j generates standard SQL compatible with:
- ✅ PostgreSQL
- ✅ MySQL
- ✅ H2
- ✅ Oracle (basic support)
- ✅ SQL Server (basic support)

Database-specific features (window functions, CTEs) may require manual SQL construction.

### Is Query4j production-ready?

Yes! Query4j is production-ready with:
- 95%+ test coverage
- Comprehensive benchmarking
- Thread-safe immutable design
- Well-documented error handling
- Active maintenance

---

## Usage Questions

### How do I build a simple query?

```java
import com.github.query4j.core.QueryBuilder;

QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .orderBy("lastName");

String sql = query.toSQL();
Map<String, Object> params = query.getParameters();
```

See [Getting Started](Getting-Started) for complete tutorial.

### How do I handle dynamic filters?

```java
public QueryBuilder<User> buildQuery(UserCriteria criteria) {
    QueryBuilder<User> query = QueryBuilder.forEntity(User.class);
    
    if (criteria.getDepartment() != null) {
        query = query.where("department", criteria.getDepartment());
    }
    
    if (criteria.getRoles() != null && !criteria.getRoles().isEmpty()) {
        query = query.and().whereIn("role", criteria.getRoles());
    }
    
    if (criteria.getActive() != null) {
        query = query.and().where("active", criteria.getActive());
    }
    
    return query;
}
```

### How do I execute queries?

Query4j focuses on query building. Execute with your preferred data access layer:

**With JDBC:**
```java
String sql = query.toSQL();
Map<String, Object> params = query.getParameters();

try (PreparedStatement stmt = conn.prepareStatement(sql)) {
    int idx = 1;
    for (Object value : params.values()) {
        stmt.setObject(idx++, value);
    }
    
    ResultSet rs = stmt.executeQuery();
    // Process results...
}
```

**With Spring JdbcTemplate:**
```java
String sql = query.toSQL();
Map<String, Object> params = query.getParameters();

List<User> users = jdbcTemplate.query(sql, params, userRowMapper);
```

**With JPA:**
```java
String sql = query.toSQL();
Map<String, Object> params = query.getParameters();

TypedQuery<User> jpaQuery = entityManager.createQuery(sql, User.class);
params.forEach(jpaQuery::setParameter);

List<User> users = jpaQuery.getResultList();
```

### How do I handle complex AND/OR logic?

Use logical grouping:

```java
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .openGroup()
        .where("department", "Engineering")
        .or()
        .where("department", "Sales")
    .closeGroup()
    .and()
    .openGroup()
        .where("level", "senior")
        .or()
        .where("level", "lead")
    .closeGroup();

// SQL: WHERE (department = :p1 OR department = :p2) 
//        AND (level = :p3 OR level = :p4)
```

### Can I use Query4j with Spring Boot?

Yes! Create a configuration bean:

```java
@Configuration
public class Query4jConfig {
    
    @Bean
    public QueryCacheManager queryCacheManager() {
        return QueryCacheManager.withLRU(1000);
    }
    
    @Bean
    public QueryOptimizer queryOptimizer() {
        return QueryOptimizer.create(
            OptimizerConfig.highPerformanceConfig());
    }
}
```

---

## Performance Questions

### What is the performance overhead?

From benchmarks:
- **Simple query construction:** 0.8 µs (sub-millisecond)
- **Complex query:** 2.8 µs
- **vs Raw JDBC:** 3.3× construction time, 1.05× execution time
- **vs Hibernate:** 1.3× faster construction, 6× faster execution

See [Benchmarking](Benchmarking) for details.

### Should I use caching?

**Use caching for:**
- Frequently accessed queries
- Read-heavy workloads
- Expensive queries
- Predictable data

**Example:**
```java
QueryCacheManager cache = QueryCacheManager.withLRU(500);

String key = query.toSQL();
List<User> users = cache.get(key);

if (users == null) {
    users = executeQuery(query);
    cache.put(key, users);
}
```

See [Cache Manager](Cache-Manager) for configuration.

### When should I use the optimizer?

Use optimizer:
- ✅ In development/staging for query analysis
- ✅ For complex queries with JOINs
- ✅ When database performance is critical
- ❌ Not in hot paths (adds 5-15% overhead)

```java
// Analyze during development
if (isDevelopment()) {
    OptimizationResult result = optimizer.analyze(query);
    logSuggestions(result);
}
```

### How can I optimize query performance?

1. **Use indexes** (apply optimizer suggestions)
2. **Enable caching** for frequently accessed queries
3. **Paginate** large result sets
4. **Limit** SELECT columns
5. **Optimize JOINs** (use optimizer's join reordering)

---

## Troubleshooting

### Query returns wrong results

**Debug steps:**

1. Print generated SQL:
```java
System.out.println("SQL: " + query.toSQL());
System.out.println("Params: " + query.getParameters());
```

2. Test SQL directly in database client

3. Verify field names match database columns

4. Check parameter types match column types

### "Field name does not match required pattern" error

Field names must match `[A-Za-z0-9_\.]+`:

```java
// ❌ Invalid
.where("user-name", value)    // Hyphen not allowed
.where("user name", value)    // Space not allowed

// ✅ Valid
.where("user_name", value)    // Underscore OK
.where("userName", value)     // CamelCase OK
.where("table.column", value) // Qualified OK
```

### "Empty IN collection" error

Cannot call `whereIn()` with empty collection:

```java
// ❌ Invalid
.whereIn("role", Collections.emptyList())

// ✅ Valid - check before adding
if (!roles.isEmpty()) {
    query = query.whereIn("role", roles);
}
```

### Query is slow

1. **Enable optimizer analysis:**
```java
OptimizationResult result = optimizer.analyze(query);
result.getIndexSuggestions().forEach(s -> 
    System.out.println(s.getIndexDefinition()));
```

2. **Check database execution plan:**
```sql
EXPLAIN ANALYZE [your generated SQL]
```

3. **Add appropriate indexes**

4. **Enable query caching**

### Cache not working

**Common causes:**

1. **Cache disabled:**
```java
// Check configuration
CacheConfig config = CacheConfig.builder()
    .maxSize(100)  // Must be > 0
    .build();
```

2. **Cache key collision:**
```java
// Use unique keys
String key = query.toSQL() + "|" + query.getParameters();
```

3. **TTL too short:**
```java
.ttl(Duration.ofMinutes(10))  // Increase if too short
```

### OutOfMemoryError with cache

**Solutions:**

1. Reduce cache size:
```java
.maxSize(500)  // Smaller cache
```

2. Add TTL:
```java
.ttl(Duration.ofMinutes(30))  // Expire old entries
```

3. Clear periodically:
```java
@Scheduled(fixedRate = 3600000)
public void clearCache() {
    cache.clear();
}
```

---

## Integration Questions

### How do I integrate with Spring Data?

```java
@Repository
public class UserRepositoryImpl {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public List<User> findByCriteria(UserCriteria criteria) {
        QueryBuilder<User> query = buildQuery(criteria);
        
        return jdbcTemplate.query(
            query.toSQL(),
            query.getParameters(),
            new BeanPropertyRowMapper<>(User.class)
        );
    }
}
```

### Can I use Query4j with Kotlin?

Yes! Query4j is Java-based and fully compatible with Kotlin:

```kotlin
val query = QueryBuilder.forEntity(User::class.java)
    .where("active", true)
    .and()
    .whereIn("role", listOf("admin", "user"))
    .orderBy("lastName")

val sql = query.toSQL()
val params = query.getParameters()
```

### How do I handle transactions?

Query4j doesn't manage transactions. Use your existing transaction management:

**Spring:**
```java
@Transactional
public void updateUsers(UserCriteria criteria) {
    QueryBuilder<User> query = buildQuery(criteria);
    // Execute query within transaction
}
```

**Manual:**
```java
try {
    conn.setAutoCommit(false);
    
    // Execute queries
    
    conn.commit();
} catch (Exception e) {
    conn.rollback();
    throw e;
}
```

---

## Still Have Questions?

### Documentation
- [Getting Started](Getting-Started) - Setup and basics
- [Core Module](Core-Module) - Complete API reference
- [Error Handling](Error-Handling) - Error catalog

### Community
- [GitHub Discussions](https://github.com/query4j/dynamicquerybuilder/discussions) - Ask questions
- [GitHub Issues](https://github.com/query4j/dynamicquerybuilder/issues) - Report bugs
- [Stack Overflow](https://stackoverflow.com/questions/tagged/query4j) - Tag: `query4j`

### Contributing
- [Contributing Guide](Contributing) - Help improve Query4j
- [Report an Issue](https://github.com/query4j/dynamicquerybuilder/issues/new)

---

**Last Updated:** October 2025  
**Version:** 1.0.0
