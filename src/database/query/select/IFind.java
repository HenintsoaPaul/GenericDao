package database.query.select;

import java.util.List;

public interface IFind {
    String from( Object object );

    String query( Object object, List<String> columnsNames );
}
