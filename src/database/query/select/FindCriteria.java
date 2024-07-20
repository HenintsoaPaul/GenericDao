package database.query.select;

import database.GenericDaoException;
import database.utils.PaginationUtil;
import database.utils.QueryUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class FindCriteria {
    public String from( Object object, List<String> criteriaColumns ) {
        Class<?> clazz = object.getClass();
        String criteria, colName, valueStr, foo;
        List<String> listCriteria = new ArrayList<>();
        try {
            for ( Field field : QueryUtil.getColumnsFields( clazz ) ) {
                colName = QueryUtil.getColumnName( field );
                if ( criteriaColumns == null || criteriaColumns.contains( colName ) ) {
                    field.setAccessible( true );
                    Object value = field.get( object );
                    valueStr = value instanceof Number ? value.toString() : "'" + value + "'";
                    criteria = colName + " = " + valueStr;
                    listCriteria.add( criteria );
                    field.setAccessible( false );
                }
            }
        } catch ( IllegalAccessException | IllegalArgumentException | GenericDaoException e ) {
            throw new RuntimeException( e );
        }
        foo = listCriteria.isEmpty() ? "" : " WHERE " + String.join( " AND ", listCriteria );
        return QueryUtil.getTableName( clazz ) + foo;
    }

    public String query( Object object, List<String> columnsNames, List<String> criteriaColumns ) {
        String columns = QueryUtil.selectColumns( columnsNames );
        return columns + " FROM " + from( object, criteriaColumns );
    }

    public String queryPagination( Object object, List<String> columnsNames, List<String> criteriaColumns, int offSet, int limit, String DB_TYPE )
            throws GenericDaoException {
        if ( offSet == 0 && limit == 0 ) { // Query without pagination
            return query( object, columnsNames, criteriaColumns );
        }
        String from = from( object, criteriaColumns );
        return PaginationUtil.paginationQuery( columnsNames, offSet, limit, DB_TYPE, from );
    }
}
