package de.devicez.server.networking;

import de.devicez.common.packet.AbstractPacket;
import lombok.extern.slf4j.Slf4j;
import org.snf4j.core.handler.AbstractStreamHandler;
import org.snf4j.core.handler.SessionEvent;

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

        handlePacket((AbstractPacket) message);
    }

    @Override
    public void event(final SessionEvent event) {
        switch (event) {
            case OPENED -> server.addClient(getSession());
            case CLOSED -> server.removeClient(getSession().getId());
        }
    }

    private void handlePacket(final AbstractPacket packet) {

    }
}
