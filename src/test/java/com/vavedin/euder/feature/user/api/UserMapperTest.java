package com.vavedin.euder.feature.user.api;

import com.vavedin.euder.feature.user.api.dto.AddressDto;
import com.vavedin.euder.feature.user.api.dto.CreateUserDto;
import com.vavedin.euder.feature.user.api.dto.UserDto;
import com.vavedin.euder.feature.user.domain.Address;
import com.vavedin.euder.feature.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UserMapperTest {

    private User user;
    private UserDto userDto;
    private CreateUserDto createUserDto;
    private UserMapper userMapper;

    @BeforeEach
    void setup() {
        userMapper = new UserMapper(new AddressMapper());
        Address address = new Address("RedKeepRoad",
                1,
                1000,
                "Kingslanding");
        AddressDto addressDto = new AddressDto("RedKeepRoad",
                1,
                1000,
                "Kingslanding");
        user = new User("Joffrey",
                "Baratheon",
                "theonetrueking@gmail.com",
                address,
                "0123456789");
        userDto = new UserDto("Joffrey",
                "Baratheon",
                "theonetrueking@gmail.com",
                addressDto,
                "0123456789",
                user.getId());
        createUserDto = new CreateUserDto("Joffrey",
                "Baratheon",
                "theonetrueking@gmail.com",
                addressDto,
                "0123456789");
    }

    @Test
    void toDto_fromUserToUserDto_keepsAllValues() {
        UserDto result = userMapper.toDto(user);
        assertThat(result).usingRecursiveComparison().isEqualTo(userDto);
    }

    @Test
    void toEntity_fromUserDtoToUser_keepsAllValues() {
        User result = userMapper.toEntity(userDto);
        assertThat(result).usingRecursiveComparison().isEqualTo(user);
    }

    @Test
    void toEntity_fromCreateUserDtoToUser_keepsAllValues() {
        User result = userMapper.toEntity(createUserDto);
        assertThat(result).usingRecursiveComparison()
                .ignoringFields("id").isEqualTo(user);
        assertThat(result.getId()).isNotBlank();
    }

}