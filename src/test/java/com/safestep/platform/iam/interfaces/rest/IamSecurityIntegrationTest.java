package com.safestep.platform.iam.interfaces.rest;

import com.safestep.platform.iam.application.commandservices.UserCommandService;
import com.safestep.platform.iam.domain.model.commands.SignUpCommand;
import com.safestep.platform.iam.domain.model.commands.UpdateUserStatusCommand;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IamSecurityIntegrationTest {

    private static final String PASSWORD = "SecurePass123!";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @LocalServerPort
    private int port;

    @Autowired
    private UserCommandService userCommandService;

    @Test
    void signUpIgnoresRequestedAdminRole() {
        var username = uniqueUsername("public");
        var response = postJson("/api/v1/authentication/sign-up",
                Map.of("username", username, "password", PASSWORD, "roles", List.of("ROLE_ADMIN")));

        assertThat(response.status()).isEqualTo(201);
        assertThat(response.body().get("roles").toString()).contains("ROLE_USER").doesNotContain("ROLE_ADMIN");
    }

    @Test
    void usersAndRolesEndpointsRequireAdminRole() {
        var userTokens = createPublicUserAndSignIn("regular");
        var adminTokens = createAdminAndSignIn("admin");

        assertThat(getWithOptionalToken("/api/v1/users", null).status()).isEqualTo(401);
        assertThat(getWithOptionalToken("/api/v1/users", userTokens.token()).status()).isEqualTo(403);
        assertThat(getWithOptionalToken("/api/v1/users", adminTokens.token()).status()).isEqualTo(200);

        assertThat(getWithOptionalToken("/api/v1/roles", null).status()).isEqualTo(401);
        assertThat(getWithOptionalToken("/api/v1/roles", userTokens.token()).status()).isEqualTo(403);
        assertThat(getWithOptionalToken("/api/v1/roles", adminTokens.token()).status()).isEqualTo(200);
    }

    @Test
    void refreshTokenRotatesAndLogoutRevokesRefreshToken() {
        var tokens = createPublicUserAndSignIn("refresh");

        var refreshResponse = postJson("/api/v1/authentication/refresh-token",
                Map.of("refreshToken", tokens.refreshToken()));
        assertThat(refreshResponse.status()).isEqualTo(200);
        var rotatedRefreshToken = refreshResponse.body().get("refreshToken").toString();
        assertThat(rotatedRefreshToken).isNotEqualTo(tokens.refreshToken());

        var reuseResponse = postJson("/api/v1/authentication/refresh-token",
                Map.of("refreshToken", tokens.refreshToken()));
        assertThat(reuseResponse.status()).isEqualTo(400);

        var logoutResponse = postJson("/api/v1/authentication/logout", Map.of("refreshToken", rotatedRefreshToken));
        assertThat(logoutResponse.status()).isEqualTo(200);

        var revokedResponse = postJson("/api/v1/authentication/refresh-token",
                Map.of("refreshToken", rotatedRefreshToken));
        assertThat(revokedResponse.status()).isEqualTo(400);
    }

    @Test
    void disabledUserCannotSignIn() {
        var user = userCommandService.handle(new SignUpCommand(uniqueUsername("disabled"), PASSWORD, List.of("ROLE_USER")))
                .toOptional().orElseThrow();

        userCommandService
                .handle(new UpdateUserStatusCommand(user.getId(), false, true, true, true)).toOptional().orElseThrow();

        var response = postJson("/api/v1/authentication/sign-in",
                Map.of("username", user.getUsername(), "password", PASSWORD));

        assertThat(response.status()).isEqualTo(422);
    }

    @Test
    void resetPasswordChangesPasswordAndRevokesActiveRefreshTokens() {
        var username = uniqueUsername("reset");
        postJson("/api/v1/authentication/sign-up", Map.of("username", username, "password", PASSWORD));
        var tokens = signIn(username, PASSWORD);

        var forgotResponse = postJson("/api/v1/authentication/forgot-password", Map.of("username", username));
        assertThat(forgotResponse.status()).isEqualTo(200);
        var resetToken = forgotResponse.body().get("resetToken").toString();

        var resetResponse = postJson("/api/v1/authentication/reset-password",
                Map.of("resetToken", resetToken, "newPassword", "NewSecurePass123!"));
        assertThat(resetResponse.status()).isEqualTo(200);

        assertThat(postJson("/api/v1/authentication/sign-in",
                Map.of("username", username, "password", PASSWORD)).status()).isEqualTo(400);
        assertThat(postJson("/api/v1/authentication/sign-in",
                Map.of("username", username, "password", "NewSecurePass123!")).status()).isEqualTo(200);
        assertThat(postJson("/api/v1/authentication/refresh-token",
                Map.of("refreshToken", tokens.refreshToken())).status()).isEqualTo(400);
    }

    private AuthTokens createPublicUserAndSignIn(String prefix) {
        var username = uniqueUsername(prefix);
        postJson("/api/v1/authentication/sign-up", Map.of("username", username, "password", PASSWORD));
        return signIn(username, PASSWORD);
    }

    private AuthTokens createAdminAndSignIn(String prefix) {
        var username = uniqueUsername(prefix);
        userCommandService.handle(new SignUpCommand(username, PASSWORD, List.of("ROLE_ADMIN"))).toOptional()
                .orElseThrow();
        return signIn(username, PASSWORD);
    }

    private AuthTokens signIn(String username, String password) {
        var response = postJson("/api/v1/authentication/sign-in", Map.of("username", username, "password", password));
        assertThat(response.status()).isEqualTo(200);
        return new AuthTokens(response.body().get("token").toString(),
                response.body().get("refreshToken").toString());
    }

    private TestResponse postJson(String path, Object body) {
        try {
            var request = HttpRequest.newBuilder(URI.create(url(path)))
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body))).build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return new TestResponse(response.statusCode(), toMap(response.body()));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private TestResponse getWithOptionalToken(String path, String token) {
        try {
            var builder = HttpRequest.newBuilder(URI.create(url(path))).GET();
            if (token != null) {
                builder.header("Authorization", "Bearer " + token);
            }
            var response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return new TestResponse(response.statusCode(), toMap(response.body()));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private Map<String, Object> toMap(String json) {
        try {
            if (json == null || json.isBlank()) {
                return Map.of();
            }
            if (json.stripLeading().startsWith("[")) {
                return Map.of("_array", objectMapper.readValue(json, new TypeReference<List<Object>>() {
                }));
            }
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private String uniqueUsername(String prefix) {
        return "%s-%s".formatted(prefix, UUID.randomUUID().toString().substring(0, 8));
    }

    private String url(String path) {
        return "http://localhost:%d%s".formatted(port, path);
    }

    private record AuthTokens(String token, String refreshToken) {
    }

    private record TestResponse(int status, Map<String, Object> body) {
    }
}
