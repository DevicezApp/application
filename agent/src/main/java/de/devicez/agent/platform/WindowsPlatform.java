package de.devicez.agent.platform;

import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.security.auth.module.NTSystem;
import de.devicez.agent.DeviceZAgentApplication;
import de.devicez.agent.installer.AgentInstaller;
import de.devicez.common.application.Platform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class WindowsPlatform extends AbstractPlatform {

    private final NTSystem system = new NTSystem();

    protected WindowsPlatform(final DeviceZAgentApplication application) {
        super(application, Platform.WINDOWS);
    }

    @Override
    public void installAgent() throws IOException, InterruptedException {
        // Copy WinSW executable
        try (final InputStream inputStream = AgentInstaller.class.getResource("/windows/DeviceZService.exe").openStream()) {
            Files.copy(inputStream, new File(getApplication().getApplicationFolder(), "DeviceZService.exe").toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        // Copy WinSW configuration
        try (final InputStream inputStream = AgentInstaller.class.getResource("/windows/DeviceZService.xml").openStream()) {
            Files.copy(inputStream, new File(getApplication().getApplicationFolder(), "DeviceZService.xml").toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        // Copy run script
        try (final InputStream inputStream = AgentInstaller.class.getResource("/windows/DeviceZAgent.bat").openStream()) {
            Files.copy(inputStream, new File(getApplication().getApplicationFolder(), "DeviceZAgent.bat").toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        // Register service
        final Process installProcess = new ProcessBuilder("cmd", "/c", "DeviceZService.exe", "install").directory(getApplication().getApplicationFolder()).start();
        installProcess.waitFor();
    }

    @Override
    public void startAgent() throws IOException {
        new ProcessBuilder("cmd", "/c", "DeviceZService.exe", "start").directory(getApplication().getApplicationFolder()).start();
    }

    @Override
    public void restartAgent() throws IOException {
        new ProcessBuilder("cmd", "/c", "DeviceZService.exe", "restart").directory(getApplication().getApplicationFolder()).start();
    }

    @Override
    public Path getApplicationFolder() {
        return Path.of(System.getenv("ProgramFiles"), "DeviceZ");
    }

    @Override
    public String getHostname() {
        return Kernel32Util.getComputerName();
    }

    @Override
    public String getUsername() {
        return system.getName();
    }

    @Override
    public boolean isDaemonUser() {
        return getUsername().equals("SYSTEM");
    }

    @Override
    public void shutdown(final boolean restart, final boolean force, final int delay, final String message) throws IOException {
        Runtime.getRuntime().exec("shutdown " + (restart ? "/r " : "/s ") + (delay >= 1 ? "/t " + delay : "") + (force ? " /f " : "") + (message != null && !message.isBlank() ? "/c \"" + message + "\"" : ""));
    }

    @Override
    public void cancelShutdown(final String message) throws IOException {
        Runtime.getRuntime().exec("shutdown /a" + (message != null && !message.isBlank() ? " /c " + "\"" + message + "\"" : ""));
    }
}
