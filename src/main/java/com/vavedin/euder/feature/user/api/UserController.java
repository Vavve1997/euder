package com.vavedin.euder.feature.user.api;

import com.vavedin.euder.feature.security.SecurityService;
import com.vavedin.euder.feature.security.enums.Feature;
import com.vavedin.euder.feature.user.api.dto.CreateUserDto;
import com.vavedin.euder.feature.user.api.dto.UserDto;
import com.vavedin.euder.feature.user.domain.User;
import com.vavedin.euder.feature.user.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
public class UserController {

    private final UserService userService;
    private final SecurityService securityService;

    public UserController(UserService userService, SecurityService securityService) {
        this.userService = userService;
        this.securityService = securityService;
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(@RequestBody CreateUserDto createUserDto) {
        return userService.addUser(createUserDto);
    }

    @GetMapping(path = "/customers", produces = "application/json")
    @SecurityRequirement(name = "basicAuth")
    public List<UserDto> getAllCustomers(@Parameter(hidden = true)
                                  @RequestHeader(value = "Authorization", required = false)
                                      String authorization) {
        securityService.validateAuthorization(authorization, Feature.VIEW_CUSTOMERS);
        return userService.getAllCustomers();
    }

    @GetMapping( produces = "application/json")
    @SecurityRequirement(name = "basicAuth")
    public List<UserDto> getAllUsers(@Parameter(hidden = true)
                                         @RequestHeader(value = "Authorization", required = false)
                                         String authorization) {
        securityService.validateAuthorization(authorization, Feature.VIEW_USERS);
        return userService.getAllUsers();
    }

    @GetMapping(path = "/{id}", produces = "application/json")
    @SecurityRequirement(name = "basicAuth")
    public UserDto getUserById(@Parameter(hidden = true)
                                     @RequestHeader(value = "Authorization", required = false)
                                     String authorization,
                                     @PathVariable String id) {
        securityService.validateAuthorization(authorization, Feature.VIEW_USERS);
        return userService.getUserById(id);
    }

}
