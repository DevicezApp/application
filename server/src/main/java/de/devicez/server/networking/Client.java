package de.devicez.server.networking;

import de.devicez.common.application.Platform;
import lombok.Getter;
import org.snf4j.core.session.IStreamSession;

import java.util.UUID;

@Getter
public final class Client {

    private final IStreamSession session;
    private final UUID id;
    private final String name;
    private final Platform platform;

    public Client(final IStreamSession session, final UUID id,
                  final String name, final Platform platform) {
        this.session = session;
        this.id = id;
        this.name = name;
        this.platform = platform;
    }
}
