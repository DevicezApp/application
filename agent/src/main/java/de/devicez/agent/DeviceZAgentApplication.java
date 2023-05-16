package de.devicez.agent;

import de.devicez.agent.networking.NetworkingClient;
import de.devicez.common.application.AbstractApplication;
import de.devicez.common.application.config.ApplicationConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class DeviceZAgentApplication extends AbstractApplication {

    private final ApplicationConfig config = new ApplicationConfig();

    private NetworkingClient networkingClient;

    @Override
    public void startup() throws Exception {
        try {
            config.loadFromPath("config.properties");
        } catch (final IOException e) {
            log.error("Error while reading configuration", e);
            System.exit(1);
        }

        networkingClient = new NetworkingClient(config.getString("hostname"), config.getIntOrDefault("port", 1337));
    }

    @Override
    public void shutdown() throws Exception {

    }
}
