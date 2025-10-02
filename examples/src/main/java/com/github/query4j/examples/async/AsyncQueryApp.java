package com.github.query4j.examples.async;

import com.github.query4j.core.QueryBuilder;
import com.github.query4j.examples.model.User;
import com.github.query4j.examples.model.Order;
import com.github.query4j.examples.model.Product;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Example application demonstrating asynchronous query execution patterns 
 * using Query4j dynamic query builder.
 * 
 * Features demonstrated:
 * - Concurrent query execution with thread-safe builders
 * - CompletableFuture patterns for async operations
 * - Result aggregation from multiple async queries
 * - Thread pool management and resource optimization
 * - Error handling in concurrent environments
 * - Performance monitoring for async operations
 * 
 * This example shows how to:
 * - Execute multiple queries concurrently
 * - Aggregate results asynchronously
 * - Implement parallel data processing pipelines
 * - Handle exceptions in async workflows
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
public class AsyncQueryApp {
    
    private static final Logger logger = Logger.getLogger(AsyncQueryApp.class.getName());
    private static final int DEFAULT_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    private static final int QUERY_TIMEOUT_SECONDS = 30;
    
    private final ExecutorService executorService;
    private final AtomicLong queriesExecuted = new AtomicLong(0);
    private final AtomicLong queriesSucceeded = new AtomicLong(0);
    private final AtomicLong queriesFailed = new AtomicLong(0);
    private final AtomicInteger activeQueries = new AtomicInteger(0);
    
    /**
     * Constructs AsyncQueryApp with default thread pool size.
     */
    public AsyncQueryApp() {
        this(DEFAULT_THREAD_POOL_SIZE);
    }
    
    /**
     * Constructs AsyncQueryApp with specified thread pool size.
     * 
     * @param threadPoolSize the number of threads for concurrent query execution
     * @throws IllegalArgumentException if threadPoolSize is not positive
     */
    public AsyncQueryApp(int threadPoolSize) {
        if (threadPoolSize <= 0) {
            throw new IllegalArgumentException("Thread pool size must be positive");
        }
        
        this.executorService = Executors.newFixedThreadPool(threadPoolSize, r -> {
            Thread t = new Thread(r, "AsyncQuery-" + System.currentTimeMillis());
            t.setDaemon(true);
            return t;
        });
        
        logger.info("AsyncQueryApp initialized with " + threadPoolSize + " threads");
    }
    
    /**
     * Main entry point for async query application.
     */
    public static void main(String[] args) {
        AsyncQueryApp app = new AsyncQueryApp();
        
        try {
            logger.info("Starting asynchronous query application...");
            
            // Demonstrate various async query patterns
            app.parallelQueryExecution();
            app.asyncDataAggregation();
            app.concurrentReportGeneration();
            app.asyncPipelineProcessing();
            
            logger.info("Async query application completed successfully");
            app.printStatistics();
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Async query application failed", e);
            System.exit(1);
        } finally {
            app.shutdown();
        }
    }
    
    /**
     * Demonstrates executing multiple independent queries in parallel.
     */
    public void parallelQueryExecution() {
        logger.info("=== Parallel Query Execution ===");
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Create multiple async queries that can run in parallel
            CompletableFuture<List<User>> activeUsersQuery = executeAsyncQuery(
                "ActiveUsers",
                () -> QueryBuilder.forEntity(User.class)
                    .where("active", true)
                    .orderBy("lastName")
                    .findAllAsync()
            );
            
            CompletableFuture<List<User>> recentUsersQuery = executeAsyncQuery(
                "RecentUsers", 
                () -> QueryBuilder.forEntity(User.class)
                    .where("joinDate", ">=", LocalDate.now().minusDays(30))
                    .orderBy("joinDate")
                    .findAllAsync()
            );
            
            CompletableFuture<List<Order>> pendingOrdersQuery = executeAsyncQuery(
                "PendingOrders",
                () -> QueryBuilder.forEntity(Order.class)
                    .where("status", "PENDING")
                    .orderBy("orderDate")
                    .findAllAsync()
            );
            
            CompletableFuture<List<Product>> featuredProductsQuery = executeAsyncQuery(
                "FeaturedProducts",
                () -> QueryBuilder.forEntity(Product.class)
                    .where("featured", true)
                    .whereIsNotNull("price")
                    .orderBy("name")
                    .findAllAsync()
            );
            
            // Wait for all queries to complete
            CompletableFuture<Void> allQueries = CompletableFuture.allOf(
                activeUsersQuery, recentUsersQuery, pendingOrdersQuery, featuredProductsQuery
            );
            
            // Apply timeout to prevent hanging
            allQueries.get(QUERY_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            
            // Process results when all queries complete
            List<User> activeUsers = activeUsersQuery.get();
            List<User> recentUsers = recentUsersQuery.get();
            List<Order> pendingOrders = pendingOrdersQuery.get();
            List<Product> featuredProducts = featuredProductsQuery.get();
            
            long duration = System.currentTimeMillis() - startTime;
            
            logger.info("Parallel query execution completed:");
            logger.info("  Active users: " + activeUsers.size());
            logger.info("  Recent users: " + recentUsers.size()); 
            logger.info("  Pending orders: " + pendingOrders.size());
            logger.info("  Featured products: " + featuredProducts.size());
            logger.info("  Total duration: " + duration + "ms");
            
            // Demonstrate result processing
            processParallelResults(activeUsers, recentUsers, pendingOrders, featuredProducts);
            
        } catch (TimeoutException e) {
            logger.log(Level.SEVERE, "Parallel queries timed out after " + QUERY_TIMEOUT_SECONDS + " seconds", e);
            throw new RuntimeException("Query timeout", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to execute parallel queries", e);
            throw new RuntimeException("Parallel query execution failed", e);
        }
    }
    
    /**
     * Demonstrates async data aggregation from multiple sources.
     */
    public void asyncDataAggregation() {
        logger.info("=== Async Data Aggregation ===");
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Async aggregation queries
            CompletableFuture<Long> totalUsersCount = executeAsyncQuery(
                "UserCount",
                () -> QueryBuilder.forEntity(User.class).countAsync()
            );
            
            CompletableFuture<Long> activeUsersCount = executeAsyncQuery(
                "ActiveUserCount", 
                () -> QueryBuilder.forEntity(User.class)
                    .where("active", true)
                    .countAsync()
            );
            
            CompletableFuture<Long> totalOrdersCount = executeAsyncQuery(
                "OrderCount",
                () -> QueryBuilder.forEntity(Order.class).countAsync()
            );
            
            CompletableFuture<Long> recentOrdersCount = executeAsyncQuery(
                "RecentOrderCount",
                () -> QueryBuilder.forEntity(Order.class)
                    .where("orderDate", ">=", LocalDate.now().minusDays(7))
                    .countAsync()
            );
            
            // Combine results asynchronously
            CompletableFuture<DashboardMetrics> metricsResult = CompletableFuture
                .allOf(totalUsersCount, activeUsersCount, totalOrdersCount, recentOrdersCount)
                .thenApply(ignored -> {
                    try {
                        DashboardMetrics metrics = new DashboardMetrics();
                        metrics.totalUsers = totalUsersCount.get();
                        metrics.activeUsers = activeUsersCount.get();
                        metrics.totalOrders = totalOrdersCount.get();
                        metrics.recentOrders = recentOrdersCount.get();
                        return metrics;
                    } catch (Exception e) {
                        throw new CompletionException("Failed to aggregate metrics", e);
                    }
                });
            
            // Get aggregated results
            DashboardMetrics metrics = metricsResult.get(QUERY_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            
            long duration = System.currentTimeMillis() - startTime;
            
            logger.info("Async data aggregation completed:");
            logger.info("  Total users: " + metrics.totalUsers);
            logger.info("  Active users: " + metrics.activeUsers + " (" + 
                       calculatePercentage(metrics.activeUsers, metrics.totalUsers) + "%)");
            logger.info("  Total orders: " + metrics.totalOrders);
            logger.info("  Recent orders: " + metrics.recentOrders + " (" + 
                       calculatePercentage(metrics.recentOrders, metrics.totalOrders) + "%)");
            logger.info("  Aggregation duration: " + duration + "ms");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to perform async data aggregation", e);
            throw new RuntimeException("Async aggregation failed", e);
        }
    }
    
    /**
     * Demonstrates concurrent report generation with multiple data sources.
     */
    public void concurrentReportGeneration() {
        logger.info("=== Concurrent Report Generation ===");
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Generate multiple reports concurrently
            CompletableFuture<UserReport> userReportFuture = generateUserReportAsync();
            CompletableFuture<OrderReport> orderReportFuture = generateOrderReportAsync();
            CompletableFuture<ProductReport> productReportFuture = generateProductReportAsync();
            
            // Wait for all reports to complete
            CompletableFuture<CombinedReport> combinedReportFuture = 
                CompletableFuture.allOf(userReportFuture, orderReportFuture, productReportFuture)
                .thenApply(ignored -> {
                    try {
                        CombinedReport report = new CombinedReport();
                        report.userReport = userReportFuture.get();
                        report.orderReport = orderReportFuture.get();
                        report.productReport = productReportFuture.get();
                        report.generationTime = LocalDateTime.now();
                        return report;
                    } catch (Exception e) {
                        throw new CompletionException("Failed to combine reports", e);
                    }
                });
            
            // Get final combined report
            CombinedReport report = combinedReportFuture.get(QUERY_TIMEOUT_SECONDS * 2, TimeUnit.SECONDS);
            
            long duration = System.currentTimeMillis() - startTime;
            
            logger.info("Concurrent report generation completed:");
            logger.info("  User report: " + report.userReport.summary);
            logger.info("  Order report: " + report.orderReport.summary);
            logger.info("  Product report: " + report.productReport.summary);
            logger.info("  Total generation time: " + duration + "ms");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to generate concurrent reports", e);
            throw new RuntimeException("Report generation failed", e);
        }
    }
    
    /**
     * Demonstrates async pipeline processing with chained operations.
     */
    public void asyncPipelineProcessing() {
        logger.info("=== Async Pipeline Processing ===");
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Create an async processing pipeline
            CompletableFuture<List<String>> pipeline = QueryBuilder.forEntity(User.class)
                .where("active", true)
                .findAllAsync()
                .thenCompose(users -> {
                    // Stage 2: Filter VIP users asynchronously
                    return CompletableFuture.supplyAsync(() -> {
                        logger.info("Pipeline: Filtering VIP users from " + users.size() + " active users");
                        return users.stream()
                            .filter(user -> Boolean.TRUE.equals(user.getVipStatus()))
                            .collect(Collectors.toList());
                    }, executorService);
                })
                .thenCompose(vipUsers -> {
                    // Stage 3: Get orders for VIP users asynchronously
                    logger.info("Pipeline: Getting orders for " + vipUsers.size() + " VIP users");
                    
                    if (vipUsers.isEmpty()) {
                        return CompletableFuture.completedFuture(new ArrayList<Order>());
                    }
                    
                    List<Long> vipUserIds = vipUsers.stream()
                        .map(User::getId)
                        .collect(Collectors.toList());
                    
                    return QueryBuilder.forEntity(Order.class)
                        .whereIn("customerId", vipUserIds.stream()
                            .map(Object.class::cast)
                            .collect(Collectors.toList()))
                        .where("status", "COMPLETED")
                        .findAllAsync();
                })
                .thenApply(orders -> {
                    // Stage 4: Generate summary report
                    logger.info("Pipeline: Processing " + orders.size() + " VIP orders");
                    
                    return orders.stream()
                        .map(order -> "Order " + order.getOrderNumber() + 
                             " - Amount: " + order.getTotalAmount())
                        .collect(Collectors.toList());
                })
                .exceptionally(throwable -> {
                    logger.log(Level.SEVERE, "Pipeline processing failed", throwable);
                    return Arrays.asList("Pipeline failed: " + throwable.getMessage());
                });
            
            // Get pipeline result
            List<String> pipelineResult = pipeline.get(QUERY_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            
            long duration = System.currentTimeMillis() - startTime;
            
            logger.info("Async pipeline processing completed:");
            logger.info("  Pipeline result entries: " + pipelineResult.size());
            logger.info("  Pipeline duration: " + duration + "ms");
            
            // Log first few results as examples
            pipelineResult.stream()
                .limit(5)
                .forEach(result -> logger.info("  " + result));
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed async pipeline processing", e);
            throw new RuntimeException("Pipeline processing failed", e);
        }
    }
    
    /**
     * Generic method to execute async queries with monitoring.
     */
    private <T> CompletableFuture<T> executeAsyncQuery(String queryName, Supplier<CompletableFuture<T>> querySupplier) {
        queriesExecuted.incrementAndGet();
        activeQueries.incrementAndGet();
        
        long startTime = System.currentTimeMillis();
        
        return CompletableFuture
            .supplyAsync(() -> {
                try {
                    logger.fine("Executing async query: " + queryName);
                    return querySupplier.get();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Query " + queryName + " failed", e);
                    throw new CompletionException("Query failed: " + queryName, e);
                }
            }, executorService)
            .thenCompose(future -> future)
            .whenComplete((result, throwable) -> {
                activeQueries.decrementAndGet();
                long duration = System.currentTimeMillis() - startTime;
                
                if (throwable == null) {
                    queriesSucceeded.incrementAndGet();
                    logger.fine("Query " + queryName + " completed successfully in " + duration + "ms");
                } else {
                    queriesFailed.incrementAndGet();
                    logger.log(Level.WARNING, "Query " + queryName + " failed after " + duration + "ms", throwable);
                }
            });
    }
    
    /**
     * Generates user report asynchronously.
     */
    private CompletableFuture<UserReport> generateUserReportAsync() {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Generating user report...");
            
            // Simulate report generation with multiple queries
            try {
                Thread.sleep(500); // Simulate processing time
                
                UserReport report = new UserReport();
                report.summary = "User Report: Active users analysis completed";
                report.generatedAt = LocalDateTime.now();
                
                return report;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new CompletionException("User report generation interrupted", e);
            }
        }, executorService);
    }
    
    /**
     * Generates order report asynchronously.
     */
    private CompletableFuture<OrderReport> generateOrderReportAsync() {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Generating order report...");
            
            try {
                Thread.sleep(700); // Simulate processing time
                
                OrderReport report = new OrderReport();
                report.summary = "Order Report: Sales analysis and trends completed";
                report.generatedAt = LocalDateTime.now();
                
                return report;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new CompletionException("Order report generation interrupted", e);
            }
        }, executorService);
    }
    
    /**
     * Generates product report asynchronously.
     */
    private CompletableFuture<ProductReport> generateProductReportAsync() {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Generating product report...");
            
            try {
                Thread.sleep(300); // Simulate processing time
                
                ProductReport report = new ProductReport();
                report.summary = "Product Report: Inventory and performance analysis completed";
                report.generatedAt = LocalDateTime.now();
                
                return report;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new CompletionException("Product report generation interrupted", e);
            }
        }, executorService);
    }
    
    /**
     * Processes results from parallel query execution.
     */
    private void processParallelResults(List<User> activeUsers, List<User> recentUsers, 
                                      List<Order> pendingOrders, List<Product> featuredProducts) {
        logger.info("Processing parallel query results...");
        
        // Example processing: Find overlap between active and recent users
        long activeRecentOverlap = activeUsers.stream()
            .filter(recentUsers::contains)
            .count();
        
        logger.info("Active users who joined recently: " + activeRecentOverlap);
        
        // Example processing: Calculate metrics
        double avgOrderValue = pendingOrders.stream()
            .filter(order -> order.getTotalAmount() != null)
            .mapToDouble(order -> order.getTotalAmount().doubleValue())
            .average()
            .orElse(0.0);
        
        logger.info("Average pending order value: $" + String.format("%.2f", avgOrderValue));
    }
    
    /**
     * Prints execution statistics.
     */
    public void printStatistics() {
        logger.info("=== Async Query Statistics ===");
        logger.info("Total queries executed: " + queriesExecuted.get());
        logger.info("Queries succeeded: " + queriesSucceeded.get());
        logger.info("Queries failed: " + queriesFailed.get());
        logger.info("Active queries: " + activeQueries.get());
        
        if (queriesExecuted.get() > 0) {
            double successRate = (queriesSucceeded.get() * 100.0) / queriesExecuted.get();
            logger.info("Success rate: " + String.format("%.1f%%", successRate));
        }
    }
    
    /**
     * Shuts down the thread pool and releases resources.
     */
    public void shutdown() {
        logger.info("Shutting down async query application...");
        
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    private double calculatePercentage(long part, long total) {
        return total == 0 ? 0.0 : (part * 100.0) / total;
    }
    
    // Data classes for reports and metrics
    
    private static class DashboardMetrics {
        long totalUsers;
        long activeUsers;
        long totalOrders;
        long recentOrders;
    }
    
    private static class UserReport {
        String summary;
        LocalDateTime generatedAt;
    }
    
    private static class OrderReport {
        String summary;
        LocalDateTime generatedAt;
    }
    
    private static class ProductReport {
        String summary;
        LocalDateTime generatedAt;
    }
    
    private static class CombinedReport {
        UserReport userReport;
        OrderReport orderReport;
        ProductReport productReport;
        LocalDateTime generationTime;
    }
    
    // Getters for testing
    public long getQueriesExecuted() { return queriesExecuted.get(); }
    public long getQueriesSucceeded() { return queriesSucceeded.get(); }
    public long getQueriesFailed() { return queriesFailed.get(); }
    public int getActiveQueries() { return activeQueries.get(); }
}