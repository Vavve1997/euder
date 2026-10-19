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
        return new UserDto(user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                addressMapper.toDto(user.getAddress()),
                user.getPhoneNumber(),
                user.getId());
    }

    public User toEntity(UserDto userDto) {
        return new User(userDto.getFirstName(),
                userDto.getLastName(),
                userDto.getEmail(),
                addressMapper.toEntity(userDto.getAddressDto()),
                userDto.getPhoneNumber(),
                userDto.getId());
    }

    public User toEntity(CreateUserDto createUserDto) {
        return new User(createUserDto.getFirstName(),
                createUserDto.getLastName(),
                createUserDto.getEmail(),
                addressMapper.toEntity(createUserDto.getAddress()),
                createUserDto.getPhoneNumber());
    }
}
