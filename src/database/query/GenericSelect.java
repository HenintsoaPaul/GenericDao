package database.query;

import database.utils.QueryUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class GenericSelect {
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

    public static List<Object> exeSelectQuery( String query, Object object, Connection connection )
            throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException,
            IllegalAccessException {
        List<Object> results = new ArrayList<>();
        try ( PreparedStatement preparedStatement = connection.prepareStatement( query );
              ResultSet resultSet = preparedStatement.executeQuery() ) {
            if ( !resultSet.next() ) {
                throw new SQLException( "0 row matching! ResultSet is empty." );
            } else {
                do {
                    Object newInstance = object.getClass().getConstructor().newInstance();
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

    public static String writeQueryFindAll( Object object, List<String> columnsNames ) {
        return QueryUtil.selectColumns( columnsNames ) + " FROM " + QueryUtil.getTableName( object.getClass() );
    }

    public static List<Object> findAll( Object object, List<String> columnsNames, Connection connection )
            throws SQLException, InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        String query = writeQueryFindAll( object, columnsNames );
        return exeSelectQuery( query, object, connection );
    }
}
