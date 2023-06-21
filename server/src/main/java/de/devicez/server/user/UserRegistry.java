package de.devicez.server.user;

import at.favre.lib.crypto.bcrypt.BCrypt;
import de.devicez.server.DeviceZServerApplication;
import de.devicez.server.task.Task;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class UserRegistry {

    private final DeviceZServerApplication application;

    private final Map<UUID, User> userMap = new HashMap<>();

    public UserRegistry(final DeviceZServerApplication application) {
        this.application = application;

        application.getDatabaseClient().queryList(User.class, User.SELECT_ALL).forEach(task -> userMap.put(task.getId(), task));
    }

    public User createUser(final String name, final String email, final String password) {
        final String passwordHash = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        final User user = new User(application, UUID.randomUUID(), name, email, passwordHash);
        userMap.put(user.getId(), user);
        application.getDatabaseClient().save(user);

        final String confirmationUrl = application.getInformation().getFrontendUrl() + "/confirm?id=" + user.getId();
        application.getMailService().sendMail(user.getName(), user.getEmail(), "confirm",
                "%name%", user.getName(),
                "%organisation%", application.getInformation().getOrganisationName(),
                "%url%", confirmationUrl);

        return user;
    }

    public void deleteUser(final User user) {
        userMap.remove(user.getId());
        application.getDatabaseClient().delete(user);
    }

    public User getUserById(final UUID id) {
        return userMap.get(id);
    }

    public User getUserByEmail(final String email) {
        return userMap.values().stream().filter(user -> user.getEmail().equals(email)).findAny().orElse(null);
    }

    public User getUserBySessionToken(final UUID sessionToken) {
        return userMap.values().stream().filter(user -> user.hasSession() && user.getSessionToken().equals(sessionToken)).findAny().orElse(null);
    }

    public Collection<User> getUsers() {
        return userMap.values();
    }
}
