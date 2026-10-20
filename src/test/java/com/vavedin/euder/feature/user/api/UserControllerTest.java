package com.vavedin.euder.feature.user.api;

import com.vavedin.euder.feature.security.SecurityService;
import com.vavedin.euder.feature.user.api.dto.AddressDto;
import com.vavedin.euder.feature.user.api.dto.CreateUserDto;
import com.vavedin.euder.feature.user.api.dto.UserDto;
import com.vavedin.euder.feature.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    private UserController userController;
    private UserService userService;
    private SecurityService securityService;

    @BeforeEach
    void setup() {
        userService = mock(UserService.class);
        securityService = mock(SecurityService.class);
        userController = new UserController(userService, securityService);
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
                "0123456789",
                "PeopleLoveMe");
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