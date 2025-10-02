# Query4j Query Optimizer

The Query4j Query Optimizer is a sophisticated analysis engine that examines dynamic queries and provides comprehensive optimization suggestions including index recommendations, predicate pushdown strategies, and join reordering optimizations.

## Features

- **Index Suggestion**: Analyzes predicates and join conditions to recommend database indexes
- **Predicate Pushdown**: Optimizes predicate placement and ordering for improved performance  
- **Join Reordering**: Suggests optimal join sequences to minimize intermediate result sizes
- **Modular Architecture**: Extensible design supporting future optimization strategies
- **Thread-Safe**: Immutable design following Query4j architectural principles
- **Configurable**: Flexible configuration options for different optimization scenarios

## Quick Start

### Basic Usage

```java
import com.github.query4j.core.QueryBuilder;
import com.github.query4j.optimizer.QueryOptimizer;
import com.github.query4j.optimizer.OptimizationResult;

// Create a QueryBuilder
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .where("email", "john@example.com")
    .and()
    .where("status", "ACTIVE")
    .join("orders")
    .where("orders.amount", ">", 100);

// Analyze with optimizer
QueryOptimizer optimizer = QueryOptimizer.create();
OptimizationResult result = optimizer.optimize(query);

// Review suggestions
System.out.println("Analysis Summary: " + result.getSummary());
System.out.println("Total Suggestions: " + result.getTotalSuggestionCount());

// Index suggestions
result.getIndexSuggestions().forEach(suggestion -> {
    System.out.println("Index: " + suggestion.generateCreateIndexSQL());
    System.out.println("Reason: " + suggestion.getReason());
    System.out.println("Priority: " + suggestion.getPriority());
});

// Predicate optimization suggestions  
result.getPredicatePushdownSuggestions().forEach(suggestion -> {
    System.out.println("Optimization: " + suggestion.getOptimizationType());
    System.out.println("Expected Impact: " + suggestion.getExpectedImpact());
});

// Join reordering suggestions
result.getJoinReorderSuggestions().forEach(suggestion -> {
    System.out.println("Original Sequence: " + suggestion.getOriginalJoinSequence());
    System.out.println("Suggested Sequence: " + suggestion.getSuggestedJoinSequence()); 
    System.out.println("Estimated Improvement: " + 
                     (suggestion.getEstimatedImprovement() * 100) + "%");
});
```

### Custom Configuration

```java
import com.github.query4j.optimizer.OptimizerConfig;

// High-performance configuration for production workloads
OptimizerConfig config = OptimizerConfig.highPerformanceConfig();

QueryOptimizer optimizer = QueryOptimizer.create(config);

// Development configuration with verbose output
OptimizerConfig devConfig = OptimizerConfig.developmentConfig();
QueryOptimizer devOptimizer = QueryOptimizer.create(devConfig);

// Custom configuration
OptimizerConfig customConfig = OptimizerConfig.builder()
    .indexSuggestionsEnabled(true)
    .predicatePushdownEnabled(true)
    .joinReorderingEnabled(false)
    .indexSelectivityThreshold(0.05)
    .maxAnalysisTimeMs(10000)
    .verboseOutput(true)
    .targetDatabase(OptimizerConfig.DatabaseType.POSTGRESQL)
    .build();
```

### SQL String Analysis

```java
String sqlQuery = "SELECT u.*, o.* FROM users u JOIN orders o ON u.id = o.user_id " +
                 "WHERE u.email = 'john@example.com' AND o.status = 'PENDING'";

OptimizationResult result = optimizer.optimize(sqlQuery);
// Note: SQL analysis capabilities are currently limited
// Use QueryBuilder analysis for comprehensive optimization
```

## Architecture

### Core Components

1. **QueryOptimizer**: Main service interface coordinating optimization strategies
2. **IndexAdvisor**: Analyzes predicates and joins to suggest database indexes  
3. **PredicatePushdownOptimizer**: Optimizes predicate placement and ordering
4. **JoinReorderOptimizer**: Analyzes join sequences for optimal performance

### Data Structures

- **OptimizationResult**: Immutable container for all optimization suggestions
- **IndexSuggestion**: Represents database index recommendations with metadata
- **PredicatePushdownSuggestion**: Describes predicate reordering opportunities
- **JoinReorderSuggestion**: Contains join sequence optimization recommendations

## Configuration Options

### OptimizerConfig Parameters

| Parameter | Default | Description |
|-----------|---------|-------------|
| `indexSuggestionsEnabled` | `true` | Enable/disable index analysis |
| `predicatePushdownEnabled` | `true` | Enable/disable predicate optimization |
| `joinReorderingEnabled` | `true` | Enable/disable join reordering |
| `indexSelectivityThreshold` | `0.1` | Minimum selectivity for index suggestions |
| `predicateReorderingThreshold` | `0.05` | Minimum improvement for reordering |
| `joinReorderingThreshold` | `0.1` | Minimum cardinality reduction for joins |
| `maxAnalysisTimeMs` | `5000` | Maximum analysis time before timeout |
| `verboseOutput` | `false` | Include detailed explanations |
| `maxCompositeIndexColumns` | `3` | Maximum columns in composite indexes |
| `targetDatabase` | `GENERIC` | Target database for SQL optimizations |

### Predefined Configurations

```java
// Default balanced configuration
OptimizerConfig.defaultConfig()

// Optimized for high-volume production queries  
OptimizerConfig.highPerformanceConfig()

// Development/debugging with verbose output
OptimizerConfig.developmentConfig()
```

## Index Suggestions

The IndexAdvisor analyzes query patterns and suggests three types of indexes:

### Single-Column Indexes
```java
// Analyzes individual predicates
IndexSuggestion emailIndex = IndexSuggestion.builder()
    .tableName("users")
    .columnNames(List.of("email"))
    .indexType(IndexSuggestion.IndexType.BTREE)
    .reason("Equality predicate optimization")
    .priority(IndexSuggestion.Priority.HIGH)
    .build();

System.out.println(emailIndex.generateCreateIndexSQL());
// Output: CREATE INDEX idx_users_email ON users (email)
```

### Composite Indexes
```java
// Multi-column indexes for complex queries
IndexSuggestion compositeIndex = IndexSuggestion.builder()
    .tableName("orders")  
    .columnNames(List.of("user_id", "status", "created_date"))
    .indexType(IndexSuggestion.IndexType.COMPOSITE)
    .reason("Multi-column query optimization")
    .build();
```

### Join Indexes
```java
// Foreign key indexes for join optimization
IndexSuggestion joinIndex = IndexSuggestion.builder()
    .tableName("order_items")
    .columnNames(List.of("order_id"))
    .reason("Join optimization for orders relationship")
    .priority(IndexSuggestion.Priority.HIGH)
    .build();
```

## Predicate Pushdown

The PredicatePushdownOptimizer identifies opportunities to improve query performance through predicate reordering and pushdown:

### Selectivity-Based Reordering
```java
// Reorders predicates by selectivity (most selective first)
PredicatePushdownSuggestion reordering = PredicatePushdownSuggestion.builder()
    .originalPosition(2)
    .suggestedPosition(0)
    .selectivity(0.05)  // Highly selective
    .optimizationType(OptimizationType.REORDER_BY_SELECTIVITY)
    .reason("Move highly selective predicate earlier")
    .build();
```

### Join Source Pushdown
```java
// Pushes predicates to join source tables
PredicatePushdownSuggestion pushdown = PredicatePushdownSuggestion.builder()
    .optimizationType(OptimizationType.PUSH_TO_JOIN_SOURCE)
    .targetTable("users")
    .reason("Filter users table before join")
    .expectedImpact("Reduced data transfer in join operation")
    .build();
```

## Join Reordering

The JoinReorderOptimizer analyzes join sequences and suggests optimal ordering:

### Selectivity-Based Reordering
```java
JoinReorderSuggestion joinOpt = JoinReorderSuggestion.builder()
    .originalJoinSequence(List.of("users", "orders", "items"))
    .suggestedJoinSequence(List.of("items", "orders", "users"))
    .reorderType(JoinReorderType.SELECTIVITY_BASED)
    .estimatedImprovement(0.4)  // 40% improvement
    .reason("Start with most selective table")
    .build();
```

### Index-Driven Optimization
```java
JoinReorderSuggestion indexOpt = JoinReorderSuggestion.builder()
    .reorderType(JoinReorderType.INDEX_DRIVEN)
    .reason("Leverage available indexes on join columns")
    .expectedImpact("Improved join performance through index utilization")
    .build();
```

## Advanced Usage

### Custom Analyzers

You can extend the optimizer with custom analyzers:

```java
// Custom table statistics for better join optimization
JoinReorderOptimizer.TableStatistics customStats = new JoinReorderOptimizer.TableStatistics() {
    @Override
    public long getEstimatedRowCount(String tableName) {
        // Return actual table row counts from database metadata
        return databaseMetadata.getRowCount(tableName);
    }
    
    @Override
    public double getJoinSelectivity(String leftTable, String rightTable, String joinField) {
        // Calculate actual join selectivity
        return statisticsService.getJoinSelectivity(leftTable, rightTable, joinField);
    }
    
    @Override
    public boolean hasIndexOnField(String tableName, String fieldName) {
        // Check actual database indexes
        return indexService.hasIndex(tableName, fieldName);
    }
};
```

### Batch Analysis

```java
List<QueryBuilder<?>> queries = getFrequentQueries();
List<OptimizationResult> results = new ArrayList<>();

QueryOptimizer optimizer = QueryOptimizer.create();

for (QueryBuilder<?> query : queries) {
    try {
        OptimizationResult result = optimizer.optimize(query);
        results.add(result);
    } catch (OptimizationException e) {
        logger.warn("Failed to optimize query: " + e.getMessage());
    }
}

// Aggregate results for database optimization plan
Set<IndexSuggestion> allIndexes = results.stream()
    .flatMap(r -> r.getIndexSuggestions().stream())
    .collect(Collectors.toSet());
```

## Performance Considerations

### Analysis Time
- Default timeout: 5 seconds per query
- Configurable via `maxAnalysisTimeMs`
- Use shorter timeouts for real-time optimization
- Longer timeouts for batch analysis scenarios

### Memory Usage
- All optimization results are immutable
- Predicate analysis scales with query complexity
- Join analysis scales with number of joined tables
- Consider query complexity when setting timeouts

### Thread Safety
- All optimizer components are thread-safe
- Immutable result objects can be shared between threads
- Safe for concurrent optimization requests
- No shared mutable state between analyses

## Integration Examples

### Spring Boot Integration

```java
@Component
public class QueryOptimizationService {
    
    private final QueryOptimizer optimizer;
    
    public QueryOptimizationService() {
        this.optimizer = QueryOptimizer.create(OptimizerConfig.highPerformanceConfig());
    }
    
    @Async
    public CompletableFuture<OptimizationResult> analyzeQueryAsync(QueryBuilder<?> query) {
        return CompletableFuture.supplyAsync(() -> optimizer.optimize(query));
    }
    
    public List<String> generateIndexDDL(OptimizationResult result) {
        return result.getIndexSuggestions().stream()
                .filter(s -> s.getPriority() == IndexSuggestion.Priority.HIGH)
                .map(IndexSuggestion::generateCreateIndexSQL)
                .collect(Collectors.toList());
    }
}
```

### Monitoring Integration

```java
@Component  
public class OptimizationMetrics {
    
    private final MeterRegistry meterRegistry;
    
    public void recordOptimization(OptimizationResult result) {
        // Record analysis time
        meterRegistry.timer("query.optimization.time")
                    .record(result.getAnalysisTimeMs(), TimeUnit.MILLISECONDS);
        
        // Record suggestion counts
        meterRegistry.counter("query.optimization.index.suggestions")
                    .increment(result.getIndexSuggestions().size());
        
        meterRegistry.counter("query.optimization.predicate.suggestions")  
                    .increment(result.getPredicatePushdownSuggestions().size());
        
        meterRegistry.counter("query.optimization.join.suggestions")
                    .increment(result.getJoinReorderSuggestions().size());
    }
}
```

## Testing

The optimizer includes comprehensive test coverage across multiple layers:

```bash
# Run all optimizer tests
../gradlew :optimizer:test

# Run specific test categories
../gradlew :optimizer:test --tests "*CorrectnessTest"
../gradlew :optimizer:test --tests "*IntegrationTest" 
../gradlew :optimizer:test --tests "*PerformanceTest"

# Run with coverage report
../gradlew :optimizer:jacocoTestReport

# View coverage report
open optimizer/build/reports/jacoco/test/html/index.html
```

### Test Architecture and Approach

The optimizer test suite is organized into four main categories for comprehensive correctness validation:

#### 1. **Unit Tests for Correctness** (`*CorrectnessTest`)
- **IndexAdvisorCorrectnessTest**: Validates index suggestion accuracy for various predicate patterns
- **PredicatePushdownOptimizerCorrectnessTest**: Tests predicate reordering logic and semantic preservation  
- **JoinReorderOptimizerCorrectnessTest**: Verifies join optimization correctness for inner/outer joins

These tests ensure that each optimizer component behaves correctly across edge cases, error conditions, and maintains query semantics.

#### 2. **Integration Tests** (`*IntegrationCorrectnessTest`)
- End-to-end optimizer functionality with complex multi-table queries
- Verification that index suggestions correlate to predicates and join keys
- Testing that predicate pushdown moves filtering closer to scan sources
- Validation that join orders reduce estimated intermediate result sizes
- Query semantic equivalence preservation after optimization

#### 3. **Performance Tests** (`*PerformanceTest`)
- Scalability testing with large queries (up to 20 tables)
- Concurrent optimization request handling and thread safety
- Memory usage and leak detection over repeated optimizations
- Performance threshold validation and benchmark logging

#### 4. **Property-Based Testing** (using jqwik)
Comprehensive randomized testing ensures robustness across wide input ranges.

```bash
# Run optimizer tests
./gradlew :optimizer:test

# Run with coverage report
./gradlew :optimizer:jacocoTestReport

# View coverage report
open optimizer/build/reports/jacoco/test/html/index.html
```

### Property-Based Testing

The optimizer uses jqwik for property-based testing of complex optimization scenarios:

```java
@Property
void optimizationShouldAlwaysReturnValidResult(@ForAll("queryBuilders") QueryBuilder<?> query) {
    QueryOptimizer optimizer = QueryOptimizer.create();
    OptimizationResult result = optimizer.optimize(query);
    
    assertThat(result).isNotNull();
    assertThat(result.getAnalysisTimeMs()).isGreaterThanOrEqualTo(0);
    assertThat(result.getSummary()).isNotNull();
}
```

## Documentation

For comprehensive documentation and API references:

- **[Main README](../README.md)** - Library overview, installation, and quick start
- **[API Reference Guide](../docs/API_GUIDE.md)** - Complete API documentation with examples
- **[Configuration Guide](../docs/Configuration.md)** - Optimizer configuration options
- **[JavaDoc Generation](../docs/JAVADOC_GENERATION.md)** - Generate and access API documentation
- **[Advanced Usage Tutorial](../ADVANCED.md)** - Query optimization strategies and best practices

### API Documentation (JavaDoc)

Generate JavaDoc for the optimizer module:

```bash
# Generate JavaDoc
./gradlew optimizer:javadoc

# View generated documentation
open optimizer/build/docs/javadoc/index.html
```

The optimizer module JavaDoc includes comprehensive documentation for:
- `QueryOptimizer` - Main interface for query optimization analysis
- `OptimizationResult` - Immutable container with index, predicate, and join suggestions
- `OptimizationException` - Exception handling for optimization failures
- `OptimizerConfig` - Configuration with predefined strategies
- Suggestion classes with SQL generation methods

## Related Modules

- **[Core Module](../core/README.md)** - Query building and execution
- **[Cache Module](../cache/README.md)** - Query caching with optimization strategies
- **[Examples Module](../examples/README.md)** - Optimization examples and patterns

## Contributing

When extending the optimizer:

1. **Follow immutability principles** - all optimization results must be immutable
2. **Implement comprehensive tests** - include unit, integration, and property-based tests  
3. **Document performance characteristics** - include time/space complexity analysis
4. **Support configuration** - make optimization strategies configurable
5. **Handle errors gracefully** - use OptimizationException for analysis failures
6. **Follow [documentation standards](../docs/API_DOCUMENTATION_GUIDE.md)**

See the [Contributing Guide](../CONTRIBUTING.md) for complete guidelines.

## Support

- **Issues**: [GitHub Issues](https://github.com/query4j/dynamicquerybuilder/issues)
- **Discussions**: [GitHub Discussions](https://github.com/query4j/dynamicquerybuilder/discussions)

## Dependencies

The optimizer module depends on:

- **Core module**: For QueryBuilder and predicate analysis
- **Lombok**: For immutable data classes
- **JUnit 5**: For unit testing
- **jqwik**: For property-based testing
- **Mockito**: For test mocking
- **AssertJ**: For fluent test assertions

## Version History

- **1.0.0**: Initial release with index suggestions, predicate pushdown, and join reordering