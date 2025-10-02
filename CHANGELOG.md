# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2024-10-01

### 🎉 First Stable Release

Query4j Dynamic Query Builder reaches production-ready status with comprehensive features, excellent performance, and 95%+ test coverage.

### ✨ Features

#### Core Module
- **Fluent Builder API**: Intuitive, chainable methods for constructing complex SQL queries
- **Thread-Safe Design**: Immutable builder pattern with copy-on-write semantics
- **Comprehensive Predicate Support**: 
  - Simple predicates (=, !=, <, >, <=, >=)
  - LIKE pattern matching with wildcards
  - IN and NOT IN with collections
  - BETWEEN for range queries
  - IS NULL and IS NOT NULL checks
  - Logical operators (AND, OR, NOT) with grouping
- **Subquery Support**: 
  - EXISTS and NOT EXISTS subqueries
  - IN and NOT IN subqueries
  - Proper correlation and parameter handling
- **Custom Function Support**: Database-specific functions with type-safe parameter handling
- **Query Hints**: Performance optimization with database hints
- **Join Operations**: INNER, LEFT, RIGHT, and FETCH joins with proper alias handling
- **Aggregations**: COUNT, SUM, AVG, MIN, MAX with GROUP BY and HAVING
- **Pagination**: Page-based and offset-based pagination with configurable page sizes
- **Sorting**: Multi-column sorting with ASC/DESC and null handling
- **Query Statistics**: Comprehensive execution tracking and performance metrics
- **Reusable Queries**: Build once, execute multiple times with DynamicQuery
- **Type Safety**: Strong typing with compile-time validation
- **Configuration System**: Flexible configuration with profiles and environment overrides

#### Cache Module
- **Query Caching**: In-memory LRU cache for query results
- **Region-Based Caching**: Organize cache entries by logical regions
- **TTL Support**: Configurable time-to-live for cached entries
- **Cache Statistics**: Hit/miss ratios, eviction tracking, and performance metrics
- **Thread-Safe Operations**: Concurrent access with minimal lock contention
- **Automatic Maintenance**: Background cleanup of expired entries
- **Integration**: Seamless integration with QueryBuilder API

#### Optimizer Module
- **Query Analysis**: Intelligent analysis of query structure and predicates
- **Index Suggestions**: Automated recommendations for database indexes
- **Predicate Pushdown**: Optimization suggestions for filtering operations
- **Join Reordering**: Analysis of join order for performance improvement
- **Performance Metrics**: Detailed analysis timing and impact assessment
- **Configurable Thresholds**: Customize optimization rules and priorities
- **Multiple Priorities**: Critical, High, Medium, Low prioritization of suggestions

### 🚀 Performance

- **Basic Queries**: ~1.6 μs average execution time
- **Moderate Complexity**: ~6.7 μs average execution time
- **Complex Queries**: ~16.0 μs average execution time
- **Optimizer Analysis**: < 0.5 μs negligible overhead
- **vs. JPA Criteria**: +20% overhead (competitive performance)
- **Memory Efficient**: Minimal object allocation during query building
- **Thread-Safe**: Zero lock contention for read operations

### 📚 Documentation

- **Comprehensive README**: Quick start, features, and examples
- **API Guide**: Complete API reference with usage patterns
- **Quickstart Guide**: Step-by-step tutorials for common scenarios
- **Advanced Guide**: Complex query patterns and optimization techniques
- **Configuration Guide**: Environment-specific configuration and profiles
- **FAQ and Troubleshooting**: Common issues and solutions
- **Benchmark Results**: Detailed performance analysis and comparisons
- **JavaDoc**: Complete API documentation with examples
- **Module READMEs**: Dedicated documentation for each module
- **Contributing Guide**: Development standards and contribution process

### 🧪 Testing

- **95%+ Code Coverage**: Comprehensive test coverage across all modules
- **Unit Tests**: Extensive unit testing with JUnit 5
- **Property-Based Tests**: Generative testing with jqwik for edge cases
- **Integration Tests**: End-to-end testing with real database scenarios
- **Performance Tests**: JMH benchmarks for critical code paths
- **Correctness Tests**: Validation of SQL generation and parameter mapping
- **Thread-Safety Tests**: Concurrent access testing
- **Error Handling Tests**: Comprehensive exception scenario coverage

### 🛠️ Build & Infrastructure

- **Java 17**: Modern Java features and performance improvements
- **Gradle 8.5**: Fast, reliable builds with incremental compilation
- **Multi-Module**: Clean separation of core, cache, optimizer, and examples
- **CI/CD**: Automated testing and coverage reporting with GitHub Actions
- **Codecov Integration**: Automatic coverage tracking and reporting
- **JaCoCo**: Code coverage measurement and verification
- **Dependency Management**: Centralized version management for all modules

### 🎯 SQL Dialect Support

- **H2 Database**: Full support for in-memory and file-based databases
- **PostgreSQL**: Complete feature support including advanced functions
- **MySQL**: Comprehensive compatibility with MySQL 5.7+
- **MariaDB**: Full support with MySQL dialect
- **Other JDBC Databases**: Compatible with standard SQL databases

### 📦 Modules

- **core**: Core query building functionality (required)
- **cache**: Query result caching (optional)
- **optimizer**: Query analysis and optimization (optional)
- **examples**: Sample applications and integration patterns
- **benchmark**: JMH performance benchmarks
- **test**: Common test utilities and fixtures

### 🔧 Dependencies

- **Lombok**: Immutable data classes and builders
- **JUnit 5**: Modern testing framework
- **jqwik**: Property-based testing
- **Mockito**: Test mocking
- **AssertJ**: Fluent assertions
- **JMH**: Micro-benchmarking
- **Spring Boot**: Example applications (examples module only)

### 🎓 Educational Resources

- **Interactive Demo**: Console-based demonstration application
- **Consumer Applications**: Production-ready example applications
  - REST API with caching
  - Batch processing
  - Report generation
  - Data migration
- **Monitoring Dashboard**: Query statistics and performance tracking
- **Best Practices**: Production deployment patterns and recommendations

### 🔒 Security

- **SQL Injection Prevention**: Parameterized queries with proper escaping
- **Input Validation**: Comprehensive validation of field names and operators
- **Thread Safety**: Immutable design prevents concurrent modification issues
- **No Reflection**: Type-safe API without runtime reflection overhead

### 📄 License

- **Apache License 2.0**: Open source, commercial-friendly license

### 🙏 Acknowledgments

Special thanks to all contributors who helped make this release possible:
- Code contributors for features, bug fixes, and improvements
- Documentation contributors for guides and examples
- Community members for feedback and bug reports
- CodeRabbit AI for automated code review assistance

---

## Release Assets

The following artifacts are available for this release:

- **JAR Files**: Core, Cache, and Optimizer modules
- **Source JARs**: Complete source code for all modules
- **JavaDoc JARs**: API documentation for all modules
- **Benchmark JAR**: JMH benchmarks for performance testing

## Maven Coordinates

```xml
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
```

## Gradle Coordinates

```groovy
// Core Module (Required)
implementation 'com.github.query4j:dynamicquerybuilder-core:1.0.0'

// Cache Module (Optional)
implementation 'com.github.query4j:dynamicquerybuilder-cache:1.0.0'

// Optimizer Module (Optional)
implementation 'com.github.query4j:dynamicquerybuilder-optimizer:1.0.0'
```

---

## Known Issues

No critical issues are known at this time. For bug reports and feature requests, please visit:
- [GitHub Issues](https://github.com/query4j/dynamicquerybuilder/issues)

## What's Next?

See our [Roadmap](docs/ROADMAP.md) for planned features in future releases:
- Version 1.1.x: Advanced caching strategies and distributed cache support
- Version 1.2.x: Query rewrite engine and advanced optimization
- Version 2.0.x: Multi-database transaction support

---

For complete release details, see [RELEASE_NOTES_v1.0.0.md](RELEASE_NOTES_v1.0.0.md)

[1.0.0]: https://github.com/query4j/dynamicquerybuilder/releases/tag/v1.0.0
# Changelog

All notable changes to the Query4j Dynamic Query Builder project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Comprehensive CI/CD pipeline with GitHub Actions
- Automated release workflow with artifact publishing
- Security scanning with CodeQL and dependency checks
- Automated documentation publishing to GitHub Pages
- Performance benchmark automation
- Multi-JDK testing (Java 17 and 21)
- GitHub Packages publishing support
- Comprehensive pipeline documentation

### Changed
- Enhanced CI workflow with parallel job execution
- Improved test coverage reporting
- Updated build configuration for artifact publishing

### Security
- Added TruffleHog secret scanning
- Implemented CodeQL analysis
- Added SBOM generation support
- License compliance checking

## [1.0.0-SNAPSHOT] - Unreleased

### Added
- Core dynamic query builder functionality
- Predicate system with fluent API
- Cache module for query caching
- Optimizer module for query optimization
- Comprehensive test suite with 574+ tests
- JavaDoc documentation
- Benchmark suite using JMH
- Configuration system with multiple sources
- Examples and integration tests
- Spring Boot integration support

### Documentation
- Quick start guide
- API documentation guide
- Configuration documentation
- FAQ and troubleshooting guide
- Advanced topics guide
- Benchmark documentation
- CI/CD pipeline documentation

---

## Release Types

- **Major** (x.0.0): Breaking changes, major new features
- **Minor** (1.x.0): New features, backward compatible
- **Patch** (1.0.x): Bug fixes, minor improvements

## Version History Format

Each version entry should include:

- **Date**: Release date in YYYY-MM-DD format
- **Added**: New features
- **Changed**: Changes in existing functionality
- **Deprecated**: Soon-to-be removed features
- **Removed**: Removed features
- **Fixed**: Bug fixes
- **Security**: Security fixes and improvements

---

For detailed commit history, see [GitHub Releases](https://github.com/query4j/dynamicquerybuilder/releases).
