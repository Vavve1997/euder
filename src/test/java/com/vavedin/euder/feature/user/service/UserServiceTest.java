package com.vavedin.euder.feature.user.service;

import com.vavedin.euder.feature.security.PasswordEncoder;
import com.vavedin.euder.feature.user.api.AddressMapper;
import com.vavedin.euder.feature.user.api.UserMapper;
import com.vavedin.euder.feature.user.api.dto.AddressDto;
import com.vavedin.euder.feature.user.api.dto.CreateUserDto;
import com.vavedin.euder.feature.user.api.dto.UserDto;
import com.vavedin.euder.feature.user.domain.User;
import com.vavedin.euder.feature.user.domain.UserRepository;
import com.vavedin.euder.feature.user.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setup() {
        userRepository = Mockito.mock(UserRepository.class);
        userService = new UserService(new UserMapper(new AddressMapper()), userRepository, new PasswordEncoder());
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
                "0123456789",
                "PeopleLoveMe");
        when(userRepository.addUser(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.addUser(userDto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).addUser(captor.capture());

        User savedUser = captor.getValue();

        assertThat(savedUser)
                .usingRecursiveComparison()
                .ignoringFields("id", "address", "password", "hashedPassword", "role")
                .isEqualTo(userDto);

        UserDto expected = new UserDto(
                "Joffrey",
                "Baratheon",
                "theonetrueking@gmail.com",
                address,
                "0123456789",
                savedUser.getId()
        );

        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void getAllUsers_withoutUsers_returnsAnEmptyList() {
        when(userRepository.getAllUsers()).thenReturn(List.of());

        List<UserDto> result = userService.getAllUsers();

        assertThat(result).isEmpty();
    }

    @Test
    void getUserById_withUnknownId_throwsIllegalArgumentException() {
        when(userRepository.findById("unknown-id"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById("unknown-id"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("No user was found for id unknown-id");
    }

}