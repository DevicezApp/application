package de.devicez.server.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Slf4j
public class DatabaseClient {

    private final HikariDataSource dataSource;

    public DatabaseClient(final String hostname, final int port, final String database, final String username, final String password) {
        final HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://" + hostname + ":" + port + "/" + database + "?useSSL=false");
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(20);

        this.dataSource = new HikariDataSource(config);
    }

    public <T> T query(final String query, final StatementModifier modifier, final ResultSetTransformer<T> consumer) {
        try {
            try (final Connection connection = dataSource.getConnection(); final PreparedStatement preparedStatement = connection.prepareStatement(query); final ResultSet resultSet = preparedStatement.executeQuery();) {
                modifier.apply(preparedStatement);
                return consumer.apply(resultSet);
            }
        } catch (final SQLException e) {
            log.error("Error while executing database query", e);
            throw new DatabaseException(e);
        }
    }

    public <T> List<T> queryList(final String query, final StatementModifier modifier, final ResultSetTransformer<T> consumer) {
        try {
            try (final Connection connection = dataSource.getConnection(); final PreparedStatement preparedStatement = connection.prepareStatement(query); final ResultSet resultSet = preparedStatement.executeQuery();) {
                modifier.apply(preparedStatement);

                final List<T> list = new ArrayList<>();
                while (resultSet.next()) {
                    list.add(consumer.apply(resultSet));
                }
                return list;
            }
        } catch (final SQLException e) {
            log.error("Error while executing database query", e);
            throw new DatabaseException(e);
        }
    }

    public <T extends DatabaseSerializable> T readSerializable(final Class<T> clazz, final String column, final Object value) throws DatabaseException {
        try {
            final DatabaseSerializable serializable = clazz.getDeclaredConstructor().newInstance();
            final QueryConstructor queryConstructor = serializable.constructDeserializeQuery(column, value);

            return query(queryConstructor.query(), queryConstructor::statement, resultSet -> {
                serializable.deserialize(resultSet);
                return (T) serializable;
            });
        } catch (final NoSuchMethodException | InstantiationException | IllegalAccessException |
                       InvocationTargetException e) {
            log.error("Error while executing database query", e);
            throw new DatabaseException(e);
        }
    }

    public <T extends DatabaseSerializable> List<T> readSerializableList(final Class<T> clazz) {
        return readSerializableList(clazz, null, null);
    }

    public <T extends DatabaseSerializable> List<T> readSerializableList(final Class<T> clazz, final String column, final Object value) throws DatabaseException {
        try {
            final DatabaseSerializable serializable = clazz.getDeclaredConstructor().newInstance();
            final QueryConstructor queryConstructor = serializable.constructDeserializeQuery(column, value);

            return queryList(queryConstructor.query(), queryConstructor::statement, resultSet -> {
                serializable.deserialize(resultSet);
                return (T) serializable;
            });
        } catch (final NoSuchMethodException | InstantiationException | IllegalAccessException |
                       InvocationTargetException e) {
            log.error("Error while executing database query", e);
            throw new DatabaseException(e);
        }
    }

    public void saveSerializable(final DatabaseSerializable serializable) {
        final QueryConstructor queryConstructor = serializable.serialize();
        prepare(queryConstructor.query(), preparedStatement -> {
            try {
                return queryConstructor.statement(preparedStatement);
            } catch (final SQLException e) {
                log.error("Error while executing database query", e);
                throw new DatabaseException(e);
            }
        });
    }

    public void prepare(final String query) throws DatabaseException {
        prepare(query, null);
    }

    public void prepare(final String query, final Function<PreparedStatement, PreparedStatement> statementFunction) throws DatabaseException {
        try {
            try (final Connection connection = dataSource.getConnection(); final PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                if (statementFunction == null) {
                    preparedStatement.execute();
                    return;
                }

                final PreparedStatement newStatement = statementFunction.apply(preparedStatement);
                newStatement.execute();
            }
        } catch (final SQLException e) {
            log.error("Error while executing database query", e);
            throw new DatabaseException(e);
        }
    }

    @FunctionalInterface
    public interface StatementModifier {
        PreparedStatement apply(PreparedStatement preparedStatement) throws SQLException;
    }

    @FunctionalInterface
    public interface ResultSetTransformer<T> {
        T apply(ResultSet resultSet) throws SQLException;
    }
}
