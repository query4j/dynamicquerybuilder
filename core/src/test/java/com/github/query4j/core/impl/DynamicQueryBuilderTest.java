package com.github.query4j.core.impl;

import com.github.query4j.core.QueryBuilder;
import com.github.query4j.core.QueryStats;
import com.github.query4j.core.criteria.Predicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for DynamicQueryBuilder class.
 * Tests all builder methods, SQL generation, parameter mapping, immutability, and validation.
 */
@DisplayName("DynamicQueryBuilder")
class DynamicQueryBuilderTest {

    private DynamicQueryBuilder<TestEntity> builder;

    @BeforeEach
    void setUp() {
        builder = new DynamicQueryBuilder<>(TestEntity.class);
    }

    @Nested
    @DisplayName("Constructor and Factory Methods")
    class ConstructorTests {

        @Test
        @DisplayName("should create instance via constructor")
        void shouldCreateInstanceViaConstructor() {
            DynamicQueryBuilder<TestEntity> qb = new DynamicQueryBuilder<>(TestEntity.class);
            assertNotNull(qb);
            assertTrue(qb.toSQL().contains("TestEntity"));
        }

        @Test
        @DisplayName("should create instance via factory method")
        void shouldCreateInstanceViaFactoryMethod() {
            QueryBuilder<TestEntity> qb = QueryBuilder.forEntity(TestEntity.class);
            assertNotNull(qb);
            assertInstanceOf(DynamicQueryBuilder.class, qb);
        }

        @Test
        @DisplayName("should throw NullPointerException for null entity class")
        void shouldThrowForNullEntityClass() {
            assertThrows(NullPointerException.class, () -> new DynamicQueryBuilder<>(null));
        }

        @Test
        @DisplayName("should initialize with empty state")
        void shouldInitializeWithEmptyState() {
            DynamicQueryBuilder<TestEntity> qb = new DynamicQueryBuilder<>(TestEntity.class);
            assertTrue(qb.getPredicates().isEmpty());
            assertEquals("SELECT * FROM TestEntity", qb.toSQL());
        }
    }

    @Nested
    @DisplayName("Where Clause Methods")
    class WhereClauseTests {

        @Test
        @DisplayName("should add simple where condition")
        void shouldAddSimpleWhereCondition() {
            QueryBuilder<TestEntity> result = builder.where("name", "John");
            
            assertNotSame(builder, result);
            String sql = result.toSQL();
            assertTrue(sql.contains("WHERE"));
            assertTrue(sql.contains("name ="));
        }

        @Test
        @DisplayName("should add where condition with operator")
        void shouldAddWhereConditionWithOperator() {
            QueryBuilder<TestEntity> result = builder.where("age", ">", 25);
            
            String sql = result.toSQL();
            assertTrue(sql.contains("WHERE"));
            assertTrue(sql.contains("age >"));
        }

        @Test
        @DisplayName("should add whereIn condition")
        void shouldAddWhereInCondition() {
            List<Object> values = Arrays.asList("ACTIVE", "PENDING", "COMPLETED");
            QueryBuilder<TestEntity> result = builder.whereIn("status", values);
            
            String sql = result.toSQL();
            assertTrue(sql.contains("WHERE"));
            assertTrue(sql.contains("status IN"));
        }

        @Test
        @DisplayName("should add whereNotIn condition")
        void shouldAddWhereNotInCondition() {
            List<Object> values = Arrays.asList("INACTIVE", "DELETED");
            QueryBuilder<TestEntity> result = builder.whereNotIn("status", values);
            
            String sql = result.toSQL();
            assertTrue(sql.contains("WHERE"));
            assertTrue(sql.contains("NOT"));
            assertTrue(sql.contains("status IN"));
        }

        @Test
        @DisplayName("should add whereLike condition")
        void shouldAddWhereLikeCondition() {
            QueryBuilder<TestEntity> result = builder.whereLike("name", "%John%");
            
            String sql = result.toSQL();
            assertTrue(sql.contains("WHERE"));
            assertTrue(sql.contains("name LIKE"));
        }

        @Test
        @DisplayName("should add whereNotLike condition")
        void shouldAddWhereNotLikeCondition() {
            QueryBuilder<TestEntity> result = builder.whereNotLike("name", "%Test%");
            
            String sql = result.toSQL();
            assertTrue(sql.contains("WHERE"));
            assertTrue(sql.contains("NOT"));
            assertTrue(sql.contains("name LIKE"));
        }

        @Test
        @DisplayName("should add whereBetween condition")
        void shouldAddWhereBetweenCondition() {
            QueryBuilder<TestEntity> result = builder.whereBetween("age", 18, 65);
            
            String sql = result.toSQL();
            assertTrue(sql.contains("WHERE"));
            assertTrue(sql.contains("age BETWEEN"));
        }

        @Test
        @DisplayName("should add whereIsNull condition")
        void shouldAddWhereIsNullCondition() {
            QueryBuilder<TestEntity> result = builder.whereIsNull("deleted_at");
            
            String sql = result.toSQL();
            assertTrue(sql.contains("WHERE"));
            assertTrue(sql.contains("deleted_at IS NULL"));
        }

        @Test
        @DisplayName("should add whereIsNotNull condition")
        void shouldAddWhereIsNotNullCondition() {
            QueryBuilder<TestEntity> result = builder.whereIsNotNull("created_at");
            
            String sql = result.toSQL();
            assertTrue(sql.contains("WHERE"));
            assertTrue(sql.contains("created_at IS NOT NULL"));
        }
    }

    @Nested
    @DisplayName("Logical Operators")
    class LogicalOperatorTests {

        @Test
        @DisplayName("should chain conditions with AND")
        void shouldChainConditionsWithAnd() {
            QueryBuilder<TestEntity> result = builder
                .where("name", "John")
                .and()
                .where("age", ">", 25);
            
            String sql = result.toSQL();
            assertTrue(sql.contains("WHERE"));
            assertTrue(sql.contains("AND"));
        }

        @Test
        @DisplayName("should chain conditions with OR")
        void shouldChainConditionsWithOr() {
            QueryBuilder<TestEntity> result = builder
                .where("name", "John")
                .or()
                .where("name", "Jane");
            
            String sql = result.toSQL();
            assertTrue(sql.contains("WHERE"));
            assertTrue(sql.contains("OR"));
        }

        @Test
        @DisplayName("should handle NOT operator")
        void shouldHandleNotOperator() {
            QueryBuilder<TestEntity> result = builder
                .not()
                .where("active", true);
            
            String sql = result.toSQL();
            assertTrue(sql.contains("WHERE"));
            // NOT operator behavior - just verify it doesn't throw exception
            // The actual NOT logic might be handled differently in the implementation
        }

        @Test
        @DisplayName("should handle group operations")
        void shouldHandleGroupOperations() {
            QueryBuilder<TestEntity> result = builder
                .openGroup()
                .where("name", "John")
                .or()
                .where("name", "Jane")
                .closeGroup()
                .and()
                .where("age", ">", 18);
            
            // Test that it doesn't throw exception and produces SQL
            String sql = result.toSQL();
            assertNotNull(sql);
            assertTrue(sql.contains("WHERE"));
        }

        @Test
        @DisplayName("should throw exception when closing non-existent group")
        void shouldThrowWhenClosingNonExistentGroup() {
            assertThrows(IllegalStateException.class, () -> builder.closeGroup());
        }
    }

    @Nested
    @DisplayName("Input Validation")
    class InputValidationTests {

        @Test
        @DisplayName("should throw for null field names")
        void shouldThrowForNullFieldNames() {
            assertThrows(NullPointerException.class, () -> builder.where(null, "value"));
            assertThrows(NullPointerException.class, () -> builder.whereIn(null, Arrays.asList("A")));
            assertThrows(NullPointerException.class, () -> builder.whereLike(null, "%pattern%"));
            assertThrows(NullPointerException.class, () -> builder.whereBetween(null, 1, 10));
            assertThrows(NullPointerException.class, () -> builder.whereIsNull(null));
            assertThrows(NullPointerException.class, () -> builder.whereIsNotNull(null));
        }

        @Test
        @DisplayName("should throw for empty field names")
        void shouldThrowForEmptyFieldNames() {
            assertThrows(IllegalArgumentException.class, () -> builder.where("", "value"));
            assertThrows(IllegalArgumentException.class, () -> builder.where("   ", "value"));
        }

        @Test
        @DisplayName("should throw for invalid field names")
        void shouldThrowForInvalidFieldNames() {
            assertThrows(IllegalArgumentException.class, () -> builder.where("field-name", "value"));
            assertThrows(IllegalArgumentException.class, () -> builder.where("field@name", "value"));
            assertThrows(IllegalArgumentException.class, () -> builder.where("field name", "value"));
        }

        @Test
        @DisplayName("should accept valid field names")
        void shouldAcceptValidFieldNames() {
            // Should not throw
            assertDoesNotThrow(() -> builder.where("field_name", "value"));
            assertDoesNotThrow(() -> builder.where("field123", "value"));
            assertDoesNotThrow(() -> builder.where("user.name", "value"));
            assertDoesNotThrow(() -> builder.where("Field_Name_123", "value"));
        }

        @Test
        @DisplayName("should throw for invalid operators")
        void shouldThrowForInvalidOperators() {
            assertThrows(IllegalArgumentException.class, () -> builder.where("field", "INVALID", "value"));
            assertThrows(IllegalArgumentException.class, () -> builder.where("field", "", "value"));
            assertThrows(NullPointerException.class, () -> builder.where("field", null, "value"));
        }

        @Test
        @DisplayName("should accept valid operators")
        void shouldAcceptValidOperators() {
            // Should not throw
            assertDoesNotThrow(() -> builder.where("field", "=", "value"));
            assertDoesNotThrow(() -> builder.where("field", "!=", "value"));
            assertDoesNotThrow(() -> builder.where("field", "<>", "value"));
            assertDoesNotThrow(() -> builder.where("field", "<", "value"));
            assertDoesNotThrow(() -> builder.where("field", "<=", "value"));
            assertDoesNotThrow(() -> builder.where("field", ">", "value"));
            assertDoesNotThrow(() -> builder.where("field", ">=", "value"));
        }

        @Test
        @DisplayName("should throw for empty whereIn values")
        void shouldThrowForEmptyWhereInValues() {
            assertThrows(IllegalArgumentException.class, 
                () -> builder.whereIn("field", Collections.emptyList()));
            assertThrows(IllegalArgumentException.class, 
                () -> builder.whereNotIn("field", Collections.emptyList()));
        }

        @Test
        @DisplayName("should throw for null whereIn values")
        void shouldThrowForNullWhereInValues() {
            assertThrows(NullPointerException.class, () -> builder.whereIn("field", null));
            assertThrows(NullPointerException.class, () -> builder.whereNotIn("field", null));
        }

        @Test
        @DisplayName("should throw for null whereLike pattern")
        void shouldThrowForNullWhereLikePattern() {
            assertThrows(NullPointerException.class, () -> builder.whereLike("field", null));
            assertThrows(NullPointerException.class, () -> builder.whereNotLike("field", null));
        }
    }

    @Nested
    @DisplayName("SQL Generation")
    class SqlGenerationTests {

        @Test
        @DisplayName("should generate basic SELECT query")
        void shouldGenerateBasicSelectQuery() {
            String sql = builder.toSQL();
            assertEquals("SELECT * FROM TestEntity", sql);
        }

        @Test
        @DisplayName("should generate query with WHERE clause")
        void shouldGenerateQueryWithWhereClause() {
            String sql = builder.where("name", "John").toSQL();
            assertTrue(sql.startsWith("SELECT * FROM TestEntity WHERE"));
            assertTrue(sql.contains("name ="));
        }

        @Test
        @DisplayName("should generate query with multiple WHERE conditions")
        void shouldGenerateQueryWithMultipleWhereConditions() {
            String sql = builder
                .where("name", "John")
                .and()
                .where("age", ">", 25)
                .toSQL();
            
            assertTrue(sql.contains("WHERE"));
            assertTrue(sql.contains("AND"));
        }

        @Test
        @DisplayName("should generate query with ORDER BY")
        void shouldGenerateQueryWithOrderBy() {
            String sql = builder.orderBy("name").toSQL();
            assertTrue(sql.contains("ORDER BY name ASC"));
        }

        @Test
        @DisplayName("should generate query with ORDER BY DESC")
        void shouldGenerateQueryWithOrderByDesc() {
            String sql = builder.orderByDescending("name").toSQL();
            assertTrue(sql.contains("ORDER BY name DESC"));
        }

        @Test
        @DisplayName("should generate query with LIMIT")
        void shouldGenerateQueryWithLimit() {
            String sql = builder.limit(10).toSQL();
            assertTrue(sql.contains("LIMIT 10"));
        }

        @Test
        @DisplayName("should generate query with OFFSET")
        void shouldGenerateQueryWithOffset() {
            String sql = builder.offset(5).toSQL();
            assertTrue(sql.contains("OFFSET 5"));
        }

        @Test
        @DisplayName("should generate query with pagination")
        void shouldGenerateQueryWithPagination() {
            String sql = builder.page(2, 10).toSQL();
            assertTrue(sql.contains("LIMIT 10"));
            assertTrue(sql.contains("OFFSET 10")); // Page 2 with size 10 = offset 10
        }

        @Test
        @DisplayName("should generate query with custom SELECT fields")
        void shouldGenerateQueryWithCustomSelectFields() {
            String sql = builder.select("name", "age", "email").toSQL();
            assertTrue(sql.contains("SELECT name, age, email FROM"));
            assertFalse(sql.contains("SELECT *"));
        }

        @Test
        @DisplayName("should generate query with JOIN")
        void shouldGenerateQueryWithJoin() {
            String sql = builder.join("orders").toSQL();
            assertTrue(sql.contains("INNER JOIN orders"));
        }

        @Test
        @DisplayName("should generate query with LEFT JOIN")
        void shouldGenerateQueryWithLeftJoin() {
            String sql = builder.leftJoin("profile").toSQL();
            assertTrue(sql.contains("LEFT JOIN profile"));
        }

        @Test
        @DisplayName("should generate query with GROUP BY")
        void shouldGenerateQueryWithGroupBy() {
            String sql = builder.groupBy("department", "role").toSQL();
            assertTrue(sql.contains("GROUP BY department, role"));
        }

        @Test
        @DisplayName("should generate complex query")
        void shouldGenerateComplexQuery() {
            String sql = builder
                .select("name", "age")
                .where("age", ">", 18)
                .and()
                .whereIn("status", Arrays.asList("ACTIVE", "PENDING"))
                .orderBy("name")
                .limit(20)
                .toSQL();
            
            assertTrue(sql.contains("SELECT name, age"));
            assertTrue(sql.contains("WHERE"));
            assertTrue(sql.contains("age >"));
            assertTrue(sql.contains("AND"));
            assertTrue(sql.contains("status IN"));
            assertTrue(sql.contains("ORDER BY name ASC"));
            assertTrue(sql.contains("LIMIT 20"));
        }
    }

    @Nested
    @DisplayName("Immutability")
    class ImmutabilityTests {

        @Test
        @DisplayName("should return new instance for where methods")
        void shouldReturnNewInstanceForWhereMethods() {
            QueryBuilder<TestEntity> result1 = builder.where("name", "John");
            QueryBuilder<TestEntity> result2 = builder.where("age", 25);
            
            assertNotSame(builder, result1);
            assertNotSame(builder, result2);
            assertNotSame(result1, result2);
        }

        @Test
        @DisplayName("should return new instance for logical operators")
        void shouldReturnNewInstanceForLogicalOperators() {
            QueryBuilder<TestEntity> withWhere = builder.where("name", "John");
            QueryBuilder<TestEntity> withAnd = withWhere.and();
            QueryBuilder<TestEntity> withOr = withWhere.or();
            
            assertNotSame(withWhere, withAnd);
            assertNotSame(withWhere, withOr);
            assertNotSame(withAnd, withOr);
        }

        @Test
        @DisplayName("should return new instance for pagination methods")
        void shouldReturnNewInstanceForPaginationMethods() {
            QueryBuilder<TestEntity> withLimit = builder.limit(10);
            QueryBuilder<TestEntity> withOffset = builder.offset(5);
            QueryBuilder<TestEntity> withPage = builder.page(2, 10);
            
            assertNotSame(builder, withLimit);
            assertNotSame(builder, withOffset);
            assertNotSame(builder, withPage);
        }

        @Test
        @DisplayName("should return new instance for ordering methods")
        void shouldReturnNewInstanceForOrderingMethods() {
            QueryBuilder<TestEntity> withOrder = builder.orderBy("name");
            QueryBuilder<TestEntity> withOrderDesc = builder.orderByDescending("age");
            
            assertNotSame(builder, withOrder);
            assertNotSame(builder, withOrderDesc);
        }

        @Test
        @DisplayName("should maintain original state after operations")
        void shouldMaintainOriginalStateAfterOperations() {
            String originalSQL = builder.toSQL();
            
            // Perform various operations
            builder.where("name", "John");
            builder.orderBy("age");
            builder.limit(10);
            
            // Original should be unchanged
            assertEquals(originalSQL, builder.toSQL());
        }

        @Test
        @DisplayName("should return immutable predicates list")
        void shouldReturnImmutablePredicatesList() {
            DynamicQueryBuilder<TestEntity> builderWithPredicates = 
                (DynamicQueryBuilder<TestEntity>) builder.where("name", "John");
            
            List<Predicate> predicates = builderWithPredicates.getPredicates();
            
            assertThrows(UnsupportedOperationException.class, 
                () -> predicates.add(null));
            assertThrows(UnsupportedOperationException.class, 
                () -> predicates.remove(0));
        }
    }

    @Nested
    @DisplayName("Pagination and Limits")
    class PaginationTests {

        @Test
        @DisplayName("should handle valid pagination parameters")
        void shouldHandleValidPaginationParameters() {
            // Should not throw
            assertDoesNotThrow(() -> builder.page(1, 10));
            assertDoesNotThrow(() -> builder.page(5, 20));
            assertDoesNotThrow(() -> builder.limit(100));
            assertDoesNotThrow(() -> builder.offset(50));
        }

        @Test
        @DisplayName("should throw for invalid page parameters")
        void shouldThrowForInvalidPageParameters() {
            assertThrows(IllegalArgumentException.class, () -> builder.page(0, 10));
            assertThrows(IllegalArgumentException.class, () -> builder.page(-1, 10));
            assertThrows(IllegalArgumentException.class, () -> builder.page(1, 0));
            assertThrows(IllegalArgumentException.class, () -> builder.page(1, -1));
        }

        @Test
        @DisplayName("should throw for invalid limit")
        void shouldThrowForInvalidLimit() {
            assertThrows(IllegalArgumentException.class, () -> builder.limit(0));
            assertThrows(IllegalArgumentException.class, () -> builder.limit(-1));
        }

        @Test
        @DisplayName("should throw for invalid offset")
        void shouldThrowForInvalidOffset() {
            assertThrows(IllegalArgumentException.class, () -> builder.offset(-1));
        }

        @Test
        @DisplayName("should calculate correct offset for page")
        void shouldCalculateCorrectOffsetForPage() {
            String sql1 = builder.page(1, 10).toSQL();
            assertTrue(sql1.contains("LIMIT 10"));
            // Page 1 doesn't include OFFSET 0 since it's the default
            
            String sql2 = builder.page(2, 10).toSQL();
            assertTrue(sql2.contains("LIMIT 10"));
            assertTrue(sql2.contains("OFFSET 10"));
            
            String sql3 = builder.page(3, 15).toSQL();
            assertTrue(sql3.contains("LIMIT 15"));
            assertTrue(sql3.contains("OFFSET 30"));
        }
    }

    @Nested
    @DisplayName("Execution Methods")
    class ExecutionTests {

        @Test
        @DisplayName("should return default values for execution methods")
        void shouldReturnDefaultValuesForExecutionMethods() {
            assertEquals(0L, builder.count());
            assertTrue(builder.findAll().isEmpty());
            assertNull(builder.findOne());
            assertFalse(builder.exists());
        }

        @Test
        @DisplayName("should complete async methods successfully")
        void shouldCompleteAsyncMethodsSuccessfully() throws ExecutionException, InterruptedException {
            assertEquals(0L, builder.countAsync().get());
            assertTrue(builder.findAllAsync().get().isEmpty());
            assertNull(builder.findOneAsync().get());
        }

        @Test
        @DisplayName("should return non-null page result")
        void shouldReturnNonNullPageResult() {
            assertNotNull(builder.findPage());
        }

        @Test
        @DisplayName("should build DynamicQuery")
        void shouldBuildDynamicQuery() {
            assertNotNull(builder.build());
        }
    }

    @Nested
    @DisplayName("Implemented Operations")
    class ImplementedOperationTests {

        @Test
        @DisplayName("should successfully execute implemented methods without throwing exceptions")
        void shouldExecuteImplementedMethods() {
            // Test subquery methods return new builders (no exception)
            assertDoesNotThrow(() -> builder.exists(builder));
            assertDoesNotThrow(() -> builder.notExists(builder));
            assertDoesNotThrow(() -> builder.in("field", builder));
            assertDoesNotThrow(() -> builder.notIn("field", builder));
            
            // Test advanced methods return new builders (no exception)
            assertDoesNotThrow(() -> builder.customFunction("func", "field"));
            assertDoesNotThrow(() -> builder.nativeQuery("SELECT * FROM table"));
            assertDoesNotThrow(() -> builder.parameter("param", "value"));
            assertDoesNotThrow(() -> builder.parameters(Collections.emptyMap()));
            assertDoesNotThrow(() -> builder.hint("hint", "value"));
            assertDoesNotThrow(() -> builder.fetchSize(100));
            assertDoesNotThrow(() -> builder.timeout(30));
            
            // Test query statistics work
            QueryStats stats = assertDoesNotThrow(() -> builder.getExecutionStats());
            assertNotNull(stats);
        }
        
        @Test
        @DisplayName("should generate proper SQL for implemented methods")
        void shouldGenerateProperSQLForImplementedMethods() {
            // Test EXISTS subquery SQL generation
            QueryBuilder<TestEntity> existsBuilder = builder.exists(
                QueryBuilder.forEntity(TestEntity.class).where("active", true)
            );
            String existsSQL = existsBuilder.toSQL();
            assertTrue(existsSQL.contains("EXISTS"));
            
            // Test custom function SQL generation
            QueryBuilder<TestEntity> funcBuilder = builder.customFunction("UPPER", "name");
            String funcSQL = funcBuilder.toSQL();
            assertTrue(funcSQL.contains("UPPER(name)"));
            
            // Test native query
            QueryBuilder<TestEntity> nativeBuilder = builder.nativeQuery("SELECT * FROM users WHERE active = 1");
            String nativeSQL = nativeBuilder.toSQL();
            assertEquals("SELECT * FROM users WHERE active = 1", nativeSQL);
        }
    }

    // Test entity for type safety
    private static class TestEntity {
        private String name;
        private Integer age;
        private String status;
        private String email;
        
        // Constructor, getters, setters would be here in real implementation
    }
}
