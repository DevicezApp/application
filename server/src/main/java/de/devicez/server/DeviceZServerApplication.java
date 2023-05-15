package de.devicez.server;

import de.devicez.common.application.AbstractApplication;
import de.devicez.server.http.HTTPServer;
import de.devicez.server.networking.NetworkingServer;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@Slf4j
public class DeviceZServerApplication extends AbstractApplication {

    private final Properties properties = new Properties();

    private NetworkingServer networkingServer;
    private HTTPServer httpServer;

    @Override
    public void startup() throws Exception {
        try {
            readConfiguration();
        } catch (final IOException e) {
            log.error("Error while reading configuration", e);
            System.exit(1);
        }

        networkingServer = new NetworkingServer(Integer.parseInt((String) properties.getOrDefault("networking-port", 1337)));
        httpServer = new HTTPServer(Integer.parseInt((String) properties.getOrDefault("http-port", 8080)), properties.getProperty("api-key"));
    }

    @Override
    public void shutdown() throws Exception {
        networkingServer.close();
        httpServer.close();
    }

    private void readConfiguration() throws IOException {
        final File configurationFile = new File("config.properties");
        final Path configurationPath = configurationFile.toPath();
        if (!configurationFile.exists()) {
            Files.createFile(configurationPath);
        }

        properties.load(new BufferedInputStream(new FileInputStream(configurationFile)));
    }
}
