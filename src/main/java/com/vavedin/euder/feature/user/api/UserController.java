package com.vavedin.euder.feature.user.api;

import com.vavedin.euder.feature.user.api.dto.CreateUserDto;
import com.vavedin.euder.feature.user.api.dto.UserDto;
import com.vavedin.euder.feature.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(@RequestBody CreateUserDto createUserDto) {
    }
}
