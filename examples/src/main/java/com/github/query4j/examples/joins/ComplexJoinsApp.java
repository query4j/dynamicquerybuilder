package com.github.query4j.examples.joins;

import com.github.query4j.core.QueryBuilder;
import com.github.query4j.core.Page;
import com.github.query4j.examples.model.User;
import com.github.query4j.examples.model.Order;
import com.github.query4j.examples.model.OrderItem;
import com.github.query4j.examples.model.Product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Example application demonstrating complex multi-table joins and advanced querying
 * using Query4j dynamic query builder.
 * 
 * Features demonstrated:
 * - Multi-table joins with complex relationships
 * - Dynamic query building with nested conditions
 * - Subqueries and correlated subqueries
 * - Aggregations across joined tables
 * - Hierarchical data mapping from joins
 * - Advanced filtering with joined table conditions
 * - Performance optimization for complex queries
 * - RESTful API patterns for dynamic filtering
 * 
 * This example shows realistic e-commerce scenarios:
 * - Customer order analysis with product details
 * - Sales reporting across multiple dimensions
 * - Inventory management with cross-table analytics
 * - Dynamic search and filtering interfaces
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
public class ComplexJoinsApp {
    
    private static final Logger logger = Logger.getLogger(ComplexJoinsApp.class.getName());
    
    /**
     * Main entry point for complex joins application.
     */
    public static void main(String[] args) {
        ComplexJoinsApp app = new ComplexJoinsApp();
        
        try {
            logger.info("Starting complex joins application...");
            
            // Demonstrate various complex join patterns
            app.customerOrderAnalysis();
            app.productSalesReporting();
            app.hierarchicalDataMapping();
            app.dynamicFilteringAPI();
            app.advancedAggregationQueries();
            app.correlatedSubqueryExamples();
            
            logger.info("Complex joins application completed successfully");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Complex joins application failed", e);
            System.exit(1);
        }
    }
    
    /**
     * Demonstrates complex customer order analysis with multiple joins.
     */
    public void customerOrderAnalysis() {
        logger.info("=== Customer Order Analysis ===");
        
        try {
            // Complex query: Find high-value customers with recent large orders
            List<Order> highValueOrders = QueryBuilder.forEntity(Order.class)
                .join("customer")                    // Join with User (customer) table
                .leftJoin("orderItems")             // Left join with OrderItem table
                .join("orderItems.product")         // Join with Product table through OrderItem
                .where("customer.active", true)    // Customer must be active
                .and()
                .where("customer.vipStatus", true) // Customer must be VIP
                .and()
                .where("totalAmount", ">", new BigDecimal("1000.00")) // High-value orders
                .and()
                .where("orderDate", ">=", LocalDate.now().minusDays(90)) // Recent orders
                .and()
                .where("orderItems.product.category", "Electronics") // Electronics category
                .and()
                .whereIsNotNull("orderItems.product.price") // Product must have price
                .orderByDescending("totalAmount")
                .orderBy("orderDate")
                .limit(50)
                .findAll();
            
            logger.info("Found " + highValueOrders.size() + " high-value electronics orders from VIP customers");
            
            // Additional analysis: Customer spending patterns
            analyzeCustomerSpending(highValueOrders);
            
            // Query with multiple aggregations across joins
            List<Order> customerSummary = QueryBuilder.forEntity(Order.class)
                .select("customer.id", "customer.firstName", "customer.lastName")
                .sum("totalAmount")
                .count("id")
                .join("customer")
                .where("customer.active", true)
                .and()
                .where("orderDate", ">=", LocalDate.now().minusYears(1))
                .groupBy("customer.id", "customer.firstName", "customer.lastName")
                .having("SUM(totalAmount)", ">", new BigDecimal("5000.00"))
                .orderByDescending("SUM(totalAmount)")
                .limit(25)
                .findAll();
            
            logger.info("Found " + customerSummary.size() + " customers with >$5000 annual spending");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to perform customer order analysis", e);
            throw new RuntimeException("Customer analysis failed", e);
        }
    }
    
    /**
     * Demonstrates product sales reporting with complex joins and aggregations.
     */
    public void productSalesReporting() {
        logger.info("=== Product Sales Reporting ===");
        
        try {
            // Complex multi-join query for product performance analysis
            List<Product> productPerformance = QueryBuilder.forEntity(Product.class)
                .select("id", "name", "category", "price")
                .count("orderItems.id")         // Count order items
                .sum("orderItems.quantity")     // Sum quantities sold
                .avg("orderItems.unitPrice")    // Average selling price
                .leftJoin("orderItems")         // Left join to include products with no sales
                .leftJoin("orderItems.order")   // Join with orders through order items
                .leftJoin("orderItems.order.customer") // Join with customers
                .where("active", true)          // Only active products
                .and()
                .openGroup()                    // Group for date conditions
                    .whereIsNull("orderItems.order.orderDate") // Products with no orders
                    .or()
                    .where("orderItems.order.orderDate", ">=", LocalDate.now().minusDays(180))
                .closeGroup()
                .groupBy("id", "name", "category", "price")
                .orderByDescending("COUNT(orderItems.id)")
                .orderByDescending("SUM(orderItems.quantity)")
                .limit(100)
                .findAll();
            
            logger.info("Generated performance report for " + productPerformance.size() + " products");
            
            // Category-wise sales analysis
            List<Product> categorySales = QueryBuilder.forEntity(Product.class)
                .select("category")
                .sum("orderItems.quantity")
                .sum("orderItems.unitPrice * orderItems.quantity") // Total revenue calculation
                .count("DISTINCT orderItems.order.customer.id")     // Unique customers
                .leftJoin("orderItems")
                .leftJoin("orderItems.order")
                .leftJoin("orderItems.order.customer")
                .where("orderItems.order.orderDate", ">=", LocalDate.now().minusDays(365))
                .and()
                .where("orderItems.order.status", "COMPLETED")
                .groupBy("category")
                .having("SUM(orderItems.quantity)", ">", 0)
                .orderByDescending("SUM(orderItems.quantity)")
                .findAll();
            
            logger.info("Category analysis completed for " + categorySales.size() + " categories");
            
            // Top-selling products with customer demographics
            analyzeBestSellersWithDemographics();
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to generate product sales report", e);
            throw new RuntimeException("Product sales reporting failed", e);
        }
    }
    
    /**
     * Demonstrates hierarchical data mapping from complex joins.
     */
    public void hierarchicalDataMapping() {
        logger.info("=== Hierarchical Data Mapping ===");
        
        try {
            // Query that returns hierarchical customer data with orders and items
            List<User> customersWithOrderDetails = QueryBuilder.forEntity(User.class)
                .leftJoin("orders")                 // Customer -> Orders
                .leftJoin("orders.orderItems")      // Orders -> OrderItems  
                .leftJoin("orders.orderItems.product") // OrderItems -> Products
                .fetch("orders")                    // Eager fetch to avoid N+1
                .fetch("orders.orderItems")         // Eager fetch order items
                .fetch("orders.orderItems.product") // Eager fetch products
                .where("active", true)
                .and()
                .whereIsNotNull("orders.id")        // Only customers with orders
                .and()
                .where("orders.orderDate", ">=", LocalDate.now().minusDays(30))
                .orderBy("lastName")
                .orderBy("firstName")
                .limit(20)
                .findAll();
            
            logger.info("Retrieved hierarchical data for " + customersWithOrderDetails.size() + " customers");
            
            // Process and map hierarchical data
            mapHierarchicalData(customersWithOrderDetails);
            
            // Advanced join with conditional fetching
            List<Order> detailedOrders = QueryBuilder.forEntity(Order.class)
                .join("customer")
                .leftJoin("orderItems")
                .leftJoin("orderItems.product")
                .fetch("customer")
                .fetch("orderItems")
                .fetch("orderItems.product")
                .where("status", "SHIPPED")
                .and()
                .where("customer.country", "USA")
                .and()
                .where("orderItems.product.category", "IN", 
                       Arrays.asList("Electronics", "Books", "Clothing"))
                .orderByDescending("orderDate")
                .limit(50)
                .findAll();
            
            logger.info("Retrieved detailed order data for " + detailedOrders.size() + " shipped orders");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to map hierarchical data", e);
            throw new RuntimeException("Hierarchical data mapping failed", e);
        }
    }
    
    /**
     * Demonstrates dynamic filtering API patterns with complex joins.
     */
    public void dynamicFilteringAPI() {
        logger.info("=== Dynamic Filtering API ===");
        
        try {
            // Simulate API request parameters
            Map<String, Object> filters = new HashMap<>();
            filters.put("customerCountry", "USA");
            filters.put("productCategory", "Electronics");
            filters.put("minOrderAmount", new BigDecimal("100.00"));
            filters.put("maxOrderAmount", new BigDecimal("2000.00"));
            filters.put("orderStatus", Arrays.asList("SHIPPED", "DELIVERED"));
            filters.put("orderDateFrom", LocalDate.now().minusDays(60));
            filters.put("customerVip", true);
            
            // Build dynamic query based on filters
            QueryBuilder<Order> dynamicQuery = buildDynamicOrderQuery(filters);
            
            // Execute query with pagination
            Page<Order> results = dynamicQuery
                .page(0, 25)
                .findPage();
            
            logger.info("Dynamic query returned " + results.getContent().size() + " orders");
            logger.info("Total matching orders: " + results.getTotalElements());
            logger.info("Total pages: " + results.getTotalPages());
            
            // Demonstrate different filter combinations
            testDifferentFilterCombinations();
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to execute dynamic filtering", e);
            throw new RuntimeException("Dynamic filtering failed", e);
        }
    }
    
    /**
     * Demonstrates advanced aggregation queries across multiple tables.
     */
    public void advancedAggregationQueries() {
        logger.info("=== Advanced Aggregation Queries ===");
        
        try {
            // Monthly sales aggregation across multiple tables
            List<Order> monthlySales = QueryBuilder.forEntity(Order.class)
                .select("YEAR(orderDate)", "MONTH(orderDate)")
                .count("id")
                .sum("totalAmount")
                .avg("totalAmount")
                .min("totalAmount")
                .max("totalAmount")
                .join("customer")
                .leftJoin("orderItems")
                .where("status", "COMPLETED")
                .and()
                .where("orderDate", ">=", LocalDate.now().minusYears(2))
                .groupBy("YEAR(orderDate)", "MONTH(orderDate)")
                .orderByDescending("YEAR(orderDate)")
                .orderByDescending("MONTH(orderDate)")
                .findAll();
            
            logger.info("Generated monthly sales report for " + monthlySales.size() + " months");
            
            // Customer segmentation with aggregated metrics
            List<User> customerSegmentation = QueryBuilder.forEntity(User.class)
                .select("region", "department")
                .count("id")
                .count("orders.id")
                .sum("orders.totalAmount")
                .avg("orders.totalAmount")
                .leftJoin("orders")
                .where("active", true)
                .and()
                .whereIsNotNull("region")
                .and()
                .openGroup()
                    .whereIsNull("orders.orderDate")
                    .or()
                    .where("orders.orderDate", ">=", LocalDate.now().minusYears(1))
                .closeGroup()
                .groupBy("region", "department")
                .having("COUNT(id)", ">", 5)
                .orderBy("region")
                .orderByDescending("SUM(orders.totalAmount)")
                .findAll();
            
            logger.info("Customer segmentation completed for " + customerSegmentation.size() + " segments");
            
            // Product performance across customer segments
            analyzeProductPerformanceBySegment();
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to execute advanced aggregation queries", e);
            throw new RuntimeException("Advanced aggregation failed", e);
        }
    }
    
    /**
     * Demonstrates correlated subqueries and EXISTS clauses.
     */
    public void correlatedSubqueryExamples() {
        logger.info("=== Correlated Subquery Examples ===");
        
        try {
            // Find customers who have placed orders above their average order value
            List<User> customersAboveAverage = QueryBuilder.forEntity(User.class)
                .exists(
                    QueryBuilder.forEntity(Order.class)
                        .where("customerId", "users.id")
                        .and()
                        .where("totalAmount", ">", 
                            QueryBuilder.forEntity(Order.class)
                                .select("AVG(totalAmount)")
                                .where("customerId", "users.id")
                        )
                )
                .where("active", true)
                .orderBy("lastName")
                .findAll();
            
            logger.info("Found " + customersAboveAverage.size() + " customers with above-average orders");
            
            // Find products that haven't been ordered in the last 6 months
            List<Product> staleProducts = QueryBuilder.forEntity(Product.class)
                .notExists(
                    QueryBuilder.forEntity(OrderItem.class)
                        .join("order")
                        .where("product.id", "products.id")
                        .and()
                        .where("order.orderDate", ">=", LocalDate.now().minusMonths(6))
                )
                .where("active", true)
                .orderBy("name")
                .findAll();
            
            logger.info("Found " + staleProducts.size() + " products not ordered in 6 months");
            
            // Find customers with orders in multiple categories
            List<User> diverseCustomers = QueryBuilder.forEntity(User.class)
                .where("id", "IN",
                    QueryBuilder.forEntity(OrderItem.class)
                        .select("order.customer.id")
                        .join("order")
                        .join("product")
                        .groupBy("order.customer.id")
                        .having("COUNT(DISTINCT product.category)", ">", 2)
                )
                .orderBy("lastName")
                .findAll();
            
            logger.info("Found " + diverseCustomers.size() + " customers ordering from multiple categories");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to execute correlated subqueries", e);
            throw new RuntimeException("Correlated subquery execution failed", e);
        }
    }
    
    /**
     * Builds dynamic order query based on provided filters.
     */
    private QueryBuilder<Order> buildDynamicOrderQuery(Map<String, Object> filters) {
        QueryBuilder<Order> query = QueryBuilder.forEntity(Order.class)
            .join("customer")
            .leftJoin("orderItems")
            .leftJoin("orderItems.product");
        
        // Apply filters dynamically
        if (filters.containsKey("customerCountry")) {
            query = query.where("customer.country", filters.get("customerCountry"));
        }
        
        if (filters.containsKey("productCategory")) {
            query = query.and().where("orderItems.product.category", filters.get("productCategory"));
        }
        
        if (filters.containsKey("minOrderAmount")) {
            query = query.and().where("totalAmount", ">=", filters.get("minOrderAmount"));
        }
        
        if (filters.containsKey("maxOrderAmount")) {
            query = query.and().where("totalAmount", "<=", filters.get("maxOrderAmount"));
        }
        
        if (filters.containsKey("orderStatus")) {
            @SuppressWarnings("unchecked")
            List<String> statuses = (List<String>) filters.get("orderStatus");
            query = query.and().whereIn("status", statuses.stream()
                .map(Object.class::cast)
                .collect(Collectors.toList()));
        }
        
        if (filters.containsKey("orderDateFrom")) {
            query = query.and().where("orderDate", ">=", filters.get("orderDateFrom"));
        }
        
        if (filters.containsKey("customerVip")) {
            query = query.and().where("customer.vipStatus", filters.get("customerVip"));
        }
        
        return query.orderByDescending("orderDate").orderBy("id");
    }
    
    /**
     * Analyzes customer spending patterns from order data.
     */
    private void analyzeCustomerSpending(List<Order> orders) {
        logger.info("Analyzing customer spending patterns...");
        
        Map<String, Double> departmentSpending = orders.stream()
            .filter(order -> order.getCustomer() != null && order.getCustomer().getDepartment() != null)
            .collect(Collectors.groupingBy(
                order -> order.getCustomer().getDepartment(),
                Collectors.summingDouble(order -> 
                    order.getTotalAmount() != null ? order.getTotalAmount().doubleValue() : 0.0)
            ));
        
        departmentSpending.forEach((dept, total) -> 
            logger.info("Department " + dept + " spending: $" + String.format("%.2f", total)));
    }
    
    /**
     * Analyzes best-selling products with customer demographics.
     */
    private void analyzeBestSellersWithDemographics() {
        logger.info("Analyzing best sellers with customer demographics...");
        
        List<Product> bestSellers = QueryBuilder.forEntity(Product.class)
            .select("id", "name", "category")
            .sum("orderItems.quantity")
            .count("DISTINCT orderItems.order.customer.id")
            .leftJoin("orderItems")
            .leftJoin("orderItems.order")
            .leftJoin("orderItems.order.customer")
            .where("orderItems.order.status", "COMPLETED")
            .and()
            .where("orderItems.order.orderDate", ">=", LocalDate.now().minusDays(90))
            .groupBy("id", "name", "category")
            .having("SUM(orderItems.quantity)", ">", 10)
            .orderByDescending("SUM(orderItems.quantity)")
            .limit(10)
            .findAll();
        
        logger.info("Identified " + bestSellers.size() + " best-selling products");
    }
    
    /**
     * Maps and processes hierarchical customer data.
     */
    private void mapHierarchicalData(List<User> customers) {
        logger.info("Processing hierarchical customer data...");
        
        for (User customer : customers) {
            if (customer.getOrders() != null) {
                int totalOrders = customer.getOrders().size();
                double totalSpent = customer.getOrders().stream()
                    .filter(order -> order.getTotalAmount() != null)
                    .mapToDouble(order -> order.getTotalAmount().doubleValue())
                    .sum();
                
                logger.fine("Customer " + customer.getLastName() + 
                           " has " + totalOrders + " orders totaling $" + 
                           String.format("%.2f", totalSpent));
            }
        }
    }
    
    /**
     * Tests different filter combinations for API patterns.
     */
    private void testDifferentFilterCombinations() {
        logger.info("Testing different filter combinations...");
        
        // Test minimal filters
        Map<String, Object> minimalFilters = new HashMap<>();
        minimalFilters.put("orderStatus", Arrays.asList("COMPLETED"));
        
        long count1 = buildDynamicOrderQuery(minimalFilters).count();
        logger.info("Completed orders count: " + count1);
        
        // Test complex filters
        Map<String, Object> complexFilters = new HashMap<>();
        complexFilters.put("customerCountry", "USA");
        complexFilters.put("minOrderAmount", new BigDecimal("50.00"));
        complexFilters.put("orderDateFrom", LocalDate.now().minusDays(30));
        
        long count2 = buildDynamicOrderQuery(complexFilters).count();
        logger.info("Complex filtered orders count: " + count2);
    }
    
    /**
     * Analyzes product performance by customer segment.
     */
    private void analyzeProductPerformanceBySegment() {
        logger.info("Analyzing product performance by customer segment...");
        
        List<Product> segmentAnalysis = QueryBuilder.forEntity(Product.class)
            .select("category", "orderItems.order.customer.region")
            .sum("orderItems.quantity")
            .avg("orderItems.unitPrice")
            .leftJoin("orderItems")
            .leftJoin("orderItems.order")
            .leftJoin("orderItems.order.customer")
            .where("orderItems.order.status", "COMPLETED")
            .and()
            .whereIsNotNull("orderItems.order.customer.region")
            .groupBy("category", "orderItems.order.customer.region")
            .having("SUM(orderItems.quantity)", ">", 0)
            .orderBy("category")
            .orderBy("orderItems.order.customer.region")
            .findAll();
        
        logger.info("Completed segment analysis for " + segmentAnalysis.size() + " category-region combinations");
    }
}