package com.vavedin.euder.feature.item.api.dto;

public class CreateItemDto {

    private String name;
    private String description;
    private double price;
    private int amount;

    public CreateItemDto() {
    }

    public CreateItemDto(String name, String description, double price, int amount) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.amount = amount;
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
}
