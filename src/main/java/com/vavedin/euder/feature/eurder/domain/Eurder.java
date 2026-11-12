package com.vavedin.euder.feature.eurder.domain;

import com.vavedin.euder.feature.user.domain.Address;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "eurders")
public class Eurder {

    @Id
    @Column(nullable = false, length = 255)
    private String id;

    @Column(name = "customer_id", nullable = false, length = 255)
    private String customerId;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "eurder_id", nullable = false)
    private List<ItemGroup> itemGroups = new ArrayList<>();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(
                    name = "street",
                    column = @Column(
                            name = "shipping_street",
                            nullable = false,
                            length = 200
                    )
            ),
            @AttributeOverride(
                    name = "houseNumber",
                    column = @Column(
                            name = "shipping_house_number",
                            nullable = false,
                            length = 20
                    )
            ),
            @AttributeOverride(
                    name = "postalCode",
                    column = @Column(
                            name = "shipping_postal_code",
                            nullable = false,
                            length = 20
                    )
            ),
            @AttributeOverride(
                    name = "city",
                    column = @Column(
                            name = "shipping_city",
                            nullable = false,
                            length = 100
                    )
            )
    })
    private Address shippingAddress;


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

    public Eurder() {

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
