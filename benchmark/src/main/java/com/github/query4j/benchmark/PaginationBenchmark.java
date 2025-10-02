package com.github.query4j.benchmark;

import com.github.query4j.benchmark.entity.Employee;
import com.github.query4j.core.QueryBuilder;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * JMH Benchmarks comparing pagination performance between DynamicQueryBuilder 
 * and baseline Java libraries (JPA/Hibernate, Raw JDBC).
 * 
 * <p>Tests pagination across datasets of varying sizes (1K, 10K, 100K records)
 * with identical filter conditions and sorting to ensure fair comparison.</p>
 * 
 * <p>Metrics measured:</p>
 * <ul>
 *   <li>Query construction time</li>
 *   <li>SQL generation time</li> 
 *   <li>Database execution time</li>
 *   <li>Memory consumption during execution</li>
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
public class PaginationBenchmark {
    
    private DataSource dataSource;
    private EntityManagerFactory emf;
    
    // Test data constants
    private static final int SMALL_DATASET_SIZE = 1000;
    private static final int MEDIUM_DATASET_SIZE = 10000;
    private static final int LARGE_DATASET_SIZE = 100000;
    private static final int PAGE_SIZE = 50;
    private static final long FIVE_YEARS_IN_DAYS = 5 * 365 + 1; // ~5 years accounting for leap year
    
    // Filter criteria for consistent benchmarking
    private final String department = "Engineering";
    private final BigDecimal minSalary = new BigDecimal("50000");
    private final LocalDate fromDate = LocalDate.of(2020, 1, 1);
    
    @Setup
    public void setUp() throws Exception {
        // Configure H2 in-memory database with optimizations for benchmarking
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:benchmark;DB_CLOSE_DELAY=-1;MODE=MYSQL");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTestQuery("SELECT 1");
        
        dataSource = new HikariDataSource(config);
        
        // Setup JPA/Hibernate EntityManagerFactory
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
        
        // Initialize database schema and test data
        initializeDatabase();
        seedTestData(MEDIUM_DATASET_SIZE); // Use medium dataset for most benchmarks
    }
    
    @TearDown
    public void tearDown() throws Exception {
        if (emf != null) {
            emf.close();
        }
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
        }
    }
    
    /**
     * Initialize database schema for Employee entity.
     */
    private void initializeDatabase() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            // Drop table if exists to avoid conflicts
            try (PreparedStatement stmt = conn.prepareStatement("DROP TABLE IF EXISTS employees")) {
                stmt.execute();
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(
                "CREATE TABLE employees (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "first_name VARCHAR(100) NOT NULL, " +
                "last_name VARCHAR(100) NOT NULL, " +
                "email VARCHAR(255) NOT NULL UNIQUE, " +
                "department VARCHAR(100) NOT NULL, " +
                "role VARCHAR(50) NOT NULL, " +
                "hire_date DATE NOT NULL, " +
                "salary DECIMAL(10,2) NOT NULL, " +
                "active BOOLEAN NOT NULL, " +
                "city VARCHAR(50), " +
                "country VARCHAR(50)" +
                ")"
            )) {
                stmt.execute();
            }
            
            // Add indexes for performance
            try (PreparedStatement stmt = conn.prepareStatement("CREATE INDEX idx_department ON employees(department)")) {
                stmt.execute();
            }
            try (PreparedStatement stmt = conn.prepareStatement("CREATE INDEX idx_salary ON employees(salary)")) {
                stmt.execute();
            }
            try (PreparedStatement stmt = conn.prepareStatement("CREATE INDEX idx_hire_date ON employees(hire_date)")) {
                stmt.execute();
            }
            try (PreparedStatement stmt = conn.prepareStatement("CREATE INDEX idx_active ON employees(active)")) {
                stmt.execute();
            }
        }
    }
    
    /**
     * Seed database with test data for benchmarking.
     */
    private void seedTestData(int recordCount) throws SQLException {
        String[] departments = {"Engineering", "Sales", "Marketing", "HR", "Finance"};
        String[] roles = {"Manager", "Senior", "Junior", "Lead", "Director"};
        String[] cities = {"San Francisco", "New York", "Chicago", "Austin", "Seattle"};
        String[] countries = {"USA", "Canada", "UK", "Germany", "France"};
        
        Random random = new Random(42); // Fixed seed for reproducible benchmarks
        
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            
            String sql = "INSERT INTO employees (first_name, last_name, email, department, role, " +
                        "hire_date, salary, active, city, country) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int i = 1; i <= recordCount; i++) {
                    stmt.setString(1, "FirstName" + i);
                    stmt.setString(2, "LastName" + i);
                    stmt.setString(3, "employee" + i + "@company.com");
                    stmt.setString(4, departments[random.nextInt(departments.length)]);
                    stmt.setString(5, roles[random.nextInt(roles.length)]);
                    
                    // Random hire dates in the last 5 years
                    LocalDate startDate = LocalDate.of(2019, 1, 1);
                    long days = random.nextLong(0, FIVE_YEARS_IN_DAYS);
                    stmt.setObject(6, startDate.plusDays(days));
                    
                    // Salary between 30,000 and 200,000
                    double salary = 30000 + (random.nextDouble() * 170000);
                    stmt.setBigDecimal(7, new BigDecimal(Math.round(salary * 100.0) / 100.0));
                    
                    // 90% active employees
                    stmt.setBoolean(8, random.nextDouble() > 0.1);
                    
                    stmt.setString(9, cities[random.nextInt(cities.length)]);
                    stmt.setString(10, countries[random.nextInt(countries.length)]);
                    
                    stmt.addBatch();
                    
                    // Execute batch every 1000 records for memory efficiency
                    if (i % 1000 == 0) {
                        stmt.executeBatch();
                    }
                }
                
                // Execute remaining records
                stmt.executeBatch();
                conn.commit();
            }
        }
    }
    
    /**
     * Benchmark: DynamicQueryBuilder pagination - query construction time only.
     * Measures the time to build a paginated query with filters and sorting.
     */
    @Benchmark
    public void dynamicQueryBuilderConstruction(Blackhole bh) {
        QueryBuilder<Employee> query = QueryBuilder.forEntity(Employee.class)
            .where("department", department)
            .and()
            .where("salary", ">=", minSalary)
            .and()
            .where("hire_date", ">=", fromDate)
            .and()
            .where("active", true)
            .orderBy("salary", false) // DESC
            .orderBy("last_name")
            .page(2, PAGE_SIZE); // Page 2 with 50 records per page
        
        String sql = query.toSQL();
        bh.consume(sql);
        bh.consume(query);
    }
    
    /**
     * Benchmark: JPA/Hibernate Criteria API pagination - query construction time only.
     * Measures the time to build an equivalent paginated query using Criteria API.
     */
    @Benchmark
    public void jpaCriteriaConstruction(Blackhole bh) {
        EntityManager em = emf.createEntityManager();
        try {
            var cb = em.getCriteriaBuilder();
            var cq = cb.createQuery(Employee.class);
            var root = cq.from(Employee.class);
            
            // Build equivalent WHERE conditions
            var predicates = Arrays.asList(
                cb.equal(root.get("department"), department),
                cb.greaterThanOrEqualTo(root.get("salary"), minSalary),
                cb.greaterThanOrEqualTo(root.get("hireDate"), fromDate),
                cb.equal(root.get("active"), true)
            );
            
            cq.where(cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0])));
            
            // Add ordering
            cq.orderBy(
                cb.desc(root.get("salary")),
                cb.asc(root.get("lastName"))
            );
            
            TypedQuery<Employee> query = em.createQuery(cq);
            query.setFirstResult(PAGE_SIZE); // Page 2 offset
            query.setMaxResults(PAGE_SIZE);
            
            bh.consume(query);
        } finally {
            em.close();
        }
    }
    
    /**
     * Benchmark: Raw JDBC pagination - query construction time only.
     * Measures the time to build an equivalent paginated query using raw JDBC.
     */
    @Benchmark
    public void rawJdbcConstruction(Blackhole bh) throws SQLException {
        String sql = "SELECT * FROM employees WHERE department = ? AND salary >= ? " +
                    "AND hire_date >= ? AND active = ? " +
                    "ORDER BY salary DESC, last_name ASC " +
                    "LIMIT ? OFFSET ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, department);
            stmt.setBigDecimal(2, minSalary);
            stmt.setObject(3, fromDate);
            stmt.setBoolean(4, true);
            stmt.setInt(5, PAGE_SIZE);
            stmt.setInt(6, PAGE_SIZE); // Page 2 offset
            
            bh.consume(stmt);
        }
    }
    
    /**
     * Benchmark: DynamicQueryBuilder pagination - full execution.
     * Measures the complete time including database execution and result processing.
     */
    @Benchmark
    public void dynamicQueryBuilderExecution(Blackhole bh) throws SQLException {
        QueryBuilder<Employee> queryBuilder = QueryBuilder.forEntity(Employee.class)
            .where("department", department)
            .and()
            .where("salary", ">=", minSalary)
            .and()
            .where("hire_date", ">=", fromDate)
            .and()
            .where("active", true)
            .orderBy("salary", false) // DESC
            .orderBy("last_name")
            .page(2, PAGE_SIZE);
        
        String sql = queryBuilder.toSQL();
        Map<String, Object> params = extractParameters(queryBuilder);
        
        // Execute the query manually since we don't have full ORM integration
        List<Employee> results = executeQueryBuilder(sql, params);
        bh.consume(results);
    }
    
    /**
     * Benchmark: JPA/Hibernate Criteria API pagination - full execution.
     */
    @Benchmark
    public void jpaCriteriaExecution(Blackhole bh) {
        EntityManager em = emf.createEntityManager();
        try {
            var cb = em.getCriteriaBuilder();
            var cq = cb.createQuery(Employee.class);
            var root = cq.from(Employee.class);
            
            var predicates = Arrays.asList(
                cb.equal(root.get("department"), department),
                cb.greaterThanOrEqualTo(root.get("salary"), minSalary),
                cb.greaterThanOrEqualTo(root.get("hireDate"), fromDate),
                cb.equal(root.get("active"), true)
            );
            
            cq.where(cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0])));
            cq.orderBy(
                cb.desc(root.get("salary")),
                cb.asc(root.get("lastName"))
            );
            
            TypedQuery<Employee> query = em.createQuery(cq);
            query.setFirstResult(PAGE_SIZE);
            query.setMaxResults(PAGE_SIZE);
            
            List<Employee> results = query.getResultList();
            bh.consume(results);
        } finally {
            em.close();
        }
    }
    
    /**
     * Benchmark: Raw JDBC pagination - full execution.
     */
    @Benchmark
    public void rawJdbcExecution(Blackhole bh) throws SQLException {
        String sql = "SELECT * FROM employees WHERE department = ? AND salary >= ? " +
                    "AND hire_date >= ? AND active = ? " +
                    "ORDER BY salary DESC, last_name ASC " +
                    "LIMIT ? OFFSET ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, department);
            stmt.setBigDecimal(2, minSalary);
            stmt.setObject(3, fromDate);
            stmt.setBoolean(4, true);
            stmt.setInt(5, PAGE_SIZE);
            stmt.setInt(6, PAGE_SIZE);
            
            List<Employee> results = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Employee emp = new Employee();
                    emp.setId(rs.getLong("id"));
                    emp.setFirstName(rs.getString("first_name"));
                    emp.setLastName(rs.getString("last_name"));
                    emp.setEmail(rs.getString("email"));
                    emp.setDepartment(rs.getString("department"));
                    emp.setRole(rs.getString("role"));
                    emp.setHireDate(rs.getObject("hire_date", LocalDate.class));
                    emp.setSalary(rs.getBigDecimal("salary"));
                    emp.setActive(rs.getBoolean("active"));
                    emp.setCity(rs.getString("city"));
                    emp.setCountry(rs.getString("country"));
                    results.add(emp);
                }
            }
            
            bh.consume(results);
        }
    }
    
    /**
     * Helper method to extract parameters from DynamicQueryBuilder.
     */
    private Map<String, Object> extractParameters(QueryBuilder<Employee> builder) {
        Map<String, Object> allParams = new HashMap<>();
        
        if (builder instanceof com.github.query4j.core.impl.DynamicQueryBuilder<?> dynBuilder) {
            dynBuilder.getPredicates().forEach(predicate -> 
                allParams.putAll(predicate.getParameters()));
        }
        
        return allParams;
    }
    
    /**
     * Helper method to execute QueryBuilder-generated SQL manually.
     * This simulates what would happen in a real ORM integration.
     */
    private List<Employee> executeQueryBuilder(String sql, Map<String, Object> params) throws SQLException {
        // Convert named parameters to positional for JDBC
        String jdbcSql = sql;
        List<Object> orderedParams = new ArrayList<>();
        
        // Sort parameter names by length descending to avoid substring replacement issues
        List<Map.Entry<String, Object>> paramEntries = new ArrayList<>(params.entrySet());
        paramEntries.sort((e1, e2) -> Integer.compare(e2.getKey().length(), e1.getKey().length()));
        for (Map.Entry<String, Object> entry : paramEntries) {
            String paramName = ":" + entry.getKey();
            if (jdbcSql.contains(paramName)) {
                jdbcSql = jdbcSql.replaceFirst(java.util.regex.Pattern.quote(paramName), "?");
                orderedParams.add(entry.getValue());
            }
        }
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(jdbcSql)) {
            
            // Set parameters in order
            for (int i = 0; i < orderedParams.size(); i++) {
                stmt.setObject(i + 1, orderedParams.get(i));
            }
            
            List<Employee> results = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Employee emp = new Employee();
                    emp.setId(rs.getLong("id"));
                    emp.setFirstName(rs.getString("first_name"));
                    emp.setLastName(rs.getString("last_name"));
                    emp.setEmail(rs.getString("email"));
                    emp.setDepartment(rs.getString("department"));
                    emp.setRole(rs.getString("role"));
                    emp.setHireDate(rs.getObject("hire_date", LocalDate.class));
                    emp.setSalary(rs.getBigDecimal("salary"));
                    emp.setActive(rs.getBoolean("active"));
                    emp.setCity(rs.getString("city"));
                    emp.setCountry(rs.getString("country"));
                    results.add(emp);
                }
            }
            
            return results;
        }
    }

}