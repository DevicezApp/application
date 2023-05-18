package de.devicez.server.database;

public class DatabaseException extends RuntimeException {

    public DatabaseException(final String message) {
        super(message);
    }

    public DatabaseException(final Throwable cause) {
        super(cause);
    }
}
