package com.github.query4j.optimizer.predicate;

import com.github.query4j.core.criteria.Predicate;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.List;

/**
 * Immutable suggestion for predicate pushdown optimization.
 * Recommends reordering predicates to apply the most selective filters earliest
 * and push predicates closer to data sources.
 *
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
@Value
@Builder
public class PredicatePushdownSuggestion {
    
    /**
     * The original predicate that should be moved or reordered.
     * Never null.
     */
    @NonNull
    Predicate originalPredicate;
    
    /**
     * Suggested new position in the predicate evaluation order.
     * Lower values indicate earlier evaluation.
     */
    int suggestedPosition;
    
    /**
     * Original position in the predicate evaluation order.
     */
    int originalPosition;
    
    /**
     * Estimated selectivity of this predicate (0.0 to 1.0).
     * Lower values indicate more selective predicates that should be evaluated first.
     */
    @Builder.Default
    double selectivity = 0.5;
    
    /**
     * The type of optimization being suggested.
     * Never null.
     */
    @NonNull
    OptimizationType optimizationType;
    
    /**
     * Human-readable reason for the pushdown suggestion.
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
    String expectedImpact = "Reduced intermediate result set size";
    
    /**
     * Priority level for this suggestion.
     * Never null.
     */
    @NonNull
    @Builder.Default
    Priority priority = Priority.MEDIUM;
    
    /**
     * Table or join target where the predicate should be pushed.
     * May be null if the optimization is just reordering.
     */
    String targetTable;
    
    /**
     * Checks if this suggestion involves moving the predicate to a different position.
     *
     * @return true if the suggested position differs from original position
     */
    public boolean isReordering() {
        return suggestedPosition != originalPosition;
    }
    
    /**
     * Calculates the position improvement (negative means earlier execution).
     *
     * @return position delta, negative values indicate earlier execution
     */
    public int getPositionImprovement() {
        return suggestedPosition - originalPosition;
    }
    
    /**
     * Type of predicate pushdown optimization.
     */
    public enum OptimizationType {
        REORDER_BY_SELECTIVITY("Reorder by selectivity"),
        PUSH_TO_JOIN_SOURCE("Push to join source table"),
        PUSH_TO_SUBQUERY("Push to subquery"),
        EARLY_FILTER("Apply as early filter"),
        INDEX_FRIENDLY_REORDER("Reorder for index usage");
        
        private final String displayName;
        
        OptimizationType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Priority enumeration for predicate pushdown suggestions.
     */
    public enum Priority {
        HIGH("High - Significant reduction in intermediate results expected"),
        MEDIUM("Medium - Moderate performance improvement expected"),
        LOW("Low - Minor optimization opportunity");
        
        private final String description;
        
        Priority(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}