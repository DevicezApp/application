package de.devicez.server.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ConstructedQuery {

    String query();

    void preparedStatement(PreparedStatement statement) throws SQLException;
}
