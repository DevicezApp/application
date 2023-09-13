package de.devicez.agent.platform;

import de.devicez.agent.DeviceZAgentApplication;
import de.devicez.common.application.Platform;

import java.io.IOException;
import java.nio.file.Path;

public abstract class AbstractPlatform {

    private final DeviceZAgentApplication application;
    private final Platform platform;

    protected AbstractPlatform(final DeviceZAgentApplication application, final Platform platform) {
        this.application = application;
        this.platform = platform;
    }

    public abstract void installAgent() throws IOException, InterruptedException;

    public abstract void startAgent() throws IOException;

    public abstract void restartAgent() throws IOException;

    public abstract Path getApplicationFolder();

    public abstract String getHostname();

    public abstract String getUsername();

    public abstract boolean isDaemonUser();

    public abstract void shutdown(final boolean restart, final boolean force, final int delay, final String message) throws IOException;

    public abstract void cancelShutdown(final String message) throws IOException;

    public DeviceZAgentApplication getApplication() {
        return application;
    }

    public Platform getPlatform() {
        return platform;
    }
}
