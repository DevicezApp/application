package de.devicez.server.http;

import de.devicez.server.DeviceZServerApplication;
import de.devicez.server.http.controller.DeviceController;
import de.devicez.server.user.User;
import de.devicez.server.http.controller.AuthController;
import io.javalin.Javalin;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;
import io.javalin.http.servlet.JavalinServletContext;
import io.javalin.json.JavalinGson;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
public class HTTPServer {

    public static final String USER_ATTRIBUTE = "user";
    private static final List<String> UNPROTECTED_ROUTES = Arrays.asList("/", "/login", "/register", "/confirm");

    private final DeviceZServerApplication application;
    private final Javalin server;

    public HTTPServer(final DeviceZServerApplication application, final int port) {
        this.application = application;
        this.server = Javalin.create(config -> {
            config.plugins.enableDevLogging();
            config.plugins.enableCors(cors -> cors.add(it -> {
                it.anyHost();
                it.allowCredentials = true;
                it.exposeHeader("Authorization");
            }));
            config.jsonMapper(new JavalinGson(application.getGson()));
        }).start(port);

        server.before(context -> {
            // If route is not protected -> let them pass
            if (UNPROTECTED_ROUTES.contains(context.path())) {
                return;
            }

            final String rawToken = context.header("Authorization");
            if (rawToken != null) {
                final UUID token;
                try {
                    token = UUID.fromString(rawToken.substring(7));

                    final User user = application.getUserRegistry().getUserBySessionToken(token);
                    if (user != null && !user.isSessionExpired()) {
                        context.attribute(USER_ATTRIBUTE, user);

                        // If user was found by session -> reset expiration timer
                        user.extendSession();
                        return;
                    }
                } catch (final IllegalArgumentException ignored) {
                }
            }

            // Intercept any unauthenticated requests
            if (context.method() != HandlerType.OPTIONS) {
                context.status(HttpStatus.UNAUTHORIZED);
            }

            JavalinServletContext servletContext = (JavalinServletContext) context;
            servletContext.getTasks().clear();
        });

        registerRoutes();
        log.info("Started http server on port " + port);
    }

    private void registerRoutes() {
        server.get("/", context -> context.json(application.getInformation()));

        new AuthController(application, server);
        new DeviceController(application, server);
    }

    public void close() {
        server.close();
    }
}
