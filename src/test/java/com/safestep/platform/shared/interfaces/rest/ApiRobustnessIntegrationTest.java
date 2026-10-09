package com.safestep.platform.shared.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/** Verifies, over real HTTP, how the API reacts to unreadable bodies and to browser origins (CORS). */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiRobustnessIntegrationTest {

    private static final String ALLOWED_ORIGIN = "http://localhost:4200";
    private static final String FOREIGN_ORIGIN = "https://evil.example";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @LocalServerPort
    private int port;

    private HttpResponse<String> send(HttpRequest.Builder builder) throws Exception {
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
    }

    @Test
    @DisplayName("A malformed JSON body should be answered with 400 and a validation error, not with 500")
    void malformedJson_IsAnsweredWith400() throws Exception {
        // Act
        var response = send(request("/api/v1/authentication/sign-in").header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{ this is not json")));

        // Assert
        assertThat(response.statusCode()).isEqualTo(400);
        var body = objectMapper.readTree(response.body());
        assertThat(body.get("code").asText()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.body()).doesNotContain("Unexpected character");
    }

    @Test
    @DisplayName("A preflight request from an allowed origin should receive the CORS headers")
    void preflightFromAllowedOrigin_IsAccepted() throws Exception {
        // Act
        var response = send(request("/api/v1/authentication/sign-in").header("Origin", ALLOWED_ORIGIN)
                .header("Access-Control-Request-Method", "POST").header("Access-Control-Request-Headers", "content-type")
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody()));

        // Assert
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Access-Control-Allow-Origin")).contains(ALLOWED_ORIGIN);
    }

    @Test
    @DisplayName("A preflight request from a foreign origin should be rejected")
    void preflightFromForeignOrigin_IsRejected() throws Exception {
        // Act
        var response = send(request("/api/v1/authentication/sign-in").header("Origin", FOREIGN_ORIGIN)
                .header("Access-Control-Request-Method", "POST")
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody()));

        // Assert
        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(response.headers().firstValue("Access-Control-Allow-Origin")).isEmpty();
    }
}
