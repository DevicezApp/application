package de.devicez.server.console.command;

import de.devicez.server.DeviceZServerApplication;

public abstract class AbstractCommandHandler {

    private final DeviceZServerApplication application;

    protected AbstractCommandHandler(final DeviceZServerApplication application) {
        this.application = application;
    }

    protected DeviceZServerApplication getApplication() {
        return application;
    }

    public abstract void onCommand(final String[] args);

    public abstract String getDescription();
}
