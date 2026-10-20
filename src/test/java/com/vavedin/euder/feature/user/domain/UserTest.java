package com.vavedin.euder.feature.user.domain;

import com.vavedin.euder.feature.security.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


class UserTest {
    private static final String HASHED_PASSWORD = "test-password-hash";
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
                "0123456789",
                "PeopleLoveMe");

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
                        "0123456789"
                        , HASHED_PASSWORD));
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
                        "0123456789",
                        HASHED_PASSWORD));
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
                        "0123456789",
                        HASHED_PASSWORD));
    }

    @Test
    void constructor_userCanNotBeCreatedWithMissingAddress() {
        assertThrows(IllegalArgumentException.class,
                () -> new User("Joffrey",
                        "Baratheon",
                        "theonetrueking@gmail.com",
                        null,
                        "0123456789",
                        HASHED_PASSWORD));
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
                        phoneNumber,
                        HASHED_PASSWORD));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void constructor_userCanNotBeCreatedWithMissingPassword(String password) {
        assertThrows(IllegalArgumentException.class,
                () -> new User("Joffrey",
                        "Baratheon",
                        "theonetrueking@gmail.com",
                        address,
                        "0123456789",
                        password));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void doesPasswordMatch_returnsEncoderResult(boolean matches) {
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        User user = new User(
                "Joffrey",
                "Baratheon",
                "theonetrueking@gmail.com",
                address,
                "0123456789",
                HASHED_PASSWORD
        );

        when(passwordEncoder.matches("PeopleLoveMe", HASHED_PASSWORD))
                .thenReturn(matches);

        boolean result =
                user.doesPasswordMatch("PeopleLoveMe", passwordEncoder);

        assertThat(result).isEqualTo(matches);
        verify(passwordEncoder).matches("PeopleLoveMe", HASHED_PASSWORD);
    }
}