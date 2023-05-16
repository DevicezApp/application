package de.devicez.common.packet;

import org.snf4j.core.codec.IEncoder;
import org.snf4j.core.session.ISession;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.List;

public class PacketEncoder implements IEncoder<AbstractPacket, byte[]> {

    @Override
    public void encode(final ISession iSession, final AbstractPacket abstractPacket, final List<byte[]> out) throws Exception {
        final ByteArrayOutputStream arrayStream = new ByteArrayOutputStream();

        final DataOutputStream dataStream = new DataOutputStream(arrayStream);
        dataStream.writeUTF(abstractPacket.getClass().getCanonicalName());
        abstractPacket.encode(dataStream);
        dataStream.flush();

        out.add(arrayStream.toByteArray());
    }

    @Override
    public Class<AbstractPacket> getInboundType() {
        return AbstractPacket.class;
    }

    @Override
    public Class<byte[]> getOutboundType() {
        return byte[].class;
    }
}
