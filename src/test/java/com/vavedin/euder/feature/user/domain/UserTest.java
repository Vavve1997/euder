package com.vavedin.euder.feature.user.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;


class UserTest {
    private Address address;

    @BeforeEach
    void setup() {
        address = new Address("RedKeepRoad",
                1,
                1000,
                "Kingslanding");
    }

    @Test
    void constructor_containsAllExpectedValuesAfterInitialisation() {
        User user = new User("Joffrey",
                "Baratheon",
                "theonetrueking@gmail.com",
                address,
                "0123456789");

        assertThat(user.getFirstName()).isEqualTo("Joffrey");
        assertThat(user.getLastName()).isEqualTo("Baratheon");
        assertThat(user.getEmail()).isEqualTo("theonetrueking@gmail.com");
        assertThat(user.getAddress()).isEqualTo(address);
        assertThat(user.getPhoneNumber()).isEqualTo("0123456789");
        assertThat(user.getId()).isNotEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void constructor_userCanNotBeCreatedWithMissingFirstname(String firstName) {
        assertThrows(IllegalArgumentException.class,
                () -> new User(firstName,
                        "Baratheon",
                        "theonetrueking@gmail.com",
                        address,
                        "0123456789"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void constructor_userCanNotBeCreatedWithMissingLastname(String lastName) {
        assertThrows(IllegalArgumentException.class,
                () -> new User("Joffrey",
                        lastName,
                        "theonetrueking@gmail.com",
                        address,
                        "0123456789"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void constructor_userCanNotBeCreatedWithMissingEmail(String email) {
        assertThrows(IllegalArgumentException.class,
                () -> new User("Joffrey",
                        "Baratheon",
                        email,
                        address,
                        "0123456789"));
    }

    @Test
    void constructor_userCanNotBeCreatedWithMissingAddress() {
        assertThrows(IllegalArgumentException.class,
                () -> new User("Joffrey",
                        "Baratheon",
                        "theonetrueking@gmail.com",
                        null,
                        "0123456789"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void constructor_userCanNotBeCreatedWithMissingPhoneNumber(String phoneNumber) {
        assertThrows(IllegalArgumentException.class,
                () -> new User("Joffrey",
                        "Baratheon",
                        "theonetrueking@gmail.com",
                        address,
                        phoneNumber));
    }

}