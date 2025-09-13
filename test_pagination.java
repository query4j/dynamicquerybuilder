import com.github.query4j.core.QueryBuilder;

public class TestPagination {
    public static void main(String[] args) {
        // Test page calculations as mentioned in issue
        QueryBuilder<String> builder = QueryBuilder.forEntity(String.class);
        
        // page(1, 20) → OFFSET 0 LIMIT 20
        System.out.println("Page 1, Size 20:");
        System.out.println(builder.page(1, 20).toSQL());
        System.out.println();
        
        // page(2, 20) → OFFSET 20 LIMIT 20
        System.out.println("Page 2, Size 20:");
        System.out.println(builder.page(2, 20).toSQL());
        System.out.println();
        
        // page(3, 15) → OFFSET 30 LIMIT 15
        System.out.println("Page 3, Size 15:");
        System.out.println(builder.page(3, 15).toSQL());
        System.out.println();
        
        // Test multiple orderBy
        System.out.println("Multiple Order By:");
        System.out.println(builder.orderBy("field1").orderBy("field2", false).orderBy("field3").toSQL());
    }
}
