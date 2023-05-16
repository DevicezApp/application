package de.devicez.common.packet.server;

import de.devicez.common.packet.AbstractPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public final class ShutdownPacket extends AbstractPacket {
    private boolean restart;
    private boolean force;
    private int delay;
    private String message;

    @Override
    public void decode(final DataInputStream stream) throws IOException {
        restart = stream.readBoolean();
        force = stream.readBoolean();
        delay = stream.readInt();
        message = stream.readUTF();
    }

    @Override
    public void encode(final DataOutputStream stream) throws IOException {
        stream.writeBoolean(restart);
        stream.writeBoolean(force);
        stream.writeInt(delay);
        stream.writeUTF(message);
    }
}
