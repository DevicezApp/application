package de.devicez.server.networking.packet;

import de.devicez.server.networking.NetworkingServer;
import org.snf4j.core.session.IStreamSession;

public abstract class AbstractPacketHandler<T> {

    private final NetworkingServer server;

    protected AbstractPacketHandler(final NetworkingServer server) {
        this.server = server;
    }

    public abstract void handlePacket(final IStreamSession session, final T packet);

    protected NetworkingServer getServer() {
        return server;
    }
}
