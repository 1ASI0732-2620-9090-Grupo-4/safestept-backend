package com.safestep.platform.commerce.domain.model.aggregates;

import com.safestep.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.safestep.platform.commerce.domain.model.valueobjects.CommerceValueObjects.*;
import java.math.*;
import java.util.*;

public class Product extends AbstractDomainAggregateRoot<Product> {
    private Long id;
    private final String externalId, name, category, type, imageUrl, description;
    private final BigDecimal price, oldPrice;
    private final double rating;
    private int stock;
    private final List<String> tags;

    public Product(Long id, String externalId, String name, String category, String type, BigDecimal price,
            BigDecimal oldPrice, double rating, int stock, String imageUrl, String description, List<String> tags) {
        this.id = id;
        this.externalId = externalId;
        this.name = name;
        this.category = category;
        this.type = type;
        this.price = new Money(price).value();
        this.oldPrice = oldPrice == null ? null : new Money(oldPrice).value();
        this.rating = rating;
        this.stock = new Stock(stock).value();
        this.imageUrl = imageUrl;
        this.description = description;
        this.tags = tags == null ? List.of() : List.copyOf(tags);
    }

    public void reduceStock(int quantity) {
        if (quantity < 1 || quantity > stock)
            throw new IllegalArgumentException("Insufficient stock");
        stock -= quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long v) {
        id = v;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getOldPrice() {
        return oldPrice;
    }

    public double getRating() {
        return rating;
    }

    public int getStock() {
        return stock;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getTags() {
        return tags;
    }
}
