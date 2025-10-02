# Query4j Example Consumer Applications

This directory contains three comprehensive example applications demonstrating advanced usage patterns of the Query4j Dynamic Query Builder library. These applications showcase real-world scenarios and best practices for using the library in production environments.

## Applications Overview

### 1. BatchProcessingApp
**Location:** `src/main/java/com/github/query4j/examples/batch/BatchProcessingApp.java`

Demonstrates efficient batch processing of large datasets with:
- **Pagination-based processing** for memory efficiency
- **Fault tolerance** with retry mechanisms and error recovery
- **Progress tracking** and performance monitoring
- **Resource optimization** to prevent system overload
- **Comprehensive logging** for debugging and monitoring

**Key Features:**
- Configurable batch sizes (default 1000 records)
- Exponential backoff retry logic with up to 3 attempts
- Real-time progress reporting with throughput calculations
- Graceful handling of empty datasets
- Statistics tracking and final performance reports

**Use Cases:**
- Data migration and transformation jobs
- User cleanup and maintenance tasks
- Large-scale analytics and reporting
- System maintenance operations

### 2. AsyncQueryApp
**Location:** `src/main/java/com/github/query4j/examples/async/AsyncQueryApp.java`

Demonstrates asynchronous query execution patterns with:
- **Concurrent query execution** using CompletableFuture
- **Thread-safe operations** with proper resource management
- **Pipeline processing** with chained async operations
- **Result aggregation** from multiple concurrent queries
- **Error handling** in asynchronous environments

**Key Features:**
- Configurable thread pool sizing
- Query execution statistics and monitoring
- Async pipeline processing with stage chaining
- Graceful shutdown and resource cleanup
- Comprehensive error handling with timeouts

**Use Cases:**
- Dashboard data aggregation
- Parallel report generation
- Real-time analytics pipelines
- Concurrent data processing workflows

### 3. ComplexJoinsApp
**Location:** `src/main/java/com/github/query4j/examples/joins/ComplexJoinsApp.java`

Demonstrates advanced querying with complex joins:
- **Multi-table joins** with deep relationship navigation
- **Dynamic query building** based on runtime parameters
- **Hierarchical data mapping** from joined results
- **Advanced aggregations** across multiple tables
- **Correlated subqueries** and EXISTS clauses

**Key Features:**
- Customer order analysis with product details
- Dynamic filtering API patterns
- Sales reporting across multiple dimensions
- Inventory management with cross-table analytics
- Performance-optimized complex queries

**Use Cases:**
- E-commerce analytics and reporting
- Customer relationship management
- Financial reporting and auditing
- Business intelligence applications

## Getting Started

### Prerequisites
- Java 17 or higher
- Gradle build system
- Query4j library dependencies (automatically managed)

### Running the Applications

#### 1. Batch Processing Example
```bash
# Run with default settings (1000 records per batch)
./gradlew :examples:run -PmainClass=com.github.query4j.examples.batch.BatchProcessingApp

# Or run with custom batch size (programmatic configuration)
java -cp examples/build/libs/examples.jar com.github.query4j.examples.batch.BatchProcessingApp
```

#### 2. Async Query Example
```bash
# Run with default thread pool (CPU cores * 2)
./gradlew :examples:run -PmainClass=com.github.query4j.examples.async.AsyncQueryApp

# Monitor async execution patterns
java -cp examples/build/libs/examples.jar com.github.query4j.examples.async.AsyncQueryApp
```

#### 3. Complex Joins Example
```bash
# Run complex join demonstrations
./gradlew :examples:run -PmainClass=com.github.query4j.examples.joins.ComplexJoinsApp

# Explore advanced querying patterns
java -cp examples/build/libs/examples.jar com.github.query4j.examples.joins.ComplexJoinsApp
```

### Configuration Options

Each application can be configured through constructor parameters or environment variables:

#### BatchProcessingApp Configuration
```java
// Default batch size (1000)
BatchProcessingApp app = new BatchProcessingApp();

// Custom batch size
BatchProcessingApp app = new BatchProcessingApp(500);
```

#### AsyncQueryApp Configuration
```java
// Default thread pool size (CPU cores * 2)
AsyncQueryApp app = new AsyncQueryApp();

// Custom thread pool size
AsyncQueryApp app = new AsyncQueryApp(4);
```

#### ComplexJoinsApp Configuration
The ComplexJoinsApp demonstrates various filter combinations through its dynamic API methods.

## Application Architecture

### Common Patterns

All applications follow these architectural patterns:

1. **Builder Pattern Usage**
   - Immutable query builders for thread safety
   - Fluent API for readable query construction
   - Method chaining for complex query building

2. **Error Handling**
   - Custom exception hierarchy usage
   - Graceful degradation on failures
   - Comprehensive logging and monitoring

3. **Resource Management**
   - Proper connection handling
   - Memory-efficient pagination
   - Thread pool management and cleanup

4. **Performance Optimization**
   - Batch processing to minimize database round trips
   - Connection pooling and reuse
   - Query result caching where appropriate

### Integration Patterns

The applications demonstrate various integration patterns:

- **Spring Boot Integration**: See test classes for Spring configuration examples
- **JPA Entity Mapping**: Uses realistic domain models (User, Order, Product)
- **REST API Patterns**: ComplexJoinsApp shows dynamic filtering for web APIs
- **Monitoring Integration**: Built-in statistics and metrics collection

## Testing

### Running Tests
```bash
# Run all example application tests
./gradlew :examples:test

# Run specific application tests
./gradlew :examples:test --tests "*BatchProcessingAppTest*"
./gradlew :examples:test --tests "*AsyncQueryAppTest*"
./gradlew :examples:test --tests "*ComplexJoinsAppTest*"
```

### Test Coverage
- Unit tests for core functionality
- Integration tests with H2 in-memory database
- Performance tests for large datasets
- Concurrent execution tests for thread safety

## Performance Considerations

### BatchProcessingApp
- **Memory Usage**: O(batch_size) memory consumption
- **Throughput**: Optimized for high-volume processing
- **Scalability**: Horizontal scaling through multiple instances

### AsyncQueryApp
- **Concurrency**: Thread pool sizing based on CPU cores and I/O patterns
- **Resource Usage**: Bounded thread pools prevent resource exhaustion
- **Latency**: Parallel execution reduces overall response times

### ComplexJoinsApp
- **Query Optimization**: Efficient join ordering and indexing
- **Result Caching**: Caching strategies for repeated queries
- **Database Load**: Connection pooling and query batching

## Production Deployment

### Configuration Management
```properties
# application.properties example
query4j.batch.size=1000
query4j.async.threadPool.size=8
query4j.cache.enabled=true
query4j.monitoring.enabled=true
```

### Monitoring and Alerting
- Built-in statistics collection
- Integration with APM tools (Micrometer, Prometheus)
- Health check endpoints
- Performance metrics tracking

### Scaling Considerations
- Database connection pooling configuration
- Memory allocation for batch processing
- Thread pool sizing for async operations
- Caching strategies for complex queries

## Troubleshooting

### Common Issues

1. **OutOfMemoryError in BatchProcessingApp**
   - Reduce batch size
   - Increase JVM heap size
   - Check for memory leaks in processing logic

2. **Timeout issues in AsyncQueryApp**
   - Increase query timeout values
   - Check database connection pool settings
   - Monitor thread pool saturation

3. **Slow queries in ComplexJoinsApp**
   - Analyze query execution plans
   - Add database indexes
   - Optimize join conditions

### Debug Mode
Enable debug logging for detailed query information:
```properties
logging.level.com.github.query4j=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

## Contributing

To add new example applications:

1. Follow the established package structure
2. Include comprehensive JavaDoc documentation
3. Add corresponding integration tests
4. Update this README with usage instructions
5. Ensure examples follow Query4j best practices

## Related Documentation

- [Query4j Core Documentation](../core/README.md)
- [Query4j Caching Guide](../cache/README.md)
- [Query4j Optimization Guide](../optimizer/README.md)
- [API Reference](../docs/api-reference.md)

## License

These example applications are part of the Query4j project and are licensed under the same terms as the main library.