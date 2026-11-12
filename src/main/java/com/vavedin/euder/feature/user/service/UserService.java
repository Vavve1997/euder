package com.vavedin.euder.feature.user.service;

import com.vavedin.euder.feature.security.PasswordEncoder;
import com.vavedin.euder.feature.user.api.UserMapper;
import com.vavedin.euder.feature.user.api.dto.CreateUserDto;
import com.vavedin.euder.feature.user.api.dto.UserDto;
import com.vavedin.euder.feature.user.domain.User;
import com.vavedin.euder.feature.user.domain.UserRepository;
import com.vavedin.euder.feature.user.domain.UserRepositoryOutOfUse;
import com.vavedin.euder.feature.user.exception.EmailAlreadyExistsException;
import com.vavedin.euder.feature.user.exception.UserNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDto addUser(CreateUserDto createUserDto) {
        if (userRepository.findByEmail(createUserDto.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(createUserDto.getEmail());
        }
        String hashedPassword = passwordEncoder.encode(createUserDto.getPassword());
        return userMapper.toDto(userRepository.save(userMapper.toEntity(createUserDto, hashedPassword)));
    }

    public List<UserDto> getAllUsers() {
        return userMapper.toDto(userRepository.findAll());
    }

    public List<UserDto> getAllCustomers() {
        return userMapper.toDto(userRepository.findAll()
                .stream().filter(User::isCustomer).toList());
    }


    public UserDto getUserById(String id) {
        return userMapper.toDto(userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id)));
    }
}
