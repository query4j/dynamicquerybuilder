# Query4j Configuration Guide

Query4j provides a comprehensive configuration system that supports multiple configuration sources and modules. This guide explains how to configure and use the Query4j library.

## Table of Contents
- [Overview](#overview)
- [Configuration Sources](#configuration-sources)  
- [Configuration Modules](#configuration-modules)
- [Usage Examples](#usage-examples)
- [Best Practices](#best-practices)

## Overview

Query4j uses an auto-configuration system that loads settings from multiple sources with priority-based overrides:

1. **Environment Variables** (highest priority)
2. **System Properties**
3. **YAML Files**
4. **Properties Files**
5. **Default Values** (lowest priority)

## Configuration Sources

### Properties Files

Create a `query4j.properties` file in your project root or resources directory:

```properties
# Core Configuration
query4j.core.defaultQueryTimeoutMs=30000
query4j.core.maxPredicateDepth=10
query4j.core.maxPredicateCount=50
query4j.core.strictFieldValidation=true
query4j.core.queryStatisticsEnabled=true

# Cache Configuration
query4j.cache.enabled=true
query4j.cache.defaultTtlSeconds=3600
query4j.cache.maxSize=10000
query4j.cache.defaultRegion=default

# Optimizer Configuration
query4j.optimizer.indexSuggestionsEnabled=true
query4j.optimizer.predicatePushdownEnabled=true
query4j.optimizer.verboseOutput=false
```

### YAML Files

Alternatively, use a `query4j.yml` file:

```yaml
query4j:
  core:
    defaultQueryTimeoutMs: 30000
    maxPredicateDepth: 10
    maxPredicateCount: 50
    strictFieldValidation: true
    queryStatisticsEnabled: true
  
  cache:
    enabled: true
    defaultTtlSeconds: 3600
    maxSize: 10000
    defaultRegion: "default"
  
  optimizer:
    indexSuggestionsEnabled: true
    predicatePushdownEnabled: true
    verboseOutput: false
```

### Environment Variables

Override any configuration property using environment variables:

```bash
export QUERY4J_CORE_MAX_PREDICATE_DEPTH=15
export QUERY4J_CACHE_ENABLED=false
export QUERY4J_OPTIMIZER_VERBOSE_OUTPUT=true
```

### System Properties

Set configuration via JVM system properties:

```bash
java -Dquery4j.core.maxPredicateDepth=15 \
     -Dquery4j.cache.enabled=false \
     -Dquery4j.optimizer.verboseOutput=true \
     MyApplication
```

## Configuration Modules

### Core Configuration

Controls query building behavior and validation rules:

| Property | Default | Description |
|----------|---------|-------------|
| `defaultQueryTimeoutMs` | 30000 | Default query timeout in milliseconds |
| `maxPredicateDepth` | 10 | Maximum nesting depth for logical predicates |
| `maxPredicateCount` | 50 | Maximum number of predicates per query |
| `likePredicatesEnabled` | true | Enable/disable LIKE predicate support |
| `inPredicatesEnabled` | true | Enable/disable IN predicate support |
| `betweenPredicatesEnabled` | true | Enable/disable BETWEEN predicate support |
| `nullPredicatesEnabled` | true | Enable/disable NULL predicate support |
| `maxInPredicateSize` | 1000 | Maximum items in IN predicates |
| `strictFieldValidation` | true | Enable strict field name validation |
| `parameterCollisionDetection` | true | Detect parameter name collisions |
| `defaultPageSize` | 20 | Default page size for pagination |
| `maxPageSize` | 1000 | Maximum allowed page size |
| `queryStatisticsEnabled` | true | Enable query statistics collection |

### Cache Configuration

Controls caching behavior and performance settings:

| Property | Default | Description |
|----------|---------|-------------|
| `enabled` | true | Enable/disable caching globally |
| `defaultTtlSeconds` | 3600 | Default cache entry TTL in seconds |
| `maxSize` | 10000 | Maximum cache size (number of entries) |
| `defaultRegion` | "default" | Default cache region name |
| `statisticsEnabled` | true | Enable cache statistics collection |
| `maintenanceIntervalSeconds` | 300 | Cache maintenance interval |
| `keyValidationEnabled` | true | Enable cache key validation |
| `maxKeyLength` | 512 | Maximum cache key length |
| `concurrencyLevel` | 16 | Cache concurrency level |
| `autoWarmupEnabled` | false | Enable automatic cache warming |
| `warmupSize` | 100 | Number of entries for cache warming |

### Optimizer Configuration

Controls query optimization and analysis:

| Property | Default | Description |
|----------|---------|-------------|
| `indexSuggestionsEnabled` | true | Enable index suggestion analysis |
| `predicatePushdownEnabled` | true | Enable predicate pushdown optimization |
| `joinReorderingEnabled` | true | Enable join reordering optimization |
| `indexSelectivityThreshold` | 0.1 | Selectivity threshold for index suggestions |
| `predicateReorderingThreshold` | 0.05 | Threshold for predicate reordering |
| `joinReorderingThreshold` | 0.1 | Threshold for join reordering |
| `maxAnalysisTimeMs` | 5000 | Maximum analysis time in milliseconds |
| `verboseOutput` | false | Include detailed optimization explanations |
| `maxCompositeIndexColumns` | 3 | Max columns in composite index suggestions |
| `targetDatabase` | GENERIC | Target database type (GENERIC, POSTGRESQL, MYSQL, H2, ORACLE, SQL_SERVER) |

## Usage Examples

### Basic Auto-Configuration

```java
import com.github.query4j.core.config.Query4jConfigurationFactory;

// Get default auto-configured instance
Query4jConfig config = Query4jConfigurationFactory.getDefault();

// Use the configuration
CoreConfig coreConfig = config.getCore();
CacheConfig cacheConfig = config.getCache();
```

### Load from Specific File

```java
// Load from specific configuration file
Query4jConfig config = Query4jConfigurationFactory.loadFromFile("myapp.properties");
```

### Programmatic Configuration

```java
// Create custom configuration programmatically
Query4jConfig config = Query4jConfigurationFactory.builder()
    .core(CoreConfig.builder()
        .maxPredicateDepth(15)
        .queryStatisticsEnabled(false)
        .build())
    .cache(CacheConfig.builder()
        .maxSize(50_000L)
        .defaultTtlSeconds(7200L)
        .build())
    .build();

// Set as default for the application
Query4jConfigurationFactory.setDefault(config);
```

### Using Configured Cache Managers

```java
import com.github.query4j.cache.config.ConfigurableCacheManager;

// Create cache manager with auto-configuration
CacheManager cacheManager = ConfigurableCacheManager.create();

// Create named cache manager with custom config
CacheConfig customConfig = CacheConfig.builder()
    .maxSize(5000L)
    .defaultTtlSeconds(1800L)
    .build();
CacheManager namedCache = ConfigurableCacheManager.forRegion("myregion", customConfig);
```

### Using Configured Optimizer

```java
import com.github.query4j.optimizer.config.OptimizerConfigurationFactory;

// Get auto-configured optimizer config
OptimizerConfig optimizerConfig = OptimizerConfigurationFactory.getDefault();

// Create custom optimizer configuration
OptimizerConfig customOptimizer = OptimizerConfigurationFactory.builder()
    .verboseOutput(true)
    .maxAnalysisTimeMs(10_000L)
    .targetDatabase(OptimizerConfig.DatabaseType.POSTGRESQL)
    .build();
```

### Predefined Configuration Profiles

Query4j provides several predefined configuration profiles:

```java
// High-performance configuration (optimized for speed)
Query4jConfig highPerf = Query4jConfigurationFactory.builder()
    .core(CoreConfig.highPerformanceConfig())
    .cache(CacheConfig.highPerformanceConfig())
    .build();

// Development configuration (optimized for debugging)
Query4jConfig dev = Query4jConfigurationFactory.builder()
    .core(CoreConfig.developmentConfig())
    .cache(CacheConfig.developmentConfig())
    .build();

// Minimal configuration (core functionality only)
Query4jConfig minimal = Query4jConfigurationFactory.builder()
    .core(CoreConfig.defaultConfig())
    .cache(CacheConfig.disabledConfig())
    .build();
```

## Best Practices

### Production Deployment

1. **Use external configuration files** rather than hardcoded values
2. **Enable statistics and monitoring** in production environments
3. **Set appropriate timeouts** based on your application's SLA requirements
4. **Configure cache sizing** based on available memory and usage patterns

```yaml
query4j:
  core:
    defaultQueryTimeoutMs: 10000  # Conservative timeout
    queryStatisticsEnabled: true   # Enable monitoring
  cache:
    maxSize: 50000                # Size for production load
    defaultTtlSeconds: 1800       # 30-minute TTL
    statisticsEnabled: true       # Enable cache metrics
```

### Development Environment

1. **Enable verbose output** for debugging optimization suggestions
2. **Use shorter timeouts** for faster feedback during development
3. **Reduce cache sizes** to conserve development machine resources

```yaml
query4j:
  core:
    defaultQueryTimeoutMs: 60000  # Longer for debugging
    maxPredicateDepth: 15         # Allow more complex queries
  cache:
    maxSize: 1000                 # Smaller cache for dev
    defaultTtlSeconds: 300        # Shorter TTL
  optimizer:
    verboseOutput: true           # Detailed optimization info
```

### Testing Environment

1. **Disable caching** to ensure test repeatability
2. **Use minimal configuration** to reduce external dependencies
3. **Enable strict validation** to catch issues early

```yaml
query4j:
  core:
    strictFieldValidation: true
    parameterCollisionDetection: true
  cache:
    enabled: false                # Disable for tests
  optimizer:
    indexSuggestionsEnabled: false
    predicatePushdownEnabled: false
```

### Configuration Validation

Always validate your configuration:

```java
try {
    Query4jConfig config = Query4jConfigurationFactory.getDefault();
    config.validate(); // Throws exception if invalid
} catch (IllegalStateException e) {
    logger.error("Invalid configuration: " + e.getMessage());
    // Handle configuration error
}
```

### Environment-Specific Overrides

Use environment variables for environment-specific settings:

```bash
# Production
export QUERY4J_CACHE_MAX_SIZE=100000
export QUERY4J_CORE_DEFAULT_QUERY_TIMEOUT_MS=5000

# Development  
export QUERY4J_CACHE_MAX_SIZE=1000
export QUERY4J_OPTIMIZER_VERBOSE_OUTPUT=true
```

This configuration system provides the flexibility to adapt Query4j to various deployment scenarios while maintaining sensible defaults for quick setup.