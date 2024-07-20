package database.utils;

import annotations.ColumnAnnotation;
import annotations.TableAnnotation;
import database.GenericDaoException;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QueryUtil {
    public static String getTableName( Class<?> clazz ) {
        return clazz.isAnnotationPresent( TableAnnotation.class ) ?
                clazz.getAnnotation( TableAnnotation.class ).tableName() : clazz.getSimpleName();
    }

    public static String getColumnName( Field field )
            throws GenericDaoException {
        String fieldName = field.getName();
        ColumnAnnotation columnAnnotation = field.getAnnotation( ColumnAnnotation.class );
        if ( columnAnnotation == null ) {
            throw new GenericDaoException( "Field \"" + fieldName + "\" is not a column." );
        }
        String annotationValue = columnAnnotation.columnName();
        return annotationValue.isEmpty() ? fieldName : annotationValue;
    }

    public static String getColumnValue( Field field, Object object )
            throws IllegalAccessException, GenericDaoException {
        field.setAccessible( true );
        String columnValue = field.get( object ).toString();
        ColumnAnnotation columnAnnotation = field.getAnnotation( ColumnAnnotation.class );
        if ( columnAnnotation == null ) {
            throw new GenericDaoException( "Field \"" + field.getName() + "\" is not a column." );
        }
        return columnAnnotation.quoted() ? "'" + columnValue + "'" : columnValue;
    }

    public static Field getPrimaryKeyField( Class<?> clazz ) {
        for ( Field field : getColumnsFields( clazz ) ) {
            ColumnAnnotation columnAnnotation = field.getAnnotation( ColumnAnnotation.class );
            if ( columnAnnotation.primaryKey() ) return field;
        }
        return null;
    }

    /**
     * Loop through the fields of `clazz` then return a list of the fields
     * annotated with `ColumnAnnotation`.
     *
     * @param clazz The class containing the fields.
     * @return A list of the fields in `clazz` annotated with `ColumnAnnotation`.
     */
    public static List<Field> getColumnsFields( Class<?> clazz ) {
        List<Field> fields = new ArrayList<>();
        for ( Field field : clazz.getDeclaredFields() ) {
            if ( field.isAnnotationPresent( ColumnAnnotation.class ) ) fields.add( field );
        }
        return fields;
    }

    /**
     * @param query          The query to be executed.
     * @param connection     An SQL connection to the database.
     * @param nbRowsToUpdate The estimated number of rows to be affected by the query.
     *                       When we do not know in advance how many rows will be affected, use -1.
     * @return The number of rows that have been really affected.
     */
    public static int executeUpdateQuery( String query, Connection connection, int nbRowsToUpdate )
            throws SQLException {
        try ( PreparedStatement preparedStatement = connection.prepareStatement( query ) ) {
            if ( nbRowsToUpdate == -1 ) {
                return preparedStatement.executeUpdate();
            } else {
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

    public static String selectColumns( List<String> columnsNames ) {
        String columns = columnsNames == null || columnsNames.isEmpty() ?
                "*" : String.join( ", ", columnsNames );
        return "SELECT " + columns;
    }

    public static Map<String, String> getProperties( String configFilePath ) {
        ConfigFileReader configReader = new ConfigFileReader( configFilePath );
        return configReader.loadProperties();
    }
}
