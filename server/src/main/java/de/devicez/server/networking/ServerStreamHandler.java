package de.devicez.server.networking;

import de.devicez.common.packet.AbstractPacket;
import de.devicez.common.packet.SessionConfig;
import lombok.extern.slf4j.Slf4j;
import org.snf4j.core.handler.AbstractStreamHandler;
import org.snf4j.core.handler.SessionEvent;
import org.snf4j.core.session.ISessionConfig;

@Slf4j
public class ServerStreamHandler extends AbstractStreamHandler {

    private final NetworkingServer server;

    public ServerStreamHandler(final NetworkingServer server) {
        this.server = server;
    }

    @Override
    public void read(final Object message) {
        if (!(message instanceof AbstractPacket)) {
            log.warn("Invalid message received from " + getSession().getName());
            return;
        }

        server.handlePacket(getSession(), (AbstractPacket) message);
    }

    @Override
    public void event(final SessionEvent event) {
        if (event == SessionEvent.CLOSED) {
            server.removeClient(getSession().getId());
        }
    }

    @Override
    public ISessionConfig getConfig() {
        return new SessionConfig();
    }
}
