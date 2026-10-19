package com.vavedin.euder.feature.item.api.dto;

public class ItemDto {
    private String name;
    private String description;
    private double price;
    private int amount;
    private String id;

    public ItemDto() {
    }

    public ItemDto(String name, String description, double price, int amount, String id) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.amount = amount;
        this.id = id;
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
