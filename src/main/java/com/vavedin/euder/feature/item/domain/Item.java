package com.vavedin.euder.feature.item.domain;

import java.util.UUID;

public class Item {

    private String name;
    private String description;
    private double price;
    private int amount;
    private String id;

    public Item(String name, String description, double price, int amount, String id) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.amount = amount;
        this.id = id;
    }

    public Item(String name, String description, double price, int amount) {
        this(name,
                description,
                price,
                amount,
                UUID.randomUUID().toString());
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public int getAmount() {
        return amount;
    }

    public String getId() {
        return id;
    }
}
