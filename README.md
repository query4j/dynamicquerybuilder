# Query4j Dynamic Query Builder

A high-performance, thread-safe Java library for building dynamic SQL queries with fluent API design and comprehensive predicate support.

## Features

- **Fluent Builder API**: Intuitive, chainable methods for constructing complex queries
- **Thread-Safe**: Immutable builder pattern with copy-on-write semantics
- **SQL Dialect Support**: Compatible with H2, PostgreSQL, MySQL, and other major databases
- **Performance Optimized**: Minimal object allocation and efficient string building
- **Comprehensive Testing**: 95%+ test coverage with unit and property-based tests
- **Type Safety**: Strong typing with compile-time validation

## Quick Start

```java
DynamicQueryBuilder builder = new DynamicQueryBuilder("users")
    .where("status", Operator.EQUALS, "active")
    .and()
    .whereIn("role", Arrays.asList("admin", "user"))
    .or()
    .openGroup()
        .where("created_date", Operator.GREATER_THAN, LocalDate.now().minusDays(30))
        .and()
        .whereIsNotNull("email")
    .closeGroup()
    .orderBy("created_date", SortOrder.DESC)
    .limit(50);

String sql = builder.toSQL();
Map<String, Object> parameters = builder.getParameters();
```

## Documentation

- [Contributing Guide](CONTRIBUTING.md) - How to contribute to the project
- [CodeRabbit-Copilot Integration](docs/CODERABBIT_COPILOT_INTEGRATION.md) - Automated code review workflow
- [API Documentation](docs/) - Detailed API reference

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