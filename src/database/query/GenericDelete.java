package database.query;

import database.GenericDaoException;
import database.connector.DatabaseConnector;
import database.utils.QueryUtil;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;

public class GenericDelete {
    public static int deleteAll( Object object, Connection connection )
            throws SQLException {
        String query = "DELETE FROM " + QueryUtil.getTableName( object.getClass() );
        return QueryUtil.executeUpdateQuery( query, connection, -1 );
    }

    public static int deleteAll( Object object, DatabaseConnector databaseConnector )
            throws SQLException, ClassNotFoundException {
        try ( Connection connection = databaseConnector.getConnection() ) {
            return deleteAll( object, connection );
        }
    }

    // delete by id primary_key (set ColumnAnnotation.primarykey = true)
    public static int deleteById( Object object, int pkValue, Connection connection )
            throws SQLException {
        Class<?> clazz = object.getClass();
        Field pkField = QueryUtil.getPrimaryKeyField( clazz );
        if ( pkField == null ) {
            throw new GenericDaoException( "Cannot perform delete query 'coz there is no PRIMARY KEY in the object." +
                    " Please, verify your annotations." );
        }
        String pkColumnName = QueryUtil.getColumnName( pkField ),
                query = "DELETE FROM " + QueryUtil.getTableName( clazz ) + " WHERE " + pkColumnName + " = " + pkValue;
        return QueryUtil.executeUpdateQuery( query, connection, 1 );
    }

    public static int deleteById( Object object, int pkValue, DatabaseConnector databaseConnector )
            throws SQLException, ClassNotFoundException {
        try ( Connection connection = databaseConnector.getConnection() ) {
            return deleteById( object, pkValue, connection );
        }
    }

    // delete using a condition ==
    // delete using a condition <
    // delete using a condition >
    // delete using a condition LIKE
}
