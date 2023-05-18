package de.devicez.server.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface QueryConstructor {

    String query();

    PreparedStatement statement(PreparedStatement statement) throws SQLException;
}
