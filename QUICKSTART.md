# Query4j Quickstart Tutorial

Welcome to Query4j! This quickstart guide will get you up and running with the Query4j Dynamic Query Builder in minutes. By the end of this tutorial, you'll be building dynamic SQL queries with filters, sorting, and pagination.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Project Setup](#project-setup)
- [Your First Query](#your-first-query)
- [Adding Filters](#adding-filters)
- [Sorting Results](#sorting-results)
- [Pagination](#pagination)
- [Pattern Matching](#pattern-matching)
- [Logical Operators](#logical-operators)
- [Next Steps](#next-steps)

## Prerequisites

Before you begin, ensure you have:

- **Java 17** or higher installed
- **Maven 3.6+** or **Gradle 8.5+** installed
- Your favorite Java IDE (IntelliJ IDEA, Eclipse, VS Code, etc.)
- Basic understanding of Java and SQL

Verify your Java version:

```bash
java -version
# Should show: java version "17" or higher
```

## Project Setup

### Using Gradle

Add Query4j to your `build.gradle`:

```gradle
dependencies {
    // Core query builder functionality
    implementation 'com.github.query4j:dynamicquerybuilder-core:1.0.0'
    
    // Optional: Add cache module for result caching
    implementation 'com.github.query4j:dynamicquerybuilder-cache:1.0.0'
    
    // Optional: Add optimizer module for query optimization
    implementation 'com.github.query4j:dynamicquerybuilder-optimizer:1.0.0'
}
```

### Using Maven

Add Query4j to your `pom.xml`:

```xml
<dependencies>
    <!-- Core query builder functionality -->
    <dependency>
        <groupId>com.github.query4j</groupId>
        <artifactId>dynamicquerybuilder-core</artifactId>
        <version>1.0.0</version>
    </dependency>
    
    <!-- Optional: Add cache module for result caching -->
    <dependency>
        <groupId>com.github.query4j</groupId>
        <artifactId>dynamicquerybuilder-cache</artifactId>
        <version>1.0.0</version>
    </dependency>
    
    <!-- Optional: Add optimizer module for query optimization -->
    <dependency>
        <groupId>com.github.query4j</groupId>
        <artifactId>dynamicquerybuilder-optimizer</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

### Creating a Simple Domain Model

Let's create a `User` entity for our examples:

```java
package com.example.model;

import java.time.LocalDate;

public class User {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private String role;
    private Boolean active;
    private LocalDate joinDate;
    
    // Constructors, getters, and setters omitted for brevity
}
```

## Your First Query

Let's build your first dynamic query! We'll fetch all active users.

```java
import com.github.query4j.core.QueryBuilder;
import java.util.List;

public class QuickstartDemo {
    public static void main(String[] args) {
        // Simple equality query
        List<User> activeUsers = QueryBuilder.forEntity(User.class)
            .where("active", true)
            .findAll();
        
        System.out.println("Found " + activeUsers.size() + " active users");
    }
}
```

**What's happening here?**

1. `QueryBuilder.forEntity(User.class)` - Creates a new query builder for the User entity
2. `.where("active", true)` - Adds a WHERE condition: `active = true`
3. `.findAll()` - Executes the query and returns all matching results

**Generated SQL:**
```sql
SELECT * FROM User WHERE active = :p1
-- Parameters: {p1: true}
```

## Adding Filters

### Single Condition

Filter users by department:

```java
List<User> engineers = QueryBuilder.forEntity(User.class)
    .where("department", "Engineering")
    .findAll();
```

### Multiple Conditions with AND

Filter users by department AND active status:

```java
List<User> activeEngineers = QueryBuilder.forEntity(User.class)
    .where("department", "Engineering")
    .and()
    .where("active", true)
    .findAll();
```

**Generated SQL:**
```sql
SELECT * FROM User WHERE department = :p1 AND active = :p2
-- Parameters: {p1: "Engineering", p2: true}
```

### Using Comparison Operators

Query4j supports all standard comparison operators:

```java
import java.time.LocalDate;

// Greater than
List<User> recentHires = QueryBuilder.forEntity(User.class)
    .where("joinDate", ">", LocalDate.now().minusMonths(6))
    .findAll();

// Less than or equal
List<User> seniorStaff = QueryBuilder.forEntity(User.class)
    .where("joinDate", "<=", LocalDate.of(2020, 1, 1))
    .findAll();

// Not equal
List<User> nonInterns = QueryBuilder.forEntity(User.class)
    .where("role", "!=", "intern")
    .findAll();
```

**Supported operators:** `=`, `!=`, `<`, `<=`, `>`, `>=`

### IN Predicate

Check if a field matches any value in a list:

```java
import java.util.Arrays;

List<User> leadership = QueryBuilder.forEntity(User.class)
    .whereIn("role", Arrays.asList("manager", "director", "vp"))
    .findAll();
```

**Generated SQL:**
```sql
SELECT * FROM User WHERE role IN (:p1, :p2, :p3)
-- Parameters: {p1: "manager", p2: "director", p3: "vp"}
```

### NOT IN Predicate

Exclude specific values:

```java
List<User> nonTestUsers = QueryBuilder.forEntity(User.class)
    .whereNotIn("department", Arrays.asList("Test", "QA"))
    .findAll();
```

### NULL Checks

Query for null or non-null values:

```java
// Find users without email
List<User> noEmail = QueryBuilder.forEntity(User.class)
    .whereIsNull("email")
    .findAll();

// Find users with email
List<User> withEmail = QueryBuilder.forEntity(User.class)
    .whereIsNotNull("email")
    .findAll();
```

## Sorting Results

### Single Field Sort

Sort users by last name (ascending):

```java
List<User> users = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .orderBy("lastName")
    .findAll();
```

### Descending Sort

Sort by join date (newest first):

```java
List<User> users = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .orderByDescending("joinDate")
    .findAll();
```

### Multiple Sort Fields

Sort by department, then by lastName:

```java
List<User> users = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .orderBy("department")
    .orderBy("lastName")
    .findAll();
```

**Generated SQL:**
```sql
SELECT * FROM User 
WHERE active = :p1 
ORDER BY department ASC, lastName ASC
```

## Pagination

### Using limit() and offset()

Limit results and skip records:

```java
// Get 10 users, skipping the first 20
List<User> users = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .orderBy("lastName")
    .limit(10)
    .offset(20)
    .findAll();
```

### Using page()

More intuitive page-based pagination:

```java
import com.github.query4j.core.Page;

// Get page 2 (zero-based), with 20 users per page
Page<User> userPage = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .orderBy("lastName")
    .page(1, 20)  // Page 1 (second page), 20 items per page
    .findPage();

// Access page metadata
System.out.println("Total users: " + userPage.getTotalElements());
System.out.println("Total pages: " + userPage.getTotalPages());
System.out.println("Current page: " + userPage.getNumber());
System.out.println("Page size: " + userPage.getSize());
System.out.println("Has next page: " + userPage.hasNext());
System.out.println("Has previous page: " + userPage.hasPrevious());

// Access page content
List<User> usersOnPage = userPage.getContent();
```

**Typical pagination loop:**

```java
int pageSize = 20;
int pageNumber = 0;
Page<User> page;

do {
    page = QueryBuilder.forEntity(User.class)
        .where("active", true)
        .orderBy("lastName")
        .page(pageNumber, pageSize)
        .findPage();
    
    // Process users on this page
    for (User user : page.getContent()) {
        System.out.println(user.getFirstName() + " " + user.getLastName());
    }
    
    pageNumber++;
} while (page.hasNext());
```

## Pattern Matching

### LIKE Operator

Find users with email addresses containing "gmail":

```java
List<User> gmailUsers = QueryBuilder.forEntity(User.class)
    .whereLike("email", "%gmail.com%")
    .findAll();
```

**Pattern wildcards:**
- `%` - Matches any sequence of characters
- `_` - Matches any single character

**Common patterns:**

```java
// Starts with "John"
.whereLike("firstName", "John%")

// Ends with "Smith"
.whereLike("lastName", "%Smith")

// Contains "admin"
.whereLike("role", "%admin%")

// Exactly 5 characters
.whereLike("code", "_____")
```

### NOT LIKE Operator

Exclude test accounts:

```java
List<User> realUsers = QueryBuilder.forEntity(User.class)
    .whereNotLike("email", "%test%")
    .and()
    .whereNotLike("email", "%dummy%")
    .findAll();
```

### BETWEEN Operator

Find users who joined in a date range:

```java
import java.time.LocalDate;

LocalDate startDate = LocalDate.of(2023, 1, 1);
LocalDate endDate = LocalDate.of(2023, 12, 31);

List<User> users2023 = QueryBuilder.forEntity(User.class)
    .whereBetween("joinDate", startDate, endDate)
    .findAll();
```

**Generated SQL:**
```sql
SELECT * FROM User 
WHERE joinDate BETWEEN :p1 AND :p2
-- Parameters: {p1: "2023-01-01", p2: "2023-12-31"}
```

## Logical Operators

### AND Operator

Combine conditions with AND (all must be true):

```java
List<User> results = QueryBuilder.forEntity(User.class)
    .where("department", "Engineering")
    .and()
    .where("active", true)
    .and()
    .where("role", "developer")
    .findAll();
```

### OR Operator

Combine conditions with OR (at least one must be true):

```java
List<User> results = QueryBuilder.forEntity(User.class)
    .where("department", "Engineering")
    .or()
    .where("department", "Product")
    .findAll();
```

### Complex Conditions with Grouping

Use `openGroup()` and `closeGroup()` to control operator precedence:

```java
// Find users who are:
// (Engineering OR Product department) AND (active) AND (manager, lead, or architect role)
List<User> leaders = QueryBuilder.forEntity(User.class)
    .openGroup()
        .where("department", "Engineering")
        .or()
        .where("department", "Product")
    .closeGroup()
    .and()
    .where("active", true)
    .and()
    .whereIn("role", Arrays.asList("manager", "lead", "architect"))
    .findAll();
```

**Generated SQL:**
```sql
SELECT * FROM User 
WHERE (department = :p1 OR department = :p2) 
  AND active = :p3 
  AND role IN (:p4, :p5, :p6)
```

### NOT Operator

Negate a condition:

```java
List<User> results = QueryBuilder.forEntity(User.class)
    .not()
    .where("status", "deleted")
    .findAll();
```

## Complete Example

Let's put it all together with a real-world scenario:

```java
package com.example;

import com.github.query4j.core.QueryBuilder;
import com.github.query4j.core.Page;
import com.example.model.User;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class UserSearchExample {
    
    public static void main(String[] args) {
        // Complex user search with multiple criteria
        Page<User> results = searchUsers(
            Arrays.asList("Engineering", "Product"),  // departments
            Arrays.asList("developer", "manager"),     // roles
            LocalDate.now().minusYears(2),            // joined after
            true,                                      // active only
            0,                                         // page number
            25                                         // page size
        );
        
        // Display results
        System.out.println("=== User Search Results ===");
        System.out.println("Total matching users: " + results.getTotalElements());
        System.out.println("Page " + (results.getNumber() + 1) + " of " + results.getTotalPages());
        System.out.println();
        
        for (User user : results.getContent()) {
            System.out.printf("%s %s - %s (%s)%n",
                user.getFirstName(),
                user.getLastName(),
                user.getDepartment(),
                user.getRole()
            );
        }
    }
    
    public static Page<User> searchUsers(
            List<String> departments,
            List<String> roles,
            LocalDate joinedAfter,
            boolean activeOnly,
            int pageNumber,
            int pageSize) {
        
        // Build dynamic query based on provided criteria
        QueryBuilder<User> query = QueryBuilder.forEntity(User.class);
        
        // Filter by departments (if provided)
        if (departments != null && !departments.isEmpty()) {
            query = query.whereIn("department", departments);
        }
        
        // Filter by roles (if provided)
        if (roles != null && !roles.isEmpty()) {
            if (departments != null && !departments.isEmpty()) {
                query = query.and();
            }
            query = query.whereIn("role", roles);
        }
        
        // Filter by join date (if provided)
        if (joinedAfter != null) {
            query = query.and()
                .where("joinDate", ">=", joinedAfter);
        }
        
        // Filter by active status
        if (activeOnly) {
            query = query.and()
                .where("active", true);
        }
        
        // Require email address
        query = query.and()
            .whereIsNotNull("email");
        
        // Sort and paginate
        return query
            .orderBy("department")
            .orderBy("lastName")
            .orderBy("firstName")
            .page(pageNumber, pageSize)
            .findPage();
    }
}
```

## Expected Output

When you run the complete example, you should see output similar to:

```
=== User Search Results ===
Total matching users: 87
Page 1 of 4

Alice Johnson - Engineering (developer)
Bob Smith - Engineering (developer)
Carol White - Engineering (manager)
David Brown - Product (developer)
Emma Davis - Product (manager)
...
```

## Troubleshooting

### Common Issues

#### Issue: `ClassNotFoundException: com.github.query4j.core.QueryBuilder`

**Solution:** Ensure Query4j is properly added to your dependencies and your project is rebuilt.

```bash
# For Gradle
./gradlew clean build

# For Maven
mvn clean install
```

#### Issue: `IllegalArgumentException: Field name cannot be null or empty`

**Solution:** Make sure all field names in your queries match your entity's field names exactly (case-sensitive).

```java
// ❌ Wrong - field name doesn't exist
.where("userName", "John")  

// ✅ Correct - matches entity field
.where("firstName", "John")
```

#### Issue: SQL generated doesn't match your database dialect

**Solution:** Query4j generates standard SQL. If you need dialect-specific features, you may need to customize the SQL generation or use native queries for those cases.

#### Issue: Null values in collections

**Solution:** Always check for null before passing collections to `whereIn()`:

```java
List<String> roles = getUserRoles();  // Might be null

if (roles != null && !roles.isEmpty()) {
    query = query.whereIn("role", roles);
}
```

### Getting Help

If you encounter issues:

1. Check the [API Guide](docs/API_GUIDE.md) for detailed API documentation
2. Review the [Configuration Guide](docs/Configuration.md) for setup options
3. Explore [examples/](examples/) directory for more usage patterns
4. Open an issue on [GitHub Issues](https://github.com/query4j/dynamicquerybuilder/issues)

## Next Steps

Congratulations! You've learned the basics of Query4j. Here's what to explore next:

### 1. Advanced Features

Check out the [**Advanced Usage Tutorial**](ADVANCED.md) to learn about:
- Complex joins and associations
- Subqueries and correlated queries
- Aggregations and GROUP BY
- Query optimization and caching
- Asynchronous execution
- Batch processing

### 2. Configuration

Learn how to customize Query4j behavior in the [Configuration Guide](docs/Configuration.md):
- Cache settings
- Query timeouts
- Performance tuning
- Validation rules

### 3. Spring Boot Integration

See the [examples module](examples/README.md) for Spring Boot integration patterns.

### 4. Performance Optimization

Read about [performance benchmarks](benchmark/README.md) and optimization strategies.

### 5. Real-World Examples

Explore the comprehensive example applications:
- **AsyncQueryApp** - Concurrent query execution
- **BatchProcessingApp** - Large dataset processing
- **ComplexJoinsApp** - Multi-table queries

## Quick Reference

### Common Query Patterns

```java
// Simple query
QueryBuilder.forEntity(User.class).where("active", true).findAll()

// Multiple conditions
QueryBuilder.forEntity(User.class)
    .where("department", "Engineering")
    .and().where("active", true)
    .findAll()

// OR conditions
QueryBuilder.forEntity(User.class)
    .where("role", "admin")
    .or().where("role", "manager")
    .findAll()

// Pattern matching
QueryBuilder.forEntity(User.class)
    .whereLike("email", "%@company.com")
    .findAll()

// Range query
QueryBuilder.forEntity(User.class)
    .whereBetween("salary", 50000, 100000)
    .findAll()

// Null checks
QueryBuilder.forEntity(User.class)
    .whereIsNotNull("email")
    .findAll()

// IN query
QueryBuilder.forEntity(User.class)
    .whereIn("status", Arrays.asList("active", "pending"))
    .findAll()

// Sorting
QueryBuilder.forEntity(User.class)
    .orderBy("lastName")
    .orderByDescending("joinDate")
    .findAll()

// Pagination
QueryBuilder.forEntity(User.class)
    .page(0, 20)
    .findPage()

// Complex grouping
QueryBuilder.forEntity(User.class)
    .openGroup()
        .where("dept", "Eng")
        .or().where("dept", "Prod")
    .closeGroup()
    .and().where("active", true)
    .findAll()
```

---

**Ready to build powerful dynamic queries?** Start experimenting with Query4j in your project today!

For advanced topics, continue to the [**Advanced Usage Tutorial →**](ADVANCED.md)
