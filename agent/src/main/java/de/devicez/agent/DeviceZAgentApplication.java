package de.devicez.agent;

import de.devicez.agent.networking.NetworkingClient;
import de.devicez.agent.util.PlatformUtil;
import de.devicez.common.application.AbstractApplication;
import de.devicez.common.application.Platform;
import de.devicez.common.application.config.ApplicationConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
public class DeviceZAgentApplication extends AbstractApplication {

    private ApplicationConfig config;
    private NetworkingClient networkingClient;

    private Platform platform;
    private File applicationFolder;
    private UUID clientId;

    @Override
    public void startup() throws Exception {
        try {
            platform = PlatformUtil.determinePlatform();

            final Path applicationFolderPath = PlatformUtil.getApplicationFolder(platform);
            applicationFolder = applicationFolderPath.toFile();

            if (!applicationFolder.exists()) {
                Files.createDirectories(applicationFolderPath);
            }

            config = new ApplicationConfig(new File(applicationFolder, "config.properties"));
            clientId = config.getUUID("client-id");

            if (clientId == null) {
                clientId = UUID.randomUUID();
                config.setUUID("client-id", clientId);
                config.save();
            }
        } catch (final IOException e) {
            log.error("Error while reading configuration", e);
            System.exit(1);
        }

        networkingClient = new NetworkingClient(config.getString("hostname"), config.getIntOrDefault("port", 1337));
    }

    @Override
    public void shutdown() throws Exception {
        networkingClient.close();
    }

    public Platform getPlatform() {
        return platform;
    }

    public File getApplicationFolder() {
        return applicationFolder;
    }

    public UUID getClientId() {
        return clientId;
    }
}
