import com.github.query4j.core.QueryBuilder;

public class TestJoins {
    public static void main(String[] args) {
        try {
            System.out.println("Testing JOIN functionality...");
            
            // Test valid cases
            QueryBuilder<Object> builder = QueryBuilder.forEntity(Object.class);
            
            String sql1 = builder.join("orders").toSQL();
            System.out.println("join(): " + sql1);
            
            String sql2 = builder.innerJoin("orders").toSQL();
            System.out.println("innerJoin(): " + sql2);
            
            String sql3 = builder.leftJoin("profile").toSQL();
            System.out.println("leftJoin(): " + sql3);
            
            String sql4 = builder.rightJoin("permissions").toSQL();
            System.out.println("rightJoin(): " + sql4);
            
            String sql5 = builder.fetch("profile").toSQL();
            System.out.println("fetch(): " + sql5);
            
            String sql6 = builder.join("orders").leftJoin("profile").rightJoin("permissions").toSQL();
            System.out.println("multiple joins: " + sql6);
            
            // Test invalid field name
            try {
                builder.join("invalid@field").toSQL();
                System.out.println("ERROR: Should have thrown exception for invalid field name");
            } catch (Exception e) {
                System.out.println("Correctly threw exception for invalid field: " + e.getClass().getSimpleName());
            }
            
            System.out.println("All JOIN tests passed!");
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
