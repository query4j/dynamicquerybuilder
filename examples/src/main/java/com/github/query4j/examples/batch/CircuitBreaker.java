package com.github.query4j.examples.batch;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.function.Supplier;

/**
 * Circuit breaker implementation for batch processing operations.
 * Provides resilience against cascading failures and automatic recovery.
 * 
 * States:
 * - CLOSED: Normal operation, all calls pass through
 * - OPEN: Circuit is open, all calls fail fast 
 * - HALF_OPEN: Testing if service has recovered
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
public class CircuitBreaker {
    
    private static final Logger logger = Logger.getLogger(CircuitBreaker.class.getName());
    
    public enum State {
        CLOSED,    // Normal operation
        OPEN,      // Failing fast
        HALF_OPEN  // Testing recovery
    }
    
    private final int failureThreshold;
    private final int minimumThroughput;
    private final Duration timeout;
    private final String name;
    
    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger requestCount = new AtomicInteger(0);
    private final AtomicReference<Instant> lastFailureTime = new AtomicReference<>();
    private final AtomicReference<Instant> stateChangeTime = new AtomicReference<>(Instant.now());
    
    public CircuitBreaker(String name, int failureThreshold, int minimumThroughput, Duration timeout) {
        this.name = name;
        this.failureThreshold = failureThreshold;
        this.minimumThroughput = minimumThroughput;
        this.timeout = timeout;
    }
    
    /**
     * Executes the given supplier with circuit breaker protection
     */
    public <T> T execute(Supplier<T> supplier) throws CircuitBreakerOpenException {
        if (!allowRequest()) {
            throw new CircuitBreakerOpenException("Circuit breaker [" + name + "] is OPEN");
        }
        
        try {
            T result = supplier.get();
            recordSuccess();
            return result;
        } catch (Exception e) {
            recordFailure();
            throw e;
        }
    }
    
    /**
     * Executes the given runnable with circuit breaker protection
     */
    public void execute(Runnable runnable) throws CircuitBreakerOpenException {
        execute(() -> {
            runnable.run();
            return null;
        });
    }
    
    /**
     * Checks if the request should be allowed based on current state
     */
    private boolean allowRequest() {
        State currentState = state.get();
        
        switch (currentState) {
            case CLOSED:
                return true;
                
            case OPEN:
                if (shouldAttemptReset()) {
                    state.set(State.HALF_OPEN);
                    logger.info("Circuit breaker [" + name + "] moving to HALF_OPEN state");
                    return true;
                }
                return false;
                
            case HALF_OPEN:
                return true;
                
            default:
                return false;
        }
    }
    
    /**
     * Records successful execution
     */
    private void recordSuccess() {
        requestCount.incrementAndGet();
        successCount.incrementAndGet();
        
        State currentState = state.get();
        if (currentState == State.HALF_OPEN) {
            // If we're in HALF_OPEN and got a success, close the circuit
            state.set(State.CLOSED);
            reset();
            logger.info("Circuit breaker [" + name + "] closed after successful test");
        }
    }
    
    /**
     * Records failed execution
     */
    private void recordFailure() {
        requestCount.incrementAndGet();
        int failures = failureCount.incrementAndGet();
        lastFailureTime.set(Instant.now());
        
        State currentState = state.get();
        
        if (currentState == State.HALF_OPEN) {
            // If we're testing and got a failure, go back to OPEN
            state.set(State.OPEN);
            stateChangeTime.set(Instant.now());
            logger.warning("Circuit breaker [" + name + "] opened after failed test");
        } else if (currentState == State.CLOSED) {
            // Check if we should open the circuit
            int requests = requestCount.get();
            if (requests >= minimumThroughput && failures >= failureThreshold) {
                state.set(State.OPEN);
                stateChangeTime.set(Instant.now());
                logger.warning("Circuit breaker [" + name + "] opened due to " + failures + " failures out of " + requests + " requests");
            }
        }
    }
    
    /**
     * Checks if circuit breaker should attempt to reset from OPEN to HALF_OPEN
     */
    private boolean shouldAttemptReset() {
        Instant stateChange = stateChangeTime.get();
        return stateChange != null && Duration.between(stateChange, Instant.now()).compareTo(timeout) >= 0;
    }
    
    /**
     * Resets circuit breaker counters
     */
    private void reset() {
        failureCount.set(0);
        successCount.set(0);
        requestCount.set(0);
        stateChangeTime.set(Instant.now());
    }
    
    /**
     * Forces circuit breaker to OPEN state
     */
    public void forceOpen() {
        state.set(State.OPEN);
        stateChangeTime.set(Instant.now());
        logger.warning("Circuit breaker [" + name + "] forced to OPEN state");
    }
    
    /**
     * Forces circuit breaker to CLOSED state and resets counters
     */
    public void forceClose() {
        state.set(State.CLOSED);
        reset();
        logger.info("Circuit breaker [" + name + "] forced to CLOSED state");
    }
    
    /**
     * Gets current circuit breaker metrics
     */
    public CircuitBreakerMetrics getMetrics() {
        return new CircuitBreakerMetrics(
                name,
                state.get(),
                failureCount.get(),
                successCount.get(),
                requestCount.get(),
                lastFailureTime.get(),
                stateChangeTime.get(),
                failureThreshold,
                minimumThroughput,
                timeout
        );
    }
    
    /**
     * Checks if circuit breaker is currently allowing requests
     */
    public boolean isCallPermitted() {
        return allowRequest();
    }
    
    /**
     * Gets current state of the circuit breaker
     */
    public State getState() {
        return state.get();
    }
    
    /**
     * Gets current failure count
     */
    public int getFailureCount() {
        return failureCount.get();
    }
    
    /**
     * Gets current success count
     */
    public int getSuccessCount() {
        return successCount.get();
    }
    
    /**
     * Gets failure rate (failures / total requests)
     */
    public double getFailureRate() {
        int requests = requestCount.get();
        return requests > 0 ? (double) failureCount.get() / requests : 0.0;
    }
    
    /**
     * Exception thrown when circuit breaker is open
     */
    public static class CircuitBreakerOpenException extends RuntimeException {
        public CircuitBreakerOpenException(String message) {
            super(message);
        }
    }
    
    /**
     * Immutable snapshot of circuit breaker metrics
     */
    public static class CircuitBreakerMetrics {
        private final String name;
        private final State state;
        private final int failureCount;
        private final int successCount;
        private final int requestCount;
        private final Instant lastFailureTime;
        private final Instant stateChangeTime;
        private final int failureThreshold;
        private final int minimumThroughput;
        private final Duration timeout;
        
        public CircuitBreakerMetrics(String name, State state, int failureCount, int successCount,
                                   int requestCount, Instant lastFailureTime, Instant stateChangeTime,
                                   int failureThreshold, int minimumThroughput, Duration timeout) {
            this.name = name;
            this.state = state;
            this.failureCount = failureCount;
            this.successCount = successCount;
            this.requestCount = requestCount;
            this.lastFailureTime = lastFailureTime;
            this.stateChangeTime = stateChangeTime;
            this.failureThreshold = failureThreshold;
            this.minimumThroughput = minimumThroughput;
            this.timeout = timeout;
        }
        
        // Getters
        public String getName() { return name; }
        public State getState() { return state; }
        public int getFailureCount() { return failureCount; }
        public int getSuccessCount() { return successCount; }
        public int getRequestCount() { return requestCount; }
        public Instant getLastFailureTime() { return lastFailureTime; }
        public Instant getStateChangeTime() { return stateChangeTime; }
        public int getFailureThreshold() { return failureThreshold; }
        public int getMinimumThroughput() { return minimumThroughput; }
        public Duration getTimeout() { return timeout; }
        
        public double getFailureRate() {
            return requestCount > 0 ? (double) failureCount / requestCount : 0.0;
        }
        
        public double getSuccessRate() {
            return requestCount > 0 ? (double) successCount / requestCount : 0.0;
        }
        
        @Override
        public String toString() {
            return "CircuitBreakerMetrics{" +
                    "name='" + name + '\'' +
                    ", state=" + state +
                    ", failureCount=" + failureCount +
                    ", successCount=" + successCount +
                    ", requestCount=" + requestCount +
                    ", failureRate=" + String.format("%.2f%%", getFailureRate() * 100) +
                    ", lastFailureTime=" + lastFailureTime +
                    ", stateChangeTime=" + stateChangeTime +
                    '}';
        }
    }
}