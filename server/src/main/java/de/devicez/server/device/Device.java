package de.devicez.server.device;

import de.devicez.common.application.Platform;
import de.devicez.server.database.DatabaseSerializable;
import de.devicez.server.database.QueryConstructor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

public class Device implements DatabaseSerializable {

    private UUID id;
    private String name;
    private Platform platform;
    private Timestamp lastSeen;

    public Device() {
    }

    public Device(final UUID id, final String name, final Platform platform) {
        this.id = id;
        this.name = name;
        this.platform = platform;
    }

    @Override
    public QueryConstructor serialize() {
        return new QueryConstructor() {
            @Override
            public String query() {
                return "INSERT INTO devicez_devices (id, name, platform) VALUES(?,?,?) ON DUPLICATE KEY UPDATE name=?,platform=?,last_seen=?";
            }

            @Override
            public PreparedStatement statement(final PreparedStatement statement) throws SQLException {
                statement.setString(1, id.toString());
                statement.setString(2, name);
                statement.setString(3, platform.name());
                statement.setString(4, name);
                statement.setString(5, platform.name());
                statement.setTimestamp(6, lastSeen != null ? lastSeen : new Timestamp(System.currentTimeMillis()));
                return statement;
            }
        };
    }

    @Override
    public QueryConstructor constructDeserializeQuery(final String column, final Object value) {
        return new QueryConstructor() {
            @Override
            public String query() {
                return "SELECT * FROM devicez_devices" + (column != null ? " WHERE " + column + " = ?" : "");
            }

            @Override
            public PreparedStatement statement(final PreparedStatement statement) throws SQLException {
                if (value != null) {
                    statement.setString(1, (String) value);
                }
                return statement;
            }
        };
    }

    @Override
    public void deserialize(final ResultSet resultSet) throws SQLException {
        id = UUID.fromString(resultSet.getString("id"));
        name = resultSet.getString("name");
        platform = Platform.valueOf(resultSet.getString("platform"));
        lastSeen = resultSet.getTimestamp("last_seen");
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Platform getPlatform() {
        return platform;
    }

    public Timestamp getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(final Timestamp lastSeen) {
        this.lastSeen = lastSeen;
    }
}
