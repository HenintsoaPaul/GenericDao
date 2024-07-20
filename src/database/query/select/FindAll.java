package database.query.select;

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
}
