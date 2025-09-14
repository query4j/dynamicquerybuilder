package com.github.query4j.optimizer.join;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for JoinReorderSuggestion and its inner classes
 * to achieve 95%+ code coverage.
 */
@DisplayName("JoinReorderSuggestion Tests")
class JoinReorderSuggestionTest {
    
    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {
        
        @Test
        @DisplayName("Should build suggestion with required fields")
        void shouldBuildSuggestionWithRequiredFields() {
            List<String> original = List.of("table1", "table2");
            List<String> suggested = List.of("table2", "table1");
            
            JoinReorderSuggestion suggestion = JoinReorderSuggestion.builder()
                    .originalJoinSequence(original)
                    .suggestedJoinSequence(suggested)
                    .reorderType(JoinReorderSuggestion.JoinReorderType.SELECTIVITY_BASED)
                    .reason("Test reason")
                    .build();
            
            assertThat(suggestion.getOriginalJoinSequence()).isEqualTo(original);
            assertThat(suggestion.getSuggestedJoinSequence()).isEqualTo(suggested);
            assertThat(suggestion.getReorderType()).isEqualTo(JoinReorderSuggestion.JoinReorderType.SELECTIVITY_BASED);
            assertThat(suggestion.getReason()).isEqualTo("Test reason");
            assertThat(suggestion.getEstimatedImprovement()).isEqualTo(0.0); // Default value
            assertThat(suggestion.getExpectedImpact()).isEqualTo("Reduced intermediate join result sizes"); // Default
            assertThat(suggestion.getPriority()).isEqualTo(JoinReorderSuggestion.Priority.MEDIUM); // Default
            assertThat(suggestion.getInfluencingConditions()).isEmpty(); // Default
        }
        
        @Test
        @DisplayName("Should build suggestion with all fields")
        void shouldBuildSuggestionWithAllFields() {
            List<String> original = List.of("table1", "table2", "table3");
            List<String> suggested = List.of("table3", "table1", "table2");
            List<JoinReorderSuggestion.JoinCondition> conditions = List.of(
                    JoinReorderSuggestion.JoinCondition.builder()
                            .leftTable("table1")
                            .rightTable("table2")
                            .joinField("id")
                            .selectivity(0.1)
                            .hasIndex(true)
                            .build()
            );
            
            JoinReorderSuggestion suggestion = JoinReorderSuggestion.builder()
                    .originalJoinSequence(original)
                    .suggestedJoinSequence(suggested)
                    .estimatedImprovement(0.75)
                    .reorderType(JoinReorderSuggestion.JoinReorderType.INDEX_DRIVEN)
                    .reason("Custom reason")
                    .expectedImpact("Custom impact")
                    .priority(JoinReorderSuggestion.Priority.HIGH)
                    .influencingConditions(conditions)
                    .build();
            
            assertThat(suggestion.getOriginalJoinSequence()).isEqualTo(original);
            assertThat(suggestion.getSuggestedJoinSequence()).isEqualTo(suggested);
            assertThat(suggestion.getEstimatedImprovement()).isEqualTo(0.75);
            assertThat(suggestion.getReorderType()).isEqualTo(JoinReorderSuggestion.JoinReorderType.INDEX_DRIVEN);
            assertThat(suggestion.getReason()).isEqualTo("Custom reason");
            assertThat(suggestion.getExpectedImpact()).isEqualTo("Custom impact");
            assertThat(suggestion.getPriority()).isEqualTo(JoinReorderSuggestion.Priority.HIGH);
            assertThat(suggestion.getInfluencingConditions()).hasSize(1);
        }
        
        @Test
        @DisplayName("Should throw exception when required fields are null")
        void shouldThrowExceptionWhenRequiredFieldsAreNull() {
            assertThatThrownBy(() -> JoinReorderSuggestion.builder().build())
                    .isInstanceOf(NullPointerException.class);
        }
    }
    
    @Nested
    @DisplayName("Method Tests")
    class MethodTests {
        
        @Test
        @DisplayName("Should detect when reordering is recommended")
        void shouldDetectWhenReorderingIsRecommended() {
            JoinReorderSuggestion suggestion = JoinReorderSuggestion.builder()
                    .originalJoinSequence(List.of("table1", "table2"))
                    .suggestedJoinSequence(List.of("table2", "table1"))
                    .reorderType(JoinReorderSuggestion.JoinReorderType.SELECTIVITY_BASED)
                    .reason("Test")
                    .build();
            
            assertThat(suggestion.isReorderingRecommended()).isTrue();
        }
        
        @Test
        @DisplayName("Should detect when reordering is not recommended")
        void shouldDetectWhenReorderingIsNotRecommended() {
            JoinReorderSuggestion suggestion = JoinReorderSuggestion.builder()
                    .originalJoinSequence(List.of("table1", "table2"))
                    .suggestedJoinSequence(List.of("table1", "table2"))
                    .reorderType(JoinReorderSuggestion.JoinReorderType.SELECTIVITY_BASED)
                    .reason("Test")
                    .build();
            
            assertThat(suggestion.isReorderingRecommended()).isFalse();
        }
        
        @Test
        @DisplayName("Should calculate correct sequence change count when reordering is recommended")
        void shouldCalculateCorrectSequenceChangeCountWhenReorderingIsRecommended() {
            JoinReorderSuggestion suggestion = JoinReorderSuggestion.builder()
                    .originalJoinSequence(List.of("table1", "table2", "table3"))
                    .suggestedJoinSequence(List.of("table3", "table2", "table1"))
                    .reorderType(JoinReorderSuggestion.JoinReorderType.SELECTIVITY_BASED)
                    .reason("Test")
                    .build();
            
            assertThat(suggestion.getSequenceChangeCount()).isEqualTo(2); // table1 and table3 changed positions
        }
        
        @Test
        @DisplayName("Should return zero change count when no reordering")
        void shouldReturnZeroChangeCountWhenNoReordering() {
            JoinReorderSuggestion suggestion = JoinReorderSuggestion.builder()
                    .originalJoinSequence(List.of("table1", "table2"))
                    .suggestedJoinSequence(List.of("table1", "table2"))
                    .reorderType(JoinReorderSuggestion.JoinReorderType.SELECTIVITY_BASED)
                    .reason("Test")
                    .build();
            
            assertThat(suggestion.getSequenceChangeCount()).isEqualTo(0);
        }
        
        @Test
        @DisplayName("Should handle partial sequence changes")
        void shouldHandlePartialSequenceChanges() {
            JoinReorderSuggestion suggestion = JoinReorderSuggestion.builder()
                    .originalJoinSequence(List.of("table1", "table2", "table3", "table4"))
                    .suggestedJoinSequence(List.of("table1", "table3", "table2", "table4"))
                    .reorderType(JoinReorderSuggestion.JoinReorderType.SELECTIVITY_BASED)
                    .reason("Test")
                    .build();
            
            assertThat(suggestion.getSequenceChangeCount()).isEqualTo(2); // table2 and table3 swapped
        }
    }
    
    @Nested
    @DisplayName("JoinReorderType Enum Tests")
    class JoinReorderTypeEnumTests {
        
        @Test
        @DisplayName("Should have correct display names for all types")
        void shouldHaveCorrectDisplayNamesForAllTypes() {
            assertThat(JoinReorderSuggestion.JoinReorderType.SELECTIVITY_BASED.getDisplayName())
                    .isEqualTo("Reorder based on predicate selectivity");
            assertThat(JoinReorderSuggestion.JoinReorderType.CARDINALITY_REDUCTION.getDisplayName())
                    .isEqualTo("Minimize intermediate result cardinality");
            assertThat(JoinReorderSuggestion.JoinReorderType.INDEX_DRIVEN.getDisplayName())
                    .isEqualTo("Optimize for available indexes");
            assertThat(JoinReorderSuggestion.JoinReorderType.COST_BASED.getDisplayName())
                    .isEqualTo("Cost-based optimization");
            assertThat(JoinReorderSuggestion.JoinReorderType.NESTED_LOOP_OPTIMIZATION.getDisplayName())
                    .isEqualTo("Optimize nested loop joins");
        }
        
        @Test
        @DisplayName("Should have all enum values")
        void shouldHaveAllEnumValues() {
            JoinReorderSuggestion.JoinReorderType[] types = JoinReorderSuggestion.JoinReorderType.values();
            assertThat(types).hasSize(5);
            assertThat(types).contains(
                    JoinReorderSuggestion.JoinReorderType.SELECTIVITY_BASED,
                    JoinReorderSuggestion.JoinReorderType.CARDINALITY_REDUCTION,
                    JoinReorderSuggestion.JoinReorderType.INDEX_DRIVEN,
                    JoinReorderSuggestion.JoinReorderType.COST_BASED,
                    JoinReorderSuggestion.JoinReorderType.NESTED_LOOP_OPTIMIZATION
            );
        }
        
        @Test
        @DisplayName("Should support valueOf for all types")
        void shouldSupportValueOfForAllTypes() {
            assertThat(JoinReorderSuggestion.JoinReorderType.valueOf("SELECTIVITY_BASED"))
                    .isEqualTo(JoinReorderSuggestion.JoinReorderType.SELECTIVITY_BASED);
            assertThat(JoinReorderSuggestion.JoinReorderType.valueOf("CARDINALITY_REDUCTION"))
                    .isEqualTo(JoinReorderSuggestion.JoinReorderType.CARDINALITY_REDUCTION);
            assertThat(JoinReorderSuggestion.JoinReorderType.valueOf("INDEX_DRIVEN"))
                    .isEqualTo(JoinReorderSuggestion.JoinReorderType.INDEX_DRIVEN);
            assertThat(JoinReorderSuggestion.JoinReorderType.valueOf("COST_BASED"))
                    .isEqualTo(JoinReorderSuggestion.JoinReorderType.COST_BASED);
            assertThat(JoinReorderSuggestion.JoinReorderType.valueOf("NESTED_LOOP_OPTIMIZATION"))
                    .isEqualTo(JoinReorderSuggestion.JoinReorderType.NESTED_LOOP_OPTIMIZATION);
        }
    }
    
    @Nested
    @DisplayName("Priority Enum Tests")
    class PriorityEnumTests {
        
        @Test
        @DisplayName("Should have correct descriptions for all priorities")
        void shouldHaveCorrectDescriptionsForAllPriorities() {
            assertThat(JoinReorderSuggestion.Priority.HIGH.getDescription())
                    .isEqualTo("High - Significant performance improvement expected");
            assertThat(JoinReorderSuggestion.Priority.MEDIUM.getDescription())
                    .isEqualTo("Medium - Moderate join performance improvement expected");
            assertThat(JoinReorderSuggestion.Priority.LOW.getDescription())
                    .isEqualTo("Low - Minor join optimization opportunity");
        }
        
        @Test
        @DisplayName("Should have all enum values")
        void shouldHaveAllEnumValues() {
            JoinReorderSuggestion.Priority[] priorities = JoinReorderSuggestion.Priority.values();
            assertThat(priorities).hasSize(3);
            assertThat(priorities).contains(
                    JoinReorderSuggestion.Priority.HIGH,
                    JoinReorderSuggestion.Priority.MEDIUM,
                    JoinReorderSuggestion.Priority.LOW
            );
        }
        
        @Test
        @DisplayName("Should support valueOf for all priorities")
        void shouldSupportValueOfForAllPriorities() {
            assertThat(JoinReorderSuggestion.Priority.valueOf("HIGH"))
                    .isEqualTo(JoinReorderSuggestion.Priority.HIGH);
            assertThat(JoinReorderSuggestion.Priority.valueOf("MEDIUM"))
                    .isEqualTo(JoinReorderSuggestion.Priority.MEDIUM);
            assertThat(JoinReorderSuggestion.Priority.valueOf("LOW"))
                    .isEqualTo(JoinReorderSuggestion.Priority.LOW);
        }
    }
    
    @Nested
    @DisplayName("JoinCondition Tests")
    class JoinConditionTests {
        
        @Test
        @DisplayName("Should build join condition with required fields")
        void shouldBuildJoinConditionWithRequiredFields() {
            JoinReorderSuggestion.JoinCondition condition = JoinReorderSuggestion.JoinCondition.builder()
                    .leftTable("table1")
                    .rightTable("table2")
                    .joinField("id")
                    .build();
            
            assertThat(condition.getLeftTable()).isEqualTo("table1");
            assertThat(condition.getRightTable()).isEqualTo("table2");
            assertThat(condition.getJoinField()).isEqualTo("id");
            assertThat(condition.getSelectivity()).isEqualTo(0.5); // Default value
            assertThat(condition.isHasIndex()).isFalse(); // Default value
        }
        
        @Test
        @DisplayName("Should build join condition with all fields")
        void shouldBuildJoinConditionWithAllFields() {
            JoinReorderSuggestion.JoinCondition condition = JoinReorderSuggestion.JoinCondition.builder()
                    .leftTable("users")
                    .rightTable("orders")
                    .joinField("user_id")
                    .selectivity(0.1)
                    .hasIndex(true)
                    .build();
            
            assertThat(condition.getLeftTable()).isEqualTo("users");
            assertThat(condition.getRightTable()).isEqualTo("orders");
            assertThat(condition.getJoinField()).isEqualTo("user_id");
            assertThat(condition.getSelectivity()).isEqualTo(0.1);
            assertThat(condition.isHasIndex()).isTrue();
        }
        
        @Test
        @DisplayName("Should throw exception when required fields are null")
        void shouldThrowExceptionWhenRequiredFieldsAreNull() {
            assertThatThrownBy(() -> JoinReorderSuggestion.JoinCondition.builder().build())
                    .isInstanceOf(NullPointerException.class);
        }
        
        @Test
        @DisplayName("Should support various selectivity values")
        void shouldSupportVariousSelectivityValues() {
            JoinReorderSuggestion.JoinCondition lowSelectivity = JoinReorderSuggestion.JoinCondition.builder()
                    .leftTable("table1")
                    .rightTable("table2")
                    .joinField("id")
                    .selectivity(0.9)
                    .build();
            
            JoinReorderSuggestion.JoinCondition highSelectivity = JoinReorderSuggestion.JoinCondition.builder()
                    .leftTable("table1")
                    .rightTable("table2")
                    .joinField("id")
                    .selectivity(0.01)
                    .build();
            
            assertThat(lowSelectivity.getSelectivity()).isEqualTo(0.9);
            assertThat(highSelectivity.getSelectivity()).isEqualTo(0.01);
        }
        
        @Test
        @DisplayName("Should handle index presence flag")
        void shouldHandleIndexPresenceFlag() {
            JoinReorderSuggestion.JoinCondition withIndex = JoinReorderSuggestion.JoinCondition.builder()
                    .leftTable("table1")
                    .rightTable("table2")
                    .joinField("indexed_field")
                    .hasIndex(true)
                    .build();
            
            JoinReorderSuggestion.JoinCondition withoutIndex = JoinReorderSuggestion.JoinCondition.builder()
                    .leftTable("table1")
                    .rightTable("table2")
                    .joinField("non_indexed_field")
                    .hasIndex(false)
                    .build();
            
            assertThat(withIndex.isHasIndex()).isTrue();
            assertThat(withoutIndex.isHasIndex()).isFalse();
        }
    }
    
    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {
        
        @Test
        @DisplayName("Should be immutable - lists cannot be modified")
        void shouldBeImmutableListsCannotBeModified() {
            JoinReorderSuggestion suggestion = JoinReorderSuggestion.builder()
                    .originalJoinSequence(List.of("table1", "table2"))
                    .suggestedJoinSequence(List.of("table2", "table1"))
                    .reorderType(JoinReorderSuggestion.JoinReorderType.SELECTIVITY_BASED)
                    .reason("Test")
                    .build();
            
            // Lists should be unmodifiable
            assertThatThrownBy(() -> suggestion.getOriginalJoinSequence().clear())
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> suggestion.getSuggestedJoinSequence().clear())
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> suggestion.getInfluencingConditions().clear())
                    .isInstanceOf(UnsupportedOperationException.class);
        }
        
        @Test
        @DisplayName("Should maintain equals and hashcode contract")
        void shouldMaintainEqualsAndHashcodeContract() {
            JoinReorderSuggestion suggestion1 = JoinReorderSuggestion.builder()
                    .originalJoinSequence(List.of("table1", "table2"))
                    .suggestedJoinSequence(List.of("table2", "table1"))
                    .reorderType(JoinReorderSuggestion.JoinReorderType.SELECTIVITY_BASED)
                    .reason("Test")
                    .build();
            
            JoinReorderSuggestion suggestion2 = JoinReorderSuggestion.builder()
                    .originalJoinSequence(List.of("table1", "table2"))
                    .suggestedJoinSequence(List.of("table2", "table1"))
                    .reorderType(JoinReorderSuggestion.JoinReorderType.SELECTIVITY_BASED)
                    .reason("Test")
                    .build();
            
            assertThat(suggestion1).isEqualTo(suggestion2);
            assertThat(suggestion1.hashCode()).isEqualTo(suggestion2.hashCode());
        }
        
        @Test
        @DisplayName("Should have proper toString representation")
        void shouldHaveProperToStringRepresentation() {
            JoinReorderSuggestion suggestion = JoinReorderSuggestion.builder()
                    .originalJoinSequence(List.of("table1", "table2"))
                    .suggestedJoinSequence(List.of("table2", "table1"))
                    .reorderType(JoinReorderSuggestion.JoinReorderType.SELECTIVITY_BASED)
                    .reason("Test reason")
                    .build();
            
            String toString = suggestion.toString();
            assertThat(toString).contains("JoinReorderSuggestion");
            assertThat(toString).contains("table1");
            assertThat(toString).contains("table2");
            assertThat(toString).contains("SELECTIVITY_BASED");
            assertThat(toString).contains("Test reason");
        }
    }
}