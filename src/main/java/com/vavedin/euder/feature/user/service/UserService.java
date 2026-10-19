package com.vavedin.euder.feature.user.service;

import com.vavedin.euder.feature.user.api.UserMapper;
import com.vavedin.euder.feature.user.api.dto.CreateUserDto;
import com.vavedin.euder.feature.user.api.dto.UserDto;
import com.vavedin.euder.feature.user.domain.User;
import com.vavedin.euder.feature.user.domain.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;

    public UserService(UserMapper userMapper, UserRepository userRepository) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
    }

    public UserDto addUser(CreateUserDto createUserDto) {
        return userMapper.toDto(userRepository.addUser(userMapper.toEntity(createUserDto)));
    }
}
