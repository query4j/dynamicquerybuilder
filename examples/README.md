# Query4j Examples

This module contains comprehensive examples and tutorials demonstrating the capabilities of the Query4j Dynamic Query Builder library. The examples range from basic usage patterns to advanced production-scale consumer applications.

## 🚀 New: Consumer Applications

**Three comprehensive example applications demonstrating real-world Query4j usage patterns:**

### BatchProcessingApp
Large-scale data processing with pagination, fault tolerance, and performance monitoring.
```bash
./gradlew :examples:run -PmainClass=com.github.query4j.examples.ConsumerAppsDemo -Pargs="batch"
```

### AsyncQueryApp  
Concurrent query execution patterns with CompletableFuture and thread-safe operations.
```bash
./gradlew :examples:run -PmainClass=com.github.query4j.examples.ConsumerAppsDemo -Pargs="async"
```

### ComplexJoinsApp
Advanced multi-table joins, dynamic filtering, and hierarchical data mapping.
```bash
./gradlew :examples:run -PmainClass=com.github.query4j.examples.ConsumerAppsDemo -Pargs="joins"
```

**📖 [Complete Consumer Apps Documentation](README-CONSUMER-APPS.md)**

---

## Spring Boot Integration Examples

This module also demonstrates how to integrate Query4j dynamic query builder with Spring Boot applications. It provides comprehensive examples of using Query4j's core, cache, and optimizer modules within a Spring Boot test harness.

## Overview

The integration tests in this module showcase:

- **Spring Boot Test Configuration**: Setting up Query4j components as Spring beans
- **JPA Entity Integration**: Using Query4j with JPA entities and Spring Data repositories  
- **Dynamic Query Service Layer**: Building service classes that leverage Query4j for dynamic querying
- **Cache Integration**: Using Query4j's cache manager within Spring context
- **Query Optimization**: Integrating the optimizer for performance analysis
- **Transactional Support**: Working with Spring's transaction management

## Project Structure

```
examples/
├── src/
│   └── test/
│       ├── java/com/github/query4j/examples/
│       │   ├── config/
│       │   │   ├── TestApplication.java           # Spring Boot test application
│       │   │   └── Query4jTestConfiguration.java  # Query4j beans configuration
│       │   ├── entity/
│       │   │   ├── Customer.java                  # JPA entity with Query4j integration
│       │   │   ├── Order.java                     # Related entity for join examples
│       │   │   └── OrderItem.java                 # Many-to-many relationship example
│       │   ├── repository/
│       │   │   ├── CustomerRepository.java        # Spring Data JPA repository
│       │   │   └── OrderRepository.java           # Additional repository
│       │   ├── service/
│       │   │   └── DynamicQueryService.java       # Service layer using Query4j
│       │   └── integration/
│       │       ├── SpringBootIntegrationTest.java # Full Spring Boot integration tests
│       │       ├── BasicDynamicQueryTest.java     # Basic functionality tests
│       │       └── SimpleDynamicQueryTest.java    # Simple component tests
│       └── resources/
│           └── application-test.properties        # Spring Boot test configuration
└── build.gradle                                   # Dependencies and Spring Boot setup
```

## Dependencies

The integration adds these key dependencies to the base Query4j modules:

```gradle
// Spring Boot dependencies for integration testing
testImplementation 'org.springframework.boot:spring-boot-starter'
testImplementation 'org.springframework.boot:spring-boot-starter-data-jpa'
testImplementation 'org.springframework.boot:spring-boot-starter-jdbc'
testImplementation 'org.springframework.boot:spring-boot-starter-test'

// H2 Database for embedded testing
testRuntimeOnly 'com.h2database:h2:2.2.224'

// Additional test utilities
testImplementation 'org.testcontainers:junit-jupiter:1.19.1'
```

## Configuration

### Spring Boot Test Configuration

The `Query4jTestConfiguration` class demonstrates how to configure Query4j components as Spring beans:

```java
@TestConfiguration
public class Query4jTestConfiguration {
    
    @Bean
    @Primary
    public CoreConfig coreConfig() {
        return CoreConfig.developmentConfig();
    }
    
    @Bean
    @Primary  
    public CacheManager cacheManager() {
        return CaffeineCacheManager.create(100L, 300L);
    }
    
    @Bean
    @Primary
    public QueryOptimizer queryOptimizer() {
        OptimizerConfig config = OptimizerConfig.builder()
            .indexSuggestionsEnabled(true)
            .predicatePushdownEnabled(true)
            .joinReorderingEnabled(true)
            .verboseOutput(true)
            .maxAnalysisTimeMs(5000L)
            .build();
        
        return new QueryOptimizerImpl(config);
    }
}
```

### Application Properties

The `application-test.properties` file configures H2 database and logging:

```properties
# H2 Database Configuration
spring.datasource.url=jdbc:h2:mem:query4j_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA Configuration
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# Query4j Configuration
query4j.core.maxPredicateDepth=20
query4j.core.queryStatisticsEnabled=true
query4j.cache.enabled=true
query4j.optimizer.verboseOutput=true
```

## Usage Examples

### Service Layer Integration

The `DynamicQueryService` shows how to use Query4j in a Spring service:

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DynamicQueryService {
    
    private final JdbcTemplate jdbcTemplate;
    private final CacheManager cacheManager;
    private final QueryOptimizer queryOptimizer;
    
    public List<Customer> findCustomersWithDynamicQuery(String region, Boolean active, 
                                                       Double minCreditLimit, int page, int size) {
        // Build cache key
        String cacheKey = String.format("customers:%s:%s:%s:%d:%d", 
            region, active, minCreditLimit, page, size);
        
        // Try cache first
        List<Customer> cached = (List<Customer>) cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // Build dynamic query
        DynamicQueryBuilder<Customer> builder = new DynamicQueryBuilder<>(Customer.class);
        
        if (region != null) {
            builder = (DynamicQueryBuilder<Customer>) builder.where("region", "=", region);
        }
        if (active != null) {
            builder = (DynamicQueryBuilder<Customer>) builder.and().where("active", "=", active);
        }
        if (minCreditLimit != null) {
            builder = (DynamicQueryBuilder<Customer>) builder.and().where("creditLimit", ">=", minCreditLimit);
        }
        
        // Add pagination
        builder = (DynamicQueryBuilder<Customer>) builder.page(page, size);
        
        // Execute and cache results
        List<Customer> results = executeQuery(builder);
        cacheManager.put(cacheKey, results, 600L);
        
        return results;
    }
}
```

### Integration Testing

The integration tests demonstrate various scenarios:

```java
@SpringBootTest(classes = {TestApplication.class, Query4jTestConfiguration.class})
@ActiveProfiles("test")
@DisplayName("Spring Boot Integration Tests for Query4j")
class SpringBootIntegrationTest {
    
    @Autowired
    private DynamicQueryService dynamicQueryService;
    
    @Test
    @DisplayName("should execute dynamic query with single filter condition")
    @Transactional(readOnly = true)
    void shouldExecuteDynamicQueryWithSingleFilter() {
        List<Customer> northCustomers = dynamicQueryService.findCustomersWithDynamicQuery(
            "North", null, null, 1, 10
        );
        
        assertNotNull(northCustomers);
        assertTrue(northCustomers.stream().allMatch(c -> "North".equals(c.getRegion())));
    }
    
    @Test
    @DisplayName("should cache query results and show cache hits")
    @Transactional(readOnly = true)
    void shouldCacheQueryResultsAndShowCacheHits() {
        // Execute the same query twice
        dynamicQueryService.findCustomersWithDynamicQuery("South", true, null, 1, 10);
        dynamicQueryService.findCustomersWithDynamicQuery("South", true, null, 1, 10);
        
        // Verify cache statistics show at least one hit
        CacheStatistics stats = dynamicQueryService.getCacheStatistics();
        assertTrue(stats.getHitCount() > 0);
    }
}
```

## Running the Tests

### Run All Integration Tests

```bash
./gradlew :examples:test
```

### Run Specific Test Classes

```bash
# Run basic functionality tests (no Spring Boot dependencies)
./gradlew :examples:test --tests "BasicDynamicQueryTest"

# Run Spring Boot integration tests  
./gradlew :examples:test --tests "SpringBootIntegrationTest"

# Run simple component tests
./gradlew :examples:test --tests "SimpleDynamicQueryTest"
```

### Run with Verbose Output

```bash
./gradlew :examples:test --info
```

## Test Categories

### BasicDynamicQueryTest
- **Purpose**: Tests core Query4j functionality without Spring Boot dependencies
- **Coverage**: Query building, SQL generation, cache operations, optimizer integration
- **Status**: ✅ Compiles and runs independently

### SimpleDynamicQueryTest  
- **Purpose**: Tests Query4j components with minimal Spring configuration
- **Coverage**: Bean wiring, basic integration
- **Status**: ⚠️ Requires Spring Boot context fixes

### SpringBootIntegrationTest
- **Purpose**: Full end-to-end integration testing with Spring Boot
- **Coverage**: Service layer, transactions, JPA integration, cache statistics
- **Status**: ⚠️ Requires Spring Boot context and data setup fixes

## Implementation Notes

### Current Status

The integration demonstrates the complete architecture for using Query4j with Spring Boot:

1. ✅ **Dependencies configured** - Spring Boot, JPA, H2 database
2. ✅ **Bean configuration** - Query4j components as Spring beans  
3. ✅ **Entity modeling** - JPA entities with proper relationships
4. ✅ **Service layer** - Integration of Query4j with Spring services
5. ✅ **Basic tests** - Core functionality verification
6. ⚠️ **Spring context** - Some configuration issues to resolve
7. ⚠️ **Data setup** - Test data initialization needs refinement

### Known Issues

1. **Spring Boot Context Loading**: Some tests fail due to autoconfiguration package detection
2. **Data Initialization**: SQL script loading has syntax compatibility issues with H2
3. **Bean Wiring**: Some dependency injection issues in complex integration scenarios

### Next Steps

1. Fix Spring Boot autoconfiguration issues
2. Resolve test data setup for reliable integration testing  
3. Add more comprehensive query scenarios
4. Add performance benchmarking integration
5. Document production deployment considerations

## Documentation

For comprehensive documentation and API references:

- **[Main README](../README.md)** - Library overview, installation, and quick start
- **[Quickstart Tutorial](../QUICKSTART.md)** - Learn the basics in 15 minutes
- **[Advanced Usage Tutorial](../ADVANCED.md)** - Master complex queries and optimization
- **[API Reference Guide](../docs/API_GUIDE.md)** - Complete API documentation
- **[Configuration Guide](../docs/Configuration.md)** - Configuration options and best practices
- **[Consumer Apps Documentation](README-CONSUMER-APPS.md)** - Detailed consumer application examples
- **[JavaDoc Generation](../docs/JAVADOC_GENERATION.md)** - Generate and access API documentation

### Example Applications

This module includes three comprehensive consumer applications:

1. **[BatchProcessingApp](README-CONSUMER-APPS.md#batchprocessingapp)** - Large-scale data processing with pagination
2. **[AsyncQueryApp](README-CONSUMER-APPS.md#asyncqueryapp)** - Concurrent query execution patterns
3. **[ComplexJoinsApp](README-CONSUMER-APPS.md#complexjoinsapp)** - Advanced multi-table joins

## Related Modules

- **[Core Module](../core/README.md)** - Query building and execution fundamentals
- **[Cache Module](../cache/README.md)** - Result caching for performance
- **[Optimizer Module](../optimizer/README.md)** - Query optimization strategies

## Contributing

See the [Contributing Guide](../CONTRIBUTING.md) for guidelines on:
- Adding new examples
- Testing requirements
- Documentation standards

## Support

- **Issues**: [GitHub Issues](https://github.com/query4j/dynamicquerybuilder/issues)
- **Discussions**: [GitHub Discussions](https://github.com/query4j/dynamicquerybuilder/discussions)

## Benefits of Spring Boot Integration

This integration provides several advantages:

1. **Dependency Injection**: Query4j components managed by Spring container
2. **Configuration Management**: Externalized configuration through application properties
3. **Transaction Management**: Automatic transaction handling with Spring
4. **Testing Support**: Comprehensive test harness with embedded database
5. **Monitoring Integration**: Cache statistics and performance metrics
6. **Production Ready**: Ready for deployment in Spring Boot applications

## Architecture Benefits

The integration demonstrates Query4j's architecture strengths:

- **Immutability**: Thread-safe query builders work perfectly with Spring's singleton beans
- **Modularity**: Core, cache, and optimizer modules integrate independently
- **Performance**: Sub-millisecond query building with caching support
- **Flexibility**: Dynamic queries adapt to runtime conditions
- **Testability**: Comprehensive test coverage with real database execution

This Spring Boot integration shows how Query4j can be seamlessly incorporated into enterprise applications while maintaining its performance and flexibility advantages.