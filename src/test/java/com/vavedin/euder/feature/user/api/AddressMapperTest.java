package com.vavedin.euder.feature.user.api;

import com.vavedin.euder.feature.user.api.dto.AddressDto;
import com.vavedin.euder.feature.user.api.dto.CreateUserDto;
import com.vavedin.euder.feature.user.api.dto.UserDto;
import com.vavedin.euder.feature.user.domain.Address;
import com.vavedin.euder.feature.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class AddressMapperTest {

    private Address address;
    private AddressDto addressDto;
    private AddressMapper addressMapper;

    @BeforeEach
    void setup() {
        addressMapper = new AddressMapper();
        address = new Address("RedKeepRoad",
                1,
                1000,
                "Kingslanding");
        addressDto = new AddressDto("RedKeepRoad",
                1,
                1000,
                "Kingslanding");
    }

    @Test
    void toDto_keepsAllValues() {
        AddressDto result = addressMapper.toDto(address);
        assertThat(result).usingRecursiveComparison().isEqualTo(address);
    }

    @Test
    void toEntity_keepsAllValues() {
        Address result = addressMapper.toEntity(addressDto);
        assertThat(result).usingRecursiveComparison().isEqualTo(addressDto);
    }

}