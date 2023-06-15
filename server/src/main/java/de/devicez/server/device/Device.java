package de.devicez.server.device;

import de.devicez.common.application.Platform;
import de.devicez.common.util.NetworkUtil;
import de.devicez.server.DeviceZServerApplication;
import de.devicez.server.database.AbstractDatabaseSerializable;
import de.devicez.server.database.ConstructedQuery;
import de.devicez.server.database.ParameterConstructedQuerySupplier;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.sql.*;
import java.util.UUID;

@Slf4j
public class Device extends AbstractDatabaseSerializable {

    public static final ConstructedQuery SELECT_ALL = new ConstructedQuery() {
        @Override
        public String query() {
            return "SELECT * FROM devicez_devices";
        }

        @Override
        public void preparedStatement(final PreparedStatement statement) {
        }
    };

    public static final ParameterConstructedQuerySupplier<UUID> SELECT_ID = id -> new ConstructedQuery() {
        @Override
        public String query() {
            return "SELECT * FROM devicez_devices WHERE id = ?";
        }

        @Override
        public void preparedStatement(final PreparedStatement statement) throws SQLException {
            statement.setString(1, id.toString());
        }
    };

    public static final ParameterConstructedQuerySupplier<String> SELECT_NAME = name -> new ConstructedQuery() {
        @Override
        public String query() {
            return "SELECT * FROM devicez_devices WHERE name = ?";
        }

        @Override
        public void preparedStatement(final PreparedStatement statement) throws SQLException {
            statement.setString(1, name);
        }
    };

    private UUID id;
    private String name;
    private Platform platform;
    private byte[] macAddress = new byte[0];
    private Timestamp lastSeen;

    public Device(final DeviceZServerApplication application) {
        super(application);
    }

    public Device(final DeviceZServerApplication application, final UUID id, final String name, final Platform platform, final byte[] macAddress) {
        super(application);
        this.id = id;
        this.name = name;
        this.platform = platform;
        this.macAddress = macAddress;
    }

    @Override
    public ConstructedQuery constructSaveQuery() {
        return new ConstructedQuery() {
            @Override
            public String query() {
                return "INSERT INTO devicez_devices (id, name, platform) VALUES(?,?,?) ON DUPLICATE KEY UPDATE name=?,platform=?,mac_address=?,last_seen=?";
            }

            @Override
            public void preparedStatement(final PreparedStatement statement) throws SQLException {
                statement.setString(1, id.toString());
                statement.setString(2, name);
                statement.setString(3, platform.name());
                statement.setString(4, name);
                statement.setString(5, platform.name());
                statement.setBlob(6, new ByteArrayInputStream(macAddress), macAddress.length);
                statement.setTimestamp(7, lastSeen != null ? lastSeen : new Timestamp(System.currentTimeMillis()));
            }
        };
    }

    @Override
    public ConstructedQuery constructDeleteQuery() {
        return new ConstructedQuery() {
            @Override
            public String query() {
                return "DELETE FROM devicez_devices WHERE id = ?";
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
        platform = Platform.valueOf(resultSet.getString("platform"));

        final Blob macAddressBlob = resultSet.getBlob("mac_address");
        macAddress = macAddressBlob.getBytes(1, (int) macAddressBlob.length());
        macAddressBlob.free();

        lastSeen = resultSet.getTimestamp("last_seen");
    }

    public void wakeUp() {
        try {
            final InetAddress broadcastAddress = NetworkUtil.getBroadcastAddress();
            NetworkUtil.sendMagicPaket(broadcastAddress, getMacAddress());
        } catch (final IOException e) {
            log.error("Error while waking up device", e);
        }
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

    public byte[] getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(final byte[] macAddress) {
        this.macAddress = macAddress;
    }

    public Timestamp getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(final Timestamp lastSeen) {
        this.lastSeen = lastSeen;
    }
}
