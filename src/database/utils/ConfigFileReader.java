package database.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This class is responsible for reading configuration files from a specified directory.
 * It provides a simple way to load properties from a.properties file into a Map.
 *
 * @author Henintsoa Paul MANITRAJA
 */
public class ConfigFileReader {

    /**
     * The default folder where configuration files are stored relative to the project root.
     */
    private static final String CONFIGS_FOLDER = "configs/";

    /**
     * The correct path to the configuration file to read.
     */
    private final String configFilePathFromProjectDirectory;

    /**
     * Constructs a ConfigFileReader instance with the specified configuration file name.
     *
     * @param configFileName The name of the configuration file to read.
     */
    public ConfigFileReader( String configFileName ) {
        this.configFilePathFromProjectDirectory = CONFIGS_FOLDER + configFileName;
    }

    /**
     * Loads properties from the specified configuration file into a Map.
     *
     * @return A Map containing the loaded properties.
     */
    public Map<String, String> loadProperties() {
        Map<String, String> map = new HashMap<>();
        try ( InputStream input = new FileInputStream( this.configFilePathFromProjectDirectory ) ) {
            Properties properties = new Properties();
            properties.load( input );
            for ( Map.Entry<Object, Object> entry : properties.entrySet() ) {
                map.put( ( String ) entry.getKey(), ( String ) entry.getValue() );
            }
        } catch ( IOException e ) {
            throw new RuntimeException( "Failed to load configuration properties", e );
        }
        return map;
    }
}
