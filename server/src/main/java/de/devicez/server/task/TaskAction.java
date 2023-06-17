package de.devicez.server.task;

import de.devicez.server.device.group.DeviceGroup;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

public enum TaskAction {

    GROUP_WAKE((app, task) -> {
        final WakeTaskPayload payload = task.getParsedPayload(WakeTaskPayload.class);
        final DeviceGroup group = app.getDeviceGroupRegistry().getGroupById(payload.getId());
        group.wakeUp();
    }), GROUP_SHUTDOWN((app, task) -> {
        final ShutdownAndRestartTaskPayload payload = task.getParsedPayload(ShutdownAndRestartTaskPayload.class);
        final DeviceGroup group = app.getDeviceGroupRegistry().getGroupById(payload.getId());
        group.shutdown(payload.getDelay(), payload.isForce(), payload.getMessage());
    }), GROUP_RESTART((app, task) -> {
        final ShutdownAndRestartTaskPayload payload = task.getParsedPayload(ShutdownAndRestartTaskPayload.class);
        final DeviceGroup group = app.getDeviceGroupRegistry().getGroupById(payload.getId());
        group.restart(payload.getDelay(), payload.isForce(), payload.getMessage());
    });

    private final TaskActionFunction function;

    TaskAction(final TaskActionFunction function) {
        this.function = function;
    }

    public TaskActionFunction getFunction() {
        return function;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class WakeTaskPayload {
        private UUID id;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class ShutdownAndRestartTaskPayload {
        private UUID id;
        private int delay;
        private boolean force;
        private String message;
    }
}
