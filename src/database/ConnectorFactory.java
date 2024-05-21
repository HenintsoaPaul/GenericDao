package database;

import database.connector.DatabaseConnector;
import database.connector.MSSQLConnector;
import database.connector.MySQLConnector;
import database.connector.PgConnector;

public abstract class ConnectorFactory {
    public static DatabaseConnector getConnector( String DB_HOST, int DB_PORT, String DB_NAME,
                                           String DB_USER, String DB_PASSWORD, String DB_TYPE )
            throws ClassNotFoundException {
        DatabaseConnector connector;
        switch ( DB_TYPE ) {
            case "pg" -> connector = new PgConnector( DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD );
            case "mysql" -> connector = new MySQLConnector( DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD );
            case "mssql" -> connector = new MSSQLConnector( DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD );
            default -> throw new ClassNotFoundException("Unknown DB_TYPE \""+ DB_TYPE +"\"");
        }
        return connector;
    }

    // TODO
    // Get Db Connector Using Informations in .properties file
}
