package com.vavedin.euder.feature.eurder.api.dto;

import java.math.BigDecimal;
import java.util.List;

public class EurderDto {

    private String id;
    private String customerId;

    private List<ItemGroupDto> itemGroups;
    private BigDecimal totalPrice;

    public EurderDto(String id, String customerId, List<ItemGroupDto> itemGroups, BigDecimal totalPrice) {
        this.id = id;
        this.customerId = customerId;

        this.itemGroups = itemGroups;
        this.totalPrice = totalPrice;
    }

    public EurderDto() {
    }

    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }



    public List<ItemGroupDto> getItemGroups() {
        return itemGroups;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
}
