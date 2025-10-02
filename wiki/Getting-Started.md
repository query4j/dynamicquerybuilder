# Getting Started with Query4j

Welcome to Query4j! This guide will help you set up Query4j and build your first dynamic query in minutes.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Installation](#installation)
3. [Your First Query](#your-first-query)
4. [Basic Query Operations](#basic-query-operations)
5. [Next Steps](#next-steps)

---

## Prerequisites

Before you begin, ensure you have:

- **Java 17** or higher installed
- **Maven 3.6+** or **Gradle 8.5+** build tool
- Your favorite Java IDE (IntelliJ IDEA, Eclipse, VS Code, etc.)
- Basic understanding of Java and SQL

### Verify Java Installation

```bash
java -version
# Expected output: java version "17" or higher
```

---

## Installation

### Maven

Add Query4j dependencies to your `pom.xml`:

```xml
<dependencies>
    <!-- Core module (required) -->
    <dependency>
        <groupId>com.github.query4j</groupId>
        <artifactId>dynamicquerybuilder-core</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>

    <!-- Cache module (optional) -->
    <dependency>
        <groupId>com.github.query4j</groupId>
        <artifactId>dynamicquerybuilder-cache</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>

    <!-- Optimizer module (optional) -->
    <dependency>
        <groupId>com.github.query4j</groupId>
        <artifactId>dynamicquerybuilder-optimizer</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

### Gradle (Groovy DSL)

Add to your `build.gradle`:

```groovy
dependencies {
    // Core module (required)
    implementation 'com.github.query4j:dynamicquerybuilder-core:1.0.0-SNAPSHOT'
    
    // Cache module (optional)
    implementation 'com.github.query4j:dynamicquerybuilder-cache:1.0.0-SNAPSHOT'
    
    // Optimizer module (optional)
    implementation 'com.github.query4j:dynamicquerybuilder-optimizer:1.0.0-SNAPSHOT'
}
```

### Gradle (Kotlin DSL)

Add to your `build.gradle.kts`:

```kotlin
dependencies {
    // Core module (required)
    implementation("com.github.query4j:dynamicquerybuilder-core:1.0.0-SNAPSHOT")
    
    // Cache module (optional)
    implementation("com.github.query4j:dynamicquerybuilder-cache:1.0.0-SNAPSHOT")
    
    // Optimizer module (optional)
    implementation("com.github.query4j:dynamicquerybuilder-optimizer:1.0.0-SNAPSHOT")
}
```

### Verify Installation

Create a simple test to verify the installation:

```java
import com.github.query4j.core.QueryBuilder;

public class VerifyInstallation {
    public static void main(String[] args) {
        QueryBuilder<?> query = QueryBuilder.forEntity("users")
            .where("active", true);
        
        System.out.println("SQL: " + query.toSQL());
        // Output: SQL: SELECT * FROM users WHERE active = :p1
        
        System.out.println("Query4j is ready!");
    }
}
```

---

## Your First Query

Let's build a simple query to fetch active users from a database.

### Step 1: Import Required Classes

```java
import com.github.query4j.core.QueryBuilder;
import java.util.List;
import java.util.Map;
```

### Step 2: Create Your Entity Model

```java
public class User {
    private Long id;
    private String username;
    private String email;
    private boolean active;
    private String department;
    
    // Constructors, getters, setters...
}
```

### Step 3: Build Your Query

```java
// Create a query builder for User entity
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .where("active", true);

// Generate SQL
String sql = query.toSQL();
System.out.println("SQL: " + sql);
// Output: SELECT * FROM User WHERE active = :p1

// Get parameters
Map<String, Object> params = query.getParameters();
System.out.println("Parameters: " + params);
// Output: Parameters: {p1=true}
```

### Step 4: Execute the Query

```java
// In a real application, you'd execute with your persistence layer
// Example with JDBC:
try (Connection conn = dataSource.getConnection();
     PreparedStatement stmt = conn.prepareStatement(query.toSQL())) {
    
    // Bind parameters
    int paramIndex = 1;
    for (Object value : query.getParameters().values()) {
        stmt.setObject(paramIndex++, value);
    }
    
    // Execute and process results
    try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
            // Process results...
        }
    }
}
```

---

## Basic Query Operations

### Simple WHERE Clause

```java
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .where("department", "Engineering");

// SQL: SELECT * FROM User WHERE department = :p1
```

### Multiple Conditions (AND)

```java
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .where("department", "Engineering")
    .and()
    .where("active", true);

// SQL: SELECT * FROM User WHERE department = :p1 AND active = :p2
```

### OR Conditions

```java
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .where("department", "Engineering")
    .or()
    .where("department", "Sales");

// SQL: SELECT * FROM User WHERE department = :p1 OR department = :p2
```

### IN Predicate

```java
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .whereIn("role", Arrays.asList("admin", "developer", "manager"));

// SQL: SELECT * FROM User WHERE role IN (:p1, :p2, :p3)
```

### LIKE Pattern Matching

```java
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .whereLike("email", "%@company.com");

// SQL: SELECT * FROM User WHERE email LIKE :p1
```

### BETWEEN Range Queries

```java
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .whereBetween("age", 25, 45);

// SQL: SELECT * FROM User WHERE age BETWEEN :p1 AND :p2
```

### NULL Checks

```java
// IS NULL
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .whereIsNull("deletedAt");

// SQL: SELECT * FROM User WHERE deletedAt IS NULL

// IS NOT NULL
QueryBuilder<User> usersWithEmail = QueryBuilder.forEntity(User.class)
    .whereIsNotNull("email");

// SQL: SELECT * FROM User WHERE email IS NOT NULL
```

### Sorting

```java
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .orderBy("lastName")
    .orderBy("firstName", "ASC");

// SQL: SELECT * FROM User WHERE active = :p1 ORDER BY lastName, firstName ASC
```

### Pagination

```java
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .orderBy("id")
    .limit(20)
    .offset(40);

// SQL: SELECT * FROM User WHERE active = :p1 ORDER BY id LIMIT 20 OFFSET 40
// This fetches page 3 (20 records per page)
```

### Logical Grouping

```java
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .openGroup()
        .where("department", "Engineering")
        .or()
        .where("department", "Sales")
    .closeGroup()
    .and()
    .where("active", true);

// SQL: SELECT * FROM User WHERE (department = :p1 OR department = :p2) AND active = :p3
```

---

## Next Steps

Now that you've built your first queries, explore more advanced features:

### Tutorials
- **[Advanced Usage Tutorial](https://github.com/query4j/dynamicquerybuilder/blob/master/ADVANCED.md)** - Complex queries, joins, aggregations
- **[Core Module Guide](Core-Module)** - Comprehensive API documentation
- **[Examples](https://github.com/query4j/dynamicquerybuilder/tree/master/examples)** - Real-world usage examples

### Performance & Optimization
- **[Query Optimizer](Optimizer)** - Optimize query performance
- **[Caching Strategies](Cache-Manager)** - Cache query results
- **[Benchmarking](Benchmarking)** - Performance metrics and tuning

### Configuration & Best Practices
- **[Configuration Guide](Configuration)** - Customize Query4j behavior
- **[Error Handling](Error-Handling)** - Exception handling patterns
- **[FAQ](FAQ-and-Troubleshooting)** - Common questions and issues

### Contributing
- **[Contributing Guide](Contributing)** - Help improve Query4j
- **[GitHub Issues](https://github.com/query4j/dynamicquerybuilder/issues)** - Report bugs or request features

---

## Troubleshooting

### Common Issues

#### Query doesn't compile

**Problem:** Import errors or compilation failures.

**Solution:** Ensure you've added the correct dependency and imported `com.github.query4j.core.QueryBuilder`.

#### Invalid field name error

**Problem:** `QueryBuildException` with message about invalid field names.

**Solution:** Field names must match pattern `[A-Za-z0-9_\.]+` and cannot be empty:

```java
// ❌ Invalid
.where("", value)              // Empty field name
.where("field-name", value)    // Contains hyphen

// ✅ Valid
.where("fieldName", value)     // CamelCase
.where("field_name", value)    // Snake case
.where("table.field", value)   // Qualified name
```

#### Parameters not binding correctly

**Problem:** Parameters aren't being passed to the database correctly.

**Solution:** Use `query.getParameters()` to retrieve the parameter map and bind in order:

```java
Map<String, Object> params = query.getParameters();
for (Map.Entry<String, Object> entry : params.entrySet()) {
    // Bind each parameter to your prepared statement
}
```

For more troubleshooting help, see **[FAQ and Troubleshooting](FAQ-and-Troubleshooting)**.

---

**Next:** [Core Module Documentation](Core-Module)

---

**Last Updated:** December 2024  
**Version:** 1.0.0
