package com.github.query4j.optimizer.index;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for IndexSuggestion data class.
 * Tests index suggestion creation, SQL generation, and utility methods.
 */
class IndexSuggestionTest {
    
    @Test
    @DisplayName("Should create index suggestion with all required fields")
    void builder_AllRequiredFields_CreatesValidSuggestion() {
        IndexSuggestion suggestion = IndexSuggestion.builder()
                .tableName("users")
                .columnNames(List.of("email"))
                .reason("Email lookup optimization")
                .build();
        
        assertThat(suggestion.getTableName()).isEqualTo("users");
        assertThat(suggestion.getColumnNames()).containsExactly("email");
        assertThat(suggestion.getReason()).isEqualTo("Email lookup optimization");
        assertThat(suggestion.getIndexType()).isEqualTo(IndexSuggestion.IndexType.BTREE); // default
        assertThat(suggestion.getPriority()).isEqualTo(IndexSuggestion.Priority.MEDIUM); // default
    }
    
    @Test
    @DisplayName("Should generate correct CREATE INDEX SQL for single column")
    void generateCreateIndexSQL_SingleColumn_GeneratesCorrectSQL() {
        IndexSuggestion suggestion = IndexSuggestion.builder()
                .tableName("users")
                .columnNames(List.of("email"))
                .reason("Test")
                .build();
        
        String sql = suggestion.generateCreateIndexSQL();
        
        assertThat(sql).isEqualTo("CREATE INDEX idx_users_email ON users (email)");
    }
    
    @Test
    @DisplayName("Should generate correct CREATE INDEX SQL for composite index")
    void generateCreateIndexSQL_CompositeIndex_GeneratesCorrectSQL() {
        IndexSuggestion suggestion = IndexSuggestion.builder()
                .tableName("users")
                .columnNames(List.of("first_name", "last_name", "department"))
                .reason("Test")
                .build();
        
        String sql = suggestion.generateCreateIndexSQL();
        
        assertThat(sql).isEqualTo("CREATE INDEX idx_users_first_name_last_name_department ON users (first_name, last_name, department)");
    }
    
    @Test
    @DisplayName("Should correctly identify composite indexes")
    void isComposite_SingleColumn_ReturnsFalse() {
        IndexSuggestion suggestion = IndexSuggestion.builder()
                .tableName("users")
                .columnNames(List.of("email"))
                .reason("Test")
                .build();
        
        assertThat(suggestion.isComposite()).isFalse();
    }
    
    @Test
    @DisplayName("Should correctly identify composite indexes")
    void isComposite_MultipleColumns_ReturnsTrue() {
        IndexSuggestion suggestion = IndexSuggestion.builder()
                .tableName("users")
                .columnNames(List.of("first_name", "last_name"))
                .reason("Test")
                .build();
        
        assertThat(suggestion.isComposite()).isTrue();
    }
    
    @Test
    @DisplayName("Should handle custom index type")
    void builder_CustomIndexType_SetsCorrectType() {
        IndexSuggestion suggestion = IndexSuggestion.builder()
                .tableName("users")
                .columnNames(List.of("email"))
                .indexType(IndexSuggestion.IndexType.UNIQUE)
                .reason("Test")
                .build();
        
        assertThat(suggestion.getIndexType()).isEqualTo(IndexSuggestion.IndexType.UNIQUE);
    }
    
    @Test
    @DisplayName("Should handle custom priority")
    void builder_CustomPriority_SetsCorrectPriority() {
        IndexSuggestion suggestion = IndexSuggestion.builder()
                .tableName("users")
                .columnNames(List.of("id"))
                .priority(IndexSuggestion.Priority.HIGH)
                .reason("Test")
                .build();
        
        assertThat(suggestion.getPriority()).isEqualTo(IndexSuggestion.Priority.HIGH);
    }
    
    @Test
    @DisplayName("Should handle selectivity value")
    void builder_CustomSelectivity_SetsCorrectSelectivity() {
        IndexSuggestion suggestion = IndexSuggestion.builder()
                .tableName("users")
                .columnNames(List.of("email"))
                .selectivity(0.15)
                .reason("Test")
                .build();
        
        assertThat(suggestion.getSelectivity()).isEqualTo(0.15);
    }
    
    @Test
    @DisplayName("Should handle expected impact description")
    void builder_CustomExpectedImpact_SetsCorrectImpact() {
        IndexSuggestion suggestion = IndexSuggestion.builder()
                .tableName("users")
                .columnNames(List.of("status"))
                .expectedImpact("Major performance improvement for status queries")
                .reason("Test")
                .build();
        
        assertThat(suggestion.getExpectedImpact()).isEqualTo("Major performance improvement for status queries");
    }
    
    @Test
    @DisplayName("IndexType enum should have correct display names")
    void indexType_DisplayNames_AreCorrect() {
        assertThat(IndexSuggestion.IndexType.BTREE.getDisplayName()).isEqualTo("B-Tree");
        assertThat(IndexSuggestion.IndexType.HASH.getDisplayName()).isEqualTo("Hash");
        assertThat(IndexSuggestion.IndexType.COMPOSITE.getDisplayName()).isEqualTo("Composite");
        assertThat(IndexSuggestion.IndexType.PARTIAL.getDisplayName()).isEqualTo("Partial");
        assertThat(IndexSuggestion.IndexType.UNIQUE.getDisplayName()).isEqualTo("Unique");
    }
    
    @Test
    @DisplayName("Priority enum should have correct descriptions")
    void priority_Descriptions_AreCorrect() {
        assertThat(IndexSuggestion.Priority.HIGH.getDescription())
                .isEqualTo("High - Significant performance improvement expected");
        assertThat(IndexSuggestion.Priority.MEDIUM.getDescription())
                .isEqualTo("Medium - Moderate performance improvement expected");
        assertThat(IndexSuggestion.Priority.LOW.getDescription())
                .isEqualTo("Low - Minor performance improvement expected");
    }
    
    @Test
    @DisplayName("Should be immutable - builder creates new instances")
    void immutability_BuilderPattern_CreatesNewInstances() {
        IndexSuggestion.IndexSuggestionBuilder builder = IndexSuggestion.builder()
                .tableName("users")
                .columnNames(List.of("email"))
                .reason("Test");
        
        IndexSuggestion suggestion1 = builder.build();
        IndexSuggestion suggestion2 = builder.priority(IndexSuggestion.Priority.HIGH).build();
        
        // Should be different instances
        assertThat(suggestion1).isNotSameAs(suggestion2);
        assertThat(suggestion1.getPriority()).isEqualTo(IndexSuggestion.Priority.MEDIUM);
        assertThat(suggestion2.getPriority()).isEqualTo(IndexSuggestion.Priority.HIGH);
    }
}