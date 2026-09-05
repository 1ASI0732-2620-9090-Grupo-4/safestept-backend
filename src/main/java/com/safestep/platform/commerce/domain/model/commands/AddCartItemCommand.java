package com.safestep.platform.commerce.domain.model.commands;

public record AddCartItemCommand(String username, String productId, int quantity) {
}
