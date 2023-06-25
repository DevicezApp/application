package de.devicez.server.http.controller;

import de.devicez.server.DeviceZServerApplication;
import io.javalin.Javalin;


public abstract class AbstractController {

    private final DeviceZServerApplication application;

    public AbstractController(final DeviceZServerApplication application, final Javalin server) {
        this.application = application;
        registerHandlers(server);
    }

    protected abstract void registerHandlers(Javalin javalin);

    public DeviceZServerApplication getApplication() {
        return application;
    }

    public static class SuccessResponse {
        private final boolean success = true;
    }

    public static class FailureResponse {
        private final boolean success = false;
    }
}
