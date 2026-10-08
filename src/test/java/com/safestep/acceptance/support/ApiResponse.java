package com.safestep.acceptance.support;

import com.fasterxml.jackson.databind.JsonNode;

/** HTTP status and parsed JSON body of an API call made by a scenario. */
public record ApiResponse(int status, JsonNode body) {

    public String text(String field) {
        var value = body.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }
}
