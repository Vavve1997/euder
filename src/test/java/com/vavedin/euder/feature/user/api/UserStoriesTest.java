package com.vavedin.euder.feature.user.api;

import com.vavedin.euder.testsupport.StoryApiTest;
import com.vavedin.euder.feature.user.api.dto.*;
import com.vavedin.euder.feature.user.domain.User;
import com.vavedin.euder.feature.security.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.*;

class UserStoriesTest extends StoryApiTest {
    private Map<String, Object> registration() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("firstName", "Arya");
        body.put("lastName", "Stark");
        body.put("email", "arya@westeros.test");
        body.put("address", new AddressDto("Winterfell", "2A", "0800", "The North"));
        body.put("phoneNumber", "0123456789");
        body.put("password", PASSWORD);
        return body;
    }

    @Test
    @DisplayName("Story 1 - Anyone can register; all customer data is persisted")
    void givenValidCustomer_whenPost_thenCreatedAndStored() {
        var response = guest.assertPost("/users", registration()).response(CREATED);
        UserDto result = response.as(UserDto.class);
        assertThat(result.getId()).isNotBlank();
        assertThat(result.getFirstName()).isEqualTo("Arya");
        assertThat(result.getLastName()).isEqualTo("Stark");
        assertThat(result.getEmail()).isEqualTo("arya@westeros.test");
        assertThat(result.getPhoneNumber()).isEqualTo("0123456789");
        assertThat(result.getAddressDto()).usingRecursiveComparison()
                .isEqualTo(new AddressDto("Winterfell", "2A", "0800", "The North"));
        User stored = users.findById(result.getId()).orElseThrow();
        assertThat(stored.isCustomer()).isTrue();
        assertThat(stored.getFirstName()).isEqualTo("Arya");
        assertThat(stored.getAddress().houseNumber()).isEqualTo("2A");
        assertThat(stored.doesPasswordMatch(PASSWORD, passwords)).isTrue();
        String hash = jdbc.queryForObject("SELECT password_hash FROM eurder.users WHERE id = ?",
                String.class, result.getId());
        assertThat(hash).isNotEqualTo(PASSWORD);
        assertThat(response.jsonPath().getMap("$")).doesNotContainKeys("password", "hashedPassword", "passwordHash");
    }

    @ParameterizedTest
    @ValueSource(strings = {"firstName", "lastName", "email", "address", "phoneNumber", "password"})
    @DisplayName("Story 1 - Missing required data is rejected without saving a customer")
    void givenMissingField_whenPost_thenBadRequest(String field) {
        var body = registration();
        body.remove(field);
        long before = users.count();
        guest.assertPost("/users", body).failsWith(BAD_REQUEST);
        assertThat(users.count()).isEqualTo(before);
    }

    @Test
    @DisplayName("Story 1 - Duplicate email is rejected")
    void givenExistingEmail_whenRegisterAgain_thenBadRequest() {
        var body = registration();
        body.put("email", stannis.getEmail());
        guest.assertPost("/users", body).failsWith(BAD_REQUEST);
        assertThat(users.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Story 7 - Admin sees all customers, excluding admins")
    void givenTwoCustomers_whenAdminListsCustomers_thenBothReturned() {
        User arya = saveUser("arya", Role.MEMBER);
        UserDto[] result = admin.assertGet("/users/customers").returns(UserDto[].class);
        assertThat(result).extracting(UserDto::getId)
                .containsExactlyInAnyOrder(stannis.getId(), arya.getId());
    }

    @Test
    @DisplayName("Story 8 - Admin sees the requested customer's complete details")
    void givenCustomer_whenAdminGetsDetails_thenCorrectCustomerReturned() {
        UserDto result = admin.assertGet("/users/{id}", stannis.getId()).returns(UserDto.class);
        assertThat(result.getId()).isEqualTo(stannis.getId());
        assertThat(result.getFirstName()).isEqualTo(stannis.getFirstName());
        assertThat(result.getLastName()).isEqualTo(stannis.getLastName());
        assertThat(result.getEmail()).isEqualTo(stannis.getEmail());
        assertThat(result.getPhoneNumber()).isEqualTo(stannis.getPhoneNumber());
        assertThat(result.getAddressDto()).usingRecursiveComparison()
                .isEqualTo(new AddressDto("Dragonstone Keep", "1", "1000", "Dragonstone"));
    }

    @Test
    void givenUnknownCustomer_whenAdminGetsDetails_thenNotFound() {
        admin.assertGet("/users/unknown").failsWith(NOT_FOUND);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/users/customers", "/users/unknown"})
    void givenMemberOrGuest_whenReadingCustomers_thenAccessDenied(String path) {
        member.assertGet(path).failsWith(FORBIDDEN);
        guest.assertGet(path).failsWith(UNAUTHORIZED);
    }
}
