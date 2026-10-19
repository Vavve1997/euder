package com.vavedin.euder.feature.user.api;

import com.vavedin.euder.feature.user.api.dto.AddressDto;
import com.vavedin.euder.feature.user.domain.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {
    public AddressDto toDto(Address address) {
        return new AddressDto(address.street(), address.houseNumber(), address.postalCode(), address.city());
    }

    public Address toEntity(AddressDto addressDto) {
        if (addressDto == null) {
            throw new IllegalArgumentException("address must not be null");
        }
        return new Address(addressDto.getStreet(), addressDto.getHouseNumber(), addressDto.getPostalCode(), addressDto.getCity());
    }
}
