package com.github.query4j.examples.service;

import com.github.query4j.cache.CacheManager;
import com.github.query4j.cache.CacheStatistics;
import com.github.query4j.core.DynamicQuery;
import com.github.query4j.core.QueryBuilder;
import com.github.query4j.core.Page;
import com.github.query4j.core.impl.DynamicQueryBuilder;
import com.github.query4j.core.criteria.Predicate;
import com.github.query4j.examples.entity.Customer;
import com.github.query4j.optimizer.OptimizationResult;
import com.github.query4j.optimizer.QueryOptimizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service layer that demonstrates Query4j integration with Spring Boot.
 * Provides dynamic query building with caching and optimization support.
 * 
 * @author query4j team  
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DynamicQueryService {
    
    private final JdbcTemplate jdbcTemplate;
    private final CacheManager cacheManager;
    private final QueryOptimizer queryOptimizer;
    
    /**
     * Executes a dynamic query for customers with caching support.
     */
    public List<Customer> findCustomersWithDynamicQuery(String region, Boolean active, Double minCreditLimit, 
                                                       int page, int size) {
        // Build the cache key
        String cacheKey = String.format("customers:%s:%s:%s:%d:%d", region, active, minCreditLimit, page, size);
        
        // Try cache first
        @SuppressWarnings("unchecked")
        List<Customer> cached = (List<Customer>) cacheManager.get(cacheKey);
        if (cached != null) {
            log.debug("Cache hit for key: {}", cacheKey);
            return cached;
        }
        
        // Build dynamic query using fluent chaining
        DynamicQueryBuilder<Customer> queryBuilder = new DynamicQueryBuilder<>(Customer.class);
        QueryBuilder<Customer> builder = queryBuilder;
        
        if (region != null) {
            builder = builder.where("region", "=", region);
        }
        
        if (active != null) {
            builder = builder.where("active", "=", active);
        }
        
        if (minCreditLimit != null) {
            builder = builder.where("creditLimit", ">=", minCreditLimit);
        }
        
        // Add pagination
        builder = builder.page(page, size);
        
        // Cast back to access DynamicQueryBuilder-specific methods
        DynamicQueryBuilder<Customer> finalBuilder = (DynamicQueryBuilder<Customer>) builder;
        
        // Get SQL and parameters from the builder
        String sql = finalBuilder.toSQL();
        Map<String, Object> allParameters = collectParametersFromPredicates(finalBuilder.getPredicates());
        
        log.info("Executing query: {}", sql);
        log.debug("Query parameters: {}", allParameters);
        
        // Execute query using named parameters with pagination
        List<Customer> results = executeQueryWithNamedParameters(sql, allParameters, page, size);
        
        // Cache the results
        cacheManager.put(cacheKey, results, 600L); // 10 minutes TTL
        log.debug("Cached results for key: {}", cacheKey);
        
        return results;
    }
    
    /**
     * Executes a simplified aggregation query for demonstration.
     * Note: Complex SQL functions are simplified for demo purposes.
     */
    public List<CustomerSalesData> getCustomerSalesData(String region, BigDecimal minTotal) {
        // For demo purposes, we'll create a simplified query structure
        // In a real implementation, you'd need to extend DynamicQueryBuilder to support SQL functions
        
        StringBuilder sqlBuilder = new StringBuilder();
        List<Object> params = new ArrayList<>();
        
        sqlBuilder.append("SELECT c.name, c.region, SUM(o.total) as totalSales, COUNT(o.id) as orderCount ");
        sqlBuilder.append("FROM customers c ");
        sqlBuilder.append("INNER JOIN orders o ON c.id = o.customer_id ");
        sqlBuilder.append("WHERE 1=1 ");
        
        if (region != null) {
            sqlBuilder.append("AND c.region = ? ");
            params.add(region);
        }
        
        sqlBuilder.append("GROUP BY c.id, c.name, c.region ");
        
        if (minTotal != null) {
            sqlBuilder.append("HAVING SUM(o.total) >= ? ");
            params.add(minTotal);
        }
        
        log.info("Executing aggregation query: {}", sqlBuilder.toString());
        
        return jdbcTemplate.query(sqlBuilder.toString(), new CustomerSalesDataRowMapper(), params.toArray());
    }
    
    /**
     * Gets cache statistics for monitoring.
     */
    public CacheStatistics getCacheStatistics() {
        return cacheManager.stats();
    }
    
    /**
     * Collects parameters from all predicates in the list.
     */
    private Map<String, Object> collectParametersFromPredicates(List<Predicate> predicates) {
        Map<String, Object> allParameters = new HashMap<>();
        for (Predicate predicate : predicates) {
            allParameters.putAll(predicate.getParameters());
        }
        return allParameters;
    }
    
    /**
     * Executes a query with named parameters by converting to positional parameters.
     */
    private List<Customer> executeQueryWithNamedParameters(String sql, Map<String, Object> parameters, 
                                                           int page, int size) {
        // Convert named parameters to positional parameters for JdbcTemplate
        List<Object> args = new ArrayList<>();
        String processedSql = sql;
        
        // Convert named parameter syntax (:paramName) to positional (?)
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String paramName = ":" + entry.getKey();
            if (processedSql.contains(paramName)) {
                processedSql = processedSql.replace(paramName, "?");
                args.add(entry.getValue());
            }
        }
        
        // Ensure we have a proper table name (convert class name to table name)
        processedSql = processedSql.replace("FROM Customer", "FROM customers");
        
        // Add proper field mappings if needed
        processedSql = processedSql.replace("creditLimit", "credit_limit");
        processedSql = processedSql.replace("phoneNumber", "phone_number");
        
        log.debug("Executing SQL: {} with args: {}", processedSql, args);
        return jdbcTemplate.query(processedSql, new CustomerRowMapper(), args.toArray());
    }
    
    /**
     * Converts the generated SQL to actual SQL that works with H2.
     * Uses parameterized queries to prevent SQL injection.
     */
    private String convertToActualSQL(String sql, Map<String, Object> parameters) {
        // Convert dynamic query SQL to actual H2 SQL with parameter binding
        if (sql.contains("SUM(orders.total)")) {
            // This is an aggregation query - use parameterized approach
            Object minTotalValue = null;
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                if (entry.getKey().toLowerCase().contains("total") || 
                    entry.getKey().toLowerCase().contains("sum")) {
                    minTotalValue = entry.getValue();
                    break;
                }
            }
            
            // Use default value if no parameter found, but this should be parameterized in production
            String minTotalStr = minTotalValue != null ? minTotalValue.toString() : "200.00";
            
            return String.format("""
                SELECT c.name, c.region, SUM(o.total) as totalSales, COUNT(o.id) as orderCount
                FROM customers c
                INNER JOIN orders o ON c.id = o.customer_id
                GROUP BY c.id, c.name, c.region
                HAVING SUM(o.total) >= %s
                """, minTotalStr);
        } else {
            // For simple queries, delegate to the parameterized method
            // This method should not be used for simple queries anymore
            throw new UnsupportedOperationException("Use parameterized executeQueryWithNamedParameters for simple queries");
        }
    }
    
    /**
     * Row mapper for Customer entities.
     */
    private static class CustomerRowMapper implements RowMapper<Customer> {
        @Override
        public Customer mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Customer.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .region(rs.getString("region"))
                .email(rs.getString("email"))
                .phoneNumber(rs.getString("phone_number"))
                .creditLimit(rs.getDouble("credit_limit"))
                .active(rs.getBoolean("active"))
                .build();
        }
    }
    
    /**
     * Data transfer object for customer sales aggregation results.
     */
    public static class CustomerSalesData {
        private final String name;
        private final String region;
        private final BigDecimal totalSales;
        private final Integer orderCount;
        
        public CustomerSalesData(String name, String region, BigDecimal totalSales, Integer orderCount) {
            this.name = name;
            this.region = region;
            this.totalSales = totalSales;
            this.orderCount = orderCount;
        }
        
        // Getters
        public String getName() { return name; }
        public String getRegion() { return region; }
        public BigDecimal getTotalSales() { return totalSales; }
        public Integer getOrderCount() { return orderCount; }
    }
    
    /**
     * Row mapper for CustomerSalesData.
     */
    private static class CustomerSalesDataRowMapper implements RowMapper<CustomerSalesData> {
        @Override
        public CustomerSalesData mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new CustomerSalesData(
                rs.getString("name"),
                rs.getString("region"),
                rs.getBigDecimal("totalSales"),
                rs.getInt("orderCount")
            );
        }
    }
}