package ru.clevertec.data.connection;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

/**
 * Manager for reading properties from a properties file
 */
public class ConfigManager {

    private final Map<String, Object> map;


    public ConfigManager(String propsFile) {
        Yaml yaml = new Yaml();
        try (InputStream input = getClass().getResourceAsStream(propsFile)) {
            map = yaml.load(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Object getProperty(String key) {
        return map.get(key);
    }
}
