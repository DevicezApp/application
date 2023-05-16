package de.devicez.agent.networking;

import de.devicez.common.application.Platform;
import de.devicez.common.packet.AbstractPacket;
import de.devicez.common.packet.SessionConfig;
import de.devicez.common.packet.client.LoginPacket;
import lombok.extern.slf4j.Slf4j;
import org.snf4j.core.handler.AbstractStreamHandler;
import org.snf4j.core.handler.SessionEvent;
import org.snf4j.core.session.ISessionConfig;

import java.util.UUID;

@Slf4j
public class ClientStreamHandler extends AbstractStreamHandler {

    private final NetworkingClient client;

    public ClientStreamHandler(final NetworkingClient client) {
        this.client = client;
    }

    @Override
    public void read(final Object message) {
        if (!(message instanceof AbstractPacket)) {
            log.warn("Invalid message received from " + getSession().getName());
            return;
        }

        client.handlePacket(getSession(), (AbstractPacket) message);
    }

    @Override
    public void event(SessionEvent event) {
        if (event == SessionEvent.READY) {
            getSession().writenf(new LoginPacket(client.getApplication().getClientId(), client.getApplication().getHostname(), client.getApplication().getPlatform()));
        }
    }

    @Override
    public ISessionConfig getConfig() {
        return new SessionConfig();
    }
}
