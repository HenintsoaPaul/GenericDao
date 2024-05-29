package database.utils;

import annotations.ColumnAnnotation;
import annotations.TableAnnotation;

import java.lang.reflect.Field;

public class Util {
    public static String getTableName( Class<?> clazz ) {
        return clazz.isAnnotationPresent( TableAnnotation.class ) ?
                clazz.getAnnotation( TableAnnotation.class ).tableName() : clazz.getSimpleName();
    }

    public static String getColumnName( Field field ) {
        ColumnAnnotation columnAnnotation = field.getAnnotation( ColumnAnnotation.class );
        if ( columnAnnotation != null ) {
            String annotationValue = columnAnnotation.columnName();
            return annotationValue.isEmpty() ? field.getName() : annotationValue;
        }
        return field.getName();
    }

    public static String getColumnValue( Field field, Object object )
            throws IllegalAccessException {
        field.setAccessible( true );
        String columnValue = field.get( object ).toString();
        ColumnAnnotation columnAnnotation = field.getAnnotation( ColumnAnnotation.class );
        if ( columnAnnotation != null ) {
            return columnAnnotation.quoted() ? "'" + columnValue + "'" : columnValue;
        }
        return columnValue;
    }

    public static Field getPrimaryKeyField( Class<?> clazz ) {
        for ( Field field : clazz.getDeclaredFields() ) {
            ColumnAnnotation columnAnnotation = field.getAnnotation( ColumnAnnotation.class );
            if ( columnAnnotation != null && columnAnnotation.primaryKey()) {
                return field;
            }
        }
        return null;
    }
}
