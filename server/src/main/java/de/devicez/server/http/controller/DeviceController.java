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

        javalin.addHandler(HandlerType.GET, "/device/{id}", context -> {
            final String rawId = context.pathParam("id");
            final Device device = getApplication().getDeviceRegistry().getDeviceByIdOrName(rawId);
            if (device == null) {
                context.json(new DeviceErrorResponse(DeviceError.DEVICE_NOT_FOUND));
                return;
            }

            context.json(new DeviceSuccessResponse(DeviceModel.convert(device)));
        });

        javalin.addHandler(HandlerType.GET, "/device/{id}/shutdown", context -> {
            final String rawId = context.pathParam("id");
            final Device device = getApplication().getDeviceRegistry().getDeviceByIdOrName(rawId);
            if (device == null) {
                context.json(new DeviceErrorResponse(DeviceError.DEVICE_NOT_FOUND));
                return;
            }

            if (device instanceof ConnectedDevice connectedDevice) {
                final int delay = Integer.parseInt(context.queryParam("delay"));
                final boolean force = Boolean.parseBoolean(context.queryParam("force"));
                connectedDevice.shutdown(delay, force, context.queryParam("message"));
                context.json(new SuccessResponse());
                return;
            }

            context.json(new DeviceErrorResponse(DeviceError.DEVICE_OFFLINE));
        });

        javalin.addHandler(HandlerType.GET, "/device/{id}/restart", context -> {
            final String rawId = context.pathParam("id");
            final Device device = getApplication().getDeviceRegistry().getDeviceByIdOrName(rawId);
            if (device == null) {
                context.json(new DeviceErrorResponse(DeviceError.DEVICE_NOT_FOUND));
                return;
            }

            if (device instanceof ConnectedDevice connectedDevice) {
                final int delay = Integer.parseInt(context.queryParam("delay"));
                final boolean force = Boolean.parseBoolean(context.queryParam("force"));
                connectedDevice.restart(delay, force, context.queryParam("message"));
                context.json(new SuccessResponse());
                return;
            }

            context.json(new DeviceErrorResponse(DeviceError.DEVICE_OFFLINE));
        });

        javalin.addHandler(HandlerType.GET, "/device/{id}/wakeup", context -> {
            final String rawId = context.pathParam("id");
            final Device device = getApplication().getDeviceRegistry().getDeviceByIdOrName(rawId);
            if (device == null) {
                context.json(new DeviceErrorResponse(DeviceError.DEVICE_NOT_FOUND));
                return;
            }

            if (device instanceof ConnectedDevice) {
                context.json(new DeviceErrorResponse(DeviceError.DEVICE_ONLINE));
                return;
            }

            device.wakeUp();
            context.json(new SuccessResponse());
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
    public static class DeviceSuccessResponse extends SuccessResponse {
        private DeviceModel device;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class DeviceErrorResponse extends FailureResponse {
        private DeviceError error;
    }

    public enum DeviceError {
        DEVICE_NOT_FOUND,
        DEVICE_OFFLINE,
        DEVICE_ONLINE
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
            return new DeviceModel(device.getId(), device.getName(), device.getPlatform(), NetworkUtil.formatHardwareAddress(device.getMacAddress()), device.getLastSeen().getTime(), device instanceof ConnectedDevice);
        }
    }
}
