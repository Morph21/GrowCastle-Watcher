package com.morph.general;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class PropertiesService {

    private final static Logger logger = LoggerFactory.getLogger(PropertiesService.class);

    static {
        instance = new PropertiesService();
    }

    private static PropertiesService instance;
    private Properties properties;

    public static PropertiesService instance() {
        return instance;
    }

    private PropertiesService() {
        try {
            this.properties = new Properties();

            String path = "./application.properties";
            if (Files.exists(Path.of(path))) {
                FileInputStream file = new FileInputStream(path);
                this.properties.load(file);
                file.close();
            } else {
                this.properties.load(Main.class.getClassLoader().getResourceAsStream("application.properties"));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Properties properties() {
        return properties;
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return getProperty(key, Boolean.class, defaultValue);
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public String getString(String key, String defaultValue) {
        return getProperty(key, String.class, defaultValue);
    }

    public Long getLong(String key) {
        return getLong(key, null);
    }

    public Long getLong(String key, Long defaultValue) {
        return getProperty(key, Long.class, defaultValue);
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int getInt(String key, int defaultValue) {
        return getProperty(key, Integer.class, defaultValue);
    }

    public double getDouble(String key) {
        return getDouble(key, 0);
    }

    public double getDouble(String key, double defaultValue) {
        return getProperty(key, Double.class, defaultValue);
    }
    
    private <T> T getProperty(String key, Class<T> clazz, T defaultValue) {
        try {
            if (this.properties == null) {
                return defaultValue;
            }

            String property = this.properties.getProperty(key);

            if (property == null) {
                return defaultValue;
            }

            if (clazz.equals(String.class)) {
                return clazz.cast(property);
            }

            if (clazz.equals(Integer.class)) {
                return clazz.cast(Integer.valueOf(property));
            }

            if (clazz.equals(Boolean.class)) {
                return clazz.cast(Boolean.valueOf(property));
            }

            if (clazz.equals(Double.class)) {
                return clazz.cast(Double.valueOf(property));
            }

            return defaultValue;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return defaultValue;
        }
    }

}
