package de.devicez.server.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.devicez.server.DeviceZServerApplication;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DatabaseClient {

    private final DeviceZServerApplication application;
    private final HikariDataSource dataSource;

    public DatabaseClient(final DeviceZServerApplication application, final String hostname, final int port, final String database, final String username, final String password) {
        this.application = application;
        final HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://" + hostname + ":" + port + "/" + database + "?useSSL=false");
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(20);

        this.dataSource = new HikariDataSource(config);
    }

    public <T> T query(final String query, final PreparedStatementModifier modifier, final ResultSetTransformer<T> transformer) {
        try {
            try (final Connection connection = dataSource.getConnection(); final PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                if (modifier != null) {
                    modifier.apply(preparedStatement);
                }

                try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next() ? transformer.apply(resultSet) : null;
                }
            }
        } catch (final Exception e) {
            log.error("Error while executing database query", e);
            throw new DatabaseException(e);
        }
    }

    public <T> List<T> queryList(final String query, final PreparedStatementModifier modifier, final ResultSetTransformer<T> consumer) {
        try {
            try (final Connection connection = dataSource.getConnection(); final PreparedStatement preparedStatement = connection.prepareStatement(query); final ResultSet resultSet = preparedStatement.executeQuery();) {
                if (modifier != null) {
                    modifier.apply(preparedStatement);
                }

                final List<T> list = new ArrayList<>();
                while (resultSet.next()) {
                    list.add(consumer.apply(resultSet));
                }
                return list;
            }
        } catch (final Exception e) {
            log.error("Error while executing database query", e);
            throw new DatabaseException(e);
        }
    }

    public <T extends AbstractDatabaseSerializable> T query(final Class<T> clazz, final ConstructedQuery query) throws DatabaseException {
        try {
            final AbstractDatabaseSerializable serializable = clazz.getDeclaredConstructor(DeviceZServerApplication.class).newInstance(application);
            return query(query.query(), query::preparedStatement, resultSet -> {
                serializable.deserialize(resultSet);
                return (T) serializable;
            });
        } catch (final Exception e) {
            log.error("Error while executing database query", e);
            throw new DatabaseException(e);
        }
    }

    public <T extends AbstractDatabaseSerializable> List<T> queryList(final Class<T> clazz, final ConstructedQuery query) throws DatabaseException {
        return queryList(query.query(), query::preparedStatement, resultSet -> {
            final AbstractDatabaseSerializable serializable = clazz.getDeclaredConstructor(DeviceZServerApplication.class).newInstance(application);
            serializable.deserialize(resultSet);
            return (T) serializable;
        });
    }

    public void save(final AbstractDatabaseSerializable serializable) {
        final ConstructedQuery queryConstructor = serializable.constructSaveQuery();
        prepare(queryConstructor.query(), preparedStatement -> {
            try {
                queryConstructor.preparedStatement(preparedStatement);
            } catch (final SQLException e) {
                log.error("Error while executing database query", e);
                throw new DatabaseException(e);
            }
        });
    }

    public void delete(final AbstractDatabaseSerializable serializable) {
        final ConstructedQuery queryConstructor = serializable.constructDeleteQuery();
        prepare(queryConstructor.query(), preparedStatement -> {
            try {
                queryConstructor.preparedStatement(preparedStatement);
            } catch (final SQLException e) {
                log.error("Error while executing database query", e);
                throw new DatabaseException(e);
            }
        });
    }

    public void prepare(final String query) throws DatabaseException {
        prepare(query, null);
    }

    public void prepare(final String query, final PreparedStatementModifier modifier) throws DatabaseException {
        try {
            try (final Connection connection = dataSource.getConnection(); final PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                if (modifier != null) {
                    modifier.apply(preparedStatement);
                }

                preparedStatement.execute();
            }
        } catch (final Exception e) {
            log.error("Error while executing database query", e);
            throw new DatabaseException(e);
        }
    }

    @FunctionalInterface
    public interface PreparedStatementModifier {
        void apply(PreparedStatement preparedStatement) throws Exception;
    }

    @FunctionalInterface
    public interface ResultSetTransformer<T> {
        T apply(ResultSet resultSet) throws Exception;
    }
}
