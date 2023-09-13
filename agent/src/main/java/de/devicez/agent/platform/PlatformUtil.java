package de.devicez.agent.platform;

import de.devicez.agent.DeviceZAgentApplication;
import de.devicez.common.application.Platform;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public class PlatformUtil {

    private static final Map<Platform, Function<DeviceZAgentApplication, AbstractPlatform>> platformFunctionMap = new HashMap<>();

    static {
        platformFunctionMap.put(Platform.WINDOWS, WindowsPlatform::new);
    }

    public static Platform determinePlatform() throws PlatformUnsupportedException {
        if (com.sun.jna.Platform.isWindows()) {
            return Platform.WINDOWS;
        } else if (com.sun.jna.Platform.isLinux()) {
            return Platform.LINUX;
        }

        throw new PlatformUnsupportedException();
    }

    public static AbstractPlatform createInstance(final DeviceZAgentApplication application) {
        return platformFunctionMap.get(application.getPlatformType()).apply(application);
    }
}
