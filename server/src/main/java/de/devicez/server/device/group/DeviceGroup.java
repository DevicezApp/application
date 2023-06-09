package de.devicez.server.device.group;

import de.devicez.common.packet.server.ShutdownPacket;
import de.devicez.server.DeviceZServerApplication;
import de.devicez.server.database.AbstractDatabaseSerializable;
import de.devicez.server.database.ConstructedQuery;
import de.devicez.server.database.ParameterConstructedQuerySupplier;
import de.devicez.server.device.ConnectedDevice;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DeviceGroup extends AbstractDatabaseSerializable {

    public static ConstructedQuery SELECT_ALL = new ConstructedQuery() {
        @Override
        public String query() {
            return "SELECT * FROM devicez_devicegroups";
        }

        @Override
        public void preparedStatement(final PreparedStatement statement) {
        }
    };

    public static ParameterConstructedQuerySupplier<UUID> SELECT_ID = id -> new ConstructedQuery() {
        @Override
        public String query() {
            return "SELECT * FROM devicez_devicegroups WHERE id = ?";
        }

        @Override
        public void preparedStatement(final PreparedStatement statement) throws SQLException {
            statement.setString(1, id.toString());
        }
    };

    public static ParameterConstructedQuerySupplier<String> SELECT_NAME = name -> new ConstructedQuery() {
        @Override
        public String query() {
            return "SELECT * FROM devicez_devicegroups WHERE name = ?";
        }

        @Override
        public void preparedStatement(final PreparedStatement statement) throws SQLException {
            statement.setString(1, name);
        }
    };

    private UUID id;
    private String name;

    public DeviceGroup(final DeviceZServerApplication application) {
        super(application);
    }

    public DeviceGroup(final DeviceZServerApplication application, final UUID id, final String name) {
        super(application);
        this.id = id;
        this.name = name;
    }

    @Override
    public ConstructedQuery constructSaveQuery() {
        return new ConstructedQuery() {
            @Override
            public String query() {
                return "INSERT INTO devicez_devicegroups (id, name) VALUES(?,?) ON DUPLICATE KEY UPDATE name=?";
            }

            @Override
            public void preparedStatement(final PreparedStatement statement) throws SQLException {
                statement.setString(1, id.toString());
                statement.setString(2, name);
                statement.setString(3, name);
            }
        };
    }

    @Override
    public ConstructedQuery constructDeleteQuery() {
        return new ConstructedQuery() {
            @Override
            public String query() {
                return "DELETE FROM devicez_devicegroups WHERE id = ?";
            }

            @Override
            public void preparedStatement(final PreparedStatement statement) throws SQLException {
                statement.setString(1, id.toString());
            }
        };
    }

    @Override
    public void deserialize(final ResultSet resultSet) throws SQLException {
        id = UUID.fromString(resultSet.getString("id"));
        name = resultSet.getString("name");
    }

    public void delete() {
        getApplication().getDatabaseClient().delete(this);
    }

    public void shutdown(final int delay, final boolean force, final String message) {
        getApplication().getDeviceGroupRegistry().getGroupMemberDevices(getId()).forEach(device -> {
            if (device instanceof ConnectedDevice connectedDevice) {
                connectedDevice.shutdown(delay, force, message);
            }
        });
    }

    public void restart(final int delay, final boolean force, final String message) {
        getApplication().getDeviceGroupRegistry().getGroupMemberDevices(getId()).forEach(device -> {
            if (device instanceof ConnectedDevice connectedDevice) {
                connectedDevice.restart(delay, force, message);
            }
        });
    }

    public void cancelShutdown(final String message) {
        getApplication().getDeviceGroupRegistry().getGroupMemberDevices(getId()).forEach(device -> {
            if (device instanceof ConnectedDevice connectedDevice) {
                connectedDevice.cancelShutdown(message);
            }
        });
    }

    public void wakeUp() {
        getApplication().getDeviceGroupRegistry().getGroupMemberDevices(getId()).forEach(device -> {
            if (device instanceof ConnectedDevice) return;

            device.wakeUp();
        });
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
