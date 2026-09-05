package com.safestep.platform.commerce.domain.model.commands;

public record UpdateCartItemCommand(String username, String itemId, int quantity) {
}
