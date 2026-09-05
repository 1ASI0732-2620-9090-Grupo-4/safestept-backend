package com.safestep.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "IamMessageResponse", description = "Simple IAM operation response")
public record MessageResource(@Schema(description = "Operation message", example = "OK") String message) {
}
