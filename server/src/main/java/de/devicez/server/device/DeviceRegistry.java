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
    private final Map<Long, ConnectedDevice> connectedDeviceMap = new HashMap<>();

    public DeviceRegistry(final DeviceZServerApplication application) {
        this.application = application;
    }

    public void handleDeviceConnect(final UUID id, final String name, final Platform platform, final IStreamSession session) {
        final ConnectedDevice device = new ConnectedDevice(application, id, name, platform, session);
        connectedDeviceMap.put(session.getId(), device);
        log.info("Device {} connected from {}", name, session.getRemoteAddress().toString());

        // Update information in database
        application.getDatabaseClient().saveSerializable(device);
    }

    public void handleDeviceDisconnect(final long sessionId) {
        final ConnectedDevice device = connectedDeviceMap.remove(sessionId);
        device.setLastSeen(new Timestamp(System.currentTimeMillis()));
        log.info("Device {} disconnected", device.getName());

        // Update information in database
        application.getDatabaseClient().saveSerializable(device);
    }

    public void handleDeviceHeartbeat(final IStreamSession session, final HeartbeatPacket packet) {
        final ConnectedDevice device = getConnectedDeviceBySession(session);
        device.applyHeartbeat(packet);
    }

    public Device getDeviceById(final UUID id) {
        return application.getDatabaseClient().readSerializable(Device.class, "id", id);
    }

    public Collection<Device> getAllDevices() {
        final List<Device> devices = application.getDatabaseClient().readSerializableList(Device.class);
        devices.removeIf(device -> {
            for (final ConnectedDevice connectedDevice : connectedDeviceMap.values()) {
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
        return connectedDeviceMap.get(session.getId());
    }

    public Collection<ConnectedDevice> getConnectedDevices() {
        return connectedDeviceMap.values();
    }
}
