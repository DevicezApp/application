package de.devicez.server.http.controller;

import de.devicez.server.DeviceZServerApplication;
import de.devicez.server.http.HTTPServer;
import de.devicez.server.user.User;
import io.javalin.Javalin;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class AuthController extends AbstractController {

    public AuthController(final DeviceZServerApplication application, final Javalin server) {
        super(application, server);
    }

    @Override
    protected void registerHandlers(final Javalin javalin) {
        javalin.addHandler(HandlerType.POST, "/login", context -> {
            final LoginRequest request = context.bodyAsClass(LoginRequest.class);
            final User user = getApplication().getUserRegistry().getUserByEmail(request.getEmail());
            if (user == null) {
                context.json(new LoginErrorResponse(LoginError.INVALID_USER));
                return;
            }

            if (!user.login(request.getPassword())) {
                context.json(new LoginErrorResponse(LoginError.INVALID_PASSWORD));
                return;
            }

            if (!user.isActive()) {
                context.json(new LoginErrorResponse(LoginError.INACTIVE));
                return;
            }

            context.json(new LoginSuccessResponse(user.getSessionToken()));
        });

        javalin.addHandler(HandlerType.POST, "/register", context -> {
            final RegistrationRequest request = context.bodyAsClass(RegistrationRequest.class);

            User user = getApplication().getUserRegistry().getUserByEmail(request.getEmail());
            if (user != null) {
                context.json(new RegistrationErrorResponse(RegistrationError.EMAIL_TAKEN));
                return;
            }

            getApplication().getUserRegistry().createUser(request.getName(), request.getEmail(), request.getPassword());
            context.json(new SuccessResponse());
        });

        javalin.addHandler(HandlerType.POST, "/confirm", context -> {
            final ConfirmRequest request = context.bodyAsClass(ConfirmRequest.class);
            final User user = getApplication().getUserRegistry().getUserById(request.getId());
            if (user != null && !user.isActive()) {
                user.setActive(true);
                context.json(new SuccessResponse());
                return;
            }

            context.json(new FailureResponse());
        });

        javalin.addHandler(HandlerType.GET, "/users", context -> {
            context.json(new UserListResponse(getApplication().getUserRegistry().getUsers().stream().map(UserModel::convert).collect(Collectors.toSet())));
        });

        javalin.addHandler(HandlerType.GET, "/whoami", context -> {
            final User user = context.attribute(HTTPServer.USER_ATTRIBUTE);
            if (user == null) {
                context.status(HttpStatus.FORBIDDEN);
                return;
            }

            context.json(new WhoAmIResponse(UserModel.convert(user)));
        });
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class WhoAmIResponse extends SuccessResponse {
        private UserModel user;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class UserListResponse extends SuccessResponse {
        private Set<UserModel> users;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class LoginSuccessResponse extends SuccessResponse {
        private UUID token;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class LoginErrorResponse extends FailureResponse {
        private LoginError error;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class RegistrationErrorResponse extends FailureResponse {
        private RegistrationError error;
    }

    public enum LoginError {
        INVALID_USER, INVALID_PASSWORD, INACTIVE
    }

    public enum RegistrationError {
        EMAIL_TAKEN
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class RegistrationRequest {
        private String name;
        private String email;
        private String password;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class ConfirmRequest {
        private UUID id;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class UserModel {
        private UUID id;
        private String name;
        private String email;
        private long lastLogin;
        private boolean active;

        public static UserModel convert(final User user) {
            return new UserModel(user.getId(), user.getName(), user.getEmail(), user.getLastLogin().getTime(), user.isActive());
        }
    }
}
