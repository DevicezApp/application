package de.devicez.server.networking.packet;

import de.devicez.common.packet.client.HeartbeatPacket;
import de.devicez.common.packet.client.LoginPacket;
import de.devicez.server.networking.NetworkingServer;
import org.snf4j.core.session.IStreamSession;

public class HeartbeatPacketHandler extends AbstractPacketHandler<HeartbeatPacket> {

    public HeartbeatPacketHandler(final NetworkingServer server) {
        super(server);
    }

    @Override
    public void handlePacket(final IStreamSession session, final HeartbeatPacket packet) {
        getServer().getApplication().getDeviceRegistry().handleDeviceHeartbeat(session, packet);
    }
}
