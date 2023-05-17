package de.devicez.server.console;

import de.devicez.server.DeviceZServerApplication;
import de.devicez.server.console.command.AbstractCommandHandler;
import de.devicez.server.console.command.DeviceCommand;
import de.devicez.server.console.command.ExitCommand;
import de.devicez.server.console.command.HelpCommand;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class ServerConsole {

    private final DeviceZServerApplication application;
    private final Map<String, AbstractCommandHandler> commandHandlerMap = new HashMap<>();

    public ServerConsole(final DeviceZServerApplication application) {
        this.application = application;
        registerCommandHandlers();
    }

    private void registerCommandHandlers() {
        commandHandlerMap.put("device", new DeviceCommand(application));
        commandHandlerMap.put("help", new HelpCommand(application));
        commandHandlerMap.put("exit", new ExitCommand(application));
    }

    public void start() {
        final Scanner scanner = new Scanner(System.in);
        while (!Thread.interrupted()) {
            final String[] userInputParts = scanner.nextLine().split(" ");
            final String command = userInputParts[0].toLowerCase();

            final AbstractCommandHandler handler = commandHandlerMap.get(command);
            if (handler == null) {
                log.error("Invalid command");
                continue;
            }

            handler.onCommand(Arrays.copyOfRange(userInputParts, 1, userInputParts.length));
        }
    }

    public Map<String, AbstractCommandHandler> getCommandHandlerMap() {
        return commandHandlerMap;
    }
}
