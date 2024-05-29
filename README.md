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

Let us admit that you have the following class:
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

### Insert Query

```java
import database.connector.ConnectorFactory;
import database.connector.DatabaseConnector;
import database.query.GenericInsert;
import entity.Employee;

public class Main {
  public static void main( String[] args )
          throws Exception {
    Employee emp = new Employee( 1, "henin", "01/01/2001" );
    String config = "db.properties";
    DatabaseConnector databaseConnector = ConnectorFactory.getConnector( config );

    // 1st method
    int nbRows = GenericInsert.insert( emp, databaseConnector );
    System.out.println( nbRows + " row(s) inserted." );
    
    // 2nd method
    int nbRows = GenericInsert.insert( emp, databaseConnector.getConnection() );
    System.out.println( nbRows + " row(s) inserted." );
  }
}
```

### Delete Query

```java
import database.connector.ConnectorFactory;
import database.connector.DatabaseConnector;
import database.query.GenericDelete;
import entity.Employee;

public class Main {
  public static void main( String[] args )
          throws Exception {
    String config = "db.properties";
    DatabaseConnector databaseConnector = ConnectorFactory.getConnector( config );

    // Delete by idPrimaryKey
    int nbRows = GenericDelete.deleteById( new Employee(), 1, databaseConnector );
    // int nbRows = GenericDelete.deleteById( new Employee(), 1, databaseConnector.getConnection() );
    System.out.println( nbRows + " row(s) deleted." );
    
    // Delete all
    int nbRows = GenericDelete.deleteAll( new Employee(), databaseConnector );
    // int nbRows = GenericDelete.deleteAll( new Employee(), databaseConnector.getConnection() );
    System.out.println( nbRows + " row(s) deleted." );
  }
}
```