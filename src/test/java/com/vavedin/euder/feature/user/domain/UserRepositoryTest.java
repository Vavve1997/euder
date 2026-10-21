package com.vavedin.euder.feature.user.domain;

import com.vavedin.euder.feature.security.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

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
                "0123456789",
                "PeopleLoveMe");
    }

    @Test
    void addUser_userIsStoredUnderItsIdAndReturned() {
        User result = userRepository.addUser(user);

        assertThat(userDatabase.getUserDatabase())
                .containsEntry(user.getId(), user);

        assertThat(result).isSameAs(user);
    }


}