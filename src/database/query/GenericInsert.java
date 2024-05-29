package database.query;

import database.connector.DatabaseConnector;
import database.utils.QueryUtil;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GenericInsert {
    public static String writeInsertQuery( Object object )
            throws IllegalAccessException {
        Class<?> clazz = object.getClass();
        String tableName = QueryUtil.getTableName( clazz );
        List<String> listColNames = new ArrayList<>(), listColValues = new ArrayList<>();
        for ( Field field : clazz.getDeclaredFields() ) {
            listColNames.add( QueryUtil.getColumnName( field ) );
            listColValues.add( QueryUtil.getColumnValue( field, object ) );
        }
        String columns = String.join( ", ", listColNames ),
                values = String.join( ", ", listColValues );
        return "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ")";
    }

    public static int insert( Object object, Connection connection )
            throws IllegalAccessException, SQLException {
        String query = writeInsertQuery( object );
        return QueryUtil.executeUpdateQuery( query, connection, 1 );
    }

    public static int insert( Object object, DatabaseConnector databaseConnector )
            throws SQLException, ClassNotFoundException, IllegalAccessException {
        try ( Connection connection = databaseConnector.getConnection() ) {
            return insert( object, connection );
        }
    }
}
