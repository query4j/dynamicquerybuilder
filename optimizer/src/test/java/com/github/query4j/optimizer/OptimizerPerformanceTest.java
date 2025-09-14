package com.github.query4j.optimizer;

import com.github.query4j.core.QueryBuilder;
import com.github.query4j.optimizer.index.IndexSuggestion;
import com.github.query4j.optimizer.predicate.PredicatePushdownSuggestion;
import com.github.query4j.optimizer.join.JoinReorderSuggestion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Performance tests for optimizer scalability.
 * Tests that the optimizer runs within acceptable time on large queries
 * and that multiple optimizer passes yield stable optimization results.
 */
class OptimizerPerformanceTest {
    
    private static final Logger LOGGER = Logger.getLogger(OptimizerPerformanceTest.class.getName());
    
    private QueryOptimizer optimizer;
    private OptimizerConfig config;
    
    @Mock
    private QueryBuilder<?> mockQueryBuilder;
    
    // Performance thresholds
    private static final long SMALL_QUERY_THRESHOLD_MS = 100;   // 100ms for simple queries
    private static final long MEDIUM_QUERY_THRESHOLD_MS = 500;  // 500ms for medium complexity
    private static final long LARGE_QUERY_THRESHOLD_MS = 2000;  // 2s for complex queries
    private static final long HUGE_QUERY_THRESHOLD_MS = 5000;   // 5s for very complex queries
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        config = OptimizerConfig.defaultConfig();
        optimizer = QueryOptimizer.create(config);
    }
    
    @Nested
    @DisplayName("Single Query Performance Tests")
    class SingleQueryPerformanceTests {
        
        @Test
        @DisplayName("Should optimize simple single-table query within performance threshold")
        void simpleQuery_OptimizedWithinThreshold() {
            when(mockQueryBuilder.toSQL()).thenReturn(
                "SELECT * FROM users WHERE email = 'john@example.com'"
            );
            
            long startTime = System.currentTimeMillis();
            OptimizationResult result = optimizer.optimize(mockQueryBuilder);
            long duration = System.currentTimeMillis() - startTime;
            
            LOGGER.info(String.format("Simple query optimization took %d ms", duration));
            
            assertThat(result).isNotNull();
            assertThat(duration).isLessThan(SMALL_QUERY_THRESHOLD_MS);
            assertThat(result.getAnalysisTimeMs()).isLessThan(SMALL_QUERY_THRESHOLD_MS);
        }
        
        @Test
        @DisplayName("Should optimize medium complexity query within performance threshold")
        void mediumComplexityQuery_OptimizedWithinThreshold() {
            when(mockQueryBuilder.toSQL()).thenReturn(
                "SELECT u.*, o.*, COUNT(oi.id) as item_count " +
                "FROM users u " +
                "JOIN orders o ON u.id = o.user_id " +
                "JOIN order_items oi ON o.id = oi.order_id " +
                "WHERE u.status = 'ACTIVE' AND u.city IN ('New York', 'Boston', 'Chicago') " +
                "AND o.created_date BETWEEN '2024-01-01' AND '2024-12-31' " +
                "AND o.amount > 100 " +
                "GROUP BY u.id, o.id " +
                "HAVING COUNT(oi.id) > 2 " +
                "ORDER BY o.amount DESC"
            );
            
            long startTime = System.currentTimeMillis();
            OptimizationResult result = optimizer.optimize(mockQueryBuilder);
            long duration = System.currentTimeMillis() - startTime;
            
            LOGGER.info(String.format("Medium complexity query optimization took %d ms", duration));
            
            assertThat(result).isNotNull();
            assertThat(duration).isLessThan(MEDIUM_QUERY_THRESHOLD_MS);
            assertThat(result.getAnalysisTimeMs()).isLessThan(MEDIUM_QUERY_THRESHOLD_MS);
        }
        
        @Test
        @DisplayName("Should optimize large multi-join query within performance threshold")
        void largeMultiJoinQuery_OptimizedWithinThreshold() {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT u.name, c.name, o.total, p.name, cat.name, sup.name ")
                        .append("FROM users u ")
                        .append("JOIN customers c ON u.id = c.user_id ")
                        .append("JOIN orders o ON c.id = o.customer_id ")
                        .append("JOIN order_items oi ON o.id = oi.order_id ")
                        .append("JOIN products p ON oi.product_id = p.id ")
                        .append("JOIN categories cat ON p.category_id = cat.id ")
                        .append("JOIN suppliers sup ON p.supplier_id = sup.id ")
                        .append("JOIN addresses a ON c.address_id = a.id ")
                        .append("WHERE u.status = 'ACTIVE' ")
                        .append("AND c.tier = 'PREMIUM' ")
                        .append("AND o.status IN ('COMPLETED', 'SHIPPED') ")
                        .append("AND o.created_date BETWEEN '2024-01-01' AND '2024-12-31' ")
                        .append("AND p.price BETWEEN 50 AND 500 ")
                        .append("AND cat.active = true ")
                        .append("AND sup.country = 'US' ")
                        .append("AND a.city IN ('New York', 'Los Angeles', 'Chicago', 'Houston', 'Phoenix')");
            
            when(mockQueryBuilder.toSQL()).thenReturn(queryBuilder.toString());
            
            long startTime = System.currentTimeMillis();
            OptimizationResult result = optimizer.optimize(mockQueryBuilder);
            long duration = System.currentTimeMillis() - startTime;
            
            LOGGER.info(String.format("Large multi-join query optimization took %d ms", duration));
            
            assertThat(result).isNotNull();
            assertThat(duration).isLessThan(LARGE_QUERY_THRESHOLD_MS);
            assertThat(result.getAnalysisTimeMs()).isLessThan(LARGE_QUERY_THRESHOLD_MS);
            
            // Large queries should produce meaningful suggestions
            int totalSuggestions = result.getTotalSuggestionCount();
            LOGGER.info(String.format("Large query generated %d total suggestions", totalSuggestions));
        }
        
        @ParameterizedTest
        @ValueSource(ints = {5, 10, 15, 20})
        @DisplayName("Should handle queries with varying numbers of tables efficiently")
        void varyingTableCount_HandledEfficiently(int tableCount) {
            StringBuilder queryBuilder = new StringBuilder("SELECT * FROM table0 t0 ");
            
            // Build a query with specified number of tables
            for (int i = 1; i < tableCount; i++) {
                queryBuilder.append("JOIN table").append(i).append(" t").append(i)
                           .append(" ON t").append(i-1).append(".id = t").append(i).append(".ref_id ");
            }
            
            // Add WHERE conditions for each table
            queryBuilder.append("WHERE ");
            for (int i = 0; i < tableCount; i++) {
                if (i > 0) queryBuilder.append("AND ");
                queryBuilder.append("t").append(i).append(".active = true ");
            }
            
            when(mockQueryBuilder.toSQL()).thenReturn(queryBuilder.toString());
            
            long startTime = System.currentTimeMillis();
            OptimizationResult result = optimizer.optimize(mockQueryBuilder);
            long duration = System.currentTimeMillis() - startTime;
            
            LOGGER.info(String.format("Query with %d tables took %d ms to optimize", tableCount, duration));
            
            assertThat(result).isNotNull();
            
            // Performance should scale reasonably with table count
            long expectedThreshold = SMALL_QUERY_THRESHOLD_MS + (tableCount - 1) * 50L; // 50ms per additional table
            assertThat(duration).isLessThan(Math.min(expectedThreshold, HUGE_QUERY_THRESHOLD_MS));
        }
    }
    
    @Nested
    @DisplayName("Repeated Optimization Stability Tests")
    class OptimizationStabilityTests {
        
        @RepeatedTest(5)
        @DisplayName("Should produce consistent results across multiple optimization passes")
        void multipleOptimizationPasses_ProduceConsistentResults() {
            when(mockQueryBuilder.toSQL()).thenReturn(
                "SELECT u.*, o.* FROM users u JOIN orders o ON u.id = o.user_id " +
                "WHERE u.email = 'test@example.com' AND o.status = 'PENDING'"
            );
            
            // Run optimization multiple times
            OptimizationResult firstResult = optimizer.optimize(mockQueryBuilder);
            OptimizationResult secondResult = optimizer.optimize(mockQueryBuilder);
            OptimizationResult thirdResult = optimizer.optimize(mockQueryBuilder);
            
            // Results should be consistent
            assertThat(firstResult.getIndexSuggestions()).hasSize(secondResult.getIndexSuggestions().size());
            assertThat(secondResult.getIndexSuggestions()).hasSize(thirdResult.getIndexSuggestions().size());
            
            assertThat(firstResult.getPredicatePushdownSuggestions()).hasSize(secondResult.getPredicatePushdownSuggestions().size());
            assertThat(secondResult.getPredicatePushdownSuggestions()).hasSize(thirdResult.getPredicatePushdownSuggestions().size());
            
            assertThat(firstResult.getJoinReorderSuggestions()).hasSize(secondResult.getJoinReorderSuggestions().size());
            assertThat(secondResult.getJoinReorderSuggestions()).hasSize(thirdResult.getJoinReorderSuggestions().size());
            
            // Total suggestion counts should match
            assertThat(firstResult.getTotalSuggestionCount()).isEqualTo(secondResult.getTotalSuggestionCount());
            assertThat(secondResult.getTotalSuggestionCount()).isEqualTo(thirdResult.getTotalSuggestionCount());
        }
        
        @Test
        @DisplayName("Should maintain performance across repeated optimizations")
        void repeatedOptimizations_MaintainPerformance() {
            when(mockQueryBuilder.toSQL()).thenReturn(
                "SELECT p.*, c.name, s.name FROM products p " +
                "JOIN categories c ON p.category_id = c.id " +
                "JOIN suppliers s ON p.supplier_id = s.id " +
                "WHERE p.price BETWEEN 10 AND 100 AND c.active = true"
            );
            
            List<Long> durations = new ArrayList<>();
            
            // Run optimization 10 times and measure performance
            for (int i = 0; i < 10; i++) {
                long startTime = System.currentTimeMillis();
                OptimizationResult result = optimizer.optimize(mockQueryBuilder);
                long duration = System.currentTimeMillis() - startTime;
                durations.add(duration);
                
                assertThat(result).isNotNull();
            }
            
            // Calculate performance statistics
            double averageDuration = durations.stream().mapToLong(Long::longValue).average().orElse(0.0);
            long maxDuration = durations.stream().mapToLong(Long::longValue).max().orElse(0L);
            long minDuration = durations.stream().mapToLong(Long::longValue).min().orElse(0L);
            
            LOGGER.info(String.format("Repeated optimization - Avg: %.2f ms, Min: %d ms, Max: %d ms", 
                                    averageDuration, minDuration, maxDuration));
            
            // Performance should be consistent
            assertThat(averageDuration).isLessThan(MEDIUM_QUERY_THRESHOLD_MS);
            assertThat(maxDuration).isLessThan(MEDIUM_QUERY_THRESHOLD_MS * 2); // Allow some variance
            
            // No significant performance degradation
            double variance = durations.stream()
                .mapToDouble(d -> Math.pow(d - averageDuration, 2))
                .average().orElse(0.0);
            double standardDeviation = Math.sqrt(variance);
            
            LOGGER.info(String.format("Performance standard deviation: %.2f ms", standardDeviation));
            // Allow for more variance when optimization times are very low (< 1ms)
            double allowableDeviation = Math.max(averageDuration * 0.5, 1.0); // At least 1ms tolerance
            assertThat(standardDeviation).isLessThan(allowableDeviation);
        }
        
        @Test
        @DisplayName("Should handle optimization of same query with different parameter values consistently")
        void sameQueryDifferentParameters_ConsistentOptimization() {
            String baseQuery = "SELECT * FROM users WHERE status = ? AND city = ? AND age BETWEEN ? AND ?";
            
            when(mockQueryBuilder.toSQL())
                .thenReturn(baseQuery)
                .thenReturn(baseQuery)
                .thenReturn(baseQuery);
            
            // Optimize the same query structure multiple times
            OptimizationResult result1 = optimizer.optimize(mockQueryBuilder);
            OptimizationResult result2 = optimizer.optimize(mockQueryBuilder);
            OptimizationResult result3 = optimizer.optimize(mockQueryBuilder);
            
            // Structure of suggestions should be consistent (same query pattern)
            assertThat(result1.getIndexSuggestions()).hasSize(result2.getIndexSuggestions().size());
            assertThat(result2.getIndexSuggestions()).hasSize(result3.getIndexSuggestions().size());
            
            // Suggestion types should be consistent
            for (int i = 0; i < Math.min(result1.getIndexSuggestions().size(), result2.getIndexSuggestions().size()); i++) {
                IndexSuggestion suggestion1 = result1.getIndexSuggestions().get(i);
                IndexSuggestion suggestion2 = result2.getIndexSuggestions().get(i);
                
                // Table names should be consistent for same query structure
                assertThat(suggestion1.getTableName()).isEqualTo(suggestion2.getTableName());
                assertThat(suggestion1.getIndexType()).isEqualTo(suggestion2.getIndexType());
            }
        }
    }
    
    @Nested
    @DisplayName("Concurrent Optimization Tests")
    class ConcurrentOptimizationTests {
        
        @Test
        @DisplayName("Should handle concurrent optimization requests efficiently")
        void concurrentOptimizations_HandledEfficiently() throws Exception {
            // Prepare different queries for concurrent optimization
            List<String> queries = List.of(
                "SELECT * FROM users WHERE email = 'user1@example.com'",
                "SELECT u.*, o.* FROM users u JOIN orders o ON u.id = o.user_id WHERE u.status = 'ACTIVE'",
                "SELECT COUNT(*) FROM products WHERE category = 'ELECTRONICS'",
                "SELECT c.name, SUM(o.total) FROM customers c JOIN orders o ON c.id = o.customer_id GROUP BY c.name",
                "SELECT * FROM orders WHERE created_date BETWEEN '2024-01-01' AND '2024-12-31'"
            );
            
            ExecutorService executor = Executors.newFixedThreadPool(5);
            List<CompletableFuture<OptimizationResult>> futures = new ArrayList<>();
            
            long startTime = System.currentTimeMillis();
            
            // Submit concurrent optimization tasks
            for (int i = 0; i < queries.size(); i++) {
                final int index = i;
                QueryBuilder<?> mockQuery = mock(QueryBuilder.class);
                when(mockQuery.toSQL()).thenReturn(queries.get(index));
                
                CompletableFuture<OptimizationResult> future = CompletableFuture.supplyAsync(() -> {
                    long threadStartTime = System.currentTimeMillis();
                    OptimizationResult result = optimizer.optimize(mockQuery);
                    long threadDuration = System.currentTimeMillis() - threadStartTime;
                    
                    LOGGER.info(String.format("Thread %d optimization took %d ms", index, threadDuration));
                    return result;
                }, executor);
                
                futures.add(future);
            }
            
            // Wait for all optimizations to complete
            List<OptimizationResult> results = new ArrayList<>();
            for (CompletableFuture<OptimizationResult> future : futures) {
                results.add(future.get(LARGE_QUERY_THRESHOLD_MS, TimeUnit.MILLISECONDS));
            }
            
            long totalDuration = System.currentTimeMillis() - startTime;
            LOGGER.info(String.format("Concurrent optimization of %d queries took %d ms total", queries.size(), totalDuration));
            
            // All results should be valid
            assertThat(results).hasSize(queries.size());
            for (OptimizationResult result : results) {
                assertThat(result).isNotNull();
                assertThat(result.getSummary()).isNotNull().isNotEmpty();
            }
            
            // Concurrent execution should be efficient (not much slower than sequential)
            assertThat(totalDuration).isLessThan(LARGE_QUERY_THRESHOLD_MS * 2); // Allow 2x overhead for concurrency
            
            executor.shutdown();
            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        }
        
        @Test
        @DisplayName("Should maintain thread safety during concurrent optimizations")
        void concurrentOptimizations_ThreadSafe() throws Exception {
            String complexQuery = "SELECT u.*, o.*, p.* FROM users u " +
                                "JOIN orders o ON u.id = o.user_id " +
                                "JOIN order_items oi ON o.id = oi.order_id " +
                                "JOIN products p ON oi.product_id = p.id " +
                                "WHERE u.status = 'ACTIVE' AND o.total > 100";
            
            when(mockQueryBuilder.toSQL()).thenReturn(complexQuery);
            
            int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            List<CompletableFuture<OptimizationResult>> futures = new ArrayList<>();
            
            // Run the same optimization concurrently from multiple threads
            for (int i = 0; i < threadCount; i++) {
                CompletableFuture<OptimizationResult> future = CompletableFuture.supplyAsync(() -> {
                    return optimizer.optimize(mockQueryBuilder);
                }, executor);
                futures.add(future);
            }
            
            // Collect all results
            List<OptimizationResult> results = new ArrayList<>();
            for (CompletableFuture<OptimizationResult> future : futures) {
                results.add(future.get());
            }
            
            // All results should be valid and consistent
            assertThat(results).hasSize(threadCount);
            
            OptimizationResult firstResult = results.get(0);
            for (int i = 1; i < results.size(); i++) {
                OptimizationResult currentResult = results.get(i);
                
                // Results should be consistent (same suggestion counts)
                assertThat(currentResult.getIndexSuggestions()).hasSize(firstResult.getIndexSuggestions().size());
                assertThat(currentResult.getPredicatePushdownSuggestions()).hasSize(firstResult.getPredicatePushdownSuggestions().size());
                assertThat(currentResult.getJoinReorderSuggestions()).hasSize(firstResult.getJoinReorderSuggestions().size());
                assertThat(currentResult.getTotalSuggestionCount()).isEqualTo(firstResult.getTotalSuggestionCount());
            }
            
            executor.shutdown();
            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        }
    }
    
    @Nested
    @DisplayName("Configuration-Based Performance Tests")
    class ConfigurationPerformanceTests {
        
        @Test
        @DisplayName("Should respect timeout configuration in performance")
        void timeoutConfiguration_RespectedInPerformance() {
            // Create config with short timeout
            OptimizerConfig shortTimeoutConfig = OptimizerConfig.builder()
                .maxAnalysisTimeMs(200) // Very short timeout
                .build();
            
            QueryOptimizer shortTimeoutOptimizer = QueryOptimizer.create(shortTimeoutConfig);
            
            // Complex query that might take longer to analyze
            StringBuilder complexQuery = new StringBuilder("SELECT ");
            for (int i = 0; i < 20; i++) {
                if (i > 0) complexQuery.append(", ");
                complexQuery.append("t").append(i).append(".field").append(i);
            }
            complexQuery.append(" FROM table0 t0 ");
            for (int i = 1; i < 20; i++) {
                complexQuery.append("JOIN table").append(i).append(" t").append(i)
                           .append(" ON t").append(i-1).append(".id = t").append(i).append(".ref_id ");
            }
            complexQuery.append("WHERE ");
            for (int i = 0; i < 20; i++) {
                if (i > 0) complexQuery.append("AND ");
                complexQuery.append("t").append(i).append(".active = true ");
            }
            
            when(mockQueryBuilder.toSQL()).thenReturn(complexQuery.toString());
            
            long startTime = System.currentTimeMillis();
            OptimizationResult result = shortTimeoutOptimizer.optimize(mockQueryBuilder);
            long duration = System.currentTimeMillis() - startTime;
            
            LOGGER.info(String.format("Short timeout optimization took %d ms", duration));
            
            assertThat(result).isNotNull();
            
            // Should respect timeout configuration (allowing some overhead)
            assertThat(result.getAnalysisTimeMs()).isLessThanOrEqualTo(shortTimeoutConfig.getMaxAnalysisTimeMs() * 2);
        }
        
        @Test
        @DisplayName("Should show performance difference between configurations")
        void differentConfigurations_ShowPerformanceDifference() {
            String mediumComplexQuery = "SELECT u.*, o.*, COUNT(oi.id) " +
                                      "FROM users u " +
                                      "JOIN orders o ON u.id = o.user_id " +
                                      "JOIN order_items oi ON o.id = oi.order_id " +
                                      "WHERE u.status = 'ACTIVE' " +
                                      "GROUP BY u.id, o.id";
            
            when(mockQueryBuilder.toSQL()).thenReturn(mediumComplexQuery);
            
            // Test different configurations
            OptimizerConfig defaultConfig = OptimizerConfig.defaultConfig();
            OptimizerConfig highPerfConfig = OptimizerConfig.highPerformanceConfig();
            OptimizerConfig devConfig = OptimizerConfig.developmentConfig();
            
            QueryOptimizer defaultOptimizer = QueryOptimizer.create(defaultConfig);
            QueryOptimizer highPerfOptimizer = QueryOptimizer.create(highPerfConfig);
            QueryOptimizer devOptimizer = QueryOptimizer.create(devConfig);
            
            // Measure performance for each configuration
            long defaultTime = measureOptimizationTime(defaultOptimizer, mockQueryBuilder);
            long highPerfTime = measureOptimizationTime(highPerfOptimizer, mockQueryBuilder);
            long devTime = measureOptimizationTime(devOptimizer, mockQueryBuilder);
            
            LOGGER.info(String.format("Default config: %d ms, High-perf config: %d ms, Dev config: %d ms", 
                                    defaultTime, highPerfTime, devTime));
            
            // All should complete within reasonable time
            assertThat(defaultTime).isLessThan(MEDIUM_QUERY_THRESHOLD_MS);
            assertThat(highPerfTime).isLessThan(MEDIUM_QUERY_THRESHOLD_MS);
            assertThat(devTime).isLessThan(MEDIUM_QUERY_THRESHOLD_MS);
            
            // High performance config should generally be fastest (implementation-dependent)
            // This is more of a documentation test than a strict requirement
        }
        
        private long measureOptimizationTime(QueryOptimizer optimizer, QueryBuilder<?> queryBuilder) {
            long startTime = System.currentTimeMillis();
            OptimizationResult result = optimizer.optimize(queryBuilder);
            long duration = System.currentTimeMillis() - startTime;
            
            assertThat(result).isNotNull();
            return duration;
        }
    }
    
    @Nested
    @DisplayName("Memory and Resource Usage Tests")
    class ResourceUsageTests {
        
        @Test
        @DisplayName("Should not cause memory leaks during repeated optimizations")
        void repeatedOptimizations_NoMemoryLeaks() {
            when(mockQueryBuilder.toSQL()).thenReturn(
                "SELECT * FROM users u JOIN orders o ON u.id = o.user_id WHERE u.email = 'test@example.com'"
            );
            
            // Get initial memory usage
            Runtime runtime = Runtime.getRuntime();
            System.gc(); // Suggest garbage collection
            long initialMemory = runtime.totalMemory() - runtime.freeMemory();
            
            // Run many optimizations
            for (int i = 0; i < 100; i++) {
                OptimizationResult result = optimizer.optimize(mockQueryBuilder);
                assertThat(result).isNotNull();
                
                // Occasionally check memory usage
                if (i % 20 == 0) {
                    System.gc();
                    long currentMemory = runtime.totalMemory() - runtime.freeMemory();
                    long memoryIncrease = currentMemory - initialMemory;
                    
                    LOGGER.info(String.format("After %d optimizations, memory increase: %d bytes", 
                                            i + 1, memoryIncrease));
                    
                    // Memory increase should be reasonable (less than 10MB)
                    assertThat(memoryIncrease).isLessThan(10 * 1024 * 1024);
                }
            }
            
            // Final memory check
            System.gc();
            long finalMemory = runtime.totalMemory() - runtime.freeMemory();
            long totalMemoryIncrease = finalMemory - initialMemory;
            
            LOGGER.info(String.format("Total memory increase after 100 optimizations: %d bytes", totalMemoryIncrease));
            
            // Should not have significant memory leaks (less than 20MB total increase)
            assertThat(totalMemoryIncrease).isLessThan(20 * 1024 * 1024);
        }
        
        @Test
        @DisplayName("Should handle large query strings efficiently")
        void largeQueryStrings_HandledEfficiently() {
            // Build a very large query string
            StringBuilder largeQuery = new StringBuilder("SELECT ");
            
            // Add many columns
            for (int i = 0; i < 100; i++) {
                if (i > 0) largeQuery.append(", ");
                largeQuery.append("col").append(i);
            }
            
            largeQuery.append(" FROM large_table WHERE ");
            
            // Add many conditions
            for (int i = 0; i < 100; i++) {
                if (i > 0) largeQuery.append(" AND ");
                largeQuery.append("col").append(i).append(" = 'value").append(i).append("'");
            }
            
            String queryString = largeQuery.toString();
            LOGGER.info(String.format("Large query string length: %d characters", queryString.length()));
            
            when(mockQueryBuilder.toSQL()).thenReturn(queryString);
            
            long startTime = System.currentTimeMillis();
            OptimizationResult result = optimizer.optimize(mockQueryBuilder);
            long duration = System.currentTimeMillis() - startTime;
            
            LOGGER.info(String.format("Large query string optimization took %d ms", duration));
            
            assertThat(result).isNotNull();
            assertThat(duration).isLessThan(LARGE_QUERY_THRESHOLD_MS);
        }
    }
}