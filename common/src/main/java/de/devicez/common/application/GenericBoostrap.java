package de.devicez.common.application;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.spi.LoggerContext;

import java.util.function.Supplier;

@Slf4j
public final class GenericBoostrap {

    public static void run(final Supplier<AbstractApplication> consumer) {
        final AbstractApplication application = consumer.get();

        // Shutdown hook for doing important things on CTRL + C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                application.shutdown();
            } catch (final Exception e) {
                log.error("Unhandled exception occurred on shutdown", e);
            }
        }));

        // Run application
        try {
            application.startup();
        } catch (final Exception e) {
            log.error("Unhandled exception occurred on startup", e);
        }
    }
}
