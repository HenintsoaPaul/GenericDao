package database.query;

import database.GenericDaoException;
import database.connector.DatabaseConnector;
import database.utils.QueryUtil;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class GenericDelete {
    /**
     *
     * @param query The delete query to be executed.
     * @param connection An SQL connection to the database.
     * @param nbRowsToDeleted The estimated number of rows to be deleted by the query.
     *  When we do not know in advance how many rows will be affected, use -1.
     * @return The number of rows that have been deleted.
     */
    public static int exeDelete( String query, Connection connection, int nbRowsToDeleted )
            throws SQLException {
        try ( PreparedStatement preparedStatement = connection.prepareStatement( query ) ) {
            if ( nbRowsToDeleted != -1 ) {
                return preparedStatement.executeUpdate();
            }
            else {
                connection.setAutoCommit( false );
                int nbRows = preparedStatement.executeUpdate();
                if ( nbRows != nbRowsToDeleted ) {
                    throw new GenericDaoException( " An Exception occurred during DELETE. " + nbRows + " row(s) deleted\n" +
                            ", but " + nbRowsToDeleted + " row(s) was supposed to be deleted." );
                }
                connection.commit();
                return nbRows;
            }
        }
    }

    /**
     * Use this method when you do not know how many rows will be deleted.
     */
    public static int exeDelete( String query, Connection connection )
            throws SQLException {
        return exeDelete( query, connection, -1 );
    }

    public static int deleteAll( Object object, Connection connection )
            throws SQLException {
        String query = "DELETE FROM " + QueryUtil.getTableName( object.getClass() );
        return exeDelete( query, connection );
    }

    public static int deleteAll( Object object, DatabaseConnector databaseConnector )
            throws SQLException, ClassNotFoundException {
        int nbRows;
        try ( Connection connection = databaseConnector.getConnection() ) {
            nbRows = deleteAll( object, connection );
        }
        return nbRows;
    }

    // delete by id primary_key (set ColumnAnnotation.primarykey = true)
    public static int deleteById( Object object, int pkValue, Connection connection )
            throws SQLException {
        Class<?> clazz = object.getClass();
        String tableName = QueryUtil.getTableName( clazz );
        Field pkField = QueryUtil.getPrimaryKeyField( clazz );
        if ( pkField == null ) {
            throw new GenericDaoException( "Cannot perform delete query 'coz there is no PRIMARY KEY in the relation \"" + tableName + "\"." );
        }
        String pkColumnName = QueryUtil.getColumnName( pkField ),
                query = "DELETE FROM " + tableName + " WHERE " + pkColumnName + " = " + pkValue;
        return exeDelete( query, connection, 1 );
    }

    public static int deleteById( Object object, int pkValue, DatabaseConnector databaseConnector )
            throws SQLException, ClassNotFoundException {
        int nbRows;
        try ( Connection connection = databaseConnector.getConnection() ) {
            nbRows = deleteById( object, pkValue, connection );
        }
        return nbRows;
    }

    // delete using a condition ==
    // delete using a condition <
    // delete using a condition >
    // delete using a condition LIKE
}
