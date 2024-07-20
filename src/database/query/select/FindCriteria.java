package database.query.select;

import database.GenericDaoException;
import database.utils.QueryUtil;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

public class FindCriteria implements IFind {
    public String from( Object object ) {
        Class<?> clazz = object.getClass();
        List<Field> fields = QueryUtil.getColumnsFields( clazz );
        String tableName = QueryUtil.getTableName( clazz ),
                criteria = fields.stream()
                        .peek( field -> field.setAccessible( true ) )
                        .filter( field -> {
                            try {
                                return field.get( object ) != null;
                            } catch ( IllegalAccessException e ) {
                                throw new RuntimeException( e );
                            }
                        } )
                        .map( field -> {
                            try {
                                Object columnValue = field.get( object );
                                String strColumnValue = columnValue instanceof Number ?
                                        columnValue.toString() : "'" + columnValue + "'";
                                return QueryUtil.getColumnName( field ) + " = " + strColumnValue;
                            } catch ( IllegalAccessException | GenericDaoException e ) {
                                throw new RuntimeException( e );
                            }
                        } )
                        .collect( Collectors.joining( " AND " ) );
        return tableName + ( criteria.isEmpty() ? "" : " WHERE " + criteria );
    }

    public String query( Object object, List<String> columnsNames ) {
        String columns = QueryUtil.selectColumns( columnsNames );
        return columns + " FROM " + from( object );
    }
}
