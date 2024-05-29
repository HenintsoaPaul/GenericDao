package database.query;

import database.connector.DatabaseConnector;
import database.utils.Util;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GenericInsert {
    public static String writeInsertQuery( Object object )
            throws IllegalAccessException {
        Class<?> clazz = object.getClass();
        String tableName = Util.getTableName( clazz );
        List<String> listColNames = new ArrayList<>(), listColValues = new ArrayList<>();
        for ( Field field : clazz.getDeclaredFields() ) {
            listColNames.add( Util.getColumnName( field ) );
            listColValues.add( Util.getColumnValue( field, object ) );
        }
        String columns = String.join( ", ", listColNames ),
                values = String.join( ", ", listColValues );
        return "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ")";
    }

    public static int insert( Object object, Connection connection )
            throws IllegalAccessException, SQLException {
        int nbRows;
        try ( PreparedStatement preparedStatement = connection.prepareStatement( writeInsertQuery( object ) ) ) {
            connection.setAutoCommit( false );
            nbRows = preparedStatement.executeUpdate();
            if ( nbRows > 0 ) {
                connection.commit();
            }
        }
        return nbRows;
    }

    public static int insert( Object object, DatabaseConnector databaseConnector )
            throws SQLException, ClassNotFoundException, IllegalAccessException {
        return insert( object, databaseConnector.getConnection() );
    }
}
