package de.devicez.server.console.command;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import de.devicez.common.util.NetworkUtil;
import de.devicez.server.DeviceZServerApplication;
import de.devicez.server.device.ConnectedDevice;
import de.devicez.server.device.Device;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;

@Slf4j
public class DeviceCommand extends AbstractCommandHandler {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yy HH:mm:ss");

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
                log.info("Usage: device list <all/connected>");
            } else {
                log.info("Usage: device <list/shutdown/restart> [deviceId]");
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("list")) {
                if (args[1].equalsIgnoreCase("all")) {
                    final Collection<Device> devices = getApplication().getDeviceRegistry().getAllDevices();
                    if (devices.isEmpty()) {
                        log.info("No devices.");
                        return;
                    }

                    log.info("All devices ({}):", devices.size());

                    final String table = AsciiTable.getTable(AsciiTable.FANCY_ASCII, devices, Arrays.asList(
                            new Column().header("ID").with(client -> client.getId().toString()),
                            new Column().header("Name").with(Device::getName),
                            new Column().header("Platform").with(client -> client.getPlatform().toString()),
                            new Column().header("MAC").with(client -> NetworkUtil.formatHardwareAddress(client.getMacAddress())),
                            new Column().header("Connected").with(client -> client instanceof ConnectedDevice ? "Yes" : "No"),
                            new Column().header("Last seen").with(this::formatLastSeen)));
                    log.info("\n" + table);
                } else if (args[1].equalsIgnoreCase("connected")) {
                    final Collection<ConnectedDevice> devices = getApplication().getDeviceRegistry().getConnectedDevices();
                    if (devices.isEmpty()) {
                        log.info("No devices connected.");
                        return;
                    }

                    log.info("Connected devices ({}):", devices.size());

                    final String table = AsciiTable.getTable(AsciiTable.FANCY_ASCII, devices, Arrays.asList(
                            new Column().header("ID").with(client -> client.getId().toString()),
                            new Column().header("Name").with(Device::getName),
                            new Column().header("Platform").with(client -> client.getPlatform().toString()),
                            new Column().header("MAC").with(client -> NetworkUtil.formatHardwareAddress(client.getMacAddress()))));
                    log.info("\n" + table);
                } else {
                    log.info("Usage: device list <all/connected>");
                }
            }
        }
    }

    @Override
    public String getDescription() {
        return "Manage all available devices.";
    }

    private String formatLastSeen(final Device device) {
        return device instanceof ConnectedDevice ? "-/-" : DATE_FORMAT.format(device.getLastSeen());
    }
}
