package de.devicez.server.console.command;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import de.devicez.server.DeviceZServerApplication;
import de.devicez.server.device.Device;
import de.devicez.server.device.group.DeviceGroup;
import de.devicez.server.task.Task;
import de.devicez.server.task.TaskAction;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Slf4j
public class TaskCommand extends AbstractCommandHandler {

    public TaskCommand(final DeviceZServerApplication application) {
        super(application);
    }

    @Override
    public void onCommand(final String[] args) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("create")) {
                if (args.length >= 5) {
                    final String name = args[1];
                    final TaskAction action;
                    try {
                        action = TaskAction.valueOf(args[2]);
                    } catch (final IllegalArgumentException e) {
                        log.error("Invalid action! Choose one of the following: {}", (Object[]) TaskAction.values());
                        return;
                    }

                    final String target = args[3];
                    final StringBuilder cronExpression = new StringBuilder();
                    for (int i = 4; i < args.length; i++) {
                        cronExpression.append(args[i]).append(" ");
                    }

                    getApplication().getTaskRegistry().createTask(name, cronExpression.toString(), action, target);
                    log.info("Created task {}", name);
                    return;
                }

                log.info("Usage: task create <name> <action> <target> <cron>");
                return;
            } else if (args[0].equalsIgnoreCase("list")) {
                if (args.length == 1) {
                    final Collection<Task> tasks = getApplication().getTaskRegistry().getTasks();
                    if (tasks.isEmpty()) {
                        log.error("No tasks found.");
                        return;
                    }

                    log.info("Tasks ({}):", tasks.size());

                    final String table = AsciiTable.getTable(AsciiTable.FANCY_ASCII, tasks, Arrays.asList(
                            new Column().header("ID").with(task -> task.getId().toString()),
                            new Column().header("Name").with(Task::getName)));
                    log.info("\n" + table);
                    return;
                }

                log.info("Usage: task list");
                return;
            } else if (args[0].equalsIgnoreCase("delete")) {
                if (args.length == 2) {
                    final Task task = getApplication().getTaskRegistry().getTaskByIdOrName(args[1]);
                    if (task == null) {
                        log.error("Task not found.");
                        return;
                    }

                    task.delete();
                    log.info("Deleted task {}", task.getName());
                    return;
                }

                log.info("Usage: task delete <id>");
                return;
            }
        }

        log.info("Usage: task <create/list/delete>");
    }

    @Override
    public String getDescription() {
        return "Manage device groups.";
    }
}
