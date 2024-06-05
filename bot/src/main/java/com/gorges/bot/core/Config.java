package com.gorges.bot.core;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Component
public class Config {

    private final Properties properties;

    private Config() {
        this.properties = new Properties();
        this.load();
    }

    private void load() {
        try (InputStream stream = Config.class.getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(stream);
        } catch (IOException e) {
            throw new IllegalStateException("Failed load config file", e);
        }

        appendSystemProperties(properties);
    }

    private void appendSystemProperties (Properties properties) {
        System.getProperties().forEach(
            (key, value) ->
                properties.setProperty(key.toString(), value.toString()));
    }

    public String get(String name) {
        return properties.getProperty(name);
    }

}
