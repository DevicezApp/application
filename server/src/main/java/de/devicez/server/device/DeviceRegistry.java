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

        // Check if there is already a device with that id online
        final ConnectedDevice connectedDevice = connectedDeviceIdMap.get(id);
        if (connectedDevice != null) {
            connectedDevice.getSession().quickClose();
            log.warn("Device {} logged in from another location", name);
        }

        // Add to id map
        connectedDeviceIdMap.put(id, newDevice);

        // Add to session map
        connectedDeviceSessionMap.put(session.getId(), newDevice);
        log.info("Device {} connected from {}", name, session.getRemoteAddress().toString());

        // Update information in database
        application.getDatabaseClient().saveSerializable(newDevice);
    }

    public void handleDeviceDisconnect(final long sessionId) {
        // Remove from session map
        final ConnectedDevice device = connectedDeviceSessionMap.remove(sessionId);
        device.setLastSeen(new Timestamp(System.currentTimeMillis()));
        log.info("Device {} disconnected", device.getName());

        // Remove from id map
        connectedDeviceIdMap.remove(device.getId());

        // Update information in database
        application.getDatabaseClient().saveSerializable(device);
    }

    public void handleDeviceHeartbeat(final IStreamSession session, final HeartbeatPacket packet) {
        final ConnectedDevice device = getConnectedDeviceBySession(session);
        device.applyHeartbeat(packet);
    }

    public Device getDeviceById(final UUID id) {
        final ConnectedDevice connectedDevice = connectedDeviceIdMap.get(id);
        if (connectedDevice != null) {
            return connectedDevice;
        }

        return application.getDatabaseClient().readSerializable(Device.class, "id", id);
    }

    public Collection<Device> getAllDevices() {
        final List<Device> devices = application.getDatabaseClient().readSerializableList(Device.class);
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
