package com.vavedin.euder.feature.security;

import com.vavedin.euder.feature.security.enums.Role;
import com.vavedin.euder.feature.security.exceptions.*;
import com.vavedin.euder.feature.user.domain.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import static com.vavedin.euder.feature.security.enums.Feature.ADD_ITEM;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityServiceTest {
    private UserRepository repository;
    private PasswordEncoder encoder;
    private SecurityService service;
    @BeforeEach void setup() {
        repository = mock(UserRepository.class);
        encoder = mock(PasswordEncoder.class);
        service = new SecurityService(repository, encoder);
    }
    private User user(Role role) {
        return new User("Test", "User", "test@example.com",
                new Address("Street", 1, 1000, "City"), "012345", "user-id", role, "stored-hash");
    }
    private String basic(String credentials) {
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }
    @ParameterizedTest
    @ValueSource(strings = {"Basic ", "basic ", "BASIC "})
    void validAdmin_isReturned_andPasswordMayContainColon(String prefix) {
        User admin = user(Role.ADMIN);
        when(repository.findByEmail("test@example.com")).thenReturn(Optional.of(admin));
        when(encoder.matches("secret:with:colon", "stored-hash")).thenReturn(true);
        String header = prefix + basic("test@example.com:secret:with:colon").substring(6);
        assertThat(service.validateAuthorization(header, ADD_ITEM)).isSameAs(admin);
        verify(encoder).matches("secret:with:colon", "stored-hash");
    }
    @ParameterizedTest @NullAndEmptySource
    @ValueSource(strings = {" ", "Bearer token", "Basic !!!", "Basic ", "Basic bm9jb2xvbg=="})
    void malformedHeader_isUnauthorized_withoutLookingUpUsers(String header) {
        assertThatThrownBy(() -> service.validateAuthorization(header, ADD_ITEM))
                .isInstanceOf(UnauthorizedException.class);
        verifyNoInteractions(repository, encoder);
    }
    @Test void unknownEmail_isUnauthorized() {
        when(repository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.validateAuthorization(basic("missing@example.com:secret"), ADD_ITEM))
                .isInstanceOf(UnauthorizedException.class);
        verifyNoInteractions(encoder);
    }
    @Test void wrongPassword_isUnauthorized_evenForAdmin() {
        when(repository.findByEmail("test@example.com")).thenReturn(Optional.of(user(Role.ADMIN)));
        when(encoder.matches("wrong", "stored-hash")).thenReturn(false);
        assertThatThrownBy(() -> service.validateAuthorization(basic("test@example.com:wrong"), ADD_ITEM))
                .isInstanceOf(UnauthorizedException.class);
    }
    @Test void authenticatedMember_cannotAddItems() {
        when(repository.findByEmail("test@example.com")).thenReturn(Optional.of(user(Role.MEMBER)));
        when(encoder.matches("secret", "stored-hash")).thenReturn(true);
        assertThatThrownBy(() -> service.validateAuthorization(basic("test@example.com:secret"), ADD_ITEM))
                .isInstanceOf(FeatureAccessDeniedException.class);
    }
}
