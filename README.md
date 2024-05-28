# GenericDao

## How to get Connection?

- Create a `configs` folder.
- Create a `.properties` file, for instance `db.properties`, in the `configs` folder.
- Add your database access configurations to that newly created file. For instance:
    ```properties
    db=pg
    db.host=localhost
    db.port=5432
    db.name=my_db_name
    db.user=my_user
    db.password=my_password
  # "db" possible values: pg = postgres | mssql = sql_server | mysql
    ```

- Add the `generic-dao.jar` to the `lib/` folder of your project.
- Add the required database drivers to the `lib/` folder.

```java
    String config = "db.properties";
    DatabaseConnector databaseConnector = ConnectorFactory.getConnector(config);
    Connection conn = databaseConnector.getConnection();
    if ( conn != null ) {
        System.out.println("Connected to the database");
        conn.close();
    } else {
        System.out.println("Failed to connect to the database");
    }
```