package de.devicez.server.http;

import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.javalin.http.servlet.JavalinServletContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HTTPServer {

    private final Javalin server;

    public HTTPServer(final int port, final String apiKey) {
        this.server = Javalin.create().start(port);

        server.before(context -> {
            final String transmittedApiKey = context.header("API-KEY");
            if (apiKey.equals(transmittedApiKey)) {
                return;
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
        server.get("/", context -> context.result("It works!"));
        // TODO add routes
    }

    public void close() {
        server.close();
    }
}
