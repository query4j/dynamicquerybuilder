package com.github.query4j.optimizer.predicate;

import com.github.query4j.core.QueryBuilder;
import com.github.query4j.core.criteria.SimplePredicate;
import com.github.query4j.core.criteria.InPredicate;
import com.github.query4j.core.criteria.BetweenPredicate;
import com.github.query4j.core.criteria.LikePredicate;
import com.github.query4j.core.criteria.NullPredicate;
import com.github.query4j.core.criteria.Predicate;
import com.github.query4j.optimizer.OptimizerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Set;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PredicatePushdownOptimizerImpl.
 * Tests predicate selectivity estimation and reordering logic.
 */
class PredicatePushdownOptimizerImplTest {
    
    private PredicatePushdownOptimizerImpl optimizer;
    private OptimizerConfig config;
    
    @Mock
    private QueryBuilder<?> mockQueryBuilder;
    
    @Mock
    private PredicatePushdownOptimizer.JoinGraph mockJoinGraph;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        config = OptimizerConfig.defaultConfig();
        optimizer = new PredicatePushdownOptimizerImpl(config);
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException when QueryBuilder is null")
    void optimizeQuery_NullQueryBuilder_ThrowsException() {
        assertThatThrownBy(() -> optimizer.optimizeQuery(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("QueryBuilder must not be null");
    }
    
    @Test
    @DisplayName("Should return empty list for empty predicates")
    void suggestPredicateReordering_EmptyPredicates_ReturnsEmptyList() {
        List<PredicatePushdownSuggestion> suggestions = optimizer.suggestPredicateReordering(List.of());
        
        assertThat(suggestions).isEmpty();
    }
    
    @Test
    @DisplayName("Should suggest reordering based on selectivity")
    void suggestPredicateReordering_DifferentSelectivity_SuggestsReordering() {
        // Create predicates with different selectivity
        SimplePredicate highSelectivity = new SimplePredicate("id", "=", 123, "p1"); // Very selective
        LikePredicate lowSelectivity = new LikePredicate("description", "%text%", "p2"); // Less selective
        
        List<Predicate> predicates = List.of(lowSelectivity, highSelectivity); // Wrong order
        
        List<PredicatePushdownSuggestion> suggestions = optimizer.suggestPredicateReordering(predicates);
        
        // Should suggest moving the more selective predicate earlier
        assertThat(suggestions).hasSizeGreaterThanOrEqualTo(1);
        
        PredicatePushdownSuggestion suggestion = suggestions.stream()
                .filter(s -> s.getOriginalPredicate().equals(highSelectivity))
                .findFirst()
                .orElse(null);
        
        if (suggestion != null) {
            assertThat(suggestion.getSuggestedPosition())
                    .isLessThan(suggestion.getOriginalPosition());
            assertThat(suggestion.getOptimizationType())
                    .isEqualTo(PredicatePushdownSuggestion.OptimizationType.REORDER_BY_SELECTIVITY);
        }
    }
    
    @Test
    @DisplayName("Should estimate SimplePredicate selectivity based on operator")
    void estimateSelectivity_SimplePredicate_ReturnsCorrectEstimate() {
        SimplePredicate equalityPredicate = new SimplePredicate("id", "=", 123, "p1");
        SimplePredicate notEqualPredicate = new SimplePredicate("status", "!=", "DELETED", "p2");
        SimplePredicate rangePredicate = new SimplePredicate("age", ">", 18, "p3");
        
        double equalitySelectivity = optimizer.estimateSelectivity(equalityPredicate);
        double notEqualSelectivity = optimizer.estimateSelectivity(notEqualPredicate);
        double rangeSelectivity = optimizer.estimateSelectivity(rangePredicate);
        
        assertThat(equalitySelectivity).isEqualTo(0.1); // Equality is selective
        assertThat(notEqualSelectivity).isEqualTo(0.9); // Not equal is not selective
        assertThat(rangeSelectivity).isEqualTo(0.3); // Range has moderate selectivity
    }
    
    @Test
    @DisplayName("Should estimate InPredicate selectivity based on value count")
    void estimateSelectivity_InPredicate_ReturnsCorrectEstimate() {
        InPredicate smallInPredicate = new InPredicate("status", 
                List.of("ACTIVE", "PENDING"), "p1");
        InPredicate largeInPredicate = new InPredicate("category", 
                List.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J"), 
                "p1");
        
        double smallInSelectivity = optimizer.estimateSelectivity(smallInPredicate);
        double largeInSelectivity = optimizer.estimateSelectivity(largeInPredicate);
        
        assertThat(smallInSelectivity).isLessThan(largeInSelectivity);
        assertThat(smallInSelectivity).isEqualTo(0.02); // 2/100
        assertThat(largeInSelectivity).isEqualTo(0.1); // 10/100
    }
    
    @Test
    @DisplayName("Should estimate BetweenPredicate selectivity")
    void estimateSelectivity_BetweenPredicate_ReturnsCorrectEstimate() {
        BetweenPredicate betweenPredicate = new BetweenPredicate("age", 25, 65, "p1", "p2");
        
        double selectivity = optimizer.estimateSelectivity(betweenPredicate);
        
        assertThat(selectivity).isEqualTo(0.3);
    }
    
    @Test
    @DisplayName("Should estimate LikePredicate selectivity based on pattern")
    void estimateSelectivity_LikePredicate_ReturnsCorrectEstimate() {
        LikePredicate exactPattern = new LikePredicate("email", "john.doe@example.com", "p1");
        LikePredicate prefixPattern = new LikePredicate("name", "John%", "p2");
        LikePredicate wildcardPattern = new LikePredicate("description", "%keyword%", "p3");
        
        double exactSelectivity = optimizer.estimateSelectivity(exactPattern);
        double prefixSelectivity = optimizer.estimateSelectivity(prefixPattern);
        double wildcardSelectivity = optimizer.estimateSelectivity(wildcardPattern);
        
        assertThat(exactSelectivity).isEqualTo(0.1); // No wildcards = very selective
        assertThat(prefixSelectivity).isEqualTo(0.3); // Prefix = moderately selective
        assertThat(wildcardSelectivity).isEqualTo(0.7); // Leading wildcard = not very selective
    }
    
    @Test
    @DisplayName("Should estimate NullPredicate selectivity")
    void estimateSelectivity_NullPredicate_ReturnsCorrectEstimate() {
        NullPredicate nullPredicate = new NullPredicate("optional_field", true);
        
        double selectivity = optimizer.estimateSelectivity(nullPredicate);
        
        assertThat(selectivity).isEqualTo(0.1); // NULL checks are usually selective
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException when predicate is null")
    void estimateSelectivity_NullPredicate_ThrowsException() {
        assertThatThrownBy(() -> optimizer.estimateSelectivity(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Predicate must not be null");
    }
    
    @Test
    @DisplayName("Should identify pushdown opportunities using join graph")
    void identifyPushdownOpportunities_ValidJoinGraph_ReturnsSuggestions() {
        SimplePredicate predicate = new SimplePredicate("user_id", "=", 123, "p1");
        List<Predicate> predicates = List.of(predicate);
        
        when(mockJoinGraph.getTables()).thenReturn(Set.of("users", "orders"));
        when(mockJoinGraph.canPushToTable(predicate, "users")).thenReturn(true);
        
        List<PredicatePushdownSuggestion> suggestions = 
                optimizer.identifyPushdownOpportunities(predicates, mockJoinGraph);
        
        assertThat(suggestions).hasSizeGreaterThanOrEqualTo(1);
        
        PredicatePushdownSuggestion suggestion = suggestions.get(0);
        assertThat(suggestion.getOptimizationType())
                .isEqualTo(PredicatePushdownSuggestion.OptimizationType.PUSH_TO_JOIN_SOURCE);
        assertThat(suggestion.getTargetTable()).isEqualTo("users");
        assertThat(suggestion.getPriority()).isEqualTo(PredicatePushdownSuggestion.Priority.HIGH);
    }
    
    @Test
    @DisplayName("Should optimize for index usage")
    void optimizeForIndexUsage_PredicatesWithIndexes_ReturnsSuggestions() {
        SimplePredicate indexedPredicate = new SimplePredicate("email", "=", "test@example.com", "p1");
        SimplePredicate nonIndexedPredicate = new SimplePredicate("description", "LIKE", "%text%", "p2");
        
        List<Predicate> predicates = List.of(nonIndexedPredicate, indexedPredicate);
        List<String> availableIndexes = List.of("email", "user_id");
        
        List<PredicatePushdownSuggestion> suggestions = 
                optimizer.optimizeForIndexUsage(predicates, availableIndexes);
        
        assertThat(suggestions).hasSizeGreaterThanOrEqualTo(1);
        
        PredicatePushdownSuggestion suggestion = suggestions.stream()
                .filter(s -> s.getOriginalPredicate().equals(indexedPredicate))
                .findFirst()
                .orElse(null);
        
        if (suggestion != null) {
            assertThat(suggestion.getOptimizationType())
                    .isEqualTo(PredicatePushdownSuggestion.OptimizationType.INDEX_FRIENDLY_REORDER);
            assertThat(suggestion.getPriority())
                    .isEqualTo(PredicatePushdownSuggestion.Priority.MEDIUM);
        }
    }
    
    @Test
    @DisplayName("Should handle null parameters gracefully")
    void identifyPushdownOpportunities_NullParameters_ThrowsException() {
        assertThatThrownBy(() -> optimizer.identifyPushdownOpportunities(null, mockJoinGraph))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Predicates list must not be null");
        
        assertThatThrownBy(() -> optimizer.identifyPushdownOpportunities(List.of(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Join graph must not be null");
    }
    
    @Test
    @DisplayName("Should handle null parameters gracefully")
    void optimizeForIndexUsage_NullParameters_ThrowsException() {
        assertThatThrownBy(() -> optimizer.optimizeForIndexUsage(null, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Predicates list must not be null");
        
        assertThatThrownBy(() -> optimizer.optimizeForIndexUsage(List.of(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Available indexes list must not be null");
    }
}