# Query4j Core Module

The core module provides the fundamental query building functionality for the Query4j Dynamic Query Builder library.

## Features

- **Fluent Builder API**: Chainable methods for constructing complex queries
- **Multi-table JOIN Support**: INNER, LEFT, RIGHT, and FETCH joins
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

```java
// Count orders per user
List<Object[]> userOrderCounts = QueryBuilder.forEntity(User.class)
    .select("id", "name", "COUNT(orders.id)")
    .leftJoin("orders")
    .groupBy("id", "name")
    .having("COUNT(orders.id)", ">", 5)
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

## Next Steps

- See the [main README](../README.md) for complete library overview
- Check the [examples module](../examples/) for more detailed use cases
- Refer to the JavaDoc API documentation for complete method signatures