package de.devicez.server.networking.packet;

import de.devicez.common.packet.PacketHandler;
import de.devicez.common.packet.client.LoginPacket;
import org.snf4j.core.session.IStreamSession;

public class LoginPacketHandler implements PacketHandler<LoginPacket> {

    @Override
    public void handle(final IStreamSession session, final LoginPacket packet) {
        session.getAttributes().put()
    }

}
