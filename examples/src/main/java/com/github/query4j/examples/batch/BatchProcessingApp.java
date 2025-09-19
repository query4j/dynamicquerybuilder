package com.github.query4j.examples.batch;

import com.github.query4j.core.QueryBuilder;
import com.github.query4j.core.Page;
import com.github.query4j.examples.model.User;
import com.github.query4j.examples.model.Order;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Example application demonstrating batch processing of large datasets 
 * using Query4j dynamic query builder.
 * 
 * Features demonstrated:
 * - Efficient pagination for large data sets
 * - Batch processing with progress tracking
 * - Fault tolerance and retry mechanisms
 * - Resource usage optimization
 * - Performance monitoring and logging
 * 
 * This example processes users in batches to:
 * - Update inactive user status
 * - Calculate user statistics
 * - Generate reports
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
public class BatchProcessingApp {
    
    private static final Logger logger = Logger.getLogger(BatchProcessingApp.class.getName());
    private static final int DEFAULT_BATCH_SIZE = 1000;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;
    
    private final BatchProcessingConfig config;
    private final BatchMetrics metrics;
    private final CircuitBreaker circuitBreaker;
    private final AtomicLong processedCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    private final AtomicInteger currentPage = new AtomicInteger(0);
    
    /**
     * Constructs a BatchProcessingApp with default configuration.
     */
    public BatchProcessingApp() {
        this(new BatchProcessingConfig());
    }
    
    /**
     * Constructs a BatchProcessingApp with specified configuration.
     * 
     * @param config the batch processing configuration
     * @throws IllegalArgumentException if config is null
     */
    public BatchProcessingApp(BatchProcessingConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration must not be null");
        }
        
        this.config = config;
        this.metrics = new BatchMetrics();
        this.circuitBreaker = new CircuitBreaker(
            "BatchProcessing", 
            config.getCircuitBreakerFailureThreshold(),
            config.getCircuitBreakerMinimumThroughput(),
            config.getCircuitBreakerTimeout()
        );
        
        logger.info("BatchProcessingApp initialized with configuration: " + config);
    }
    
    /**
     * Legacy constructor for backward compatibility.
     * 
     * @param batchSize the number of records to process in each batch
     * @throws IllegalArgumentException if batchSize is not positive
     * @deprecated Use {@link #BatchProcessingApp(BatchProcessingConfig)} instead
     */
    @Deprecated(since = "1.0.0", forRemoval = true)
    public BatchProcessingApp(int batchSize) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("Batch size must be positive");
        }
        
        this.config = new BatchProcessingConfig().setBatchSize(batchSize);
        this.metrics = new BatchMetrics();
        this.circuitBreaker = new CircuitBreaker(
            "BatchProcessing", 
            config.getCircuitBreakerFailureThreshold(),
            config.getCircuitBreakerMinimumThroughput(),
            config.getCircuitBreakerTimeout()
        );
        
        logger.info("BatchProcessingApp initialized with batch size: " + batchSize);
    }
    
    /**
     * Main entry point for batch processing application.
     */
    public static void main(String[] args) {
        BatchProcessingApp app = new BatchProcessingApp();
        
        try {
            logger.info("Starting batch processing application...");
            
            // Run different batch processing scenarios
            app.processInactiveUsers();
            app.generateUserStatistics();
            app.processLargeOrderDataset();
            
            logger.info("Batch processing application completed successfully");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Batch processing failed", e);
            System.exit(1);
        }
    }
    
    /**
     * Processes inactive users in batches, updating their status and generating reports.
     */
    public void processInactiveUsers() {
        logger.info("=== Processing Inactive Users ===");
        
        long startTime = System.currentTimeMillis();
        resetCounters();
        
        try {
            // Build the base query for inactive users
            QueryBuilder<User> baseQuery = QueryBuilder.forEntity(User.class)
                .where("active", false)
                .whereIsNull("deletedAt") // Only process non-deleted users
                .orderBy("id"); // Consistent ordering for pagination
            
            // Get total count for progress tracking
            long totalUsers = baseQuery.count();
            logger.info("Found " + totalUsers + " inactive users to process");
            
            if (totalUsers == 0) {
                logger.info("No inactive users to process");
                return;
            }
            
            // Process in batches with pagination
            Page<User> page;
            int pageNumber = 0;
            int batchSize = config.getBatchSize();
            
            do {
                page = processUserBatch(baseQuery, pageNumber);
                pageNumber++;
                
                // Log progress
                logProgress(totalUsers, startTime);
                
                // Optional: Add small delay to prevent overwhelming the system
                Thread.sleep(100);
                
            } while (page.hasNext());
            
            // Final statistics
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Inactive user processing completed:");
            logger.info("  Total processed: " + processedCount.get());
            logger.info("  Errors: " + errorCount.get());
            logger.info("  Duration: " + duration + "ms");
            logger.info("  Throughput: " + calculateThroughput(processedCount.get(), duration) + " users/sec");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to process inactive users", e);
            throw new RuntimeException("Batch processing failed", e);
        }
    }
    
    /**
     * Processes a single batch of users with retry logic.
     */
    private Page<User> processUserBatch(QueryBuilder<User> baseQuery, int pageNumber) {
        Page<User> page = null;
        int attempt = 0;
        int maxRetries = config.getMaxRetries();
        long retryDelayMs = config.getRetryDelay().toMillis();
        int batchSize = config.getBatchSize();
        
        while (attempt < maxRetries) {
            try {
                // Use circuit breaker pattern for resilience
                page = circuitBreaker.execute(() -> {
                    // Fetch the page with timeout configuration
                    return baseQuery
                        .timeout((int) config.getQueryTimeout().getSeconds())
                        .fetchSize(batchSize)
                        .page(pageNumber, batchSize)
                        .findPage();
                });
                
                List<User> users = page.getContent();
                logger.fine("Processing batch " + pageNumber + " with " + users.size() + " users");
                
                // Process each user in the batch
                for (User user : users) {
                    processIndividualUser(user);
                }
                
                // Update counters and metrics
                processedCount.addAndGet(users.size());
                currentPage.set(pageNumber);
                metrics.recordProcessedRecords(users.size(), java.time.Duration.ofMillis(100)); // Estimated processing time
                break; // Success, exit retry loop
                
            } catch (Exception e) {
                attempt++;
                errorCount.incrementAndGet();
                metrics.recordFailedRecords(1, e.getMessage());
                
                if (attempt >= maxRetries) {
                    logger.log(Level.SEVERE, "Failed to process batch " + pageNumber + " after " + maxRetries + " attempts", e);
                    throw e;
                } else {
                    logger.log(Level.WARNING, "Batch " + pageNumber + " failed, attempt " + attempt + "/" + maxRetries, e);
                    try {
                        Thread.sleep(retryDelayMs * attempt); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during retry delay", ie);
                    }
                }
            }
        }
        
        return page;
    }
    
    /**
     * Processes an individual user record.
     */
    private void processIndividualUser(User user) {
        try {
            // Simulate user processing logic
            // In a real application, this might involve:
            // - Sending notification emails
            // - Updating user statistics
            // - Archiving user data
            // - Generating reports
            
            logger.finest("Processing user: " + user.getId() + " (" + user.getEmail() + ")");
            
            // Example processing: Check if user should be marked for deletion
            if (shouldMarkForDeletion(user)) {
                markUserForDeletion(user);
            }
            
            // Example: Update user metrics
            updateUserMetrics(user);
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to process user " + user.getId(), e);
            throw e;
        }
    }
    
    /**
     * Generates comprehensive statistics for all users in batches.
     */
    public void generateUserStatistics() {
        logger.info("=== Generating User Statistics ===");
        
        long startTime = System.currentTimeMillis();
        resetCounters();
        
        UserStatistics stats = new UserStatistics();
        
        try {
            // Process all users in batches
            QueryBuilder<User> allUsersQuery = QueryBuilder.forEntity(User.class)
                .orderBy("id");
            
            long totalUsers = allUsersQuery.count();
            logger.info("Analyzing " + totalUsers + " total users for statistics");
            
            Page<User> page;
            int pageNumber = 0;
            
            do {
                page = allUsersQuery
                    .page(pageNumber, config.getBatchSize())
                    .findPage();
                
                List<User> users = page.getContent();
                
                // Accumulate statistics for this batch
                for (User user : users) {
                    stats.processUser(user);
                }
                
                processedCount.addAndGet(users.size());
                pageNumber++;
                
                // Log progress every 10 batches
                if (pageNumber % 10 == 0) {
                    logProgress(totalUsers, startTime);
                }
                
            } while (page.hasNext());
            
            // Generate and log final statistics
            stats.generateReport();
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Statistics generation completed in " + duration + "ms");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to generate user statistics", e);
            throw new RuntimeException("Statistics generation failed", e);
        }
    }
    
    /**
     * Processes large order datasets with complex filtering and aggregation.
     */
    public void processLargeOrderDataset() {
        logger.info("=== Processing Large Order Dataset ===");
        
        long startTime = System.currentTimeMillis();
        resetCounters();
        
        try {
            // Complex query for orders requiring processing
            QueryBuilder<Order> orderQuery = QueryBuilder.forEntity(Order.class)
                .where("status", "PENDING")
                .and()
                .where("orderDate", ">=", LocalDate.now().minusDays(30))
                .and()
                .whereIsNotNull("totalAmount")
                .orderBy("orderDate")
                .orderBy("id"); // Secondary sort for consistent pagination
            
            long totalOrders = orderQuery.count();
            logger.info("Found " + totalOrders + " pending orders to process");
            
            if (totalOrders == 0) {
                logger.info("No pending orders to process");
                return;
            }
            
            // Process orders in batches
            Page<Order> page;
            int pageNumber = 0;
            OrderProcessor processor = new OrderProcessor();
            
            do {
                page = orderQuery
                    .page(pageNumber, config.getBatchSize())
                    .findPage();
                
                List<Order> orders = page.getContent();
                logger.info("Processing order batch " + pageNumber + " with " + orders.size() + " orders");
                
                // Process the batch with business logic
                processor.processBatch(orders);
                
                processedCount.addAndGet(orders.size());
                pageNumber++;
                
                logProgress(totalOrders, startTime);
                
            } while (page.hasNext());
            
            // Generate processing summary
            processor.generateSummary();
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Order processing completed:");
            logger.info("  Orders processed: " + processedCount.get());
            logger.info("  Duration: " + duration + "ms");
            logger.info("  Throughput: " + calculateThroughput(processedCount.get(), duration) + " orders/sec");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to process order dataset", e);
            throw new RuntimeException("Order processing failed", e);
        }
    }
    
    // Helper methods
    
    private void resetCounters() {
        processedCount.set(0);
        errorCount.set(0);
        currentPage.set(0);
    }
    
    private void logProgress(long totalRecords, long startTime) {
        long processed = processedCount.get();
        long elapsed = System.currentTimeMillis() - startTime;
        double percentage = (processed * 100.0) / totalRecords;
        double throughput = calculateThroughput(processed, elapsed);
        
        logger.info(String.format("Progress: %.1f%% (%d/%d) - %.1f records/sec - Errors: %d", 
            percentage, processed, totalRecords, throughput, errorCount.get()));
    }
    
    private double calculateThroughput(long recordsProcessed, long elapsedMs) {
        if (elapsedMs == 0) return 0.0;
        return (recordsProcessed * 1000.0) / elapsedMs;
    }
    
    private boolean shouldMarkForDeletion(User user) {
        // Example business logic: Mark users inactive for more than 2 years
        if (user.getUpdatedAt() == null) return false;
        return user.getUpdatedAt().isBefore(LocalDateTime.now().minusYears(2));
    }
    
    private void markUserForDeletion(User user) {
        // Simulate marking user for deletion
        logger.info("Marking user " + user.getId() + " for deletion (inactive for 2+ years)");
        // In real implementation, this would update the database
        user.setDeletedAt(LocalDateTime.now());
    }
    
    private void updateUserMetrics(User user) {
        // Simulate updating user metrics
        logger.finest("Updating metrics for user " + user.getId());
        // In real implementation, this might update analytics tables
    }
    
    /**
     * Inner class for accumulating user statistics during batch processing.
     */
    private static class UserStatistics {
        private long totalUsers = 0;
        private long activeUsers = 0;
        private long inactiveUsers = 0;
        private long vipUsers = 0;
        private AtomicInteger departmentCount = new AtomicInteger(0);
        
        void processUser(User user) {
            totalUsers++;
            
            if (Boolean.TRUE.equals(user.getActive())) {
                activeUsers++;
            } else {
                inactiveUsers++;
            }
            
            if (Boolean.TRUE.equals(user.getVipStatus())) {
                vipUsers++;
            }
            
            if (user.getDepartment() != null) {
                departmentCount.incrementAndGet();
            }
        }
        
        void generateReport() {
            logger.info("=== USER STATISTICS REPORT ===");
            logger.info("Total Users: " + totalUsers);
            logger.info("Active Users: " + activeUsers + " (" + percentage(activeUsers, totalUsers) + "%)");
            logger.info("Inactive Users: " + inactiveUsers + " (" + percentage(inactiveUsers, totalUsers) + "%)");
            logger.info("VIP Users: " + vipUsers + " (" + percentage(vipUsers, totalUsers) + "%)");
            logger.info("Users with Department: " + departmentCount.get() + " (" + percentage(departmentCount.get(), totalUsers) + "%)");
        }
        
        private double percentage(long part, long total) {
            return total == 0 ? 0.0 : (part * 100.0) / total;
        }
    }
    
    /**
     * Inner class for processing order batches with business logic.
     */
    private static class OrderProcessor {
        private long processedOrders = 0;
        private long cancelledOrders = 0;
        private long approvedOrders = 0;
        
        void processBatch(List<Order> orders) {
            for (Order order : orders) {
                processOrder(order);
            }
        }
        
        private void processOrder(Order order) {
            processedOrders++;
            
            // Example business logic: Auto-approve small orders, flag large ones
            if (order.getTotalAmount() != null) {
                if (order.getTotalAmount().doubleValue() < 100.0) {
                    approveOrder(order);
                } else {
                    // Large orders might need manual review
                    logger.fine("Flagging large order " + order.getId() + " for manual review");
                }
            }
        }
        
        private void approveOrder(Order order) {
            approvedOrders++;
            logger.finest("Auto-approving small order " + order.getId());
            // In real implementation, would update order status
        }
        
        void generateSummary() {
            logger.info("=== ORDER PROCESSING SUMMARY ===");
            logger.info("Total Orders Processed: " + processedOrders);
            logger.info("Auto-Approved Orders: " + approvedOrders);
            logger.info("Orders Flagged for Review: " + (processedOrders - approvedOrders - cancelledOrders));
        }
    }
    
    // Getters for testing and monitoring
    public long getProcessedCount() { return processedCount.get(); }
    public long getErrorCount() { return errorCount.get(); }
    public int getCurrentPage() { return currentPage.get(); }
    public int getBatchSize() { return config.getBatchSize(); }
    public BatchProcessingConfig getConfig() { return config; }
    public BatchMetrics getMetrics() { return metrics; }
    public CircuitBreaker getCircuitBreaker() { return circuitBreaker; }
    
    /**
     * Provides comprehensive health check information.
     * 
     * @return health status with detailed metrics
     */
    public HealthStatus getHealthStatus() {
        HealthStatus.HealthStatusBuilder builder = HealthStatus.builder();
        
        // Check overall processing health
        long totalProcessed = processedCount.get();
        long totalErrors = errorCount.get();
        double errorRate = totalProcessed > 0 ? (double) totalErrors / totalProcessed : 0.0;
        
        if (errorRate > 0.1) { // More than 10% error rate
            builder.status("UNHEALTHY").detail("error_rate", String.format("%.2f%%", errorRate * 100));
        } else if (errorRate > 0.05) { // More than 5% error rate
            builder.status("DEGRADED").detail("error_rate", String.format("%.2f%%", errorRate * 100));
        } else {
            builder.status("HEALTHY").detail("error_rate", String.format("%.2f%%", errorRate * 100));
        }
        
        // Add circuit breaker status
        builder.detail("circuit_breaker_state", circuitBreaker.getState().toString());
        builder.detail("circuit_breaker_failure_count", String.valueOf(circuitBreaker.getFailureCount()));
        
        // Add processing statistics
        builder.detail("processed_count", String.valueOf(totalProcessed));
        builder.detail("error_count", String.valueOf(totalErrors));
        builder.detail("current_page", String.valueOf(currentPage.get()));
        builder.detail("batch_size", String.valueOf(config.getBatchSize()));
        
        // Add metrics if available
        if (metrics != null) {
            BatchMetrics.MetricsSnapshot snapshot = metrics.getSnapshot();
            builder.detail("throughput_per_second", String.valueOf(snapshot.processedRecords));
            builder.detail("failed_records", String.valueOf(snapshot.failedRecords));
            builder.detail("error_rate", String.format("%.2f%%", snapshot.getErrorRate() * 100));
            builder.detail("retry_rate", String.format("%.2f%%", snapshot.getRetryRate() * 100));
        }
        
        return builder.build();
    }
    
    /**
     * Simple health status representation.
     */
    public static class HealthStatus {
        private final String status;
        private final Map<String, String> details;
        
        private HealthStatus(String status, Map<String, String> details) {
            this.status = status;
            this.details = details;
        }
        
        public String getStatus() { return status; }
        public Map<String, String> getDetails() { return details; }
        
        public static HealthStatusBuilder builder() {
            return new HealthStatusBuilder();
        }
        
        @Override
        public String toString() {
            return "HealthStatus{status='" + status + "', details=" + details + "}";
        }
        
        public static class HealthStatusBuilder {
            private String status = "UNKNOWN";
            private final Map<String, String> details = new java.util.HashMap<>();
            
            public HealthStatusBuilder status(String status) {
                this.status = status;
                return this;
            }
            
            public HealthStatusBuilder detail(String key, String value) {
                this.details.put(key, value);
                return this;
            }
            
            public HealthStatus build() {
                return new HealthStatus(status, java.util.Collections.unmodifiableMap(details));
            }
        }
    }
}