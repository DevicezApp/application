package de.devicez.agent.platform;

public class PlatformUnsupportedException extends RuntimeException {

    public PlatformUnsupportedException() {
        super("platform unsupported");
    }
}
