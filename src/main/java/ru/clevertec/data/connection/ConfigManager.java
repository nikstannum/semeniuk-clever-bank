package ru.clevertec.data.connection;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

public class ConfigManager {

    private final Map<String, Object> map;


    public ConfigManager(String propsFile) {
        Yaml yaml = new Yaml();
        try (InputStream input = getClass().getResourceAsStream(propsFile)) {
            map = yaml.load(input);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }

    public Object getProperty(String key) {
        return map.get(key);
    }
}
