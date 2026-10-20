package com.vavedin.euder.feature.item.api;

import com.vavedin.euder.feature.item.api.dto.ItemDto;
import com.vavedin.euder.feature.item.domain.Item;
import com.vavedin.euder.feature.item.domain.ItemDatabase;
import com.vavedin.euder.feature.security.PasswordEncoder;
import com.vavedin.euder.feature.security.enums.Role;
import com.vavedin.euder.feature.user.domain.Address;
import com.vavedin.euder.feature.user.domain.User;
import com.vavedin.euder.feature.user.domain.UserDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ItemUpdateEndToEndTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ItemDatabase itemDatabase;

    @Autowired
    private UserDatabase userDatabase;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Item item;
    private User admin;
    private User customer;
    private String unknownItemId;

    private final String password = "Secret123!";

    private final String validUpdate = """
            {
              "name": "Premium coffee",
              "description": "Updated description",
              "price": 12.50,
              "amount": 8
            }
            """;

    @BeforeEach
    void setup() {
        item = new Item(
                "Coffee",
                "Original description",
                new BigDecimal("10.00"),
                5
        );

        itemDatabase.getItemDatabase().put(item.getId(), item);

        admin = createUser(Role.ADMIN);
        customer = createUser(Role.MEMBER);
        unknownItemId = UUID.randomUUID().toString();
    }

    private User createUser(Role role) {
        String id = UUID.randomUUID().toString();

        User user = new User(
                "Test",
                "User",
                id + "@example.com",
                new Address("Teststraat", 1, 3000, "Leuven"),
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
        itemDatabase.getItemDatabase().remove(item.getId());
        itemDatabase.getItemDatabase().remove(unknownItemId);
        userDatabase.getUserDatabase().remove(admin.getId());
        userDatabase.getUserDatabase().remove(customer.getId());
    }

    @Test
    void updateItem_asAdmin_updatesTheExistingItem() {
        ItemDto result = given()
                .baseUri("http://localhost")
                .port(port)
                .auth().preemptive().basic(admin.getEmail(), password)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(validUpdate)
                .when()
                .put("/items/{id}", item.getId())
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(ItemDto.class);

        assertThat(result.getId()).isEqualTo(item.getId());
        assertThat(result.getName()).isEqualTo("Premium coffee");
        assertThat(result.getDescription()).isEqualTo("Updated description");
        assertThat(result.getPrice()).isEqualByComparingTo("12.50");
        assertThat(result.getAmount()).isEqualTo(8);

        Item savedItem = itemDatabase.getItemDatabase().get(item.getId());

        assertThat(savedItem.getName()).isEqualTo("Premium coffee");
        assertThat(savedItem.getDescription()).isEqualTo("Updated description");
        assertThat(savedItem.getPrice()).isEqualByComparingTo("12.50");
        assertThat(savedItem.getAmount()).isEqualTo(8);
    }

    @Test
    void updateItem_withoutAuthentication_returnsUnauthorized() {
        given()
                .baseUri("http://localhost")
                .port(port)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(validUpdate)
                .when()
                .put("/items/{id}", item.getId())
                .then()
                .assertThat()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .header("WWW-Authenticate", startsWith("Basic "));

        assertOriginalItemIsUnchanged();
    }

    @Test
    void updateItem_asCustomer_returnsForbidden() {
        given()
                .baseUri("http://localhost")
                .port(port)
                .auth().preemptive().basic(customer.getEmail(), password)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(validUpdate)
                .when()
                .put("/items/{id}", item.getId())
                .then()
                .assertThat()
                .statusCode(HttpStatus.FORBIDDEN.value());

        assertOriginalItemIsUnchanged();
    }

    @Test
    void updateItem_withUnknownId_returnsNotFoundWithoutCreatingAnItem() {
        given()
                .baseUri("http://localhost")
                .port(port)
                .auth().preemptive().basic(admin.getEmail(), password)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(validUpdate)
                .when()
                .put("/items/{id}", unknownItemId)
                .then()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value());

        assertThat(itemDatabase.getItemDatabase())
                .doesNotContainKey(unknownItemId);

        assertOriginalItemIsUnchanged();
    }

    @Test
    void updateItem_withBlankName_returnsBadRequestWithoutChangingTheItem() {
        given()
                .baseUri("http://localhost")
                .port(port)
                .auth().preemptive().basic(admin.getEmail(), password)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        {
                          "name": " ",
                          "description": "Changed description",
                          "price": 12.50,
                          "amount": 8
                        }
                        """)
                .when()
                .put("/items/{id}", item.getId())
                .then()
                .assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value());

        assertOriginalItemIsUnchanged();
    }

    private void assertOriginalItemIsUnchanged() {
        Item savedItem = itemDatabase.getItemDatabase().get(item.getId());

        assertThat(savedItem).isNotNull();
        assertThat(savedItem.getName()).isEqualTo("Coffee");
        assertThat(savedItem.getDescription()).isEqualTo("Original description");
        assertThat(savedItem.getPrice()).isEqualByComparingTo("10.00");
        assertThat(savedItem.getAmount()).isEqualTo(5);
    }
}