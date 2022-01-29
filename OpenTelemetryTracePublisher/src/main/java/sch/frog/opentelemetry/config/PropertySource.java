package sch.frog.opentelemetry.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertySource {
    private final Properties prop;

    public PropertySource(String configPath) throws IOException {
        prop = new Properties();

        try(
                FileInputStream fileInputStream = new FileInputStream(configPath);
                ){
            prop.load(fileInputStream);
        }
    }

    public String getProperty(String name){
        return prop.getProperty(name);
    }

    public String getProperty(String name, String defaultValue){
        return prop.getProperty(name, defaultValue);
    }

}
