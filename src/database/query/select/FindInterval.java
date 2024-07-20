package database.query.select;

import database.GenericDaoException;
import database.utils.PaginationUtil;
import database.utils.QueryUtil;

import java.lang.reflect.Field;
import java.util.List;

public class FindInterval {
    public String from( Object object, String fieldName,
                        Number minValue, Number maxValue )
            throws GenericDaoException {
        Class<?> clazz = object.getClass();
        String tableName = QueryUtil.getTableName( clazz ), columnName = "";
        for ( Field field : QueryUtil.getColumnsFields( clazz ) ) {
            if ( field.getName().equals( fieldName ) ) {
                columnName = QueryUtil.getColumnName( field );
                break;
            }
        }
        if ( columnName.isEmpty() ) {
            throw new GenericDaoException( fieldName + " is not a column of the relation \"" + tableName + "\"." );
        }
        return tableName + " WHERE " + columnName + " BETWEEN " + minValue + " AND " + maxValue;
    }

    public String query( Object object, List<String> columnsNames,
                         String fieldName, Number minValue, Number maxValue )
            throws GenericDaoException {
        String columns = QueryUtil.selectColumns( columnsNames );
        return columns + " FROM " + from( object, fieldName, minValue, maxValue );
    }

    public String queryPagination( Object object, List<String> columnsNames, int offSet, int limit,
                                   String DB_TYPE, String fieldName, Number minValue, Number maxValue )
            throws GenericDaoException {
        if ( offSet == 0 && limit == 0 ) { // Query without pagination
            return query( object, columnsNames, fieldName, minValue, maxValue );
        }
        String from = from( object, fieldName, minValue, maxValue );
        return PaginationUtil.paginationQuery( columnsNames, offSet, limit, DB_TYPE, from );
    }
}
