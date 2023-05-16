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
public final class ShutdownCancelPacket extends AbstractPacket {
    private String message;

    @Override
    public void decode(final DataInputStream stream) throws IOException {
        message = stream.readUTF();
    }

    @Override
    public void encode(final DataOutputStream stream) throws IOException {
        stream.writeUTF(message);
    }
}
