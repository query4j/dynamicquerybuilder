# Query4j Dynamic Query Builder - Upgrade Guide

## Overview

This guide provides instructions for upgrading to or adopting Query4j Dynamic Query Builder v1.0.0 in your projects.

---

## Table of Contents

1. [New Installation](#new-installation)
2. [Upgrading from Pre-release Versions](#upgrading-from-pre-release-versions)
3. [Migration from Other Libraries](#migration-from-other-libraries)
4. [Configuration Changes](#configuration-changes)
5. [API Changes](#api-changes)
6. [Testing Your Upgrade](#testing-your-upgrade)
7. [Troubleshooting](#troubleshooting)

---

## New Installation

### For New Projects

If you're starting a new project, simply add Query4j to your dependencies:

#### Maven

```xml
<dependencies>
    <!-- Core Module (Required) -->
    <dependency>
        <groupId>com.github.query4j</groupId>
        <artifactId>dynamicquerybuilder-core</artifactId>
        <version>1.0.0</version>
    </dependency>
    
    <!-- Cache Module (Optional) -->
    <dependency>
        <groupId>com.github.query4j</groupId>
        <artifactId>dynamicquerybuilder-cache</artifactId>
        <version>1.0.0</version>
    </dependency>
    
    <!-- Optimizer Module (Optional) -->
    <dependency>
        <groupId>com.github.query4j</groupId>
        <artifactId>dynamicquerybuilder-optimizer</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

#### Gradle

```groovy
dependencies {
    // Core Module (Required)
    implementation 'com.github.query4j:dynamicquerybuilder-core:1.0.0'
    
    // Cache Module (Optional)
    implementation 'com.github.query4j:dynamicquerybuilder-cache:1.0.0'
    
    // Optimizer Module (Optional)
    implementation 'com.github.query4j:dynamicquerybuilder-optimizer:1.0.0'
}
```

### Quick Start

After adding dependencies:

1. **Import the QueryBuilder**:
   ```java
   import com.github.query4j.core.QueryBuilder;
   ```

2. **Build Your First Query**:
   ```java
   QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
       .where("status", "ACTIVE")
       .orderBy("lastName", "ASC")
       .page(0, 20);
   
   List<User> users = query.findAll();
   ```

3. **Explore Examples**: Check the [examples](examples/README.md) directory for more patterns

---

## Upgrading from Pre-release Versions

### From v1.0.0-SNAPSHOT

If you were using the snapshot version, upgrading is straightforward:

#### Step 1: Update Dependencies

Change your version from `1.0.0-SNAPSHOT` to `1.0.0`:

**Maven:**
```xml
<dependency>
    <groupId>com.github.query4j</groupId>
    <artifactId>dynamicquerybuilder-core</artifactId>
    <version>1.0.0</version> <!-- Changed from 1.0.0-SNAPSHOT -->
</dependency>
```

**Gradle:**
```groovy
implementation 'com.github.query4j:dynamicquerybuilder-core:1.0.0' // Changed from 1.0.0-SNAPSHOT
```

#### Step 2: Update Imports (if necessary)

No import changes are required. All package names remain the same:
- `com.github.query4j.core.*`
- `com.github.query4j.cache.*`
- `com.github.query4j.optimizer.*`

#### Step 3: Review Configuration

If you were using custom configuration, verify it against the stable [Configuration Guide](docs/Configuration.md):

```java
// Verify your configuration is compatible
Query4jConfig config = Query4jConfig.defaultConfig();
// or use your custom configuration
```

#### Step 4: Run Your Tests

Execute your test suite to ensure everything works:

```bash
# Maven
mvn clean test

# Gradle
./gradlew clean test
```

#### Step 5: Update Documentation References

If you have internal documentation referencing the snapshot version, update to v1.0.0.

### Breaking Changes from Snapshot

**None** - The v1.0.0 release maintains full compatibility with the latest snapshot versions.

---

## Migration from Other Libraries

### From JPA Criteria API

Query4j provides a more fluent and intuitive API compared to JPA Criteria:

#### Before (JPA Criteria):
```java
CriteriaBuilder cb = entityManager.getCriteriaBuilder();
CriteriaQuery<User> cq = cb.createQuery(User.class);
Root<User> root = cq.from(User.class);
cq.select(root)
  .where(cb.and(
      cb.equal(root.get("status"), "ACTIVE"),
      cb.greaterThanOrEqualTo(root.get("age"), 18)
  ))
  .orderBy(cb.asc(root.get("lastName")));

TypedQuery<User> query = entityManager.createQuery(cq);
List<User> users = query.getResultList();
```

#### After (Query4j):
```java
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .where("status", "ACTIVE")
    .and()
    .where("age", ">=", 18)
    .orderBy("lastName", "ASC");

List<User> users = query.findAll();
```

**Migration Steps:**

1. Identify CriteriaBuilder usage in your codebase
2. Replace with QueryBuilder.forEntity()
3. Convert predicates to fluent where() calls
4. Replace orderBy() with fluent orderBy() calls
5. Replace createQuery() and getResultList() with findAll()

### From QueryDSL

Query4j offers similar type safety without code generation:

#### Before (QueryDSL):
```java
QUser user = QUser.user;
List<User> users = queryFactory
    .selectFrom(user)
    .where(user.status.eq("ACTIVE")
        .and(user.age.goe(18)))
    .orderBy(user.lastName.asc())
    .fetch();
```

#### After (Query4j):
```java
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .where("status", "ACTIVE")
    .and()
    .where("age", ">=", 18)
    .orderBy("lastName", "ASC");

List<User> users = query.findAll();
```

**Migration Steps:**

1. Remove Q-class generation from build
2. Replace queryFactory.selectFrom() with QueryBuilder.forEntity()
3. Convert type-safe predicates to string-based where() calls
4. Replace fetch() with findAll()
5. Remove QueryDSL dependencies

### From jOOQ

Query4j provides similar SQL-like fluency with less setup:

#### Before (jOOQ):
```java
Result<Record> result = dsl
    .select()
    .from(USERS)
    .where(USERS.STATUS.eq("ACTIVE"))
    .and(USERS.AGE.ge(18))
    .orderBy(USERS.LAST_NAME.asc())
    .fetch();
```

#### After (Query4j):
```java
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .where("status", "ACTIVE")
    .and()
    .where("age", ">=", 18)
    .orderBy("lastName", "ASC");

List<User> users = query.findAll();
```

**Migration Steps:**

1. Map jOOQ table/field references to entity field names
2. Replace DSL context with QueryBuilder
3. Convert column references to string field names
4. Replace fetch() with appropriate Query4j methods
5. Update test cases for new API

### From Spring Data Specifications

Query4j can complement or replace Spring Data Specifications:

#### Before (Spring Data Specifications):
```java
Specification<User> spec = (root, query, cb) -> 
    cb.and(
        cb.equal(root.get("status"), "ACTIVE"),
        cb.greaterThanOrEqualTo(root.get("age"), 18)
    );

List<User> users = userRepository.findAll(spec, Sort.by("lastName").ascending());
```

#### After (Query4j):
```java
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .where("status", "ACTIVE")
    .and()
    .where("age", ">=", 18)
    .orderBy("lastName", "ASC");

List<User> users = query.findAll();
```

**Migration Steps:**

1. Replace Specification interface implementations
2. Convert lambda expressions to fluent API calls
3. Replace repository.findAll() with query.findAll()
4. Update pagination to use Query4j Page API
5. Maintain Spring Data repositories for simple queries

---

## Configuration Changes

### Default Configuration

The default configuration is suitable for most applications:

```java
Query4jConfig config = Query4jConfig.defaultConfig();
```

### Custom Configuration

If you need custom settings:

```java
Query4jConfig config = Query4jConfig.builder()
    .core(CoreConfig.builder()
        .maxPredicateDepth(10)
        .maxPredicateCount(100)
        .defaultPageSize(20)
        .queryStatisticsEnabled(true)
        .build())
    .cache(CacheConfig.builder()
        .enabled(true)
        .maxSize(1000L)
        .defaultTtlSeconds(300L)
        .build())
    .build();
```

### Environment-Specific Configuration

Use profiles for different environments:

```java
// Development
Query4jConfig devConfig = Query4jConfigurationFactory.builder()
    .core(CoreConfig.developmentConfig())
    .cache(CacheConfig.developmentConfig())
    .build();

// Production
Query4jConfig prodConfig = Query4jConfigurationFactory.builder()
    .core(CoreConfig.highPerformanceConfig())
    .cache(CacheConfig.highPerformanceConfig())
    .build();
```

See [Configuration Guide](docs/Configuration.md) for complete details.

---

## API Changes

### No Breaking Changes

The v1.0.0 release introduces **no breaking changes** from the latest snapshot versions.

### New APIs Added

The following APIs are new in v1.0.0:

#### Subquery Support
```java
// EXISTS
query.exists(subquery);

// IN with subquery
query.in("field", subquery);
```

#### Custom Functions
```java
query.customFunction("UPPER", "firstName", "JOHN");
```

#### Query Hints
```java
query.hint("USE_INDEX", "idx_user_email");
```

#### Enhanced Statistics
```java
QueryStats stats = query.getExecutionStats();
```

### Deprecated APIs

**None** - This is the first stable release, so no APIs are deprecated.

---

## Testing Your Upgrade

### Recommended Testing Approach

1. **Unit Tests**: Run your existing unit tests
2. **Integration Tests**: Verify database interactions
3. **Performance Tests**: Benchmark query execution
4. **Manual Testing**: Test critical user workflows

### Test Checklist

- [ ] All unit tests pass
- [ ] Integration tests with database pass
- [ ] Query generation produces expected SQL
- [ ] Parameter binding works correctly
- [ ] Pagination functions as expected
- [ ] Cache behavior is correct (if using cache module)
- [ ] Performance meets expectations
- [ ] Error handling works as expected

### Smoke Test

Run this quick smoke test after upgrading:

```java
@Test
void smokeTest() {
    // Basic query
    QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
        .where("status", "ACTIVE")
        .orderBy("id", "ASC");
    
    List<User> users = query.findAll();
    assertNotNull(users);
    
    // Pagination
    Page<User> page = query.findPage(0, 10);
    assertNotNull(page);
    assertTrue(page.getTotalElements() >= 0);
    
    // Count
    long count = query.count();
    assertTrue(count >= 0);
}
```

---

## Troubleshooting

### Common Issues

#### Issue: Dependencies Not Resolving

**Symptoms**: Build fails with "Cannot resolve dependency" errors

**Solution**: 
1. Ensure Maven Central is in your repositories
2. Clear your local Maven cache: `rm -rf ~/.m2/repository/com/github/query4j`
3. Refresh dependencies: `mvn clean install` or `./gradlew clean build --refresh-dependencies`

#### Issue: NoClassDefFoundError

**Symptoms**: Runtime exception `NoClassDefFoundError`

**Solution**:
1. Verify all required dependencies are included
2. Check for dependency conflicts: `mvn dependency:tree` or `./gradlew dependencies`
3. Ensure Java 17+ is being used at runtime

#### Issue: SQL Generation Errors

**Symptoms**: Generated SQL is malformed or incorrect

**Solution**:
1. Review field names in where() clauses (must match entity fields)
2. Check operator syntax (use ">=", "<=", etc.)
3. Enable SQL logging to inspect generated queries
4. Consult [FAQ](docs/FAQ_AND_TROUBLESHOOTING.md)

#### Issue: Performance Degradation

**Symptoms**: Queries are slower than expected

**Solution**:
1. Enable query statistics to identify bottlenecks
2. Use the optimizer module to get suggestions
3. Review cache configuration and hit rates
4. Check database indexes on frequently queried fields
5. See [Performance Guide](BENCHMARKS.md)

### Getting Help

If you encounter issues not covered here:

1. **Check Documentation**: [docs/](docs/)
2. **Search Issues**: [GitHub Issues](https://github.com/query4j/dynamicquerybuilder/issues)
3. **Ask Questions**: [GitHub Discussions](https://github.com/query4j/dynamicquerybuilder/discussions)
4. **Report Bugs**: Open a new issue with reproduction steps

---

## Best Practices for Adoption

### Gradual Migration

For large codebases, migrate gradually:

1. **Start Small**: Begin with simple queries in non-critical paths
2. **Measure Impact**: Monitor performance and correctness
3. **Expand Coverage**: Gradually replace more complex queries
4. **Maintain Tests**: Keep existing tests passing throughout migration

### Integration Patterns

#### With Spring Boot

```java
@Service
public class UserService {
    private final EntityManager entityManager;
    
    public UserService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    public List<User> findActiveUsers() {
        return QueryBuilder.forEntity(User.class)
            .where("status", "ACTIVE")
            .findAll();
    }
}
```

#### With Repository Pattern

```java
public interface UserRepository {
    List<User> findActiveUsers();
}

public class UserRepositoryImpl implements UserRepository {
    @Override
    public List<User> findActiveUsers() {
        return QueryBuilder.forEntity(User.class)
            .where("status", "ACTIVE")
            .findAll();
    }
}
```

#### With Caching

```java
@Service
public class CachedUserService {
    private final CacheManager cacheManager;
    
    public List<User> findActiveUsers() {
        return QueryBuilder.forEntity(User.class)
            .where("status", "ACTIVE")
            .cache(true, 300) // Cache for 5 minutes
            .findAll();
    }
}
```

---

## Version Compatibility

### Supported Java Versions

- ✅ **Java 17** (LTS) - Recommended
- ✅ **Java 18, 19, 20** - Supported
- ✅ **Java 21** (LTS) - Fully supported

### Supported Databases

- ✅ **H2**: 1.4.x, 2.x
- ✅ **PostgreSQL**: 10, 11, 12, 13, 14, 15, 16
- ✅ **MySQL**: 5.7, 8.0, 8.1
- ✅ **MariaDB**: 10.3, 10.4, 10.5, 10.6, 10.11

### Supported Frameworks

- ✅ **Spring Boot**: 2.7.x, 3.0.x, 3.1.x, 3.2.x
- ✅ **Hibernate**: 5.6.x, 6.0.x, 6.1.x, 6.2.x, 6.3.x
- ✅ **JPA**: 2.2, 3.0, 3.1

---

## Additional Resources

- **[Quick Start Guide](QUICKSTART.md)**: Get started quickly
- **[API Guide](docs/API_GUIDE.md)**: Complete API reference
- **[Configuration Guide](docs/Configuration.md)**: Configuration options
- **[Examples](examples/README.md)**: Sample applications
- **[FAQ](docs/FAQ_AND_TROUBLESHOOTING.md)**: Common questions
- **[Benchmarks](BENCHMARKS.md)**: Performance analysis

---

## Feedback

We value your feedback! Let us know about your upgrade experience:

- **Positive Experience**: Star the project on GitHub ⭐
- **Issues**: Report on [GitHub Issues](https://github.com/query4j/dynamicquerybuilder/issues)
- **Questions**: Ask on [GitHub Discussions](https://github.com/query4j/dynamicquerybuilder/discussions)
- **Suggestions**: Share on [GitHub Discussions](https://github.com/query4j/dynamicquerybuilder/discussions)

---

**Welcome to Query4j v1.0.0! 🎉**

*Happy querying!*
