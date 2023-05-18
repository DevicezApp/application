package de.devicez.agent.networking;

import de.devicez.common.application.Platform;
import de.devicez.common.packet.AbstractPacket;
import de.devicez.common.packet.SessionConfig;
import de.devicez.common.packet.client.HeartbeatPacket;
import de.devicez.common.packet.client.LoginPacket;
import de.devicez.common.util.NetworkUtil;
import lombok.extern.slf4j.Slf4j;
import org.snf4j.core.handler.AbstractStreamHandler;
import org.snf4j.core.handler.SessionEvent;
import org.snf4j.core.session.ISessionConfig;

import java.io.IOException;

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
            try {
                getSession().writenf(new LoginPacket(client.getApplication().getClientId(), client.getApplication().getHostname(),
                        client.getApplication().getPlatform(), NetworkUtil.getHardwareAddress()));
            } catch (final IOException e) {
                log.error("Error while sending LoginPacket", e);
            }
        }
    }

    @Override
    public void exception(final Throwable t) {
        log.error("Error in networking", t);
    }

    @Override
    public ISessionConfig getConfig() {
        return new SessionConfig();
    }
}
