package com.vavedin.euder.feature.user.api;

import com.vavedin.euder.feature.security.PasswordEncoder;
import com.vavedin.euder.feature.security.enums.Role;
import com.vavedin.euder.feature.user.api.dto.AddressDto;
import com.vavedin.euder.feature.user.api.dto.UserDto;
import com.vavedin.euder.feature.user.domain.Address;
import com.vavedin.euder.feature.user.domain.User;
import com.vavedin.euder.feature.user.domain.UserDatabase;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserReadEndToEndTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserDatabase userDatabase;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User admin;
    private User customer;
    private User secondCustomer;

    private final String password = "Secret123!";

    @BeforeEach
    void setup() {
        admin = createUser("Admin", Role.ADMIN);
        customer = createUser("Stannis", Role.MEMBER);
        secondCustomer = createUser("Davos", Role.MEMBER);
    }

    private User createUser(String firstName, Role role) {
        String id = UUID.randomUUID().toString();

        User user = new User(
                firstName,
                "Test",
                id + "@example.com",
                new Address("Teststraat", 12, 3000, "Leuven"),
                "0123456789",
                id,
                role,
                passwordEncoder.encode(password)
        );

        userDatabase.getUserDatabase().put(id, user);
        return user;
    }

    @AfterEach
    void cleanup() {
        userDatabase.getUserDatabase().remove(admin.getId());
        userDatabase.getUserDatabase().remove(customer.getId());
        userDatabase.getUserDatabase().remove(secondCustomer.getId());
    }

    @Test
    void getAllUsers_asAdmin_returnsCustomersWithoutPasswords() {
        Response response = given()
                .baseUri("http://localhost")
                .port(port)
                .auth().preemptive().basic(admin.getEmail(), password)
                .when()
                .get("/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .response();

        UserDto[] result = response.as(UserDto[].class);

        assertThat(result)
                .extracting(UserDto::getId)
                .contains(customer.getId(), secondCustomer.getId())
                .doesNotHaveDuplicates();

        // Controleer de echte JSON: een DTO zonder password-veld
        // zou extra JSON-velden bij het inlezen kunnen negeren.
        for (Object entry : response.jsonPath().getList("$")) {
            Map<?, ?> userJson = (Map<?, ?>) entry;

            assertThat(userJson.containsKey("password")).isFalse();
            assertThat(userJson.containsKey("hashedPassword")).isFalse();
        }
    }

    @Test
    void getUserById_asAdmin_returnsTheRequestedCustomer() {
        Response response = given()
                .baseUri("http://localhost")
                .port(port)
                .auth().preemptive().basic(admin.getEmail(), password)
                .when()
                .get("/users/{id}", customer.getId())
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .response();

        UserDto result = response.as(UserDto.class);

        assertThat(result.getId()).isEqualTo(customer.getId());
        assertThat(result.getFirstName()).isEqualTo("Stannis");
        assertThat(result.getLastName()).isEqualTo("Test");
        assertThat(result.getEmail()).isEqualTo(customer.getEmail());
        assertThat(result.getPhoneNumber()).isEqualTo("0123456789");
        assertThat(result.getAddressDto())
                .usingRecursiveComparison()
                .isEqualTo(new AddressDto(
                        "Teststraat", 12, 3000, "Leuven"
                ));

        Map<String, Object> userJson = response.jsonPath().getMap("$");

        assertThat(userJson).doesNotContainKeys(
                "password", "hashedPassword"
        );
    }

    @Test
    void getUserById_withUnknownId_returnsBadRequest() {
        given()
                .baseUri("http://localhost")
                .port(port)
                .auth().preemptive().basic(admin.getEmail(), password)
                .when()
                .get("/users/{id}", UUID.randomUUID().toString())
                .then()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void getAllUsers_withoutAuthentication_returnsUnauthorized() {
        given()
                .baseUri("http://localhost")
                .port(port)
                .when()
                .get("/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .header("WWW-Authenticate", startsWith("Basic "));
    }

    @Test
    void getUserById_withoutAuthentication_returnsUnauthorized() {
        given()
                .baseUri("http://localhost")
                .port(port)
                .when()
                .get("/users/{id}", customer.getId())
                .then()
                .assertThat()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .header("WWW-Authenticate", startsWith("Basic "));
    }

    @Test
    void getAllUsers_asCustomer_returnsForbidden() {
        given()
                .baseUri("http://localhost")
                .port(port)
                .auth().preemptive().basic(customer.getEmail(), password)
                .when()
                .get("/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void getUserById_asCustomer_returnsForbidden() {
        given()
                .baseUri("http://localhost")
                .port(port)
                .auth().preemptive().basic(customer.getEmail(), password)
                .when()
                .get("/users/{id}", secondCustomer.getId())
                .then()
                .assertThat()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }
}