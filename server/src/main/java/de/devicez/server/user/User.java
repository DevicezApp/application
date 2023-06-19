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

@Slf4j
public class User extends AbstractDatabaseSerializable {

    private UUID id;
    private String username;
    private String email;
    private String passwordHash;
    private Timestamp lastLogin;

    public User(final DeviceZServerApplication application) {
        super(application);
    }

    public User(final DeviceZServerApplication application, final UUID id, final String username, final String email, final String passwordHash) {
        super(application);
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    @Override
    public ConstructedQuery constructSaveQuery() {
        return new ConstructedQuery() {
            @Override
            public String query() {
                return "INSERT INTO devicez_users (id, username, email, password, last_login) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE username=?,email=?,password=?,last_login=?";
            }

            @Override
            public void preparedStatement(final PreparedStatement statement) throws SQLException {
                statement.setString(1, id.toString());
                statement.setString(2, username);
                statement.setString(3, email);
                statement.setString(4, passwordHash);
                statement.setTimestamp(5, lastLogin);
                statement.setString(6, username);
                statement.setString(7, email);
                statement.setString(8, passwordHash);
                statement.setTimestamp(9, lastLogin);
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
        username = resultSet.getString("username");
        email = resultSet.getString("email");
        passwordHash = resultSet.getString("password");
        lastLogin = resultSet.getTimestamp("last_login");
    }

    public void delete() {
        getApplication().getUserRegistry().deleteUser(this);
    }

    public boolean checkPassword(final String password) {
        final BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), passwordHash);
        return result.verified;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
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
}
