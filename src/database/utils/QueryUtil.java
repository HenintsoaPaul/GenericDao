package database.utils;

import annotations.ColumnAnnotation;
import annotations.TableAnnotation;
import database.GenericDaoException;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class QueryUtil {
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

    /**
     *
     * @param query The query to be executed.
     * @param connection An SQL connection to the database.
     * @param nbRowsToUpdate The estimated number of rows to be affected by the query.
     *  When we do not know in advance how many rows will be affected, use -1.
     * @return The number of rows that have been really affected.
     */
    public static int executeUpdateQuery( String query, Connection connection, int nbRowsToUpdate )
            throws SQLException {
        try ( PreparedStatement preparedStatement = connection.prepareStatement( query ) ) {
            if ( nbRowsToUpdate == -1 ) {
                return preparedStatement.executeUpdate();
            }
            else {
                connection.setAutoCommit( false );
                int nbRows = preparedStatement.executeUpdate();
                if ( nbRows != nbRowsToUpdate ) {
                    throw new GenericDaoException( " An Exception occurred during the query. " + nbRows + " row(s) affected" +
                            ", but " + nbRowsToUpdate + " row(s) was supposed to be affected." );
                }
                connection.commit();
                return nbRows;
            }
        }
    }
}
