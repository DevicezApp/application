package de.devicez.common.packet.client;

import de.devicez.common.application.Platform;
import de.devicez.common.packet.AbstractPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class LoginPacket extends AbstractPacket {
    private UUID id;
    private String name;
    private Platform platform;

    @Override
    public void decode(final DataInputStream stream) throws IOException {
        id = new UUID(stream.readLong(), stream.readLong());
        name = stream.readUTF();
        platform = Platform.values()[stream.readShort()];
    }

    @Override
    public void encode(final DataOutputStream stream) throws IOException {
        stream.writeLong(id.getLeastSignificantBits());
        stream.writeLong(id.getMostSignificantBits());
        stream.writeUTF(name);
        stream.writeShort(platform.ordinal());
    }
}
