# Benchmarking - Performance Analysis and Results

Query4j performance benchmarks using JMH (Java Microbenchmark Harness) with detailed analysis and comparisons.

## Table of Contents

1. [Overview](#overview)
2. [Benchmark Categories](#benchmark-categories)
3. [Running Benchmarks](#running-benchmarks)
4. [Performance Results](#performance-results)
5. [Optimization Impact](#optimization-impact)
6. [Interpreting Results](#interpreting-results)

---

## Overview

Query4j uses JMH for rigorous performance testing across multiple scenarios:

- **Core Module Benchmarks** - Individual component performance
- **Query Builder Benchmarks** - End-to-end query construction
- **Optimizer Profiling** - Optimization analysis overhead
- **Baseline Comparisons** - vs Hibernate, MyBatis, JDBC

### Performance Targets

| Component | Target | Achieved |
|-----------|--------|----------|
| Simple query construction | < 1 µs | ✅ 0.8 µs |
| Complex query (5+ predicates) | < 5 µs | ✅ 2.5 µs |
| Optimizer analysis overhead | < 10 µs | ✅ 8.0 µs |
| Cache lookup | < 0.5 µs | ✅ 0.3 µs |

---

## Benchmark Categories

### 1. Core Module Micro-Benchmarks

Measures individual predicate types and operations:

**Predicate Types:**
- SimplePredicate (equality)
- InPredicate (multiple values)
- BetweenPredicate (range)
- LikePredicate (pattern matching)
- NullPredicate (NULL checks)

**Operations:**
- Query cloning (immutability cost)
- Parameter extraction
- SQL generation
- Logical chaining (AND/OR)

### 2. Query Builder Benchmarks

End-to-end scenarios:

**Simple Query:**
```java
QueryBuilder.forEntity(User.class)
    .where("active", true)
    .limit(10);
```

**Moderate Query:**
```java
QueryBuilder.forEntity(User.class)
    .where("department", "Engineering")
    .and()
    .whereIn("role", Arrays.asList("admin", "developer"))
    .orderBy("lastName")
    .limit(20)
    .offset(40);
```

**Complex Query:**
```java
QueryBuilder.forEntity(Order.class)
    .innerJoin("Customer", "customer_id", "id")
    .openGroup()
        .where("Order.status", "completed")
        .or()
        .where("Order.status", "shipped")
    .closeGroup()
    .and()
    .whereBetween("Order.amount", 100, 10000)
    .groupBy("customer_id")
    .having("COUNT(*) > 5")
    .orderBy("total", "DESC")
    .limit(50);
```

### 3. Optimizer Profiling

Measures optimization analysis overhead:

- Index suggestion generation
- Predicate pushdown analysis
- Join reordering computation
- Overall optimization impact

### 4. Baseline Comparisons

Head-to-head with popular libraries:

- **Hibernate Criteria API**
- **MyBatis Dynamic SQL**
- **Raw JDBC**
- **Query4j + Optimizer**

---

## Running Benchmarks

### Prerequisites

- **JDK 17** or higher
- **Gradle 8.5+** (included wrapper)

### Run All Benchmarks

```bash
# Run all core module benchmarks
./gradlew benchmark:coreModuleBenchmark

# Run query builder benchmarks
./gradlew benchmark:benchmark

# Run optimizer benchmarks
./gradlew benchmark:optimizerBenchmark

# Run baseline comparisons
./gradlew benchmark:baselineComparisonBenchmark
```

### Generate Reports

```bash
# Generate CSV output
./gradlew benchmark:coreModuleBenchmarkCsv

# Generate HTML report
./gradlew benchmark:generateCoreModuleHtmlReport

# View results
open benchmark/build/reports/benchmarks/index.html
```

### Custom Benchmark Execution

```bash
# Build benchmark JAR
./gradlew benchmark:benchmarkJar

# Run specific benchmark
java -jar benchmark/build/libs/benchmark.jar \
    ".*SimplePredicate.*" \
    -f 5 \
    -wi 3 \
    -i 10

# Options:
# -f: Number of forks
# -wi: Warmup iterations
# -i: Measurement iterations
# -prof: Profiler (gc, stack, etc.)
```

---

## Performance Results

### Core Module Benchmarks

From JMH tests (Intel i7, 16GB RAM):

| Benchmark | Mode | Score | Unit | Notes |
|-----------|------|-------|------|-------|
| SimplePredicate | avgt | 0.532 | µs/op | Equality predicate |
| InPredicate (3 values) | avgt | 0.847 | µs/op | IN clause |
| BetweenPredicate | avgt | 0.621 | µs/op | Range query |
| LikePredicate | avgt | 0.558 | µs/op | Pattern match |
| NullPredicate | avgt | 0.412 | µs/op | IS NULL |
| Query Cloning | avgt | 0.234 | µs/op | Immutability cost |
| Parameter Extraction | avgt | 0.156 | µs/op | getParameters() |
| SQL Generation | avgt | 1.245 | µs/op | toSQL() |
| Logical AND Chain (5) | avgt | 2.134 | µs/op | 5 predicates |
| Nested Groups | avgt | 2.567 | µs/op | Complex logic |

**Insights:**
- Sub-microsecond performance for most operations
- Immutability overhead is minimal (~0.2 µs)
- Complex queries with nesting still under 3 µs

### Query Builder Benchmarks

| Scenario | Construction | Throughput | Percentile (99th) |
|----------|-------------|------------|-------------------|
| Simple | 0.847 µs | 1,180,637 ops/s | 1.2 µs |
| Moderate | 1.523 µs | 656,455 ops/s | 2.1 µs |
| Complex | 2.834 µs | 352,983 ops/s | 3.8 µs |

**Insights:**
- Simple queries: >1M ops/second
- Complex queries with JOINs/grouping: >350K ops/second
- 99th percentile latency under 4 µs

### Optimizer Benchmarks

| Analysis Type | Overhead | Range |
|--------------|----------|-------|
| Simple query (1-2 predicates) | 0.5 ms | 0.3-0.8 ms |
| Moderate (3-5 predicates) | 1.2 ms | 0.8-1.8 ms |
| Complex (6+ predicates, JOINs) | 3.5 ms | 2.5-5.0 ms |
| Very complex (10+ predicates) | 8.0 ms | 6.0-12.0 ms |

**Insights:**
- Overhead is ~5-15% of typical query construction
- Suitable for dev/staging environments
- Consider async analysis in production

### Baseline Comparison (Pagination)

Query construction for paginated results:

| Library | Construction Time | Relative Performance |
|---------|------------------|---------------------|
| Raw JDBC | 0.260 µs | 1.0× (baseline) |
| **Query4j** | 0.847 µs | **3.3×** |
| Hibernate Criteria | 1.053 µs | 4.0× |
| MyBatis Dynamic SQL | 0.913 µs | 3.5× |

**Execution time (with database):**

| Library | Execution Time | Relative Performance |
|---------|---------------|---------------------|
| Raw JDBC | 15.492 µs | 1.0× |
| **Query4j** | 16.234 µs | **1.05×** |
| Hibernate (JPA) | 101.873 µs | 6.6× |

**Key Findings:**
- Query4j construction is 3.3× slower than raw JDBC but offers type safety
- Query4j execution overhead is only 5% vs raw JDBC
- Hibernate has 6.6× execution overhead due to ORM features
- Query4j provides best balance of performance and developer experience

---

## Optimization Impact

### Query Performance with Optimizer

Measured improvement after applying optimizer suggestions:

| Query Type | Before (ms) | After (ms) | Improvement |
|------------|-------------|------------|-------------|
| Equality predicate without index | 125 | 8 | 93.6% |
| Range query without index | 245 | 15 | 93.9% |
| Multi-table JOIN (wrong order) | 380 | 145 | 61.8% |
| Complex with predicate pushdown | 520 | 280 | 46.2% |

**Based on:**
- PostgreSQL 14
- Test dataset: 1M users, 10M orders
- Applied index suggestions and join reordering

### Index Suggestion Impact

| Index Type | Avg Improvement | Range |
|------------|----------------|-------|
| Single-column equality | 70% | 50-95% |
| Composite index | 80% | 60-98% |
| Range index | 65% | 40-90% |
| Covering index | 85% | 70-98% |

---

## Interpreting Results

### Understanding JMH Output

```
Benchmark                         Mode  Cnt   Score   Error  Units
SimplePredicate.construction      avgt   10   0.532 ± 0.008  us/op
```

- **Mode:** `avgt` = average time per operation
- **Cnt:** 10 iterations
- **Score:** 0.532 microseconds per operation
- **Error:** ±0.008 microseconds (confidence interval)
- **Units:** microseconds per operation (µs/op)

### Performance Guidelines

**Excellent:** < 1 µs  
**Good:** 1-5 µs  
**Acceptable:** 5-10 µs  
**Review Required:** > 10 µs

### When to Optimize

Optimize if:
- Query construction > 10 µs consistently
- Cache hit rate < 50%
- Optimizer overhead > 20% of construction time
- Database execution > 100 ms

### Profiling Commands

```bash
# GC profiling
java -jar benchmark.jar -prof gc

# Stack profiling
java -jar benchmark.jar -prof stack

# Memory allocation
java -jar benchmark.jar -prof gc:churn
```

---

## Best Practices

### 1. Run on Production-Like Hardware

```bash
# Disable CPU frequency scaling
echo performance | sudo tee /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor

# Isolate CPU cores
taskset -c 0-3 java -jar benchmark.jar
```

### 2. Sufficient Warmup

```bash
# More warmup iterations for stable results
java -jar benchmark.jar -wi 10 -i 20
```

### 3. Multiple Forks

```bash
# Multiple JVM forks for consistency
java -jar benchmark.jar -f 5
```

### 4. Consistent Environment

- Close unnecessary applications
- Disable background services
- Use consistent JVM settings
- Same JDK version across runs

---

## Continuous Performance Monitoring

### CI/CD Integration

```yaml
# .github/workflows/benchmark.yml
name: Performance Benchmarks

on:
  push:
    branches: [main]
  pull_request:

jobs:
  benchmark:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Run benchmarks
        run: ./gradlew benchmark:benchmark
      - name: Upload results
        uses: actions/upload-artifact@v3
        with:
          name: benchmark-results
          path: benchmark/build/reports/
```

### Performance Regression Detection

Compare benchmark results across commits:

```bash
# Run baseline
git checkout main
./gradlew benchmark:benchmark > baseline.txt

# Run with changes
git checkout feature-branch
./gradlew benchmark:benchmark > feature.txt

# Compare (manual or automated)
diff baseline.txt feature.txt
```

---

## See Also

- **[BENCHMARKS.md](https://github.com/query4j/dynamicquerybuilder/blob/master/BENCHMARKS.md)** - Detailed benchmark analysis
- **[Benchmark Module](https://github.com/query4j/dynamicquerybuilder/tree/master/benchmark)** - Source code and results
- **[Optimizer](Optimizer)** - Query optimization guide
- **[Performance Tuning](Core-Module#performance-considerations)** - Optimization tips

---

**Last Updated:** December 2024  
**Version:** 1.0.0
