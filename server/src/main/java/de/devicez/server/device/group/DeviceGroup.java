package de.devicez.server.device.group;

import de.devicez.server.DeviceZServerApplication;
import de.devicez.server.database.AbstractDatabaseSerializable;
import de.devicez.server.database.ConstructedQuery;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DeviceGroup extends AbstractDatabaseSerializable {

    private UUID id;
    private String name;

    public DeviceGroup(final DeviceZServerApplication application) {
        super(application);
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
                statement.setString(1, name);
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
            public void preparedStatement(PreparedStatement statement) throws SQLException {
                statement.setString(1, id.toString());
            }
        };
    }

    @Override
    public void deserialize(final ResultSet resultSet) throws SQLException {
        id = UUID.fromString(resultSet.getString("id"));
        name = resultSet.getString("name");
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
