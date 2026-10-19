package com.vavedin.euder.feature.user.service;

import com.vavedin.euder.feature.user.api.AddressMapper;
import com.vavedin.euder.feature.user.api.UserMapper;
import com.vavedin.euder.feature.user.api.dto.AddressDto;
import com.vavedin.euder.feature.user.api.dto.CreateUserDto;
import com.vavedin.euder.feature.user.api.dto.UserDto;
import com.vavedin.euder.feature.user.domain.Address;
import com.vavedin.euder.feature.user.domain.User;
import com.vavedin.euder.feature.user.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setup() {
        userRepository = Mockito.mock(UserRepository.class);
        userService = new UserService(new UserMapper(new AddressMapper()), userRepository);
    }

    @Test
    void addUser_callsAddUserMethodInUserRepositoryWithMappedUser() {
        AddressDto address = new AddressDto("RedKeepRoad",
                1,
                1000,
                "Kingslanding");
        CreateUserDto userDto = new CreateUserDto("Joffrey",
                "Baratheon",
                "theonetrueking@gmail.com",
                address,
                "0123456789");
        when(userRepository.addUser(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.addUser(userDto);

        verify(userRepository).addUser(any(User.class));

        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(userDto);
    }

}