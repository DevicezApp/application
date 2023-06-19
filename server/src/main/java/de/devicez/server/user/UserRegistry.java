package de.devicez.server.user;

import at.favre.lib.crypto.bcrypt.BCrypt;
import de.devicez.server.DeviceZServerApplication;
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
    }

    public User createUser(final String username, final String email, final String password) {
        final String passwordHash = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        final User user = new User(application, UUID.randomUUID(), username, email, passwordHash);
        userMap.put(user.getId(), user);
        application.getDatabaseClient().save(user);
        return user;
    }

    public void deleteUser(final User user) {
        userMap.remove(user.getId());
        application.getDatabaseClient().delete(user);
    }

    public User getUserById(final UUID id) {
        return userMap.get(id);
    }

    public User getUserByName(final String username) {
        return userMap.values().stream().filter(user -> user.getUsername().equals(username)).findAny().orElse(null);
    }

    public Collection<User> getUsers() {
        return userMap.values();
    }
}
