package database.utils;

import database.GenericDaoException;

import java.util.List;

public abstract class PaginationUtil {
    public static String paginationQuery(
            List<String> columnsNames, int offSet, int limit, String DB_TYPE, String from )
            throws GenericDaoException {
        return switch ( DB_TYPE ) {
            case "pg", "mysql" -> {
                String pagination = " LIMIT " + limit + " OFFSET " + offSet,
                        select = QueryUtil.selectColumns( columnsNames );
                yield select + " FROM " + from + pagination;
            }
            case "mssql" -> {
                String rowNumber = ", ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) AS rn FROM ",
                        subQuery = QueryUtil.selectColumns( columnsNames ) + rowNumber + from;
                yield "SELECT * FROM ( " + subQuery + " ) WHERE rn > " + offSet + " AND rn <= " + ( offSet + limit );
            }
            default -> throw new GenericDaoException( "Unknown DB_TYPE " + DB_TYPE );
        };
    }
}
