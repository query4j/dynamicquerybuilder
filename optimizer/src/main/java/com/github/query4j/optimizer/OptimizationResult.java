package com.github.query4j.optimizer;

import com.github.query4j.optimizer.index.IndexSuggestion;
import com.github.query4j.optimizer.join.JoinReorderSuggestion;
import com.github.query4j.optimizer.predicate.PredicatePushdownSuggestion;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.List;

/**
 * Immutable result container for comprehensive query optimization analysis.
 * 
 * <p>
 * Contains all optimization recommendations produced by the query optimizer,
 * including index suggestions, predicate pushdown opportunities, and join
 * reordering recommendations. This class follows the immutable value object
 * pattern for thread safety and reliable sharing across components.
 * </p>
 * 
 * <p>
 * The optimization result provides three main categories of suggestions:
 * </p>
 * <ul>
 * <li><b>Index Suggestions:</b> Recommended database indexes for WHERE clauses and JOIN conditions</li>
 * <li><b>Predicate Pushdown:</b> Opportunities to move filters closer to data sources</li>
 * <li><b>Join Reordering:</b> Alternative join orders to minimize intermediate result sizes</li>
 * </ul>
 * 
 * <p>
 * Performance characteristics:
 * </p>
 * <ul>
 * <li>Analysis time: Typically &lt; 10ms for moderate complexity queries</li>
 * <li>Memory overhead: Minimal - only stores suggestion metadata</li>
 * <li>Thread safety: Fully immutable and thread-safe</li>
 * </ul>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * // Analyze a query
 * QueryOptimizer optimizer = QueryOptimizer.create();
 * QueryBuilder<User> query = QueryBuilder.forEntity(User.class)
 *     .where("email", "john@example.com")
 *     .join("orders")
 *     .where("orders.status", "ACTIVE");
 * 
 * OptimizationResult result = optimizer.optimize(query);
 * 
 * // Check if optimization is needed
 * if (result.hasSuggestions()) {
 *     System.out.println(result.getSummary());
 *     System.out.println("Total suggestions: " + result.getTotalSuggestionCount());
 *     
 *     // Review index suggestions
 *     result.getIndexSuggestions().forEach(suggestion -> {
 *         System.out.println("Create index: " + suggestion.generateCreateIndexSQL());
 *         System.out.println("Reason: " + suggestion.getReason());
 *         System.out.println("Priority: " + suggestion.getPriority());
 *     });
 * }
 * 
 * // Monitor analysis performance
 * System.out.println("Analysis completed in " + result.getAnalysisTimeMs() + "ms");
 * }</pre>
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
     * 
     * <p>
     * Each suggestion identifies a potential database index that could improve
     * query performance by speeding up WHERE clause filtering or JOIN operations.
     * Suggestions include the table, columns, index type, and estimated impact.
     * </p>
     * 
     * <p>
     * Index suggestions are prioritized based on selectivity and query frequency.
     * High-priority suggestions should be implemented first for maximum benefit.
     * </p>
     * 
     * @return list of index suggestions, never null but may be empty
     */
    @NonNull
    @Builder.Default
    List<IndexSuggestion> indexSuggestions = List.of();
    
    /**
     * List of predicate pushdown suggestions for query optimization.
     * 
     * <p>
     * Predicate pushdown moves filtering operations closer to the data source,
     * reducing the amount of data that needs to be transferred and processed.
     * This is particularly effective for queries with joins where filters can
     * be applied before the join operation.
     * </p>
     * 
     * <p>
     * Each suggestion identifies a predicate that can be pushed down and
     * estimates the performance benefit. Implementing these suggestions can
     * significantly reduce query execution time for large datasets.
     * </p>
     * 
     * @return list of predicate pushdown suggestions, never null but may be empty
     */
    @NonNull
    @Builder.Default
    List<PredicatePushdownSuggestion> predicatePushdownSuggestions = List.of();
    
    /**
     * List of join reordering suggestions to minimize intermediate result sizes.
     * 
     * <p>
     * Join order can significantly impact query performance. This list contains
     * alternative join orders that may reduce the size of intermediate results
     * and improve overall query execution time.
     * </p>
     * 
     * <p>
     * Suggestions are based on estimated table sizes, join selectivity, and
     * available indexes. The most selective joins (those producing smallest
     * intermediate results) are recommended to execute first.
     * </p>
     * 
     * @return list of join reordering suggestions, never null but may be empty
     */
    @NonNull
    @Builder.Default
    List<JoinReorderSuggestion> joinReorderSuggestions = List.of();
    
    /**
     * Analysis execution time in milliseconds for performance tracking.
     * 
     * <p>
     * Measures the time taken to analyze the query and generate all suggestions.
     * This metric helps ensure the optimizer itself is performant and doesn't
     * add significant overhead to query planning.
     * </p>
     * 
     * <p>
     * Target: &lt; 10ms for typical queries, &lt; 100ms for complex queries
     * </p>
     * 
     * @return analysis time in milliseconds, always &gt;= 0
     */
    long analysisTimeMs;
    
    /**
     * Human-readable summary of optimization recommendations.
     * 
     * <p>
     * Provides a concise textual description of all suggested improvements,
     * suitable for logging, debugging, or presenting to users. The summary
     * highlights the most impactful optimizations and estimated benefits.
     * </p>
     * 
     * @return optimization summary, never null or empty
     */
    @NonNull
    String summary;
    
    /**
     * Checks if any optimization suggestions are available.
     * 
     * <p>
     * This is a convenience method that returns true if at least one suggestion
     * of any type (index, predicate pushdown, or join reorder) is present.
     * Use this to quickly determine if optimization is recommended.
     * </p>
     *
     * @return true if there are any suggestions for improvement, false otherwise
     */
    public boolean hasSuggestions() {
        return !indexSuggestions.isEmpty() || 
               !predicatePushdownSuggestions.isEmpty() || 
               !joinReorderSuggestions.isEmpty();
    }
    
    /**
     * Returns the total number of optimization suggestions across all categories.
     * 
     * <p>
     * This count includes all index suggestions, predicate pushdown suggestions,
     * and join reordering suggestions. Useful for metrics, reporting, and
     * prioritizing optimization efforts.
     * </p>
     *
     * @return total count of all suggestions, always &gt;= 0
     */
    public int getTotalSuggestionCount() {
        return indexSuggestions.size() + 
               predicatePushdownSuggestions.size() + 
               joinReorderSuggestions.size();
    }
}