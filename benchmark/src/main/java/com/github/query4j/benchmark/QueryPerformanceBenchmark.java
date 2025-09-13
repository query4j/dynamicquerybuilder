package com.github.query4j.benchmark;

import com.github.query4j.core.QueryBuilder;
import com.github.query4j.core.impl.DynamicQueryBuilder;
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
 * JMH Performance benchmarks for Query4j Dynamic Query Builder.
 * 
 * <p>Measures performance of basic, moderate, and complex query scenarios
 * including builder construction, SQL generation, and parameter extraction.</p>
 * 
 * <p>Performance targets:</p>
 * <ul>
 *   <li>Basic query: &lt; 1 ms average</li>
 *   <li>Moderate query: &lt; 2 ms average</li>
 *   <li>Complex query: &lt; 5 ms average</li>
 *   <li>95th percentile within 2× average</li>
 * </ul>
 * 
 * @since 1.0.0
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class QueryPerformanceBenchmark {

    // Test data for benchmarking
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;

    @Setup
    public void setup() {
        dateFrom = LocalDate.of(2020, 1, 1);
        dateTo = LocalDate.of(2023, 12, 31);
        salaryMin = new BigDecimal("50000");
        salaryMax = new BigDecimal("150000");
    }

    /**
     * Extracts parameters from all predicates in the query builder.
     * 
     * @param builder the query builder to extract parameters from
     * @return map of all parameters combined from predicates
     */
    private Map<String, Object> extractParameters(QueryBuilder<TestEntity> builder) {
        Map<String, Object> allParams = new HashMap<>();
        
        // Cast to DynamicQueryBuilder to access getPredicates method
        if (builder instanceof DynamicQueryBuilder<?> dynBuilder) {
            // Extract parameters from all predicates
            dynBuilder.getPredicates().forEach(predicate -> allParams.putAll(predicate.getParameters()));
        }
        
        return allParams;
    }

    /**
     * Benchmark Scenario 1: Basic Query
     * - Single WHERE predicate
     * - No joins, no aggregation
     * - LIMIT/OFFSET for pagination
     * 
     * Target: &lt; 1 ms average
     */
    @Benchmark
    public void basicQuery(Blackhole bh) {
        QueryBuilder<TestEntity> builder = QueryBuilder.forEntity(TestEntity.class)
            .where("active", true)
            .limit(20)
            .offset(0);

        String sql = builder.toSQL();
        Map<String, Object> params = extractParameters(builder);
        
        // Consume results to prevent dead code elimination
        bh.consume(sql);
        bh.consume(params);
    }

    /**
     * Benchmark Scenario 2: Moderate Query  
     * - Two WHERE predicates combined with AND
     * - One IN predicate
     * - Simple ORDER BY
     * 
     * Target: &lt; 2 ms average
     */
    @Benchmark
    public void moderateQuery(Blackhole bh) {
        List<Object> statusObjects = Arrays.asList("ACTIVE", "PENDING", "INACTIVE");
        
        QueryBuilder<TestEntity> builder = QueryBuilder.forEntity(TestEntity.class)
            .where("active", true)
            .and()
            .where("department", "Engineering")
            .and()
            .whereIn("status", statusObjects)
            .orderBy("name")
            .limit(50);

        String sql = builder.toSQL();
        Map<String, Object> params = extractParameters(builder);
        
        bh.consume(sql);
        bh.consume(params);
    }

    /**
     * Benchmark Scenario 3: Complex Query
     * - Multiple WHERE predicates with AND/OR
     * - BETWEEN predicate
     * - LIKE predicate  
     * - GROUP BY + HAVING
     * - Aggregation (SUM)
     * - Pagination
     * 
     * Target: &lt; 5 ms average
     */
    @Benchmark
    public void complexQuery(Blackhole bh) {
        QueryBuilder<TestEntity> builder = QueryBuilder.forEntity(TestEntity.class)
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
            .orderBy("salary", false) // false = DESC
            .limit(25)
            .offset(50);

        String sql = builder.toSQL();
        Map<String, Object> params = extractParameters(builder);
        
        bh.consume(sql);
        bh.consume(params);
    }

    /**
     * Benchmark: Builder Construction Only
     * Measures just the time to create and configure the builder
     * without SQL generation.
     */
    @Benchmark
    public void builderConstructionBasic(Blackhole bh) {
        QueryBuilder<TestEntity> builder = QueryBuilder.forEntity(TestEntity.class)
            .where("active", true)
            .limit(20)
            .offset(0);
            
        bh.consume(builder);
    }

    /**
     * Benchmark: SQL Generation Only
     * Measures just the SQL string generation time.
     */
    @Benchmark
    public void sqlGenerationBasic(Blackhole bh) {
        QueryBuilder<TestEntity> builder = QueryBuilder.forEntity(TestEntity.class)
            .where("active", true)
            .limit(20)
            .offset(0);

        String sql = builder.toSQL();
        bh.consume(sql);
    }

    /**
     * Benchmark: Parameter Extraction Only  
     * Measures just the parameter extraction time.
     */
    @Benchmark
    public void parameterExtractionBasic(Blackhole bh) {
        List<Object> statusObjects = Arrays.asList("ACTIVE", "PENDING", "INACTIVE");
        
        QueryBuilder<TestEntity> builder = QueryBuilder.forEntity(TestEntity.class)
            .where("active", true)
            .whereIn("status", statusObjects)
            .whereBetween("salary", salaryMin, salaryMax);

        Map<String, Object> params = extractParameters(builder);
        bh.consume(params);
    }

    /**
     * Benchmark: Complex Builder Construction  
     * Measures complex builder creation time.
     */
    @Benchmark
    public void builderConstructionComplex(Blackhole bh) {
        QueryBuilder<TestEntity> builder = QueryBuilder.forEntity(TestEntity.class)
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
            
        bh.consume(builder);
    }

    /**
     * Benchmark: Complex SQL Generation
     * Measures complex SQL string generation time.
     */
    @Benchmark
    public void sqlGenerationComplex(Blackhole bh) {
        QueryBuilder<TestEntity> builder = QueryBuilder.forEntity(TestEntity.class)
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
            .groupBy("department")
            .having("salary", ">", new BigDecimal("500000"))
            .orderBy("salary", false);

        String sql = builder.toSQL();
        bh.consume(sql);
    }
}