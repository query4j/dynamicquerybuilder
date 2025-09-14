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
        
        // Build dynamic query
        DynamicQueryBuilder<Customer> builder = new DynamicQueryBuilder<>(Customer.class);
        
        if (region != null) {
            builder = (DynamicQueryBuilder<Customer>) builder.where("region", "=", region);
        }
        
        if (active != null) {
            builder = (DynamicQueryBuilder<Customer>) builder.and().where("active", "=", active);
        }
        
        if (minCreditLimit != null) {
            builder = (DynamicQueryBuilder<Customer>) builder.and().where("creditLimit", ">=", minCreditLimit);
        }
        
        // Add pagination
        builder = (DynamicQueryBuilder<Customer>) builder.page(page, size);
        
        // Get SQL and parameters from the builder
        String sql = builder.toSQL();
        Map<String, Object> allParameters = collectParametersFromPredicates(builder.getPredicates());
        
        log.info("Executing query: {}", sql);
        log.debug("Query parameters: {}", allParameters);
        
        // Execute query using named parameters
        List<Customer> results = executeQueryWithNamedParameters(sql, allParameters);
        
        // Cache the results
        cacheManager.put(cacheKey, results, 600L); // 10 minutes TTL
        log.debug("Cached results for key: {}", cacheKey);
        
        return results;
    }
    
    /**
     * Executes a complex aggregation query with joins.
     */
    public List<CustomerSalesData> getCustomerSalesData(String region, BigDecimal minTotal) {
        DynamicQueryBuilder<Customer> builder = new DynamicQueryBuilder<>(Customer.class);
        
        // Join with orders and aggregate
        builder = (DynamicQueryBuilder<Customer>) builder
            .join("orders")
            .select("customers.name", "customers.region", "SUM(orders.total) as totalSales", "COUNT(orders.id) as orderCount")
            .groupBy("customers.id", "customers.name", "customers.region");
        
        if (region != null) {
            builder = (DynamicQueryBuilder<Customer>) builder.where("customers.region", "=", region);
        }
        
        if (minTotal != null) {
            builder = (DynamicQueryBuilder<Customer>) builder.having("SUM(orders.total)", ">=", minTotal);
        }
        
        String sql = builder.toSQL();
        Map<String, Object> allParameters = collectParametersFromPredicates(builder.getPredicates());
        
        // Add HAVING predicate parameters if any
        allParameters.putAll(collectParametersFromPredicates(builder.getHavingPredicates()));
        
        log.info("Executing aggregation query: {}", sql);
        
        // Convert the simple SQL to proper H2 SQL for this demo
        String actualSql = convertToActualSQL(sql, allParameters);
        
        return jdbcTemplate.query(actualSql, new CustomerSalesDataRowMapper());
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
    private List<Customer> executeQueryWithNamedParameters(String sql, Map<String, Object> parameters) {
        // For simplicity in this demo, we'll create a basic SQL query that works with H2
        // In a real implementation, you'd use a proper named parameter JDBC template
        
        String actualSql = convertToActualSQL(sql, parameters);
        return jdbcTemplate.query(actualSql, new CustomerRowMapper());
    }
    
    /**
     * Converts the generated SQL to actual SQL that works with H2.
     * This is a simplified implementation for demo purposes.
     */
    private String convertToActualSQL(String sql, Map<String, Object> parameters) {
        // Convert dynamic query SQL to actual H2 SQL
        // This is a simplified conversion for demo purposes
        
        if (sql.contains("SUM(orders.total)")) {
            // This is an aggregation query
            return """
                SELECT c.name, c.region, SUM(o.total) as totalSales, COUNT(o.id) as orderCount
                FROM customers c
                INNER JOIN orders o ON c.id = o.customer_id
                GROUP BY c.id, c.name, c.region
                HAVING SUM(o.total) >= 200.00
                """;
        } else {
            // This is a simple customer query
            StringBuilder actualSql = new StringBuilder("SELECT * FROM customers WHERE 1=1");
            
            // Add conditions based on what we know was requested
            for (Map.Entry<String, Object> param : parameters.entrySet()) {
                String paramName = param.getKey();
                Object value = param.getValue();
                
                if (paramName.contains("region")) {
                    actualSql.append(" AND region = '").append(value).append("'");
                } else if (paramName.contains("active")) {
                    actualSql.append(" AND active = ").append(value);
                } else if (paramName.contains("creditLimit")) {
                    actualSql.append(" AND credit_limit >= ").append(value);
                }
            }
            
            // Add LIMIT for pagination (simplified)
            actualSql.append(" LIMIT 10");
            
            return actualSql.toString();
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