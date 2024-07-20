package database.utils;

import java.util.Map;

public abstract class PropertiesUtil {
    public static Map<String, String> getProperties( String configFilePath ) {
        ConfigFileReader configReader = new ConfigFileReader( configFilePath );
        return configReader.loadProperties();
    }

    public static String getDbType( String configFilePath ) {
        Map<String, String> properties = getProperties( configFilePath );
        return properties.get( "db" );
    }

    public static int getLimit( String configFilePath ) {
        Map<String, String> properties = getProperties( configFilePath );
        return Integer.parseInt( properties.get( "limit" ) );
    }
}
