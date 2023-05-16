package de.devicez.agent.networking;

import de.devicez.common.packet.SessionConfig;
import org.snf4j.core.handler.AbstractStreamHandler;
import org.snf4j.core.session.ISessionConfig;

public class ClientStreamHandler extends AbstractStreamHandler {

    @Override
    public void read(final Object message) {

    }

    @Override
    public ISessionConfig getConfig() {
        return new SessionConfig();
    }
}
