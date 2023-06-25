package de.devicez.server.http;

import de.devicez.server.DeviceZServerApplication;
import de.devicez.server.user.User;
import de.devicez.server.http.controller.AuthController;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.javalin.http.servlet.JavalinServletContext;
import io.javalin.json.JavalinGson;
import io.javalin.plugin.bundled.CorsPluginConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
public class HTTPServer {

    private static final List<String> UNPROTECTED_ROUTES = Arrays.asList("/", "/login", "/register");

    private final DeviceZServerApplication application;
    private final Javalin server;

    public HTTPServer(final DeviceZServerApplication application, final int port) {
        this.application = application;
        this.server = Javalin.create(config -> {
            config.plugins.enableCors(cors -> cors.add(CorsPluginConfig::anyHost));
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
                        // If user was found by session -> reset expiration timer
                        user.extendSession();
                        return;
                    }
                } catch (final IllegalArgumentException ignored) {
                }
            }

            // Intercept any unauthenticated requests
            context.status(HttpStatus.FORBIDDEN);
            JavalinServletContext servletContext = (JavalinServletContext) context;
            servletContext.getTasks().clear();
        });

        registerRoutes();
        log.info("Started http server on port " + port);
    }

    private void registerRoutes() {
        server.get("/", context -> context.json(application.getInformation()));

        new AuthController(application, server);
    }

    public void close() {
        server.close();
    }
}
