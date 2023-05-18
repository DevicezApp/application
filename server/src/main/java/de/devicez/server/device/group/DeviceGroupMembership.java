package de.devicez.server.device.group;

import de.devicez.server.DeviceZServerApplication;
import de.devicez.server.database.AbstractDatabaseSerializable;
import de.devicez.server.database.BiParameterConstructedQuerySupplier;
import de.devicez.server.database.ConstructedQuery;
import de.devicez.server.database.ParameterConstructedQuerySupplier;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DeviceGroupMembership extends AbstractDatabaseSerializable {

    public static BiParameterConstructedQuerySupplier<UUID, UUID> SELECT_DEVICE_GROUP = (deviceId, groupId) -> new ConstructedQuery() {

        @Override
        public String query() {
            return "SELECT * FROM devicez_devicegroup_memberships WHERE device_id = ? AND group_id = ?";
        }

        @Override
        public void preparedStatement(final PreparedStatement statement) throws SQLException {
            statement.setString(1, deviceId.toString());
            statement.setString(2, groupId.toString());
        }
    };

    public static ParameterConstructedQuerySupplier<UUID> SELECT_GROUP = groupId -> new ConstructedQuery() {
        @Override
        public String query() {
            return "SELECT * FROM devicez_devicegroup_memberships WHERE group_id = ?";
        }

        @Override
        public void preparedStatement(final PreparedStatement statement) throws SQLException {
            statement.setString(1, groupId.toString());
        }
    };

    private UUID id;
    private UUID deviceId;
    private UUID groupId;

    public DeviceGroupMembership(final DeviceZServerApplication application) {
        super(application);
    }

    public DeviceGroupMembership(final DeviceZServerApplication application, final UUID id, final UUID deviceId, final UUID groupId) {
        super(application);
        this.id = id;
        this.deviceId = deviceId;
        this.groupId = groupId;
    }

    @Override
    public ConstructedQuery constructSaveQuery() {
        return new ConstructedQuery() {
            @Override
            public String query() {
                return "INSERT INTO devicez_devicegroup_memberships (id, device_id, group_id) VALUES(?,?,?)";
            }

            @Override
            public void preparedStatement(final PreparedStatement statement) throws SQLException {
                statement.setString(1, id.toString());
                statement.setString(2, deviceId.toString());
                statement.setString(3, groupId.toString());
            }
        };
    }

    @Override
    public ConstructedQuery constructDeleteQuery() {
        return new ConstructedQuery() {
            @Override
            public String query() {
                return "DELETE FROM devicez_devicegroup_memberships WHERE id = ?";
            }

            @Override
            public void preparedStatement(PreparedStatement statement) throws SQLException {
                statement.setString(1, id.toString());
            }
        };
    }

    @Override
    public void deserialize(final ResultSet resultSet) throws SQLException {
        id = UUID.fromString(resultSet.getString("id"));
        deviceId = UUID.fromString(resultSet.getString("device_id"));
        groupId = UUID.fromString(resultSet.getString("group_id"));
    }

    public UUID getId() {
        return id;
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public UUID getGroupId() {
        return groupId;
    }
}
