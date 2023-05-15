package de.devicez.server.networking;

import org.snf4j.core.session.IStreamSession;

public final class Client {

    private final IStreamSession session;

    public Client(final IStreamSession session) {
        this.session = session;
    }
}
