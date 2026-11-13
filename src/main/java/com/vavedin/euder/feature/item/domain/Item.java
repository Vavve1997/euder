package com.vavedin.euder.feature.item.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "items")
public class Item {

    @Id
    @Column(nullable = false, length = 255)
    private String id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int amount;

    @Version
    @Column(nullable = false)
    private Long version;

    public void updateDetails(
            String name,
            String description,
            BigDecimal price,
            int amount
    ) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description must not be blank");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must not be negative");
        }

        this.name = name;
        this.description = description;
        this.price = price;
        this.amount = amount;
    }

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

    public Item() {

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

    public void takeAmountFromStock(int amount) {
        this.amount = Math.max(0, this.amount - amount);

    }
}
