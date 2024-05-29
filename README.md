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
import database.connector.ConnectorFactory;
import database.connector.DatabaseConnector;
import java.sql.Connection;

public class Main {
  public static void main(String[] args) throws Exception {
    String config = "db.properties";
    DatabaseConnector databaseConnector = ConnectorFactory.getConnector(config);
    Connection conn = databaseConnector.getConnection();
    if ( conn != null ) {
      System.out.println("Connected to the database");
      conn.close();
    } else {
      System.out.println("Failed to connect to the database");
    }
  }
}
```

## Query

Let us admit that you have the following classes:

- Employee.java
```java
package entity;

import annotations.ColumnAnnotation;
import annotations.TableAnnotation;

@TableAnnotation( tableName = "emp" )
public class Employee {
    @ColumnAnnotation( columnName = "id_emp", primaryKey = true )
    int idEmployee;
    @ColumnAnnotation( columnName = "nom_emp", quoted = true )
    String employeeName;
    @ColumnAnnotation( columnName = "date_naissance", quoted = true )
    String employeeBornDate;

    public Employee() {
    }

    public Employee( int idEmployee, String employeeName, String employeeBornDate ) {
        this.idEmployee = idEmployee;
        this.employeeName = employeeName;
        this.employeeBornDate = employeeBornDate;
    }
}
```

- Main.java
```java
import java.sql.SQLException;

import database.connector.ConnectorFactory;
import database.connector.DatabaseConnector;
import database.query.GenericDelete;
import database.query.GenericInsert;
import database.query.GenericUpdate;
import entity.Employee;

public class MainApp {
    public static void main( String[] args )
            throws Exception {
        String config = "db.properties";
        DatabaseConnector databaseConnector = ConnectorFactory.getConnector( config );
        insert( databaseConnector );
        update( databaseConnector );
        delete( databaseConnector );
    }

    private static void update( DatabaseConnector databaseConnector )
            throws ClassNotFoundException, IllegalAccessException, SQLException {
        Employee heninUpd = new Employee( 1, "henintsoa", "01/01/2001" );
        int nbRows = GenericUpdate.update( heninUpd, databaseConnector );
        System.out.println( nbRows + " row(s) updated" );
    }

    public static void insert( DatabaseConnector databaseConnector )
            throws ClassNotFoundException, IllegalAccessException, SQLException {
        Employee emp1 = new Employee( 1, "henin", "01/01/2001" ),
                emp2 = new Employee( 2, "malak", "01/01/2001" );
        int nbRows = GenericInsert.insert( emp1, databaseConnector );
        System.out.println( nbRows + " row(s) inserted." );

        nbRows = GenericInsert.insert( emp2, databaseConnector );
        System.out.println( nbRows + " row(s) inserted." );
    }

    public static void delete( DatabaseConnector databaseConnector )
            throws ClassNotFoundException, IllegalAccessException, SQLException {
        Employee emp1 = new Employee( 1, "henin", "01/01/2001" ),
                emp2 = new Employee( 2, "malak", "01/01/2001" );

        int nbRows = GenericDelete.deleteAll( emp1, databaseConnector );
        // int nbRows = GenericDelete.deleteById(emp2, 1, databaseConnector);
        System.out.println( nbRows + " row(s) deleted." );
    }
}
```
