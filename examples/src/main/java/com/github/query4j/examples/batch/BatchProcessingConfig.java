package com.github.query4j.examples.batch;

import java.time.Duration;
import java.util.Properties;

/**
 * Configuration class for batch processing operations.
 * Supports environment-specific configurations and runtime tuning.
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
public class BatchProcessingConfig {
    
    // Batch Processing Configuration
    private int batchSize = 1000;
    private int maxRetries = 3;
    private Duration retryDelay = Duration.ofSeconds(1);
    private Duration queryTimeout = Duration.ofSeconds(30);
    
    // Performance Configuration
    private int threadPoolSize = 4;
    private int connectionPoolSize = 10;
    private Duration connectionTimeout = Duration.ofSeconds(5);
    
    // Circuit Breaker Configuration
    private int circuitBreakerFailureThreshold = 5;
    private Duration circuitBreakerTimeout = Duration.ofMinutes(1);
    private int circuitBreakerMinimumThroughput = 10;
    
    // Monitoring Configuration
    private boolean metricsEnabled = true;
    private Duration metricsReportingInterval = Duration.ofMinutes(1);
    private String metricsPrefix = "query4j.batch";
    
    // Logging Configuration
    private String logLevel = "INFO";
    private boolean structuredLogging = true;
    
    public BatchProcessingConfig() {
        loadFromEnvironment();
    }
    
    public BatchProcessingConfig(Properties properties) {
        loadFromProperties(properties);
    }
    
    /**
     * Load configuration from environment variables and system properties
     */
    private void loadFromEnvironment() {
        batchSize = getIntEnv("BATCH_SIZE", batchSize);
        maxRetries = getIntEnv("MAX_RETRIES", maxRetries);
        retryDelay = Duration.ofMillis(getIntEnv("RETRY_DELAY_MS", (int) retryDelay.toMillis()));
        queryTimeout = Duration.ofSeconds(getIntEnv("QUERY_TIMEOUT_SECONDS", (int) queryTimeout.getSeconds()));
        
        threadPoolSize = getIntEnv("THREAD_POOL_SIZE", threadPoolSize);
        connectionPoolSize = getIntEnv("CONNECTION_POOL_SIZE", connectionPoolSize);
        connectionTimeout = Duration.ofSeconds(getIntEnv("CONNECTION_TIMEOUT_SECONDS", (int) connectionTimeout.getSeconds()));
        
        circuitBreakerFailureThreshold = getIntEnv("CIRCUIT_BREAKER_FAILURE_THRESHOLD", circuitBreakerFailureThreshold);
        circuitBreakerTimeout = Duration.ofMinutes(getIntEnv("CIRCUIT_BREAKER_TIMEOUT_MINUTES", (int) circuitBreakerTimeout.toMinutes()));
        
        metricsEnabled = getBooleanEnv("METRICS_ENABLED", metricsEnabled);
        metricsReportingInterval = Duration.ofMinutes(getIntEnv("METRICS_REPORTING_INTERVAL_MINUTES", (int) metricsReportingInterval.toMinutes()));
        
        logLevel = getStringEnv("LOG_LEVEL", logLevel);
        structuredLogging = getBooleanEnv("STRUCTURED_LOGGING", structuredLogging);
    }
    
    private void loadFromProperties(Properties properties) {
        try {
            batchSize = Integer.parseInt(properties.getProperty("batch.size", String.valueOf(batchSize)));
            maxRetries = Integer.parseInt(properties.getProperty("batch.retries.max", String.valueOf(maxRetries)));
            retryDelay = Duration.ofMillis(Integer.parseInt(properties.getProperty("batch.retries.delay.ms", String.valueOf(retryDelay.toMillis()))));
            
            threadPoolSize = Integer.parseInt(properties.getProperty("thread.pool.size", String.valueOf(threadPoolSize)));
            connectionPoolSize = Integer.parseInt(properties.getProperty("connection.pool.size", String.valueOf(connectionPoolSize)));
            
            metricsEnabled = Boolean.parseBoolean(properties.getProperty("metrics.enabled", String.valueOf(metricsEnabled)));
            logLevel = properties.getProperty("log.level", logLevel);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid configuration property value", e);
        }
    }
    
    private int getIntEnv(String name, int defaultValue) {
        String value = System.getenv(name);
        if (value == null) {
            value = System.getProperty(name.toLowerCase().replace("_", "."));
        }
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid integer value for " + name + ": " + value, e);
            }
        }
        return defaultValue;
    }
    
    private boolean getBooleanEnv(String name, boolean defaultValue) {
        String value = System.getenv(name);
        if (value == null) {
            value = System.getProperty(name.toLowerCase().replace("_", "."));
        }
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }
    
    private String getStringEnv(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null) {
            value = System.getProperty(name.toLowerCase().replace("_", "."));
        }
        return value != null ? value : defaultValue;
    }
    
    // Getters and setters
    public int getBatchSize() { return batchSize; }
    public BatchProcessingConfig setBatchSize(int batchSize) { this.batchSize = batchSize; return this; }
    
    public int getMaxRetries() { return maxRetries; }
    public BatchProcessingConfig setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; return this; }
    
    public Duration getRetryDelay() { return retryDelay; }
    public BatchProcessingConfig setRetryDelay(Duration retryDelay) { this.retryDelay = retryDelay; return this; }
    
    public Duration getQueryTimeout() { return queryTimeout; }
    public BatchProcessingConfig setQueryTimeout(Duration queryTimeout) { this.queryTimeout = queryTimeout; return this; }
    
    public int getThreadPoolSize() { return threadPoolSize; }
    public BatchProcessingConfig setThreadPoolSize(int threadPoolSize) { this.threadPoolSize = threadPoolSize; return this; }
    
    public int getConnectionPoolSize() { return connectionPoolSize; }
    public BatchProcessingConfig setConnectionPoolSize(int connectionPoolSize) { this.connectionPoolSize = connectionPoolSize; return this; }
    
    public Duration getConnectionTimeout() { return connectionTimeout; }
    public BatchProcessingConfig setConnectionTimeout(Duration connectionTimeout) { this.connectionTimeout = connectionTimeout; return this; }
    
    public int getCircuitBreakerFailureThreshold() { return circuitBreakerFailureThreshold; }
    public BatchProcessingConfig setCircuitBreakerFailureThreshold(int threshold) { this.circuitBreakerFailureThreshold = threshold; return this; }
    
    public Duration getCircuitBreakerTimeout() { return circuitBreakerTimeout; }
    public BatchProcessingConfig setCircuitBreakerTimeout(Duration timeout) { this.circuitBreakerTimeout = timeout; return this; }
    
    public int getCircuitBreakerMinimumThroughput() { return circuitBreakerMinimumThroughput; }
    public BatchProcessingConfig setCircuitBreakerMinimumThroughput(int throughput) { this.circuitBreakerMinimumThroughput = throughput; return this; }
    
    public boolean isMetricsEnabled() { return metricsEnabled; }
    public BatchProcessingConfig setMetricsEnabled(boolean enabled) { this.metricsEnabled = enabled; return this; }
    
    public Duration getMetricsReportingInterval() { return metricsReportingInterval; }
    public BatchProcessingConfig setMetricsReportingInterval(Duration interval) { this.metricsReportingInterval = interval; return this; }
    
    public String getMetricsPrefix() { return metricsPrefix; }
    public BatchProcessingConfig setMetricsPrefix(String prefix) { this.metricsPrefix = prefix; return this; }
    
    public String getLogLevel() { return logLevel; }
    public BatchProcessingConfig setLogLevel(String level) { this.logLevel = level; return this; }
    
    public boolean isStructuredLogging() { return structuredLogging; }
    public BatchProcessingConfig setStructuredLogging(boolean structured) { this.structuredLogging = structured; return this; }
    
    @Override
    public String toString() {
        return "BatchProcessingConfig{" +
                "batchSize=" + batchSize +
                ", maxRetries=" + maxRetries +
                ", retryDelay=" + retryDelay +
                ", queryTimeout=" + queryTimeout +
                ", threadPoolSize=" + threadPoolSize +
                ", connectionPoolSize=" + connectionPoolSize +
                ", connectionTimeout=" + connectionTimeout +
                ", circuitBreakerFailureThreshold=" + circuitBreakerFailureThreshold +
                ", circuitBreakerTimeout=" + circuitBreakerTimeout +
                ", metricsEnabled=" + metricsEnabled +
                ", metricsReportingInterval=" + metricsReportingInterval +
                ", logLevel='" + logLevel + '\'' +
                '}';
    }
}