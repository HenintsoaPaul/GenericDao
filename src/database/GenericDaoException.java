package database;

import java.sql.SQLException;

public class GenericDaoException extends SQLException {
    public GenericDaoException( String reason ) {
        super( reason );
    }
}
