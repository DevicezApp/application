package de.devicez.common.application.config;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ApplicationConfig extends Properties {

    public void loadFromPath(final String path) throws IOException {
        final File configurationFile = new File(path);
        final Path configurationPath = configurationFile.toPath();
        if (!configurationFile.exists()) {
            Files.createFile(configurationPath);
        }

        load(new BufferedInputStream(new FileInputStream(configurationFile)));
    }

    public String getString(final String key) {
        return getProperty(key);
    }

    public int getInt(final String key) {
        return Integer.parseInt(getProperty(key));
    }

    public int getIntOrDefault(final String key, final int defaultValue) {
        return contains(key) ? getInt(key) : defaultValue;
    }
}
