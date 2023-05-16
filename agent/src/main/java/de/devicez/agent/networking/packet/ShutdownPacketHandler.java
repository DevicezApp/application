package de.devicez.agent.networking.packet;

import de.devicez.agent.networking.NetworkingClient;
import de.devicez.agent.util.PlatformUtil;
import de.devicez.common.packet.server.ShutdownPacket;
import org.snf4j.core.session.IStreamSession;

import java.io.IOException;

public class ShutdownPacketHandler extends AbstractPacketHandler<ShutdownPacket> {

    public ShutdownPacketHandler(final NetworkingClient client) {
        super(client);
    }

    @Override
    public void handlePacket(final IStreamSession session, final ShutdownPacket packet) throws IOException {
        PlatformUtil.shutdown(packet.isRestart(), packet.isForce(), packet.getDelay(), packet.getMessage());
    }
}
