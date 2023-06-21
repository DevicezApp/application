package de.devicez.server.user;

import at.favre.lib.crypto.bcrypt.BCrypt;
import de.devicez.server.DeviceZServerApplication;
import de.devicez.server.database.AbstractDatabaseSerializable;
import de.devicez.server.database.ConstructedQuery;
import lombok.extern.slf4j.Slf4j;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
public class User extends AbstractDatabaseSerializable {

    public static ConstructedQuery SELECT_ALL = new ConstructedQuery() {
        @Override
        public String query() {
            return "SELECT * FROM devicez_users";
        }

        @Override
        public void preparedStatement(final PreparedStatement statement) {
        }
    };

    private UUID id;
    private String name;
    private String email;
    private String passwordHash;
    private Timestamp lastLogin;
    private boolean active;

    private UUID sessionToken;
    private long sessionExpiresAt;

    public User(final DeviceZServerApplication application) {
        super(application);
    }

    public User(final DeviceZServerApplication application, final UUID id, final String name, final String email, final String passwordHash) {
        super(application);
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    @Override
    public ConstructedQuery constructSaveQuery() {
        return new ConstructedQuery() {
            @Override
            public String query() {
                return "INSERT INTO devicez_users (id, name, email, password, last_login, active) VALUES(?,?,?,?,?,?) ON DUPLICATE KEY UPDATE name=?,email=?,password=?,last_login=?,active=?";
            }

            @Override
            public void preparedStatement(final PreparedStatement statement) throws SQLException {
                statement.setString(1, id.toString());
                statement.setString(2, name);
                statement.setString(3, email);
                statement.setString(4, passwordHash);
                statement.setTimestamp(5, lastLogin);
                statement.setBoolean(6, active);
                statement.setString(7, name);
                statement.setString(8, email);
                statement.setString(9, passwordHash);
                statement.setTimestamp(10, lastLogin);
                statement.setBoolean(11, active);
            }
        };
    }

    @Override
    public ConstructedQuery constructDeleteQuery() {
        return new ConstructedQuery() {
            @Override
            public String query() {
                return "DELETE FROM devicez_users WHERE id = ?";
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
        email = resultSet.getString("email");
        passwordHash = resultSet.getString("password");
        lastLogin = resultSet.getTimestamp("last_login");
        active = resultSet.getBoolean("active");
    }

    public void delete() {
        getApplication().getUserRegistry().deleteUser(this);
    }

    public boolean login(final String password) {
        final BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), passwordHash);
        if (!result.verified) return false;

        sessionToken = UUID.randomUUID();
        extendSession();

        return true;
    }

    public void extendSession() {
        sessionExpiresAt = System.currentTimeMillis() + (TimeUnit.SECONDS.toMillis(getApplication().getConfig().getIntOrDefault("session-ttl", 3600)));
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }

    public boolean isActive() {
        return active;
    }

    public UUID getSessionToken() {
        return sessionToken;
    }

    public boolean hasSession() {
        return sessionToken != null;
    }

    public boolean isSessionExpired() {
        return System.currentTimeMillis() > sessionExpiresAt;
    }
}
