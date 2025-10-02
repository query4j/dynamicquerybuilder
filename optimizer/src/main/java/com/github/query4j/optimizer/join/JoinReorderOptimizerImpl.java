package com.github.query4j.optimizer.join;

import com.github.query4j.core.QueryBuilder;
import com.github.query4j.optimizer.OptimizerConfig;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.*;

/**
 * Implementation of JoinReorderOptimizer that analyzes join sequences
 * and suggests optimal reordering to minimize intermediate result sizes.
 *
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
@RequiredArgsConstructor
public class JoinReorderOptimizerImpl implements JoinReorderOptimizer {
    
    @NonNull
    private final OptimizerConfig config;
    
    @Override
    public List<JoinReorderSuggestion> optimizeJoinOrder(QueryBuilder<?> queryBuilder) {
        if (queryBuilder == null) {
            throw new IllegalArgumentException("QueryBuilder must not be null");
        }
        
        // For now, return basic suggestions
        // In a full implementation, this would extract join information from QueryBuilder
        return createBasicJoinSuggestions();
    }
    
    @Override
    public List<JoinReorderSuggestion> analyzeJoinSequence(
            List<String> joinSequence, 
            List<JoinReorderSuggestion.JoinCondition> joinConditions) {
        
        if (joinSequence == null) {
            throw new IllegalArgumentException("Join sequence must not be null");
        }
        if (joinConditions == null) {
            throw new IllegalArgumentException("Join conditions must not be null");
        }
        
        if (joinSequence.size() < 2) {
            return List.of(); // No reordering needed for single table
        }
        
        List<JoinReorderSuggestion> suggestions = new ArrayList<>();
        
        // Calculate optimal sequence based on selectivity
        List<String> optimalSequence = calculateOptimalSequence(joinSequence, joinConditions);
        
        // Check if reordering would provide significant benefit
        double improvement = calculateImprovementPotential(joinSequence, optimalSequence, joinConditions);
        
        if (improvement > config.getJoinReorderingThreshold()) {
            suggestions.add(JoinReorderSuggestion.builder()
                    .originalJoinSequence(new ArrayList<>(joinSequence))
                    .suggestedJoinSequence(optimalSequence)
                    .estimatedImprovement(improvement)
                    .reorderType(JoinReorderSuggestion.JoinReorderType.SELECTIVITY_BASED)
                    .reason("Reordering joins based on estimated selectivity to reduce intermediate result sizes")
                    .expectedImpact(String.format("Estimated %.1f%% reduction in intermediate result size", improvement * 100))
                    .priority(improvement > 0.5 ? JoinReorderSuggestion.Priority.HIGH : 
                             JoinReorderSuggestion.Priority.MEDIUM)
                    .influencingConditions(new ArrayList<>(joinConditions))
                    .build());
        }
        
        return suggestions;
    }
    
    @Override
    public double estimateCardinalityReduction(List<String> joinSequence, TableStatistics tableStatistics) {
        if (joinSequence == null) {
            throw new IllegalArgumentException("Join sequence must not be null");
        }
        
        if (joinSequence.size() < 2 || tableStatistics == null) {
            return 0.0; // No improvement possible
        }
        
        // Calculate estimated cardinality for current sequence
        double currentCardinality = estimateSequenceCardinality(joinSequence, tableStatistics);
        
        // Calculate estimated cardinality for optimal sequence
        List<String> optimalSequence = reorderByCardinality(joinSequence, tableStatistics);
        double optimalCardinality = estimateSequenceCardinality(optimalSequence, tableStatistics);
        
        // Return improvement ratio
        if (currentCardinality > 0) {
            return Math.max(0.0, (currentCardinality - optimalCardinality) / currentCardinality);
        }
        
        return 0.0;
    }
    
    @Override
    public List<JoinReorderSuggestion> optimizeForIndexUsage(
            List<String> joinSequence, 
            Map<String, List<String>> indexInformation) {
        
        if (joinSequence == null) {
            throw new IllegalArgumentException("Join sequence must not be null");
        }
        if (indexInformation == null) {
            throw new IllegalArgumentException("Index information must not be null");
        }
        
        List<JoinReorderSuggestion> suggestions = new ArrayList<>();
        
        // Find tables with good index coverage for join operations
        List<String> indexOptimizedSequence = reorderByIndexCoverage(joinSequence, indexInformation);
        
        if (!joinSequence.equals(indexOptimizedSequence)) {
            suggestions.add(JoinReorderSuggestion.builder()
                    .originalJoinSequence(new ArrayList<>(joinSequence))
                    .suggestedJoinSequence(indexOptimizedSequence)
                    .estimatedImprovement(0.3) // Reasonable estimate for index-based improvement
                    .reorderType(JoinReorderSuggestion.JoinReorderType.INDEX_DRIVEN)
                    .reason("Reordering to maximize usage of available indexes on join columns")
                    .expectedImpact("Improved join performance through better index utilization")
                    .priority(JoinReorderSuggestion.Priority.MEDIUM)
                    .build());
        }
        
        return suggestions;
    }
    
    /**
     * Calculates optimal join sequence based on selectivity estimates.
     */
    private List<String> calculateOptimalSequence(
            List<String> originalSequence, 
            List<JoinReorderSuggestion.JoinCondition> joinConditions) {
        
        List<String> optimized = new ArrayList<>(originalSequence);
        
        // Sort by selectivity (more selective joins first)
        Map<String, Double> tableSelectivity = calculateTableSelectivity(joinConditions);
        
        optimized.sort((t1, t2) -> {
            double sel1 = tableSelectivity.getOrDefault(t1, 0.5);
            double sel2 = tableSelectivity.getOrDefault(t2, 0.5);
            return Double.compare(sel1, sel2);
        });
        
        return optimized;
    }
    
    /**
     * Calculates selectivity for each table based on join conditions.
     */
    private Map<String, Double> calculateTableSelectivity(List<JoinReorderSuggestion.JoinCondition> joinConditions) {
        Map<String, Double> selectivity = new HashMap<>();
        
        for (JoinReorderSuggestion.JoinCondition condition : joinConditions) {
            selectivity.put(condition.getLeftTable(), condition.getSelectivity());
            selectivity.put(condition.getRightTable(), condition.getSelectivity());
        }
        
        return selectivity;
    }
    
    /**
     * Calculates improvement potential between two join sequences.
     */
    private double calculateImprovementPotential(
            List<String> original, 
            List<String> optimal, 
            List<JoinReorderSuggestion.JoinCondition> joinConditions) {
        
        if (original.equals(optimal)) {
            return 0.0; // No improvement
        }
        
        // Estimate improvement based on how much the sequence changed
        // and the selectivity of the joins
        double avgSelectivity = joinConditions.stream()
                .mapToDouble(JoinReorderSuggestion.JoinCondition::getSelectivity)
                .average()
                .orElse(0.5);
        
        // More selective joins moved earlier = better improvement
        double positionImprovement = calculatePositionImprovement(original, optimal);
        
        return Math.min(0.8, positionImprovement * (1.0 - avgSelectivity));
    }
    
    /**
     * Calculates how much positions improved in the reordered sequence.
     */
    private double calculatePositionImprovement(List<String> original, List<String> optimal) {
        int changes = 0;
        for (int i = 0; i < original.size(); i++) {
            if (!original.get(i).equals(optimal.get(i))) {
                changes++;
            }
        }
        
        return changes / (double) original.size();
    }
    
    /**
     * Estimates cardinality for a join sequence.
     */
    private double estimateSequenceCardinality(List<String> sequence, TableStatistics stats) {
        if (sequence.isEmpty()) {
            return 0.0;
        }
        
        double cardinality = stats.getEstimatedRowCount(sequence.get(0));
        
        for (int i = 1; i < sequence.size(); i++) {
            String currentTable = sequence.get(i);
            String previousTable = sequence.get(i - 1);
            
            double tableRows = stats.getEstimatedRowCount(currentTable);
            double joinSelectivity = stats.getJoinSelectivity(previousTable, currentTable, "id");
            
            cardinality *= tableRows * joinSelectivity;
        }
        
        return cardinality;
    }
    
    /**
     * Reorders tables by estimated cardinality (smallest first).
     */
    private List<String> reorderByCardinality(List<String> sequence, TableStatistics stats) {
        List<String> reordered = new ArrayList<>(sequence);
        
        reordered.sort((t1, t2) -> {
            long rows1 = stats.getEstimatedRowCount(t1);
            long rows2 = stats.getEstimatedRowCount(t2);
            return Long.compare(rows1, rows2);
        });
        
        return reordered;
    }
    
    /**
     * Reorders tables to maximize index coverage.
     */
    private List<String> reorderByIndexCoverage(List<String> sequence, Map<String, List<String>> indexInfo) {
        List<String> reordered = new ArrayList<>(sequence);
        
        // Prioritize tables with more indexes (better join performance)
        reordered.sort((t1, t2) -> {
            int indexes1 = indexInfo.getOrDefault(t1, List.of()).size();
            int indexes2 = indexInfo.getOrDefault(t2, List.of()).size();
            return Integer.compare(indexes2, indexes1); // Descending order
        });
        
        return reordered;
    }
    
    /**
     * Creates basic join suggestions for demonstration.
     */
    private List<JoinReorderSuggestion> createBasicJoinSuggestions() {
        // This is a placeholder - in reality would analyze actual joins
        return List.of();
    }
}