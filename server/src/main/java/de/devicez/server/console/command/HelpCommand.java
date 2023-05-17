package de.devicez.server.console.command;

import de.devicez.server.DeviceZServerApplication;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelpCommand extends AbstractCommandHandler {

    public HelpCommand(final DeviceZServerApplication application) {
        super(application);
    }

    @Override
    public void onCommand(final String[] args) {
        log.info("All commands:");
        log.info("");
        getApplication().getConsole().getCommandHandlerMap().forEach((command, handler) -> {
            log.info(command + " – " + handler.getDescription());
        });
    }

    @Override
    public String getDescription() {
        return "Shows this overview.";
    }
}
