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
import java.util.UUID;

@Slf4j
public class DeviceCommand extends AbstractCommandHandler {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yy HH:mm:ss");

    public DeviceCommand(final DeviceZServerApplication application) {
        super(application);
    }

    @Override
    public void onCommand(final String[] args) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("list")) {
                if (args.length == 2) {
                    if (args[1].equalsIgnoreCase("all")) {
                        final Collection<Device> devices = getApplication().getDeviceRegistry().getAllDevices();
                        if (devices.isEmpty()) {
                            log.info("No devices.");
                            return;
                        }

                        log.info("All devices ({}):", devices.size());

                        final String table = AsciiTable.getTable(AsciiTable.FANCY_ASCII, devices, Arrays.asList(new Column().header("ID").with(client -> client.getId().toString()), new Column().header("Name").with(Device::getName), new Column().header("Platform").with(client -> client.getPlatform().toString()), new Column().header("MAC").with(client -> NetworkUtil.formatHardwareAddress(client.getMacAddress())), new Column().header("Connected").with(client -> client instanceof ConnectedDevice ? "Yes" : "No"), new Column().header("Last seen").with(this::formatLastSeen)));
                        log.info("\n" + table);
                        return;
                    } else if (args[1].equalsIgnoreCase("connected")) {
                        final Collection<ConnectedDevice> devices = getApplication().getDeviceRegistry().getConnectedDevices();
                        if (devices.isEmpty()) {
                            log.info("No devices connected.");
                            return;
                        }

                        log.info("Connected devices ({}):", devices.size());

                        final String table = AsciiTable.getTable(AsciiTable.FANCY_ASCII, devices, Arrays.asList(new Column().header("ID").with(client -> client.getId().toString()), new Column().header("Name").with(Device::getName), new Column().header("Platform").with(client -> client.getPlatform().toString()), new Column().header("MAC").with(client -> NetworkUtil.formatHardwareAddress(client.getMacAddress()))));
                        log.info("\n" + table);
                        return;
                    }
                }

                log.info("Usage: device list <all/connected>");
                return;
            } else if (args[0].equalsIgnoreCase("shutdown")) {
                if (args.length >= 4) {
                    UUID deviceId;
                    int delay;
                    boolean force;
                    final StringBuilder message = new StringBuilder();
                    try {
                        deviceId = UUID.fromString(args[1]);
                        delay = Integer.parseInt(args[2]);
                        force = Boolean.parseBoolean(args[3]);
                        for (int i = 4; i < args.length; i++) {
                            message.append(args[i]).append(" ");
                        }
                    } catch (final NumberFormatException e) {
                        log.error("Invalid <delay> given.");
                        return;
                    } catch (final IllegalArgumentException e) {
                        log.error("Invalid <deviceId> given.");
                        return;
                    }

                    final ConnectedDevice device = getApplication().getDeviceRegistry().getConnectedDeviceById(deviceId);
                    if (device == null) {
                        log.error("No device found with given id, or is offline.");
                        return;
                    }

                    device.shutdown(delay, force, message.toString().trim());
                    log.info("Sent shutdown command.");
                    return;
                }

                log.info("Usage: device shutdown <deviceId> <delay> <force> [message]");
                return;
            } else if (args[0].equalsIgnoreCase("restart")) {
                if (args.length >= 4) {
                    UUID deviceId;
                    int delay;
                    boolean force;
                    final StringBuilder message = new StringBuilder();
                    try {
                        deviceId = UUID.fromString(args[1]);
                        delay = Integer.parseInt(args[2]);
                        force = Boolean.parseBoolean(args[3]);
                        for (int i = 4; i < args.length; i++) {
                            message.append(args[i]).append(" ");
                        }
                    } catch (final NumberFormatException e) {
                        log.error("Invalid <delay> given.");
                        return;
                    } catch (final IllegalArgumentException e) {
                        log.error("Invalid <deviceId> given.");
                        return;
                    }

                    final ConnectedDevice device = getApplication().getDeviceRegistry().getConnectedDeviceById(deviceId);
                    if (device == null) {
                        log.error("No device found with given id, or is offline.");
                        return;
                    }

                    device.restart(delay, force, message.toString().trim());
                    log.info("Sent restart command.");
                    return;
                }

                log.info("Usage: device restart <deviceId> <delay> <force> [message]");
                return;
            } else if (args[0].equalsIgnoreCase("cancelshutdown") || args[0].equalsIgnoreCase("cancelrestart")) {
                if (args.length >= 2) {
                    UUID deviceId;
                    final StringBuilder message = new StringBuilder();
                    try {
                        deviceId = UUID.fromString(args[1]);
                        for (int i = 2; i < args.length; i++) {
                            message.append(args[i]).append(" ");
                        }
                    } catch (final IllegalArgumentException e) {
                        log.error("Invalid <deviceId> given.");
                        return;
                    }

                    final ConnectedDevice device = getApplication().getDeviceRegistry().getConnectedDeviceById(deviceId);
                    if (device == null) {
                        log.error("No device found with given id, or is offline.");
                        return;
                    }

                    device.cancelShutdown(message.toString().trim());
                    log.info("Sent shutdown/restart cancel command.");
                    return;
                }

                log.info("Usage: device cancelshutdown/cancelrestart <deviceId> [message]");
            } else if (args[0].equalsIgnoreCase("wake")) {
                if (args.length == 2) {
                    UUID deviceId;
                    try {
                        deviceId = UUID.fromString(args[1]);
                    } catch (final IllegalArgumentException e) {
                        log.error("Invalid <deviceId> given.");
                        return;
                    }

                    final Device device = getApplication().getDeviceRegistry().getDeviceById(deviceId);
                    if (device instanceof ConnectedDevice) {
                        log.error("Device is already awake.");
                        return;
                    }

                    device.wakeUp();
                    log.info("Sent wakeup magic packet.");
                    return;
                }

                log.info("Usage: device wake <deviceId>");
            }
        }

        log.info("Usage: device <list/shutdown/restart/cancelshutdown/cancelrestart/wake>");
    }

    @Override
    public String getDescription() {
        return "Manage all available devices.";
    }

    private String formatLastSeen(final Device device) {
        return device instanceof ConnectedDevice ? "-/-" : DATE_FORMAT.format(device.getLastSeen());
    }
}
