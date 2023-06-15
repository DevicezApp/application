package de.devicez.server.task;

import de.devicez.server.DeviceZServerApplication;

@FunctionalInterface
public interface TaskActionFunction {

    void run(final DeviceZServerApplication application, final Task task) throws Exception;

}
