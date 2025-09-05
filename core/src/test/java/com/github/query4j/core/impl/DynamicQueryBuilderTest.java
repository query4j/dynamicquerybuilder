package com.github.query4j.core.impl;

import com.github.query4j.core.QueryBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class DynamicQueryBuilderTest {

    @Test
    void forEntityShouldCreateInstance() {
        QueryBuilder<Object> qb = QueryBuilder.forEntity(Object.class);
        assertNotNull(qb, "QueryBuilder instance should not be null");
    }

    @Test
    void whereShouldAddCondition() {
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        QueryBuilder<Object> modified = builder.where("field1", "value");

        assertNotSame(builder, modified, "Builder should be immutable and return new instance");
    }

    @Test
    void countReturnsZeroByDefault() {
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        long count = builder.count();
        assertEquals(0, count, "Default count should be 0");
    }

    @Test
    void findAllReturnsEmptyListByDefault() {
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        List<Object> results = builder.findAll();
        assertNotNull(results, "Results list should not be null");
        assertTrue(results.isEmpty(), "Results list should be empty");
    }

    @Test
    void findOneReturnsNullByDefault() {
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        assertNull(builder.findOne(), "findOne should return null by default");
    }

    @Test
    void queryBuilderThrowsOnNullFieldName() {
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        assertThrows(IllegalArgumentException.class, () -> builder.where(null, "value"));
        assertThrows(IllegalArgumentException.class, () -> builder.where("", "value"));
    }

    @Test
    void asyncMethodsCompleteSuccessfully() throws ExecutionException, InterruptedException {
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);

        assertNotNull(builder.findAllAsync());
        assertNotNull(builder.findOneAsync());
        assertNotNull(builder.countAsync());

        assertEquals(0, builder.countAsync().get());
        assertTrue(builder.findAllAsync().get().isEmpty());
        assertNull(builder.findOneAsync().get());
    }

    @Test
    void limitAndOffsetValidation() {
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        assertThrows(IllegalArgumentException.class, () -> builder.limit(0));
        assertThrows(IllegalArgumentException.class, () -> builder.limit(-5));
        assertThrows(IllegalArgumentException.class, () -> builder.offset(-1));

        assertNotNull(builder.limit(10));
        assertNotNull(builder.offset(1));
    }

    @Test
    void toSqlGeneratesQueryString() {
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        String sql = builder.toSQL();
        assertNotNull(sql);
        assertTrue(sql.contains("SELECT"));
        assertTrue(sql.contains(Object.class.getSimpleName()));
    }
}
