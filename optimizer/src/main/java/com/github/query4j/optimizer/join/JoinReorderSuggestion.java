package com.github.query4j.optimizer.join;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.List;

/**
 * Immutable suggestion for join reordering to minimize intermediate result sizes
 * and improve query performance. Analyzes join graph and predicate selectivity
 * to recommend optimal join sequence.
 *
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
@Value
@Builder
public class JoinReorderSuggestion {
    
    /**
     * Original join sequence as table names.
     * Never null or empty.
     */
    @NonNull
    List<String> originalJoinSequence;
    
    /**
     * Suggested optimized join sequence as table names.
     * Never null or empty.
     */
    @NonNull
    List<String> suggestedJoinSequence;
    
    /**
     * Estimated reduction in intermediate result set size (0.0 to 1.0).
     * Higher values indicate better optimization potential.
     */
    @Builder.Default
    double estimatedImprovement = 0.0;
    
    /**
     * Type of join reordering optimization being suggested.
     * Never null.
     */
    @NonNull
    JoinReorderType reorderType;
    
    /**
     * Human-readable reason for the join reordering suggestion.
     * Never null.
     */
    @NonNull
    String reason;
    
    /**
     * Expected performance impact as a descriptive string.
     * Never null.
     */
    @NonNull
    @Builder.Default
    String expectedImpact = "Reduced intermediate join result sizes";
    
    /**
     * Priority level for this suggestion.
     * Never null.
     */
    @NonNull
    @Builder.Default
    Priority priority = Priority.MEDIUM;
    
    /**
     * Join conditions that influence the reordering decision.
     * Never null, but may be empty.
     */
    @NonNull
    @Builder.Default
    List<JoinCondition> influencingConditions = List.of();
    
    /**
     * Checks if the suggested sequence differs from the original.
     *
     * @return true if reordering is recommended
     */
    public boolean isReorderingRecommended() {
        return !originalJoinSequence.equals(suggestedJoinSequence);
    }
    
    /**
     * Calculates the number of position changes in the join sequence.
     *
     * @return number of tables that changed positions
     */
    public int getSequenceChangeCount() {
        if (!isReorderingRecommended()) {
            return 0;
        }
        
        int changes = 0;
        for (int i = 0; i < originalJoinSequence.size(); i++) {
            if (!originalJoinSequence.get(i).equals(suggestedJoinSequence.get(i))) {
                changes++;
            }
        }
        return changes;
    }
    
    /**
     * Type of join reordering optimization.
     */
    public enum JoinReorderType {
        SELECTIVITY_BASED("Reorder based on predicate selectivity"),
        CARDINALITY_REDUCTION("Minimize intermediate result cardinality"),
        INDEX_DRIVEN("Optimize for available indexes"),
        COST_BASED("Cost-based optimization"),
        NESTED_LOOP_OPTIMIZATION("Optimize nested loop joins");
        
        private final String displayName;
        
        JoinReorderType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Priority enumeration for join reordering suggestions.
     */
    public enum Priority {
        HIGH("High - Significant performance improvement expected"),
        MEDIUM("Medium - Moderate join performance improvement expected"),
        LOW("Low - Minor join optimization opportunity");
        
        private final String description;
        
        Priority(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Represents a join condition that influences reordering decisions.
     */
    @Value
    @Builder
    public static class JoinCondition {
        @NonNull String leftTable;
        @NonNull String rightTable;
        @NonNull String joinField;
        @Builder.Default double selectivity = 0.5;
        @Builder.Default boolean hasIndex = false;
    }
}