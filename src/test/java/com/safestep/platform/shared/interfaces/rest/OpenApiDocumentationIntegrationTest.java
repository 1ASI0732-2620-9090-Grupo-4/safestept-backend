package com.safestep.platform.shared.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/** Verifies that the published OpenAPI contract describes the real responses of the API. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApiDocumentationIntegrationTest {

    private static final List<String> HTTP_METHODS = List.of("get", "post", "put", "delete");

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @LocalServerPort
    private int port;

    private JsonNode paths;

    private JsonNode loadPaths() throws Exception {
        if (paths == null) {
            var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/v3/api-docs")).build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).isEqualTo(200);
            paths = objectMapper.readTree(response.body()).get("paths");
        }
        return paths;
    }

    private List<String> operationsWhere(Predicate<JsonNode> violation) throws Exception {
        var offenders = new ArrayList<String>();
        loadPaths().properties().forEach(path -> HTTP_METHODS.forEach(method -> {
            var operation = path.getValue().get(method);
            if (operation != null && violation.test(operation)) {
                offenders.add(method.toUpperCase() + " " + path.getKey());
            }
        }));
        return offenders;
    }

    private static boolean hasSuccessSchema(JsonNode operation) {
        var responses = operation.get("responses");
        var iterator = responses.properties().iterator();
        while (iterator.hasNext()) {
            var response = iterator.next();
            if (!response.getKey().startsWith("2")) {
                continue;
            }
            // A 204 response has no body, so it carries no schema by definition
            if (response.getKey().equals("204")) {
                return true;
            }
            // The schema must point to a component (or to an array of components), not to a bare object
            var schema = response.getValue().path("content").path("application/json").path("schema");
            if (schema.has("$ref") || schema.path("items").has("$ref")) {
                return true;
            }
        }
        return false;
    }

    @Test
    @DisplayName("Every operation should document the schema of its successful response")
    void everyOperation_DocumentsItsSuccessSchema() throws Exception {
        // Act
        var offenders = operationsWhere(operation -> !hasSuccessSchema(operation));
        // Product reviews are not implemented yet: the endpoint always answers an empty list
        offenders.remove("GET /api/v1/commerce/reviews");

        // Assert
        assertThat(offenders).as("operations without a success response schema").isEmpty();
    }

    @Test
    @DisplayName("Resource creation should be documented as 201 and not as 200")
    void creationOperations_AreDocumentedAs201() throws Exception {
        // Act
        var offenders = operationsWhere(operation -> {
            var summary = operation.path("summary").asText();
            // Creating a Stripe Checkout session does not create a resource of ours: it answers 200
            var creates = (!summary.contains("Stripe") && summary.startsWith("Create ")) || summary.startsWith("Add item")
                    || summary.startsWith("Redeem") || summary.equals("User registration");
            return creates && !operation.get("responses").has("201");
        });

        // Assert
        assertThat(offenders).as("creation operations not documented as 201").isEmpty();
    }

    @Test
    @DisplayName("List endpoints should document an array in their successful response")
    void listEndpoints_DocumentAnArray() throws Exception {
        // Arrange
        var listPaths = List.of("/api/v1/users", "/api/v1/roles", "/api/v1/profiles", "/api/v1/simulations",
                "/api/v1/commerce/products", "/api/v1/commerce/coupons");

        // Act / Assert
        for (var path : listPaths) {
            var schema = loadPaths().get(path).get("get").get("responses").get("200").get("content")
                    .get("application/json").get("schema");
            assertThat(schema.get("type").asText()).as("schema type of GET " + path).isEqualTo("array");
        }
    }

    @Test
    @DisplayName("Error responses should not repeat the schema of the successful response")
    void errorResponses_DoNotRepeatTheSuccessSchema() throws Exception {
        // Act
        var unauthorized = loadPaths().get("/api/v1/users").get("get").get("responses").get("401");

        // Assert
        assertThat(unauthorized.has("content")).isFalse();
    }
}
