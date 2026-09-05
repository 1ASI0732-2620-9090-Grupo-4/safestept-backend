package com.safestep.platform.simulation.interfaces.rest.resources;

import java.util.List;

public record StepResource(String id, String prompt, String correctOptionId, List<OptionResource> options) {
}
