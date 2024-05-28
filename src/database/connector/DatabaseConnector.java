package database.connector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class DatabaseConnector {
    String DB_NAME, DB_HOST, DB_USER, DB_PASSWORD;
    int DB_PORT;

    public void initConnector( String DB_HOST, int DB_PORT, String DB_NAME,
                               String DB_USER, String DB_PASSWORD ) {
        this.setDB_HOST( DB_HOST );
        this.setDB_PORT( DB_PORT );
        this.setDB_NAME( DB_NAME );
        this.setDB_USER( DB_USER );
        this.setDB_PASSWORD( DB_PASSWORD );
    }

    protected abstract void getDriverClass()
            throws ClassNotFoundException;

    protected abstract String getUrl();

    public Connection getConnection()
            throws ClassNotFoundException, SQLException {
        Connection conn;
        getDriverClass();
        conn = DriverManager.getConnection( getUrl(), getDB_USER(), getDB_PASSWORD() );
        return conn;
    }

    public String getDB_NAME() {
        return DB_NAME;
    }

    public void setDB_NAME( String DB_NAME ) {
        this.DB_NAME = DB_NAME;
    }

    public String getDB_HOST() {
        return DB_HOST;
    }

    public void setDB_HOST( String DB_HOST ) {
        this.DB_HOST = DB_HOST;
    }

    public String getDB_USER() {
        return DB_USER;
    }

    public void setDB_USER( String DB_USER ) {
        this.DB_USER = DB_USER;
    }

    public String getDB_PASSWORD() {
        return DB_PASSWORD;
    }

    public void setDB_PASSWORD( String DB_PASSWORD ) {
        this.DB_PASSWORD = DB_PASSWORD;
    }

    public int getDB_PORT() {
        return DB_PORT;
    }

    public void setDB_PORT( int DB_PORT ) {
        this.DB_PORT = DB_PORT;
    }
}
