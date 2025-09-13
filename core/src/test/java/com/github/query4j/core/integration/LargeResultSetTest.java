package com.github.query4j.core.integration;

import com.github.query4j.core.impl.DynamicQueryBuilder;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for large result sets and pagination using H2 in-memory database.
 * Tests retrieval of 10,000+ records and validates pagination performance.
 * 
 * Performance Targets:
 * - Single-threaded retrieval of 10,000 records < 500ms
 * - Pagination should maintain performance with large datasets
 * - Memory usage should remain reasonable
 */
@DisplayName("Large Result Set Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LargeResultSetTest {

    private static final String DB_URL = "jdbc:h2:mem:large_testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";
    
    private static final int LARGE_DATASET_SIZE = 10_000;
    private static final int PAGINATION_SIZE = 100;

    private Connection connection;

    /**
     * User entity for testing large result sets
     */
    public static class User {
        private int id;
        private String firstName;
        private String lastName;
        private String email;
        private String department;
        private String role;
        private LocalDate hireDate;
        private Double salary;
        private boolean active;
        private String city;
        private String country;

        public User() {}

        public User(int id, String firstName, String lastName, String email, String department, 
                   String role, LocalDate hireDate, Double salary, boolean active, String city, String country) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.department = department;
            this.role = role;
            this.hireDate = hireDate;
            this.salary = salary;
            this.active = active;
            this.city = city;
            this.country = country;
        }

        // Getters and setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public LocalDate getHireDate() { return hireDate; }
        public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
        public Double getSalary() { return salary; }
        public void setSalary(Double salary) { this.salary = salary; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
    }

    @BeforeEach
    void setUp() throws SQLException {
        // Create H2 in-memory database connection
        connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        
        // Create schema
        createSchema();
        
        // Seed large test data set
        seedLargeDataSet();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            // Clean up tables
            connection.createStatement().execute("DROP TABLE IF EXISTS \"User\"");
            connection.close();
        }
    }

    private void createSchema() throws SQLException {
        Statement stmt = connection.createStatement();

        // User table optimized for large datasets with proper indexes
        stmt.execute("""
            CREATE TABLE "User" (
                id INT PRIMARY KEY,
                first_name VARCHAR(100) NOT NULL,
                last_name VARCHAR(100) NOT NULL,
                email VARCHAR(255) NOT NULL UNIQUE,
                department VARCHAR(100) NOT NULL,
                role VARCHAR(100) NOT NULL,
                hire_date DATE NOT NULL,
                salary DECIMAL(10,2) NOT NULL,
                active BOOLEAN NOT NULL DEFAULT TRUE,
                city VARCHAR(100) NOT NULL,
                country VARCHAR(100) NOT NULL
            )
            """);

        // Create indexes for performance
        stmt.execute("CREATE INDEX idx_user_department ON \"User\"(department)");
        stmt.execute("CREATE INDEX idx_user_active ON \"User\"(active)");
        stmt.execute("CREATE INDEX idx_user_hire_date ON \"User\"(hire_date)");
        stmt.execute("CREATE INDEX idx_user_salary ON \"User\"(salary)");
        stmt.execute("CREATE INDEX idx_user_country ON \"User\"(country)");

        stmt.close();
    }

    private void seedLargeDataSet() throws SQLException {
        System.out.println("Seeding " + LARGE_DATASET_SIZE + " user records...");
        
        long startTime = System.currentTimeMillis();

        PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO \"User\" (id, first_name, last_name, email, department, role, hire_date, salary, active, city, country) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
        );

        connection.setAutoCommit(false); // Batch insert for performance

        String[] firstNames = {"John", "Jane", "Mike", "Sarah", "David", "Lisa", "Robert", "Maria", "James", "Jennifer", 
                              "Michael", "Patricia", "William", "Linda", "Richard", "Barbara", "Joseph", "Elizabeth", "Thomas", "Susan"};
        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
                             "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin"};
        String[] departments = {"Engineering", "Sales", "Marketing", "HR", "Finance", "Operations", "Legal", "IT", "Support", "Research"};
        String[] roles = {"Manager", "Senior", "Associate", "Analyst", "Specialist", "Lead", "Director", "Coordinator", "Administrator", "Executive"};
        String[] cities = {"New York", "Los Angeles", "Chicago", "Houston", "Phoenix", "Philadelphia", "San Antonio", "San Diego", "Dallas", "San Jose"};
        String[] countries = {"USA", "Canada", "Mexico", "UK", "Germany", "France", "Australia", "Japan", "India", "Brazil"};

        for (int i = 1; i <= LARGE_DATASET_SIZE; i++) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            
            stmt.setInt(1, i);
            stmt.setString(2, firstNames[random.nextInt(firstNames.length)]);
            stmt.setString(3, lastNames[random.nextInt(lastNames.length)]);
            stmt.setString(4, "user" + i + "@example.com");
            stmt.setString(5, departments[random.nextInt(departments.length)]);
            stmt.setString(6, roles[random.nextInt(roles.length)]);
            
            // Random hire dates in the last 5 years
            LocalDate startDate = LocalDate.of(2019, 1, 1);
            long days = random.nextLong(0, 1826); // ~5 years worth of days
            stmt.setDate(7, Date.valueOf(startDate.plusDays(days)));
            
            // Salary between 30,000 and 200,000
            double salary = 30000 + (random.nextDouble() * 170000);
            stmt.setDouble(8, Math.round(salary * 100.0) / 100.0);
            
            // 90% active users
            stmt.setBoolean(9, random.nextDouble() > 0.1);
            
            stmt.setString(10, cities[random.nextInt(cities.length)]);
            stmt.setString(11, countries[random.nextInt(countries.length)]);
            
            stmt.addBatch();
            
            // Execute batch every 1000 records for memory efficiency
            if (i % 1000 == 0) {
                stmt.executeBatch();
            }
        }
        
        // Execute remaining records
        stmt.executeBatch();
        connection.commit();
        connection.setAutoCommit(true);
        
        stmt.close();
        
        long endTime = System.currentTimeMillis();
        System.out.println("Seeded " + LARGE_DATASET_SIZE + " records in " + (endTime - startTime) + "ms");
    }

    /**
     * Helper method to create a User object from ResultSet
     */
    private User createUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setEmail(rs.getString("email"));
        user.setDepartment(rs.getString("department"));
        user.setRole(rs.getString("role"));
        user.setHireDate(rs.getDate("hire_date").toLocalDate());
        user.setSalary(rs.getDouble("salary"));
        user.setActive(rs.getBoolean("active"));
        user.setCity(rs.getString("city"));
        user.setCountry(rs.getString("country"));
        return user;
    }

    /**
     * Helper method to create a partial User object for pagination tests
     */
    private User createPartialUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setEmail(rs.getString("email"));
        user.setDepartment(rs.getString("department"));
        return user;
    }

    @Test
    @Order(1)
    @DisplayName("should retrieve all 10,000+ records within performance target")
    void shouldRetrieveAllRecordsWithinPerformanceTarget() throws SQLException {
        System.out.println("Testing full retrieval of " + LARGE_DATASET_SIZE + " records...");
        
        // Test query builder SQL generation
        DynamicQueryBuilder<User> builder = new DynamicQueryBuilder<>(User.class);
        String sql = builder.toSQL();
        
        assertNotNull(sql);
        assertTrue(sql.contains("SELECT * FROM User"), "Should generate basic SELECT query");
        
        // Measure performance of actual retrieval
        long startTime = System.currentTimeMillis();
        
        String actualSQL = "SELECT COUNT(*) FROM \"User\"";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(actualSQL)) {
            
            assertTrue(rs.next());
            int totalCount = rs.getInt(1);
            assertEquals(LARGE_DATASET_SIZE, totalCount, "Should have seeded exactly " + LARGE_DATASET_SIZE + " records");
        }
        
        // Now test actual data retrieval
        actualSQL = "SELECT * FROM \"User\" ORDER BY id";
        List<User> users = new ArrayList<>();
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(actualSQL)) {
            
            while (rs.next()) {
                users.add(createUserFromResultSet(rs));
            }
        }
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        System.out.println("Retrieved " + users.size() + " records in " + executionTime + "ms");
        
        // Verify results
        assertEquals(LARGE_DATASET_SIZE, users.size(), "Should retrieve all seeded records");
        
        // Performance assertion (target: < 500ms)
        assertTrue(executionTime < 500, 
            "Retrieval should complete within 500ms (actual: " + executionTime + "ms)");
        
        // Verify data integrity
        assertNotNull(users.get(0).getEmail(), "First record should have valid email");
        assertNotNull(users.get(users.size() - 1).getEmail(), "Last record should have valid email");
        assertTrue(users.get(0).getEmail().contains("@example.com"), "Email should be properly formatted");
    }

    @Test
    @Order(2)
    @DisplayName("should perform efficient pagination through large result set")
    void shouldPerformEfficientPagination() throws SQLException {
        System.out.println("Testing pagination through " + LARGE_DATASET_SIZE + " records...");
        
        int totalPages = (int) Math.ceil((double) LARGE_DATASET_SIZE / PAGINATION_SIZE);
        List<Long> pageExecutionTimes = new ArrayList<>();
        
        for (int page = 0; page < Math.min(totalPages, 10); page++) { // Test first 10 pages
            int offset = page * PAGINATION_SIZE;
            
            // Test query builder pagination API
            DynamicQueryBuilder<User> builder = new DynamicQueryBuilder<>(User.class);
            int pageNumber = page + 1; // Convert 0-based to 1-based
            builder = (DynamicQueryBuilder<User>) builder.page(pageNumber, PAGINATION_SIZE);
            
            String builderSQL = builder.toSQL();
            assertNotNull(builderSQL);
            assertTrue(builderSQL.contains("LIMIT"), "Should generate LIMIT clause for pagination");
            
            // Measure actual pagination performance
            long startTime = System.currentTimeMillis();
            
            String actualSQL = "SELECT * FROM \"User\" ORDER BY id LIMIT ? OFFSET ?";
            List<User> pageUsers = new ArrayList<>();
            
            try (PreparedStatement stmt = connection.prepareStatement(actualSQL)) {
                stmt.setInt(1, PAGINATION_SIZE);
                stmt.setInt(2, offset);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        pageUsers.add(createPartialUserFromResultSet(rs));
                    }
                }
            }
            
            long endTime = System.currentTimeMillis();
            long pageTime = endTime - startTime;
            pageExecutionTimes.add(pageTime);
            
            // Verify page results
            int expectedPageSize = Math.min(PAGINATION_SIZE, LARGE_DATASET_SIZE - offset);
            assertEquals(expectedPageSize, pageUsers.size(), 
                "Page " + page + " should have " + expectedPageSize + " records");
            
            if (page < 3) { // Log first few pages
                System.out.println("Page " + page + " (" + pageUsers.size() + " records) retrieved in " + pageTime + "ms");
            }
            
            // Verify pagination ordering
            if (!pageUsers.isEmpty()) {
                int expectedFirstId = offset + 1;
                assertEquals(expectedFirstId, pageUsers.get(0).getId(), 
                    "First record on page " + page + " should have correct ID");
            }
        }
        
        // Calculate average pagination time
        double avgPageTime = pageExecutionTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        System.out.println("Average pagination time: " + String.format("%.2f", avgPageTime) + "ms");
        
        // Performance assertion - pagination should be fast even with large datasets
        assertTrue(avgPageTime < 50, 
            "Average pagination time should be < 50ms (actual: " + String.format("%.2f", avgPageTime) + "ms)");
        
        // Verify pagination performance consistency (no page should be extremely slow)
        long maxPageTime = pageExecutionTimes.stream().mapToLong(Long::longValue).max().orElse(0L);
        assertTrue(maxPageTime < 100, 
            "No single page should take more than 100ms (max: " + maxPageTime + "ms)");
    }

    @Test
    @Order(3)
    @DisplayName("should perform efficient filtered queries on large dataset")
    void shouldPerformEfficientFilteredQueries() throws SQLException {
        System.out.println("Testing filtered queries on large dataset...");
        
        // Test 1: Department filter (should use index)
        DynamicQueryBuilder<User> builder = new DynamicQueryBuilder<>(User.class);
        builder = (DynamicQueryBuilder<User>) builder.where("department", "Engineering");
        
        String builderSQL = builder.toSQL();
        assertNotNull(builderSQL);
        assertTrue(builderSQL.contains("WHERE department = :"), "Should generate WHERE clause");
        
        long startTime = System.currentTimeMillis();
        
        String actualSQL = "SELECT COUNT(*) FROM \"User\" WHERE department = ?";
        int engineeringCount;
        
        try (PreparedStatement stmt = connection.prepareStatement(actualSQL)) {
            stmt.setString(1, "Engineering");
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                engineeringCount = rs.getInt(1);
            }
        }
        
        long endTime = System.currentTimeMillis();
        long filterTime = endTime - startTime;
        
        System.out.println("Engineering department filter found " + engineeringCount + " records in " + filterTime + "ms");
        
        assertTrue(engineeringCount > 0, "Should find some engineering employees");
        assertTrue(filterTime < 100, "Filtered query should be fast with index (actual: " + filterTime + "ms)");
        
        // Test 2: Range query on salary (should use index)
        startTime = System.currentTimeMillis();
        
        actualSQL = "SELECT COUNT(*) FROM \"User\" WHERE salary BETWEEN ? AND ?";
        int salaryRangeCount;
        
        try (PreparedStatement stmt = connection.prepareStatement(actualSQL)) {
            stmt.setDouble(1, 50000.0);
            stmt.setDouble(2, 100000.0);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                salaryRangeCount = rs.getInt(1);
            }
        }
        
        endTime = System.currentTimeMillis();
        filterTime = endTime - startTime;
        
        System.out.println("Salary range filter found " + salaryRangeCount + " records in " + filterTime + "ms");
        
        assertTrue(salaryRangeCount > 0, "Should find employees in salary range");
        assertTrue(filterTime < 100, "Range query should be fast with index (actual: " + filterTime + "ms)");
        
        // Test 3: Complex multi-filter query
        startTime = System.currentTimeMillis();
        
        actualSQL = "SELECT COUNT(*) FROM \"User\" WHERE active = ? AND country = ? AND salary > ?";
        int complexFilterCount;
        
        try (PreparedStatement stmt = connection.prepareStatement(actualSQL)) {
            stmt.setBoolean(1, true);
            stmt.setString(2, "USA");
            stmt.setDouble(3, 75000.0);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                complexFilterCount = rs.getInt(1);
            }
        }
        
        endTime = System.currentTimeMillis();
        filterTime = endTime - startTime;
        
        System.out.println("Complex filter found " + complexFilterCount + " records in " + filterTime + "ms");
        
        assertTrue(complexFilterCount > 0, "Should find some records matching complex criteria");
        assertTrue(filterTime < 150, "Complex query should still be reasonably fast (actual: " + filterTime + "ms)");
    }

    @Test
    @Order(4)
    @DisplayName("should verify data integrity across large result set")
    void shouldVerifyDataIntegrityAcrossLargeResultSet() throws SQLException {
        System.out.println("Verifying data integrity across large dataset...");
        
        // Test 1: Verify all emails are unique
        String uniqueEmailSQL = "SELECT COUNT(DISTINCT email) as unique_count, COUNT(*) as total_count FROM \"User\"";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(uniqueEmailSQL)) {
            
            rs.next();
            int uniqueCount = rs.getInt("unique_count");
            int totalCount = rs.getInt("total_count");
            
            assertEquals(totalCount, uniqueCount, "All emails should be unique");
            assertEquals(LARGE_DATASET_SIZE, totalCount, "Total count should match seeded data");
        }
        
        // Test 2: Verify salary ranges are reasonable
        String salaryStatsSQL = "SELECT MIN(salary) as min_sal, MAX(salary) as max_sal, AVG(salary) as avg_sal FROM \"User\"";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(salaryStatsSQL)) {
            
            rs.next();
            double minSalary = rs.getDouble("min_sal");
            double maxSalary = rs.getDouble("max_sal");
            double avgSalary = rs.getDouble("avg_sal");
            
            assertTrue(minSalary >= 30000, "Minimum salary should be >= 30,000");
            assertTrue(maxSalary <= 200000, "Maximum salary should be <= 200,000");
            assertTrue(avgSalary > 80000 && avgSalary < 140000, "Average salary should be reasonable");
            
            System.out.println("Salary stats - Min: $" + String.format("%.2f", minSalary) + 
                             ", Max: $" + String.format("%.2f", maxSalary) + 
                             ", Avg: $" + String.format("%.2f", avgSalary));
        }
        
        // Test 3: Verify department distribution
        String deptDistSQL = "SELECT department, COUNT(*) as count FROM \"User\" GROUP BY department ORDER BY count DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(deptDistSQL)) {
            
            int totalVerified = 0;
            while (rs.next()) {
                String dept = rs.getString("department");
                int count = rs.getInt("count");
                totalVerified += count;
                
                assertNotNull(dept, "Department should not be null");
                assertTrue(count > 0, "Each department should have at least one employee");
            }
            
            assertEquals(LARGE_DATASET_SIZE, totalVerified, "Department distribution should account for all users");
        }
        
        // Test 4: Verify active/inactive ratio
        String activeStatsSQL = "SELECT active, COUNT(*) as count FROM \"User\" GROUP BY active";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(activeStatsSQL)) {
            
            int activeCount = 0;
            int inactiveCount = 0;
            
            while (rs.next()) {
                boolean active = rs.getBoolean("active");
                int count = rs.getInt("count");
                
                if (active) {
                    activeCount = count;
                } else {
                    inactiveCount = count;
                }
            }
            
            System.out.println("Active users: " + activeCount + ", Inactive users: " + inactiveCount);
            
            assertEquals(LARGE_DATASET_SIZE, activeCount + inactiveCount, "All users should be accounted for");
            assertTrue(activeCount > inactiveCount, "Should have more active than inactive users (~90% active)");
            
            // Should be approximately 90% active (allow some variance due to randomization)
            double activePercentage = (double) activeCount / LARGE_DATASET_SIZE;
            assertTrue(activePercentage > 0.85 && activePercentage < 0.95, 
                "Active percentage should be around 90% (actual: " + String.format("%.1f", activePercentage * 100) + "%)");
        }
    }

    @Test
    @Order(5)
    @DisplayName("should handle memory efficiently with large result sets")
    void shouldHandleMemoryEfficientlyWithLargeResultSets() throws SQLException {
        System.out.println("Testing memory efficiency with large result sets...");
        
        // Get initial memory state
        Runtime runtime = Runtime.getRuntime();
        runtime.gc(); // Suggest garbage collection before measurement
        
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Process data in chunks to simulate real-world batch processing
        int chunkSize = 1000;
        int chunksProcessed = 0;
        long maxMemoryUsed = initialMemory;
        
        for (int offset = 0; offset < LARGE_DATASET_SIZE; offset += chunkSize) {
            String chunkSQL = "SELECT * FROM \"User\" ORDER BY id LIMIT ? OFFSET ?";
            
            try (PreparedStatement stmt = connection.prepareStatement(chunkSQL)) {
                stmt.setInt(1, chunkSize);
                stmt.setInt(2, offset);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    List<User> chunk = new ArrayList<>();
                    
                    while (rs.next()) {
                        User user = new User();
                        user.setId(rs.getInt("id"));
                        user.setFirstName(rs.getString("first_name"));
                        user.setLastName(rs.getString("last_name"));
                        user.setEmail(rs.getString("email"));
                        user.setDepartment(rs.getString("department"));
                        chunk.add(user);
                    }
                    
                    // Simulate processing the chunk
                    assertFalse(chunk.isEmpty(), "Chunk should not be empty unless at end of dataset");
                    
                    // Measure memory after processing chunk
                    long currentMemory = runtime.totalMemory() - runtime.freeMemory();
                    maxMemoryUsed = Math.max(maxMemoryUsed, currentMemory);
                    
                    chunksProcessed++;
                }
            }
            
            // Periodically suggest garbage collection to prevent memory buildup
            if (chunksProcessed % 5 == 0) {
                runtime.gc();
            }
        }
        
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;
        long maxMemoryIncrease = maxMemoryUsed - initialMemory;
        
        System.out.println("Processed " + chunksProcessed + " chunks");
        System.out.println("Initial memory: " + (initialMemory / 1024 / 1024) + "MB");
        System.out.println("Final memory: " + (finalMemory / 1024 / 1024) + "MB");
        System.out.println("Memory increase: " + (memoryIncrease / 1024 / 1024) + "MB");
        System.out.println("Max memory increase: " + (maxMemoryIncrease / 1024 / 1024) + "MB");
        
        // Memory assertions (target: < 200MB total heap increase)
        long maxMemoryIncreaseInMB = maxMemoryIncrease / 1024 / 1024;
        assertTrue(maxMemoryIncreaseInMB < 200, 
            "Memory increase should be < 200MB (actual: " + maxMemoryIncreaseInMB + "MB)");
        
        // Final memory should not be excessive
        long finalMemoryInMB = finalMemory / 1024 / 1024;
        assertTrue(finalMemoryInMB < 300, 
            "Final memory usage should be reasonable (actual: " + finalMemoryInMB + "MB)");
        
        assertEquals(LARGE_DATASET_SIZE / chunkSize, chunksProcessed, 
            "Should have processed expected number of chunks");
    }
}