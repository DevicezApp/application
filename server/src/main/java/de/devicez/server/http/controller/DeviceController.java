package de.devicez.server.http.controller;

import de.devicez.common.application.Platform;
import de.devicez.common.util.NetworkUtil;
import de.devicez.server.DeviceZServerApplication;
import de.devicez.server.device.ConnectedDevice;
import de.devicez.server.device.Device;
import io.javalin.Javalin;
import io.javalin.http.HandlerType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class DeviceController extends AbstractController {

    public DeviceController(final DeviceZServerApplication application, final Javalin server) {
        super(application, server);
    }

    @Override
    protected void registerHandlers(final Javalin javalin) {
        javalin.addHandler(HandlerType.GET, "/devices", context -> {
            context.json(new DeviceListResponse(getApplication().getDeviceRegistry().getAllDevices().stream().map(DeviceModel::convert).collect(Collectors.toSet())));
        });
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class DeviceListResponse extends SuccessResponse {
        private Set<DeviceModel> devices;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class DeviceModel {
        private UUID id;
        private String name;
        private Platform platform;
        private String macAddress;
        private long lastSeen;
        private boolean online;

        private static DeviceModel convert(final Device device) {
            return new DeviceModel(device.getId(), device.getName(), device.getPlatform(), NetworkUtil.formatHardwareAddress(device.getMacAddress()),
                    device.getLastSeen().getTime(), device instanceof ConnectedDevice);
        }
    }
}
