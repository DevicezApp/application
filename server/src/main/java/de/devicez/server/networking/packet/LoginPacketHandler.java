package de.devicez.server.networking.packet;

import de.devicez.common.packet.client.LoginPacket;
import de.devicez.server.networking.NetworkingServer;
import org.snf4j.core.session.IStreamSession;

public class LoginPacketHandler extends AbstractPacketHandler<LoginPacket> {

    public LoginPacketHandler(final NetworkingServer server) {
        super(server);
    }

    @Override
    public void handlePacket(final IStreamSession session, final LoginPacket packet) {
        getServer().getApplication().getDeviceRegistry().handleDeviceConnect(packet.getId(), packet.getName(), packet.getPlatform(), packet.getHardwareAddress(), session);
    }
}
