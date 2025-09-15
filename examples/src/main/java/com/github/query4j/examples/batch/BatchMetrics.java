package com.github.query4j.examples.batch;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Comprehensive metrics collection and monitoring for batch processing operations.
 * Provides real-time statistics, performance monitoring, and health indicators.
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
public class BatchMetrics {
    
    private static final Logger logger = Logger.getLogger(BatchMetrics.class.getName());
    
    // Processing metrics
    private final AtomicLong processedRecords = new AtomicLong(0);
    private final AtomicLong failedRecords = new AtomicLong(0);
    private final AtomicLong retriedRecords = new AtomicLong(0);
    private final AtomicLong skippedRecords = new AtomicLong(0);
    
    // Timing metrics
    private final AtomicReference<Instant> processingStartTime = new AtomicReference<>();
    private final AtomicReference<Instant> lastProcessedTime = new AtomicReference<>();
    private final AtomicLong totalProcessingTimeMs = new AtomicLong(0);
    private final AtomicLong minProcessingTimeMs = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong maxProcessingTimeMs = new AtomicLong(0);
    
    // Throughput metrics
    private final AtomicLong currentThroughputPerSecond = new AtomicLong(0);
    private final AtomicLong peakThroughputPerSecond = new AtomicLong(0);
    private final AtomicLong averageThroughputPerSecond = new AtomicLong(0);
    
    // Database metrics
    private final AtomicLong queryExecutionCount = new AtomicLong(0);
    private final AtomicLong queryExecutionTimeMs = new AtomicLong(0);
    private final AtomicLong connectionAcquisitionTimeMs = new AtomicLong(0);
    private final AtomicLong connectionFailures = new AtomicLong(0);
    
    // Circuit breaker metrics
    private final AtomicLong circuitBreakerTrips = new AtomicLong(0);
    private final AtomicReference<Instant> lastCircuitBreakerTrip = new AtomicReference<>();
    
    // Memory and resource metrics
    private final AtomicLong memoryUsageMb = new AtomicLong(0);
    private final AtomicLong gcCount = new AtomicLong(0);
    private final AtomicLong gcTimeMs = new AtomicLong(0);
    
    // Custom business metrics
    private final Map<String, AtomicLong> customCounters = new ConcurrentHashMap<>();
    private final Map<String, AtomicReference<String>> customMetadata = new ConcurrentHashMap<>();
    
    // Health status
    public enum HealthStatus {
        HEALTHY, DEGRADED, UNHEALTHY, UNKNOWN
    }
    private final AtomicReference<HealthStatus> healthStatus = new AtomicReference<>(HealthStatus.UNKNOWN);
    private final AtomicReference<String> healthMessage = new AtomicReference<>("");
    
    /**
     * Records the start of batch processing
     */
    public void startProcessing() {
        processingStartTime.set(Instant.now());
        healthStatus.set(HealthStatus.HEALTHY);
        healthMessage.set("Processing started");
        logger.info("Batch processing started at " + processingStartTime.get());
    }
    
    /**
     * Records successful processing of records
     */
    public void recordProcessedRecords(long count, Duration processingTime) {
        processedRecords.addAndGet(count);
        lastProcessedTime.set(Instant.now());
        
        long processingMs = processingTime.toMillis();
        totalProcessingTimeMs.addAndGet(processingMs);
        updateMinMaxProcessingTime(processingMs);
        updateThroughput(count, processingTime);
    }
    
    /**
     * Records failed records
     */
    public void recordFailedRecords(long count, String reason) {
        failedRecords.addAndGet(count);
        updateHealthStatus();
        logger.warning("Recorded " + count + " failed records. Reason: " + reason);
    }
    
    /**
     * Records retried records
     */
    public void recordRetriedRecords(long count) {
        retriedRecords.addAndGet(count);
        updateHealthStatus();
    }
    
    /**
     * Records skipped records
     */
    public void recordSkippedRecords(long count, String reason) {
        skippedRecords.addAndGet(count);
        customMetadata.computeIfAbsent("lastSkipReason", k -> new AtomicReference<>()).set(reason);
    }
    
    /**
     * Records query execution metrics
     */
    public void recordQueryExecution(Duration executionTime) {
        queryExecutionCount.incrementAndGet();
        queryExecutionTimeMs.addAndGet(executionTime.toMillis());
    }
    
    /**
     * Records connection metrics
     */
    public void recordConnectionAcquisition(Duration acquisitionTime) {
        connectionAcquisitionTimeMs.addAndGet(acquisitionTime.toMillis());
    }
    
    /**
     * Records connection failure
     */
    public void recordConnectionFailure() {
        connectionFailures.incrementAndGet();
        updateHealthStatus();
    }
    
    /**
     * Records circuit breaker trip
     */
    public void recordCircuitBreakerTrip() {
        circuitBreakerTrips.incrementAndGet();
        lastCircuitBreakerTrip.set(Instant.now());
        healthStatus.set(HealthStatus.DEGRADED);
        healthMessage.set("Circuit breaker tripped");
        logger.warning("Circuit breaker tripped at " + lastCircuitBreakerTrip.get());
    }
    
    /**
     * Updates memory usage metrics
     */
    public void updateMemoryMetrics() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        memoryUsageMb.set(usedMemory);
    }
    
    /**
     * Records custom counter
     */
    public void incrementCustomCounter(String name, long value) {
        customCounters.computeIfAbsent(name, k -> new AtomicLong(0)).addAndGet(value);
    }
    
    /**
     * Sets custom metadata
     */
    public void setCustomMetadata(String key, String value) {
        customMetadata.computeIfAbsent(key, k -> new AtomicReference<>()).set(value);
    }
    
    /**
     * Calculates and returns comprehensive metrics snapshot
     */
    public MetricsSnapshot getSnapshot() {
        updateMemoryMetrics();
        return new MetricsSnapshot(
                processedRecords.get(),
                failedRecords.get(),
                retriedRecords.get(),
                skippedRecords.get(),
                getTotalDuration(),
                getAverageProcessingTimeMs(),
                currentThroughputPerSecond.get(),
                peakThroughputPerSecond.get(),
                averageThroughputPerSecond.get(),
                queryExecutionCount.get(),
                getAverageQueryExecutionTimeMs(),
                connectionFailures.get(),
                circuitBreakerTrips.get(),
                memoryUsageMb.get(),
                healthStatus.get(),
                healthMessage.get(),
                Map.copyOf(customCounters),
                getCustomMetadataSnapshot()
        );
    }
    
    private void updateMinMaxProcessingTime(long processingMs) {
        minProcessingTimeMs.updateAndGet(current -> Math.min(current, processingMs));
        maxProcessingTimeMs.updateAndGet(current -> Math.max(current, processingMs));
    }
    
    private void updateThroughput(long recordCount, Duration processingTime) {
        if (processingTime.toMillis() > 0) {
            long throughput = recordCount * 1000 / processingTime.toMillis();
            currentThroughputPerSecond.set(throughput);
            peakThroughputPerSecond.updateAndGet(current -> Math.max(current, throughput));
            
            // Update average throughput
            Instant start = processingStartTime.get();
            if (start != null) {
                Duration totalDuration = Duration.between(start, Instant.now());
                if (totalDuration.toMillis() > 0) {
                    long avgThroughput = processedRecords.get() * 1000 / totalDuration.toMillis();
                    averageThroughputPerSecond.set(avgThroughput);
                }
            }
        }
    }
    
    private void updateHealthStatus() {
        long total = processedRecords.get() + failedRecords.get();
        if (total == 0) {
            return;
        }
        
        double errorRate = (double) failedRecords.get() / total;
        long connectionFailureCount = connectionFailures.get();
        
        if (errorRate > 0.1 || connectionFailureCount > 10) {
            healthStatus.set(HealthStatus.UNHEALTHY);
            healthMessage.set("High error rate: " + String.format("%.2f%%", errorRate * 100));
        } else if (errorRate > 0.05 || connectionFailureCount > 5) {
            healthStatus.set(HealthStatus.DEGRADED);
            healthMessage.set("Elevated error rate: " + String.format("%.2f%%", errorRate * 100));
        } else {
            healthStatus.set(HealthStatus.HEALTHY);
            healthMessage.set("Processing normally");
        }
    }
    
    private Duration getTotalDuration() {
        Instant start = processingStartTime.get();
        return start != null ? Duration.between(start, Instant.now()) : Duration.ZERO;
    }
    
    private double getAverageProcessingTimeMs() {
        long totalQueries = queryExecutionCount.get();
        return totalQueries > 0 ? (double) totalProcessingTimeMs.get() / totalQueries : 0.0;
    }
    
    private double getAverageQueryExecutionTimeMs() {
        long totalQueries = queryExecutionCount.get();
        return totalQueries > 0 ? (double) queryExecutionTimeMs.get() / totalQueries : 0.0;
    }
    
    private Map<String, String> getCustomMetadataSnapshot() {
        Map<String, String> snapshot = new ConcurrentHashMap<>();
        customMetadata.forEach((key, value) -> snapshot.put(key, value.get()));
        return snapshot;
    }
    
    /**
     * Logs comprehensive metrics summary
     */
    public void logSummary() {
        MetricsSnapshot snapshot = getSnapshot();
        logger.info("=== Batch Processing Metrics Summary ===");
        logger.info("Processed: " + snapshot.processedRecords + " records");
        logger.info("Failed: " + snapshot.failedRecords + " records");
        logger.info("Retried: " + snapshot.retriedRecords + " records");
        logger.info("Total Duration: " + formatDuration(snapshot.totalDuration));
        logger.info("Average Throughput: " + snapshot.averageThroughputPerSecond + " records/sec");
        logger.info("Peak Throughput: " + snapshot.peakThroughputPerSecond + " records/sec");
        logger.info("Memory Usage: " + snapshot.memoryUsageMb + " MB");
        logger.info("Health Status: " + snapshot.healthStatus + " - " + snapshot.healthMessage);
        logger.info("========================================");
    }
    
    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
    
    /**
     * Immutable snapshot of metrics at a point in time
     */
    public static class MetricsSnapshot {
        public final long processedRecords;
        public final long failedRecords;
        public final long retriedRecords;
        public final long skippedRecords;
        public final Duration totalDuration;
        public final double averageProcessingTimeMs;
        public final long currentThroughputPerSecond;
        public final long peakThroughputPerSecond;
        public final long averageThroughputPerSecond;
        public final long queryExecutionCount;
        public final double averageQueryExecutionTimeMs;
        public final long connectionFailures;
        public final long circuitBreakerTrips;
        public final long memoryUsageMb;
        public final HealthStatus healthStatus;
        public final String healthMessage;
        public final Map<String, AtomicLong> customCounters;
        public final Map<String, String> customMetadata;
        
        public MetricsSnapshot(long processedRecords, long failedRecords, long retriedRecords, 
                             long skippedRecords, Duration totalDuration, double averageProcessingTimeMs,
                             long currentThroughputPerSecond, long peakThroughputPerSecond, 
                             long averageThroughputPerSecond, long queryExecutionCount, 
                             double averageQueryExecutionTimeMs, long connectionFailures, 
                             long circuitBreakerTrips, long memoryUsageMb, HealthStatus healthStatus,
                             String healthMessage, Map<String, AtomicLong> customCounters,
                             Map<String, String> customMetadata) {
            this.processedRecords = processedRecords;
            this.failedRecords = failedRecords;
            this.retriedRecords = retriedRecords;
            this.skippedRecords = skippedRecords;
            this.totalDuration = totalDuration;
            this.averageProcessingTimeMs = averageProcessingTimeMs;
            this.currentThroughputPerSecond = currentThroughputPerSecond;
            this.peakThroughputPerSecond = peakThroughputPerSecond;
            this.averageThroughputPerSecond = averageThroughputPerSecond;
            this.queryExecutionCount = queryExecutionCount;
            this.averageQueryExecutionTimeMs = averageQueryExecutionTimeMs;
            this.connectionFailures = connectionFailures;
            this.circuitBreakerTrips = circuitBreakerTrips;
            this.memoryUsageMb = memoryUsageMb;
            this.healthStatus = healthStatus;
            this.healthMessage = healthMessage;
            this.customCounters = customCounters;
            this.customMetadata = customMetadata;
        }
        
        public boolean isHealthy() {
            return healthStatus == HealthStatus.HEALTHY;
        }
        
        public double getErrorRate() {
            long total = processedRecords + failedRecords;
            return total > 0 ? (double) failedRecords / total : 0.0;
        }
        
        public double getRetryRate() {
            long total = processedRecords + failedRecords + retriedRecords;
            return total > 0 ? (double) retriedRecords / total : 0.0;
        }
    }
}