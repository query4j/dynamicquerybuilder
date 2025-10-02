# Query4j Advanced Usage Tutorial

This tutorial covers advanced features and patterns for building complex, high-performance queries with Query4j. You'll learn about joins, subqueries, aggregations, optimization, caching, and production-ready patterns.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Complex Query Composition](#complex-query-composition)
- [JOIN Operations](#join-operations)
- [Subqueries](#subqueries)
- [Aggregations and GROUP BY](#aggregations-and-group-by)
- [Query Optimization](#query-optimization)
- [Result Caching](#result-caching)
- [Asynchronous Execution](#asynchronous-execution)
- [Batch Processing](#batch-processing)
- [Configuration Customization](#configuration-customization)
- [Performance Tuning](#performance-tuning)
- [Best Practices](#best-practices)
- [Common Pitfalls](#common-pitfalls)

## Prerequisites

Before diving into advanced topics, you should:

- Complete the [Quickstart Tutorial](QUICKSTART.md)
- Understand basic SQL concepts (joins, subqueries, aggregations)
- Have Query4j integrated into your project
- Be familiar with Java 17+ features (CompletableFuture, Streams API)

## Complex Query Composition

### Nested Logical Groups

Build complex boolean logic with multiple levels of nesting:

```java
import com.github.query4j.core.QueryBuilder;
import java.time.LocalDate;
import java.util.Arrays;

// Find users who meet complex business criteria:
// Active users in Engineering/Product who are either:
//   - Managers with 2+ years tenure, OR
//   - Developers hired in the last 6 months with senior level
List<User> complexQuery = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .and()
    .openGroup()
        .where("department", "Engineering")
        .or()
        .where("department", "Product")
    .closeGroup()
    .and()
    .openGroup()
        .openGroup()
            .where("role", "manager")
            .and()
            .where("joinDate", "<=", LocalDate.now().minusYears(2))
        .closeGroup()
        .or()
        .openGroup()
            .where("role", "developer")
            .and()
            .where("level", "senior")
            .and()
            .where("joinDate", ">=", LocalDate.now().minusMonths(6))
        .closeGroup()
    .closeGroup()
    .orderByDescending("joinDate")
    .findAll();
```

**Generated SQL:**
```sql
SELECT * FROM User 
WHERE active = :p1 
  AND (department = :p2 OR department = :p3)
  AND (
    (role = :p4 AND joinDate <= :p5) 
    OR 
    (role = :p6 AND level = :p7 AND joinDate >= :p8)
  )
ORDER BY joinDate DESC
```

### Dynamic Query Building

Build queries conditionally based on runtime parameters:

```java
public List<User> searchUsers(UserSearchCriteria criteria) {
    QueryBuilder<User> query = QueryBuilder.forEntity(User.class);
    
    // Always filter active users
    query = query.where("active", true);
    
    // Add optional filters only if provided
    if (criteria.getDepartments() != null && !criteria.getDepartments().isEmpty()) {
        query = query.and().whereIn("department", criteria.getDepartments());
    }
    
    if (criteria.getMinSalary() != null) {
        query = query.and().where("salary", ">=", criteria.getMinSalary());
    }
    
    if (criteria.getMaxSalary() != null) {
        query = query.and().where("salary", "<=", criteria.getMaxSalary());
    }
    
    if (criteria.getSearchTerm() != null && !criteria.getSearchTerm().isEmpty()) {
        String pattern = "%" + criteria.getSearchTerm() + "%";
        query = query.and().openGroup()
            .whereLike("firstName", pattern)
            .or()
            .whereLike("lastName", pattern)
            .or()
            .whereLike("email", pattern)
            .closeGroup();
    }
    
    if (criteria.getJoinedAfter() != null) {
        query = query.and().where("joinDate", ">=", criteria.getJoinedAfter());
    }
    
    if (criteria.getJoinedBefore() != null) {
        query = query.and().where("joinDate", "<=", criteria.getJoinedBefore());
    }
    
    // Apply sorting
    String sortField = criteria.getSortField() != null ? criteria.getSortField() : "lastName";
    boolean ascending = criteria.isSortAscending();
    
    if (ascending) {
        query = query.orderBy(sortField);
    } else {
        query = query.orderByDescending(sortField);
    }
    
    // Apply pagination
    if (criteria.getPageSize() > 0) {
        query = query.page(criteria.getPageNumber(), criteria.getPageSize());
        return query.findPage().getContent();
    }
    
    return query.findAll();
}
```

## JOIN Operations

### Inner Joins

Join related entities to access associated data:

```java
// Find users with their orders
List<User> usersWithOrders = QueryBuilder.forEntity(User.class)
    .join("orders")
    .where("orders.status", "COMPLETED")
    .and()
    .where("orders.totalAmount", ">", 1000.0)
    .findAll();
```

### Left Joins

Include users even if they don't have matching orders:

```java
// Find all active users and their orders (if any)
List<User> usersAndOrders = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .leftJoin("orders")
    .orderBy("lastName")
    .findAll();
```

### Multiple Joins

Join multiple related entities:

```java
// Users -> Orders -> OrderItems -> Products
List<User> userOrderDetails = QueryBuilder.forEntity(User.class)
    .join("orders")
    .join("orders.items")
    .join("orders.items.product")
    .where("orders.orderDate", ">=", LocalDate.now().minusMonths(1))
    .and()
    .where("orders.items.product.category", "Electronics")
    .findAll();
```

### Complex Join with Aggregation

Find customers with their order statistics:

```java
// Customer order summary
List<User> customerStats = QueryBuilder.forEntity(User.class)
    .select("id", "firstName", "lastName", "email")
    .count("orders.id")
    .sum("orders.totalAmount")
    .avg("orders.totalAmount")
    .max("orders.orderDate")
    .leftJoin("orders")
    .where("active", true)
    .groupBy("id", "firstName", "lastName", "email")
    .having("COUNT(orders.id)", ">", 0)
    .orderByDescending("SUM(orders.totalAmount)")
    .findAll();
```

**Generated SQL:**
```sql
SELECT 
    u.id, u.firstName, u.lastName, u.email,
    COUNT(o.id),
    SUM(o.totalAmount),
    AVG(o.totalAmount),
    MAX(o.orderDate)
FROM User u
LEFT JOIN orders o ON u.id = o.customerId
WHERE u.active = :p1
GROUP BY u.id, u.firstName, u.lastName, u.email
HAVING COUNT(o.id) > :p2
ORDER BY SUM(o.totalAmount) DESC
```

### Fetch Joins for Performance

Use fetch joins to avoid N+1 query problems:

```java
// Eagerly load users with their orders in a single query
List<User> usersWithOrders = QueryBuilder.forEntity(User.class)
    .fetch("orders")
    .where("active", true)
    .findAll();
```

## Subqueries

### EXISTS Subqueries

Check for the existence of related records:

```java
// Find users who have placed orders
List<User> usersWithOrders = QueryBuilder.forEntity(User.class)
    .exists(
        QueryBuilder.forEntity(Order.class)
            .where("customerId", "users.id")
    )
    .findAll();
```

### NOT EXISTS Subqueries

Find records without related data:

```java
// Find users who have never placed an order
List<User> usersWithoutOrders = QueryBuilder.forEntity(User.class)
    .notExists(
        QueryBuilder.forEntity(Order.class)
            .where("customerId", "users.id")
    )
    .findAll();
```

### IN Subqueries

Filter based on subquery results:

```java
// Find users who have orders in the "Electronics" category
List<User> electronicsCustomers = QueryBuilder.forEntity(User.class)
    .in("id",
        QueryBuilder.forEntity(Order.class)
            .select("customerId")
            .join("items")
            .join("items.product")
            .where("items.product.category", "Electronics")
    )
    .findAll();
```

### Correlated Subqueries

Subqueries that reference the outer query:

```java
// Find users whose most recent order was over $1000
List<User> bigSpenders = QueryBuilder.forEntity(User.class)
    .exists(
        QueryBuilder.forEntity(Order.class)
            .where("customerId", "User.id")
            .and()
            .where("totalAmount", ">", 1000.0)
            .and()
            .where("orderDate", "=",
                QueryBuilder.forEntity(Order.class)
                    .select("MAX(orderDate)")
                    .where("customerId", "User.id")
            )
    )
    .findAll();
```

## Aggregations and GROUP BY

### Basic Aggregations

Calculate statistics across your dataset:

```java
// Count all active users
long activeUserCount = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .count();

// Get average order value
double avgOrderValue = QueryBuilder.forEntity(Order.class)
    .where("status", "COMPLETED")
    .avg("totalAmount");

// Find highest order amount
double maxOrderValue = QueryBuilder.forEntity(Order.class)
    .max("totalAmount");
```

### GROUP BY with Aggregations

Analyze data by groups:

```java
// Sales by department
List<DepartmentStats> departmentSales = QueryBuilder.forEntity(User.class)
    .select("department")
    .count("id")
    .sum("orders.totalAmount")
    .avg("orders.totalAmount")
    .join("orders")
    .where("orders.status", "COMPLETED")
    .groupBy("department")
    .orderByDescending("SUM(orders.totalAmount)")
    .findAll();
```

### HAVING Clause

Filter aggregated results:

```java
// Find departments with more than 10 employees and average salary > 75k
List<Department> largeDepartments = QueryBuilder.forEntity(User.class)
    .select("department")
    .count("id")
    .avg("salary")
    .where("active", true)
    .groupBy("department")
    .having("COUNT(id)", ">", 10)
    .and()
    .having("AVG(salary)", ">", 75000.0)
    .orderBy("department")
    .findAll();
```

### Multiple Dimensions

Group by multiple fields:

```java
// Sales by region and category
List<RegionCategoryStats> salesStats = QueryBuilder.forEntity(Order.class)
    .select("customer.region", "items.product.category")
    .count("id")
    .sum("totalAmount")
    .avg("totalAmount")
    .join("customer")
    .join("items")
    .join("items.product")
    .where("status", "COMPLETED")
    .groupBy("customer.region", "items.product.category")
    .having("COUNT(id)", ">=", 5)
    .orderBy("customer.region")
    .orderByDescending("SUM(totalAmount)")
    .findAll();
```

## Query Optimization

Query4j includes an optimizer module for analyzing and improving query performance.

### Basic Optimization

Enable the optimizer for query analysis:

```java
import com.github.query4j.optimizer.QueryOptimizer;
import com.github.query4j.optimizer.OptimizerConfig;
import com.github.query4j.optimizer.OptimizationSuggestion;

// Configure optimizer
OptimizerConfig config = OptimizerConfig.builder()
    .indexSuggestionsEnabled(true)
    .predicatePushdownEnabled(true)
    .joinReorderingEnabled(true)
    .verboseOutput(true)
    .build();

QueryOptimizer optimizer = new QueryOptimizerImpl(config);

// Build your query
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .where("department", "Engineering")
    .and()
    .where("salary", ">", 100000)
    .orderBy("lastName");

// Analyze the query
List<OptimizationSuggestion> suggestions = optimizer.analyze(query);

// Review suggestions
for (OptimizationSuggestion suggestion : suggestions) {
    System.out.println("Type: " + suggestion.getType());
    System.out.println("Severity: " + suggestion.getSeverity());
    System.out.println("Message: " + suggestion.getMessage());
    System.out.println("SQL Impact: " + suggestion.getExpectedImprovement());
    System.out.println();
}
```

### Index Recommendations

The optimizer suggests indexes based on query patterns:

```java
// Optimizer output example:
// Type: MISSING_INDEX
// Severity: HIGH
// Message: Consider creating index on User(department, salary)
// SQL Impact: Query performance could improve by 10-100x
// 
// Suggested DDL:
// CREATE INDEX idx_user_dept_salary ON User(department, salary);
```

### Query Rewriting

Apply optimizer suggestions automatically:

```java
// Let optimizer rewrite the query for better performance
QueryBuilder<User> optimized = optimizer.optimize(query);

// Execute the optimized query
List<User> results = optimized.findAll();
```

### Performance Analysis

Collect detailed execution metrics:

```java
import com.github.query4j.core.QueryStats;

// Build and execute query with statistics collection
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .collectStats(true);

List<User> results = query.findAll();

// Access execution statistics
QueryStats stats = query.getStats();
System.out.println("Execution time: " + stats.getExecutionTimeMs() + "ms");
System.out.println("Rows returned: " + stats.getRowCount());
System.out.println("Cache hit: " + stats.isCacheHit());
System.out.println("Query complexity: " + stats.getComplexityScore());
```

## Result Caching

Query4j supports result caching to improve performance for frequently-executed queries.

### Basic Caching

Enable caching for a query:

```java
import com.github.query4j.cache.CacheManager;
import com.github.query4j.cache.CacheConfig;

// Configure cache manager
CacheConfig cacheConfig = CacheConfig.builder()
    .enabled(true)
    .maxSize(10000)
    .defaultTtlSeconds(3600)  // 1 hour
    .build();

CacheManager cacheManager = CaffeineCacheManager.create(cacheConfig);

// Cache query results for 1 hour
List<User> users = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .cached(3600)  // TTL in seconds
    .findAll();
```

### Cache Regions

Organize cached queries into logical groups:

```java
// Cache in specific region
List<User> users = QueryBuilder.forEntity(User.class)
    .where("department", "Engineering")
    .cached("department_queries", 1800)  // 30 minutes
    .findAll();

// Clear specific cache region
cacheManager.clearRegion("department_queries");
```

### Cache Keys

Query4j automatically generates cache keys based on:
- Entity class
- All predicates and their values
- Sort order
- Pagination settings

Manual cache key management:

```java
String cacheKey = "active_engineers_page_" + pageNum;

// Try to get from cache first
List<User> users = cacheManager.get(cacheKey);

if (users == null) {
    // Execute query if not cached
    users = QueryBuilder.forEntity(User.class)
        .where("department", "Engineering")
        .where("active", true)
        .page(pageNum, 20)
        .findAll();
    
    // Store in cache
    cacheManager.put(cacheKey, users, 600L);  // 10 minutes
}
```

### Cache Statistics

Monitor cache effectiveness:

```java
import com.github.query4j.cache.CacheStatistics;

CacheStatistics stats = cacheManager.getStatistics();

System.out.println("Cache Metrics:");
System.out.println("  Hit count: " + stats.getHitCount());
System.out.println("  Miss count: " + stats.getMissCount());
System.out.println("  Hit rate: " + stats.getHitRate() + "%");
System.out.println("  Current size: " + stats.getCurrentSize());
System.out.println("  Eviction count: " + stats.getEvictionCount());
System.out.println("  Average load time: " + stats.getAverageLoadTimeMs() + "ms");
```

### Cache Invalidation

Invalidate cache entries when data changes:

```java
// Clear all cache
cacheManager.clear();

// Clear specific region
cacheManager.clearRegion("user_queries");

// Invalidate by pattern
cacheManager.invalidateMatching("user:*");

// Time-based invalidation (automatic based on TTL)
```

## Asynchronous Execution

Execute queries asynchronously for better resource utilization and responsiveness.

### Basic Async Query

```java
import java.util.concurrent.CompletableFuture;

// Execute query asynchronously
CompletableFuture<List<User>> futureUsers = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .findAllAsync();

// Process results when complete
futureUsers.thenAccept(users -> {
    System.out.println("Found " + users.size() + " users");
    users.forEach(u -> System.out.println(u.getFirstName()));
});

// Or wait for results
List<User> users = futureUsers.get();  // Blocks until complete
```

### Parallel Query Execution

Execute multiple queries concurrently:

```java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

ExecutorService executor = Executors.newFixedThreadPool(4);

// Execute multiple queries in parallel
CompletableFuture<List<User>> engineersQuery = CompletableFuture.supplyAsync(() ->
    QueryBuilder.forEntity(User.class)
        .where("department", "Engineering")
        .findAll(),
    executor
);

CompletableFuture<List<Order>> ordersQuery = CompletableFuture.supplyAsync(() ->
    QueryBuilder.forEntity(Order.class)
        .where("status", "PENDING")
        .findAll(),
    executor
);

CompletableFuture<List<Product>> productsQuery = CompletableFuture.supplyAsync(() ->
    QueryBuilder.forEntity(Product.class)
        .where("inStock", true)
        .findAll(),
    executor
);

// Wait for all queries to complete
CompletableFuture.allOf(engineersQuery, ordersQuery, productsQuery).join();

// Access results
List<User> engineers = engineersQuery.get();
List<Order> orders = ordersQuery.get();
List<Product> products = productsQuery.get();

executor.shutdown();
```

### Pipeline Processing

Chain async operations:

```java
CompletableFuture<String> reportFuture = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .findAllAsync()
    .thenApply(users -> {
        // Filter VIP users
        return users.stream()
            .filter(u -> u.isVip())
            .collect(Collectors.toList());
    })
    .thenCompose(vipUsers -> {
        // Get their orders asynchronously
        List<Long> userIds = vipUsers.stream()
            .map(User::getId)
            .collect(Collectors.toList());
        
        return QueryBuilder.forEntity(Order.class)
            .whereIn("customerId", userIds)
            .findAllAsync();
    })
    .thenApply(orders -> {
        // Generate report
        double totalRevenue = orders.stream()
            .mapToDouble(Order::getTotalAmount)
            .sum();
        
        return String.format("VIP Revenue: $%.2f", totalRevenue);
    });

// Get final result
String report = reportFuture.get();
System.out.println(report);
```

### Error Handling in Async Queries

```java
CompletableFuture<List<User>> futureUsers = QueryBuilder.forEntity(User.class)
    .where("department", "Engineering")
    .findAllAsync()
    .exceptionally(ex -> {
        System.err.println("Query failed: " + ex.getMessage());
        // Return empty list as fallback
        return Collections.emptyList();
    });
```

## Batch Processing

Process large datasets efficiently with pagination and batch operations.

### Basic Batch Processing

```java
int pageSize = 100;
int pageNumber = 0;
int totalProcessed = 0;

Page<User> page;
do {
    // Fetch next page
    page = QueryBuilder.forEntity(User.class)
        .where("active", true)
        .orderBy("id")  // Important: consistent ordering
        .page(pageNumber, pageSize)
        .findPage();
    
    // Process batch
    for (User user : page.getContent()) {
        processUser(user);
        totalProcessed++;
    }
    
    System.out.println("Processed " + totalProcessed + " of " + page.getTotalElements());
    
    pageNumber++;
} while (page.hasNext());
```

### Batch Processing with Progress Tracking

```java
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class BatchProcessor {
    private static final Logger logger = Logger.getLogger(BatchProcessor.class.getName());
    
    public void processInactiveUsers() {
        int batchSize = 500;
        int pageNumber = 0;
        AtomicInteger processed = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);
        
        long startTime = System.currentTimeMillis();
        
        Page<User> page;
        do {
            try {
                page = QueryBuilder.forEntity(User.class)
                    .where("active", false)
                    .where("lastLoginDate", "<", LocalDate.now().minusYears(1))
                    .orderBy("id")
                    .page(pageNumber, batchSize)
                    .findPage();
                
                // Process each user in batch
                for (User user : page.getContent()) {
                    try {
                        archiveUser(user);
                        processed.incrementAndGet();
                    } catch (Exception e) {
                        logger.warning("Failed to archive user " + user.getId() + ": " + e.getMessage());
                        failed.incrementAndGet();
                    }
                }
                
                // Log progress
                double progress = (processed.get() * 100.0) / page.getTotalElements();
                logger.info(String.format("Progress: %.1f%% (%d/%d processed, %d failed)",
                    progress, processed.get(), page.getTotalElements(), failed.get()));
                
                pageNumber++;
                
            } catch (Exception e) {
                logger.severe("Batch processing failed at page " + pageNumber + ": " + e.getMessage());
                throw new RuntimeException("Batch processing failed", e);
            }
        } while (page.hasNext());
        
        long duration = System.currentTimeMillis() - startTime;
        logger.info(String.format("Batch processing complete: %d processed, %d failed in %dms",
            processed.get(), failed.get(), duration));
    }
    
    private void archiveUser(User user) {
        // Archive user logic
    }
}
```

### Parallel Batch Processing

Process batches in parallel for maximum throughput:

```java
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class ParallelBatchProcessor {
    
    private final ExecutorService executor;
    private final int batchSize = 1000;
    
    public ParallelBatchProcessor(int threadCount) {
        this.executor = Executors.newFixedThreadPool(threadCount);
    }
    
    public void processAllUsers() throws Exception {
        // Get total count
        long totalUsers = QueryBuilder.forEntity(User.class)
            .where("active", true)
            .count();
        
        int totalPages = (int) Math.ceil((double) totalUsers / batchSize);
        
        // Create futures for all pages
        List<CompletableFuture<Integer>> futures = IntStream.range(0, totalPages)
            .mapToObj(pageNum -> CompletableFuture.supplyAsync(() -> 
                processBatch(pageNum), executor
            ))
            .collect(Collectors.toList());
        
        // Wait for all batches to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        // Sum up results
        int totalProcessed = futures.stream()
            .map(CompletableFuture::join)
            .mapToInt(Integer::intValue)
            .sum();
        
        System.out.println("Total processed: " + totalProcessed);
        
        executor.shutdown();
    }
    
    private int processBatch(int pageNumber) {
        List<User> users = QueryBuilder.forEntity(User.class)
            .where("active", true)
            .orderBy("id")
            .page(pageNumber, batchSize)
            .findPage()
            .getContent();
        
        // Process users
        users.forEach(this::processUser);
        
        return users.size();
    }
    
    private void processUser(User user) {
        // Processing logic
    }
}
```

## Configuration Customization

### Programmatic Configuration

Configure Query4j behavior at runtime:

```java
import com.github.query4j.core.CoreConfig;

CoreConfig config = CoreConfig.builder()
    .defaultQueryTimeoutMs(30000)
    .maxPredicateDepth(15)
    .maxPredicateCount(100)
    .strictFieldValidation(true)
    .queryStatisticsEnabled(true)
    .build();

// Use custom configuration
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .withConfig(config)
    .where("active", true)
    .findAll();
```

### Configuration File

Use `query4j.yml` for project-wide settings:

```yaml
query4j:
  core:
    defaultQueryTimeoutMs: 30000
    maxPredicateDepth: 20
    maxPredicateCount: 100
    strictFieldValidation: true
    queryStatisticsEnabled: true
    defaultPageSize: 50
    maxPageSize: 1000
  
  cache:
    enabled: true
    defaultTtlSeconds: 3600
    maxSize: 10000
    defaultRegion: "app_cache"
    statisticsEnabled: true
    maintenanceIntervalSeconds: 300
  
  optimizer:
    indexSuggestionsEnabled: true
    predicatePushdownEnabled: true
    joinReorderingEnabled: true
    verboseOutput: false
    maxAnalysisTimeMs: 5000
```

### Environment-Specific Configuration

Override configuration per environment:

```yaml
# application-dev.yml
query4j:
  core:
    queryStatisticsEnabled: true
  cache:
    maxSize: 1000
  optimizer:
    verboseOutput: true

# application-prod.yml  
query4j:
  core:
    defaultQueryTimeoutMs: 10000
  cache:
    maxSize: 100000
    defaultTtlSeconds: 7200
  optimizer:
    verboseOutput: false
```

## Performance Tuning

### Query Optimization Checklist

1. **Use indexes appropriately**
   - Index columns used in WHERE clauses
   - Index columns used in JOIN conditions
   - Consider composite indexes for multi-column filters

2. **Minimize data transfer**
   - Use `.select()` to fetch only needed columns
   - Use pagination for large result sets
   - Avoid `SELECT *` when possible

3. **Optimize joins**
   - Use INNER JOIN when possible (faster than LEFT JOIN)
   - Ensure join columns are indexed
   - Consider denormalization for frequently-joined data

4. **Cache effectively**
   - Cache frequently-executed queries
   - Use appropriate TTL values
   - Monitor cache hit rates

5. **Use batch processing**
   - Process large datasets in pages
   - Use parallel processing when appropriate
   - Implement progress tracking

### Benchmarking Your Queries

```java
import java.time.Duration;
import java.time.Instant;

public class QueryBenchmark {
    
    public void benchmarkQuery() {
        // Warm-up (JVM optimization)
        for (int i = 0; i < 10; i++) {
            executeQuery();
        }
        
        // Actual benchmark
        int iterations = 100;
        long totalTime = 0;
        
        for (int i = 0; i < iterations; i++) {
            Instant start = Instant.now();
            executeQuery();
            Instant end = Instant.now();
            
            totalTime += Duration.between(start, end).toMillis();
        }
        
        double avgTime = totalTime / (double) iterations;
        System.out.println("Average execution time: " + avgTime + "ms");
    }
    
    private void executeQuery() {
        QueryBuilder.forEntity(User.class)
            .where("active", true)
            .orderBy("lastName")
            .limit(100)
            .findAll();
    }
}
```

### Performance Targets

Based on Query4j benchmarks:

| Query Type | Target | Typical Performance |
|------------|--------|-------------------|
| Basic query (single WHERE) | < 1 ms | ~1.7 μs |
| Moderate query (multiple WHERE + ORDER BY) | < 2 ms | ~6.7 μs |
| Complex query (joins + GROUP BY + HAVING) | < 5 ms | ~17.1 μs |
| Optimizer analysis | < 10 ms | ~0.4 μs |

See [benchmark/README.md](benchmark/README.md) for detailed performance analysis.

## Best Practices

### 1. Always Use Immutability

Query4j builders are immutable - each method returns a new instance:

```java
// ✅ Correct - assign returned builder
QueryBuilder<User> base = QueryBuilder.forEntity(User.class);
QueryBuilder<User> filtered = base.where("active", true);
QueryBuilder<User> sorted = filtered.orderBy("lastName");

// ❌ Wrong - discards returned builder
QueryBuilder<User> query = QueryBuilder.forEntity(User.class);
query.where("active", true);  // Returns new builder, but not assigned
query.orderBy("lastName");     // Operates on original, not filtered version
```

### 2. Validate Input Parameters

Always validate user input before building queries:

```java
public List<User> searchUsers(String department, List<String> roles) {
    QueryBuilder<User> query = QueryBuilder.forEntity(User.class);
    
    // Validate department
    if (department != null && !department.trim().isEmpty()) {
        query = query.where("department", department.trim());
    }
    
    // Validate roles list
    if (roles != null && !roles.isEmpty()) {
        // Filter out null/empty values
        List<String> validRoles = roles.stream()
            .filter(r -> r != null && !r.trim().isEmpty())
            .map(String::trim)
            .collect(Collectors.toList());
        
        if (!validRoles.isEmpty()) {
            if (department != null) {
                query = query.and();
            }
            query = query.whereIn("role", validRoles);
        }
    }
    
    return query.findAll();
}
```

### 3. Use Consistent Ordering for Pagination

Always order by a unique field (or combination) for reliable pagination:

```java
// ✅ Good - ordered by unique ID
Page<User> page = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .orderBy("id")
    .page(pageNum, 20)
    .findPage();

// ⚠️ Problematic - lastName not unique, might skip/duplicate records
Page<User> page = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .orderBy("lastName")
    .page(pageNum, 20)
    .findPage();

// ✅ Better - compound ordering ensures consistency
Page<User> page = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .orderBy("lastName")
    .orderBy("firstName")
    .orderBy("id")
    .page(pageNum, 20)
    .findPage();
```

### 4. Handle Null Values Explicitly

Be explicit about null handling:

```java
// Check for null before filtering
String searchTerm = getSearchTerm();
QueryBuilder<User> query = QueryBuilder.forEntity(User.class);

if (searchTerm != null && !searchTerm.isEmpty()) {
    query = query.whereLike("name", "%" + searchTerm + "%");
}

// Use whereIsNull/whereIsNotNull for explicit null checks
List<User> usersWithEmail = QueryBuilder.forEntity(User.class)
    .whereIsNotNull("email")
    .findAll();
```

### 5. Close Resources Properly

When using async execution or thread pools:

```java
ExecutorService executor = Executors.newFixedThreadPool(4);

try {
    // Execute async queries
    CompletableFuture<List<User>> future = CompletableFuture.supplyAsync(() ->
        QueryBuilder.forEntity(User.class).findAll(),
        executor
    );
    
    List<User> results = future.get();
    
} finally {
    executor.shutdown();
    try {
        if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }
    } catch (InterruptedException e) {
        executor.shutdownNow();
        Thread.currentThread().interrupt();
    }
}
```

### 6. Monitor and Log Query Performance

Track slow queries in production:

```java
import java.util.logging.Logger;

public class QueryExecutor {
    private static final Logger logger = Logger.getLogger(QueryExecutor.class.getName());
    private static final long SLOW_QUERY_THRESHOLD_MS = 1000;
    
    public <T> List<T> executeWithMonitoring(QueryBuilder<T> query) {
        long start = System.currentTimeMillis();
        
        try {
            List<T> results = query.findAll();
            long duration = System.currentTimeMillis() - start;
            
            if (duration > SLOW_QUERY_THRESHOLD_MS) {
                logger.warning(String.format(
                    "Slow query detected: %dms - SQL: %s",
                    duration, query.toSQL()
                ));
            }
            
            return results;
            
        } catch (Exception e) {
            logger.severe("Query execution failed: " + e.getMessage());
            throw e;
        }
    }
}
```

### 7. Use Builder Pattern for Reusable Queries

Create reusable query templates:

```java
public class UserQueries {
    
    // Base query for active users
    private QueryBuilder<User> activeUsersBase() {
        return QueryBuilder.forEntity(User.class)
            .where("active", true)
            .whereIsNotNull("email");
    }
    
    // Find active users by department
    public List<User> findActiveInDepartment(String department) {
        return activeUsersBase()
            .and()
            .where("department", department)
            .orderBy("lastName")
            .findAll();
    }
    
    // Find recent hires
    public List<User> findRecentHires(int months) {
        return activeUsersBase()
            .and()
            .where("joinDate", ">=", LocalDate.now().minusMonths(months))
            .orderByDescending("joinDate")
            .findAll();
    }
}
```

## Common Pitfalls

### 1. Forgetting Immutability

```java
// ❌ WRONG - Doesn't work due to immutability
QueryBuilder<User> query = QueryBuilder.forEntity(User.class);
query.where("active", true);  // Returns new builder, but not assigned!
List<User> users = query.findAll();  // Executes without the WHERE clause

// ✅ CORRECT
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .where("active", true);  // Assign returned builder
List<User> users = query.findAll();
```

### 2. SQL Injection in Dynamic Queries

```java
// ❌ DANGEROUS - Vulnerable to SQL injection
String userInput = getUserInput();
List<User> users = QueryBuilder.forEntity(User.class)
    .where("name", "=", userInput)  // Safe - uses parameterized query
    .findAll();

// Query4j automatically parameterizes values, so this is SAFE
// Generated SQL: WHERE name = :p1 (with parameter binding)
```

Query4j automatically parameterizes all values, protecting against SQL injection. However, field names are not parameterized, so validate them:

```java
// ✅ SAFE - Validate field names from user input
String sortField = request.getParameter("sort");
List<String> allowedFields = Arrays.asList("name", "email", "department");

if (!allowedFields.contains(sortField)) {
    throw new IllegalArgumentException("Invalid sort field");
}

List<User> users = QueryBuilder.forEntity(User.class)
    .orderBy(sortField)
    .findAll();
```

### 3. N+1 Query Problem

```java
// ❌ INEFFICIENT - N+1 queries
List<User> users = QueryBuilder.forEntity(User.class).findAll();
for (User user : users) {
    List<Order> orders = QueryBuilder.forEntity(Order.class)
        .where("customerId", user.getId())
        .findAll();  // Executes N queries for N users
}

// ✅ EFFICIENT - Single query with JOIN
List<User> users = QueryBuilder.forEntity(User.class)
    .fetch("orders")  // Eager loading
    .findAll();
```

### 4. Incorrect Pagination

```java
// ❌ WRONG - Page numbers are zero-based
Page<User> page = QueryBuilder.forEntity(User.class)
    .page(1, 20)  // This is actually the SECOND page!
    .findPage();

// ✅ CORRECT - Start from page 0
Page<User> firstPage = QueryBuilder.forEntity(User.class)
    .page(0, 20)  // First page
    .findPage();
```

### 5. Unbounded Queries

```java
// ❌ DANGEROUS - Could return millions of records
List<User> allUsers = QueryBuilder.forEntity(User.class)
    .findAll();  // No limit!

// ✅ SAFE - Always use pagination for potentially large result sets
Page<User> users = QueryBuilder.forEntity(User.class)
    .page(0, 100)
    .findPage();
```

### 6. Ignoring Cache Invalidation

```java
// ❌ PROBLEMATIC - Stale cached data
List<User> users = QueryBuilder.forEntity(User.class)
    .cached(86400)  // Cache for 24 hours
    .findAll();

// Update user in database
updateUser(userId, newData);

// Query still returns old cached data!

// ✅ CORRECT - Invalidate cache on updates
updateUser(userId, newData);
cacheManager.clearRegion("user_queries");
```

### 7. Not Handling Exceptions

```java
// ❌ RISKY - Uncaught exceptions
List<User> users = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .findAll();  // Could throw QueryExecutionException

// ✅ SAFE - Proper exception handling
try {
    List<User> users = QueryBuilder.forEntity(User.class)
        .where("active", true)
        .timeout(5000)  // Set timeout
        .findAll();
        
} catch (QueryBuildException e) {
    logger.error("Invalid query: " + e.getMessage());
    // Handle build-time error
} catch (QueryExecutionException e) {
    logger.error("Query execution failed: " + e.getMessage());
    // Handle runtime error (database issue, timeout, etc.)
}
```

## Next Steps

Congratulations! You've mastered advanced Query4j features. Here are additional resources:

### Documentation
- [Configuration Guide](docs/Configuration.md) - Detailed configuration options
- [API Reference](docs/API_GUIDE.md) - Complete API documentation  
- [Benchmark Results](benchmark/README.md) - Performance analysis

### Examples
- [AsyncQueryApp](examples/src/main/java/com/github/query4j/examples/async/AsyncQueryApp.java) - Async patterns
- [BatchProcessingApp](examples/src/main/java/com/github/query4j/examples/batch/BatchProcessingApp.java) - Batch processing
- [ComplexJoinsApp](examples/src/main/java/com/github/query4j/examples/joins/ComplexJoinsApp.java) - Advanced joins

### Community
- [GitHub Issues](https://github.com/query4j/dynamicquerybuilder/issues) - Bug reports and feature requests
- [GitHub Discussions](https://github.com/query4j/dynamicquerybuilder/discussions) - Community support
- [Contributing Guide](CONTRIBUTING.md) - Contribute to the project

---

**Happy querying!** Build powerful, performant, and maintainable data access layers with Query4j.
