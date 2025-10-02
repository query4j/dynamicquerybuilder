# Configuration - Query4j Settings and Options

Query4j provides comprehensive configuration options for core, cache, and optimizer modules. This guide covers configuration sources, options, and best practices.

## Table of Contents

1. [Configuration Sources](#configuration-sources)
2. [Core Module Configuration](#core-module-configuration)
3. [Cache Module Configuration](#cache-module-configuration)
4. [Optimizer Module Configuration](#optimizer-module-configuration)
5. [Environment-Specific Settings](#environment-specific-settings)
6. [Best Practices](#best-practices)

---

## Configuration Sources

Query4j supports multiple configuration sources with priority-based overrides:

1. **Environment Variables** (highest priority)
2. **System Properties**
3. **YAML Files** (`query4j.yml`)
4. **Properties Files** (`query4j.properties`)
5. **Default Values** (lowest priority)

### Properties File Format

Create `query4j.properties` in your resources directory:

```properties
# Core Configuration
query4j.core.defaultQueryTimeoutMs=30000
query4j.core.maxPredicateDepth=10
query4j.core.maxPredicateCount=50
query4j.core.strictFieldValidation=true

# Cache Configuration
query4j.cache.enabled=true
query4j.cache.defaultTtlSeconds=3600
query4j.cache.maxSize=10000

# Optimizer Configuration
query4j.optimizer.indexSuggestionsEnabled=true
query4j.optimizer.predicatePushdownEnabled=true
query4j.optimizer.verboseOutput=false
```

### YAML File Format

Create `query4j.yml` in your resources directory:

```yaml
query4j:
  core:
    defaultQueryTimeoutMs: 30000
    maxPredicateDepth: 10
    maxPredicateCount: 50
    strictFieldValidation: true
  
  cache:
    enabled: true
    defaultTtlSeconds: 3600
    maxSize: 10000
  
  optimizer:
    indexSuggestionsEnabled: true
    predicatePushdownEnabled: true
    verboseOutput: false
```

### Environment Variables

Override any property using environment variables:

```bash
# Naming convention: UPPER_CASE with underscores
export QUERY4J_CORE_MAX_PREDICATE_DEPTH=15
export QUERY4J_CACHE_ENABLED=false
export QUERY4J_OPTIMIZER_VERBOSE_OUTPUT=true
```

**Environment Variable Rules:**
- Convert to uppercase: `query4j.core.maxPredicateDepth` → `QUERY4J_CORE_MAX_PREDICATE_DEPTH`
- Replace dots (`.`) with underscores (`_`)

---

## Core Module Configuration

### Available Options

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `defaultQueryTimeoutMs` | long | 30000 | Default query timeout (milliseconds) |
| `maxPredicateDepth` | int | 10 | Maximum nesting depth for logical groups |
| `maxPredicateCount` | int | 50 | Maximum number of predicates per query |
| `strictFieldValidation` | boolean | true | Enforce field name validation rules |
| `queryStatisticsEnabled` | boolean | false | Track query building statistics |

### Example Configuration

```properties
# Core module settings
query4j.core.defaultQueryTimeoutMs=45000
query4j.core.maxPredicateDepth=15
query4j.core.maxPredicateCount=100
query4j.core.strictFieldValidation=true
query4j.core.queryStatisticsEnabled=true
```

### Programmatic Configuration

```java
import com.github.query4j.core.CoreConfig;

CoreConfig config = CoreConfig.builder()
    .defaultQueryTimeoutMs(45000)
    .maxPredicateDepth(15)
    .maxPredicateCount(100)
    .strictFieldValidation(true)
    .build();

// Use config with QueryBuilder
QueryBuilder.withConfig(config);
```

---

## Cache Module Configuration

### Available Options

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `enabled` | boolean | true | Enable/disable caching |
| `strategy` | String | "LRU" | Cache eviction strategy (LRU/LFU) |
| `maxSize` | int | 100 | Maximum cache entries |
| `defaultTtlSeconds` | long | 3600 | Default time-to-live (seconds) |
| `defaultRegion` | String | "default" | Default cache region name |
| `concurrencyLevel` | int | 4 | Thread concurrency hint |

### Example Configuration

```properties
# Cache module settings
query4j.cache.enabled=true
query4j.cache.strategy=LRU
query4j.cache.maxSize=5000
query4j.cache.defaultTtlSeconds=1800
query4j.cache.defaultRegion=queries
query4j.cache.concurrencyLevel=8
```

### Programmatic Configuration

```java
import com.github.query4j.cache.CacheConfig;
import java.time.Duration;

CacheConfig config = CacheConfig.builder()
    .strategy(CacheConfig.Strategy.LRU)
    .maxSize(5000)
    .ttl(Duration.ofMinutes(30))
    .concurrencyLevel(8)
    .enableMetrics(true)
    .build();

QueryCacheManager cache = QueryCacheManager.create(config);
```

---

## Optimizer Module Configuration

### Available Options

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `indexSuggestionsEnabled` | boolean | true | Enable index suggestions |
| `predicatePushdownEnabled` | boolean | true | Enable predicate pushdown analysis |
| `joinReorderingEnabled` | boolean | true | Enable join reordering suggestions |
| `indexSelectivityThreshold` | double | 0.05 | Selectivity threshold (0-1) |
| `maxAnalysisTimeMs` | long | 10000 | Maximum analysis time (ms) |
| `verboseOutput` | boolean | false | Enable detailed logging |
| `targetDatabase` | String | "H2" | Target database (H2/POSTGRESQL/MYSQL) |

### Example Configuration

```properties
# Optimizer module settings
query4j.optimizer.indexSuggestionsEnabled=true
query4j.optimizer.predicatePushdownEnabled=true
query4j.optimizer.joinReorderingEnabled=true
query4j.optimizer.indexSelectivityThreshold=0.01
query4j.optimizer.maxAnalysisTimeMs=15000
query4j.optimizer.verboseOutput=true
query4j.optimizer.targetDatabase=POSTGRESQL
```

### Programmatic Configuration

```java
import com.github.query4j.optimizer.OptimizerConfig;

OptimizerConfig config = OptimizerConfig.builder()
    .indexSuggestionsEnabled(true)
    .predicatePushdownEnabled(true)
    .joinReorderingEnabled(true)
    .indexSelectivityThreshold(0.01)
    .maxAnalysisTimeMs(15000)
    .verboseOutput(true)
    .targetDatabase(OptimizerConfig.DatabaseType.POSTGRESQL)
    .build();

QueryOptimizer optimizer = QueryOptimizer.create(config);
```

---

## Environment-Specific Settings

### Development Environment

```yaml
query4j:
  core:
    defaultQueryTimeoutMs: 10000
    maxPredicateDepth: 5
    strictFieldValidation: true
    queryStatisticsEnabled: true
  
  cache:
    enabled: false  # Disable for development
    maxSize: 10
  
  optimizer:
    indexSuggestionsEnabled: true
    verboseOutput: true  # Detailed logging
    maxAnalysisTimeMs: 5000
```

### Staging Environment

```yaml
query4j:
  core:
    defaultQueryTimeoutMs: 30000
    maxPredicateDepth: 10
    strictFieldValidation: true
    queryStatisticsEnabled: true
  
  cache:
    enabled: true
    strategy: LRU
    maxSize: 500
    defaultTtlSeconds: 600  # 10 minutes
  
  optimizer:
    indexSuggestionsEnabled: true
    verboseOutput: false
    maxAnalysisTimeMs: 10000
    targetDatabase: POSTGRESQL
```

### Production Environment

```yaml
query4j:
  core:
    defaultQueryTimeoutMs: 45000
    maxPredicateDepth: 15
    maxPredicateCount: 100
    strictFieldValidation: true
    queryStatisticsEnabled: false  # Reduce overhead
  
  cache:
    enabled: true
    strategy: LFU  # Frequency-based for production
    maxSize: 10000
    defaultTtlSeconds: 3600  # 1 hour
    concurrencyLevel: 16
  
  optimizer:
    indexSuggestionsEnabled: false  # Analyze offline
    predicatePushdownEnabled: true
    joinReorderingEnabled: true
    verboseOutput: false
    maxAnalysisTimeMs: 5000
    targetDatabase: POSTGRESQL
```

---

## Best Practices

### 1. Use Environment-Specific Configurations

```yaml
# development.yml
query4j:
  cache:
    enabled: false
  optimizer:
    verboseOutput: true

# production.yml
query4j:
  cache:
    enabled: true
    maxSize: 10000
  optimizer:
    verboseOutput: false
```

### 2. Validate Configuration at Startup

```java
@PostConstruct
public void validateConfiguration() {
    CoreConfig coreConfig = CoreConfig.load();
    
    if (coreConfig.getMaxPredicateCount() < 10) {
        throw new IllegalStateException(
            "maxPredicateCount must be at least 10");
    }
    
    logger.info("Query4j configuration validated successfully");
}
```

### 3. Override with Environment Variables in Containers

```dockerfile
# Dockerfile
ENV QUERY4J_CACHE_ENABLED=true
ENV QUERY4J_CACHE_MAX_SIZE=15000
ENV QUERY4J_OPTIMIZER_TARGET_DATABASE=POSTGRESQL
```

### 4. Use Profiles for Spring Boot

```yaml
# application.yml
spring:
  profiles:
    active: dev

---
# Development profile
spring:
  profiles: dev
  
query4j:
  cache:
    enabled: false
  optimizer:
    verboseOutput: true

---
# Production profile
spring:
  profiles: prod

query4j:
  cache:
    enabled: true
    maxSize: 10000
  optimizer:
    verboseOutput: false
```

### 5. Document Custom Configurations

Always document non-default settings and their rationale:

```yaml
query4j:
  core:
    # Increased from default 50 to support complex reporting queries
    maxPredicateCount: 150
    
  cache:
    # Using LFU in production to prioritize frequently accessed queries
    strategy: LFU
    # Increased cache size based on memory profiling
    maxSize: 20000
```

---

## Configuration Validation

### Automatic Validation

Query4j automatically validates configuration on startup:

```java
// Invalid configuration throws ConfigurationException
try {
    CoreConfig config = CoreConfig.builder()
        .maxPredicateCount(-1)  // Invalid: negative value
        .build();
} catch (ConfigurationException e) {
    logger.error("Invalid configuration: {}", e.getMessage());
}
```

### Custom Validation

```java
public void validateCacheConfig(CacheConfig config) {
    if (config.getMaxSize() < 10) {
        throw new IllegalArgumentException(
            "Cache maxSize must be at least 10");
    }
    
    if (config.getTtl() != null && 
        config.getTtl().toSeconds() < 60) {
        logger.warn("TTL less than 60 seconds may cause excessive churn");
    }
}
```

---

## Troubleshooting

### Configuration Not Loading

**Problem:** Configuration properties not being applied.

**Solutions:**
1. Verify file location (resources directory or classpath)
2. Check file naming: `query4j.properties` or `query4j.yml`
3. Validate YAML syntax (indentation matters)
4. Check environment variable naming

### Property Override Not Working

**Problem:** Environment variable not overriding property file.

**Solutions:**
1. Verify environment variable naming convention
2. Restart application after setting env vars
3. Check environment variable is set: `echo $QUERY4J_CACHE_ENABLED`

### Performance Issues

**Problem:** Degraded performance after configuration change.

**Solutions:**
1. Review cache size settings
2. Check optimizer analysis timeout
3. Disable verbose logging in production
4. Validate predicate depth/count limits

---

## See Also

- **[Core Module](Core-Module)** - Core module usage
- **[Cache Manager](Cache-Manager)** - Cache configuration details
- **[Optimizer](Optimizer)** - Optimizer configuration options
- **[Getting Started](Getting-Started)** - Initial setup guide

For complete configuration reference, see:
- [Configuration Guide](https://github.com/query4j/dynamicquerybuilder/blob/master/docs/Configuration.md)

---

**Last Updated:** December 2024  
**Version:** 1.0.0
