package de.devicez.server.http;

import de.devicez.server.DeviceZServerApplication;
import io.javalin.Javalin;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HTTPServer {

    private final DeviceZServerApplication application;
    private final Javalin server;

    public HTTPServer(final DeviceZServerApplication application, final int port) {
        this.application = application;
        this.server = Javalin.create().start(port);

        server.before(context -> {
            /*final String transmittedApiKey = context.header("API-KEY");
            if (apiKey.equals(transmittedApiKey)) {
                return;
            }

            // Intercept any unauthenticated requests
            context.status(HttpStatus.FORBIDDEN);
            JavalinServletContext servletContext = (JavalinServletContext) context;
            servletContext.getTasks().clear();*/
        });

        registerRoutes();

        log.info("Started http server on port " + port);
    }

    private void registerRoutes() {
        server.get("/", context -> context.result(application.getGson().toJson(application.getInformation())));
        // TODO add routes
    }

    public void close() {
        server.close();
    }
}
