# Query Optimizer - Performance Tuning and Analysis

The Query Optimizer module analyzes queries and provides actionable recommendations for improving performance through index suggestions, predicate pushdown, and join reordering.

## Table of Contents

1. [Overview](#overview)
2. [Installation](#installation)
3. [Quick Start](#quick-start)
4. [Optimization Features](#optimization-features)
5. [Configuration](#configuration)
6. [Analysis Results](#analysis-results)
7. [Integration Patterns](#integration-patterns)
8. [Performance Impact](#performance-impact)

---

## Overview

The Optimizer provides:

- **Index Suggestions** - Recommends indexes based on WHERE predicates and JOIN conditions
- **Predicate Pushdown** - Identifies predicates that can be pushed down to reduce data scanning
- **Join Reordering** - Suggests optimal join order based on table statistics
- **SQL Analysis** - Parses and analyzes SQL strings for optimization opportunities
- **Database-Specific Optimization** - Tailored recommendations for PostgreSQL, MySQL, H2

### When to Use the Optimizer

✅ **Use for:**
- Performance-critical queries
- Complex multi-table JOINs
- Production query tuning
- Database migration analysis
- Query performance regression testing

---

## Installation

### Maven

```xml
<dependency>
    <groupId>com.github.query4j</groupId>
    <artifactId>dynamicquerybuilder-optimizer</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Gradle

```groovy
dependencies {
    implementation 'com.github.query4j:dynamicquerybuilder-optimizer:1.0.0-SNAPSHOT'
}
```

---

## Quick Start

### Basic Optimization

```java
import com.github.query4j.optimizer.QueryOptimizer;
import com.github.query4j.optimizer.OptimizationResult;
import com.github.query4j.core.QueryBuilder;

// Create optimizer with default configuration
QueryOptimizer optimizer = QueryOptimizer.create();

// Build a query
QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
    .where("department", "Engineering")
    .and()
    .where("active", true)
    .orderBy("lastName");

// Analyze query
OptimizationResult result = optimizer.analyze(query);

// Review suggestions
result.getIndexSuggestions().forEach(suggestion -> {
    System.out.println("Suggested Index: " + suggestion.getIndexDefinition());
    System.out.println("Reason: " + suggestion.getReason());
    System.out.println("Expected Improvement: " + 
                       (suggestion.getEstimatedImprovement() * 100) + "%");
});
```

### With Custom Configuration

```java
import com.github.query4j.optimizer.OptimizerConfig;

// High-performance configuration
OptimizerConfig config = OptimizerConfig.highPerformanceConfig();

QueryOptimizer optimizer = QueryOptimizer.create(config);
OptimizationResult result = optimizer.analyze(query);
```

---

## Optimization Features

### 1. Index Suggestions

Analyzes predicates and suggests indexes to improve query performance.

```java
QueryBuilder<Order> query = QueryBuilder.forEntity(Order.class)
    .where("customer_id", 12345)
    .and()
    .where("status", "completed")
    .and()
    .whereBetween("order_date", startDate, endDate);

OptimizationResult result = optimizer.analyze(query);

// Index suggestions
result.getIndexSuggestions().forEach(suggestion -> {
    System.out.println(suggestion.getIndexDefinition());
});

// Output:
// CREATE INDEX idx_order_customer_id ON Order(customer_id)
// CREATE INDEX idx_order_status ON Order(status)
// CREATE INDEX idx_order_date_range ON Order(order_date)
```

**Index Types Recommended:**
- Single-column indexes for equality predicates
- Composite indexes for multi-column queries
- Range indexes for BETWEEN predicates
- Covering indexes for frequently queried columns

### 2. Predicate Pushdown

Identifies predicates that can be pushed down to reduce data scanning.

```java
QueryBuilder<Order> query = QueryBuilder.forEntity(Order.class)
    .innerJoin("Customer", "customer_id", "id")
    .where("Customer.country", "USA")
    .and()
    .where("Order.status", "shipped");

OptimizationResult result = optimizer.analyze(query);

result.getPredicatePushdownSuggestions().forEach(suggestion -> {
    System.out.println("Original: " + suggestion.getOriginalPredicate());
    System.out.println("Suggested: " + suggestion.getPushedDownPredicate());
    System.out.println("Benefit: " + suggestion.getEstimatedBenefit());
});
```

**Pushdown Opportunities:**
- Filter predicates before JOINs
- Apply WHERE conditions early in query plan
- Reduce intermediate result set sizes

### 3. Join Reordering

Suggests optimal join order based on table statistics and predicates.

```java
QueryBuilder<Order> query = QueryBuilder.forEntity(Order.class)
    .innerJoin("Customer", "customer_id", "id")
    .innerJoin("Product", "product_id", "id")
    .innerJoin("Warehouse", "warehouse_id", "id")
    .where("Warehouse.region", "WEST");

OptimizationResult result = optimizer.analyze(query);

result.getJoinReorderSuggestions().forEach(suggestion -> {
    System.out.println("Original Join Order: " + 
                       suggestion.getOriginalJoinSequence());
    System.out.println("Suggested Join Order: " + 
                       suggestion.getSuggestedJoinSequence());
    System.out.println("Reasoning: " + suggestion.getReasoning());
});

// Output:
// Original: Order → Customer → Product → Warehouse
// Suggested: Order → Warehouse → Product → Customer
// Reasoning: Filter on Warehouse.region early to reduce join cardinality
```

**Join Optimization Factors:**
- Table sizes (smaller tables first)
- Predicate selectivity
- Index availability
- JOIN type (INNER vs LEFT)

### 4. SQL String Analysis

Analyze raw SQL strings directly (not just QueryBuilder objects).

```java
String sql = "SELECT * FROM orders o " +
             "INNER JOIN customers c ON o.customer_id = c.id " +
             "WHERE o.status = 'completed' AND c.country = 'USA'";

OptimizationResult result = optimizer.analyzeSQL(sql);

// Get optimization suggestions
result.getAllSuggestions().forEach(System.out::println);
```

---

## Configuration

### OptimizerConfig Options

```java
import com.github.query4j.optimizer.OptimizerConfig;
import java.time.Duration;

OptimizerConfig config = OptimizerConfig.builder()
    .indexSuggestionsEnabled(true)           // Enable index suggestions
    .predicatePushdownEnabled(true)          // Enable pushdown analysis
    .joinReorderingEnabled(true)             // Enable join optimization
    .indexSelectivityThreshold(0.05)         // 5% selectivity threshold
    .maxAnalysisTimeMs(5000)                 // 5-second timeout
    .verboseOutput(true)                     // Detailed logging
    .targetDatabase(OptimizerConfig.DatabaseType.POSTGRESQL)
    .build();

QueryOptimizer optimizer = QueryOptimizer.create(config);
```

### Configuration Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `indexSuggestionsEnabled` | boolean | true | Enable index recommendations |
| `predicatePushdownEnabled` | boolean | true | Enable pushdown analysis |
| `joinReorderingEnabled` | boolean | true | Enable join optimization |
| `indexSelectivityThreshold` | double | 0.05 | Index selectivity threshold (0-1) |
| `maxAnalysisTimeMs` | long | 10000 | Maximum analysis time (ms) |
| `verboseOutput` | boolean | false | Detailed logging output |
| `targetDatabase` | DatabaseType | H2 | Target database type |

### Predefined Configurations

#### Default Configuration

```java
OptimizerConfig config = OptimizerConfig.defaultConfig();

// Balanced settings suitable for most applications
// - All optimizations enabled
// - 10-second analysis timeout
// - 5% selectivity threshold
```

#### High-Performance Configuration

```java
OptimizerConfig config = OptimizerConfig.highPerformanceConfig();

// Optimized for production workloads
// - Aggressive optimization
// - Lower selectivity threshold (1%)
// - Extended analysis time (15 seconds)
// - Verbose output disabled
```

#### Development Configuration

```java
OptimizerConfig config = OptimizerConfig.developmentConfig();

// Verbose output for debugging
// - All optimizations enabled
// - Detailed logging
// - Shorter timeout (5 seconds)
```

### Database-Specific Configuration

```java
// PostgreSQL
OptimizerConfig pgConfig = OptimizerConfig.builder()
    .targetDatabase(OptimizerConfig.DatabaseType.POSTGRESQL)
    .build();

// MySQL
OptimizerConfig mysqlConfig = OptimizerConfig.builder()
    .targetDatabase(OptimizerConfig.DatabaseType.MYSQL)
    .build();

// H2 (default for testing)
OptimizerConfig h2Config = OptimizerConfig.builder()
    .targetDatabase(OptimizerConfig.DatabaseType.H2)
    .build();
```

---

## Analysis Results

### OptimizationResult API

```java
OptimizationResult result = optimizer.analyze(query);

// Index suggestions
List<IndexSuggestion> indexes = result.getIndexSuggestions();

// Predicate pushdown suggestions
List<PredicatePushdownSuggestion> pushdowns = 
    result.getPredicatePushdownSuggestions();

// Join reordering suggestions
List<JoinReorderSuggestion> joins = result.getJoinReorderSuggestions();

// Overall optimization score (0.0 - 1.0)
double score = result.getOptimizationScore();

// Has any suggestions?
boolean hasOptimizations = result.hasSuggestions();

// Analysis duration
Duration analysisTime = result.getAnalysisTime();
```

### IndexSuggestion Details

```java
IndexSuggestion suggestion = result.getIndexSuggestions().get(0);

String indexDef = suggestion.getIndexDefinition();
// "CREATE INDEX idx_user_department ON User(department)"

String tableName = suggestion.getTableName();        // "User"
List<String> columns = suggestion.getColumns();      // ["department"]
String indexType = suggestion.getIndexType();        // "BTREE"
String reason = suggestion.getReason();              
// "Equality predicate on department column"

double improvement = suggestion.getEstimatedImprovement();  
// 0.75 (75% improvement expected)

int priority = suggestion.getPriority();  // 1 (high), 2 (medium), 3 (low)
```

### Applying Suggestions

```java
OptimizationResult result = optimizer.analyze(query);

// Generate DDL statements
List<String> ddlStatements = result.getIndexSuggestions()
    .stream()
    .map(IndexSuggestion::getIndexDefinition)
    .collect(Collectors.toList());

// Execute DDL (example with JDBC)
try (Connection conn = dataSource.getConnection();
     Statement stmt = conn.createStatement()) {
    
    for (String ddl : ddlStatements) {
        System.out.println("Executing: " + ddl);
        stmt.execute(ddl);
    }
}
```

---

## Integration Patterns

### Spring Boot Integration

```java
@Configuration
public class OptimizerConfig {
    
    @Bean
    public QueryOptimizer queryOptimizer(
            @Value("${query4j.optimizer.enabled:true}") boolean enabled,
            @Value("${query4j.optimizer.verbose:false}") boolean verbose) {
        
        if (!enabled) {
            return QueryOptimizer.noOp(); // Disabled optimizer
        }
        
        OptimizerConfig config = OptimizerConfig.builder()
            .verboseOutput(verbose)
            .targetDatabase(OptimizerConfig.DatabaseType.POSTGRESQL)
            .build();
        
        return QueryOptimizer.create(config);
    }
}

@Service
public class UserService {
    
    @Autowired
    private QueryOptimizer optimizer;
    
    public List<User> searchUsers(UserSearchCriteria criteria) {
        QueryBuilder<User> query = buildQuery(criteria);
        
        // Analyze in development/staging
        if (isDevelopmentEnvironment()) {
            OptimizationResult result = optimizer.analyze(query);
            logOptimizations(result);
        }
        
        return executeQuery(query);
    }
}
```

### Monitoring Integration

```java
@Component
public class QueryOptimizationMonitor {
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    @Autowired
    private QueryOptimizer optimizer;
    
    public OptimizationResult analyzeWithMetrics(QueryBuilder<?> query) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        OptimizationResult result = optimizer.analyze(query);
        
        sample.stop(Timer.builder("query.optimization.time")
            .tag("has_suggestions", String.valueOf(result.hasSuggestions()))
            .register(meterRegistry));
        
        // Record suggestion counts
        meterRegistry.counter("query.optimization.index_suggestions",
            "count", String.valueOf(result.getIndexSuggestions().size()))
            .increment();
        
        return result;
    }
}
```

### Automated Index Creation

```java
@Service
public class AutoIndexService {
    
    @Autowired
    private QueryOptimizer optimizer;
    
    @Autowired
    private DataSource dataSource;
    
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    public void analyzeAndCreateIndexes() {
        // Analyze common queries
        List<QueryBuilder<?>> queries = getCommonQueries();
        
        Set<String> indexDDL = new HashSet<>();
        
        for (QueryBuilder<?> query : queries) {
            OptimizationResult result = optimizer.analyze(query);
            
            result.getIndexSuggestions()
                .stream()
                .filter(s -> s.getPriority() == 1) // Only high-priority
                .map(IndexSuggestion::getIndexDefinition)
                .forEach(indexDDL::add);
        }
        
        // Create indexes
        createIndexes(indexDDL);
    }
    
    private void createIndexes(Set<String> ddlStatements) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            for (String ddl : ddlStatements) {
                try {
                    stmt.execute(ddl);
                    logger.info("Created index: {}", ddl);
                } catch (SQLException e) {
                    logger.warn("Failed to create index: {}", ddl, e);
                }
            }
        } catch (SQLException e) {
            logger.error("Database connection error", e);
        }
    }
}
```

---

## Performance Impact

### Optimizer Overhead

From JMH benchmarks:

| Query Complexity | Analysis Time | Overhead |
|-----------------|---------------|----------|
| Simple (1-2 predicates) | 0.5 ms | ~5% |
| Moderate (3-5 predicates) | 1.2 ms | ~8% |
| Complex (6+ predicates, JOINs) | 3.5 ms | ~12% |
| Very Complex (10+ predicates, multiple JOINs) | 8.0 ms | ~15% |

**Recommendation:** Use optimizer analysis in development/staging, or asynchronously in production.

### Expected Query Improvements

Based on benchmark testing:

| Optimization Type | Avg Improvement | Range |
|-------------------|----------------|-------|
| Index on equality predicate | 60-80% | 40-95% |
| Composite index | 70-90% | 50-98% |
| Join reordering | 30-50% | 10-80% |
| Predicate pushdown | 20-40% | 10-60% |

---

## Best Practices

### 1. Analyze in Development

```java
// ✅ Good - analyze during development
if (environment.isDevelopment()) {
    OptimizationResult result = optimizer.analyze(query);
    logger.info("Optimization suggestions: {}", result.getSummary());
}
```

### 2. Cache Analysis Results

```java
// ✅ Good - cache expensive analysis
Map<String, OptimizationResult> analysisCache = new ConcurrentHashMap<>();

public OptimizationResult getCachedAnalysis(QueryBuilder<?> query) {
    String key = query.toSQL();
    return analysisCache.computeIfAbsent(key, 
        k -> optimizer.analyze(query));
}
```

### 3. Review High-Priority Suggestions First

```java
// ✅ Good - prioritize by impact
result.getIndexSuggestions()
    .stream()
    .filter(s -> s.getPriority() == 1)
    .forEach(s -> applyIndexSuggestion(s));
```

### 4. Test Before Production

```java
// ✅ Good - validate in staging
@Test
public void testIndexImprovesPerformance() {
    QueryBuilder<User> query = buildComplexQuery();
    
    long baselineTime = measureQueryTime(query);
    
    // Apply optimizer suggestions
    OptimizationResult result = optimizer.analyze(query);
    applySuggestions(result);
    
    long optimizedTime = measureQueryTime(query);
    
    assertTrue(optimizedTime < baselineTime * 0.8); // 20% improvement
}
```

---

## Troubleshooting

### Problem: No Suggestions Generated

**Causes:**
- Query already optimal
- Configuration too restrictive
- Analysis timeout

**Solutions:**
- Review optimizer configuration
- Increase `maxAnalysisTimeMs`
- Enable verbose output for details

### Problem: Suggestions Not Helpful

**Causes:**
- Incorrect database type
- Missing table statistics
- Query pattern not supported

**Solutions:**
- Set correct `targetDatabase`
- Update database statistics
- Review query structure

---

## See Also

- **[Core Module](Core-Module)** - Query building fundamentals
- **[Cache Manager](Cache-Manager)** - Query result caching
- **[Benchmarking](Benchmarking)** - Performance analysis
- **[Configuration](Configuration)** - Configuration guide

---

**Last Updated:** October 2025  
**Version:** 1.0.0
