package database.query;

import annotations.ColumnAnnotation;
import database.GenericDaoException;
import database.connector.DatabaseConnector;
import database.utils.QueryUtil;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GenericUpdate {
    /**
     * Update a row of the relation using the primary key attribute of the param object.
     *
     * @param object An object containing the value of the primary key, and the other values to be updated.
     * @return The string representing the update query to be executed.
     */
    public static String writeUpdateQuery( Object object )
            throws SQLException, IllegalAccessException {
        Class<?> clazz = object.getClass();
        Field pkField = QueryUtil.getPrimaryKeyField( clazz );
        if ( pkField == null ) {
            throw new GenericDaoException( "Cannot perform update query 'coz there is no PRIMARY KEY in the object." +
                    " Please, verify your annotations." );
        }
        String pkColumnName = QueryUtil.getColumnName( pkField ),
                pkColumnValue = QueryUtil.getColumnValue( pkField, object ),
                columnName, columnValue, values;

        List<String> listColumnsValues = new ArrayList<>();
        for ( Field field : QueryUtil.getColumnsFields( clazz ) ) {
            if ( field != pkField ) {
                columnName = QueryUtil.getColumnName( field );
                columnValue = QueryUtil.getColumnValue( field, object );
                listColumnsValues.add( columnName + " = " + columnValue );
            }
        }
        values = String.join( ", ", listColumnsValues );

        return "UPDATE " + QueryUtil.getTableName( clazz ) + " SET " + values +
                " WHERE " + pkColumnName + " = " + pkColumnValue;
    }

    public static int update( Object object, Connection connection )
            throws SQLException, IllegalAccessException {
        String query = writeUpdateQuery( object );
        return QueryUtil.executeUpdateQuery( query, connection, 1 );
    }

    public static int update( Object object, DatabaseConnector databaseConnector )
            throws SQLException, ClassNotFoundException, IllegalAccessException {
        try ( Connection connection = databaseConnector.getConnection() ) {
            return update( object, connection );
        }
    }
}
