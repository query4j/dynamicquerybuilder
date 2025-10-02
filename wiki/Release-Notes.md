# Release Notes

Query4j version history and changelog.

## Current Version: 1.0.0 (October 2025)

### Initial Release

Query4j 1.0.0 marks the first production-ready release of the Dynamic Query Builder library.

#### Features

**Core Module:**
- ✅ Fluent query builder API with immutable design
- ✅ Comprehensive predicate support: Simple, In, Between, Like, Null
- ✅ Logical operators: AND, OR, NOT with grouping
- ✅ JOIN operations: INNER, LEFT, RIGHT, FETCH
- ✅ Aggregations: COUNT, SUM, AVG, MIN, MAX
- ✅ GROUP BY and HAVING clauses
- ✅ Sorting (ORDER BY) and pagination (LIMIT/OFFSET)
- ✅ Thread-safe copy-on-write semantics
- ✅ SQL dialect support: H2, PostgreSQL, MySQL

**Cache Module:**
- ✅ LRU (Least Recently Used) cache strategy
- ✅ LFU (Least Frequently Used) cache strategy
- ✅ Configurable capacity and TTL
- ✅ Cache metrics and monitoring
- ✅ Thread-safe concurrent access

**Optimizer Module:**
- ✅ Index suggestion generation
- ✅ Predicate pushdown analysis
- ✅ Join reordering recommendations
- ✅ SQL string analysis
- ✅ Database-specific optimization (PostgreSQL, MySQL, H2)
- ✅ Configurable analysis timeout and verbosity

**Documentation:**
- ✅ Comprehensive JavaDoc for all public APIs
- ✅ Quickstart tutorial
- ✅ Advanced usage guide
- ✅ API reference guide
- ✅ Configuration documentation
- ✅ Error catalog
- ✅ Performance benchmarks
- ✅ Real-world examples

#### Performance

From JMH benchmarks:
- Simple query construction: 0.8 µs
- Complex query with JOINs: 2.8 µs
- Optimizer analysis: 0.5-8.0 ms (depending on complexity)
- Cache lookup: 0.3 µs

#### Testing

- 95%+ code coverage
- 500+ unit tests
- Property-based testing with jqwik
- Integration tests with H2
- Performance benchmarks with JMH

---

## Version History

### 1.0.0-SNAPSHOT (Development)

Pre-release development versions leading to 1.0.0:

**v1.0.0-rc2 (November 2024):**
- Final bug fixes and documentation polish
- Performance tuning based on benchmarks
- Complete API documentation

**v1.0.0-rc1 (October 2024):**
- Feature complete for 1.0
- Comprehensive test coverage achieved
- Documentation review and updates

**v1.0.0-beta3 (September 2024):**
- Optimizer module refinements
- Cache performance improvements
- Additional examples and guides

**v1.0.0-beta2 (August 2024):**
- Query optimizer implementation
- Cache module enhancements
- Advanced tutorial creation

**v1.0.0-beta1 (July 2024):**
- Core query builder stabilization
- Cache module implementation
- Initial documentation

**v1.0.0-alpha (June 2024):**
- Initial prototype
- Basic query building functionality
- Proof of concept

---

## Planned Releases

### Version 1.1.0 (Q1 2025)

**Planned Features:**

**Enhanced Optimizer:**
- Cost-based query analysis
- Statistics-driven optimization
- Query plan visualization
- Performance regression detection

**Batch Operations:**
- Batch query execution API
- Bulk insert/update/delete support
- Transaction coordination

**Additional SQL Support:**
- Subquery support (IN subqueries, EXISTS)
- Window functions (ROW_NUMBER, RANK, etc.)
- Common Table Expressions (CTEs)

**Dialect Improvements:**
- Enhanced Oracle support
- SQL Server specific optimizations
- Additional database vendor support

**Monitoring:**
- Query performance telemetry
- Integration with APM tools (Prometheus, Micrometer)
- Slow query detection and logging

### Version 1.2.0 (Q2 2025)

**Planned Features:**

**Reactive Support:**
- Reactive Streams API integration
- Non-blocking query execution
- Backpressure handling

**Advanced Caching:**
- Distributed cache support (Redis, Hazelcast)
- Cache warming strategies
- Intelligent invalidation

**Security:**
- SQL injection prevention enhancements
- Parameter sanitization
- Audit logging

**Developer Tools:**
- Query builder IDE plugin
- SQL visualization tools
- Migration utilities

### Version 2.0.0 (Q4 2025)

**Major Changes:**

**Architecture:**
- Modular plugin system
- Extensible dialect support
- Custom predicate types

**Breaking Changes:**
- Java 21 minimum requirement
- Package reorganization
- API modernization

**New Capabilities:**
- Native query compilation
- Distributed query execution
- Advanced optimization algorithms

---

## Migration Guides

### Migrating to 1.0.0

#### From Pre-Release Versions

If you used snapshot or pre-release versions:

**1. Update Dependencies:**

```xml
<!-- Maven -->
<dependency>
    <groupId>com.github.query4j</groupId>
    <artifactId>dynamicquerybuilder-core</artifactId>
    <version>1.0.0</version> <!-- Updated from SNAPSHOT -->
</dependency>
```

```groovy
// Gradle
implementation 'com.github.query4j:dynamicquerybuilder-core:1.0.0'
```

**2. Review API Changes:**

No breaking changes from RC versions to 1.0.0.

**3. Configuration Updates:**

Configuration properties remain backward compatible.

#### From Other Libraries

**From Hibernate Criteria API:**

```java
// Before (Hibernate)
CriteriaBuilder cb = entityManager.getCriteriaBuilder();
CriteriaQuery<User> cq = cb.createQuery(User.class);
Root<User> root = cq.from(User.class);
cq.select(root).where(
    cb.and(
        cb.equal(root.get("department"), "Engineering"),
        cb.equal(root.get("active"), true)
    )
);

// After (Query4j)
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .where("department", "Engineering")
    .and()
    .where("active", true);
```

**From MyBatis Dynamic SQL:**

```java
// Before (MyBatis)
SelectStatementProvider selectStatement = select(userTable.allColumns())
    .from(userTable)
    .where(userTable.department, isEqualTo("Engineering"))
    .and(userTable.active, isEqualTo(true))
    .build()
    .render(RenderingStrategies.MYBATIS3);

// After (Query4j)
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .where("department", "Engineering")
    .and()
    .where("active", true);
```

---

## Deprecation Policy

Query4j follows semantic versioning and a clear deprecation policy:

### Deprecation Process

1. **Mark as @Deprecated** with replacement guidance
2. **Maintain for one major version** (e.g., deprecated in 1.x, removed in 2.0)
3. **Document in release notes**
4. **Provide migration path**

### Example Deprecation

```java
/**
 * @deprecated Use {@link #where(String, Object)} instead.
 * This method will be removed in version 2.0.
 */
@Deprecated(since = "1.1.0", forRemoval = true)
public QueryBuilder<T> whereEquals(String field, Object value) {
    return where(field, value);
}
```

---

## Support Policy

### Long-Term Support (LTS)

**Version 1.0.x:**
- Security updates: 2 years (until December 2026)
- Bug fixes: 1 year (until December 2025)
- Feature updates: Active development

### Version Support Timeline

| Version | Release | End of Support | Status |
|---------|---------|----------------|--------|
| 1.0.x | Dec 2024 | Dec 2026 | Active |
| 1.1.x | Q1 2025 | TBD | Planned |
| 1.2.x | Q2 2025 | TBD | Planned |
| 2.0.x | Q4 2025 | TBD | Planned |

---

## Getting Latest Version

### Maven Central

```xml
<dependency>
    <groupId>com.github.query4j</groupId>
    <artifactId>dynamicquerybuilder-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.github.query4j:dynamicquerybuilder-core:1.0.0'
```

### Direct Download

Download JARs from:
- [Maven Central](https://central.sonatype.com/artifact/com.github.query4j/dynamicquerybuilder-core)
- [GitHub Releases](https://github.com/query4j/dynamicquerybuilder/releases)

---

## Changelog Format

We follow [Keep a Changelog](https://keepachangelog.com/) format:

- **Added** - New features
- **Changed** - Changes in existing functionality
- **Deprecated** - Soon-to-be removed features
- **Removed** - Removed features
- **Fixed** - Bug fixes
- **Security** - Security updates

---

## Staying Updated

### Notifications

**Watch Repository:**
- Go to [repository](https://github.com/query4j/dynamicquerybuilder)
- Click "Watch" → "Custom" → "Releases"

**Subscribe to Announcements:**
- [GitHub Discussions - Announcements](https://github.com/query4j/dynamicquerybuilder/discussions/categories/announcements)

### Release Notes

- [GitHub Releases](https://github.com/query4j/dynamicquerybuilder/releases)
- [CHANGELOG.md](https://github.com/query4j/dynamicquerybuilder/blob/master/CHANGELOG.md)

---

**Last Updated:** October 2025  
**Current Version:** 1.0.0
