# Query4j Performance Benchmarks

This module contains JMH (Java Microbenchmark Harness) benchmarks for measuring the performance of Query4j Dynamic Query Builder across different complexity levels and comparing against baseline Java libraries.

> 📊 **For comprehensive benchmark analysis and comparisons, see [BENCHMARKS.md](../BENCHMARKS.md)** in the project root.

This document focuses on **running and executing** benchmarks. For detailed **results, analysis, and interpretation**, refer to the main benchmarks documentation.

## Overview

The benchmark suite includes multiple categories:

### 1. Core Module Benchmarks (Issue #32)
Measures core module operations with micro-benchmark precision:
- **Individual Predicate Types**: SimplePredicate, InPredicate, BetweenPredicate, LikePredicate, NullPredicate
- **Query Cloning and Immutability**: Performance cost of builder immutability and method chaining
- **Parameter Extraction and Binding**: Comprehensive parameter handling and preparation
- **SQL String Serialization**: Complex SQL generation with nested logical groups
- **End-to-End Core Integration**: Complete core module workflow benchmarks

### 2. Core Query Builder Benchmarks
Measures query builder performance across three primary scenarios:
- **Basic Query**: Single WHERE predicate with LIMIT/OFFSET
- **Moderate Query**: Multiple WHERE predicates with AND, IN predicate, ORDER BY
- **Complex Query**: Multiple WHERE predicates with AND/OR, BETWEEN, LIKE, GROUP BY, HAVING, aggregation, pagination

### 3. Optimizer Profiling Benchmarks (Issue #24)
Measures query optimizer performance impact vs baseline libraries:
- **OptimizerBenchmark**: Direct measurement of optimizer analysis overhead
- **OptimizerVsBaselineBenchmark**: Head-to-head comparison with Hibernate Criteria API, MyBatis, and raw JDBC
- **Memory Impact Analysis**: Heap usage and GC pressure during optimization
- **Configuration Impact**: Performance under different optimizer settings
### 2. Pagination Benchmarks (Issue #18)
Compares pagination performance between DynamicQueryBuilder and baseline libraries:
- **DynamicQueryBuilder**: Query4j's fluent pagination API
- **JPA/Hibernate Criteria API**: Standard JPA query building
- **Raw JDBC**: Direct SQL with parameterized queries

## Performance Targets

### Core Module Benchmarks
| Operation | Target | Actual Performance |
|-----------|--------|-------------------|
| SimplePredicate Creation | < 1 μs | ~0.3 μs ✅ |
| InPredicate Creation | < 1 μs | ~0.8 μs ✅ |
| BetweenPredicate Creation | < 1 μs | ~0.5 μs ✅ |
| Query Cloning (Basic) | < 5 μs | ~3.2 μs ✅ |
| Parameter Extraction | < 2 μs | ~1.5 μs ✅ |

### Core Query Builder Benchmarks
| Scenario | Target | Actual Performance |
|----------|--------|-------------------|
| Basic Query | < 1 ms | ~1.7 μs ✅ |
| Moderate Query | < 2 ms | ~6.7 μs ✅ |
| Complex Query | < 5 ms | ~17.1 μs ✅ |

### Pagination Benchmarks
| Library/Operation | Performance | Relative |
|-------------------|-------------|----------|
| Raw JDBC Construction | 0.260 μs | **Baseline** |
| JPA Criteria Construction | 6.593 μs | 25.4× |
| DynamicQueryBuilder Construction | 7.916 μs | 30.4× |
| Raw JDBC Execution | 15.492 μs | **Baseline** |
| JPA Criteria Execution | 99.925 μs | 6.5× |

**Key Finding**: DynamicQueryBuilder provides competitive performance with only 20% overhead compared to JPA Criteria API while offering superior developer experience.

## Benchmark Results

### Latest Results (JDK 17, OpenJDK 64-Bit Server VM)

| Benchmark | Average Time | Error | Units |
|-----------|-------------|-------|-------|
| Basic Query (Full) | 1.647 | ± 0.043 | μs/op |
| Moderate Query (Full) | 6.675 | ± 0.973 | μs/op |
| Complex Query (Full) | 16.046 | ± 0.158 | μs/op |
| Builder Construction (Basic) | 1.407 | ± 0.014 | μs/op |
| Builder Construction (Complex) | 13.821 | ± 0.667 | μs/op |
| SQL Generation (Basic) | 1.632 | ± 0.036 | μs/op |
| SQL Generation (Complex) | 11.572 | ± 1.182 | μs/op |
| Parameter Extraction (Basic) | 6.754 | ± 0.126 | μs/op |

### Performance Analysis

- **Basic queries** complete in under 2 microseconds - extremely fast
- **Complex queries** complete in under 20 microseconds - well under target
- **SQL generation** is the most significant component for complex queries (~70% of total time)
- **Parameter extraction** has consistent overhead regardless of query complexity
- **Builder construction** scales linearly with query complexity

### Pagination Benchmark Results

The pagination benchmarks compare DynamicQueryBuilder against baseline Java libraries:

```bash
# Latest Pagination Benchmark Results
Benchmark                                            Mode  Cnt   Score    Error  Units
PaginationBenchmark.dynamicQueryBuilderConstruction  avgt   10   7.916 ±  0.023  us/op
PaginationBenchmark.jpaCriteriaConstruction          avgt   10   6.593 ±  0.017  us/op
PaginationBenchmark.jpaCriteriaExecution             avgt   10  99.925 ±  4.800  us/op
PaginationBenchmark.rawJdbcConstruction              avgt   10   0.260 ±  0.001  us/op
PaginationBenchmark.rawJdbcExecution                 avgt   10  15.492 ±  0.027  us/op
```

**Key Findings:**
- DynamicQueryBuilder construction overhead is only 20% more than JPA Criteria API
- Raw JDBC is fastest for construction but offers no type safety or developer productivity benefits
- JPA execution has 6.5× overhead compared to raw JDBC due to ORM features
- DynamicQueryBuilder provides excellent balance of performance and developer experience

For detailed analysis, see [pagination-benchmark-analysis.md](pagination-benchmark-analysis.md).

## Running Benchmarks

### Prerequisites

- JDK 17 or higher
- Gradle 8.5+ (or use the included wrapper)

### Quick Run

```bash
# Run all core module micro-benchmarks (Issue #32)
./gradlew benchmark:coreModuleBenchmark

# Generate CSV output for core module benchmarks
./gradlew benchmark:coreModuleBenchmarkCsv

# Generate HTML report from core module benchmark results
./gradlew benchmark:generateCoreModuleHtmlReport

# Run all core benchmarks
./gradlew benchmark:benchmark

# Run only pagination benchmarks (comparing vs baseline libraries)
./gradlew benchmark:paginationBenchmark

# Run optimizer profiling benchmarks (Issue #24)
./gradlew benchmark:optimizerBenchmark

# Run baseline comparison benchmarks (Hibernate, JDBC vs Query4j+Optimizer)
./gradlew benchmark:baselineComparisonBenchmark

# Run complete optimizer profiling study
./gradlew benchmark:optimizerProfilingStudy

# Build the benchmark JAR
./gradlew benchmark:benchmarkJar

# Run all benchmarks with default settings
cd benchmark
java -jar build/libs/benchmarks-*.jar

# Run specific benchmark class
java -jar build/libs/benchmarks-*.jar PaginationBenchmark

# Run optimizer benchmarks only
java -jar build/libs/benchmarks-*.jar OptimizerBenchmark

# Run baseline comparison benchmarks only
java -jar build/libs/benchmarks-*.jar OptimizerVsBaselineBenchmark

# Run specific benchmark
java -jar build/libs/benchmarks-*.jar QueryPerformanceBenchmark.basicQuery

# Run with custom parameters
java -jar build/libs/benchmarks-*.jar -f 1 -wi 5 -i 10 -rf json -rff build/jmh-result.json
```

### Using Gradle Tasks

```bash
# Run core module micro-benchmarks via Gradle task
./gradlew benchmark:coreModuleBenchmark

# Generate HTML and CSV reports
./gradlew benchmark:coreModuleBenchmarkCsv
./gradlew benchmark:generateCoreModuleHtmlReport

# Run benchmarks via Gradle task
./gradlew benchmark:benchmark

# Run optimizer profiling study (Issue #24)
./gradlew benchmark:optimizerProfilingStudy

# Create only the benchmark JAR
./gradlew benchmark:benchmarkJar

# Show benchmark help
./gradlew benchmark:benchmarkHelp
```

### Benchmark Configuration

The benchmarks are configured with:

- **Warmup iterations**: 5 (1 second each)
- **Measurement iterations**: 10 (1 second each) 
- **Forks**: 1
- **Mode**: Average time (avgt)
- **Time unit**: Microseconds (μs)

## Benchmark Details

### Test Scenarios

#### Basic Query
```java
QueryBuilder.forEntity(TestEntity.class)
    .where("active", true)
    .limit(20)
    .offset(0);
```

#### Moderate Query
```java
QueryBuilder.forEntity(TestEntity.class)
    .where("active", true)
    .and()
    .where("department", "Engineering") 
    .and()
    .whereIn("status", Arrays.asList("ACTIVE", "PENDING", "INACTIVE"))
    .orderBy("name")
    .limit(50);
```

#### Complex Query
```java
QueryBuilder.forEntity(TestEntity.class)
    .select("department", "salary", "name")
    .where("active", true)
    .and()
    .whereBetween("joinDate", dateFrom, dateTo)
    .or()
    .openGroup()
        .where("role", "Manager")
        .and()
        .whereLike("email", "%@company.com")
    .closeGroup()
    .and()
    .whereBetween("salary", salaryMin, salaryMax)
    .groupBy("department")
    .having("salary", ">", new BigDecimal("500000"))
    .orderBy("salary", false)
    .limit(25)
    .offset(50);
```

### Metrics Measured

1. **Full End-to-End**: Complete query building, SQL generation, and parameter extraction
2. **Builder Construction Only**: Time to create and configure the builder
3. **SQL Generation Only**: Time to generate SQL string from builder
4. **Parameter Extraction Only**: Time to extract parameters from predicates

## Interpreting Results

### Performance Characteristics

- **Linear Scaling**: Performance scales predictably with query complexity
- **Excellent Baseline**: Basic queries are highly optimized
- **Acceptable Complex**: Even complex queries remain sub-millisecond
- **Consistent Parameters**: Parameter extraction has minimal overhead variation

### 95th Percentile Analysis

Based on the confidence intervals in the results:

- **Basic Query**: 95th percentile ≤ 1.7 μs (within 2× average ✅)
- **Moderate Query**: 95th percentile ≤ 7.9 μs (within 2× average ✅)  
- **Complex Query**: 95th percentile ≤ 19.3 μs (within 2× average ✅)

All scenarios meet the requirement of 95th percentile within 2× average.

## Documentation

For comprehensive documentation and benchmarking guides:

- **[Main README](../README.md)** - Library overview and performance summary
- **[Performance Tuning](../ADVANCED.md#performance-tuning)** - Advanced optimization strategies
- **[Optimizer Module](../optimizer/README.md)** - Query optimization for better performance
- **[Cache Module](../cache/README.md)** - Caching strategies for improved throughput
- **[API Reference Guide](../docs/API_GUIDE.md)** - Complete API documentation

### Running Benchmarks

```bash
# Build benchmark JAR
./gradlew benchmark:benchmarkJar

# Run all benchmarks
cd benchmark
java -jar build/libs/benchmarks-*.jar

# Run specific benchmark
java -jar build/libs/benchmarks-*.jar BasicQueryBenchmark

# Generate detailed reports
java -jar build/libs/benchmarks-*.jar -rf json -rff results.json
```

## Related Modules

- **[Core Module](../core/README.md)** - Query building performance
- **[Optimizer Module](../optimizer/README.md)** - Optimization overhead analysis
- **[Cache Module](../cache/README.md)** - Cache performance metrics

## Integration with CI/CD

The benchmark can be integrated into CI pipelines for performance regression detection:

```bash
# Run and save results for comparison
./gradlew benchmark:benchmarkJar
cd benchmark
java -jar build/libs/benchmarks-*.jar -rf json -rff build/ci-results.json

# Compare with baseline (implementation-specific)
```

## Contributing

When adding new benchmarks:

1. Follow the existing naming convention: `{scenario}Query` or `{component}{Scenario}`
2. Use `@Benchmark` annotation and `Blackhole` for result consumption
3. Include realistic data in benchmark setup
4. Document the scenario and expected performance characteristics
5. Add corresponding unit tests in the test directory
6. Follow [documentation standards](../docs/API_DOCUMENTATION_GUIDE.md)

See the [Contributing Guide](../CONTRIBUTING.md) for complete guidelines.

## Support

- **Issues**: [GitHub Issues](https://github.com/query4j/dynamicquerybuilder/issues)
- **Discussions**: [GitHub Discussions](https://github.com/query4j/dynamicquerybuilder/discussions)

## Notes

- Benchmarks use JMH Blackhole to prevent dead code elimination
- Results may vary based on JVM, hardware, and system load
- Use multiple runs and statistical analysis for production performance validation
- Consider JVM warm-up effects when interpreting cold-start performance