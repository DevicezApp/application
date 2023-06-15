package de.devicez.server.task;

import com.cronutils.model.time.ExecutionTime;
import de.devicez.server.DeviceZServerApplication;
import lombok.extern.slf4j.Slf4j;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;

@Slf4j
public class TaskRegistry {

    private final DeviceZServerApplication application;
    private final Map<UUID, Task> taskMap = new HashMap<>();

    public TaskRegistry(final DeviceZServerApplication application) {
        this.application = application;

        // Load tasks from database
        application.getDatabaseClient().queryList(Task.class, Task.SELECT_ALL).forEach(task -> taskMap.put(task.getId(), task));

        final Timer timer = new Timer(getClass().getSimpleName());
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                checkAndRunTasks();
            }
        }, 0, 1000L);
    }

    private void checkAndRunTasks() {
        final ZonedDateTime now = ZonedDateTime.now();
        final Timestamp nowTimestamp = Timestamp.from(now.toInstant());
        nowTimestamp.setNanos(0);

        taskMap.values().forEach(task -> {
            final ZonedDateTime nextExecution = ExecutionTime.forCron(task.getCron()).lastExecution(now).get();
            final Timestamp nextExecutionTimestamp = Timestamp.from(nextExecution.toInstant());

            if (nextExecutionTimestamp.equals(nowTimestamp)) {
                runTask(task, nowTimestamp);
            }
        });
    }

    private void runTask(final Task task, final Timestamp nowTimestamp) {
        try {
            task.getAction().getFunction().run(application, task);
            log.info("Task '{}' executed successfully", task.getName());

            // Store last execution in database
            task.setLastExecution(nowTimestamp);
            application.getDatabaseClient().save(task);
        } catch (final Exception e) {
            log.error("Error while executing task ({}/{})", task.getId(), task.getName(), e);
        }
    }

    public Task createTask(final String name, final String cronExpression, final TaskAction action, final String target) {
        final Task task = new Task(application, UUID.randomUUID(), name, cronExpression, action, target, null);
        taskMap.put(task.getId(), task);
        application.getDatabaseClient().save(task);
        return task;
    }

    public Collection<Task> getTasks() {
        return taskMap.values();
    }
}
