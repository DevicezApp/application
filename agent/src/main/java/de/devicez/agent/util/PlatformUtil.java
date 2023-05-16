package de.devicez.agent.util;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.security.auth.module.NTSystem;
import com.sun.security.auth.module.UnixSystem;
import de.devicez.common.application.Platform;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;

@Slf4j
public class PlatformUtil {

    public static Platform determinePlatform() throws PlatformUnsupportedException {
        if (com.sun.jna.Platform.isWindows()) {
            return Platform.WINDOWS;
        } else if (com.sun.jna.Platform.isLinux()) {
            return Platform.LINUX;
        }

        throw new PlatformUnsupportedException();
    }

    public static Path getApplicationFolder() throws PlatformUnsupportedException {
        final Platform platform = determinePlatform();
        switch (platform) {
            case WINDOWS -> {
                return Path.of(System.getenv("ProgramFiles"), "DeviceZ");
            }
            case LINUX -> {
                return Path.of("/opt/devicez");
            }
        }

        throw new PlatformUnsupportedException();
    }

    public static String getHostname() throws PlatformUnsupportedException {
        final Platform platform = determinePlatform();
        switch (platform) {
            case WINDOWS -> {
                return Kernel32Util.getComputerName();
            }
            case LINUX -> {
                byte[] hostnameBuffer = new byte[4097];
                final int result = UnixCLibrary.INSTANCE.gethostname(hostnameBuffer, hostnameBuffer.length);
                if (result != 0) {
                    throw new RuntimeException("gethostname failed");
                }
                return Native.toString(hostnameBuffer);
            }
        }

        throw new PlatformUnsupportedException();
    }

    public static String getUsername() throws PlatformUnsupportedException {
        final Platform platform = determinePlatform();
        switch (platform) {
            case WINDOWS -> {
                return new NTSystem().getName();
            }
            case LINUX -> {
                return new UnixSystem().getUsername();
            }
        }

        throw new PlatformUnsupportedException();
    }

    public static boolean isDaemonUser() {
        final Platform platform = determinePlatform();
        switch (platform) {
            case WINDOWS -> {
                return getUsername().equals("SYSTEM");
            }
            case LINUX -> {
                return getUsername().equals("root");
            }
        }

        throw new PlatformUnsupportedException();
    }

    public static void shutdown(final boolean restart, final boolean force, final int delay, final String message) throws PlatformUnsupportedException, IOException {
        final Platform platform = determinePlatform();

        String command = null;
        switch (platform) {
            case WINDOWS ->
                    command = "shutdown " + (restart ? "/r " : "/s ") + (delay >= 1 ? "/t " + delay : "") + (force ? " /f " : "") + (message != null && !message.isBlank() ? "/c \"" + message + "\"" : "");
            case LINUX ->
                    command = "shutdown " + (restart ? "-r " : "-h ") + (delay < 1 ? "now" : delay) + (message != null && !message.isBlank() ? " \"" + message + "\"" : "");
        }

        if (command == null) {
            throw new PlatformUnsupportedException();
        }

        Runtime.getRuntime().exec(command);
    }

    public static void cancelShutdown(final String message) throws IOException {
        final Platform platform = determinePlatform();

        String command = null;
        switch (platform) {
            case WINDOWS ->
                    command = "shutdown /a" + (message != null && !message.isBlank() ? " /c " + "\"" + message + "\"" : "");
            case LINUX ->
                    command = "shutdown -c" + (message != null && !message.isBlank() ? " \"" + message + "\"" : "");
        }

        if (command == null) {
            throw new PlatformUnsupportedException();
        }

        Runtime.getRuntime().exec(command);
    }

    private interface UnixCLibrary extends Library {
        UnixCLibrary INSTANCE = Native.load("c", UnixCLibrary.class);

        int gethostname(byte[] hostname, int bufferSize);
    }
}
