package com.mycompany.kafka.governance.data.protection;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class Config {

    private static final String CONFIG_FILE = "data-protection.yml";

    public static Map<String, Object> get() {
        Yaml yaml = new Yaml();
        InputStream inputStream = Config.class
                .getClassLoader()
                .getResourceAsStream(CONFIG_FILE);
        return yaml.load(inputStream);
    }
}
