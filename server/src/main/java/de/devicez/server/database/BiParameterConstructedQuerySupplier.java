package de.devicez.server.database;

public interface BiParameterConstructedQuerySupplier<T, U> {

    ConstructedQuery apply(T parameter, U otherParameter);
}
