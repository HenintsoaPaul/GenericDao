package database.utils;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class TypeCastUtil {
    public static Object getFieldValue( Field field, ResultSet resultSet )
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

    public static void setAttributesValues( Object newInstance, ResultSet resultSet )
            throws SQLException, IllegalAccessException {
        for ( Field field : QueryUtil.getColumnsFields( newInstance.getClass() ) ) {
            field.setAccessible( true );
            field.set( newInstance, getFieldValue( field, resultSet ) );
        }
    }
}
