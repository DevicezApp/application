package de.devicez.common.application;

public abstract class AbstractApplication {

    public abstract void startup() throws Exception;

    public abstract void shutdown() throws Exception;
}
