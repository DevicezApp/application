package de.devicez.agent.networking.packet;

import de.devicez.agent.networking.NetworkingClient;
import org.snf4j.core.session.IStreamSession;

public abstract class AbstractPacketHandler<T> {

    private final NetworkingClient client;

    protected AbstractPacketHandler(final NetworkingClient client) {
        this.client = client;
    }

    public abstract void handlePacket(final IStreamSession session, final T packet) throws Exception;

    public NetworkingClient getClient() {
        return client;
    }
}
