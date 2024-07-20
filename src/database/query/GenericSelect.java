package database.query;

import database.GenericDaoException;
import database.query.select.FindAll;
import database.query.select.FindCriteria;
import database.query.select.FindInterval;
import database.utils.PaginationUtil;
import database.utils.QueryUtil;
import database.utils.TypeCastUtil;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings( "unchecked" )
public class GenericSelect {
    private static final FindAll all = new FindAll();
    private static final FindCriteria criteria = new FindCriteria();
    private static final FindInterval interval = new FindInterval();

    public static <T extends GenericEntity> List<T> exeSelectQuery( String query, Class<T> tClass, Connection connection )
            throws SQLException, NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        List<T> results = new ArrayList<>();
        try ( PreparedStatement preparedStatement = connection.prepareStatement( query );
              ResultSet resultSet = preparedStatement.executeQuery() ) {
            if ( resultSet.next() ) {
                do {
                    T newInstance = tClass.getDeclaredConstructor().newInstance();
                    TypeCastUtil.setAttributesValues( newInstance, resultSet );
                    results.add( newInstance );
                }
                while ( resultSet.next() );
            }
        }
        return results;
    }

    public static <T extends GenericEntity> List<T> findAll(
            Object object, List<String> columnsNames, Connection connection )
            throws SQLException, InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        String query = all.query( object, columnsNames );
        return exeSelectQuery( query, ( Class<T> ) object.getClass(), connection );
    }

    public static <T extends GenericEntity> List<T> findByCriteria(
            Object object, List<String> columnsNames, Connection connection )
            throws SQLException, InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        String query = criteria.query( object, columnsNames );
        System.out.println( query );
        return exeSelectQuery( query, ( Class<T> ) object.getClass(), connection );
    }


    // FIND IN INTERVAL
    public static <T extends GenericEntity> List<T> findInInterval( Object object, List<String> columnsNames, Connection connection,
                                                                    String fieldName, Number minValue, Number maxValue )
            throws SQLException, InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        String query = interval.query( object, columnsNames, fieldName, minValue, maxValue );
        return exeSelectQuery( query, ( Class<T> ) object.getClass(), connection );
    }


    // FIND ALL WITH PAGINATION
    public static String writeQueryFindAll( Object object, List<String> columnsNames, int offSet, int limit, String DB_TYPE )
            throws GenericDaoException {
        if ( offSet == 0 && limit == 0 ) { // Query without pagination
            return all.query( object, columnsNames );
        }
        String from = all.from( object );
        return PaginationUtil.paginationQuery( columnsNames, offSet, limit, DB_TYPE, from );
    }

    public static <T extends GenericEntity> List<T> findAll(
            Object object, List<String> columnsNames, Connection connection,
            int offSet, int limit, String DB_TYPE )
            throws SQLException, InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        String query = writeQueryFindAll( object, columnsNames, offSet, limit, DB_TYPE );
        return exeSelectQuery( query, ( Class<T> ) object.getClass(), connection );
    }


    // FIND BY CRITERIA WITH PAGINATION
    public static String writeQueryFindByCriteria( Object object, List<String> columnsNames, int offSet, int limit, String DB_TYPE )
            throws GenericDaoException {
        if ( offSet == 0 && limit == 0 ) { // Query without pagination
            return criteria.query( object, columnsNames );
        }
        String from = criteria.from( object );
        return PaginationUtil.paginationQuery( columnsNames, offSet, limit, DB_TYPE, from );
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
            return interval.query( object, columnsNames, fieldName, minValue, maxValue );
        }
        String from = interval.from( object, fieldName, minValue, maxValue );
        return PaginationUtil.paginationQuery( columnsNames, offSet, limit, DB_TYPE, from );
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
