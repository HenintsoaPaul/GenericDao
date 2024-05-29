package database.query;

import database.connector.DatabaseConnector;
import database.utils.Util;

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
        String query = "DELETE FROM " + Util.getTableName( object.getClass() );
        return exeDelete( query, connection );
    }

    public static int deleteAll( Object object, DatabaseConnector databaseConnector )
            throws SQLException, ClassNotFoundException {
        String query = "DELETE FROM " + Util.getTableName( object.getClass() );
        return exeDelete( query, databaseConnector.getConnection() );
    }

    // delete by id
    public static int deleteById( Object object, int id, Connection connection )
            throws SQLException {
        String query = "DELETE FROM " + Util.getTableName( object.getClass() ) + " WHERE id = " + id;
        return exeDelete( query, connection );
    }

    // delete using a condition ==
    // delete using a condition <
    // delete using a condition >
    // delete using a condition LIKE

}
