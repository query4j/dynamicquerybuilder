package com.github.query4j.optimizer;

import com.github.query4j.core.QueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for QueryOptimizerImpl.
 * Tests the main optimizer coordination and configuration handling.
 */
class QueryOptimizerImplTest {
    
    private QueryOptimizerImpl optimizer;
    private OptimizerConfig defaultConfig;
    
    @Mock
    private QueryBuilder<?> mockQueryBuilder;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        defaultConfig = OptimizerConfig.defaultConfig();
        optimizer = new QueryOptimizerImpl(defaultConfig);
    }
    
    @Test
    @DisplayName("Should create optimizer with default configuration")
    void constructor_DefaultConfig_CreatesValidOptimizer() {
        QueryOptimizerImpl defaultOptimizer = new QueryOptimizerImpl();
        
        assertThat(defaultOptimizer).isNotNull();
        assertThat(defaultOptimizer.getConfig()).isNotNull();
        assertThat(defaultOptimizer.getConfig().isIndexSuggestionsEnabled()).isTrue();
        assertThat(defaultOptimizer.getConfig().isPredicatePushdownEnabled()).isTrue();
        assertThat(defaultOptimizer.getConfig().isJoinReorderingEnabled()).isTrue();
    }
    
    @Test
    @DisplayName("Should create optimizer with custom configuration")
    void constructor_CustomConfig_UsesProvidedConfig() {
        OptimizerConfig customConfig = OptimizerConfig.builder()
                .indexSuggestionsEnabled(false)
                .predicatePushdownEnabled(true)
                .joinReorderingEnabled(false)
                .verboseOutput(true)
                .build();
        
        QueryOptimizerImpl customOptimizer = new QueryOptimizerImpl(customConfig);
        
        assertThat(customOptimizer.getConfig()).isEqualTo(customConfig);
        assertThat(customOptimizer.getConfig().isIndexSuggestionsEnabled()).isFalse();
        assertThat(customOptimizer.getConfig().isPredicatePushdownEnabled()).isTrue();
        assertThat(customOptimizer.getConfig().isJoinReorderingEnabled()).isFalse();
        assertThat(customOptimizer.getConfig().isVerboseOutput()).isTrue();
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException when QueryBuilder is null")
    void optimize_NullQueryBuilder_ThrowsException() {
        assertThatThrownBy(() -> optimizer.optimize((QueryBuilder<?>) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("QueryBuilder must not be null");
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException when SQL query is null")
    void optimize_NullSqlQuery_ThrowsException() {
        assertThatThrownBy(() -> optimizer.optimize((String) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("SQL query must not be null or empty");
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException when SQL query is empty")
    void optimize_EmptySqlQuery_ThrowsException() {
        assertThatThrownBy(() -> optimizer.optimize(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("SQL query must not be null or empty");
        
        assertThatThrownBy(() -> optimizer.optimize("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("SQL query must not be null or empty");
    }
    
    @Test
    @DisplayName("Should optimize QueryBuilder and return valid result")
    void optimize_ValidQueryBuilder_ReturnsResult() {
        when(mockQueryBuilder.toSQL()).thenReturn("SELECT * FROM users WHERE email = :p1");
        
        OptimizationResult result = optimizer.optimize(mockQueryBuilder);
        
        assertThat(result).isNotNull();
        assertThat(result.getAnalysisTimeMs()).isGreaterThanOrEqualTo(0);
        assertThat(result.getSummary()).isNotNull();
        assertThat(result.getIndexSuggestions()).isNotNull();
        assertThat(result.getPredicatePushdownSuggestions()).isNotNull();
        assertThat(result.getJoinReorderSuggestions()).isNotNull();
    }
    
    @Test
    @DisplayName("Should handle SQL string optimization")
    void optimize_ValidSqlString_ReturnsResult() {
        String sqlQuery = "SELECT * FROM users WHERE email = 'test@example.com'";
        
        OptimizationResult result = optimizer.optimize(sqlQuery);
        
        assertThat(result).isNotNull();
        assertThat(result.getAnalysisTimeMs()).isGreaterThanOrEqualTo(0);
        assertThat(result.getSummary()).contains("SQL string analysis not yet fully implemented");
        assertThat(result.getIndexSuggestions()).isEmpty();
        assertThat(result.getPredicatePushdownSuggestions()).isEmpty();
        assertThat(result.getJoinReorderSuggestions()).isEmpty();
    }
    
    @Test
    @DisplayName("Should respect configuration to disable index suggestions")
    void optimize_IndexSuggestionsDisabled_SkipsIndexAnalysis() {
        OptimizerConfig configWithoutIndexes = OptimizerConfig.builder()
                .indexSuggestionsEnabled(false)
                .predicatePushdownEnabled(true)
                .joinReorderingEnabled(true)
                .build();
        
        QueryOptimizerImpl optimizerWithoutIndexes = new QueryOptimizerImpl(configWithoutIndexes);
        when(mockQueryBuilder.toSQL()).thenReturn("SELECT * FROM users");
        
        OptimizationResult result = optimizerWithoutIndexes.optimize(mockQueryBuilder);
        
        assertThat(result).isNotNull();
        // Index suggestions should be empty due to configuration
        assertThat(result.getIndexSuggestions()).isEmpty();
    }
    
    @Test
    @DisplayName("Should respect configuration to disable predicate pushdown")
    void optimize_PredicatePushdownDisabled_SkipsPredicateAnalysis() {
        OptimizerConfig configWithoutPushdown = OptimizerConfig.builder()
                .indexSuggestionsEnabled(true)
                .predicatePushdownEnabled(false)
                .joinReorderingEnabled(true)
                .build();
        
        QueryOptimizerImpl optimizerWithoutPushdown = new QueryOptimizerImpl(configWithoutPushdown);
        when(mockQueryBuilder.toSQL()).thenReturn("SELECT * FROM users");
        
        OptimizationResult result = optimizerWithoutPushdown.optimize(mockQueryBuilder);
        
        assertThat(result).isNotNull();
        // Predicate suggestions should be empty due to configuration
        assertThat(result.getPredicatePushdownSuggestions()).isEmpty();
    }
    
    @Test
    @DisplayName("Should respect configuration to disable join reordering")
    void optimize_JoinReorderingDisabled_SkipsJoinAnalysis() {
        OptimizerConfig configWithoutJoins = OptimizerConfig.builder()
                .indexSuggestionsEnabled(true)
                .predicatePushdownEnabled(true)
                .joinReorderingEnabled(false)
                .build();
        
        QueryOptimizerImpl optimizerWithoutJoins = new QueryOptimizerImpl(configWithoutJoins);
        when(mockQueryBuilder.toSQL()).thenReturn("SELECT * FROM users");
        
        OptimizationResult result = optimizerWithoutJoins.optimize(mockQueryBuilder);
        
        assertThat(result).isNotNull();
        // Join suggestions should be empty due to configuration
        assertThat(result.getJoinReorderSuggestions()).isEmpty();
    }
    
    @Test
    @DisplayName("Should create new optimizer with updated configuration")
    void withConfig_NewConfig_ReturnsNewOptimizer() {
        OptimizerConfig newConfig = OptimizerConfig.builder()
                .verboseOutput(true)
                .maxAnalysisTimeMs(10000)
                .build();
        
        QueryOptimizer newOptimizer = optimizer.withConfig(newConfig);
        
        assertThat(newOptimizer).isNotSameAs(optimizer); // Different instances
        assertThat(newOptimizer.getConfig()).isEqualTo(newConfig);
        assertThat(optimizer.getConfig()).isEqualTo(defaultConfig); // Original unchanged
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException when new config is null")
    void withConfig_NullConfig_ThrowsException() {
        assertThatThrownBy(() -> optimizer.withConfig(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Config must not be null");
    }
    
    @Test
    @DisplayName("Should handle optimization timeout configuration")
    void optimize_TimeoutConfiguration_HandlesTimeout() {
        OptimizerConfig timeoutConfig = OptimizerConfig.builder()
                .maxAnalysisTimeMs(1) // Very short timeout
                .build();
        
        QueryOptimizerImpl timeoutOptimizer = new QueryOptimizerImpl(timeoutConfig);
        when(mockQueryBuilder.toSQL()).thenReturn("SELECT * FROM users");
        
        // The optimizer might complete normally or throw OptimizationException for timeout
        // Both outcomes are acceptable for this configuration
        try {
            OptimizationResult result = timeoutOptimizer.optimize(mockQueryBuilder);
            assertThat(result).isNotNull();
        } catch (OptimizationException e) {
            // This is also acceptable - the timeout is working
            assertThat(e.getMessage()).contains("timeout");
        }
    }
    
    @Test
    @DisplayName("Static factory method should create optimizer with default config")
    void create_DefaultFactory_ReturnsOptimizerWithDefaultConfig() {
        QueryOptimizer factoryOptimizer = QueryOptimizer.create();
        
        assertThat(factoryOptimizer).isInstanceOf(QueryOptimizerImpl.class);
        assertThat(factoryOptimizer.getConfig()).isEqualTo(OptimizerConfig.defaultConfig());
    }
    
    @Test
    @DisplayName("Static factory method should create optimizer with custom config")
    void create_CustomConfigFactory_ReturnsOptimizerWithCustomConfig() {
        OptimizerConfig customConfig = OptimizerConfig.highPerformanceConfig();
        
        QueryOptimizer factoryOptimizer = QueryOptimizer.create(customConfig);
        
        assertThat(factoryOptimizer).isInstanceOf(QueryOptimizerImpl.class);
        assertThat(factoryOptimizer.getConfig()).isEqualTo(customConfig);
    }
}