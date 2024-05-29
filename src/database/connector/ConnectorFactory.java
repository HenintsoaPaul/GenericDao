package database.connector;

import database.GenericDaoException;
import database.utils.ConfigFileReader;

import java.util.Map;

public abstract class ConnectorFactory {
    private static DatabaseConnector getConnector( String DB_HOST, int DB_PORT, String DB_NAME,
                                                   String DB_USER, String DB_PASSWORD, String DB_TYPE )
            throws ClassNotFoundException, GenericDaoException {
        DatabaseConnector connector;
        switch ( DB_TYPE ) {
            case "pg" -> connector = new PgConnector( DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD );
            case "mysql" -> connector = new MySQLConnector( DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD );
            case "mssql" -> connector = new MSSQLConnector( DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD );
            default -> throw new GenericDaoException( "Unknown DB_TYPE \"" + DB_TYPE + "\"" );
        }
        return connector;
    }

    public static DatabaseConnector getConnector( String dbConfigPath )
            throws GenericDaoException {
        try {
            // Initialize the ConfigFileReader with the name of the configuration file
            ConfigFileReader configReader = new ConfigFileReader( dbConfigPath );

            // Load the properties from the configuration file
            Map<String, String> properties = configReader.loadProperties();

            // Load properties
            int dbPort = Integer.parseInt( properties.get( "db.port" ) );
            String dbType = properties.get( "db" ),
                    dbHost = properties.get( "db.host" ),
                    dbName = properties.get( "db.name" ),
                    dbUser = properties.get( "db.user" ),
                    dbPassword = properties.get( "db.password" );

            return getConnector( dbHost, dbPort, dbName, dbUser, dbPassword, dbType );
        } catch ( ClassNotFoundException e ) {
            throw new GenericDaoException( "Error loading configuration properties: " + e.getMessage() );
        }
    }
}
