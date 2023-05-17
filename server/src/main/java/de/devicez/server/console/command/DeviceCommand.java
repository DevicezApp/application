package de.devicez.server.console.command;

import de.devicez.server.DeviceZServerApplication;
import de.devicez.server.networking.Client;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

@Slf4j
public class DeviceCommand extends AbstractCommandHandler {

    public DeviceCommand(final DeviceZServerApplication application) {
        super(application);
    }

    @Override
    public void onCommand(final String[] args) {
        if (args.length == 0) {
            log.info("Usage: device <list/shutdown/restart> [deviceId]");
            return;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                final Collection<Client> clients = getApplication().getNetworkingServer().getClients();
                if (clients.isEmpty()) {
                    log.info("No clients connected.");
                    return;
                }

                log.info("Connected clients ({}):", clients.size());
                log.info("");

                for (final Client client : clients) {
                    log.info("{} ({}) running on {}", client.getName(), client.getId(), client.getPlatform());
                }
            }
        }
    }

    @Override
    public String getDescription() {
        return "Manage all connected devices.";
    }
}
