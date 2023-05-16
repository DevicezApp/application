package de.devicez.agent.util;

import de.devicez.common.application.Platform;

import java.nio.file.Path;

public class PlatformUtil {

    public static Platform determinePlatform() {
        final String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return Platform.WINDOWS;
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            return Platform.UNIX;
        }

        throw new IllegalStateException("platform unsupported");
    }

    public static Path getApplicationFolder(final Platform platform) {
        switch (platform) {
            case WINDOWS -> {
                return Path.of(System.getenv("APPDATA"), "DeviceZ");
            }
            case UNIX -> {
                return Path.of("/opt/devicez");
            }
        }

        throw new IllegalStateException("platform unsupported");
    }
}
