package database.query;

import annotations.ColumnAnnotation;
import database.GenericDaoException;
import database.utils.QueryUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GenericSelect {
    private static Object getFieldValue( Field field, ResultSet resultSet )
            throws SQLException {
        Class<?> fieldType = field.getType();
        String columnName = QueryUtil.getColumnName( field );
        if ( fieldType.equals( String.class ) ) {
            return resultSet.getString( columnName );
        } else if ( fieldType.equals( int.class ) ) {
            return resultSet.getInt( columnName );
        } else if ( fieldType.equals( double.class ) ) {
            return resultSet.getDouble( columnName );
        } else if ( fieldType.equals( boolean.class ) ) {
            return resultSet.getBoolean( columnName );
        }
        return null;
    }

    private static void setAttributesValues( Object newInstance, ResultSet resultSet )
            throws SQLException, IllegalAccessException {
        for ( Field field : newInstance.getClass().getDeclaredFields() ) {
            field.setAccessible( true );
            field.set( newInstance, getFieldValue( field, resultSet ) );
        }
    }

    public static <T extends GenericEntity> List<T> exeSelectQuery( String query, Class<T> tClass, Connection connection )
            throws SQLException, NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        List<T> results = new ArrayList<>();
        try ( PreparedStatement preparedStatement = connection.prepareStatement( query );
              ResultSet resultSet = preparedStatement.executeQuery() ) {
            if ( !resultSet.next() ) {
                throw new SQLException( "0 row matching! ResultSet is empty." );
            } else {
                do {
                    T newInstance = tClass.getDeclaredConstructor().newInstance();
                    setAttributesValues( newInstance, resultSet );
                    results.add( newInstance );
                }
                while ( resultSet.next() );
            }
        }
        return results;
    }


    // FIND ALL
    public static String fromFindAll( Object object ) {
        return QueryUtil.getTableName( object.getClass() );
    }

    public static <T extends GenericEntity> String writeQueryFindAll( Class<T> tClass, List<String> columnsNames ) {
        return QueryUtil.selectColumns( columnsNames ) + " FROM " + QueryUtil.getTableName( tClass );
    }

    public static <T extends GenericEntity> List<T> findAll( Class<T> tClass, List<String> columnsNames, Connection connection )
            throws SQLException, InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        String query = writeQueryFindAll( tClass, columnsNames );
        return exeSelectQuery( query, tClass, connection );
    }


    // FIND ALL BY CRITERIA
    public static String fromFindByCriteria( Object object ) {
        Class<?> clazz = object.getClass();
        String tableName = QueryUtil.getTableName( clazz ),
                criteria = Arrays.stream( clazz.getDeclaredFields() )
                        .peek( field -> field.setAccessible( true ) )
                        .filter( field -> {
                            try {
                                return field.get( object ) != null;
                            } catch ( IllegalAccessException e ) {
                                throw new RuntimeException( e );
                            }
                        } )
                        .map( field -> {
                            try {
                                Object columnValue = field.get( object );
                                String strColumnValue = columnValue instanceof Number ?
                                        columnValue.toString() : "'" + columnValue + "'";
                                return QueryUtil.getColumnName( field ) + " = " + strColumnValue;
                            } catch ( IllegalAccessException e ) {
                                throw new RuntimeException( e );
                            }
                        } )
                        .collect( Collectors.joining( " AND " ) );
        return tableName + ( criteria.isEmpty() ? "" : " WHERE " + criteria );
    }

    public static String writeQueryFindByCriteria( Object object, List<String> columnsNames ) {
        return QueryUtil.selectColumns( columnsNames ) + " FROM " + fromFindByCriteria( object );
    }

    public static <T extends GenericEntity> List<T> findByCriteria( Object object, List<String> columnsNames, Connection connection )
            throws SQLException, InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        String query = writeQueryFindByCriteria( object, columnsNames );
        return exeSelectQuery( query, ( Class<T> ) object.getClass(), connection );
    }


    // FIND ALL IN INTERVAL
    public static String fromFindInInterval( Object object, String fieldName, Number minValue, Number maxValue )
            throws GenericDaoException {
        Class<?> clazz = object.getClass();
        String tableName = QueryUtil.getTableName( clazz ), columnName = "";
        for ( Field field : clazz.getDeclaredFields() ) {
            if ( field.getName().equals( fieldName ) && field.isAnnotationPresent( ColumnAnnotation.class ) ) {
                columnName = QueryUtil.getColumnName( field );
                break;
            }
        }
        if ( columnName.isEmpty() ) {
            throw new GenericDaoException( fieldName + " is not a column of the relation \"" + tableName + "\"." );
        }
        return tableName + " WHERE " + columnName + " BETWEEN " + minValue + " AND " + maxValue;
    }

    public static String writeQueryFindInInterval( Object object, List<String> columnsNames,
                                                   String fieldName, Number minValue, Number maxValue )
            throws GenericDaoException {
        return QueryUtil.selectColumns( columnsNames ) + " FROM " + fromFindInInterval( object, fieldName, minValue, maxValue );
    }

    public static <T extends GenericEntity> List<T> findInInterval( Object object, List<String> columnsNames, Connection connection,
                                                                    String fieldName, Number minValue, Number maxValue )
            throws SQLException, InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        String query = writeQueryFindInInterval( object, columnsNames, fieldName, minValue, maxValue );
        return exeSelectQuery( query, ( Class<T> ) object.getClass(), connection );
    }
}
