package com.github.query4j.benchmark;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.openjdk.jmh.infra.Blackhole;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for benchmark methods to ensure they work correctly.
 * These tests verify that the benchmark methods don't throw exceptions
 * and produce valid outputs.
 */
@DisplayName("QueryPerformanceBenchmark Tests")
class QueryPerformanceBenchmarkTest {

    private static final String BLACKHOLE_MAGIC_STRING = "Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.";

    @Test
    @DisplayName("should execute basic query benchmark without errors")
    void shouldExecuteBasicQueryBenchmark() {
        QueryPerformanceBenchmark benchmark = new QueryPerformanceBenchmark();
        benchmark.setup();
        
        Blackhole blackhole = new Blackhole(BLACKHOLE_MAGIC_STRING);
        
        assertDoesNotThrow(() -> benchmark.basicQuery(blackhole));
    }
    
    @Test
    @DisplayName("should execute moderate query benchmark without errors")
    void shouldExecuteModerateQueryBenchmark() {
        QueryPerformanceBenchmark benchmark = new QueryPerformanceBenchmark();
        benchmark.setup();
        
        Blackhole blackhole = new Blackhole(BLACKHOLE_MAGIC_STRING);
        
        assertDoesNotThrow(() -> benchmark.moderateQuery(blackhole));
    }
    
    @Test
    @DisplayName("should execute complex query benchmark without errors")
    void shouldExecuteComplexQueryBenchmark() {
        QueryPerformanceBenchmark benchmark = new QueryPerformanceBenchmark();
        benchmark.setup();
        
        Blackhole blackhole = new Blackhole(BLACKHOLE_MAGIC_STRING);
        
        assertDoesNotThrow(() -> benchmark.complexQuery(blackhole));
    }
    
    @Test
    @DisplayName("should execute all individual component benchmarks")
    void shouldExecuteIndividualBenchmarks() {
        QueryPerformanceBenchmark benchmark = new QueryPerformanceBenchmark();
        benchmark.setup();
        
        Blackhole blackhole = new Blackhole(BLACKHOLE_MAGIC_STRING);
        
        assertDoesNotThrow(() -> benchmark.builderConstructionBasic(blackhole));
        assertDoesNotThrow(() -> benchmark.sqlGenerationBasic(blackhole));
        assertDoesNotThrow(() -> benchmark.parameterExtractionBasic(blackhole));
        assertDoesNotThrow(() -> benchmark.builderConstructionComplex(blackhole));
        assertDoesNotThrow(() -> benchmark.sqlGenerationComplex(blackhole));
    }
}