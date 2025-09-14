# Query4j Configuration Guide

Query4j provides a comprehensive configuration system that supports multiple configuration sources and modules. This guide explains how to configure and use the Query4j library.

## Table of Contents
- [Overview](#overview)
- [Configuration Sources](#configuration-sources)  
- [Configuration Modules](#configuration-modules)
- [Configuration Validation and Error Handling](#configuration-validation-and-error-handling)
- [Usage Examples](#usage-examples)
- [Best Practices](#best-practices)
- [Module Cross-References](#module-cross-references)

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

**Environment Variable Naming Convention:**
- Convert property keys to uppercase: `query4j.core.maxPredicateDepth` → `QUERY4J_CORE_MAX_PREDICATE_DEPTH`
- Replace dots (`.`) with underscores (`_`)
- Use uppercase for all letters
- Boolean values: `true`, `false`
- Numeric values: plain numbers without quotes
- String values: plain text without quotes (unless containing spaces)

**Examples:**
```bash
# Core module settings
export QUERY4J_CORE_DEFAULT_QUERY_TIMEOUT_MS=45000
export QUERY4J_CORE_STRICT_FIELD_VALIDATION=false

# Cache module settings  
export QUERY4J_CACHE_MAX_SIZE=25000
export QUERY4J_CACHE_DEFAULT_REGION=production

# Optimizer module settings
export QUERY4J_OPTIMIZER_TARGET_DATABASE=POSTGRESQL
export QUERY4J_OPTIMIZER_INDEX_SELECTIVITY_THRESHOLD=0.05
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

| Property | Module | Default | Description | Valid Values | Example |
|----------|--------|---------|-------------|--------------|---------|
| `defaultQueryTimeoutMs` | core | 30000 | Default query timeout in milliseconds | ≥ 0 (0 = no timeout) | `query4j.core.defaultQueryTimeoutMs=45000` |
| `maxPredicateDepth` | core | 10 | Maximum nesting depth for logical predicates | ≥ 1 | `query4j.core.maxPredicateDepth=15` |
| `maxPredicateCount` | core | 50 | Maximum number of predicates per query | ≥ 1 | `query4j.core.maxPredicateCount=100` |
| `likePredicatesEnabled` | core | true | Enable/disable LIKE predicate support | true, false | `query4j.core.likePredicatesEnabled=false` |
| `inPredicatesEnabled` | core | true | Enable/disable IN predicate support | true, false | `query4j.core.inPredicatesEnabled=true` |
| `betweenPredicatesEnabled` | core | true | Enable/disable BETWEEN predicate support | true, false | `query4j.core.betweenPredicatesEnabled=true` |
| `nullPredicatesEnabled` | core | true | Enable/disable NULL predicate support | true, false | `query4j.core.nullPredicatesEnabled=true` |
| `maxInPredicateSize` | core | 1000 | Maximum items in IN predicates | ≥ 1 | `query4j.core.maxInPredicateSize=500` |
| `strictFieldValidation` | core | true | Enable strict field name validation | true, false | `query4j.core.strictFieldValidation=false` |
| `parameterCollisionDetection` | core | true | Detect parameter name collisions | true, false | `query4j.core.parameterCollisionDetection=true` |
| `defaultPageSize` | core | 20 | Default page size for pagination | ≥ 1 | `query4j.core.defaultPageSize=25` |
| `maxPageSize` | core | 1000 | Maximum allowed page size | ≥ defaultPageSize | `query4j.core.maxPageSize=5000` |
| `queryStatisticsEnabled` | core | true | Enable query statistics collection | true, false | `query4j.core.queryStatisticsEnabled=false` |

### Cache Configuration

Controls caching behavior and performance settings:

| Property | Module | Default | Description | Valid Values | Example |
|----------|--------|---------|-------------|--------------|---------|
| `enabled` | cache | true | Enable/disable caching globally | true, false | `query4j.cache.enabled=false` |
| `defaultTtlSeconds` | cache | 3600 | Default cache entry TTL in seconds | ≥ 0 (0 = no expiration) | `query4j.cache.defaultTtlSeconds=7200` |
| `maxSize` | cache | 10000 | Maximum cache size (number of entries) | > 0 | `query4j.cache.maxSize=50000` |
| `defaultRegion` | cache | "default" | Default cache region name | Non-empty string | `query4j.cache.defaultRegion=myapp` |
| `statisticsEnabled` | cache | true | Enable cache statistics collection | true, false | `query4j.cache.statisticsEnabled=false` |
| `maintenanceIntervalSeconds` | cache | 300 | Cache maintenance interval | ≥ 0 | `query4j.cache.maintenanceIntervalSeconds=600` |
| `keyValidationEnabled` | cache | true | Enable cache key validation | true, false | `query4j.cache.keyValidationEnabled=false` |
| `maxKeyLength` | cache | 512 | Maximum cache key length | > 0 | `query4j.cache.maxKeyLength=1024` |
| `concurrencyLevel` | cache | 16 | Cache concurrency level | > 0 | `query4j.cache.concurrencyLevel=32` |
| `autoWarmupEnabled` | cache | false | Enable automatic cache warming | true, false | `query4j.cache.autoWarmupEnabled=true` |
| `warmupSize` | cache | 100 | Number of entries for cache warming | ≥ 0 | `query4j.cache.warmupSize=250` |

### Optimizer Configuration

Controls query optimization and analysis:

| Property | Module | Default | Description | Valid Values | Example |
|----------|--------|---------|-------------|--------------|---------|
| `indexSuggestionsEnabled` | optimizer | true | Enable index suggestion analysis | true, false | `query4j.optimizer.indexSuggestionsEnabled=false` |
| `predicatePushdownEnabled` | optimizer | true | Enable predicate pushdown optimization | true, false | `query4j.optimizer.predicatePushdownEnabled=true` |
| `joinReorderingEnabled` | optimizer | true | Enable join reordering optimization | true, false | `query4j.optimizer.joinReorderingEnabled=false` |
| `indexSelectivityThreshold` | optimizer | 0.1 | Selectivity threshold for index suggestions | 0.0 - 1.0 | `query4j.optimizer.indexSelectivityThreshold=0.05` |
| `predicateReorderingThreshold` | optimizer | 0.05 | Threshold for predicate reordering | 0.0 - 1.0 | `query4j.optimizer.predicateReorderingThreshold=0.02` |
| `joinReorderingThreshold` | optimizer | 0.1 | Threshold for join reordering | 0.0 - 1.0 | `query4j.optimizer.joinReorderingThreshold=0.15` |
| `maxAnalysisTimeMs` | optimizer | 5000 | Maximum analysis time in milliseconds | ≥ 0 (0 = no timeout) | `query4j.optimizer.maxAnalysisTimeMs=10000` |
| `verboseOutput` | optimizer | false | Include detailed optimization explanations | true, false | `query4j.optimizer.verboseOutput=true` |
| `maxCompositeIndexColumns` | optimizer | 3 | Max columns in composite index suggestions | ≥ 1 | `query4j.optimizer.maxCompositeIndexColumns=5` |
| `targetDatabase` | optimizer | GENERIC | Target database type | GENERIC, POSTGRESQL, MYSQL, H2, ORACLE, SQL_SERVER | `query4j.optimizer.targetDatabase=POSTGRESQL` |

## Configuration Validation and Error Handling

Query4j performs strict validation on all configuration values to ensure system stability and prevent runtime errors.

### Validation Rules

**Core Module Constraints:**
- `maxPredicateDepth`: Must be ≥ 1
- `maxPredicateCount`: Must be ≥ 1  
- `maxInPredicateSize`: Must be ≥ 1
- `defaultPageSize`: Must be ≥ 1
- `maxPageSize`: Must be ≥ `defaultPageSize`
- `defaultQueryTimeoutMs`: Must be ≥ 0 (0 means no timeout)

**Cache Module Constraints:**
- `maxSize`: Must be > 0
- `defaultTtlSeconds`: Must be ≥ 0 (0 means no expiration)
- `defaultRegion`: Must not be null or empty
- `maxKeyLength`: Must be > 0
- `concurrencyLevel`: Must be > 0
- `maintenanceIntervalSeconds`: Must be ≥ 0
- `warmupSize`: Must be ≥ 0

**Optimizer Module Constraints:**
- `indexSelectivityThreshold`: Must be 0.0 ≤ value ≤ 1.0
- `predicateReorderingThreshold`: Must be 0.0 ≤ value ≤ 1.0
- `joinReorderingThreshold`: Must be 0.0 ≤ value ≤ 1.0
- `maxAnalysisTimeMs`: Must be ≥ 0 (0 means no timeout)
- `maxCompositeIndexColumns`: Must be ≥ 1
- `targetDatabase`: Must be a valid DatabaseType enum value

### Error Behaviors

**Invalid Configuration Values:**
```java
// Throws IllegalStateException during validation
CoreConfig invalidConfig = CoreConfig.builder()
    .maxPredicateDepth(0)  // Invalid: must be ≥ 1
    .build();

try {
    invalidConfig.validate();
} catch (IllegalStateException e) {
    System.err.println("Config error: " + e.getMessage());
    // Output: "maxPredicateDepth must be at least 1, got: 0"
}
```

**Configuration Loading Errors:**
```java
try {
    Query4jConfig config = Query4jConfigurationFactory.loadFromFile("invalid.properties");
} catch (DynamicQueryException e) {
    System.err.println("Failed to load configuration: " + e.getMessage());
    // Possible causes: file not found, invalid format, constraint violations
}
```

**Environment Variable Parsing Errors:**
- Invalid boolean values (not "true"/"false") use default values with warning
- Invalid numeric values cause configuration loading to fail with detailed error message
- Invalid enum values (e.g., unsupported database type) cause validation failure

**Runtime Validation:**
- Configuration validation occurs at builder `.build()` time
- Factory methods validate immediately when loading from external sources
- Invalid configurations prevent application startup with clear error messages

### Validation Examples

**Successful Validation:**
```java
Query4jConfig config = Query4jConfigurationFactory.builder()
    .core(CoreConfig.builder()
        .maxPredicateDepth(15)     // Valid: ≥ 1
        .defaultPageSize(20)       // Valid: ≥ 1  
        .maxPageSize(1000)         // Valid: ≥ defaultPageSize
        .build())
    .cache(CacheConfig.builder()
        .maxSize(10000L)           // Valid: > 0
        .defaultRegion("prod")     // Valid: non-empty
        .build())
    .build();

config.validate(); // Passes successfully
```

**Validation Failure Example:**
```java
try {
    CacheConfig badConfig = CacheConfig.builder()
        .maxSize(-1)               // Invalid: must be > 0
        .defaultRegion("")         // Invalid: must be non-empty
        .concurrencyLevel(0)       // Invalid: must be > 0
        .build();
} catch (IllegalStateException e) {
    // Multiple validation errors reported:
    // "maxSize must be positive, got: -1"
    // "defaultRegion must not be null or empty" 
    // "concurrencyLevel must be positive, got: 0"
}
```

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

**Recommended Production Configuration:**
```yaml
query4j:
  core:
    defaultQueryTimeoutMs: 10000      # Conservative timeout for production SLA
    maxPredicateDepth: 12             # Allow moderate complexity
    maxPredicateCount: 75             # Balance flexibility vs performance
    queryStatisticsEnabled: true      # Enable monitoring and metrics
    strictFieldValidation: true       # Prevent injection attacks
    
  cache:
    enabled: true
    maxSize: 50000                    # Size for production load
    defaultTtlSeconds: 1800           # 30-minute TTL for data freshness
    statisticsEnabled: true           # Enable cache metrics
    maintenanceIntervalSeconds: 600   # Cleanup every 10 minutes
    
  optimizer:
    indexSuggestionsEnabled: true     # Help with performance tuning
    verboseOutput: false              # Reduce log noise in production
    maxAnalysisTimeMs: 3000           # Quick analysis to avoid delays
    targetDatabase: POSTGRESQL        # Set to your actual database
```

**Production Tuning Guidelines:**

*Cache Sizing:*
- **Small applications (< 1000 users):** `maxSize: 5000-10000`
- **Medium applications (1000-10000 users):** `maxSize: 25000-50000` 
- **Large applications (> 10000 users):** `maxSize: 100000+`
- Monitor cache hit rates; target > 80% for good performance

*Query Complexity Limits:*
- **Conservative:** `maxPredicateDepth: 8`, `maxPredicateCount: 30`
- **Balanced:** `maxPredicateDepth: 12`, `maxPredicateCount: 75`
- **Flexible:** `maxPredicateDepth: 20`, `maxPredicateCount: 150`

*Timeout Settings:*
- **High-performance APIs:** `defaultQueryTimeoutMs: 5000`
- **Standard applications:** `defaultQueryTimeoutMs: 10000-15000`
- **Batch processing:** `defaultQueryTimeoutMs: 30000+`

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

## Module Cross-References

For detailed module-specific information, see the individual module documentation:

### Core Module
- **[Core Module README](../core/README.md)** - Detailed core functionality and API reference
- **Key Classes:** `CoreConfig`, `Query4jConfig`, `QueryBuilder`
- **Configuration Focus:** Query building behavior, validation rules, pagination settings

### Cache Module  
- **[Cache Module README](../cache/README.md)** - Caching implementation and performance tuning
- **Key Classes:** `CacheConfig`, `CacheManager`, `ConfigurableCacheManager`
- **Configuration Focus:** Cache sizing, TTL settings, maintenance and statistics

### Optimizer Module
- **[Optimizer Module README](../optimizer/README.md)** - Query optimization and performance analysis  
- **Key Classes:** `OptimizerConfig`, `QueryOptimizer`, `OptimizationSuggestion`
- **Configuration Focus:** Optimization strategies, analysis thresholds, database-specific tuning

### Integration Examples
- **[Examples Module](../examples/)** - Real-world configuration scenarios and integration patterns
- **[Test Module](../test/)** - Configuration testing examples and validation patterns

### Additional Resources
- **[API Guide](API_GUIDE.md)** - Comprehensive API documentation
- **[Contributing Guide](../CONTRIBUTING.md)** - Development and testing guidelines
- **[Root README](../README.md)** - Project overview and quick start guide

---

This configuration system provides the flexibility to adapt Query4j to various deployment scenarios while maintaining sensible defaults for quick setup.