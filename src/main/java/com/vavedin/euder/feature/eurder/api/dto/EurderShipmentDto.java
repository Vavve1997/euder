package com.vavedin.euder.feature.eurder.api.dto;

import com.vavedin.euder.feature.user.api.dto.AddressDto;

import java.util.List;

public class EurderShipmentDto {

    private final String eurderId;
    private final AddressDto shippingAddress;
    private final List<ItemGroupDto> itemGroups;

    public EurderShipmentDto(
            String eurderId,
            AddressDto shippingAddress,
            List<ItemGroupDto> itemGroups
    ) {
        this.eurderId = eurderId;
        this.shippingAddress = shippingAddress;
        this.itemGroups = List.copyOf(itemGroups);
    }

    public String getEurderId() {
        return eurderId;
    }

    public AddressDto getShippingAddress() {
        return shippingAddress;
    }

    public List<ItemGroupDto> getItemGroups() {
        return itemGroups;
    }
}