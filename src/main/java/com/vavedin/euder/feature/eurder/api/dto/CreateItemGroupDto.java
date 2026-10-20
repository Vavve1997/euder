package com.vavedin.euder.feature.eurder.api.dto;

public class CreateItemGroupDto {

    private String itemId;
    private int amount;

    public CreateItemGroupDto() {
    }

    public CreateItemGroupDto(String itemId, int amount) {
        this.itemId = itemId;
        this.amount = amount;
    }

    public String getItemId() {
        return itemId;
    }

    public int getAmount() {
        return amount;
    }
}
