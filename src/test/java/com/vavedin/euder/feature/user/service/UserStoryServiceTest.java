package com.vavedin.euder.feature.user.service;

import com.vavedin.euder.feature.user.api.*;
import com.vavedin.euder.feature.user.api.dto.*;
import com.vavedin.euder.feature.user.domain.*;
import com.vavedin.euder.feature.user.exception.EmailAlreadyExistsException;
import com.vavedin.euder.feature.security.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserStoryServiceTest {
    @Mock private UserRepository users;
    @Mock private PasswordEncoder passwords;
    private UserService service;

    @BeforeEach
    void setUp() {
        // Real mappers: assertions cover the complete mapping as well.
        service = new UserService(new UserMapper(new AddressMapper()), users, passwords);
    }

    private CreateUserDto request() {
        return new CreateUserDto("Arya", "Stark", "arya@westeros.test",
                new AddressDto("Winterfell", "1", "0800", "North"), "0123456789", "Needle!");
    }

    @Test
    @DisplayName("Story 1 - Service hashes the password before saving")
    void givenNewCustomer_whenRegister_thenHashedPasswordSaved() {
        when(users.findByEmail("arya@westeros.test")).thenReturn(Optional.empty());
        when(passwords.encode("Needle!")).thenReturn("encoded-password");
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UserDto result = service.addUser(request());
        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(users).save(captor.capture());
        when(passwords.matches("Needle!", "encoded-password")).thenReturn(true);
        assertThat(captor.getValue().doesPasswordMatch("Needle!", passwords)).isTrue();
        assertThat(captor.getValue().isCustomer()).isTrue();
        assertThat(result.getId()).isEqualTo(captor.getValue().getId());
        assertThat(result.getEmail()).isEqualTo("arya@westeros.test");
        verify(passwords).encode("Needle!");
    }

    @Test
    void givenDuplicateEmail_whenRegister_thenNothingSaved() {
        User existing = new User("Arya", "Stark", "arya@westeros.test",
                new Address("Winterfell", "1", "0800", "North"), "0123456789", "hash");
        when(users.findByEmail("arya@westeros.test")).thenReturn(Optional.of(existing));
        assertThatThrownBy(() -> service.addUser(request())).isInstanceOf(EmailAlreadyExistsException.class);
        verify(users, never()).save(any());
        verifyNoInteractions(passwords);
    }
}
