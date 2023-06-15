package de.devicez.server.task;

import de.devicez.server.device.ConnectedDevice;
import de.devicez.server.device.Device;
import de.devicez.server.device.group.DeviceGroup;

import java.util.List;
import java.util.UUID;

public enum TaskAction {

    GROUP_WAKE((app, task) -> {
        final DeviceGroup group = app.getDeviceGroupRegistry().getGroupById(UUID.fromString(task.getTarget()));
        group.wakeUp();
    }), GROUP_SHUTDOWN((app, task) -> {
        final DeviceGroup group = app.getDeviceGroupRegistry().getGroupById(UUID.fromString(task.getTarget()));
        // TODO add possibility to define these variables
        group.shutdown(1, true, "Automated shutdown");
    }), GROUP_RESTART((app, task) -> {
        final DeviceGroup group = app.getDeviceGroupRegistry().getGroupById(UUID.fromString(task.getTarget()));
        // TODO add possibility to define these variables
        group.restart(1, true, "Automated restart");
    });;

    private final TaskActionFunction function;

    TaskAction(final TaskActionFunction function) {
        this.function = function;
    }

    public TaskActionFunction getFunction() {
        return function;
    }
}
