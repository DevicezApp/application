package de.devicez.common.packet.client;

import de.devicez.common.application.Platform;
import de.devicez.common.packet.AbstractPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class LoginPacket extends AbstractPacket {
    private UUID id;
    private String name;
    private Platform platform;
}
