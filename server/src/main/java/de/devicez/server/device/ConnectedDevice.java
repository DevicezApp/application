package de.devicez.server.device;

import de.devicez.common.application.Platform;
import org.snf4j.core.session.IStreamSession;

import java.sql.Timestamp;
import java.util.UUID;

public class ConnectedDevice extends Device {

    private final IStreamSession session;

    public ConnectedDevice(final UUID id, final String name, final Platform platform, final IStreamSession session) {
        super(id, name, platform);
        this.session = session;
    }

    @Override
    public Timestamp getLastSeen() {
        throw new IllegalStateException("device is connected");
    }

    public IStreamSession getSession() {
        return session;
    }
}
