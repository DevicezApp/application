package de.devicez.agent.installer;

import com.google.common.net.InetAddresses;
import de.devicez.agent.DeviceZAgentApplication;
import de.devicez.common.application.Platform;
import de.devicez.common.application.config.ApplicationConfig;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

@Slf4j
public class AgentInstaller {

    public static void startInstallation(final DeviceZAgentApplication application) {
        if (GraphicsEnvironment.isHeadless()) {
            startCommandlineInstallation(application);
        } else {
            startGraphicalInstallation(application);
        }
    }

    private static void startGraphicalInstallation(final DeviceZAgentApplication application) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException |
                       UnsupportedLookAndFeelException e) {
            log.error("Error while setting look and feel", e);
            System.exit(1);
        }

        final InstallerFrame frame = new InstallerFrame();
        frame.setCallback((hostname, port) -> {
            try {
                install(application, hostname, port);
                frame.dispose();
            } catch (final Exception e) {
                log.error("Error while installing agent", e);
                System.exit(1);
            }
        });
    }

    private static void startCommandlineInstallation(final DeviceZAgentApplication application) {
        final Scanner scanner = new Scanner(System.in);

        String hostname = null;
        int port = -1;

        while (hostname == null) {
            log.info("Enter server address (IPv4):");
            final String serverAddress = scanner.nextLine();
            if (!InetAddresses.isInetAddress(serverAddress)) {
                log.info("Invalid server address!");
                continue;
            }

            hostname = serverAddress;
        }

        while (port == -1) {
            log.info("Enter server port:");
            final int serverPort = scanner.nextInt();
            if (serverPort < 0 || serverPort > 65536) {
                log.info("Invalid server port! (0 - 65536)");
                continue;
            }

            port = serverPort;
        }

        try {
            install(application, hostname, port);
        } catch (final Exception e) {
            log.error("Error while installing agent", e);
            System.exit(1);
        }
    }

    private static void install(final DeviceZAgentApplication application, final String hostname, final int port) throws IOException, URISyntaxException, InterruptedException {
        // Update configuration with entered data
        final ApplicationConfig config = application.getConfig();
        config.setString("hostname", hostname);
        config.setInt("port", port);
        config.save();

        // Copy executable into application folder
        final File executable = new File(AgentInstaller.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        final Path targetPath = new File(application.getApplicationFolder(), "DeviceZAgent.jar").toPath();
        Files.copy(executable.toPath(), targetPath);

        // Platform specific installation
        switch (application.getPlatform()) {
            case WINDOWS -> {
                // Copy WinSW executable
                try (final InputStream inputStream = AgentInstaller.class.getResource("/DeviceZService.exe").openStream()) {
                    Files.copy(inputStream, new File(application.getApplicationFolder(), "DeviceZService.exe").toPath());
                }

                // Copy WinSW configuration
                try (final InputStream inputStream = AgentInstaller.class.getResource("/DeviceZService.xml").openStream()) {
                    Files.copy(inputStream, new File(application.getApplicationFolder(), "DeviceZService.xml").toPath());
                }

                // Register service
                final Process installProcess = new ProcessBuilder("cmd", "/c", "DeviceZService.exe", "install")
                        .directory(application.getApplicationFolder())
                        .start();

                installProcess.waitFor();

                // Run service
                new ProcessBuilder("cmd", "/c", "DeviceZService.exe", "start")
                        .directory(application.getApplicationFolder())
                        .start();
            }
            case LINUX -> {
                // TODO using systemctl / init.d
            }
        }
    }
}
