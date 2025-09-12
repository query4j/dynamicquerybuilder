# Query4j Dynamic Query Builder

A high-performance, thread-safe Java library for building dynamic SQL queries with fluent API design and comprehensive predicate support.

## Features

- **Fluent Builder API**: Intuitive, chainable methods for constructing complex queries
- **Thread-Safe**: Immutable builder pattern with copy-on-write semantics
- **SQL Dialect Support**: Compatible with H2, PostgreSQL, MySQL, and other major databases
- **Performance Optimized**: Minimal object allocation and efficient string building
- **Comprehensive Testing**: 95%+ test coverage with unit and property-based tests
- **Type Safety**: Strong typing with compile-time validation

## Requirements

- **Java 17** or higher
- **Maven 3.6+** for building from source

## Quick Start

### Basic Query Construction

```java
// Simple equality query
List<User> activeUsers = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .findAll();

// Complex query with multiple conditions
List<User> users = QueryBuilder.forEntity(User.class)
    .where("department", "Engineering")
    .and()
    .whereIn("role", Arrays.asList("admin", "developer"))
    .or()
    .openGroup()
        .where("joinDate", ">", LocalDate.now().minusDays(30))
        .and()
        .whereIsNotNull("email")
    .closeGroup()
    .orderByDescending("joinDate")
    .limit(50)
    .findAll();
```

### Pagination and Caching

```java
// Paginated results with caching
Page<User> userPage = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .orderBy("lastName")
    .page(0, 20)
    .cached(3600) // Cache for 1 hour
    .findPage();

System.out.println("Total users: " + userPage.getTotalElements());
System.out.println("Current page: " + userPage.getNumber());
```

## Performance Benchmarks

Query4j includes comprehensive JMH performance benchmarks to validate performance targets:

| Scenario | Target | Actual Performance |
|----------|--------|-------------------|
| Basic Query | < 1 ms | ~1.7 μs ✅ |
| Moderate Query | < 2 ms | ~6.7 μs ✅ |
| Complex Query | < 5 ms | ~17.1 μs ✅ |

### Running Benchmarks

```bash
# Build and run all benchmarks
mvn clean install -Pbenchmark

# Or run specific benchmarks manually
cd benchmark
mvn clean package
java -jar target/benchmarks.jar
```

See [benchmark/README.md](benchmark/README.md) for detailed benchmark results and analysis.

### Asynchronous Execution

```java
// Async query execution  
CompletableFuture<List<User>> futureUsers = QueryBuilder.forEntity(User.class)
    .where("active", true)
    .findAllAsync();

futureUsers.thenAccept(users -> {
    users.forEach(System.out::println);
});
```

## Documentation

- **[API Reference Guide](docs/API_GUIDE.md)** - Comprehensive API documentation with examples
- **[JavaDoc API](https://query4j.github.io/dynamicquerybuilder/)** - Generated API documentation
- [Contributing Guide](CONTRIBUTING.md) - How to contribute to the project  
- [CodeRabbit-Copilot Integration](docs/CODERABBIT_COPILOT_INTEGRATION.md) - Automated code review workflow

### API Entry Points

| Entry Point | Module | Description | Example |
|-------------|---------|-------------|---------|
| `QueryBuilder.forEntity()` | Core | Primary fluent query builder | `QueryBuilder.forEntity(User.class).where("name", "John").findAll()` |
| `DynamicQuery` | Core | Reusable compiled query | `query.execute()` |
| `Page<T>` | Core | Paginated results container | `page.getContent()` |
| `QueryStats` | Core | Execution metrics and performance data | `stats.getExecutionTimeMs()` |

## Automated Code Quality

This project includes advanced automation for code quality:

### CodeRabbit AI Integration
- **Automated Reviews**: Every PR gets intelligent code review feedback
- **Safe Auto-fixes**: Minor issues are automatically corrected
- **Quality Gates**: Maintains high code quality standards

### GitHub Copilot Integration  
- **Feedback Processing**: Automatically detects and processes CodeRabbit suggestions
- **Actionable Insights**: Provides clear next steps for developers
- **Issue Tracking**: Creates issues for complex improvements

See the [integration guide](docs/CODERABBIT_COPILOT_INTEGRATION.md) for detailed workflow information.

## Building and Testing

```bash
# Build the project
mvn clean compile

# Run tests
mvn test

# Generate coverage report
mvn verify

# Build with full integration tests
mvn clean verify
```

## Architecture

The library is organized into focused modules:

- **`core/`** - Core query building functionality
- **`cache/`** - Query caching and optimization
- **`optimizer/`** - SQL query optimization
- **`examples/`** - Usage examples and tutorials
- **`test/`** - Integration tests and benchmarks

## Performance

Designed for high-throughput applications:
- Sub-millisecond query construction
- Minimal memory allocation  
- Thread-safe concurrent usage
- Efficient parameter handling

## Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details on:

- Code style and standards
- Testing requirements  
- Pull request process
- Automated review workflow

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

- **Issues**: [GitHub Issues](https://github.com/query4j/dynamicquerybuilder/issues)
- **Discussions**: [GitHub Discussions](https://github.com/query4j/dynamicquerybuilder/discussions)
- **Documentation**: [Project Wiki](https://github.com/query4j/dynamicquerybuilder/wiki)

---

Built with ❤️ for the Java community. Empowering developers to build robust, scalable data applications.