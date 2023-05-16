package de.devicez.agent.util;

public class PlatformUnsupportedException extends RuntimeException {

    public PlatformUnsupportedException() {
        super("platform unsupported");
    }
}
