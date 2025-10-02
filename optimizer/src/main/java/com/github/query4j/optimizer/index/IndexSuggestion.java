package com.github.query4j.optimizer.index;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.List;

/**
 * Immutable suggestion for database index creation to improve query performance.
 * Includes metadata about the index type, columns, and expected performance benefit.
 *
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
@Value
@Builder
public class IndexSuggestion {
    
    /**
     * The table name for which the index is suggested.
     * Never null or empty.
     */
    @NonNull
    String tableName;
    
    /**
     * List of column names that should be included in the index.
     * Order matters for composite indexes. Never null or empty.
     */
    @NonNull
    List<String> columnNames;
    
    /**
     * Type of index suggested (e.g., "BTREE", "HASH", "COMPOSITE").
     * Never null.
     */
    @NonNull
    @Builder.Default
    IndexType indexType = IndexType.BTREE;
    
    /**
     * Estimated selectivity of the index (0.0 to 1.0).
     * Lower values indicate more selective indexes.
     */
    @Builder.Default
    double selectivity = 1.0;
    
    /**
     * Human-readable reason for the index suggestion.
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
    String expectedImpact = "Moderate improvement expected";
    
    /**
     * Priority level for this suggestion.
     * Never null.
     */
    @NonNull
    @Builder.Default
    Priority priority = Priority.MEDIUM;
    
    /**
     * Generates a SQL DDL statement to create the suggested index.
     *
     * @return SQL CREATE INDEX statement, never null
     */
    public String generateCreateIndexSQL() {
        StringBuilder sql = new StringBuilder("CREATE INDEX ");
        
        // Generate index name
        String indexName = "idx_" + tableName + "_" + String.join("_", columnNames);
        sql.append(indexName);
        
        sql.append(" ON ").append(tableName);
        sql.append(" (").append(String.join(", ", columnNames)).append(")");
        
        return sql.toString();
    }
    
    /**
     * Checks if this is a composite index (multiple columns).
     *
     * @return true if the index spans multiple columns
     */
    public boolean isComposite() {
        return columnNames.size() > 1;
    }
    
    /**
     * Index type enumeration.
     */
    public enum IndexType {
        BTREE("B-Tree"),
        HASH("Hash"),
        COMPOSITE("Composite"),
        PARTIAL("Partial"),
        UNIQUE("Unique");
        
        private final String displayName;
        
        IndexType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Priority enumeration for index suggestions.
     */
    public enum Priority {
        HIGH("High - Significant performance improvement expected"),
        MEDIUM("Medium - Moderate performance improvement expected"), 
        LOW("Low - Minor performance improvement expected");
        
        private final String description;
        
        Priority(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}