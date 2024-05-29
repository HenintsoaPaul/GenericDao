package annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
public @interface ColumnAnnotation {
    String columnName();

    boolean quote() default false;

    boolean primaryKey() default false;

    boolean nullable() default false;

    boolean unique() default false;
}