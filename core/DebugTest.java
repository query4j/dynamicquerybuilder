import com.github.query4j.core.impl.DynamicQueryBuilder;

public class DebugTest {
    public static void main(String[] args) {
        DynamicQueryBuilder<Object> builder = new DynamicQueryBuilder<>(Object.class);
        
        System.out.println("Page 1 SQL: " + builder.page(1, 10).toSQL());
        System.out.println("Page 2 SQL: " + builder.page(2, 10).toSQL());
        System.out.println("NOT SQL: " + builder.not().where("active", true).toSQL());
        
        try {
            builder.where("field", null, "value");
        } catch (Exception e) {
            System.out.println("Null operator exception: " + e.getClass().getSimpleName());
        }
    }
}
