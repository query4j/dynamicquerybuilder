package com.github.query4j.benchmark;

import com.github.query4j.core.QueryBuilder;
import com.github.query4j.core.criteria.*;
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
 * JMH Micro-benchmarks for Query4j Core Module Operations.
 * 
 * <p>Focuses specifically on core module performance including predicate operations,
 * query cloning, immutability checks, and parameter binding preparation as specified
 * in issue #32.</p>
 * 
 * <p>Benchmark Configuration:</p>
 * <ul>
 *   <li>Warm-up iterations: 5</li>
 *   <li>Measurement iterations: 10</li>
 *   <li>Forks: 1</li>
 *   <li>Output time units: microseconds or nanoseconds</li>
 *   <li>Single-threaded benchmarks for isolation</li>
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
public class CoreModuleBenchmark {

    // Test data for benchmarking
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private List<Object> statusList;
    private QueryBuilder<TestEntity> baseQuery;
    private QueryBuilder<TestEntity> complexQuery;

    @Setup
    public void setup() {
        dateFrom = LocalDate.of(2020, 1, 1);
        dateTo = LocalDate.of(2023, 12, 31);
        salaryMin = new BigDecimal("50000");
        salaryMax = new BigDecimal("150000");
        statusList = Arrays.asList("ACTIVE", "PENDING", "INACTIVE");
        
        // Pre-build queries for cloning benchmarks
        baseQuery = QueryBuilder.forEntity(TestEntity.class)
            .where("active", true)
            .and()
            .where("department", "Engineering")
            .limit(20);
            
        complexQuery = QueryBuilder.forEntity(TestEntity.class)
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
            .orderBy("salary", false)
            .limit(25)
            .offset(50);
    }

    /**
     * Extracts parameters from all predicates in the query builder.
     */
    private Map<String, Object> extractParameters(QueryBuilder<TestEntity> builder) {
        Map<String, Object> allParams = new HashMap<>();
        
        if (builder instanceof DynamicQueryBuilder<?> dynBuilder) {
            dynBuilder.getPredicates().forEach(predicate -> allParams.putAll(predicate.getParameters()));
        }
        
        return allParams;
    }

    // =========================
    // Individual Predicate Type Benchmarks
    // =========================

    /**
     * Benchmark: SimplePredicate Creation and SQL Generation
     * Measures performance of basic equality predicates.
     */
    @Benchmark
    public void simplePredicateCreation(Blackhole bh) {
        SimplePredicate predicate = new SimplePredicate("name", "=", "John Doe", "p1");
        String sql = predicate.toSQL();
        Map<String, Object> params = predicate.getParameters();
        
        bh.consume(predicate);
        bh.consume(sql);
        bh.consume(params);
    }

    /**
     * Benchmark: InPredicate Creation and SQL Generation
     * Measures performance of IN clause predicates.
     */
    @Benchmark
    public void inPredicateCreation(Blackhole bh) {
        InPredicate predicate = new InPredicate("status", statusList, "statusParam");
        String sql = predicate.toSQL();
        Map<String, Object> params = predicate.getParameters();
        
        bh.consume(predicate);
        bh.consume(sql);
        bh.consume(params);
    }

    /**
     * Benchmark: BetweenPredicate Creation and SQL Generation
     * Measures performance of BETWEEN clause predicates.
     */
    @Benchmark
    public void betweenPredicateCreation(Blackhole bh) {
        BetweenPredicate predicate = new BetweenPredicate("salary", salaryMin, salaryMax, "salaryMin", "salaryMax");
        String sql = predicate.toSQL();
        Map<String, Object> params = predicate.getParameters();
        
        bh.consume(predicate);
        bh.consume(sql);
        bh.consume(params);
    }

    /**
     * Benchmark: LikePredicate Creation and SQL Generation
     * Measures performance of LIKE clause predicates.
     */
    @Benchmark
    public void likePredicateCreation(Blackhole bh) {
        LikePredicate predicate = new LikePredicate("email", "%@company.com", "emailParam");
        String sql = predicate.toSQL();
        Map<String, Object> params = predicate.getParameters();
        
        bh.consume(predicate);
        bh.consume(sql);
        bh.consume(params);
    }

    /**
     * Benchmark: NullPredicate Creation and SQL Generation
     * Measures performance of IS NULL/IS NOT NULL predicates.
     */
    @Benchmark
    public void nullPredicateCreation(Blackhole bh) {
        NullPredicate predicate = new NullPredicate("description", true); // IS NULL
        String sql = predicate.toSQL();
        Map<String, Object> params = predicate.getParameters();
        
        bh.consume(predicate);
        bh.consume(sql);
        bh.consume(params);
    }

    // =========================
    // Query Cloning and Immutability Benchmarks
    // =========================

    /**
     * Benchmark: Basic Query Cloning via Builder Methods
     * Measures the cost of creating new instances when chaining methods (immutability).
     */
    @Benchmark
    public void basicQueryCloning(Blackhole bh) {
        QueryBuilder<TestEntity> original = QueryBuilder.forEntity(TestEntity.class)
            .where("active", true);
            
        // Each method returns a new instance (immutability)
        QueryBuilder<TestEntity> cloned = original
            .and()
            .where("department", "Engineering")
            .limit(20);
        
        bh.consume(original);
        bh.consume(cloned);
    }

    /**
     * Benchmark: Complex Query Cloning via Builder Methods
     * Measures the cost of creating new instances for complex query chains.
     */
    @Benchmark
    public void complexQueryCloning(Blackhole bh) {
        QueryBuilder<TestEntity> original = QueryBuilder.forEntity(TestEntity.class)
            .where("active", true);
            
        // Build a complex clone through method chaining
        QueryBuilder<TestEntity> cloned = original
            .and()
            .whereBetween("joinDate", dateFrom, dateTo)
            .or()
            .openGroup()
                .where("role", "Manager")
                .and()
                .whereLike("email", "%@company.com")
            .closeGroup()
            .groupBy("department")
            .orderBy("salary", false)
            .limit(25);
        
        bh.consume(original);
        bh.consume(cloned);
    }

    /**
     * Benchmark: Query Immutability Verification
     * Measures the cost of verifying that original queries remain unchanged after cloning.
     */
    @Benchmark
    public void queryImmutabilityCheck(Blackhole bh) {
        QueryBuilder<TestEntity> original = baseQuery;
        String originalSQL = original.toSQL();
        Map<String, Object> originalParams = extractParameters(original);
        
        // Create a modified version
        QueryBuilder<TestEntity> modified = original
            .and()
            .where("newField", "newValue")
            .limit(100);
        
        // Verify original is unchanged
        String stillOriginalSQL = original.toSQL();
        Map<String, Object> stillOriginalParams = extractParameters(original);
        
        boolean sqlUnchanged = originalSQL.equals(stillOriginalSQL);
        boolean paramsUnchanged = originalParams.equals(stillOriginalParams);
        
        bh.consume(original);
        bh.consume(modified);
        bh.consume(sqlUnchanged);
        bh.consume(paramsUnchanged);
    }

    // =========================
    // Parameter Extraction and Binding Preparation Benchmarks
    // =========================

    /**
     * Benchmark: Comprehensive Parameter Extraction
     * Measures performance of extracting all parameters from a complex query.
     */
    @Benchmark
    public void comprehensiveParameterExtraction(Blackhole bh) {
        QueryBuilder<TestEntity> query = QueryBuilder.forEntity(TestEntity.class)
            .where("active", true)
            .and()
            .whereIn("status", statusList)
            .and()
            .whereBetween("salary", salaryMin, salaryMax)
            .and()
            .whereLike("email", "%@company.com")
            .and()
            .where("department", "Engineering");
        
        Map<String, Object> allParams = extractParameters(query);
        
        bh.consume(allParams);
        bh.consume(allParams.size());
    }

    /**
     * Benchmark: Parameter Binding Preparation
     * Measures the cost of preparing parameters for database binding.
     */
    @Benchmark
    public void parameterBindingPreparation(Blackhole bh) {
        Map<String, Object> params = extractParameters(complexQuery);
        
        // Simulate parameter binding preparation (type checking, conversion, etc.)
        Map<String, Object> preparedParams = new HashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            // Simulate parameter preparation logic
            if (value instanceof String && ((String) value).contains("%")) {
                // LIKE parameter
                preparedParams.put(key, value);
            } else if (value instanceof List) {
                // IN parameter - convert to array
                preparedParams.put(key, ((List<?>) value).toArray());
            } else {
                // Regular parameter
                preparedParams.put(key, value);
            }
        }
        
        bh.consume(preparedParams);
    }

    /**
     * Benchmark: Nested Parameter Extraction with Logical Groups
     * Measures performance of parameter extraction from queries with nested logical groups.
     */
    @Benchmark
    public void nestedParameterExtraction(Blackhole bh) {
        QueryBuilder<TestEntity> query = QueryBuilder.forEntity(TestEntity.class)
            .where("active", true)
            .and()
            .openGroup()
                .where("department", "Engineering")
                .or()
                .openGroup()
                    .where("role", "Manager")
                    .and()
                    .whereBetween("salary", salaryMin, salaryMax)
                .closeGroup()
            .closeGroup()
            .and()
            .whereIn("status", statusList);
        
        Map<String, Object> params = extractParameters(query);
        
        bh.consume(params);
    }

    // =========================
    // SQL String Serialization Benchmarks (Core Focus)
    // =========================

    /**
     * Benchmark: SQL Generation with Multiple Predicate Types
     * Measures SQL generation performance with various predicate types.
     */
    @Benchmark
    public void multiPredicateTypeSQL(Blackhole bh) {
        QueryBuilder<TestEntity> query = QueryBuilder.forEntity(TestEntity.class)
            .select("id", "name", "salary", "department")
            .where("active", true)                              // SimplePredicate
            .and()
            .whereIn("status", statusList)                      // InPredicate
            .and()
            .whereBetween("salary", salaryMin, salaryMax)      // BetweenPredicate
            .and()
            .whereLike("email", "%@company.com")               // LikePredicate
            .and()
            .whereIsNotNull("description");                     // NullPredicate
        
        String sql = query.toSQL();
        bh.consume(sql);
    }

    /**
     * Benchmark: SQL Generation with Deep Logical Nesting
     * Measures SQL generation performance with deeply nested logical groups.
     */
    @Benchmark
    public void deepNestedSQL(Blackhole bh) {
        QueryBuilder<TestEntity> query = QueryBuilder.forEntity(TestEntity.class)
            .where("active", true)
            .and()
            .openGroup()
                .where("department", "Engineering")
                .or()
                .openGroup()
                    .where("role", "Manager")
                    .and()
                    .openGroup()
                        .whereBetween("salary", salaryMin, salaryMax)
                        .or()
                        .where("bonus", ">", new BigDecimal("10000"))
                    .closeGroup()
                .closeGroup()
            .closeGroup();
        
        String sql = query.toSQL();
        bh.consume(sql);
    }

    // =========================
    // Core Module Integration Benchmarks
    // =========================

    /**
     * Benchmark: End-to-End Core Query Building
     * Measures complete query building cycle including all core operations.
     */
    @Benchmark
    public void endToEndCoreQueryBuilding(Blackhole bh) {
        // Build query
        QueryBuilder<TestEntity> query = QueryBuilder.forEntity(TestEntity.class)
            .select("department", "AVG(salary) as avg_salary")
            .where("active", true)
            .and()
            .whereBetween("joinDate", dateFrom, dateTo)
            .and()
            .whereIn("status", statusList)
            .groupBy("department")
            .having("salary", ">", salaryMin)
            .orderBy("avg_salary", false)
            .limit(10);
            
        // Generate SQL
        String sql = query.toSQL();
        
        // Extract parameters
        Map<String, Object> params = extractParameters(query);
        
        // Create a clone for immutability check
        QueryBuilder<TestEntity> clone = query.and().where("newField", "value");
        
        bh.consume(query);
        bh.consume(sql);
        bh.consume(params);
        bh.consume(clone);
    }
}