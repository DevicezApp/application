package de.devicez.common.packet;

import org.snf4j.core.session.IStreamSession;

public interface PacketHandler<T extends AbstractPacket> {

    void handle(IStreamSession session, T packet);

}
