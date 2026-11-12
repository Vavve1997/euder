package com.vavedin.euder.feature.eurder.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "item_groups")
public class ItemGroup {

    @Id
    @Column(nullable = false, length = 255)
    private String id;

    @Column(name = "item_id", nullable = false, length = 255)
    private String itemId;

    @Column(name = "item_name", nullable = false, length = 200)
    private String itemName;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int amount;

    @Column(name = "shipping_date", nullable = false)
    private LocalDate shippingDate;

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
        this.id = UUID.randomUUID().toString();
    }

    public ItemGroup() {

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

    public String getId() {
        return id;
    }
}
