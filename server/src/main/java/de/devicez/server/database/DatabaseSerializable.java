package de.devicez.server.database;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface DatabaseSerializable {

    QueryConstructor serialize();

    QueryConstructor constructDeserializeQuery(String column, Object value);

    void deserialize(ResultSet resultSet) throws SQLException;
}
