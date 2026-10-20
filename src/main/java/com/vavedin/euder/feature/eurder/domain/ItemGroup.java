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
        this.itemId = requireNonBlank(itemId, "itemId");
        this.itemName = requireNonBlank(itemName, "itemName");
        if (unitPrice == null) {
            throw new IllegalArgumentException("unitPrice must not be null");
        }
        this.unitPrice = unitPrice;
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
        this.amount = amount;
        if (shippingDate == null) {
            throw new IllegalArgumentException("shippingDate must not be null");
        }
        this.shippingDate = shippingDate;
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
