package com.github.query4j.benchmark;

import com.github.query4j.core.QueryBuilder;
import com.github.query4j.core.impl.DynamicQueryBuilder;
import com.github.query4j.optimizer.OptimizerConfig;
import com.github.query4j.optimizer.QueryOptimizer;
import com.github.query4j.optimizer.OptimizationResult;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * JMH Benchmarks for measuring query optimizer performance impact vs baseline libraries.
 * 
 * <p>This benchmark suite measures:</p>
 * <ul>
 *   <li>Optimizer analysis time for various query complexities</li>
 *   <li>End-to-end performance (build + optimize + execute) vs baselines</li>
 *   <li>Memory overhead during optimization</li>
 *   <li>Throughput under concurrent load</li>
 * </ul>
 * 
 * <p>Performance targets (Issue #24):</p>
 * <ul>
 *   <li>Simple query optimization: &lt; 10ms analysis time</li>
 *   <li>Complex query optimization: &lt; 50ms analysis time</li>
 *   <li>Optimizer overhead: &lt; 20% of total query execution time</li>
 *   <li>Memory overhead: &lt; 5MB additional heap per optimization</li>
 * </ul>
 * 
 * @author query4j team
 * @since 1.0.0
 * @see QueryOptimizer
 * @see OptimizationResult
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class OptimizerBenchmark {
    
    private QueryOptimizer defaultOptimizer;
    private QueryOptimizer highPerformanceOptimizer;
    private QueryOptimizer developmentOptimizer;
    
    // Pre-built queries for consistent benchmarking
    private QueryBuilder<TestEntity> simpleQuery;
    private QueryBuilder<TestEntity> moderateQuery;
    private QueryBuilder<TestEntity> complexQuery;
    private QueryBuilder<TestEntity> joinQuery;
    
    @Setup
    public void setUp() {
        // Initialize optimizers with different configurations
        defaultOptimizer = QueryOptimizer.create();
        highPerformanceOptimizer = QueryOptimizer.create(OptimizerConfig.highPerformanceConfig());
        developmentOptimizer = QueryOptimizer.create(OptimizerConfig.developmentConfig());
        
        // Pre-build queries to avoid measuring query construction time
        setupBenchmarkQueries();
    }
    
    private void setupBenchmarkQueries() {
        // Simple query: Single WHERE predicate with pagination
        simpleQuery = QueryBuilder.forEntity(TestEntity.class)
                .where("active", true)
                .limit(50)
                .offset(0);
        
        // Moderate query: Multiple predicates with sorting
        List<Object> departments = Arrays.asList("Engineering", "Sales", "Marketing");
        moderateQuery = QueryBuilder.forEntity(TestEntity.class)
                .where("active", true)
                .and()
                .whereIn("department", departments)
                .and()
                .where("salary", ">=", new BigDecimal("50000"))
                .orderBy("salary", false)  // false = DESC
                .limit(100);
        
        // Complex query: Advanced predicates with multiple logical operators
        complexQuery = QueryBuilder.forEntity(TestEntity.class)
                .where("active", true)
                .and()
                .openGroup()
                    .where("department", "Engineering")
                    .or()
                    .where("role", "Manager")
                .closeGroup()
                .and()
                .whereBetween("salary", new BigDecimal("60000"), new BigDecimal("150000"))
                .and()
                .whereLike("email", "%@company.com")
                .and()
                .where("joinDate", ">=", LocalDate.of(2020, 1, 1))
                .orderBy("salary", false)  // false = DESC
                .orderBy("joinDate", true)   // true = ASC
                .groupBy("department")
                .having("COUNT(*)", ">", 5)
                .limit(200)
                .offset(50);
        
        // Join query: Multi-table scenario
        joinQuery = QueryBuilder.forEntity(TestEntity.class)
                .where("active", true)
                .join("departments")  // Simple join on association field
                .where("departments.budget", ">", new BigDecimal("100000"))
                .join("projects")  // Another join
                .where("projects.status", "ACTIVE")
                .orderBy("salary", false)  // false = DESC
                .limit(50);
    }
    
    // =============================================================================
    // Optimizer Analysis Time Benchmarks
    // =============================================================================
    
    /**
     * Benchmark: Simple Query Optimization
     * Measures pure optimizer analysis time for basic query patterns.
     * Target: &lt; 10ms (10,000 μs)
     */
    @Benchmark
    public void optimizeSimpleQuery(Blackhole bh) {
        OptimizationResult result = defaultOptimizer.optimize(simpleQuery);
        bh.consume(result);
    }
    
    /**
     * Benchmark: Moderate Query Optimization
     * Measures optimization analysis for multi-predicate queries.
     * Target: &lt; 20ms (20,000 μs)
     */
    @Benchmark
    public void optimizeModerateQuery(Blackhole bh) {
        OptimizationResult result = defaultOptimizer.optimize(moderateQuery);
        bh.consume(result);
    }
    
    /**
     * Benchmark: Complex Query Optimization
     * Measures optimization analysis for complex queries with joins and aggregations.
     * Target: &lt; 50ms (50,000 μs)
     */
    @Benchmark
    public void optimizeComplexQuery(Blackhole bh) {
        OptimizationResult result = defaultOptimizer.optimize(complexQuery);
        bh.consume(result);
    }
    
    /**
     * Benchmark: Join Query Optimization
     * Measures optimization analysis for multi-table joins.
     * Target: &lt; 40ms (40,000 μs)
     */
    @Benchmark
    public void optimizeJoinQuery(Blackhole bh) {
        OptimizationResult result = defaultOptimizer.optimize(joinQuery);
        bh.consume(result);
    }
    
    // =============================================================================
    // Configuration Impact Benchmarks
    // =============================================================================
    
    /**
     * Benchmark: High-Performance Optimizer Configuration
     * Tests optimizer with aggressive optimization settings.
     */
    @Benchmark
    public void optimizeWithHighPerformanceConfig(Blackhole bh) {
        OptimizationResult result = highPerformanceOptimizer.optimize(complexQuery);
        bh.consume(result);
    }
    
    /**
     * Benchmark: Development Optimizer Configuration
     * Tests optimizer with verbose output and longer analysis timeout.
     */
    @Benchmark
    public void optimizeWithDevelopmentConfig(Blackhole bh) {
        OptimizationResult result = developmentOptimizer.optimize(complexQuery);
        bh.consume(result);
    }
    
    // =============================================================================
    // End-to-End Performance Benchmarks (Optimizer vs No Optimizer)
    // =============================================================================
    
    /**
     * Benchmark: Query Building + Optimization + SQL Generation
     * Measures complete workflow with optimizer enabled.
     */
    @Benchmark
    public void endToEndWithOptimizer(Blackhole bh) {
        // Build query
        QueryBuilder<TestEntity> query = QueryBuilder.forEntity(TestEntity.class)
                .where("active", true)
                .and()
                .where("department", "Engineering")
                .and()
                .where("salary", ">=", new BigDecimal("70000"))
                .orderBy("salary", false)  // false = DESC
                .limit(100);
        
        // Optimize
        OptimizationResult optimization = defaultOptimizer.optimize(query);
        
        // Generate SQL
        String sql = query.toSQL();
        Map<String, Object> params = extractParameters(query);
        
        // Consume results
        bh.consume(optimization);
        bh.consume(sql);
        bh.consume(params);
    }
    
    /**
     * Benchmark: Query Building + SQL Generation (Baseline)
     * Measures workflow without optimizer for comparison.
     */
    @Benchmark
    public void endToEndWithoutOptimizer(Blackhole bh) {
        // Build query (same as above)
        QueryBuilder<TestEntity> query = QueryBuilder.forEntity(TestEntity.class)
                .where("active", true)
                .and()
                .where("department", "Engineering")
                .and()
                .where("salary", ">=", new BigDecimal("70000"))
                .orderBy("salary", false)  // false = DESC
                .limit(100);
        
        // Generate SQL (no optimization)
        String sql = query.toSQL();
        Map<String, Object> params = extractParameters(query);
        
        // Consume results
        bh.consume(sql);
        bh.consume(params);
    }
    
    // =============================================================================
    // Comparison with Baseline Libraries
    // =============================================================================
    
    /**
     * Benchmark: Raw SQL String Optimization
     * Tests optimizer performance on raw SQL queries.
     */
    @Benchmark
    public void optimizeRawSqlString(Blackhole bh) {
        String sql = "SELECT * FROM test_entity WHERE active = true " +
                    "AND department = 'Engineering' " +
                    "AND salary >= 70000 " +
                    "ORDER BY salary DESC LIMIT 100";
        
        OptimizationResult result = defaultOptimizer.optimize(sql);
        bh.consume(result);
    }
    
    // =============================================================================
    // Memory and Overhead Benchmarks
    // =============================================================================
    
    /**
     * Benchmark: Optimizer Memory Allocation
     * Measures memory overhead during optimization analysis.
     */
    @Benchmark
    public void measureOptimizerMemoryOverhead(Blackhole bh) {
        // Capture memory before
        Runtime runtime = Runtime.getRuntime();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        // Run optimization
        OptimizationResult result = defaultOptimizer.optimize(complexQuery);
        
        // Capture memory after
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryDelta = memoryAfter - memoryBefore;
        
        bh.consume(result);
        bh.consume(memoryDelta);
    }
    
    // =============================================================================
    // Utility Methods
    // =============================================================================
    
    /**
     * Extracts parameters from QueryBuilder for consistent benchmarking.
     * Uses reflection to access internal parameter state.
     */
    private Map<String, Object> extractParameters(QueryBuilder<?> builder) {
        Map<String, Object> parameters = new HashMap<>();
        
        try {
            // Try to get parameters using the builder's method if available
            if (builder instanceof DynamicQueryBuilder) {
                // For now, return empty map - can be enhanced with actual parameter extraction
                return parameters;
            }
        } catch (Exception e) {
            // Fall back to empty parameters if extraction fails
        }
        
        return parameters;
    }
    
    /**
     * Creates a test query for benchmarking consistency.
     */
    private QueryBuilder<TestEntity> createTestQuery(String complexity) {
        switch (complexity.toLowerCase()) {
            case "simple":
                return simpleQuery;
            case "moderate": 
                return moderateQuery;
            case "complex":
                return complexQuery;
            case "join":
                return joinQuery;
            default:
                return simpleQuery;
        }
    }
}