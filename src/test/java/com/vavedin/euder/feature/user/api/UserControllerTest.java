package com.vavedin.euder.feature.user.api;

import com.vavedin.euder.feature.user.api.dto.AddressDto;
import com.vavedin.euder.feature.user.api.dto.CreateUserDto;
import com.vavedin.euder.feature.user.api.dto.UserDto;
import com.vavedin.euder.feature.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserControllerTest {

    private UserController userController;
    private UserService userService;

    @BeforeEach
    void setup() {
        userService = Mockito.mock(UserService.class);
        userController = new UserController(userService);
    }

    @Test
    void addUser_callsAddUserInUserServiceAndReturnsUserDto() {
        AddressDto address = new AddressDto("RedKeepRoad",
                1,
                1000,
                "Kingslanding");
        CreateUserDto createUserDto = new CreateUserDto("Joffrey",
                "Baratheon",
                "theonetrueking@gmail.com",
                address,
                "0123456789");
        UserDto userDto = new UserDto(createUserDto.getFirstName(),
                createUserDto.getLastName(),
                createUserDto.getEmail(),
                createUserDto.getAddress(),
                createUserDto.getPhoneNumber(),
                "anyId");
        when(userService.addUser(createUserDto))
                .thenReturn(userDto);

        UserDto result = userController.addUser(createUserDto);

        verify(userService).addUser(createUserDto);
        assertThat(result)
                .isSameAs(userDto);
    }

}