package de.devicez.server.console.command;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import de.devicez.common.util.NetworkUtil;
import de.devicez.server.DeviceZServerApplication;
import de.devicez.server.device.ConnectedDevice;
import de.devicez.server.device.Device;
import de.devicez.server.device.group.DeviceGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Slf4j
public class DeviceGroupCommand extends AbstractCommandHandler {

    public DeviceGroupCommand(final DeviceZServerApplication application) {
        super(application);
    }

    @Override
    public void onCommand(final String[] args) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("create")) {
                if (args.length == 2) {
                    final String name = args[1];
                    getApplication().getDeviceGroupRegistry().createGroup(name);
                    log.info("Created device group {}", name);
                    return;
                }

                log.info("Usage: devicegroup create <name>");
                return;
            } else if (args[0].equalsIgnoreCase("list")) {
                if (args.length == 1) {
                    final Collection<DeviceGroup> groups = getApplication().getDeviceGroupRegistry().getGroups();
                    if (groups.isEmpty()) {
                        log.error("No groups found.");
                        return;
                    }

                    log.info("Groups ({}):", groups.size());

                    final String table = AsciiTable.getTable(AsciiTable.FANCY_ASCII, groups, Arrays.asList(
                            new Column().header("ID").with(group -> group.getId().toString()),
                            new Column().header("Name").with(DeviceGroup::getName)));
                    log.info("\n" + table);
                    return;
                }

                log.info("Usage: devicegroup list");
                return;
            } else if (args[0].equalsIgnoreCase("listdevices")) {
                if (args.length == 2) {
                    final DeviceGroup group = getApplication().getDeviceGroupRegistry().getGroupByIdOrName(args[1]);
                    if (group == null) {
                        log.error("Group not found.");
                        return;
                    }

                    final List<Device> devices = getApplication().getDeviceGroupRegistry().getGroupMemberDevices(group.getId());

                    log.info("Devices in group {} ({}):", group.getName(), devices.size());

                    final String table = AsciiTable.getTable(AsciiTable.FANCY_ASCII, devices, Arrays.asList(
                            new Column().header("ID").with(device -> device.getId().toString()),
                            new Column().header("Name").with(Device::getName)));
                    log.info("\n" + table);
                    return;
                }

                log.info("Usage: devicegroup listdevices <groupId>");
                return;
            } else if (args[0].equalsIgnoreCase("delete")) {
                if (args.length == 2) {
                    final DeviceGroup group = getApplication().getDeviceGroupRegistry().getGroupByIdOrName(args[1]);
                    if (group == null) {
                        log.error("Group not found.");
                        return;
                    }

                    group.delete();
                    log.info("Deleted device group {}", group.getName());
                    return;
                }

                log.info("Usage: devicegroup delete <id>");
                return;
            } else if (args[0].equalsIgnoreCase("add")) {
                if (args.length == 3) {
                    final Device device = getApplication().getDeviceRegistry().getDeviceByIdOrName(args[1]);
                    if (device == null) {
                        log.error("Device not found.");
                        return;
                    }

                    final DeviceGroup group = getApplication().getDeviceGroupRegistry().getGroupByIdOrName(args[2]);
                    if (group == null) {
                        log.error("Group not found.");
                        return;
                    }

                    getApplication().getDeviceGroupRegistry().addDevice(device.getId(), group.getId());
                    log.info("Added {} to group {}", device.getName(), group.getName());
                    return;
                }

                log.info("Usage: devicegroup add <deviceId> <groupId>");
                return;
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (args.length == 3) {
                    final Device device = getApplication().getDeviceRegistry().getDeviceByIdOrName(args[1]);
                    if (device == null) {
                        log.error("Device not found.");
                        return;
                    }

                    final DeviceGroup group = getApplication().getDeviceGroupRegistry().getGroupByIdOrName(args[2]);
                    if (group == null) {
                        log.error("Group not found.");
                        return;
                    }

                    getApplication().getDeviceGroupRegistry().removeDevice(device.getId(), group.getId());
                    log.info("Removed {} from group {}", device.getName(), group.getName());
                    return;
                }

                log.info("Usage: devicegroup remove <deviceId> <groupId>");
                return;
            }
        }

        log.info("Usage: devicegroup <create/list/listdevices/delete/add/remove>");
    }

    @Override
    public String getDescription() {
        return "Manage device groups.";
    }
}
