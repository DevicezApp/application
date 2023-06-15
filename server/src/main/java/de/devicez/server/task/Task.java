package de.devicez.server.task;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import de.devicez.server.DeviceZServerApplication;
import de.devicez.server.database.AbstractDatabaseSerializable;
import de.devicez.server.database.ConstructedQuery;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Locale;
import java.util.UUID;

public class Task extends AbstractDatabaseSerializable {

    private static final CronParser PARSER = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));

    public static ConstructedQuery SELECT_ALL = new ConstructedQuery() {
        @Override
        public String query() {
            return "SELECT * FROM devicez_tasks";
        }

        @Override
        public void preparedStatement(final PreparedStatement statement) {
        }
    };

    private UUID id;
    private String name;
    private Cron cron;
    private TaskAction action;
    private String target;
    private Timestamp lastExecution;

    public Task(final DeviceZServerApplication application) {
        super(application);
    }

    public Task(final DeviceZServerApplication application, final UUID id, final String name, final String cronExpression, final TaskAction action, final String target, final Timestamp lastExecution) {
        super(application);
        this.id = id;
        this.name = name;
        this.cron = PARSER.parse(cronExpression);
        this.action = action;
        this.target = target;
        this.lastExecution = lastExecution;
    }

    @Override
    public ConstructedQuery constructSaveQuery() {
        return new ConstructedQuery() {
            @Override
            public String query() {
                return "INSERT INTO devicez_tasks (id, name, cron, action, target, last_execution) VALUES(?,?,?,?,?,?) ON DUPLICATE KEY UPDATE name=?,cron=?,action=?,target=?,last_execution=?";
            }

            @Override
            public void preparedStatement(final PreparedStatement statement) throws SQLException {
                statement.setString(1, id.toString());
                statement.setString(2, name);
                statement.setString(3, cron.asString());
                statement.setString(4, action.name());
                statement.setString(5, target);
                statement.setTimestamp(6, lastExecution);

                statement.setString(7, name);
                statement.setString(8, cron.asString());
                statement.setString(9, action.name());
                statement.setString(10, target);
                statement.setTimestamp(11, lastExecution);
            }
        };
    }

    @Override
    public ConstructedQuery constructDeleteQuery() {
        return new ConstructedQuery() {
            @Override
            public String query() {
                return "DELETE FROM devicez_tasks WHERE id = ?";
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
        cron = PARSER.parse(resultSet.getString("cron"));
        action = TaskAction.valueOf(resultSet.getString("action"));
        target = resultSet.getString("target");
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Cron getCron() {
        return cron;
    }

    public String getCronDescription(final Locale locale) {
        return CronDescriptor.instance(locale).describe(cron);
    }

    public TaskAction getAction() {
        return action;
    }

    public void setAction(final TaskAction action) {
        this.action = action;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(final String target) {
        this.target = target;
    }

    public Timestamp getLastExecution() {
        return lastExecution;
    }

    public void setLastExecution(final Timestamp lastExecution) {
        this.lastExecution = lastExecution;
    }
}
