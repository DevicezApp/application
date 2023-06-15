package de.devicez.server;

import de.devicez.common.application.AbstractApplication;
import de.devicez.common.application.config.ApplicationConfig;
import de.devicez.server.console.ServerConsole;
import de.devicez.server.database.DatabaseClient;
import de.devicez.server.device.DeviceRegistry;
import de.devicez.server.device.group.DeviceGroupRegistry;
import de.devicez.server.http.HTTPServer;
import de.devicez.server.networking.NetworkingServer;
import de.devicez.server.task.TaskRegistry;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

@Slf4j
public class DeviceZServerApplication extends AbstractApplication {

    private ApplicationConfig config;

    private DatabaseClient databaseClient;
    private DeviceRegistry deviceRegistry;
    private DeviceGroupRegistry deviceGroupRegistry;
    private TaskRegistry taskRegistry;
    private NetworkingServer networkingServer;
    private HTTPServer httpServer;

    private ServerConsole console;

    @Override
    public void startup() throws Exception {
        try {
            config = new ApplicationConfig(new File("config.properties"));
        } catch (final IOException e) {
            log.error("Error while reading configuration", e);
            System.exit(1);
        }

        databaseClient = new DatabaseClient(this, config.getStringOrDefault("mysql-hostname", "localhost"),
                config.getIntOrDefault("mysql-port", 3306), config.getStringOrDefault("mysql-database", "database"),
                config.getStringOrDefault("mysql-username", "username"), config.getStringOrDefault("mysql-password", "password"));

        deviceRegistry = new DeviceRegistry(this);
        deviceGroupRegistry = new DeviceGroupRegistry(this);

        taskRegistry = new TaskRegistry(this);

        networkingServer = new NetworkingServer(this, config.getIntOrDefault("networking-port", 1337));
        httpServer = new HTTPServer(config.getIntOrDefault("http-port", 8080), config.getString("api-key"));

        console = new ServerConsole(this);
        console.start();
    }

    @Override
    public void shutdown() throws Exception {
        networkingServer.close();
        httpServer.close();
    }

    public ApplicationConfig getConfig() {
        return config;
    }

    public DatabaseClient getDatabaseClient() {
        return databaseClient;
    }

    public DeviceRegistry getDeviceRegistry() {
        return deviceRegistry;
    }

    public DeviceGroupRegistry getDeviceGroupRegistry() {
        return deviceGroupRegistry;
    }

    public TaskRegistry getTaskRegistry() {
        return taskRegistry;
    }

    public NetworkingServer getNetworkingServer() {
        return networkingServer;
    }

    public HTTPServer getHttpServer() {
        return httpServer;
    }

    public ServerConsole getConsole() {
        return console;
    }
}
