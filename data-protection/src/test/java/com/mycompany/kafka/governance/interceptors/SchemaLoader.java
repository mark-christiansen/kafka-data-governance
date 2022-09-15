package com.mycompany.kafka.governance.interceptors;

import org.apache.avro.Schema;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SchemaLoader {

    private final Map<String, Schema> schemas = new HashMap<>();

    public SchemaLoader() throws IOException {
        loadSchemas();
    }

    public Schema getSchema(String name) {
        return schemas.get(name);
    }

    private void loadSchemas() throws IOException {

        ClassLoader loader = SchemaLoader.class.getClassLoader();
        URL url = loader.getResource("schemas");
        assert url != null;
        String path = url.getPath();

        Schema.Parser schemaParser = new Schema.Parser();
        for (File file : Objects.requireNonNull(new File(path).listFiles())) {
            if (file.isFile() && file.getName().endsWith(".avsc")) {

                try (InputStream in = new FileInputStream(file);
                     BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line.trim());
                    }

                    String fileName = file.getName();
                    String name = fileName.substring(fileName.indexOf("-") + 1, fileName.indexOf("."));
                    Schema schema = schemaParser.parse(sb.toString());
                    schemas.put(name, schema);
                }
            }
        }
    }
}
