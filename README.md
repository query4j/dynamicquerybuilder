# Query4j Documentation Index

> ⚠️ **NOTICE**: This project is currently in **FEATURE FREEZE** for v1.0.0 release.  
> See [FEATURE_FREEZE.md](./FEATURE_FREEZE.md) for details. Only bug fixes, documentation, and tests are accepted during this period.

Welcome to the Query4j documentation! This page helps you find the right documentation for your needs.

## 📚 GitHub Wiki

For comprehensive, living documentation with detailed guides, tutorials, and reference material, visit our **[GitHub Wiki](https://github.com/query4j/dynamicquerybuilder/wiki)**:

- **[Home](https://github.com/query4j/dynamicquerybuilder/wiki/Home)** - Project vision, philosophy, and roadmap
- **[Getting Started](https://github.com/query4j/dynamicquerybuilder/wiki/Getting-Started)** - Setup and first query tutorial
- **[Core Module](https://github.com/query4j/dynamicquerybuilder/wiki/Core-Module)** - QueryBuilder API and predicates
- **[Cache Manager](https://github.com/query4j/dynamicquerybuilder/wiki/Cache-Manager)** - Caching strategies and tuning
- **[Optimizer](https://github.com/query4j/dynamicquerybuilder/wiki/Optimizer)** - Query optimization and analysis
- **[Configuration](https://github.com/query4j/dynamicquerybuilder/wiki/Configuration)** - Configuration options and best practices
- **[API Reference](https://github.com/query4j/dynamicquerybuilder/wiki/API-Reference)** - Complete API documentation
- **[Error Handling](https://github.com/query4j/dynamicquerybuilder/wiki/Error-Handling)** - Exception catalog and troubleshooting
- **[Benchmarking](https://github.com/query4j/dynamicquerybuilder/wiki/Benchmarking)** - Performance benchmarks and results
- **[FAQ & Troubleshooting](https://github.com/query4j/dynamicquerybuilder/wiki/FAQ-and-Troubleshooting)** - Common questions and solutions
- **[Contributing](https://github.com/query4j/dynamicquerybuilder/wiki/Contributing)** - Contribution guidelines and workflow

## Getting Started

New to Query4j? Start here:

- **[Quickstart Tutorial](../QUICKSTART.md)** - Learn the basics in 15 minutes
  - Prerequisites and setup
  - Basic query building
  - Filtering, sorting, and pagination
  - Pattern matching and logical operators
  - Troubleshooting guide

## Advanced Topics

Ready to dive deeper? Explore advanced features:

- **[Advanced Usage Tutorial](../ADVANCED.md)** - Master complex queries and optimization
  - Complex query composition
  - JOIN operations and subqueries
  - Aggregations and GROUP BY
  - Query optimization strategies
  - Result caching
  - Asynchronous execution
  - Batch processing
  - Best practices and common pitfalls

## Reference Documentation

Complete API and configuration references:

- **[API Reference Guide](API_GUIDE.md)** - Comprehensive API documentation
  - All QueryBuilder methods
  - Core module APIs
  - Cache module APIs
  - Optimizer module APIs
  - Exception hierarchy
  - Usage examples

- **[Configuration Guide](Configuration.md)** - Configuration options and best practices
  - Configuration sources (YAML, properties, environment)
  - Core module configuration
  - Cache module configuration
  - Optimizer module configuration
  - Environment-specific settings
  - Best practices by environment

## Integration and Workflows

- **[CodeRabbit-Copilot Integration](CODERABBIT_COPILOT_INTEGRATION.md)** - Automated code review workflow
- **[API Documentation Guide](API_DOCUMENTATION_GUIDE.md)** - API documentation standards

## Examples

Real-world usage examples:

- **[Examples Module](../examples/README.md)** - Comprehensive examples including:
  - Spring Boot integration
  - AsyncQueryApp - Concurrent query patterns
  - BatchProcessingApp - Large dataset processing
  - ComplexJoinsApp - Multi-table queries

## Performance

- **[Benchmark Results](../benchmark/README.md)** - Performance benchmarks and analysis
  - Core module benchmarks
  - Optimizer profiling
  - Pagination benchmarks
  - Performance targets and results

## Contributing

- **[Contributing Guide](../CONTRIBUTING.md)** - How to contribute to Query4j

## Quick Links by Task

### I want to...

**Build a simple query**
→ Start with [Quickstart Tutorial](../QUICKSTART.md#your-first-query)

**Add filters and conditions**
→ See [Adding Filters](../QUICKSTART.md#adding-filters) in Quickstart

**Implement pagination**
→ Read [Pagination](../QUICKSTART.md#pagination) section

**Join multiple tables**
→ Check [JOIN Operations](../ADVANCED.md#join-operations) in Advanced guide

**Optimize query performance**
→ Review [Query Optimization](../ADVANCED.md#query-optimization) and [Performance Tuning](../ADVANCED.md#performance-tuning)

**Cache query results**
→ Learn about [Result Caching](../ADVANCED.md#result-caching)

**Process large datasets**
→ Read [Batch Processing](../ADVANCED.md#batch-processing) guide

**Execute queries asynchronously**
→ See [Asynchronous Execution](../ADVANCED.md#asynchronous-execution)

**Configure Query4j**
→ Read the [Configuration Guide](Configuration.md)

**Understand all API methods**
→ Browse the [API Reference Guide](API_GUIDE.md)

**See working examples**
→ Explore the [Examples Module](../examples/README.md)

**Troubleshoot issues**
→ Check [Troubleshooting](../QUICKSTART.md#troubleshooting) and [Common Pitfalls](../ADVANCED.md#common-pitfalls)

## Wiki Publishing

The GitHub Wiki is automatically published from the `/wiki` directory:

- **Automatic Publishing**: Wiki content is automatically synced to GitHub Wiki when changes are pushed to the `master` branch
- **Manual Publishing**: Run `./publish-wiki.sh` to manually publish wiki content
- **Wiki Maintenance**: See [wiki/MAINTENANCE.md](wiki/MAINTENANCE.md) for maintenance guidelines

For details on wiki setup and publishing, see:
- [wiki/README.md](wiki/README.md) - Wiki overview and structure
- [wiki/SETUP.md](wiki/SETUP.md) - Quick setup guide

## Support

- **Issues**: [GitHub Issues](https://github.com/query4j/dynamicquerybuilder/issues)
- **Discussions**: [GitHub Discussions](https://github.com/query4j/dynamicquerybuilder/discussions)
- **Wiki**: [GitHub Wiki](https://github.com/query4j/dynamicquerybuilder/wiki)

---

**Can't find what you're looking for?** [Open an issue](https://github.com/query4j/dynamicquerybuilder/issues) or start a [discussion](https://github.com/query4j/dynamicquerybuilder/discussions).
