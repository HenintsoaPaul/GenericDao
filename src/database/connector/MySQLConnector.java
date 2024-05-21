package database.connector;

public class MySQLConnector extends DatabaseConnector {
    public MySQLConnector( String DB_HOST, int DB_PORT, String DB_NAME,
                           String DB_USER, String DB_PASSWORD ) {
        this.initConnector( DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD );
    }

    @Override
    protected void getDriverClass()
            throws ClassNotFoundException {
        Class.forName( "com.mysql.cj.jdbc.Driver" );
    }

    @Override
    protected String getUrl() {
        return "jdbc:mysql://%s:%s/%s".formatted( getDB_HOST(), getDB_PORT(), getDB_NAME() );
    }
}
