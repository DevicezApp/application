package de.devicez.agent;

import com.google.common.net.InetAddresses;
import de.devicez.agent.installer.AgentInstaller;
import de.devicez.agent.networking.NetworkingClient;
import de.devicez.agent.util.PlatformUtil;
import de.devicez.common.application.AbstractApplication;
import de.devicez.common.application.Platform;
import de.devicez.common.application.config.ApplicationConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
public class DeviceZAgentApplication extends AbstractApplication {

    private ApplicationConfig config;
    private NetworkingClient networkingClient;

    private Platform platform;
    private File applicationFolder;

    private String hostname;
    private UUID clientId;

    @Override
    public void startup() throws Exception {
        try {
            platform = PlatformUtil.determinePlatform();

            final Path applicationFolderPath = PlatformUtil.getApplicationFolder();
            applicationFolder = applicationFolderPath.toFile();

            if (!applicationFolder.exists()) {
                Files.createDirectories(applicationFolderPath);
            }

            hostname = PlatformUtil.getHostname();
        } catch (final IllegalStateException e) {
            log.error("Error while initializing platform dependant variables", e);
            System.exit(1);
        }

        try {
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

        final String serverHostname = config.getString("hostname");
        if (serverHostname == null) {
            AgentInstaller.startInstallation(this);
            return;
        }

        // Only allow non daemon user after installation if in development
        if (!PlatformUtil.isDaemonUser() && !config.getBooleanOrDefault("dev", false)) {
            throw new IllegalStateException("not a daemon user");
        }

        final int serverPort = config.getIntOrDefault("port", 1337);
        log.info("I am {} with client id {}", hostname, clientId);
        log.info("Running as user: {}", PlatformUtil.getUsername());

        networkingClient = new NetworkingClient(this, serverHostname, serverPort);
    }

    @Override
    public void shutdown() throws Exception {
        if (networkingClient != null) {
            networkingClient.close();
        }
    }

    public ApplicationConfig getConfig() {
        return config;
    }

    public Platform getPlatform() {
        return platform;
    }

    public File getApplicationFolder() {
        return applicationFolder;
    }

    public String getHostname() {
        return hostname;
    }

    public UUID getClientId() {
        return clientId;
    }
}
