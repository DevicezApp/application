package de.devicez.server.device.group;

import de.devicez.server.DeviceZServerApplication;
import de.devicez.server.device.Device;

import java.util.*;

public class DeviceGroupRegistry {

    private final DeviceZServerApplication application;

    public DeviceGroupRegistry(final DeviceZServerApplication application) {
        this.application = application;
    }

    public void addDevice(final UUID deviceId, final UUID groupId) {
        final DeviceGroupMembership query = getMembership(deviceId, groupId);
        if (query != null) {
            throw new IllegalStateException("already a member");
        }

        final DeviceGroupMembership membership = new DeviceGroupMembership(application, UUID.randomUUID(), deviceId, groupId);
        application.getDatabaseClient().save(membership);
    }

    public void removeDevice(final UUID deviceId, final UUID groupId) {
        final DeviceGroupMembership query = getMembership(deviceId, groupId);
        if (query == null) {
            throw new IllegalStateException("not a member");
        }

        application.getDatabaseClient().delete(query);
    }

    public DeviceGroup getGroupByIdOrName(final String identifier) {
        try {
            return getGroupById(UUID.fromString(identifier));
        } catch (final IllegalArgumentException e) {
            return getGroupByName(identifier);
        }
    }

    public DeviceGroup getGroupById(final UUID id) {
        return application.getDatabaseClient().query(DeviceGroup.class, DeviceGroup.SELECT_ID.apply(id));
    }

    public DeviceGroup getGroupByName(final String name) {
        return application.getDatabaseClient().query(DeviceGroup.class, DeviceGroup.SELECT_NAME.apply(name));
    }

    public List<Device> getGroupMemberDevices(final UUID id) {
        final Collection<DeviceGroupMembership> members = getGroupMembers(id);
        final List<Device> devices = new ArrayList<>();
        members.forEach(membership -> devices.add(application.getDeviceRegistry().getDeviceById(membership.getDeviceId())));
        return devices;
    }

    public List<DeviceGroupMembership> getGroupMembers(final UUID id) {
        return application.getDatabaseClient().queryList(DeviceGroupMembership.class, DeviceGroupMembership.SELECT_GROUP.apply(id));
    }

    public List<DeviceGroup> getGroups() {
        return application.getDatabaseClient().queryList(DeviceGroup.class, DeviceGroup.SELECT_ALL);
    }

    public DeviceGroupMembership getMembership(final UUID deviceId, final UUID groupId) {
        return application.getDatabaseClient().query(DeviceGroupMembership.class, DeviceGroupMembership.SELECT_DEVICE_GROUP.apply(deviceId, groupId));
    }

    public DeviceGroup createGroup(final String name) {
        final DeviceGroup group = new DeviceGroup(application, UUID.randomUUID(), name);
        application.getDatabaseClient().save(group);
        return group;
    }
}
