package de.devicez.agent.networking.packet;

import de.devicez.agent.networking.NetworkingClient;
import de.devicez.agent.util.PlatformUtil;
import de.devicez.common.packet.server.ShutdownCancelPacket;
import de.devicez.common.packet.server.ShutdownPacket;
import org.snf4j.core.session.IStreamSession;

import java.io.IOException;

public class ShutdownCancelPacketHandler extends AbstractPacketHandler<ShutdownCancelPacket> {

    public ShutdownCancelPacketHandler(final NetworkingClient client) {
        super(client);
    }

    @Override
    public void handlePacket(final IStreamSession session, final ShutdownCancelPacket packet) throws IOException {
        PlatformUtil.cancelShutdown(packet.getMessage());
    }
}
