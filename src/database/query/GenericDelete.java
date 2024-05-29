package database.query;

import database.connector.DatabaseConnector;
import database.utils.QueryUtil;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class GenericDelete {
    public static int exeDelete( String query, Connection connection )
            throws SQLException {
        try ( PreparedStatement preparedStatement = connection.prepareStatement( query ) ) {
            return preparedStatement.executeUpdate();
        }
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
    public static int deleteById( Object object, int idPrimaryKey, Connection connection )
            throws SQLException {
        Class<?> clazz = object.getClass();
        String tableName = QueryUtil.getTableName( clazz );
        Field pkField = QueryUtil.getPrimaryKeyField( clazz );
        if ( pkField == null ) {
            throw new SQLException( "Cannot perform delete query 'coz there is no PRIMARY KEY in the relation \"" + tableName + "\"." );
        }
        String pkColumnName = QueryUtil.getColumnName( pkField ),
                query = "DELETE FROM " + tableName + " WHERE " + pkColumnName + " = " + idPrimaryKey;
        return exeDelete( query, connection );
    }

    public static int deleteById( Object object, int idPrimaryKey, DatabaseConnector databaseConnector )
            throws SQLException, ClassNotFoundException {
        int nbRows;
        try ( Connection connection = databaseConnector.getConnection() ) {
            nbRows = deleteById( object, idPrimaryKey, connection );
        }
        return nbRows;
    }

    // delete using a condition ==
    // delete using a condition <
    // delete using a condition >
    // delete using a condition LIKE
}
