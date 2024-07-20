package database.query;

import annotations.ColumnAnnotation;
import database.GenericDaoException;
import database.utils.ConfigFileReader;
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
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings( "unchecked" )
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
            if ( resultSet.next() ) {
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

    /*
     * PAGINATION
     */
    private static String returnPaginationQuery( List<String> columnsNames, int offSet, int limit, String DB_TYPE, String from ) {
        return switch ( DB_TYPE ) {
            case "pg", "mysql" -> {
                String pagination = " LIMIT " + limit + " OFFSET " + offSet,
                        select = QueryUtil.selectColumns( columnsNames );
                yield select + " FROM " + from + pagination;
            }
            case "mssql" -> {
                String rowNumber = ", ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) AS rn FROM ",
                        subQuery = QueryUtil.selectColumns( columnsNames ) + rowNumber + from;
                yield "SELECT * FROM ( " + subQuery + " ) WHERE rn > " + offSet + " AND rn <= " + ( offSet + limit );
            }
            default -> "";
        };
    }


    // FIND ALL
    private static String fromFindAll( Object object ) {
        return QueryUtil.getTableName( object.getClass() );
    }

    public static String writeQueryFindAll( Object object, List<String> columnsNames ) {
        return QueryUtil.selectColumns( columnsNames ) + " FROM " + QueryUtil.getTableName( object.getClass() );
    }

    public static <T extends GenericEntity> List<T> findAll( Object object, List<String> columnsNames, Connection connection )
            throws SQLException, InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        String query = writeQueryFindAll( object, columnsNames );
        return exeSelectQuery( query, ( Class<T> ) object.getClass(), connection );
    }


    // FIND BY CRITERIA
    private static String fromFindByCriteria( Object object ) {
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
                            } catch ( IllegalAccessException | GenericDaoException e ) {
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


    // FIND IN INTERVAL
    private static String fromFindInInterval( Object object, String fieldName, Number minValue, Number maxValue )
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


    // FIND ALL WITH PAGINATION
    public static String writeQueryFindAll( Object object, List<String> columnsNames, int offSet, int limit, String DB_TYPE ) {
        if ( offSet == 0 && limit == 0 ) { // Query without pagination
            return writeQueryFindAll( object, columnsNames );
        }
        String from = fromFindAll( object );
        return returnPaginationQuery( columnsNames, offSet, limit, DB_TYPE, from );
    }

    public static <T extends GenericEntity> List<T> findAll( Object object, List<String> columnsNames, Connection connection,
                                                             int offSet, int limit, String DB_TYPE )
            throws SQLException, InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        String query = writeQueryFindAll( object, columnsNames, offSet, limit, DB_TYPE );
        return exeSelectQuery( query, ( Class<T> ) object.getClass(), connection );
    }


    // FIND BY CRITERIA WITH PAGINATION
    public static String writeQueryFindByCriteria( Object object, List<String> columnsNames, int offSet, int limit, String DB_TYPE ) {
        if ( offSet == 0 && limit == 0 ) { // Query without pagination
            return writeQueryFindByCriteria( object, columnsNames );
        }
        String from = fromFindByCriteria( object );
        return returnPaginationQuery( columnsNames, offSet, limit, DB_TYPE, from );
    }

    public static <T extends GenericEntity> List<T> findByCriteria( Object object, List<String> columnsNames, Connection connection,
                                                                    int offSet, int limit, String DB_TYPE )
            throws SQLException, InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        String query = writeQueryFindByCriteria( object, columnsNames, offSet, limit, DB_TYPE );
        return exeSelectQuery( query, ( Class<T> ) object.getClass(), connection );
    }


    // FIND IN INTERVAL WITH PAGINATION
    public static String writeQueryFindInInterval( Object object, List<String> columnsNames, int offSet, int limit,
                                                   String DB_TYPE, String fieldName, Number minValue, Number maxValue )
            throws GenericDaoException {
        if ( offSet == 0 && limit == 0 ) { // Query without pagination
            return writeQueryFindInInterval( object, columnsNames, fieldName, minValue, maxValue );
        }
        String from = fromFindInInterval( object, fieldName, minValue, maxValue );
        return returnPaginationQuery( columnsNames, offSet, limit, DB_TYPE, from );
    }

    public static <T extends GenericEntity> List<T> findInInterval( Object object, List<String> columnsNames, Connection connection,
                                                                    int offSet, int limit, String DB_TYPE, String fieldName,
                                                                    Number minValue, Number maxValue )
            throws SQLException, InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        String query = writeQueryFindInInterval( object, columnsNames, offSet, limit, DB_TYPE, fieldName, minValue, maxValue );
        return exeSelectQuery( query, ( Class<T> ) object.getClass(), connection );
    }


    // PAGINATION USING db.properties FILE

    /**
     * Find all rows with pagination functionality using configuration in the confs/ folder.
     */
    public static <T extends GenericEntity> List<T> findAll( Object object, List<String> columnsNames, Connection connection, String configFilePath )
            throws SQLException, InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        Map<String, String> properties = QueryUtil.getProperties( configFilePath );
        String DB_TYPE = properties.get( "db" );
        int offSet = Integer.parseInt( properties.get( "offSet" ) ),
                limit = Integer.parseInt( properties.get( "limit" ) );
        return findAll( object, columnsNames, connection, offSet, limit, DB_TYPE );
    }

    /**
     * Find by criteria with pagination functionality using configuration in the confs/ folder.
     */
    public static <T extends GenericEntity> List<T> findByCriteria( Object object, List<String> columnsNames, Connection connection, String configFilePath )
            throws SQLException, InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        Map<String, String> properties = QueryUtil.getProperties( configFilePath );
        String DB_TYPE = properties.get( "db" );
        int offSet = Integer.parseInt( properties.get( "offSet" ) ),
                limit = Integer.parseInt( properties.get( "limit" ) );
        return findByCriteria( object, columnsNames, connection, offSet, limit, DB_TYPE );
    }

    /**
     * Find in interval with pagination functionality using configuration in the confs/ folder.
     */
    public static <T extends GenericEntity> List<T> findInInterval( Object object, List<String> columnsNames, Connection connection,
                                                                    String configFilePath, String fieldName, Number minValue, Number maxValue )
            throws SQLException, InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        Map<String, String> properties = QueryUtil.getProperties( configFilePath );
        String DB_TYPE = properties.get( "db" );
        int offSet = Integer.parseInt( properties.get( "offSet" ) ),
                limit = Integer.parseInt( properties.get( "limit" ) );
        return findInInterval( object, columnsNames, connection, offSet, limit, DB_TYPE, fieldName, minValue, maxValue );
    }
}
