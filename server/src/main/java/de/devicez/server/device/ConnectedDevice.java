package de.devicez.server.device;

import de.devicez.common.application.Platform;
import de.devicez.common.packet.client.HeartbeatPacket;
import de.devicez.common.packet.server.ShutdownCancelPacket;
import de.devicez.common.packet.server.ShutdownPacket;
import de.devicez.common.util.NetworkUtil;
import de.devicez.server.DeviceZServerApplication;
import lombok.extern.slf4j.Slf4j;
import org.snf4j.core.session.IStreamSession;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.UUID;

@Slf4j
public class ConnectedDevice extends Device {

    private final IStreamSession session;

    public ConnectedDevice(final DeviceZServerApplication application, final UUID id, final String name,
                           final Platform platform, final byte[] macAddress, final IStreamSession session) {
        super(application, id, name, platform, macAddress);
        this.session = session;
    }

    public void applyHeartbeat(final HeartbeatPacket packet) {
        boolean update = false;

        if (!Arrays.equals(packet.getHardwareAddress(), getMacAddress())) {
            log.info("Hardware address changed for {} from {} to {}", getName(),
                    NetworkUtil.formatHardwareAddress(getMacAddress()),
                    NetworkUtil.formatHardwareAddress(packet.getHardwareAddress()));

            setMacAddress(packet.getHardwareAddress());
            update = true;
        }

        if (update) {
            getApplication().getDatabaseClient().save(this);
        }
    }

    public void shutdown(final int delay, final boolean force, final String message) {
        session.writenf(new ShutdownPacket(false, force, delay, message));
    }

    public void restart(final int delay, final boolean force, final String message) {
        session.writenf(new ShutdownPacket(true, force, delay, message));
    }

    public void cancelShutdown(final String message) {
        session.writenf(new ShutdownCancelPacket(message));
    }

    @Override
    public Timestamp getLastSeen() {
        throw new IllegalStateException("device is connected");
    }

    public IStreamSession getSession() {
        return session;
    }
}
