package com.github.query4j.core.impl;

import com.github.query4j.core.*;
import com.github.query4j.core.criteria.*;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Immutable, thread-safe implementation of QueryBuilder interface.
 * Uses predicate-based query building with fluent API.
 *
 * @param <T> Entity type
 * @since 1.0.0
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class DynamicQueryBuilder<T> implements QueryBuilder<T> {

    @With 
    private final AtomicLong paramCounter;

    @With
    @NonNull
    private final Class<T> entityClass;

    @With
    @NonNull
    private final List<Predicate> predicates;

    @With
    private final String nextLogicalOperator;

    @With
    private final int groupDepth;

    @With
    private final int offset;

    @With
    private final int limit;

    @With
    private final boolean cacheEnabled;

    @With
    @NonNull
    private final List<String> selectFields;

    @With
    @NonNull
    private final List<String> joinClauses;

    @With
    @NonNull
    private final List<String> orderByClauses;

    @With
    @NonNull
    private final List<String> groupByClauses;

    @With
    @NonNull
    private final List<Predicate> havingPredicates;

    @With
    private final String nativeSQL;

    @With
    @NonNull
    private final Map<String, Object> namedParameters;

    @With
    private final int fetchSize;

    @With
    private final int timeoutSeconds;

    @With
    @NonNull
    private final Map<String, Object> queryHints;

    @With
    @NonNull
    private final QueryStatsImpl queryStats;

    // Default constructor for factory method
    public DynamicQueryBuilder(@NonNull Class<T> entityClass) {
        this(new AtomicLong(0), entityClass, Collections.emptyList(), null, 0, 0, -1, false,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(), null, Collections.emptyMap(),
                0, 0, Collections.emptyMap(), new QueryStatsImpl());
    }

    private DynamicQueryBuilder<T> newInstance(
            List<Predicate> predicates,
            String nextLogicalOperator,
            int groupDepth,
            int offset,
            int limit,
            boolean cacheEnabled,
            List<String> selectFields,
            List<String> joinClauses,
            List<String> orderByClauses,
            List<String> groupByClauses,
            List<Predicate> havingPredicates) {
        return new DynamicQueryBuilder<>(paramCounter, entityClass, predicates, nextLogicalOperator,
                groupDepth, offset, limit, cacheEnabled, selectFields, joinClauses,
                orderByClauses, groupByClauses, havingPredicates, nativeSQL, namedParameters,
                fetchSize, timeoutSeconds, queryHints, queryStats);
    }
    
    /**
     * Returns the immutable list of predicates in this builder.
     * Primarily for testing or advanced use cases.
     *
     * @return list of predicates
     */
    public List<Predicate> getPredicates() {
        return Collections.unmodifiableList(predicates);
    }

    /**
     * Returns the immutable list of having predicates in this builder.
     * Primarily for testing or advanced use cases.
     *
     * @return list of having predicates
     */
    public List<Predicate> getHavingPredicates() {
        return Collections.unmodifiableList(havingPredicates);
    }


    // ==================== WHERE CLAUSE METHODS ====================

    @Override
    public QueryBuilder<T> where(@NonNull String fieldName, Object value) {
        return where(fieldName, "=", value);
    }

    @Override
    public QueryBuilder<T> where(@NonNull String fieldName, @NonNull String operator, Object value) {
        validateFieldName(fieldName);
        String normalized = operator.trim().toUpperCase(java.util.Locale.ROOT);
        validateOperator(normalized);

        String paramName = generateParamName(fieldName);
        SimplePredicate newPredicate = new SimplePredicate(fieldName, normalized, value, paramName);

        return addPredicate(newPredicate);
    }

    @Override
    public QueryBuilder<T> whereIn(@NonNull String fieldName, @NonNull List<Object> values) {
        validateFieldName(fieldName);
        if (values.isEmpty()) {
            throw new IllegalArgumentException("values must not be empty");
        }

        String paramName = generateParamName(fieldName);
        InPredicate newPredicate = new InPredicate(fieldName, values, paramName);

        return addPredicate(newPredicate);
    }

    @Override
    public QueryBuilder<T> whereNotIn(@NonNull String fieldName, @NonNull List<Object> values) {
        validateFieldName(fieldName);
        if (values.isEmpty()) {
            throw new IllegalArgumentException("values must not be empty");
        }

        String paramName = generateParamName(fieldName);
        InPredicate inPredicate = new InPredicate(fieldName, values, paramName);
        LogicalPredicate notPredicate = new LogicalPredicate("NOT", Arrays.asList(inPredicate));

        return addPredicate(notPredicate);
    }

    @Override
    public QueryBuilder<T> whereLike(@NonNull String fieldName, @NonNull String pattern) {
        validateFieldName(fieldName);

        String paramName = generateParamName(fieldName);
        LikePredicate newPredicate = new LikePredicate(fieldName, pattern, paramName);

        return addPredicate(newPredicate);
    }

    @Override
    public QueryBuilder<T> whereNotLike(@NonNull String fieldName, @NonNull String pattern) {
        validateFieldName(fieldName);

        String paramName = generateParamName(fieldName);
        LikePredicate likePredicate = new LikePredicate(fieldName, pattern, paramName);
        LogicalPredicate notPredicate = new LogicalPredicate("NOT", Arrays.asList(likePredicate));

        return addPredicate(notPredicate);
    }

    @Override
    public QueryBuilder<T> whereBetween(@NonNull String fieldName, Object startValue, Object endValue) {
        validateFieldName(fieldName);

        String startParamName = generateParamName(fieldName + "_start");
        String endParamName = generateParamName(fieldName + "_end");
        BetweenPredicate newPredicate = new BetweenPredicate(fieldName, startValue, endValue,
                startParamName, endParamName);

        return addPredicate(newPredicate);
    }

    @Override
    public QueryBuilder<T> whereIsNull(@NonNull String fieldName) {
        validateFieldName(fieldName);
        NullPredicate newPredicate = new NullPredicate(fieldName, true);
        return addPredicate(newPredicate);
    }

    @Override
    public QueryBuilder<T> whereIsNotNull(@NonNull String fieldName) {
        validateFieldName(fieldName);
        NullPredicate newPredicate = new NullPredicate(fieldName, false);
        return addPredicate(newPredicate);
    }

    // ==================== LOGICAL OPERATORS ====================

    @Override
    public QueryBuilder<T> and() {
        return withNextLogicalOperator("AND");
    }

    @Override
    public QueryBuilder<T> or() {
        return withNextLogicalOperator("OR");
    }

    @Override
    public QueryBuilder<T> not() {
        return withNextLogicalOperator("NOT");
    }

    @Override
    public QueryBuilder<T> openGroup() {
        return withGroupDepth(groupDepth + 1);
    }

    @Override
    public QueryBuilder<T> closeGroup() {
        if (groupDepth <= 0) {
            throw new IllegalStateException("Cannot close group - no open groups");
        }
        return withGroupDepth(groupDepth - 1);
    }

    // ==================== JOIN METHODS ====================

    @Override
    public QueryBuilder<T> join(@NonNull String associationFieldName) {
        return innerJoin(associationFieldName);
    }

    @Override
    public QueryBuilder<T> leftJoin(@NonNull String associationFieldName) {
        validateFieldName(associationFieldName);
        List<String> newJoins = new ArrayList<>(joinClauses);
        newJoins.add("LEFT JOIN " + associationFieldName);
        return withJoinClauses(Collections.unmodifiableList(newJoins));
    }

    @Override
    public QueryBuilder<T> rightJoin(@NonNull String associationFieldName) {
        validateFieldName(associationFieldName);
        List<String> newJoins = new ArrayList<>(joinClauses);
        newJoins.add("RIGHT JOIN " + associationFieldName);
        return withJoinClauses(Collections.unmodifiableList(newJoins));
    }

    @Override
    public QueryBuilder<T> innerJoin(@NonNull String associationFieldName) {
        validateFieldName(associationFieldName);
        List<String> newJoins = new ArrayList<>(joinClauses);
        newJoins.add("INNER JOIN " + associationFieldName);
        return withJoinClauses(Collections.unmodifiableList(newJoins));
    }

    @Override
    public QueryBuilder<T> fetch(@NonNull String associationFieldName) {
        validateFieldName(associationFieldName);
        List<String> newJoins = new ArrayList<>(joinClauses);
        newJoins.add("LEFT JOIN FETCH " + associationFieldName);
        return withJoinClauses(Collections.unmodifiableList(newJoins));
    }

    // ==================== SELECT METHODS ====================

    @Override
    public QueryBuilder<T> select(@NonNull String... fieldNames) {
        if (fieldNames.length == 0) {
            throw new IllegalArgumentException("fieldNames must not be empty");
        }

        for (String fieldName : fieldNames) {
            validateFieldName(fieldName);
        }

        List<String> newSelectFields = new ArrayList<>(Arrays.asList(fieldNames));
        return withSelectFields(Collections.unmodifiableList(newSelectFields));
    }

    // ==================== AGGREGATION METHODS ====================

    @Override
    public QueryBuilder<T> countAll() {
        List<String> newSelectFields = Arrays.asList("COUNT(*)");
        return withSelectFields(Collections.unmodifiableList(newSelectFields));
    }

    @Override
    public QueryBuilder<T> count(@NonNull String fieldName) {
        validateFieldName(fieldName);
        List<String> newSelectFields = Arrays.asList("COUNT(" + fieldName + ")");
        return withSelectFields(Collections.unmodifiableList(newSelectFields));
    }

    @Override
    public QueryBuilder<T> sum(@NonNull String numericFieldName) {
        validateFieldName(numericFieldName);
        List<String> newSelectFields = Arrays.asList("SUM(" + numericFieldName + ")");
        return withSelectFields(Collections.unmodifiableList(newSelectFields));
    }

    @Override
    public QueryBuilder<T> avg(@NonNull String numericFieldName) {
        validateFieldName(numericFieldName);
        List<String> newSelectFields = Arrays.asList("AVG(" + numericFieldName + ")");
        return withSelectFields(Collections.unmodifiableList(newSelectFields));
    }

    @Override
    public QueryBuilder<T> min(@NonNull String fieldName) {
        validateFieldName(fieldName);
        List<String> newSelectFields = Arrays.asList("MIN(" + fieldName + ")");
        return withSelectFields(Collections.unmodifiableList(newSelectFields));
    }

    @Override
    public QueryBuilder<T> max(@NonNull String fieldName) {
        validateFieldName(fieldName);
        List<String> newSelectFields = Arrays.asList("MAX(" + fieldName + ")");
        return withSelectFields(Collections.unmodifiableList(newSelectFields));
    }

    // ==================== GROUP BY AND HAVING ====================

    @Override
    public QueryBuilder<T> groupBy(@NonNull String... fieldNames) {
        if (fieldNames.length == 0) {
            throw new IllegalArgumentException("fieldNames must not be empty");
        }

        for (String fieldName : fieldNames) {
            validateFieldName(fieldName);
        }

        List<String> newGroupBy = new ArrayList<>(Arrays.asList(fieldNames));
        return withGroupByClauses(Collections.unmodifiableList(newGroupBy));
    }

    @Override
    public QueryBuilder<T> having(@NonNull String aggregatedFieldName, @NonNull String operator, Object value) {
        validateAggregatedFieldName(aggregatedFieldName);
        validateOperator(operator);

        String paramName = generateParamName(aggregatedFieldName + "_having");
        HavingPredicate havingPredicate = new HavingPredicate(aggregatedFieldName, operator, value, paramName);

        List<Predicate> newHavingPredicates = new ArrayList<>(havingPredicates);
        newHavingPredicates.add(havingPredicate);

        return withHavingPredicates(Collections.unmodifiableList(newHavingPredicates));
    }

    // ==================== ORDER BY METHODS ====================

    @Override
    public QueryBuilder<T> orderBy(@NonNull String fieldName) {
        return orderBy(fieldName, true);
    }

    @Override
    public QueryBuilder<T> orderByDescending(@NonNull String fieldName) {
        return orderBy(fieldName, false);
    }

    @Override
    public QueryBuilder<T> orderBy(@NonNull String fieldName, boolean ascending) {
        validateFieldName(fieldName);

        List<String> newOrderBy = new ArrayList<>(orderByClauses);
        newOrderBy.add(fieldName + (ascending ? " ASC" : " DESC"));

        return withOrderByClauses(Collections.unmodifiableList(newOrderBy));
    }

    // ==================== PAGINATION ====================

    @Override
    public QueryBuilder<T> page(int pageNumber, int pageSize) {
        if (pageNumber < 1) {
            throw new IllegalArgumentException("pageNumber must be >= 1");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize must be >= 1");
        }
        int newOffset = Math.multiplyExact(pageNumber - 1, pageSize);
        return withOffset(newOffset).withLimit(pageSize);
    }

    @Override
    public QueryBuilder<T> limit(int maxResults) {
        if (maxResults <= 0) {
            throw new IllegalArgumentException("limit must be positive");
        }
        return withLimit(maxResults);
    }

    @Override
    public QueryBuilder<T> offset(int skipCount) {
        if (skipCount < 0) {
            throw new IllegalArgumentException("offset must be non-negative");
        }
        return withOffset(skipCount);
    }

    // ==================== SUBQUERIES (STUBS FOR NOW) ====================

    @Override
    public QueryBuilder<T> exists(QueryBuilder<?> subquery) {
        Objects.requireNonNull(subquery, "subquery must not be null");
        SubqueryPredicate existsPredicate = new SubqueryPredicate("EXISTS", subquery);
        return addPredicate(existsPredicate);
    }

    @Override
    public QueryBuilder<T> notExists(QueryBuilder<?> subquery) {
        Objects.requireNonNull(subquery, "subquery must not be null");
        SubqueryPredicate notExistsPredicate = new SubqueryPredicate("NOT EXISTS", subquery);
        return addPredicate(notExistsPredicate);
    }

    @Override
    public QueryBuilder<T> in(@NonNull String fieldName, QueryBuilder<?> subquery) {
        validateFieldName(fieldName);
        Objects.requireNonNull(subquery, "subquery must not be null");
        SubqueryInPredicate inPredicate = new SubqueryInPredicate(fieldName, "IN", subquery);
        return addPredicate(inPredicate);
    }

    @Override
    public QueryBuilder<T> notIn(@NonNull String fieldName, QueryBuilder<?> subquery) {
        validateFieldName(fieldName);
        Objects.requireNonNull(subquery, "subquery must not be null");
        SubqueryInPredicate notInPredicate = new SubqueryInPredicate(fieldName, "NOT IN", subquery);
        return addPredicate(notInPredicate);
    }

    // ==================== ADVANCED FEATURES (STUBS FOR NOW) ====================

    @Override
    public QueryBuilder<T> customFunction(@NonNull String functionName, @NonNull String fieldName,
            Object... parameters) {
        validateFieldName(fieldName);
        String paramPrefix = generateParamName("func");
        CustomFunctionPredicate functionPredicate = new CustomFunctionPredicate(
            functionName, fieldName, parameters, paramPrefix);
        return addPredicate(functionPredicate);
    }

    @Override
    public QueryBuilder<T> nativeQuery(@NonNull String sqlQuery) {
        if (sqlQuery.trim().isEmpty()) {
            throw new IllegalArgumentException("sqlQuery must not be empty");
        }
        return withNativeSQL(sqlQuery.trim());
    }

    @Override
    public QueryBuilder<T> parameter(@NonNull String parameterName, Object parameterValue) {
        if (parameterName.trim().isEmpty()) {
            throw new IllegalArgumentException("parameterName must not be empty");
        }
        
        Map<String, Object> newParams = new HashMap<>(namedParameters);
        newParams.put(parameterName.trim(), parameterValue);
        
        return withNamedParameters(Collections.unmodifiableMap(newParams));
    }

    @Override
    public QueryBuilder<T> parameters(@NonNull Map<String, Object> parameterMap) {
        if (parameterMap.isEmpty()) {
            return this; // No change needed
        }
        
        Map<String, Object> newParams = new HashMap<>(namedParameters);
        newParams.putAll(parameterMap);
        
        return withNamedParameters(Collections.unmodifiableMap(newParams));
    }

    // ==================== CACHING ====================

    @Override
    public QueryBuilder<T> cached() {
        return withCacheEnabled(true);
    }

    @Override
    public QueryBuilder<T> cached(String cacheRegionName) {
        // TODO: Implement cache region support
        return withCacheEnabled(true);
    }

    @Override
    public QueryBuilder<T> cached(long timeToLiveSeconds) {
        // TODO: Implement TTL support
        return withCacheEnabled(true);
    }

    // ==================== HINTS AND PERFORMANCE (STUBS FOR NOW)
    // ====================

    @Override
    public QueryBuilder<T> hint(@NonNull String hintName, Object hintValue) {
        if (hintName.trim().isEmpty()) {
            throw new IllegalArgumentException("hintName must not be empty");
        }
        
        Map<String, Object> newHints = new HashMap<>(queryHints);
        newHints.put(hintName.trim(), hintValue);
        
        return withQueryHints(Collections.unmodifiableMap(newHints));
    }

    @Override
    public QueryBuilder<T> fetchSize(int fetchSize) {
        if (fetchSize <= 0) {
            throw new IllegalArgumentException("fetchSize must be positive");
        }
        return withFetchSize(fetchSize);
    }

    @Override
    public QueryBuilder<T> timeout(int timeoutSeconds) {
        if (timeoutSeconds <= 0) {
            throw new IllegalArgumentException("timeoutSeconds must be positive");
        }
        return withTimeoutSeconds(timeoutSeconds);
    }

    @Override
    public QueryStats getExecutionStats() {
        return queryStats.sql(toSQL()).hints(getQueryHints());
    }

    // ==================== EXECUTION METHODS ====================

    @Override
    public long count() {
        // Implementation stub: real query execution logic would go here
        return 0L;
    }

    @Override
    public List<T> findAll() {
        return Collections.emptyList();
    }

    @Override
    public T findOne() {
        return null;
    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public CompletableFuture<List<T>> findAllAsync() {
        return CompletableFuture.completedFuture(findAll());
    }

    @Override
    public CompletableFuture<T> findOneAsync() {
        return CompletableFuture.completedFuture(findOne());
    }

    @Override
    public CompletableFuture<Long> countAsync() {
        return CompletableFuture.completedFuture(count());
    }

    @Override
    public Page<T> findPage() {
        return PageImpl.empty();
    }

    @Override
    public DynamicQuery<T> build() {
        return new DynamicQueryImpl<>(findAll(), toSQL());
    }

    /**
     * Returns the immutable map of query hints for this builder.
     *
     * @return map of hint names to values
     */
    public Map<String, Object> getQueryHints() {
        return Collections.unmodifiableMap(queryHints);
    }

    /**
     * Returns the immutable map of all parameters from predicates in this builder.
     * Used for parameter binding when executing queries.
     *
     * @return map of parameter names to values
     */
    public Map<String, Object> getParameters() {
        Map<String, Object> allParameters = new HashMap<>();
        
        // Include named parameters for native queries
        allParameters.putAll(namedParameters);
        
        // Collect parameters from WHERE predicates
        for (Predicate predicate : predicates) {
            allParameters.putAll(predicate.getParameters());
        }
        
        // Collect parameters from HAVING predicates
        for (Predicate havingPredicate : havingPredicates) {
            allParameters.putAll(havingPredicate.getParameters());
        }
        
        return Collections.unmodifiableMap(allParameters);
    }

    // ==================== SQL GENERATION ====================

    @Override
    public String toSQL() {
        // If native SQL is specified, return it directly
        if (nativeSQL != null && !nativeSQL.trim().isEmpty()) {
            return nativeSQL;
        }
        
        StringBuilder sb = new StringBuilder();

        // SELECT clause
        sb.append("SELECT ");
        if (selectFields.isEmpty()) {
            sb.append("*");
        } else {
            sb.append(String.join(", ", selectFields));
        }

        // FROM clause
        sb.append(" FROM ").append(entityClass.getSimpleName());

        // JOIN clauses
        if (!joinClauses.isEmpty()) {
            sb.append(" ").append(String.join(" ", joinClauses));
        }

        // WHERE clause
        if (!predicates.isEmpty()) {
            sb.append(" WHERE ");
            sb.append(predicates.stream()
                    .map(Predicate::toSQL)
                    .collect(Collectors.joining(" AND ")));
        }

        // GROUP BY clause
        if (!groupByClauses.isEmpty()) {
            sb.append(" GROUP BY ").append(String.join(", ", groupByClauses));
        }

        // HAVING clause
        if (!havingPredicates.isEmpty()) {
            sb.append(" HAVING ");
            sb.append(havingPredicates.stream()
                    .map(Predicate::toSQL)
                    .collect(Collectors.joining(" AND ")));
        }

        // ORDER BY clause
        if (!orderByClauses.isEmpty()) {
            sb.append(" ORDER BY ").append(String.join(", ", orderByClauses));
        }

        // LIMIT clause
        if (limit > 0) {
            sb.append(" LIMIT ").append(limit);
        }

        // OFFSET clause
        if (offset > 0) {
            sb.append(" OFFSET ").append(offset);
        }

        return sb.toString();
    }

    // ==================== HELPER METHODS ====================

    private QueryBuilder<T> addPredicate(Predicate newPredicate) {
        List<Predicate> newPredicates = new ArrayList<>(predicates);

        if (predicates.isEmpty() || nextLogicalOperator == null) {
            newPredicates.add(newPredicate);
        } else {
            // Combine with logical operator
            Predicate lastPredicate = newPredicates.remove(newPredicates.size() - 1);
            LogicalPredicate combined = new LogicalPredicate(nextLogicalOperator,
                    Arrays.asList(lastPredicate, newPredicate));
            newPredicates.add(combined);
        }

        return newInstance(Collections.unmodifiableList(newPredicates), null, groupDepth,
                offset, limit, cacheEnabled, selectFields, joinClauses,
                orderByClauses, groupByClauses, havingPredicates);
    }

    private void validateFieldName(String fieldName) {
        try {
            // Use centralized field validation
            FieldValidator.validateFieldName(fieldName);
        } catch (QueryBuildException e) {
            // Convert to IllegalArgumentException for backward compatibility in builder layer
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private void validateAggregatedFieldName(String aggregatedFieldName) {
        try {
            // Use centralized aggregated field validation for HAVING clauses
            FieldValidator.validateAggregatedFieldName(aggregatedFieldName);
        } catch (QueryBuildException e) {
            // Convert to IllegalArgumentException for backward compatibility in builder layer
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private void validateOperator(String operator) {
        try {
            // Use centralized operator validation
            OperatorValidator.validateOperator(operator);
        } catch (QueryBuildException e) {
            // Convert to IllegalArgumentException for backward compatibility in builder layer
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private String generateParamName(String baseName) {
        // Generate simple parameter names like p1, p2, p3, etc.
        // This ensures compatibility with tests and provides clean SQL output
        return "p" + paramCounter.incrementAndGet();
    }
}
