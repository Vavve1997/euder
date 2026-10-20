package com.vavedin.euder.feature.eurder.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ItemGroupDto {
    private String itemId;
    private String itemName;
    private BigDecimal unitPrice;
    private int amount;
    private LocalDate shippingDate;
    private BigDecimal totalPrice;

    public ItemGroupDto(String itemId, String itemName, BigDecimal unitPrice, int amount, LocalDate shippingDate, BigDecimal totalPrice) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.unitPrice = unitPrice;
        this.amount = amount;
        this.shippingDate = shippingDate;
        this.totalPrice = totalPrice;
    }

    public ItemGroupDto() {
    }

    public String getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getAmount() {
        return amount;
    }

    public LocalDate getShippingDate() {
        return shippingDate;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
}
