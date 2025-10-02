package com.github.query4j.optimizer.predicate;

import com.github.query4j.core.criteria.SimplePredicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive unit tests for {@link PredicatePushdownSuggestion}.
 * 
 * <p>Tests cover all public methods, edge cases, and immutability contracts
 * in accordance with Query4j coding standards.</p>
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
class PredicatePushdownSuggestionTest {

    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderPatternTests {

        @Test
        @DisplayName("Should create suggestion with required fields")
        void shouldCreateSuggestionWithRequiredFields() {
            SimplePredicate predicate = new SimplePredicate("name", "=", "test", "p1");
            
            PredicatePushdownSuggestion suggestion = PredicatePushdownSuggestion.builder()
                    .originalPredicate(predicate)
                    .suggestedPosition(0)
                    .originalPosition(2)
                    .optimizationType(PredicatePushdownSuggestion.OptimizationType.REORDER_BY_SELECTIVITY)
                    .reason("High selectivity predicate should be first")
                    .build();

            assertThat(suggestion.getOriginalPredicate()).isEqualTo(predicate);
            assertThat(suggestion.getSuggestedPosition()).isZero();
            assertThat(suggestion.getOriginalPosition()).isEqualTo(2);
            assertThat(suggestion.getOptimizationType())
                    .isEqualTo(PredicatePushdownSuggestion.OptimizationType.REORDER_BY_SELECTIVITY);
            assertThat(suggestion.getReason()).isEqualTo("High selectivity predicate should be first");
        }

        @Test
        @DisplayName("Should apply default values for optional fields")
        void shouldApplyDefaultValuesForOptionalFields() {
            SimplePredicate predicate = new SimplePredicate("id", "=", 123, "p2");
            
            PredicatePushdownSuggestion suggestion = PredicatePushdownSuggestion.builder()
                    .originalPredicate(predicate)
                    .suggestedPosition(1)
                    .originalPosition(3)
                    .optimizationType(PredicatePushdownSuggestion.OptimizationType.PUSH_TO_JOIN_SOURCE)
                    .reason("Can be pushed to join source")
                    .build();

            assertThat(suggestion.getSelectivity()).isEqualTo(0.5); // default
            assertThat(suggestion.getExpectedImpact()).isEqualTo("Reduced intermediate result set size"); // default
            assertThat(suggestion.getPriority()).isEqualTo(PredicatePushdownSuggestion.Priority.MEDIUM); // default
            assertThat(suggestion.getTargetTable()).isNull(); // no default, stays null
        }

        @Test
        @DisplayName("Should fail when required fields are null")
        void shouldFailWhenRequiredFieldsAreNull() {
            assertThatThrownBy(() -> 
                    PredicatePushdownSuggestion.builder()
                            .originalPredicate(null)
                            .suggestedPosition(0)
                            .originalPosition(1)
                            .optimizationType(PredicatePushdownSuggestion.OptimizationType.REORDER_BY_SELECTIVITY)
                            .reason("Test")
                            .build())
                    .isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> 
                    PredicatePushdownSuggestion.builder()
                            .originalPredicate(new SimplePredicate("name", "=", "test", "p3"))
                            .suggestedPosition(0)
                            .originalPosition(1)
                            .optimizationType(null)
                            .reason("Test")
                            .build())
                    .isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> 
                    PredicatePushdownSuggestion.builder()
                            .originalPredicate(new SimplePredicate("name", "=", "test", "p3"))
                            .suggestedPosition(0)
                            .originalPosition(1)
                            .optimizationType(PredicatePushdownSuggestion.OptimizationType.REORDER_BY_SELECTIVITY)
                            .reason(null)
                            .build())
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should allow custom selectivity values")
        void shouldAllowCustomSelectivityValues() {
            SimplePredicate predicate = new SimplePredicate("active", "=", true, "p4");
            
            PredicatePushdownSuggestion suggestion = PredicatePushdownSuggestion.builder()
                    .originalPredicate(predicate)
                    .suggestedPosition(0)
                    .originalPosition(2)
                    .selectivity(0.1) // highly selective
                    .optimizationType(PredicatePushdownSuggestion.OptimizationType.EARLY_FILTER)
                    .reason("Boolean field with high selectivity")
                    .build();

            assertThat(suggestion.getSelectivity()).isEqualTo(0.1);
        }

        @Test
        @DisplayName("Should allow all optimization types")
        void shouldAllowAllOptimizationTypes() {
            SimplePredicate predicate = new SimplePredicate("category", "=", "electronics", "p5");
            
            for (PredicatePushdownSuggestion.OptimizationType type : PredicatePushdownSuggestion.OptimizationType.values()) {
                PredicatePushdownSuggestion suggestion = PredicatePushdownSuggestion.builder()
                        .originalPredicate(predicate)
                        .suggestedPosition(0)
                        .originalPosition(1)
                        .optimizationType(type)
                        .reason("Testing " + type.name())
                        .build();
                
                assertThat(suggestion.getOptimizationType()).isEqualTo(type);
            }
        }

        @Test
        @DisplayName("Should allow all priority levels")
        void shouldAllowAllPriorityLevels() {
            SimplePredicate predicate = new SimplePredicate("status", "=", "active", "p6");
            
            for (PredicatePushdownSuggestion.Priority priority : PredicatePushdownSuggestion.Priority.values()) {
                PredicatePushdownSuggestion suggestion = PredicatePushdownSuggestion.builder()
                        .originalPredicate(predicate)
                        .suggestedPosition(0)
                        .originalPosition(1)
                        .optimizationType(PredicatePushdownSuggestion.OptimizationType.REORDER_BY_SELECTIVITY)
                        .reason("Testing priority")
                        .priority(priority)
                        .build();
                
                assertThat(suggestion.getPriority()).isEqualTo(priority);
            }
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("isReordering should return true when positions differ")
        void isReorderingShouldReturnTrueWhenPositionsDiffer() {
            PredicatePushdownSuggestion suggestion = createBasicSuggestion(1, 3);
            
            boolean result = suggestion.isReordering(); // Call the method to get coverage
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("isReordering should return false when positions are same")
        void isReorderingShouldReturnFalseWhenPositionsAreSame() {
            PredicatePushdownSuggestion suggestion = createBasicSuggestion(2, 2);
            
            boolean result = suggestion.isReordering(); // Call the method to get coverage
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("getPositionImprovement should return correct delta")
        void getPositionImprovementShouldReturnCorrectDelta() {
            // Moving earlier in the evaluation order (improvement)
            PredicatePushdownSuggestion earlierSuggestion = createBasicSuggestion(1, 4);
            int earlierDelta = earlierSuggestion.getPositionImprovement(); // Call the method
            assertThat(earlierDelta).isEqualTo(-3);
            
            // Moving later in the evaluation order (degradation)
            PredicatePushdownSuggestion laterSuggestion = createBasicSuggestion(5, 2);
            int laterDelta = laterSuggestion.getPositionImprovement(); // Call the method
            assertThat(laterDelta).isEqualTo(3);
            
            // No change
            PredicatePushdownSuggestion noChangeSuggestion = createBasicSuggestion(2, 2);
            int noChangeDelta = noChangeSuggestion.getPositionImprovement(); // Call the method
            assertThat(noChangeDelta).isZero();
        }

        @Test
        @DisplayName("Should handle edge case positions")
        void shouldHandleEdgeCasePositions() {
            // Position 0 (first)
            PredicatePushdownSuggestion firstPosition = createBasicSuggestion(0, 5);
            assertThat(firstPosition.isReordering()).isTrue();
            assertThat(firstPosition.getPositionImprovement()).isEqualTo(-5);
            
            // Large position numbers
            PredicatePushdownSuggestion largePositions = createBasicSuggestion(100, 50);
            assertThat(largePositions.isReordering()).isTrue();
            assertThat(largePositions.getPositionImprovement()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("Enum Tests")
    class EnumTests {

        @ParameterizedTest
        @EnumSource(PredicatePushdownSuggestion.OptimizationType.class)
        @DisplayName("All OptimizationType values should have display names")
        void allOptimizationTypesShouldHaveDisplayNames(PredicatePushdownSuggestion.OptimizationType type) {
            assertThat(type.getDisplayName()).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("OptimizationType display names should be descriptive")
        void optimizationTypeDisplayNamesShouldBeDescriptive() {
            assertThat(PredicatePushdownSuggestion.OptimizationType.REORDER_BY_SELECTIVITY.getDisplayName())
                    .isEqualTo("Reorder by selectivity");
            assertThat(PredicatePushdownSuggestion.OptimizationType.PUSH_TO_JOIN_SOURCE.getDisplayName())
                    .isEqualTo("Push to join source table");
            assertThat(PredicatePushdownSuggestion.OptimizationType.PUSH_TO_SUBQUERY.getDisplayName())
                    .isEqualTo("Push to subquery");
            assertThat(PredicatePushdownSuggestion.OptimizationType.EARLY_FILTER.getDisplayName())
                    .isEqualTo("Apply as early filter");
            assertThat(PredicatePushdownSuggestion.OptimizationType.INDEX_FRIENDLY_REORDER.getDisplayName())
                    .isEqualTo("Reorder for index usage");
        }

        @ParameterizedTest
        @EnumSource(PredicatePushdownSuggestion.Priority.class)
        @DisplayName("All Priority values should have descriptions")
        void allPrioritiesShouldHaveDescriptions(PredicatePushdownSuggestion.Priority priority) {
            assertThat(priority.getDescription()).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("Priority descriptions should be informative")
        void priorityDescriptionsShouldBeInformative() {
            assertThat(PredicatePushdownSuggestion.Priority.HIGH.getDescription())
                    .isEqualTo("High - Significant reduction in intermediate results expected");
            assertThat(PredicatePushdownSuggestion.Priority.MEDIUM.getDescription())
                    .isEqualTo("Medium - Moderate performance improvement expected");
            assertThat(PredicatePushdownSuggestion.Priority.LOW.getDescription())
                    .isEqualTo("Low - Minor optimization opportunity");
        }

        @Test
        @DisplayName("Should have expected number of enum values")
        void shouldHaveExpectedNumberOfEnumValues() {
            assertThat(PredicatePushdownSuggestion.OptimizationType.values()).hasSize(5);
            assertThat(PredicatePushdownSuggestion.Priority.values()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should be immutable value object")
        void shouldBeImmutableValueObject() {
            PredicatePushdownSuggestion suggestion1 = createBasicSuggestion(1, 3);
            PredicatePushdownSuggestion suggestion2 = createBasicSuggestion(1, 3);
            
            // Test value equality
            assertThat(suggestion1).isEqualTo(suggestion2);
            assertThat(suggestion1.hashCode()).isEqualTo(suggestion2.hashCode());
        }

        @Test
        @DisplayName("Should satisfy equals and hashCode contract")
        void shouldSatisfyEqualsAndHashCodeContract() {
            SimplePredicate predicate1 = new SimplePredicate("name", "=", "test", "p3");
            SimplePredicate predicate2 = new SimplePredicate("name", "=", "test", "p3");
            SimplePredicate predicate3 = new SimplePredicate("age", "=", 25, "p7");
            
            PredicatePushdownSuggestion suggestion1 = PredicatePushdownSuggestion.builder()
                    .originalPredicate(predicate1)
                    .suggestedPosition(1)
                    .originalPosition(3)
                    .optimizationType(PredicatePushdownSuggestion.OptimizationType.REORDER_BY_SELECTIVITY)
                    .reason("Test")
                    .build();
                    
            PredicatePushdownSuggestion suggestion2 = PredicatePushdownSuggestion.builder()
                    .originalPredicate(predicate2)
                    .suggestedPosition(1)
                    .originalPosition(3)
                    .optimizationType(PredicatePushdownSuggestion.OptimizationType.REORDER_BY_SELECTIVITY)
                    .reason("Test")
                    .build();
                    
            PredicatePushdownSuggestion suggestion3 = PredicatePushdownSuggestion.builder()
                    .originalPredicate(predicate3)
                    .suggestedPosition(1)
                    .originalPosition(3)
                    .optimizationType(PredicatePushdownSuggestion.OptimizationType.REORDER_BY_SELECTIVITY)
                    .reason("Test")
                    .build();

            // Test reflexive
            assertThat(suggestion1).isEqualTo(suggestion1);
            
            // Test symmetric
            assertThat(suggestion1).isEqualTo(suggestion2);
            assertThat(suggestion2).isEqualTo(suggestion1);
            
            // Test transitive (would need more objects for complete test)
            assertThat(suggestion1).isNotEqualTo(suggestion3);
            
            // Test hashCode consistency
            assertThat(suggestion1.hashCode()).isEqualTo(suggestion2.hashCode());
            
            // Test null comparison
            assertThat(suggestion1).isNotEqualTo(null);
        }

        @Test
        @DisplayName("toString should be informative")
        void toStringShouldBeInformative() {
            PredicatePushdownSuggestion suggestion = createBasicSuggestion(1, 3);
            
            String toString = suggestion.toString();
            assertThat(toString).contains("PredicatePushdownSuggestion");
            assertThat(toString).contains("suggestedPosition=1");
            assertThat(toString).contains("originalPosition=3");
        }

        @Test
        @DisplayName("Should test all getter methods for coverage")
        void shouldTestAllGetterMethodsForCoverage() {
            SimplePredicate predicate = new SimplePredicate("testField", "=", "testValue", "p10");
            
            PredicatePushdownSuggestion suggestion = PredicatePushdownSuggestion.builder()
                    .originalPredicate(predicate)
                    .suggestedPosition(1)
                    .originalPosition(3)
                    .selectivity(0.25)
                    .optimizationType(PredicatePushdownSuggestion.OptimizationType.REORDER_BY_SELECTIVITY)
                    .reason("Test suggestion")
                    .expectedImpact("Custom impact")
                    .priority(PredicatePushdownSuggestion.Priority.HIGH)
                    .targetTable("test_table")
                    .build();

            // Call all getter methods for coverage
            assertThat(suggestion.getOriginalPredicate()).isEqualTo(predicate);
            assertThat(suggestion.getSuggestedPosition()).isEqualTo(1);
            assertThat(suggestion.getOriginalPosition()).isEqualTo(3);
            assertThat(suggestion.getSelectivity()).isEqualTo(0.25);
            assertThat(suggestion.getOptimizationType()).isEqualTo(PredicatePushdownSuggestion.OptimizationType.REORDER_BY_SELECTIVITY);
            assertThat(suggestion.getReason()).isEqualTo("Test suggestion");
            assertThat(suggestion.getExpectedImpact()).isEqualTo("Custom impact");
            assertThat(suggestion.getPriority()).isEqualTo(PredicatePushdownSuggestion.Priority.HIGH);
            assertThat(suggestion.getTargetTable()).isEqualTo("test_table");
            
            // Test behavior methods
            assertThat(suggestion.isReordering()).isTrue();
            assertThat(suggestion.getPositionImprovement()).isEqualTo(-2);
        }
    }

    @Nested
    @DisplayName("Complex Scenario Tests")
    class ComplexScenarioTests {

        @Test
        @DisplayName("Should handle suggestion with target table")
        void shouldHandleSuggestionWithTargetTable() {
            SimplePredicate predicate = new SimplePredicate("user.id", "=", 123, "p8");
            
            PredicatePushdownSuggestion suggestion = PredicatePushdownSuggestion.builder()
                    .originalPredicate(predicate)
                    .suggestedPosition(0)
                    .originalPosition(2)
                    .optimizationType(PredicatePushdownSuggestion.OptimizationType.PUSH_TO_JOIN_SOURCE)
                    .reason("Can be pushed to users table")
                    .targetTable("users")
                    .priority(PredicatePushdownSuggestion.Priority.HIGH)
                    .selectivity(0.01) // highly selective
                    .expectedImpact("Major reduction in join result set")
                    .build();

            assertThat(suggestion.getTargetTable()).isEqualTo("users");
            assertThat(suggestion.getPriority()).isEqualTo(PredicatePushdownSuggestion.Priority.HIGH);
            assertThat(suggestion.getSelectivity()).isEqualTo(0.01);
            assertThat(suggestion.getExpectedImpact()).isEqualTo("Major reduction in join result set");
            assertThat(suggestion.isReordering()).isTrue();
            assertThat(suggestion.getPositionImprovement()).isEqualTo(-2);
        }

        @Test
        @DisplayName("Should handle index optimization suggestion")
        void shouldHandleIndexOptimizationSuggestion() {
            SimplePredicate predicate = new SimplePredicate("created_date", ">=", "2023-01-01", "p9");
            
            PredicatePushdownSuggestion suggestion = PredicatePushdownSuggestion.builder()
                    .originalPredicate(predicate)
                    .suggestedPosition(0)
                    .originalPosition(0) // Already in first position
                    .optimizationType(PredicatePushdownSuggestion.OptimizationType.INDEX_FRIENDLY_REORDER)
                    .reason("Reorder for better index utilization")
                    .priority(PredicatePushdownSuggestion.Priority.MEDIUM)
                    .selectivity(0.3)
                    .expectedImpact("Improved index scan efficiency")
                    .build();

            assertThat(suggestion.isReordering()).isFalse(); // Same position
            assertThat(suggestion.getPositionImprovement()).isZero();
            assertThat(suggestion.getOptimizationType())
                    .isEqualTo(PredicatePushdownSuggestion.OptimizationType.INDEX_FRIENDLY_REORDER);
        }
    }

    /**
     * Helper method to create a basic suggestion for testing.
     *
     * @param suggestedPosition the suggested position
     * @param originalPosition the original position
     * @return a PredicatePushdownSuggestion instance
     */
    private PredicatePushdownSuggestion createBasicSuggestion(int suggestedPosition, int originalPosition) {
        SimplePredicate predicate = new SimplePredicate("testField", "=", "testValue", "p10");
        
        return PredicatePushdownSuggestion.builder()
                .originalPredicate(predicate)
                .suggestedPosition(suggestedPosition)
                .originalPosition(originalPosition)
                .optimizationType(PredicatePushdownSuggestion.OptimizationType.REORDER_BY_SELECTIVITY)
                .reason("Test suggestion")
                .build();
    }
}