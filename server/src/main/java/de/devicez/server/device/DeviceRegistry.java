package de.devicez.server.device;

import de.devicez.common.application.Platform;
import de.devicez.common.packet.client.HeartbeatPacket;
import de.devicez.server.DeviceZServerApplication;
import lombok.extern.slf4j.Slf4j;
import org.snf4j.core.session.IStreamSession;

import java.sql.Timestamp;
import java.util.*;

@Slf4j
public class DeviceRegistry {

    private final DeviceZServerApplication application;
    private final Map<Long, ConnectedDevice> connectedDeviceSessionMap = new HashMap<>();
    private final Map<UUID, ConnectedDevice> connectedDeviceIdMap = new HashMap<>();

    public DeviceRegistry(final DeviceZServerApplication application) {
        this.application = application;
    }

    public void handleDeviceConnect(final UUID id, final String name, final Platform platform, final byte[] hardwareAddress, final IStreamSession session) {
        final ConnectedDevice newDevice = new ConnectedDevice(application, id, name, platform, hardwareAddress, session);

        // Add to id map and check if there is already a device with that id online
        final ConnectedDevice connectedDevice = connectedDeviceIdMap.put(id, newDevice);
        if (connectedDevice != null) {
            connectedDevice.getSession().dirtyClose();
            log.warn("Device {} logged in from another location", name);
        }

        // Add to session map
        connectedDeviceSessionMap.put(session.getId(), newDevice);
        log.info("Device {} connected from {}", name, session.getRemoteAddress().toString());

        // Update information in database
        application.getDatabaseClient().save(newDevice);
    }

    public void handleDeviceDisconnect(final long sessionId) {
        // Remove from session map
        final ConnectedDevice device = connectedDeviceSessionMap.remove(sessionId);
        device.setLastSeen(new Timestamp(System.currentTimeMillis()));
        log.info("Device {} disconnected", device.getName());

        // Remove from id map
        connectedDeviceIdMap.remove(device.getId());

        // Update information in database
        application.getDatabaseClient().save(device);
    }

    public void handleDeviceHeartbeat(final IStreamSession session, final HeartbeatPacket packet) {
        final ConnectedDevice device = getConnectedDeviceBySession(session);
        device.applyHeartbeat(packet);
    }

    public ConnectedDevice getConnectedDeviceByIdOrName(final String identifier) {
        try {
            return getConnectedDeviceById(UUID.fromString(identifier));
        } catch (final IllegalArgumentException e) {
            return getConnectedDeviceByName(identifier);
        }
    }

    public ConnectedDevice getConnectedDeviceById(final UUID id) {
        return connectedDeviceIdMap.get(id);
    }

    public ConnectedDevice getConnectedDeviceByName(final String name) {
        return connectedDeviceIdMap.values().stream().filter(it -> it.getName().equals(name)).findAny().orElse(null);
    }

    public Device getDeviceByIdOrName(final String identifier) {
        try {
            return getDeviceById(UUID.fromString(identifier));
        } catch (final IllegalArgumentException e) {
            return getDeviceByName(identifier);
        }
    }

    public Device getDeviceById(final UUID id) {
        final ConnectedDevice connectedDevice = getConnectedDeviceById(id);
        if (connectedDevice != null) {
            return connectedDevice;
        }

        return application.getDatabaseClient().query(Device.class, Device.SELECT_ID.apply(id));
    }

    public Device getDeviceByName(final String name) {
        final ConnectedDevice connectedDevice = getConnectedDeviceByName(name);
        if (connectedDevice != null) {
            return connectedDevice;
        }

        return application.getDatabaseClient().query(Device.class, Device.SELECT_NAME.apply(name));
    }

    public Collection<Device> getAllDevices() {
        final List<Device> devices = application.getDatabaseClient().queryList(Device.class, Device.SELECT_ALL);
        devices.removeIf(device -> {
            for (final ConnectedDevice connectedDevice : connectedDeviceSessionMap.values()) {
                if (connectedDevice.getId().equals(device.getId())) {
                    return true;
                }
            }
            return false;
        });
        devices.addAll(getConnectedDevices());
        return devices;
    }

    public ConnectedDevice getConnectedDeviceBySession(final IStreamSession session) {
        return connectedDeviceSessionMap.get(session.getId());
    }

    public Collection<ConnectedDevice> getConnectedDevices() {
        return connectedDeviceSessionMap.values();
    }
}
