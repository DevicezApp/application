package de.devicez.agent.platform;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.security.auth.module.UnixSystem;
import de.devicez.agent.DeviceZAgentApplication;
import de.devicez.agent.installer.AgentInstaller;
import de.devicez.common.application.Platform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class LinuxPlatform extends AbstractPlatform {

    private final UnixCLibrary library = Native.load("c", UnixCLibrary.class);
    private final UnixSystem system = new UnixSystem();

    protected LinuxPlatform(final DeviceZAgentApplication application) {
        super(application, Platform.LINUX);
    }

    @Override
    public void installAgent() throws IOException, InterruptedException {
        // Copy service
        try (final InputStream inputStream = AgentInstaller.class.getResource("/linux/devicez.service").openStream()) {
            Files.copy(inputStream, new File("/etc/systemd/system/devicez.service").toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        // Copy run script
        try (final InputStream inputStream = AgentInstaller.class.getResource("/linux/DeviceZAgent.sh").openStream()) {
            Files.copy(inputStream, new File(getApplication().getApplicationFolder(), "DeviceZAgent.sh").toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        final Process reloadProcess = new ProcessBuilder("systemctl", "daemon-reload").directory(getApplication().getApplicationFolder()).start();
        reloadProcess.waitFor();

        final Process enableProcess = new ProcessBuilder("systemctl", "enable", "devicez").directory(getApplication().getApplicationFolder()).start();
        enableProcess.waitFor();
    }

    @Override
    public void startAgent() throws IOException {
        new ProcessBuilder("systemctl", "start", "devicez").directory(getApplication().getApplicationFolder()).start();
    }

    @Override
    public void restartAgent() throws IOException {
        new ProcessBuilder("systemctl", "restart", "devicez").directory(getApplication().getApplicationFolder()).start();
    }

    @Override
    public Path getApplicationFolder() {
        return Path.of("/opt/devicez");
    }

    @Override
    public String getHostname() {
        byte[] hostnameBuffer = new byte[4097];
        final int result = library.gethostname(hostnameBuffer, hostnameBuffer.length);
        if (result != 0) {
            throw new RuntimeException("gethostname failed");
        }
        return Native.toString(hostnameBuffer);
    }

    @Override
    public String getUsername() {
        return system.getUsername();
    }

    @Override
    public boolean isDaemonUser() {
        return getUsername().equals("root");
    }

    @Override
    public void shutdown(final boolean restart, final boolean force, final int delay, final String message) throws IOException {
        Runtime.getRuntime().exec("shutdown " + (restart ? "-r " : "-h ") + (delay < 1 ? "now" : delay) + (message != null && !message.isBlank() ? " \"" + message + "\"" : ""));
    }

    @Override
    public void cancelShutdown(final String message) throws IOException {
        Runtime.getRuntime().exec("shutdown -c" + (message != null && !message.isBlank() ? " \"" + message + "\"" : ""));
    }

    private interface UnixCLibrary extends Library {
        int gethostname(byte[] hostname, int bufferSize);
    }
}
