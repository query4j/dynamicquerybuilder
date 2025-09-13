package com.github.query4j.optimizer;

import com.github.query4j.optimizer.index.IndexSuggestion;
import com.github.query4j.optimizer.join.JoinReorderSuggestion;
import com.github.query4j.optimizer.predicate.PredicatePushdownSuggestion;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.List;

/**
 * Immutable result container for query optimization analysis.
 * Contains index suggestions, predicate pushdown recommendations,
 * and join reordering suggestions.
 *
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
@Value
@Builder
public class OptimizationResult {
    
    /**
     * List of index suggestions based on predicate and join analysis.
     * Never null, but may be empty if no suggestions are available.
     */
    @NonNull
    @Builder.Default
    List<IndexSuggestion> indexSuggestions = List.of();
    
    /**
     * List of predicate pushdown suggestions for query optimization.
     * Never null, but may be empty if no improvements are identified.
     */
    @NonNull
    @Builder.Default
    List<PredicatePushdownSuggestion> predicatePushdownSuggestions = List.of();
    
    /**
     * List of join reordering suggestions to minimize intermediate result sizes.
     * Never null, but may be empty if no joins are present or improvements identified.
     */
    @NonNull
    @Builder.Default
    List<JoinReorderSuggestion> joinReorderSuggestions = List.of();
    
    /**
     * Analysis execution time in milliseconds for performance tracking.
     */
    long analysisTimeMs;
    
    /**
     * Human-readable summary of optimization recommendations.
     * Never null, provides textual description of suggested improvements.
     */
    @NonNull
    String summary;
    
    /**
     * Checks if any optimization suggestions are available.
     *
     * @return true if there are any suggestions for improvement, false otherwise
     */
    public boolean hasSuggestions() {
        return !indexSuggestions.isEmpty() || 
               !predicatePushdownSuggestions.isEmpty() || 
               !joinReorderSuggestions.isEmpty();
    }
    
    /**
     * Returns the total number of optimization suggestions.
     *
     * @return total count of all suggestions
     */
    public int getTotalSuggestionCount() {
        return indexSuggestions.size() + 
               predicatePushdownSuggestions.size() + 
               joinReorderSuggestions.size();
    }
}