package com.vavedin.euder.feature.item.api.dto;

import java.math.BigDecimal;

public class UpdateItemDto {

    private String name;
    private String description;
    private BigDecimal price;
    private int amount;

    public UpdateItemDto() {
    }

    public UpdateItemDto(String name, String description, BigDecimal price, int amount) {
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

    public BigDecimal getPrice() {
        return price;
    }

    public int getAmount() {
        return amount;
    }
}
