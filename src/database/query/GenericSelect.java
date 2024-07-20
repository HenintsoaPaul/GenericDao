package database.query;

import database.GenericDaoException;
import database.query.select.FindAll;
import database.query.select.FindCriteria;
import database.query.select.FindInterval;
import database.utils.PaginationUtil;
import database.utils.PropertiesUtil;
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

    /**
     * Get all the rows in the relation(table).
     *
     * @param object       An instance of the entity linked to the relation.
     * @param columnsNames The names of the queried columns.
     * @return A list containing all the rows of the relation.
     */
    public static <T extends GenericEntity> List<T> findAll(
            Object object, List<String> columnsNames, Connection connection )
            throws SQLException, InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        String query = all.query( object, columnsNames );
        return exeSelectQuery( query, ( Class<T> ) object.getClass(), connection );
    }

    /**
     * Get all the rows matching the criteria in the relation(table). If `criteriaColumns`
     * is set to null, all the columns will be used to build the criteria. Otherwise, criteria
     * is build from the names of the columns in `criteriaColumns`.
     *
     * @param object          An instance of the entity linked to the relation.
     * @param columnsNames    The names of the queried columns.
     * @param criteriaColumns The names of the columns to be used as criteria.
     * @return A list containing all the rows of the relation.
     */
    public static <T extends GenericEntity> List<T> findByCriteria(
            Object object, List<String> columnsNames, List<String> criteriaColumns, Connection connection )
            throws SQLException, InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        String query = criteria.query( object, columnsNames, criteriaColumns );
        System.out.println( query );
        return exeSelectQuery( query, ( Class<T> ) object.getClass(), connection );
    }

    /**
     * Get all the rows matching the interval in the relation(table). minValue and
     * maxValue are included in the interval.
     *
     * @param object       An instance of the entity linked to the relation.
     * @param columnsNames The names of the queried columns.
     * @param fieldName    The name of column on which we apply the interval.
     * @param minValue     The minimum value(included).
     * @param maxValue     The maximum value(included).
     * @return A list containing all the rows of the relation.
     */
    public static <T extends GenericEntity> List<T> findInInterval(
            Object object, List<String> columnsNames, Connection connection,
            String fieldName, Number minValue, Number maxValue )
            throws SQLException, InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        String query = interval.query( object, columnsNames, fieldName, minValue, maxValue );
        return exeSelectQuery( query, ( Class<T> ) object.getClass(), connection );
    }


    // WITH PAGINATION
    public static <T extends GenericEntity> List<T> findAll(
            Object object, List<String> columnsNames, Connection connection,
            int offSet, int limit, String DB_TYPE )
            throws SQLException, InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        String query = all.queryPagination( object, columnsNames, offSet, limit, DB_TYPE );
        return exeSelectQuery( query, ( Class<T> ) object.getClass(), connection );
    }

    public static <T extends GenericEntity> List<T> findByCriteria(
            Object object, List<String> columnsNames, List<String> criteriaColumns,
            Connection connection, int offSet, int limit, String DB_TYPE )
            throws SQLException, InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        String query = criteria.queryPagination( object, columnsNames, criteriaColumns, offSet, limit, DB_TYPE );
        return exeSelectQuery( query, ( Class<T> ) object.getClass(), connection );
    }

    public static <T extends GenericEntity> List<T> findInInterval(
            Object object, List<String> columnsNames, Connection connection,
            int offSet, int limit, String DB_TYPE,
            String fieldName, Number minValue, Number maxValue )
            throws SQLException, InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        String query = interval.queryPagination( object, columnsNames, offSet, limit, DB_TYPE, fieldName, minValue, maxValue );
        return exeSelectQuery( query, ( Class<T> ) object.getClass(), connection );
    }


    // PAGINATION USING db.properties FILE

    /**
     * Find all rows with pagination functionality using configuration in the confs/ folder.
     */
    public static <T extends GenericEntity> List<T> findAll(
            Object object, List<String> columnsNames, Connection connection,
            String configFilePath, int offSet )
            throws SQLException, InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        String DB_TYPE = PropertiesUtil.getDbType( configFilePath );
        int limit = PropertiesUtil.getLimit( configFilePath );
        return findAll( object, columnsNames, connection, offSet, limit, DB_TYPE );
    }

    /**
     * Find by criteria with pagination functionality using configuration in the confs/ folder.
     */
    public static <T extends GenericEntity> List<T> findByCriteria(
            Object object, List<String> columnsNames, List<String> criteriaColumns, Connection connection,
            String configFilePath, int offSet )
            throws SQLException, InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        String DB_TYPE = PropertiesUtil.getDbType( configFilePath );
        int limit = PropertiesUtil.getLimit( configFilePath );
        return findByCriteria( object, columnsNames, criteriaColumns, connection, offSet, limit, DB_TYPE );
    }

    /**
     * Find in interval with pagination functionality using configuration in the confs/ folder.
     */
    public static <T extends GenericEntity> List<T> findInInterval(
            Object object, List<String> columnsNames, Connection connection,
            String configFilePath, int offSet,
            String fieldName, Number minValue, Number maxValue )
            throws SQLException, InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        String DB_TYPE = PropertiesUtil.getDbType( configFilePath );
        int limit = PropertiesUtil.getLimit( configFilePath );
        return findInInterval( object, columnsNames, connection, offSet, limit, DB_TYPE, fieldName, minValue, maxValue );
    }
}
