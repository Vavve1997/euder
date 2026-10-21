package com.vavedin.euder.feature.eurder.domain;

import com.vavedin.euder.feature.user.domain.Address;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Eurder {

    private final String customerId;
    private final List<ItemGroup> itemGroups;
    private final String id;
    private final Address shippingAddress;

    public Eurder(String customerId, List<ItemGroup> itemGroups, Address shippingAddress) {
        this.customerId = requireNonBlank(customerId, "customerId");
        if (shippingAddress == null) {
            throw new IllegalArgumentException("shippingAddress must not be empty or null");
        }
        this.shippingAddress = shippingAddress;
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

    public boolean hasItemGroupShippingToday() {
        return !getAllItemGroupsThatShipToday().isEmpty();
    }

    public List<ItemGroup> getAllItemGroupsThatShipToday() {
        return itemGroups.stream()
                .filter(itemGroup -> itemGroup.getShippingDate().equals(LocalDate.now()))
                .toList();
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }
}
