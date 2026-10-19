package com.vavedin.euder.feature.user.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

    private UserRepository userRepository;
    private UserDatabase userDatabase;
    private User user;
    private Address address;

    @BeforeEach
    void setup() {
        userDatabase = new UserDatabase();
        userRepository = new UserRepository(userDatabase);
        address = new Address("RedKeepRoad",
                1,
                1000,
                "Kingslanding");
        user = new User("Joffrey",
                "Baratheon",
                "theonetrueking@gmail.com",
                address,
                "0123456789");
    }

    @Test
    void addUser_userIsAddedToDatabase() {
        userRepository.addUser(user);

        assertThat(userDatabase.getUserDatabase().values())
                .containsExactly(user);

    }


}