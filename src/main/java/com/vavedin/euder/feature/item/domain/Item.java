package com.vavedin.euder.feature.item.domain;

import java.math.BigDecimal;
import java.util.UUID;

public class Item {

    private String name;
    private String description;
    private BigDecimal price;
    private int amount;
    private String id;

    public Item(String name, String description, BigDecimal price, int amount, String id) {
        this.name = requireNonBlank(name, "name");
        this.description = requireNonBlank(description, "description");
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("price must be greater than zero");
        }
        this.price = price;
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
        this.amount = amount;
        this.id = requireNonBlank(id, "id");
    }

    public Item(String name, String description, BigDecimal price, int amount) {
        this(name,
                description,
                price,
                amount,
                UUID.randomUUID().toString());
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be null or blank"
            );
        }
        return value;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getAmount() {
        return amount;
    }

    public String getId() {
        return id;
    }
}
