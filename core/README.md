# Query4j Core Module

The core module provides the fundamental query building functionality for the Query4j Dynamic Query Builder library.

## Features

- **Fluent Builder API**: Chainable methods for constructing complex queries
- **Multi-table JOIN Support**: INNER, LEFT, RIGHT, and FETCH joins
- **Aggregation Functions**: COUNT, SUM, AVG, MIN, MAX with GROUP BY and HAVING support
- **Thread-Safe**: Immutable builder pattern with copy-on-write semantics
- **Comprehensive Predicates**: Support for all common SQL conditions
- **Parameter Safety**: Automatic parameter binding to prevent SQL injection

## Quick Start

### Basic Query Building

```java
import com.github.query4j.core.QueryBuilder;

// Simple query
List<User> users = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .findAll();

// Complex query with multiple conditions
List<User> activeUsers = QueryBuilder.forEntity(User.class)
    .where("department", "Engineering")
    .and()
    .whereIn("role", Arrays.asList("admin", "developer"))
    .orderBy("lastName")
    .limit(50)
    .findAll();
```

## JOIN Operations

The core module provides comprehensive support for multi-table joins with a fluent API:

### Basic JOIN Types

```java
// INNER JOIN (default join type)
List<User> usersWithOrders = QueryBuilder.forEntity(User.class)
    .join("orders")  // Alias for innerJoin()
    .findAll();

// Explicit INNER JOIN
List<User> usersWithOrders = QueryBuilder.forEntity(User.class)
    .innerJoin("orders")
    .findAll();

// LEFT JOIN
List<User> usersWithOptionalProfiles = QueryBuilder.forEntity(User.class)
    .leftJoin("profile")
    .findAll();

// RIGHT JOIN
List<User> usersWithPermissions = QueryBuilder.forEntity(User.class)
    .rightJoin("permissions")
    .findAll();
```

### FETCH Joins for Eager Loading

```java
// LEFT JOIN FETCH for eager loading associations
List<User> usersWithProfiles = QueryBuilder.forEntity(User.class)
    .fetch("profile")  // Equivalent to LEFT JOIN FETCH
    .findAll();

// Multiple fetch joins
List<User> fullyLoadedUsers = QueryBuilder.forEntity(User.class)
    .fetch("profile")
    .fetch("orders")
    .fetch("permissions")
    .findAll();
```

### Multiple Joins

```java
// Combining different join types
List<User> complexQuery = QueryBuilder.forEntity(User.class)
    .innerJoin("department")           // INNER JOIN department
    .leftJoin("profile")              // LEFT JOIN profile  
    .rightJoin("permissions")         // RIGHT JOIN permissions
    .fetch("orders")                  // LEFT JOIN FETCH orders
    .where("department.name", "Engineering")
    .whereIsNotNull("profile.email")
    .findAll();
```

### JOIN with Qualified Field Names

```java
// Using dot notation for qualified field names
List<Order> orders = QueryBuilder.forEntity(Order.class)
    .join("user.profile")             // Join through nested associations
    .leftJoin("order_items")          // Underscore field names supported
    .where("user.profile.active", true)
    .findAll();
```

### Generated SQL Examples

The JOIN methods generate the following SQL patterns:

```java
QueryBuilder.forEntity(User.class)
    .join("orders")
    .toSQL();
// Result: SELECT * FROM User INNER JOIN orders

QueryBuilder.forEntity(User.class)
    .leftJoin("profile")
    .rightJoin("permissions")
    .fetch("orders")
    .toSQL();
// Result: SELECT * FROM User LEFT JOIN profile RIGHT JOIN permissions LEFT JOIN FETCH orders
```

## Aggregation Functions

The core module provides comprehensive support for SQL aggregation functions with a fluent API:

### Basic Aggregation Functions

```java
// COUNT(*) - count all rows
Long totalUsers = QueryBuilder.forEntity(User.class)
    .countAll()
    .count(); // Returns the count result

// COUNT(field) - count non-null values
List<Object[]> results = QueryBuilder.forEntity(User.class)
    .count("email")
    .findAll();

// SUM - calculate sum of numeric field
List<Object[]> totalSalary = QueryBuilder.forEntity(Employee.class)
    .sum("salary")
    .findAll();

// AVG - calculate average of numeric field
List<Object[]> avgPrice = QueryBuilder.forEntity(Product.class)
    .avg("price")
    .where("category", "Electronics")
    .findAll();

// MIN - find minimum value
List<Object[]> earliestDate = QueryBuilder.forEntity(Order.class)
    .min("order_date")
    .findAll();

// MAX - find maximum value  
List<Object[]> latestUpdate = QueryBuilder.forEntity(User.class)
    .max("last_login")
    .where("active", true)
    .findAll();
```

### Aggregation with GROUP BY

```java
// Count users by department
List<Object[]> usersByDept = QueryBuilder.forEntity(User.class)
    .select("department", "COUNT(*)")
    .groupBy("department")
    .findAll();

// Sum sales by region and month
List<Object[]> salesSummary = QueryBuilder.forEntity(Sale.class)
    .select("region", "MONTH(sale_date)", "SUM(amount)")
    .groupBy("region", "MONTH(sale_date)")
    .orderBy("region")
    .findAll();

// Average salary by department, only for active employees
List<Object[]> avgSalaryByDept = QueryBuilder.forEntity(Employee.class)
    .select("department", "AVG(salary)")
    .where("status", "ACTIVE")
    .groupBy("department")
    .orderBy("AVG(salary)", false) // DESC order
    .findAll();
```

### Aggregation with HAVING Clause

The HAVING clause filters results after grouping and aggregation:

```java
// Departments with more than 10 employees
List<Object[]> largeDepartments = QueryBuilder.forEntity(Employee.class)
    .select("department", "COUNT(*)")
    .groupBy("department")
    .having("COUNT(*)", ">", 10)
    .orderBy("COUNT(*)", false)
    .findAll();

// Products with average price above $100
List<Object[]> expensiveProducts = QueryBuilder.forEntity(Product.class)
    .select("category", "AVG(price)")
    .groupBy("category")
    .having("AVG(price)", ">", 100.0)
    .findAll();

// Regions with total sales exceeding $50,000
List<Object[]> highSalesRegions = QueryBuilder.forEntity(Sale.class)
    .select("region", "SUM(amount)")
    .where("sale_date", ">=", "2023-01-01")
    .groupBy("region")
    .having("SUM(amount)", ">", 50000)
    .orderBy("SUM(amount)", false)
    .findAll();

// Complex HAVING with multiple conditions
List<Object[]> qualifiedDepts = QueryBuilder.forEntity(Employee.class)
    .select("department", "COUNT(*)", "AVG(salary)")
    .where("hire_date", ">", "2020-01-01")
    .groupBy("department")
    .having("COUNT(*)", ">", 5)
    .and()
    .having("AVG(salary)", "<", 75000)
    .findAll();
```

### Complex Aggregation Scenarios

```java
// Quarterly sales report with growth analysis
List<Object[]> quarterlySales = QueryBuilder.forEntity(Sale.class)
    .select("QUARTER(sale_date)", "SUM(amount)", "COUNT(*)", "AVG(amount)")
    .where("sale_date", "BETWEEN", "2023-01-01", "2023-12-31")
    .groupBy("QUARTER(sale_date)")
    .having("SUM(amount)", ">", 10000)
    .orderBy("QUARTER(sale_date)")
    .findAll();

// Top performing categories by multiple metrics
List<Object[]> topCategories = QueryBuilder.forEntity(Product.class)
    .select("category", "COUNT(*)", "MIN(price)", "MAX(price)", "AVG(price)")
    .where("active", true)
    .groupBy("category")
    .having("COUNT(*)", ">=", 5)
    .and()
    .having("AVG(price)", ">", 50.0)
    .orderBy("COUNT(*)", false)
    .limit(10)
    .findAll();
```

### Aggregation Best Practices

- **Field Selection**: When using GROUP BY, ensure all non-aggregated fields in SELECT are included in GROUP BY
- **Performance**: Use appropriate indexes on grouped and aggregated fields
- **HAVING vs WHERE**: Use WHERE for filtering before grouping, HAVING for filtering after aggregation
- **Null Handling**: Aggregation functions (except COUNT(*)) ignore NULL values
- **Type Safety**: Ensure numeric aggregations (SUM, AVG) are used with appropriate data types

## Field Name Validation

JOIN association names must follow these rules:

- **Valid characters**: Letters (a-z, A-Z), digits (0-9), underscores (_), and dots (.)
- **Examples of valid names**: `orders`, `user_profile`, `department.employees`, `user123`
- **Examples of invalid names**: `invalid@field`, `invalid-field`, `invalid field`, `invalid*field`

```java
// Valid association names
builder.join("orders");                // ✅ Simple name
builder.leftJoin("user_profile");     // ✅ With underscore  
builder.rightJoin("dept.employees");  // ✅ Qualified name
builder.fetch("order123");            // ✅ With numbers

// Invalid association names - will throw IllegalArgumentException
builder.join("invalid@field");        // ❌ Contains @
builder.leftJoin("invalid-field");    // ❌ Contains -
builder.rightJoin("invalid field");   // ❌ Contains space
builder.fetch("invalid*field");       // ❌ Contains *
```

## Thread Safety and Immutability

All JOIN operations maintain immutability:

```java
QueryBuilder<User> base = QueryBuilder.forEntity(User.class);
QueryBuilder<User> withJoin = base.join("orders");       // New instance
QueryBuilder<User> withLeftJoin = withJoin.leftJoin("profile"); // New instance

// Original builder unchanged
assertEquals("SELECT * FROM User", base.toSQL());

// Each operation creates a new instance
assertNotSame(base, withJoin);
assertNotSame(withJoin, withLeftJoin);
```

## Advanced JOIN Scenarios

### Combining JOINs with WHERE Conditions

```java
List<User> activeUsersWithRecentOrders = QueryBuilder.forEntity(User.class)
    .join("orders")
    .where("active", true)
    .and()
    .where("orders.orderDate", ">", LocalDate.now().minusDays(30))
    .orderBy("orders.orderDate", false) // DESC
    .findAll();
```

### JOINs with Aggregations

Combine JOINs with aggregation functions for powerful analytical queries. See the [Aggregation Functions](#aggregation-functions) section for comprehensive examples.

```java
// Count orders per user with JOIN
List<Object[]> userOrderCounts = QueryBuilder.forEntity(User.class)
    .select("id", "name", "COUNT(orders.id)")
    .leftJoin("orders")
    .groupBy("id", "name")
    .having("COUNT(orders.id)", ">", 5)
    .orderBy("COUNT(orders.id)", false)
    .findAll();

// Average order value by customer region
List<Object[]> avgOrderByRegion = QueryBuilder.forEntity(Order.class)
    .select("user.region", "AVG(total_amount)")
    .join("user")
    .where("order_date", ">=", "2023-01-01")
    .groupBy("user.region")
    .having("AVG(total_amount)", ">", 100.0)
    .findAll();
```

### Pagination with JOINs

```java
Page<User> usersPage = QueryBuilder.forEntity(User.class)
    .join("department")
    .fetch("profile")
    .where("department.name", "Engineering")
    .orderBy("lastName")
    .page(0, 20)  // First page, 20 items
    .findPage();
```

## Performance Considerations

- **FETCH joins** eagerly load associations to avoid N+1 query problems
- **INNER JOINs** filter results to only include entities with matching associations
- **LEFT JOINs** preserve all entities even without matching associations
- **RIGHT JOINs** preserve all associated entities even without matching main entities
- Use appropriate join types based on your data requirements and performance needs

### Pagination Performance Benchmarks

DynamicQueryBuilder has been benchmarked against baseline Java libraries for pagination performance:

- **Query Construction**: 7.916 μs/op (only 20% overhead vs JPA Criteria API)
- **Competitive Performance**: Excellent balance between performance and developer experience
- **Type Safety**: Compile-time safety without the verbosity of Criteria API

For detailed benchmarking results and comparisons with JPA/Hibernate and raw JDBC, see:

```bash
# Run pagination benchmarks comparing vs baseline libraries
./gradlew benchmark:paginationBenchmark

# View detailed analysis
open benchmark/pagination-benchmark-analysis.md
```

## Error Handling

JOIN operations validate association names and throw appropriate exceptions:

```java
try {
    QueryBuilder.forEntity(User.class)
        .join("invalid@field")  // Invalid field name
        .findAll();
} catch (IllegalArgumentException e) {
    // Handle validation error
    System.err.println("Invalid association name: " + e.getMessage());
}
```

## Integration with Other Features

JOINs work seamlessly with all other query builder features:

```java
// Complex query with joins, conditions, pagination, and caching
Page<User> result = QueryBuilder.forEntity(User.class)
    .join("department")
    .leftJoin("profile") 
    .fetch("orders")
    .where("department.name", "Engineering")
    .and()
    .whereIsNotNull("profile.email")
    .or()
    .whereLike("name", "%Smith%")
    .orderBy("lastName")
    .orderBy("firstName")
    .page(0, 25)
    .cached(3600)  // Cache for 1 hour
    .findPage();
```

## Integration Tests

The core module includes comprehensive integration tests that verify multi-table join behavior and aggregation functionality using an in-memory H2 database.

### Running Integration Tests

```bash
# Run all integration tests
./gradlew :core:test --tests "*integration*"

# Run specific integration test
./gradlew :core:test --tests "*MultiTableJoinAggregationIntegrationTest*"
```

### Integration Test Scenarios

The integration tests cover the following scenarios:

1. **Simple Join**: Customer → orders with region filtering
2. **Nested Join**: Customer → orders → orderItems with product filtering  
3. **Aggregation**: SUM aggregation with GROUP BY
4. **Join + Aggregation + HAVING**: Complex queries with spending thresholds
5. **Edge Cases**: Customers with no orders, orders with no items

### Test Schema

The integration tests use the following schema:

```sql
-- Customer table
CREATE TABLE Customer (
    id INT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    region VARCHAR(255) NOT NULL
);

-- Order table  
CREATE TABLE "Order" (
    id INT PRIMARY KEY,
    customer_id INT NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    placed_at DATE NOT NULL
);

-- OrderItem table
CREATE TABLE OrderItem (
    id INT PRIMARY KEY,
    order_id INT NOT NULL,
    product VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL
);
```

### What Integration Tests Verify

- **SQL Generation**: That the QueryBuilder generates syntactically correct SQL
- **API Functionality**: That joins, aggregations, and having clauses work as expected
- **Parameter Binding**: That parameters are correctly generated and can be used
- **Database Compatibility**: That generated SQL executes successfully against H2
- **Result Accuracy**: That actual query results match expected data

## Documentation

For comprehensive documentation and API references:

- **[Main README](../README.md)** - Library overview, installation, quick start, and troubleshooting
- **[API Reference Guide](../docs/API_GUIDE.md)** - Complete API documentation with examples
- **[Configuration Guide](../docs/Configuration.md)** - Core configuration options and best practices
- **[JavaDoc Generation](../docs/JAVADOC_GENERATION.md)** - Generate and access API documentation
- **[Quickstart Tutorial](../QUICKSTART.md)** - Learn the basics in 15 minutes
- **[Advanced Usage Tutorial](../ADVANCED.md)** - Master complex queries and optimization

### API Documentation (JavaDoc)

Generate JavaDoc for the core module:

```bash
# Generate JavaDoc
./gradlew core:javadoc

# View generated documentation
open core/build/docs/javadoc/index.html
```

The core module JavaDoc includes comprehensive documentation for:
- `QueryBuilder<T>` - Primary fluent query builder interface
- `DynamicQuery<T>` - Reusable compiled queries with execution semantics
- `Page<T>` - Paginated results with metadata (1-based indexing)
- `QueryStats` - Execution metrics with performance targets
- Exception hierarchy (`QueryBuildException`, `QueryExecutionException`)
- Predicate classes with usage examples

## Related Modules

- **[Cache Module](../cache/README.md)** - Query result caching for improved performance
- **[Optimizer Module](../optimizer/README.md)** - Query optimization and analysis
- **[Examples Module](../examples/README.md)** - Working examples and integration patterns

## Contributing

See the [Contributing Guide](../CONTRIBUTING.md) for guidelines on:
- Code style and standards
- Testing requirements
- Pull request process
- [Documentation standards](../docs/API_DOCUMENTATION_GUIDE.md)

## Support

- **Issues**: [GitHub Issues](https://github.com/query4j/dynamicquerybuilder/issues)
- **Discussions**: [GitHub Discussions](https://github.com/query4j/dynamicquerybuilder/discussions)

## Large-Scale Testing

The core module includes comprehensive large-scale and performance tests to ensure the library handles high-volume scenarios efficiently.

### Large Result Set Tests

The `LargeResultSetTest` validates performance with datasets containing 10,000+ records:

```bash
# Run large result set tests
./gradlew :core:test --tests "*LargeResultSetTest*"
```

#### Test Scenarios

1. **Full Dataset Retrieval** - Retrieves all 10,000+ records within performance target (< 500ms)
2. **Pagination Performance** - Tests efficient pagination through large datasets 
3. **Filtered Queries** - Validates indexed query performance on large datasets
4. **Data Integrity** - Ensures data consistency across large result sets
5. **Memory Efficiency** - Monitors memory usage during large dataset processing

#### Performance Targets

- Single-threaded retrieval of 10,000 records: **< 500ms**
- Average pagination time: **< 50ms per page**
- Memory increase: **< 200MB heap**

### Concurrent Load Performance Tests

The `LoadPerformanceTest` validates scalability under concurrent access:

```bash
# Run load performance tests
./gradlew :core:test --tests "*LoadPerformanceTest*"
```

#### Test Scenarios

1. **Basic Concurrent Queries** - 20 threads executing simple queries simultaneously
2. **Mixed Query Types** - Concurrent execution of filters, ranges, IN clauses, and complex conditions
3. **Aggregation Under Load** - COUNT, AVG, SUM queries with concurrent access
4. **Memory Stability** - Validates memory usage remains stable under concurrent load

#### Performance Targets

- **20 concurrent threads** average execution time: **< 200ms per query**
- **No failures** or deadlocks during concurrent execution
- **Memory stability** under sustained concurrent load

### Running Performance Tests

#### Individual Test Categories

```bash
# Large result set tests only
./gradlew :core:test --tests "*LargeResultSetTest*" 

# Concurrent performance tests only  
./gradlew :core:test --tests "*LoadPerformanceTest*"

# All performance tests
./gradlew :core:test --tests "*LargeResultSetTest*" --tests "*LoadPerformanceTest*"
```

#### CI/CD Integration

Performance tests are designed to run reliably in CI environments:

```bash
# Run with timeout for CI
timeout 300 ./gradlew :core:test --tests "*Performance*" --tests "*LargeResult*"
```

#### Performance Monitoring

Tests automatically log performance metrics:

```
Seeded 10,000 user records in 1,245ms
Retrieved 10,000 records in 287ms  
Average pagination time: 23.45ms
Peak memory increase: 145MB
=== Concurrent Performance Results ===
Total queries: 200
Successful: 200
Failed: 0
Average execution time: 156ms
Max execution time: 245ms
```

### Test Database Schema

Performance tests use an optimized schema with proper indexing:

```sql
CREATE TABLE "User" (
    id INT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL, 
    email VARCHAR(255) NOT NULL UNIQUE,
    department VARCHAR(100) NOT NULL,
    role VARCHAR(100) NOT NULL,
    hire_date DATE NOT NULL,
    salary DECIMAL(10,2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL
);

-- Performance indexes
CREATE INDEX idx_user_department ON "User"(department);
CREATE INDEX idx_user_active ON "User"(active);
CREATE INDEX idx_user_salary ON "User"(salary);
CREATE INDEX idx_user_country ON "User"(country);
```

### Performance Test Configuration

Tests use configurable parameters for different environments:

```java
// Large dataset configuration
private static final int LARGE_DATASET_SIZE = 10_000;
private static final int PAGINATION_SIZE = 100;

// Concurrent testing configuration  
private static final int THREAD_COUNT = 20;
private static final int QUERIES_PER_THREAD = 10;
private static final int TIMEOUT_SECONDS = 60;
```

### Memory Profiling

Large-scale tests include memory profiling:

```java
// Memory monitoring during test execution
Runtime runtime = Runtime.getRuntime();
long initialMemory = runtime.totalMemory() - runtime.freeMemory();
// ... test execution ...
long finalMemory = runtime.totalMemory() - runtime.freeMemory();
long memoryIncrease = finalMemory - initialMemory;
```

### Troubleshooting Performance Issues

If performance tests fail:

1. **Check available memory**: Ensure adequate heap space (recommended: 512MB+)
2. **Verify database setup**: H2 in-memory database should initialize properly
3. **Review concurrency**: Check for thread contention or deadlocks
4. **Monitor GC activity**: Excessive garbage collection can impact performance

#### Common Performance Tuning

```bash
# Increase JVM memory for tests
export GRADLE_OPTS="-Xmx1g -XX:MaxMetaspaceSize=512m"

# Run tests with JVM profiling
./gradlew :core:test --tests "*Performance*" -Dcom.sun.management.jmxremote
```