package de.devicez.server.database;

public interface ParameterConstructedQuerySupplier<T> {

    ConstructedQuery apply(T parameter);
}
