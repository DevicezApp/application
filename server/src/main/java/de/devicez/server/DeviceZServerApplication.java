package de.devicez.server;

import de.devicez.common.application.AbstractApplication;
import de.devicez.common.application.config.ApplicationConfig;
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

    private final ApplicationConfig config = new ApplicationConfig();

    private NetworkingServer networkingServer;
    private HTTPServer httpServer;

    @Override
    public void startup() throws Exception {
        try {
            config.loadFromPath("config.properties");
        } catch (final IOException e) {
            log.error("Error while reading configuration", e);
            System.exit(1);
        }

        networkingServer = new NetworkingServer(config.getIntOrDefault("networking-port", 1337));
        httpServer = new HTTPServer(config.getIntOrDefault("http-port", 8080), config.getString("api-key"));
    }

    @Override
    public void shutdown() throws Exception {
        networkingServer.close();
        httpServer.close();
    }
}
