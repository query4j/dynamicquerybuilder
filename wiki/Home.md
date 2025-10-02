# Query4j Dynamic Query Builder Wiki

Welcome to the official Query4j Dynamic Query Builder Wiki! This is your comprehensive guide to understanding, using, and contributing to Query4j—a high-performance, thread-safe Java library for building dynamic SQL queries.

## Vision and Philosophy

Query4j embodies the principles of a **smart, algorithmically-inclined engineering** approach:

- **Robustness & Predictability**: Every construct is designed to be error-free, scalable, and maintainable
- **Performance-First**: Optimized for high-throughput, data-intensive applications
- **Immutability by Design**: Thread-safe, copy-on-write semantics throughout
- **Developer Experience**: Fluent, intuitive API with comprehensive error handling
- **Production-Ready**: Battle-tested patterns for enterprise-scale deployments

### Core Principles

1. **Immutability is the contract** - All builder operations return new instances
2. **Composability over duplication** - Generic algorithms, clear interfaces, no special cases
3. **Benchmark-ready** - Minimal allocation overhead, efficient algorithms, parallel-friendly
4. **SQL correctness** - Valid queries for major SQL dialects (H2, PostgreSQL, MySQL)
5. **Type safety** - Strong typing with compile-time validation

## Roadmap

### Current Version: 1.0.0

**Completed Features:**
- ✅ Core query builder with fluent API
- ✅ Comprehensive predicate support (Simple, In, Between, Like, Null)
- ✅ Logical operators (AND, OR, NOT) with grouping
- ✅ JOIN operations (INNER, LEFT, RIGHT, FETCH)
- ✅ Aggregations (COUNT, SUM, AVG, MIN, MAX)
- ✅ GROUP BY and HAVING clauses
- ✅ Sorting and pagination
- ✅ Query caching module with LRU/LFU strategies
- ✅ Query optimizer with index suggestions
- ✅ 95%+ test coverage with property-based testing
- ✅ Comprehensive documentation and examples

### Version 1.1.0 (Planned)

**In Progress:**
- 🔄 Enhanced optimizer with cost-based analysis
- 🔄 Batch query operations API
- 🔄 Additional SQL dialect support (Oracle, SQL Server)
- 🔄 Performance monitoring and telemetry hooks

**Future:**
- 📋 Subquery support
- 📋 Window functions
- 📋 Common Table Expressions (CTEs)
- 📋 Distributed query execution
- 📋 Native async/reactive API

### Long-Term Vision

Query4j aims to become the de facto standard for dynamic query building in Java, providing:
- Best-in-class performance for high-volume scenarios
- Enterprise-grade reliability and maintainability
- Comprehensive ecosystem integration (Spring, Quarkus, Micronaut)
- Advanced optimization capabilities rivaling ORM frameworks
- First-class support for modern data platforms

## Quick Navigation

### Getting Started
- **[Getting Started](Getting-Started)** - Setup, installation, and your first query
- **[Quickstart Tutorial](https://github.com/query4j/dynamicquerybuilder/blob/master/QUICKSTART.md)** - Learn the basics in 15 minutes
- **[Advanced Tutorial](https://github.com/query4j/dynamicquerybuilder/blob/master/ADVANCED.md)** - Master complex queries

### Core Documentation
- **[Core Module](Core-Module)** - QueryBuilder API and predicate types
- **[Cache Manager](Cache-Manager)** - Query result caching strategies
- **[Optimizer](Optimizer)** - Query optimization and performance tuning
- **[Configuration](Configuration)** - Configuration options and best practices

### Reference
- **[API Reference](API-Reference)** - Complete API documentation
- **[Error Handling](Error-Handling)** - Exception catalog and troubleshooting
- **[Benchmarking](Benchmarking)** - Performance benchmarks and analysis

### Community
- **[FAQ and Troubleshooting](FAQ-and-Troubleshooting)** - Common questions and solutions
- **[Contributing](Contributing)** - Contribution guidelines and workflow
- **[Release Notes](Release-Notes)** - Version history and changelog

## Project Structure

Query4j is organized into modular components:

```
dynamicquerybuilder/
├── core/           # Core query builder functionality
├── cache/          # Query result caching
├── optimizer/      # Query optimization engine
├── examples/       # Sample applications and integrations
├── benchmark/      # Performance benchmarks (JMH)
├── docs/           # Comprehensive documentation
└── test/           # Integration tests
```

## Key Features at a Glance

### Fluent Builder API
```java
List<User> users = QueryBuilder.forEntity(User.class)
    .where("department", "Engineering")
    .and()
    .whereIn("role", Arrays.asList("admin", "developer"))
    .orderBy("lastName")
    .limit(50)
    .findAll();
```

### Thread-Safe Immutability
```java
QueryBuilder<User> base = QueryBuilder.forEntity(User.class)
    .where("active", true);

// Each operation returns a new instance
QueryBuilder<User> admins = base.and().where("role", "admin");
QueryBuilder<User> devs = base.and().where("role", "developer");
// 'base' remains unchanged
```

### Performance Optimized
- Sub-millisecond query construction for typical queries
- Minimal object allocation through efficient builder patterns
- Parallel-friendly stateless design
- Benchmark-validated performance targets

### Comprehensive Error Handling
```java
try {
    QueryBuilder.forEntity(User.class)
        .where("", "value"); // Invalid field name
} catch (QueryBuildException e) {
    // Clear, actionable error message with context
    System.err.println(e.getMessage());
}
```

## Support and Community

### Getting Help
- **[GitHub Issues](https://github.com/query4j/dynamicquerybuilder/issues)** - Bug reports and feature requests
- **[GitHub Discussions](https://github.com/query4j/dynamicquerybuilder/discussions)** - Questions and community support
- **[Stack Overflow](https://stackoverflow.com/questions/tagged/query4j)** - Tag: `query4j`

### Contributing
We welcome contributions! See our **[Contributing Guide](Contributing)** for:
- Code contribution workflow
- Documentation improvements
- Testing requirements
- Code review process

### Stay Updated
- **[Release Notes](Release-Notes)** - Track new features and changes
- **[GitHub Releases](https://github.com/query4j/dynamicquerybuilder/releases)** - Download latest versions
- **Watch** the repository for updates

## License

Query4j is open source software licensed under the [Apache License 2.0](https://github.com/query4j/dynamicquerybuilder/blob/master/LICENSE).

---

**Last Updated:** October 2025  
**Current Version:** 1.0.0  
**Maintained by:** Query4j Development Team

For questions about this wiki, please [open a discussion](https://github.com/query4j/dynamicquerybuilder/discussions).
