package de.devicez.common.packet.client;

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
public class HeartbeatPacket extends AbstractPacket {
    private byte[] hardwareAddress;

    @Override
    public void decode(final DataInputStream stream) throws IOException {
        final int hardwareAddressLength = stream.readInt();
        hardwareAddress = stream.readNBytes(hardwareAddressLength);
    }

    @Override
    public void encode(final DataOutputStream stream) throws IOException {
        stream.writeInt(hardwareAddress.length);
        stream.write(hardwareAddress);
    }
}
