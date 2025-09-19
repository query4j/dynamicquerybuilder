package com.github.query4j.examples;

import com.github.query4j.examples.batch.BatchProcessingApp;
import com.github.query4j.examples.async.AsyncQueryApp;
import com.github.query4j.examples.joins.ComplexJoinsApp;

import java.util.Scanner;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Demonstration runner for all Query4j consumer applications.
 * 
 * This class provides an interactive menu to run the different example
 * applications showcasing advanced Query4j features:
 * 
 * 1. BatchProcessingApp - Large dataset processing with pagination
 * 2. AsyncQueryApp - Concurrent query execution patterns
 * 3. ComplexJoinsApp - Multi-table joins and advanced querying
 * 
 * Each application demonstrates real-world usage patterns and best practices
 * for production environments.
 * 
 * @author query4j team
 * @version 1.0.0
 * @since 1.0.0
 */
public class ConsumerAppsDemo {
    
    private static final Logger logger = Logger.getLogger(ConsumerAppsDemo.class.getName());
    
    /**
     * Main entry point for the consumer applications demo.
     * 
     * @param args command line arguments:
     *             - No args: Interactive menu
     *             - "batch": Run BatchProcessingApp
     *             - "async": Run AsyncQueryApp  
     *             - "joins": Run ComplexJoinsApp
     *             - "all": Run all applications
     */
    public static void main(String[] args) {
        ConsumerAppsDemo demo = new ConsumerAppsDemo();
        
        try {
            if (args.length == 0) {
                demo.runInteractiveMenu();
            } else {
                demo.runApplicationByName(args[0]);
            }
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Consumer applications demo failed", e);
            System.exit(1);
        }
    }
    
    /**
     * Runs an interactive menu for selecting applications to run.
     */
    public void runInteractiveMenu() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                printMenu();
                
                System.out.print("Enter your choice (1-5): ");
                String input = scanner.nextLine().trim();
                
                try {
                    int choice = Integer.parseInt(input);
                    
                    switch (choice) {
                        case 1:
                            runBatchProcessingApp();
                            break;
                        case 2:
                            runAsyncQueryApp();
                            break;
                        case 3:
                            runComplexJoinsApp();
                            break;
                        case 4:
                            runAllApplications();
                            break;
                        case 5:
                            System.out.println("Goodbye!");
                            return;
                        default:
                            System.out.println("Invalid choice. Please enter 1-5.");
                    }
                    
                    System.out.println("\nPress Enter to continue...");
                    scanner.nextLine();
                    
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number 1-5.");
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error running application", e);
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Runs an application by name from command line argument.
     */
    public void runApplicationByName(String appName) {
        logger.info("Running application: " + appName);
        
        switch (appName.toLowerCase()) {
            case "batch":
                runBatchProcessingApp();
                break;
            case "async":
                runAsyncQueryApp();
                break;
            case "joins":
                runComplexJoinsApp();
                break;
            case "all":
                runAllApplications();
                break;
            default:
                System.err.println("Unknown application: " + appName);
                System.err.println("Valid options: batch, async, joins, all");
                System.exit(1);
        }
    }
    
    /**
     * Prints the interactive menu.
     */
    private void printMenu() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("           Query4j Consumer Applications Demo");
        System.out.println("=".repeat(60));
        System.out.println("1. Batch Processing App");
        System.out.println("   - Large dataset processing with pagination");
        System.out.println("   - Fault tolerance and retry mechanisms");
        System.out.println("   - Progress tracking and performance monitoring");
        System.out.println();
        System.out.println("2. Async Query App");
        System.out.println("   - Concurrent query execution with CompletableFuture");
        System.out.println("   - Thread-safe query building and result aggregation");
        System.out.println("   - Async pipeline processing patterns");
        System.out.println();
        System.out.println("3. Complex Joins App");
        System.out.println("   - Multi-table joins with advanced relationships");
        System.out.println("   - Dynamic query building and filtering");
        System.out.println("   - Hierarchical data mapping and aggregations");
        System.out.println();
        System.out.println("4. Run All Applications");
        System.out.println("   - Sequential execution of all demo applications");
        System.out.println();
        System.out.println("5. Exit");
        System.out.println("=".repeat(60));
    }
    
    /**
     * Runs the BatchProcessingApp demonstration.
     */
    private void runBatchProcessingApp() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("Running Batch Processing App");
        System.out.println("=".repeat(50));
        System.out.println("This demo shows how to process large datasets efficiently");
        System.out.println("using pagination, retry logic, and progress monitoring.\n");
        
        try {
            // Use smaller batch size for demonstration
            BatchProcessingApp app = new BatchProcessingApp(100);
            
            // Note: In a real implementation with a database, these would process actual data
            System.out.println("Simulating batch processing operations...");
            System.out.println("(In production, this would process real database records)\n");
            
            // Simulate the processing without actual database calls
            simulateBatchProcessing(app);
            
            System.out.println("\nBatch Processing App demonstration completed!");
            System.out.println("Key features demonstrated:");
            System.out.println("- Configurable batch sizes for memory efficiency");
            System.out.println("- Retry mechanisms with exponential backoff");
            System.out.println("- Progress tracking and throughput calculations");
            System.out.println("- Error handling and recovery strategies");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "BatchProcessingApp demo failed", e);
            System.err.println("Demo failed: " + e.getMessage());
        }
    }
    
    /**
     * Runs the AsyncQueryApp demonstration.
     */
    private void runAsyncQueryApp() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("Running Async Query App");
        System.out.println("=".repeat(50));
        System.out.println("This demo shows concurrent query execution patterns");
        System.out.println("using CompletableFuture and thread-safe operations.\n");
        
        AsyncQueryApp app = null;
        try {
            app = new AsyncQueryApp(2); // Small thread pool for demo
            
            System.out.println("Simulating asynchronous query operations...");
            System.out.println("(In production, this would execute real concurrent queries)\n");
            
            // Simulate async operations without actual database calls
            simulateAsyncProcessing(app);
            
            System.out.println("\nAsync Query App demonstration completed!");
            System.out.println("Key features demonstrated:");
            System.out.println("- Concurrent query execution with thread pools");
            System.out.println("- Result aggregation from multiple async sources");
            System.out.println("- Pipeline processing with chained operations");
            System.out.println("- Thread-safe statistics and monitoring");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "AsyncQueryApp demo failed", e);
            System.err.println("Demo failed: " + e.getMessage());
        } finally {
            if (app != null) {
                app.shutdown();
            }
        }
    }
    
    /**
     * Runs the ComplexJoinsApp demonstration.
     */
    private void runComplexJoinsApp() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("Running Complex Joins App");
        System.out.println("=".repeat(50));
        System.out.println("This demo shows advanced querying with multi-table joins,");
        System.out.println("dynamic filtering, and hierarchical data mapping.\n");
        
        try {
            ComplexJoinsApp app = new ComplexJoinsApp();
            
            System.out.println("Simulating complex join operations...");
            System.out.println("(In production, this would execute complex SQL queries)\n");
            
            // Simulate complex query operations without actual database calls
            simulateComplexJoins(app);
            
            System.out.println("\nComplex Joins App demonstration completed!");
            System.out.println("Key features demonstrated:");
            System.out.println("- Multi-table joins with deep relationships");
            System.out.println("- Dynamic query building based on filters");
            System.out.println("- Advanced aggregations across joined tables");
            System.out.println("- Correlated subqueries and EXISTS clauses");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "ComplexJoinsApp demo failed", e);
            System.err.println("Demo failed: " + e.getMessage());
        }
    }
    
    /**
     * Runs all applications in sequence.
     */
    private void runAllApplications() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Running All Consumer Applications");
        System.out.println("=".repeat(60));
        System.out.println("This will run all three demo applications in sequence.\n");
        
        try {
            runBatchProcessingApp();
            Thread.sleep(1000); // Brief pause between demos
            
            runAsyncQueryApp();
            Thread.sleep(1000); // Brief pause between demos
            
            runComplexJoinsApp();
            
            System.out.println("\n" + "=".repeat(60));
            System.out.println("All Consumer Applications Demo Completed!");
            System.out.println("=".repeat(60));
            System.out.println("You have seen demonstrations of:");
            System.out.println("✓ Batch Processing with fault tolerance");
            System.out.println("✓ Asynchronous query execution patterns");
            System.out.println("✓ Complex joins and advanced querying");
            System.out.println("\nThese examples showcase Query4j's capabilities for");
            System.out.println("production-scale applications and real-world scenarios.");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.WARNING, "Demo sequence interrupted", e);
        }
    }
    
    // Simulation methods (in real implementation, these would use actual QueryBuilder calls)
    
    private void simulateBatchProcessing(BatchProcessingApp app) {
        System.out.println("Batch size: " + app.getBatchSize());
        System.out.println("Processing batches...");
        for (int i = 1; i <= 5; i++) {
            System.out.println("  Batch " + i + ": Processing records 1-100");
            try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        System.out.println("Final statistics: 500 records processed, 0 errors");
    }
    
    private void simulateAsyncProcessing(AsyncQueryApp app) {
        System.out.println("Starting concurrent operations...");
        System.out.println("  ✓ User count query started");
        System.out.println("  ✓ Order analysis query started");
        System.out.println("  ✓ Product report query started");
        try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        System.out.println("All async operations completed successfully");
        app.printStatistics();
    }
    
    private void simulateComplexJoins(ComplexJoinsApp app) {
        System.out.println("Executing complex join operations...");
        System.out.println("  ✓ Customer-Order analysis with product details");
        System.out.println("  ✓ Sales reporting across multiple dimensions");
        System.out.println("  ✓ Hierarchical data mapping");
        System.out.println("  ✓ Dynamic filtering with multiple conditions");
        System.out.println("Complex queries executed successfully");
    }
}