# Core Module - QueryBuilder API

The Core Module provides the fundamental query building functionality for Query4j. This page covers the complete API, predicate types, and usage patterns.

## Table of Contents

1. [Overview](#overview)
2. [QueryBuilder API](#querybuilder-api)
3. [Predicate Types](#predicate-types)
4. [Logical Operators](#logical-operators)
5. [JOIN Operations](#join-operations)
6. [Aggregations](#aggregations)
7. [Sorting and Pagination](#sorting-and-pagination)
8. [Immutability and Thread Safety](#immutability-and-thread-safety)
9. [Advanced Patterns](#advanced-patterns)

---

## Overview

The Core Module is the foundation of Query4j, providing:

- **Fluent Builder API** - Chainable methods for query construction
- **Thread-Safe Operations** - Immutable copy-on-write semantics
- **Type Safety** - Generic support with compile-time validation
- **SQL Dialect Support** - Compatible with H2, PostgreSQL, MySQL
- **Parameter Binding** - Automatic parameterization to prevent SQL injection

### Module Dependencies

```xml
<dependency>
    <groupId>com.github.query4j</groupId>
    <artifactId>dynamicquerybuilder-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

---

## QueryBuilder API

### Creating a QueryBuilder

#### For Entity Classes

```java
import com.github.query4j.core.QueryBuilder;

// Type-safe with entity class
QueryBuilder<User> userQuery = QueryBuilder.forEntity(User.class);

// Generates: SELECT * FROM User
```

#### For Table Names

```java
// Direct table name (no type safety)
QueryBuilder<?> query = QueryBuilder.forEntity("users");

// Generates: SELECT * FROM users
```

### Core Methods

#### `where(String field, Object value)`

Add a simple equality predicate.

```java
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .where("username", "john.doe")
    .where("active", true);

// SQL: SELECT * FROM User WHERE username = :p1 AND active = :p2
```

**Field Name Rules:**
- Must match pattern `[A-Za-z0-9_\.]+`
- Cannot be null or empty
- Can include table qualifiers: `"table.column"`

#### `whereIn(String field, Collection<?> values)`

Add an IN predicate for multiple values.

```java
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .whereIn("role", Arrays.asList("admin", "developer", "manager"));

// SQL: SELECT * FROM User WHERE role IN (:p1, :p2, :p3)
```

**Requirements:**
- Values collection cannot be null or empty
- Empty collections throw `QueryBuildException`

#### `whereLike(String field, String pattern)`

Add a LIKE predicate for pattern matching.

```java
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .whereLike("email", "%@company.com");

// SQL: SELECT * FROM User WHERE email LIKE :p1

// Wildcard patterns
.whereLike("name", "John%")      // Starts with "John"
.whereLike("name", "%Doe")       // Ends with "Doe"
.whereLike("name", "%John%")     // Contains "John"
```

#### `whereBetween(String field, Object min, Object max)`

Add a BETWEEN predicate for range queries.

```java
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .whereBetween("age", 25, 65);

// SQL: SELECT * FROM User WHERE age BETWEEN :p1 AND :p2

// Works with dates
.whereBetween("createdAt", startDate, endDate)

// Works with numeric types
.whereBetween("salary", 50000, 150000)
```

#### `whereIsNull(String field)`

Add an IS NULL predicate.

```java
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .whereIsNull("deletedAt");

// SQL: SELECT * FROM User WHERE deletedAt IS NULL
```

#### `whereIsNotNull(String field)`

Add an IS NOT NULL predicate.

```java
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .whereIsNotNull("email");

// SQL: SELECT * FROM User WHERE email IS NOT NULL
```

---

## Predicate Types

Query4j supports five core predicate types:

### 1. SimplePredicate (Equality)

```java
.where("status", "active")
// WHERE status = :p1
```

### 2. InPredicate (Multiple Values)

```java
.whereIn("category", Arrays.asList("A", "B", "C"))
// WHERE category IN (:p1, :p2, :p3)
```

### 3. LikePredicate (Pattern Matching)

```java
.whereLike("name", "John%")
// WHERE name LIKE :p1
```

### 4. BetweenPredicate (Range)

```java
.whereBetween("price", 10.0, 99.99)
// WHERE price BETWEEN :p1 AND :p2
```

### 5. NullPredicate (NULL Checks)

```java
.whereIsNull("endDate")
// WHERE endDate IS NULL

.whereIsNotNull("startDate")
// WHERE startDate IS NOT NULL
```

---

## Logical Operators

### AND Operator

Chain predicates with AND logic.

```java
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .where("department", "Engineering")
    .and()
    .where("active", true)
    .and()
    .whereIn("role", Arrays.asList("admin", "developer"));

// SQL: SELECT * FROM User 
//      WHERE department = :p1 
//      AND active = :p2 
//      AND role IN (:p3, :p4)
```

### OR Operator

Chain predicates with OR logic.

```java
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .where("department", "Engineering")
    .or()
    .where("department", "Sales")
    .or()
    .where("department", "Marketing");

// SQL: SELECT * FROM User 
//      WHERE department = :p1 
//      OR department = :p2 
//      OR department = :p3
```

### NOT Operator

Negate a predicate.

```java
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .not()
    .where("status", "inactive");

// SQL: SELECT * FROM User WHERE NOT status = :p1
```

### Logical Grouping

Use `openGroup()` and `closeGroup()` for complex logic.

```java
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .openGroup()
        .where("department", "Engineering")
        .or()
        .where("department", "Sales")
    .closeGroup()
    .and()
    .where("active", true);

// SQL: SELECT * FROM User 
//      WHERE (department = :p1 OR department = :p2) 
//      AND active = :p3
```

#### Nested Groups

```java
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .openGroup()
        .where("department", "Engineering")
        .and()
        .openGroup()
            .where("level", "senior")
            .or()
            .where("level", "lead")
        .closeGroup()
    .closeGroup()
    .and()
    .where("active", true);

// SQL: SELECT * FROM User 
//      WHERE (department = :p1 AND (level = :p2 OR level = :p3)) 
//      AND active = :p4
```

---

## JOIN Operations

### INNER JOIN

```java
QueryBuilder<Order> query = QueryBuilder.forEntity(Order.class)
    .innerJoin("Customer", "customer_id", "id")
    .where("Order.status", "completed")
    .and()
    .where("Customer.country", "USA");

// SQL: SELECT * FROM Order 
//      INNER JOIN Customer ON Order.customer_id = Customer.id 
//      WHERE Order.status = :p1 AND Customer.country = :p2
```

### LEFT JOIN

```java
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .leftJoin("Department", "department_id", "id")
    .whereIsNotNull("User.email");

// SQL: SELECT * FROM User 
//      LEFT JOIN Department ON User.department_id = Department.id 
//      WHERE User.email IS NOT NULL
```

### RIGHT JOIN

```java
QueryBuilder<Order> query = QueryBuilder.forEntity(Order.class)
    .rightJoin("Customer", "customer_id", "id");

// SQL: SELECT * FROM Order 
//      RIGHT JOIN Customer ON Order.customer_id = Customer.id
```

### Multiple JOINs

```java
QueryBuilder<Order> query = QueryBuilder.forEntity(Order.class)
    .innerJoin("Customer", "customer_id", "id")
    .innerJoin("Product", "product_id", "id")
    .where("Order.status", "shipped");

// SQL: SELECT * FROM Order 
//      INNER JOIN Customer ON Order.customer_id = Customer.id 
//      INNER JOIN Product ON Order.product_id = Product.id 
//      WHERE Order.status = :p1
```

---

## Aggregations

### COUNT

```java
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .select("COUNT(*) as total")
    .where("active", true);

// SQL: SELECT COUNT(*) as total FROM User WHERE active = :p1
```

### SUM, AVG, MIN, MAX

```java
QueryBuilder<Order> query = QueryBuilder.forEntity(Order.class)
    .select("SUM(amount) as total_revenue")
    .select("AVG(amount) as avg_order")
    .select("MIN(amount) as min_order")
    .select("MAX(amount) as max_order")
    .where("status", "completed");

// SQL: SELECT SUM(amount) as total_revenue, 
//             AVG(amount) as avg_order, 
//             MIN(amount) as min_order, 
//             MAX(amount) as max_order 
//      FROM Order 
//      WHERE status = :p1
```

### GROUP BY

```java
QueryBuilder<Order> query = QueryBuilder.forEntity(Order.class)
    .select("customer_id")
    .select("COUNT(*) as order_count")
    .select("SUM(amount) as total_spent")
    .groupBy("customer_id")
    .orderBy("total_spent", "DESC");

// SQL: SELECT customer_id, COUNT(*) as order_count, SUM(amount) as total_spent 
//      FROM Order 
//      GROUP BY customer_id 
//      ORDER BY total_spent DESC
```

### HAVING Clause

```java
QueryBuilder<Order> query = QueryBuilder.forEntity(Order.class)
    .select("customer_id")
    .select("COUNT(*) as order_count")
    .groupBy("customer_id")
    .having("COUNT(*) > 5");

// SQL: SELECT customer_id, COUNT(*) as order_count 
//      FROM Order 
//      GROUP BY customer_id 
//      HAVING COUNT(*) > 5
```

---

## Sorting and Pagination

### ORDER BY

```java
// Single field, default ASC
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .orderBy("lastName");

// SQL: SELECT * FROM User ORDER BY lastName

// Multiple fields
QueryBuilder<User> sorted = QueryBuilder.forEntity(User.class)
    .orderBy("department")
    .orderBy("lastName", "ASC")
    .orderBy("firstName", "ASC");

// SQL: SELECT * FROM User ORDER BY department, lastName ASC, firstName ASC

// Descending order
QueryBuilder<User> descending = QueryBuilder.forEntity(User.class)
    .orderBy("createdAt", "DESC");

// SQL: SELECT * FROM User ORDER BY createdAt DESC
```

### LIMIT and OFFSET

```java
// Limit only
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .limit(10);

// SQL: SELECT * FROM User LIMIT 10

// Pagination (limit + offset)
QueryBuilder<User> page2 = QueryBuilder.forEntity(User.class)
    .orderBy("id")
    .limit(20)
    .offset(20);

// SQL: SELECT * FROM User ORDER BY id LIMIT 20 OFFSET 20
```

### Pagination Helper

```java
public QueryBuilder<User> getPage(int pageNumber, int pageSize) {
    int offset = (pageNumber - 1) * pageSize;
    
    return QueryBuilder.forEntity(User.class)
        .where("active", true)
        .orderBy("id")
        .limit(pageSize)
        .offset(offset);
}

// Usage
QueryBuilder<User> page1 = getPage(1, 20);  // LIMIT 20 OFFSET 0
QueryBuilder<User> page2 = getPage(2, 20);  // LIMIT 20 OFFSET 20
QueryBuilder<User> page3 = getPage(3, 20);  // LIMIT 20 OFFSET 40
```

---

## Immutability and Thread Safety

All QueryBuilder operations are **immutable**—each method returns a new instance.

### Copy-on-Write Semantics

```java
QueryBuilder<User> base = QueryBuilder.forEntity(User.class)
    .where("active", true);

// Each operation creates a new instance
QueryBuilder<User> admins = base.and().where("role", "admin");
QueryBuilder<User> devs = base.and().where("role", "developer");

System.out.println(base.toSQL());
// Output: SELECT * FROM User WHERE active = :p1

System.out.println(admins.toSQL());
// Output: SELECT * FROM User WHERE active = :p1 AND role = :p2

System.out.println(devs.toSQL());
// Output: SELECT * FROM User WHERE active = :p1 AND role = :p2
```

### Thread-Safe Usage

```java
// Share base query across threads safely
QueryBuilder<User> baseQuery = QueryBuilder.forEntity(User.class)
    .where("active", true);

// Thread 1
CompletableFuture<List<User>> admins = CompletableFuture.supplyAsync(() -> {
    QueryBuilder<User> query = baseQuery.and().where("role", "admin");
    return executeQuery(query);
});

// Thread 2 (concurrent access to baseQuery is safe)
CompletableFuture<List<User>> devs = CompletableFuture.supplyAsync(() -> {
    QueryBuilder<User> query = baseQuery.and().where("role", "developer");
    return executeQuery(query);
});
```

---

## Advanced Patterns

### Dynamic Query Building

```java
public QueryBuilder<User> buildDynamicQuery(UserSearchCriteria criteria) {
    QueryBuilder<User> query = QueryBuilder.forEntity(User.class);
    
    if (criteria.getDepartment() != null) {
        query = query.where("department", criteria.getDepartment());
    }
    
    if (criteria.getRoles() != null && !criteria.getRoles().isEmpty()) {
        query = query.and().whereIn("role", criteria.getRoles());
    }
    
    if (criteria.getMinAge() != null && criteria.getMaxAge() != null) {
        query = query.and().whereBetween("age", criteria.getMinAge(), criteria.getMaxAge());
    }
    
    if (criteria.isActiveOnly()) {
        query = query.and().where("active", true);
    }
    
    return query.orderBy("lastName").limit(criteria.getPageSize());
}
```

### Query Templates

```java
public class UserQueries {
    private static final QueryBuilder<User> ACTIVE_USERS = 
        QueryBuilder.forEntity(User.class).where("active", true);
    
    public static QueryBuilder<User> activeInDepartment(String dept) {
        return ACTIVE_USERS.and().where("department", dept);
    }
    
    public static QueryBuilder<User> activeWithRoles(List<String> roles) {
        return ACTIVE_USERS.and().whereIn("role", roles);
    }
}
```

### Debugging Queries

```java
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .where("department", "Engineering")
    .and()
    .whereIn("role", Arrays.asList("admin", "developer"));

// Inspect generated SQL
System.out.println("SQL: " + query.toSQL());
// Output: SELECT * FROM User WHERE department = :p1 AND role IN (:p2, :p3)

// Inspect parameters
System.out.println("Parameters: " + query.getParameters());
// Output: Parameters: {p1=Engineering, p2=admin, p3=developer}

// Get parameter count
System.out.println("Parameter count: " + query.getParameters().size());
// Output: Parameter count: 3
```

---

## Performance Considerations

### Efficient Query Construction

- **Reuse base queries**: Leverage immutability to share common query fragments
- **Avoid unnecessary cloning**: Each operation creates a new instance—minimize intermediate steps
- **Parameter efficiency**: QueryBuilder uses indexed parameters (`:p1`, `:p2`) for optimal performance

### Benchmarks

From JMH benchmarks:

| Operation | Time (µs) | Throughput (ops/s) |
|-----------|-----------|-------------------|
| Simple WHERE | 0.8 | 1,250,000 |
| Multiple AND | 1.2 | 833,333 |
| Complex with groups | 2.5 | 400,000 |
| With JOINs | 3.0 | 333,333 |

See **[Benchmarking](Benchmarking)** for complete performance analysis.

---

## Error Handling

### QueryBuildException

Thrown for invalid query construction:

```java
try {
    QueryBuilder.forEntity(User.class)
        .where("", "value");  // Invalid: empty field name
} catch (QueryBuildException e) {
    System.err.println(e.getMessage());
    // Output: Field name cannot be null or empty
}
```

### Common Errors

| Error | Cause | Solution |
|-------|-------|----------|
| Invalid field name | Empty or null field | Provide valid field matching `[A-Za-z0-9_\.]+` |
| Empty IN collection | `.whereIn()` with empty list | Ensure collection has values |
| Invalid operator | Unsupported operator | Use supported predicate methods |
| Unclosed group | `.openGroup()` without `.closeGroup()` | Balance group operations |

See **[Error Handling](Error-Handling)** for complete error catalog.

---

## See Also

- **[Getting Started](Getting-Started)** - Setup and basic usage
- **[Cache Manager](Cache-Manager)** - Cache query results
- **[Optimizer](Optimizer)** - Optimize query performance
- **[API Reference](API-Reference)** - Complete API documentation
- **[Examples](https://github.com/query4j/dynamicquerybuilder/tree/master/examples)** - Real-world usage examples

---

**Last Updated:** October 2025  
**Version:** 1.0.0
