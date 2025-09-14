package com.github.query4j.optimizer.predicate;

import com.github.query4j.core.QueryBuilder;
import com.github.query4j.core.criteria.Predicate;
import com.github.query4j.core.criteria.SimplePredicate;
import com.github.query4j.core.criteria.InPredicate;
import com.github.query4j.core.criteria.BetweenPredicate;
import com.github.query4j.core.criteria.LikePredicate;
import com.github.query4j.core.criteria.NullPredicate;
import com.github.query4j.optimizer.OptimizerConfig;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Implementation of PredicatePushdownOptimizer that analyzes predicate selectivity
 * and suggests optimal ordering and pushdown opportunities.
 *
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
@RequiredArgsConstructor
public class PredicatePushdownOptimizerImpl implements PredicatePushdownOptimizer {
    
    @NonNull
    private final OptimizerConfig config;
    
    @Override
    public List<PredicatePushdownSuggestion> optimizeQuery(QueryBuilder<?> queryBuilder) {
        if (queryBuilder == null) {
            throw new IllegalArgumentException("QueryBuilder must not be null");
        }
        
        // For now, we'll create some basic suggestions
        // In a full implementation, this would extract actual predicates from the QueryBuilder
        return createBasicOptimizationSuggestions();
    }
    
    @Override
    public List<PredicatePushdownSuggestion> suggestPredicateReordering(List<Predicate> predicates) {
        if (predicates == null) {
            throw new IllegalArgumentException("Predicates list must not be null");
        }
        
        if (predicates.isEmpty()) {
            return List.of();
        }
        
        List<PredicatePushdownSuggestion> suggestions = new ArrayList<>();
        
        // Calculate selectivity for each predicate
        Map<Predicate, Double> selectivities = new HashMap<>();
        for (Predicate predicate : predicates) {
            selectivities.put(predicate, estimateSelectivity(predicate));
        }
        
        // Sort predicates by selectivity (most selective first)
        List<Predicate> sortedBySelectivity = new ArrayList<>(predicates);
        sortedBySelectivity.sort(Comparator.comparingDouble(selectivities::get));
        
        // Generate reordering suggestions
        for (int i = 0; i < predicates.size(); i++) {
            Predicate currentPredicate = predicates.get(i);
            int optimalPosition = sortedBySelectivity.indexOf(currentPredicate);
            
            // Only suggest reordering if improvement is significant
            double improvement = Math.abs(i - optimalPosition) / (double) predicates.size();
            if (improvement > config.getPredicateReorderingThreshold()) {
                suggestions.add(PredicatePushdownSuggestion.builder()
                        .originalPredicate(currentPredicate)
                        .originalPosition(i)
                        .suggestedPosition(optimalPosition)
                        .selectivity(selectivities.get(currentPredicate))
                        .optimizationType(PredicatePushdownSuggestion.OptimizationType.REORDER_BY_SELECTIVITY)
                        .reason(String.format("More selective predicate should be evaluated earlier (selectivity: %.2f)", 
                                selectivities.get(currentPredicate)))
                        .expectedImpact("Reduced rows processed by subsequent predicates")
                        .priority(improvement > 0.5 ? PredicatePushdownSuggestion.Priority.HIGH : 
                                 PredicatePushdownSuggestion.Priority.MEDIUM)
                        .build());
            }
        }
        
        return suggestions;
    }
    
    @Override
    public double estimateSelectivity(Predicate predicate) {
        if (predicate == null) {
            throw new IllegalArgumentException("Predicate must not be null");
        }
        
        // Estimate selectivity based on predicate type
        if (predicate instanceof SimplePredicate) {
            SimplePredicate simple = (SimplePredicate) predicate;
            return estimateSimplePredicateSelectivity(simple);
        } else if (predicate instanceof InPredicate) {
            InPredicate inPredicate = (InPredicate) predicate;
            // IN predicates selectivity depends on the number of values
            return Math.min(0.5, inPredicate.getValues().size() / 100.0);
        } else if (predicate instanceof BetweenPredicate) {
            // BETWEEN typically has moderate selectivity
            return 0.3;
        } else if (predicate instanceof LikePredicate) {
            LikePredicate like = (LikePredicate) predicate;
            return estimateLikeSelectivity(like.getPattern());
        } else if (predicate instanceof NullPredicate) {
            // NULL checks are usually quite selective
            return 0.1;
        }
        
        // Default selectivity for unknown predicate types
        return 0.5;
    }
    
    @Override
    public List<PredicatePushdownSuggestion> identifyPushdownOpportunities(
            List<Predicate> predicates, 
            JoinGraph joinGraph) {
        
        if (predicates == null) {
            throw new IllegalArgumentException("Predicates list must not be null");
        }
        if (joinGraph == null) {
            throw new IllegalArgumentException("Join graph must not be null");
        }
        
        List<PredicatePushdownSuggestion> suggestions = new ArrayList<>();
        
        for (int i = 0; i < predicates.size(); i++) {
            Predicate predicate = predicates.get(i);
            
            // Check if predicate can be pushed to specific tables
            for (String table : joinGraph.getTables()) {
                if (joinGraph.canPushToTable(predicate, table)) {
                    suggestions.add(PredicatePushdownSuggestion.builder()
                            .originalPredicate(predicate)
                            .originalPosition(i)
                            .suggestedPosition(0) // Push to earliest position
                            .selectivity(estimateSelectivity(predicate))
                            .optimizationType(PredicatePushdownSuggestion.OptimizationType.PUSH_TO_JOIN_SOURCE)
                            .reason("Predicate can be pushed to table: " + table)
                            .expectedImpact("Reduced data transfer from " + table)
                            .priority(PredicatePushdownSuggestion.Priority.HIGH)
                            .targetTable(table)
                            .build());
                }
            }
        }
        
        return suggestions;
    }
    
    @Override
    public List<PredicatePushdownSuggestion> optimizeForIndexUsage(
            List<Predicate> predicates, 
            List<String> availableIndexes) {
        
        if (predicates == null) {
            throw new IllegalArgumentException("Predicates list must not be null");
        }
        if (availableIndexes == null) {
            throw new IllegalArgumentException("Available indexes list must not be null");
        }
        
        List<PredicatePushdownSuggestion> suggestions = new ArrayList<>();
        
        // Analyze which predicates can benefit from available indexes
        for (int i = 0; i < predicates.size(); i++) {
            Predicate predicate = predicates.get(i);
            
            if (canBenefitFromIndex(predicate, availableIndexes)) {
                suggestions.add(PredicatePushdownSuggestion.builder()
                        .originalPredicate(predicate)
                        .originalPosition(i)
                        .suggestedPosition(Math.max(0, i - 1)) // Move earlier to use index
                        .selectivity(estimateSelectivity(predicate))
                        .optimizationType(PredicatePushdownSuggestion.OptimizationType.INDEX_FRIENDLY_REORDER)
                        .reason("Predicate can benefit from available index")
                        .expectedImpact("Improved query performance through index usage")
                        .priority(PredicatePushdownSuggestion.Priority.MEDIUM)
                        .build());
            }
        }
        
        return suggestions;
    }
    
    /**
     * Estimates selectivity for SimplePredicate based on operator.
     */
    private double estimateSimplePredicateSelectivity(SimplePredicate predicate) {
        String operator = predicate.getOperator();
        
        switch (operator.toUpperCase()) {
            case "=":
                return 0.1; // Equality is usually selective
            case "!=":
            case "<>":
                return 0.9; // Not equal is usually not selective
            case "<":
            case "<=":
            case ">":
            case ">=":
                return 0.3; // Range comparisons have moderate selectivity
            default:
                return 0.5; // Default for unknown operators
        }
    }
    
    /**
     * Estimates selectivity for LIKE patterns.
     */
    private double estimateLikeSelectivity(String pattern) {
        if (pattern == null) {
            return 0.5;
        }
        
        // Count wildcards to estimate selectivity
        long wildcardCount = pattern.chars().filter(ch -> ch == '%' || ch == '_').count();
        
        if (wildcardCount == 0) {
            return 0.1; // No wildcards = very selective
        } else if (pattern.startsWith("%")) {
            return 0.7; // Leading wildcard = not very selective
        } else {
            return 0.3; // Prefix match = moderately selective
        }
    }
    
    /**
     * Checks if a predicate can benefit from available indexes.
     */
    private boolean canBenefitFromIndex(Predicate predicate, List<String> availableIndexes) {
        if (predicate instanceof SimplePredicate) {
            SimplePredicate simple = (SimplePredicate) predicate;
            return availableIndexes.contains(simple.getField());
        } else if (predicate instanceof InPredicate) {
            InPredicate inPredicate = (InPredicate) predicate;
            return availableIndexes.contains(inPredicate.getField());
        } else if (predicate instanceof BetweenPredicate) {
            BetweenPredicate between = (BetweenPredicate) predicate;
            return availableIndexes.contains(between.getField());
        }
        
        return false;
    }
    
    /**
     * Creates basic optimization suggestions for demonstration.
     */
    private List<PredicatePushdownSuggestion> createBasicOptimizationSuggestions() {
        // This is a placeholder implementation
        // In reality, this would analyze the actual QueryBuilder structure
        return List.of();
    }
}