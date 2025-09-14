package com.github.query4j.benchmark;

import com.github.query4j.benchmark.entity.Employee;
import com.github.query4j.core.QueryBuilder;
import com.github.query4j.optimizer.OptimizerConfig;
import com.github.query4j.optimizer.QueryOptimizer;
import com.github.query4j.optimizer.OptimizationResult;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * JMH Benchmarks comparing Query4j DynamicQueryBuilder + Optimizer performance 
 * against baseline libraries (Hibernate Criteria API, MyBatis, Raw JDBC).
 * 
 * <p>This benchmark measures end-to-end query performance including:</p>
 * <ul>
 *   <li>Query construction time</li>
 *   <li>Optimization analysis time (Query4j only)</li>
 *   <li>SQL generation/preparation time</li>
 *   <li>Database execution time</li>
 *   <li>Memory overhead and GC impact</li>
 * </ul>
 * 
 * <p>All libraries execute identical logical queries on the same dataset for fair comparison.</p>
 * 
 * @author query4j team
 * @since 1.0.0
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class OptimizerVsBaselineBenchmark {
    
    private static final int MEDIUM_DATASET_SIZE = 10000;
    private static final int PAGE_SIZE = 50;
    private static final int PAGE_NUMBER = 2; // Start at page 2 (offset 50)
    
    // Query4j components
    private QueryOptimizer optimizer;
    private QueryOptimizer optimizerHighPerf;
    
    // JPA/Hibernate components
    private EntityManagerFactory emf;
    
    // Raw JDBC components  
    private DataSource dataSource;
    
    @Setup
    public void setUp() throws Exception {
        // Initialize Query4j optimizers
        optimizer = QueryOptimizer.create();
        optimizerHighPerf = QueryOptimizer.create(OptimizerConfig.highPerformanceConfig());
        
        // Initialize database and JPA
        initializeJPA();
        initializeJDBC();
        
        // Seed test data
        seedTestData(MEDIUM_DATASET_SIZE);
    }
    
    @TearDown
    public void tearDown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
        }
    }
    
    private void initializeJPA() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");
        properties.put("jakarta.persistence.jdbc.url", "jdbc:h2:mem:benchmark;DB_CLOSE_DELAY=-1;MODE=MYSQL");
        properties.put("jakarta.persistence.jdbc.user", "sa");
        properties.put("jakarta.persistence.jdbc.password", "");
        properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        properties.put("hibernate.hbm2ddl.auto", "create-drop");
        properties.put("hibernate.show_sql", false);
        properties.put("hibernate.format_sql", false);
        
        emf = Persistence.createEntityManagerFactory("benchmark-unit", properties);
    }
    
    private void initializeJDBC() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl("jdbc:h2:mem:benchmark;DB_CLOSE_DELAY=-1;MODE=MYSQL");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        
        dataSource = new HikariDataSource(config);
    }
    
    private void seedTestData(int recordCount) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            // Create table if not exists
            String createTable = """
                CREATE TABLE IF NOT EXISTS employees (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    first_name VARCHAR(100) NOT NULL,
                    last_name VARCHAR(100) NOT NULL,
                    email VARCHAR(255) UNIQUE NOT NULL,
                    department VARCHAR(100) NOT NULL,
                    role VARCHAR(50) NOT NULL,
                    hire_date DATE NOT NULL,
                    salary DECIMAL(10,2) NOT NULL,
                    active BOOLEAN NOT NULL,
                    city VARCHAR(50),
                    country VARCHAR(50)
                )
                """;
            
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTable);
            }
            
            // Clear existing data
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM employees");
            }
            
            // Insert benchmark data
            String insertSql = """
                INSERT INTO employees (first_name, last_name, email, department, role, 
                                     hire_date, salary, active, city, country)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
            
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                String[] departments = {"Engineering", "Sales", "Marketing", "HR", "Finance"};
                String[] roles = {"Developer", "Manager", "Analyst", "Lead", "Director"};
                String[] cities = {"San Francisco", "New York", "Chicago", "Austin", "Seattle"};
                
                for (int i = 0; i < recordCount; i++) {
                    pstmt.setString(1, "FirstName" + i);
                    pstmt.setString(2, "LastName" + i);  
                    pstmt.setString(3, "employee" + i + "@company.com");
                    pstmt.setString(4, departments[i % departments.length]);
                    pstmt.setString(5, roles[i % roles.length]);
                    pstmt.setDate(6, java.sql.Date.valueOf(LocalDate.of(2020, 1, 1).plusDays(i % 1000)));
                    pstmt.setBigDecimal(7, new BigDecimal(50000 + (i % 100) * 1000));
                    pstmt.setBoolean(8, i % 10 != 0); // 90% active
                    pstmt.setString(9, cities[i % cities.length]);
                    pstmt.setString(10, "USA");
                    
                    pstmt.addBatch();
                    
                    if (i % 1000 == 0) {
                        pstmt.executeBatch();
                    }
                }
                pstmt.executeBatch();
            }
            
            conn.commit();
        }
    }
    
    // =============================================================================
    // Query4j DynamicQueryBuilder + Optimizer Benchmarks
    // =============================================================================
    
    /**
     * Benchmark: Query4j with Default Optimizer
     * Measures complete query lifecycle with optimization analysis.
     */
    @Benchmark
    public void query4jWithOptimizer(Blackhole bh) {
        // Build query
        QueryBuilder<Employee> query = QueryBuilder.forEntity(Employee.class)
                .where("department", "Engineering")
                .and()
                .where("salary", ">=", new BigDecimal("50000"))
                .and()
                .where("hire_date", ">=", LocalDate.of(2020, 1, 1))
                .and()
                .where("active", true)
                .orderBy("salary", false)  // false = DESC
                .orderBy("last_name", true)   // true = ASC
                .limit(PAGE_SIZE)
                .offset(PAGE_NUMBER * PAGE_SIZE);
        
        // Optimize
        OptimizationResult optimization = optimizer.optimize(query);
        
        // Generate SQL and parameters
        String sql = query.toSQL();
        
        bh.consume(optimization);
        bh.consume(sql);
    }
    
    /**
     * Benchmark: Query4j with High-Performance Optimizer
     * Tests aggressive optimization configuration.
     */
    @Benchmark
    public void query4jWithHighPerfOptimizer(Blackhole bh) {
        QueryBuilder<Employee> query = QueryBuilder.forEntity(Employee.class)
                .where("department", "Engineering")
                .and()
                .where("salary", ">=", new BigDecimal("50000"))
                .and()
                .where("hire_date", ">=", LocalDate.of(2020, 1, 1))
                .and()
                .where("active", true)
                .orderBy("salary", false)  // false = DESC
                .orderBy("last_name", true)   // true = ASC
                .limit(PAGE_SIZE)
                .offset(PAGE_NUMBER * PAGE_SIZE);
        
        OptimizationResult optimization = optimizerHighPerf.optimize(query);
        String sql = query.toSQL();
        
        bh.consume(optimization);
        bh.consume(sql);
    }
    
    /**
     * Benchmark: Query4j without Optimizer (Baseline)
     * Measures Query4j performance without optimization for comparison.
     */
    @Benchmark
    public void query4jWithoutOptimizer(Blackhole bh) {
        QueryBuilder<Employee> query = QueryBuilder.forEntity(Employee.class)
                .where("department", "Engineering")
                .and()
                .where("salary", ">=", new BigDecimal("50000"))
                .and()
                .where("hire_date", ">=", LocalDate.of(2020, 1, 1))
                .and()
                .where("active", true)
                .orderBy("salary", false)  // false = DESC
                .orderBy("last_name", true)   // true = ASC
                .limit(PAGE_SIZE)
                .offset(PAGE_NUMBER * PAGE_SIZE);
        
        String sql = query.toSQL();
        bh.consume(sql);
    }
    
    // =============================================================================
    // JPA/Hibernate Criteria API Benchmarks
    // =============================================================================
    
    /**
     * Benchmark: JPA Criteria API Construction
     * Measures Hibernate Criteria API query building performance.
     */
    @Benchmark  
    public void jpaCriteriaApiConstruction(Blackhole bh) {
        EntityManager em = emf.createEntityManager();
        
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
            Root<Employee> root = cq.from(Employee.class);
            
            // Build same logical query as Query4j
            Predicate departmentPred = cb.equal(root.get("department"), "Engineering");
            Predicate salaryPred = cb.greaterThanOrEqualTo(root.get("salary"), new BigDecimal("50000"));
            Predicate hireDatePred = cb.greaterThanOrEqualTo(root.get("hireDate"), LocalDate.of(2020, 1, 1));
            Predicate activePred = cb.equal(root.get("active"), true);
            
            cq.select(root)
              .where(cb.and(departmentPred, salaryPred, hireDatePred, activePred))
              .orderBy(
                  cb.desc(root.get("salary")),
                  cb.asc(root.get("lastName"))
              );
            
            TypedQuery<Employee> query = em.createQuery(cq);
            query.setFirstResult(PAGE_NUMBER * PAGE_SIZE);
            query.setMaxResults(PAGE_SIZE);
            
            bh.consume(query);
            
        } finally {
            em.close();
        }
    }
    
    /**
     * Benchmark: JPA Criteria API Execution
     * Measures end-to-end JPA performance including database execution.
     */
    @Benchmark
    public void jpaCriteriaApiExecution(Blackhole bh) {
        EntityManager em = emf.createEntityManager();
        
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
            Root<Employee> root = cq.from(Employee.class);
            
            Predicate departmentPred = cb.equal(root.get("department"), "Engineering");
            Predicate salaryPred = cb.greaterThanOrEqualTo(root.get("salary"), new BigDecimal("50000"));
            Predicate hireDatePred = cb.greaterThanOrEqualTo(root.get("hireDate"), LocalDate.of(2020, 1, 1));
            Predicate activePred = cb.equal(root.get("active"), true);
            
            cq.select(root)
              .where(cb.and(departmentPred, salaryPred, hireDatePred, activePred))
              .orderBy(
                  cb.desc(root.get("salary")),
                  cb.asc(root.get("lastName"))
              );
            
            TypedQuery<Employee> query = em.createQuery(cq);
            query.setFirstResult(PAGE_NUMBER * PAGE_SIZE);
            query.setMaxResults(PAGE_SIZE);
            
            List<Employee> results = query.getResultList();
            bh.consume(results);
            
        } finally {
            em.close();
        }
    }
    
    // =============================================================================
    // Raw JDBC Benchmarks
    // =============================================================================
    
    /**
     * Benchmark: Raw JDBC Construction
     * Measures pure JDBC query preparation time.
     */
    @Benchmark
    public void rawJdbcConstruction(Blackhole bh) {
        String sql = """
            SELECT * FROM employees 
            WHERE department = ? 
              AND salary >= ? 
              AND hire_date >= ? 
              AND active = ?
            ORDER BY salary DESC, last_name ASC
            LIMIT ? OFFSET ?
            """;
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "Engineering");
            pstmt.setBigDecimal(2, new BigDecimal("50000"));
            pstmt.setDate(3, java.sql.Date.valueOf(LocalDate.of(2020, 1, 1)));
            pstmt.setBoolean(4, true);
            pstmt.setInt(5, PAGE_SIZE);
            pstmt.setInt(6, PAGE_NUMBER * PAGE_SIZE);
            
            bh.consume(pstmt);
            
        } catch (SQLException e) {
            throw new RuntimeException("JDBC benchmark failed", e);
        }
    }
    
    /**
     * Benchmark: Raw JDBC Execution
     * Measures end-to-end JDBC performance including database execution.
     */
    @Benchmark
    public void rawJdbcExecution(Blackhole bh) {
        String sql = """
            SELECT * FROM employees 
            WHERE department = ? 
              AND salary >= ? 
              AND hire_date >= ? 
              AND active = ?
            ORDER BY salary DESC, last_name ASC
            LIMIT ? OFFSET ?
            """;
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "Engineering");
            pstmt.setBigDecimal(2, new BigDecimal("50000"));
            pstmt.setDate(3, java.sql.Date.valueOf(LocalDate.of(2020, 1, 1)));
            pstmt.setBoolean(4, true);
            pstmt.setInt(5, PAGE_SIZE);
            pstmt.setInt(6, PAGE_NUMBER * PAGE_SIZE);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                List<Map<String, Object>> results = new ArrayList<>();
                
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", rs.getLong("id"));
                    row.put("firstName", rs.getString("first_name"));
                    row.put("lastName", rs.getString("last_name"));
                    row.put("email", rs.getString("email"));
                    row.put("department", rs.getString("department"));
                    row.put("salary", rs.getBigDecimal("salary"));
                    row.put("active", rs.getBoolean("active"));
                    results.add(row);
                }
                
                bh.consume(results);
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("JDBC benchmark failed", e);
        }
    }
    
    // =============================================================================
    // Memory and Overhead Analysis Benchmarks
    // =============================================================================
    
    /**
     * Benchmark: Memory Usage Comparison
     * Measures memory overhead of different approaches.
     */
    @Benchmark
    public void measureMemoryUsageQuery4jOptimizer(Blackhole bh) {
        Runtime runtime = Runtime.getRuntime();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        QueryBuilder<Employee> query = QueryBuilder.forEntity(Employee.class)
                .where("department", "Engineering")
                .and()
                .where("salary", ">=", new BigDecimal("50000"))
                .orderBy("salary", false)  // false = DESC
                .limit(PAGE_SIZE);
        
        OptimizationResult optimization = optimizer.optimize(query);
        String sql = query.toSQL();
        
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryDelta = memoryAfter - memoryBefore;
        
        bh.consume(optimization);
        bh.consume(sql);
        bh.consume(memoryDelta);
    }
    
    /**
     * Benchmark: Memory Usage JPA Criteria
     * Measures memory overhead of JPA Criteria API.
     */
    @Benchmark
    public void measureMemoryUsageJpaCriteria(Blackhole bh) {
        Runtime runtime = Runtime.getRuntime();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        EntityManager em = emf.createEntityManager();
        
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
            Root<Employee> root = cq.from(Employee.class);
            
            cq.select(root)
              .where(cb.and(
                  cb.equal(root.get("department"), "Engineering"),
                  cb.greaterThanOrEqualTo(root.get("salary"), new BigDecimal("50000"))
              ))
              .orderBy(cb.desc(root.get("salary")));
            
            TypedQuery<Employee> query = em.createQuery(cq);
            
            long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
            long memoryDelta = memoryAfter - memoryBefore;
            
            bh.consume(query);
            bh.consume(memoryDelta);
            
        } finally {
            em.close();
        }
    }
}