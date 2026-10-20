package com.vavedin.euder.feature.eurder.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Eurder {

    private final String customerId;
    private final List<ItemGroup> itemGroups;
    private final String id;

    public Eurder(String customerId, List<ItemGroup> itemGroups) {
        this.customerId = requireNonBlank(customerId, "customerId");
        if (itemGroups == null || itemGroups.isEmpty()) {
            throw new IllegalArgumentException("itemGroup must not be empty or null");
        }
        this.itemGroups = new ArrayList<>(itemGroups);
        id = UUID.randomUUID().toString();
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be null or blank"
            );
        }
        return value;
    }

    public String getCustomerId() {
        return customerId;
    }

    public List<ItemGroup> getItemGroups() {
        return List.copyOf(itemGroups);
    }

    public String getId() {
        return id;
    }

    public BigDecimal calculateTotalPrice() {
        return itemGroups.stream()
                .map(ItemGroup::calculateTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
