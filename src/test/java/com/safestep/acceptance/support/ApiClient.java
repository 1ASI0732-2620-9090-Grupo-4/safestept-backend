package com.safestep.acceptance.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.core.env.Environment;

/** Thin HTTP client used by the step definitions to drive the SafeStep REST API like a real consumer would. */
public class ApiClient {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Environment environment;

    public ApiClient(Environment environment) {
        this.environment = environment;
    }

    private String baseUrl() {
        return "http://localhost:" + environment.getRequiredProperty("local.server.port");
    }

    public ApiResponse get(String path, String token) {
        return send(builder(path, token).GET());
    }

    public ApiResponse post(String path, Object body, String token) {
        return send(builder(path, token).POST(publisher(body)));
    }

    public ApiResponse put(String path, Object body, String token) {
        return send(builder(path, token).PUT(publisher(body)));
    }

    private HttpRequest.Builder builder(String path, String token) {
        var builder = HttpRequest.newBuilder(URI.create(baseUrl() + path)).header("Content-Type", "application/json");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return builder;
    }

    private HttpRequest.BodyPublisher publisher(Object body) {
        try {
            return HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body));
        } catch (IOException exception) {
            throw new IllegalStateException("Request body could not be serialised", exception);
        }
    }

    private ApiResponse send(HttpRequest.Builder builder) {
        try {
            var response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            JsonNode body = response.body() == null || response.body().isBlank() ? objectMapper.createObjectNode()
                    : objectMapper.readTree(response.body());
            return new ApiResponse(response.statusCode(), body);
        } catch (IOException exception) {
            throw new IllegalStateException("API call failed", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("API call interrupted", exception);
        }
    }
}
