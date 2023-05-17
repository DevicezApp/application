package de.devicez.server.console.command;

import de.devicez.server.DeviceZServerApplication;

public class ExitCommand extends AbstractCommandHandler {

    public ExitCommand(final DeviceZServerApplication application) {
        super(application);
    }

    @Override
    public void onCommand(final String[] args) {
        System.exit(0);
    }

    @Override
    public String getDescription() {
        return "Stops application.";
    }
}
