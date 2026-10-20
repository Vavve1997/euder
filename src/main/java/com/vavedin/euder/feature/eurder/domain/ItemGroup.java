package com.vavedin.euder.feature.eurder.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class ItemGroup {

    private final String itemId;
    private final String itemName;
    private final BigDecimal unitPrice;
    private final int amount;
    private final LocalDate shippingDate;

    public ItemGroup(String itemId, String itemName, BigDecimal unitPrice, int amount, LocalDate shippingDate) {
        this.itemId = itemId;
        this.itemName = requireNonBlank(itemName, "itemName");
        this.unitPrice = unitPrice;
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
        this.amount = amount;
        this.shippingDate = shippingDate;
    }

    public ItemGroup(String itemName, BigDecimal unitPrice, int amount, LocalDate shippingDate) {
        this(UUID.randomUUID().toString(), itemName, unitPrice, amount, shippingDate);
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be null or blank"
            );
        }
        return value;
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

    public BigDecimal calculateTotalPrice() {
        return unitPrice.multiply(BigDecimal.valueOf(amount));
    }
}
