package database.connector;

public class PgConnector extends DatabaseConnector {
    public PgConnector( String DB_HOST, int DB_PORT, String DB_NAME,
                           String DB_USER, String DB_PASSWORD ) {
        this.initConnector( DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD );
    }

    @Override
    protected void getDriverClass()
            throws ClassNotFoundException {
        Class.forName( "org.postgresql.Driver" );
    }

    @Override
    protected String getUrl() {
        return "jdbc:postgresql://" + getDB_HOST() + ":" + getDB_PORT() + "/" + getDB_NAME();
    }
}
