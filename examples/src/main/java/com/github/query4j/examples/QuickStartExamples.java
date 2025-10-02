package com.github.query4j.examples;

import com.github.query4j.core.QueryBuilder;
import com.github.query4j.core.DynamicQuery;
import com.github.query4j.core.Page;
import com.github.query4j.examples.model.User;
import com.github.query4j.examples.model.Order;
import com.github.query4j.examples.model.Product;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Quick start examples demonstrating Query4j API entry points.
 * 
 * This class showcases the primary API patterns and usage for the
 * Query4j Dynamic Query Builder library.
 */
public class QuickStartExamples {

    /**
     * Basic query construction examples.
     */
    public void basicQueries() {
        System.out.println("=== Basic Query Examples ===");
        
        // Simple equality query
        List<User> activeUsers = QueryBuilder.forEntity(User.class)
            .where("active", true)
            .findAll();
        
        System.out.println("Found " + activeUsers.size() + " active users");

        // Multiple conditions with logical operators
        List<User> engineeringUsers = QueryBuilder.forEntity(User.class)
            .where("department", "Engineering")
            .and()
            .where("active", true)
            .orderBy("lastName")
            .findAll();

        // Complex conditions with grouping
        List<User> complexQuery = QueryBuilder.forEntity(User.class)
            .openGroup()
                .where("department", "Engineering")
                .or()
                .where("department", "Product")
            .closeGroup()
            .and()
            .whereIn("role", Arrays.asList("manager", "lead", "architect"))
            .and()
            .where("joinDate", ">", LocalDate.now().minusYears(2))
            .orderByDescending("joinDate")
            .limit(50)
            .findAll();

        System.out.println("Complex query returned " + complexQuery.size() + " users");
    }

    /**
     * Pattern matching and null check examples.
     */
    public void patternMatchingAndNulls() {
        System.out.println("=== Pattern Matching and Null Checks ===");

        // LIKE pattern matching
        List<Product> laptops = QueryBuilder.forEntity(Product.class)
            .whereLike("name", "%laptop%")
            .and()
            .whereIsNotNull("price")
            .and()
            .where("price", ">", 500.0)
            .orderBy("price")
            .findAll();

        // NOT LIKE with null checks
        List<User> validUsers = QueryBuilder.forEntity(User.class)
            .whereIsNotNull("email")
            .and()
            .whereNotLike("email", "%test%")
            .and()
            .whereIsNull("deletedAt")
            .findAll();

        // BETWEEN ranges
        List<Order> recentOrders = QueryBuilder.forEntity(Order.class)
            .whereBetween("orderDate", 
                LocalDate.now().minusDays(30), 
                LocalDate.now())
            .and()
            .where("status", "!=", "CANCELLED")
            .findAll();

        System.out.println("Found " + recentOrders.size() + " recent orders");
    }

    /**
     * JOIN operations and associations.
     */
    public void joinsAndAssociations() {
        System.out.println("=== JOIN Operations ===");

        // Inner join with conditions on related entity
        List<Order> ordersWithCustomers = QueryBuilder.forEntity(Order.class)
            .join("customer")
            .where("customer.country", "USA")
            .and()
            .where("status", "SHIPPED")
            .orderBy("orderDate")
            .findAll();

        // Left join with fetch for eager loading
        List<User> usersWithRoles = QueryBuilder.forEntity(User.class)
            .leftJoin("roles")
            .fetch("roles") // Eager loading to avoid N+1
            .where("active", true)
            .findAll();

        // Multiple joins
        List<Order> complexJoin = QueryBuilder.forEntity(Order.class)
            .join("customer")
            .leftJoin("orderItems")
            .join("orderItems.product")
            .where("customer.vipStatus", true)
            .and()
            .where("orderItems.product.category", "Electronics")
            .findAll();

        System.out.println("Complex join returned " + complexJoin.size() + " orders");
    }

    /**
     * Aggregation and grouping examples.
     */
    public void aggregationAndGrouping() {
        System.out.println("=== Aggregation and Grouping ===");

        // Simple count
        long activeUserCount = QueryBuilder.forEntity(User.class)
            .where("active", true)
            .count();

        System.out.println("Active users count: " + activeUserCount);

        // Existence check (more efficient than count > 0)
        boolean hasActiveUsers = QueryBuilder.forEntity(User.class)
            .where("active", true)
            .exists();

        // Aggregation with grouping
        // Note: In practice, aggregation queries return Object[] for projection results
        List<Order> salesByRegion = QueryBuilder.forEntity(Order.class)
            .select("customer.region")
            .sum("totalAmount")
            .groupBy("customer.region")
            .having("SUM(totalAmount)", ">", 100000.0)
            .orderByDescending("SUM(totalAmount)")
            .findAll();

        // Multiple aggregations  
        List<Order> productStats = QueryBuilder.forEntity(Order.class)
            .select("product.category")
            .countAll()
            .avg("totalAmount")
            .max("totalAmount")
            .min("totalAmount")
            .groupBy("product.category")
            .findAll();

        System.out.println("Product categories with stats: " + productStats.size());
    }

    /**
     * Pagination examples.
     */
    public void pagination() {
        System.out.println("=== Pagination Examples ===");

        // Page-based pagination with metadata
        Page<User> userPage = QueryBuilder.forEntity(User.class)
            .where("active", true)
            .orderBy("lastName")
            .page(0, 20) // First page, 20 items per page
            .findPage();

        System.out.println("Page " + (userPage.getNumber() + 1) + " of " + userPage.getTotalPages());
        System.out.println("Total users: " + userPage.getTotalElements());
        System.out.println("Current page size: " + userPage.getContent().size());

        // Manual pagination with offset/limit
        List<User> manualPage = QueryBuilder.forEntity(User.class)
            .where("department", "Engineering")
            .orderBy("joinDate")
            .offset(50)
            .limit(25)
            .findAll();

        // Iterate through all pages
        int pageNumber = 0;
        int pageSize = 50;
        Page<User> page;
        
        do {
            page = QueryBuilder.forEntity(User.class)
                .where("active", true)
                .orderBy("id")
                .page(pageNumber++, pageSize)
                .findPage();
                
            // Process page content
            processUsers(page.getContent());
            
        } while (page.hasNext());
    }

    /**
     * Caching examples.
     */
    public void caching() {
        System.out.println("=== Caching Examples ===");

        // Enable default caching
        List<User> cachedUsers = QueryBuilder.forEntity(User.class)
            .where("active", true)
            .cached()
            .findAll();

        // Region-specific caching
        List<Product> featuredProducts = QueryBuilder.forEntity(Product.class)
            .where("featured", true)
            .cached("featured-products")
            .findAll();

        // TTL-based caching (1 hour)
        List<User> recentUsers = QueryBuilder.forEntity(User.class)
            .where("joinDate", ">", LocalDate.now().minusDays(7))
            .cached(3600) // 3600 seconds = 1 hour
            .findAll();

        System.out.println("Cached queries executed successfully");
    }

    /**
     * Asynchronous execution examples.
     */
    public void asyncExecution() {
        System.out.println("=== Asynchronous Execution ===");

        // Async query execution
        CompletableFuture<List<User>> futureUsers = QueryBuilder.forEntity(User.class)
            .where("active", true)
            .findAllAsync();

        // Process results when available
        futureUsers.thenAccept(users -> {
            System.out.println("Async query completed: " + users.size() + " users found");
            users.forEach(this::processUser);
        });

        // Async count query
        CompletableFuture<Long> futureCount = QueryBuilder.forEntity(Order.class)
            .where("status", "PENDING")
            .countAsync();

        futureCount.thenAccept(count -> {
            System.out.println("Pending orders: " + count);
        });

        // Combine multiple async queries
        CompletableFuture<List<User>> activeUsers = QueryBuilder.forEntity(User.class)
            .where("active", true)
            .findAllAsync();
            
        CompletableFuture<List<User>> recentUsers = QueryBuilder.forEntity(User.class)
            .where("joinDate", ">", LocalDate.now().minusDays(30))
            .findAllAsync();

        CompletableFuture.allOf(activeUsers, recentUsers)
            .thenRun(() -> {
                System.out.println("All async queries completed");
            });
    }

    /**
     * Query building and reuse examples.
     */
    public void queryBuildingAndReuse() {
        System.out.println("=== Query Building and Reuse ===");

        // Build reusable query
        DynamicQuery<User> activeUsersQuery = QueryBuilder.forEntity(User.class)
            .where("active", true)
            .orderBy("lastName")
            .build();

        // Execute multiple times with optimal performance
        List<User> batch1 = activeUsersQuery.execute();
        List<User> batch2 = activeUsersQuery.execute();

        // Get generated SQL for debugging
        String sql = activeUsersQuery.getSQL();
        System.out.println("Generated SQL: " + sql);

        // Build query with debugging information
        QueryBuilder<User> builder = QueryBuilder.forEntity(User.class)
            .where("department", "Engineering")
            .and()
            .where("salary", ">", 75000);

        // Debug SQL before execution
        String debugSql = builder.toSQL();
        System.out.println("Debug SQL: " + debugSql);

        // Execute and get stats
        List<User> results = builder.findAll();
        // Note: QueryStats would be available in a real implementation
    }

    /**
     * Subquery examples.
     */
    public void subqueries() {
        System.out.println("=== Subquery Examples ===");

        // EXISTS subquery
        List<User> usersWithOrders = QueryBuilder.forEntity(User.class)
            .exists(
                QueryBuilder.forEntity(Order.class)
                    .where("customerId", "users.id") // Correlated subquery
                    .and()
                    .where("status", "COMPLETED")
            )
            .findAll();

        // IN subquery
        List<User> topCustomers = QueryBuilder.forEntity(User.class)
            .in("id", 
                QueryBuilder.forEntity(Order.class)
                    .select("customerId")
                    .groupBy("customerId")
                    .having("SUM(totalAmount)", ">", 10000)
            )
            .findAll();

        // NOT EXISTS subquery
        List<User> usersWithoutOrders = QueryBuilder.forEntity(User.class)
            .notExists(
                QueryBuilder.forEntity(Order.class)
                    .where("customerId", "users.id")
            )
            .findAll();

        System.out.println("Subquery examples completed");
    }

    // Helper methods
    private void processUsers(List<User> users) {
        users.forEach(this::processUser);
    }

    private void processUser(User user) {
        System.out.println("Processing user: " + user.getId());
    }

    /**
     * Main method to run all examples.
     */
    public static void main(String[] args) {
        QuickStartExamples examples = new QuickStartExamples();
        
        try {
            examples.basicQueries();
            examples.patternMatchingAndNulls();
            examples.joinsAndAssociations();
            examples.aggregationAndGrouping();
            examples.pagination();
            examples.caching();
            examples.asyncExecution();
            examples.queryBuildingAndReuse();
            examples.subqueries();
            
            System.out.println("All examples completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Example execution failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}