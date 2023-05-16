package de.devicez.common.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class AbstractPacket {

    public abstract void decode(final DataInputStream stream) throws IOException;

    public abstract void encode(DataOutputStream stream) throws IOException;

}
