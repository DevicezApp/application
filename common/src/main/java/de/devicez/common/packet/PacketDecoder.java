package de.devicez.common.packet;

import org.snf4j.core.codec.IDecoder;
import org.snf4j.core.session.ISession;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.List;

public class PacketDecoder implements IDecoder<byte[], AbstractPacket> {

    @Override
    public void decode(final ISession iSession, final byte[] bytes, final List<AbstractPacket> out) throws Exception {
        final DataInputStream stream = new DataInputStream(new ByteArrayInputStream(bytes));
        final String className = stream.readUTF();

        final AbstractPacket packet = (AbstractPacket) Class.forName(className).getDeclaredConstructor().newInstance();
        packet.decode(stream);

        out.add(packet);
    }

    @Override
    public Class<byte[]> getInboundType() {
        return byte[].class;
    }

    @Override
    public Class<AbstractPacket> getOutboundType() {
        return AbstractPacket.class;
    }
}
