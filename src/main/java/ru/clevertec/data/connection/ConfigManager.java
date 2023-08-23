package ru.clevertec.data.connection;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

public class ConfigManager {

    private static final String PROP_FILE = "/application.yml";
    public static final ConfigManager INSTANCE = new ConfigManager();
    private final Map<String, Object> map;


    private ConfigManager() {
        Yaml yaml = new Yaml();
        try (InputStream input = getClass().getResourceAsStream(PROP_FILE)) {
            map = yaml.load(input);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }

    public Object getProperty(String key) {
        return map.get(key);
    }
}
