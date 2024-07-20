package database.query.select;

import database.GenericDaoException;
import database.utils.PaginationUtil;
import database.utils.QueryUtil;

import java.util.List;

public class FindAll {
    public String from( Object object ) {
        return QueryUtil.getTableName( object.getClass() );
    }

    public String query( Object object, List<String> columnsNames ) {
        String columns = QueryUtil.selectColumns( columnsNames ),
                tableName = QueryUtil.getTableName( object.getClass() );
        return columns + " FROM " + tableName;
    }

    public String queryPagination( Object object, List<String> columnsNames, int offSet, int limit, String DB_TYPE )
            throws GenericDaoException {
        if ( offSet == 0 && limit == 0 ) { // Query without pagination
            return query( object, columnsNames );
        }
        String from = from( object );
        return PaginationUtil.paginationQuery( columnsNames, offSet, limit, DB_TYPE, from );
    }
}
