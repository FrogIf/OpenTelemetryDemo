package sch.frog.opentelemetry.config;

import java.io.IOException;

public class GlobalConfiguration {

    private static PropertySource propertySource;

    public static void init(String configFilePath) throws IOException {
        propertySource = new PropertySource(configFilePath);
    }

    public static String getProperty(String name){
        return propertySource.getProperty(name);
    }

    public static String getProperty(String name, String defaultValue){
        return propertySource.getProperty(name, defaultValue);
    }

}
