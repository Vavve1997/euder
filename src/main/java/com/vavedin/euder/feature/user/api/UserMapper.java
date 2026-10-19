package com.vavedin.euder.feature.user.api;

import com.vavedin.euder.feature.user.api.dto.CreateUserDto;
import com.vavedin.euder.feature.user.api.dto.UserDto;
import com.vavedin.euder.feature.user.domain.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private final AddressMapper addressMapper;

    public UserMapper(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }


    public UserDto toDto(User user) {
    }

    public User toEntity(UserDto userDto) {
    }

    public User toEntity(CreateUserDto createUserDto) {
    }
}
