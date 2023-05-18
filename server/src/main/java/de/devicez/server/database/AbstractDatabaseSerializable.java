package de.devicez.server.database;

import de.devicez.server.DeviceZServerApplication;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class AbstractDatabaseSerializable {

    private final DeviceZServerApplication application;

    public AbstractDatabaseSerializable(final DeviceZServerApplication application) {
        this.application = application;
    }

    public abstract QueryConstructor serialize();

    public abstract QueryConstructor constructDeserializeQuery(String column, Object value);

    public abstract void deserialize(ResultSet resultSet) throws SQLException;

    public DeviceZServerApplication getApplication() {
        return application;
    }
}
