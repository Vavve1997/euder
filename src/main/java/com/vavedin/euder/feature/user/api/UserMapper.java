package com.vavedin.euder.feature.user.api;

import com.vavedin.euder.feature.user.api.dto.CreateUserDto;
import com.vavedin.euder.feature.user.api.dto.UserDto;
import com.vavedin.euder.feature.user.domain.User;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

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

    public List<UserDto> toDto(Collection<User> users) {
        return users.stream().map(this::toDto).toList();
    }


    public User toEntity(CreateUserDto createUserDto, String hashedPassword) {
        return new User(createUserDto.getFirstName(),
                createUserDto.getLastName(),
                createUserDto.getEmail(),
                addressMapper.toEntity(createUserDto.getAddress()),
                createUserDto.getPhoneNumber(),
                hashedPassword);
    }
}
