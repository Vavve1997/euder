package com.vavedin.euder.feature.eurder.api;

import com.vavedin.euder.feature.eurder.domain.Eurder;
import com.vavedin.euder.feature.eurder.domain.EurderDatabase;
import com.vavedin.euder.feature.item.domain.Item;
import com.vavedin.euder.feature.item.domain.ItemDatabase;
import com.vavedin.euder.feature.security.PasswordEncoder;
import com.vavedin.euder.feature.security.enums.Role;
import com.vavedin.euder.feature.user.domain.Address;
import com.vavedin.euder.feature.user.domain.User;
import com.vavedin.euder.feature.user.domain.UserDatabase;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EurderEndToEndTest {

    @LocalServerPort
    private int port;

    // Dezelfde databases die de draaiende applicatie gebruikt.
    @Autowired
    private UserDatabase userDatabase;
    @Autowired
    private ItemDatabase itemDatabase;
    @Autowired
    private EurderDatabase eurderDatabase;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User customer;
    private User admin;
    private Item coffee;
    private Item toaster;
    private final String password = "Secret123!";

    @BeforeEach
    void setUp() {
        String customerId = UUID.randomUUID().toString();
        customer = new User("Test", "Customer", customerId + "@example.com",
                new Address("Teststraat", 1, 3000, "Leuven"), "0123456789",
                customerId, Role.MEMBER, passwordEncoder.encode(password));
        userDatabase.getUserDatabase().put(customer.getId(), customer);

        String adminId = UUID.randomUUID().toString();
        admin = new User("Test", "Admin", adminId + "@example.com",
                new Address("Teststraat", 2, 3000, "Leuven"), "0123456789",
                adminId, Role.ADMIN, passwordEncoder.encode(password));
        userDatabase.getUserDatabase().put(admin.getId(), admin);

        coffee = new Item("Coffee", "Good coffee", new BigDecimal("89.95"), 3);
        toaster = new Item("Toaster", "Toasts bread", new BigDecimal("29.95"), 0);
        itemDatabase.getItemDatabase().put(coffee.getId(), coffee);
        itemDatabase.getItemDatabase().put(toaster.getId(), toaster);
    }

    @AfterEach
    void cleanUp() {
        // Ook opruimen wanneer een assertion faalt, zonder andere tests hun data te wissen.
        for (Eurder eurder : eurderDatabase.getEurderDatabase().values()) {
            if (eurder.getCustomerId().equals(customer.getId())
                    || eurder.getCustomerId().equals(admin.getId())) {
                eurderDatabase.getEurderDatabase().remove(eurder.getId());
            }
        }
        userDatabase.getUserDatabase().remove(customer.getId());
        userDatabase.getUserDatabase().remove(admin.getId());
        itemDatabase.getItemDatabase().remove(coffee.getId());
        itemDatabase.getItemDatabase().remove(toaster.getId());
    }

    @Test
    void addEurder_aCustomerCanOrderMultipleItems() {
        LocalDate beforeRequest = LocalDate.now();

        JsonPath response = given()
                .baseUri("http://localhost")
                .port(port)
                .auth().preemptive().basic(customer.getEmail(), password)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        {
                          "itemGroupDtos": [
                            { "itemId": "%s", "amount": 3 },
                            { "itemId": "%s", "amount": 2 }
                          ]
                        }
                        """.formatted(coffee.getId(), toaster.getId()))
                .when()
                .post("/eurders")
                .then()
                .assertThat()
                .statusCode(HttpStatus.CREATED.value())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .extract()
                .jsonPath();

        LocalDate afterRequest = LocalDate.now();

        // De request heet itemGroupDtos, maar de response heet itemGroups (door de getter).
        String eurderId = response.getString("id");
        assertThat(eurderId).isNotBlank();
        assertThat(response.getString("customerId")).isEqualTo(customer.getId());
        assertThat(response.getList("itemGroups")).hasSize(2);
        assertThat(new BigDecimal(response.getString("totalPrice"))).isEqualByComparingTo("329.75");

        assertThat(response.getString("itemGroups[0].itemId")).isEqualTo(coffee.getId());
        assertThat(response.getString("itemGroups[0].itemName")).isEqualTo("Coffee");
        assertThat(response.getInt("itemGroups[0].amount")).isEqualTo(3);
        assertThat(new BigDecimal(response.getString("itemGroups[0].unitPrice"))).isEqualByComparingTo("89.95");
        assertThat(new BigDecimal(response.getString("itemGroups[0].totalPrice"))).isEqualByComparingTo("269.85");

        assertThat(LocalDate.parse(response.getString("itemGroups[0].shippingDate")))
                .isBetween(beforeRequest.plusDays(1), afterRequest.plusDays(1));

        assertThat(response.getString("itemGroups[1].itemId")).isEqualTo(toaster.getId());
        assertThat(response.getString("itemGroups[1].itemName")).isEqualTo("Toaster");
        assertThat(response.getInt("itemGroups[1].amount")).isEqualTo(2);
        assertThat(new BigDecimal(response.getString("itemGroups[1].unitPrice"))).isEqualByComparingTo("29.95");
        assertThat(new BigDecimal(response.getString("itemGroups[1].totalPrice"))).isEqualByComparingTo("59.90");
        assertThat(LocalDate.parse(response.getString("itemGroups[1].shippingDate")))
                .isBetween(beforeRequest.plusDays(7), afterRequest.plusDays(7));

        Eurder savedEurder = eurderDatabase.getEurderDatabase().get(eurderId);
        assertThat(savedEurder).isNotNull();
        assertThat(savedEurder.getCustomerId()).isEqualTo(customer.getId());
        assertThat(savedEurder.getItemGroups()).hasSize(2);
        assertThat(savedEurder.calculateTotalPrice()).isEqualByComparingTo("329.75");
    }

    @Test
    void addEurder_withoutAuthentication_returnsUnauthorizedWithoutSaving() {
        Map<String, Eurder> beforeRequest = new HashMap<>(eurderDatabase.getEurderDatabase());

        given()
                .baseUri("http://localhost")
                .port(port)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        { "itemGroupDtos": [{ "itemId": "%s", "amount": 1 }] }
                        """.formatted(coffee.getId()))
                .when()
                .post("/eurders")
                .then()
                .assertThat()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .header("WWW-Authenticate", startsWith("Basic "));


        assertThat(eurderDatabase.getEurderDatabase())
                .containsExactlyInAnyOrderEntriesOf(beforeRequest);
    }

    @Test
    void addEurder_withWrongPassword_returnsUnauthorizedWithoutSaving() {
        Map<String, Eurder> beforeRequest = new HashMap<>(eurderDatabase.getEurderDatabase());

        given()
                .baseUri("http://localhost")
                .port(port)
                .auth().preemptive().basic(customer.getEmail(), "wrong-password")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        { "itemGroupDtos": [{ "itemId": "%s", "amount": 1 }] }
                        """.formatted(coffee.getId()))
                .when()
                .post("/eurders")
                .then()
                .assertThat()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .header("WWW-Authenticate", startsWith("Basic "));


        assertThat(eurderDatabase.getEurderDatabase())
                .containsExactlyInAnyOrderEntriesOf(beforeRequest);
    }

    @Test
    void addEurder_asAdminWithoutOrderPermission_returnsForbiddenWithoutSaving() {
        Map<String, Eurder> beforeRequest = new HashMap<>(eurderDatabase.getEurderDatabase());

        given()
                .baseUri("http://localhost")
                .port(port)
                .auth().preemptive().basic(admin.getEmail(), password)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        { "itemGroupDtos": [{ "itemId": "%s", "amount": 1 }] }
                        """.formatted(coffee.getId()))
                .when()
                .post("/eurders")
                .then()
                .assertThat()
                .statusCode(HttpStatus.FORBIDDEN.value());


        assertThat(eurderDatabase.getEurderDatabase())
                .containsExactlyInAnyOrderEntriesOf(beforeRequest);
    }

    @Test
    void addEurder_withAnUnknownItem_returnsBadRequestWithoutSaving() {
        Map<String, Eurder> beforeRequest = new HashMap<>(eurderDatabase.getEurderDatabase());

        given()
                .baseUri("http://localhost")
                .port(port)
                .auth().preemptive().basic(customer.getEmail(), password)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        { "itemGroupDtos": [{ "itemId": "%s", "amount": 1 }, { "itemId": "unknown-item", "amount": 1 }] }
                        """.formatted(coffee.getId()))
                .when()
                .post("/eurders")
                .then()
                .assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value());


        assertThat(eurderDatabase.getEurderDatabase())
                .containsExactlyInAnyOrderEntriesOf(beforeRequest);
    }

    @Test
    void addEurder_withAnEmptyList_returnsBadRequestWithoutSaving() {
        Map<String, Eurder> beforeRequest = new HashMap<>(eurderDatabase.getEurderDatabase());

        given()
                .baseUri("http://localhost")
                .port(port)
                .auth().preemptive().basic(customer.getEmail(), password)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        { "itemGroupDtos": [] }
                        """)
                .when()
                .post("/eurders")
                .then()
                .assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value());

        assertThat(eurderDatabase.getEurderDatabase())
                .containsExactlyInAnyOrderEntriesOf(beforeRequest);
    }

    @Test
    void addEurder_withoutAnItemGroupList_returnsBadRequestWithoutSaving() {
        Map<String, Eurder> beforeRequest = new HashMap<>(eurderDatabase.getEurderDatabase());

        given()
                .baseUri("http://localhost")
                .port(port)
                .auth().preemptive().basic(customer.getEmail(), password)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        {}
                        """)
                .when()
                .post("/eurders")
                .then()
                .assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value());

        assertThat(eurderDatabase.getEurderDatabase())
                .containsExactlyInAnyOrderEntriesOf(beforeRequest);
    }

    @Test
    void addEurder_withANullItemGroupList_returnsBadRequestWithoutSaving() {
        Map<String, Eurder> beforeRequest = new HashMap<>(eurderDatabase.getEurderDatabase());

        given()
                .baseUri("http://localhost")
                .port(port)
                .auth().preemptive().basic(customer.getEmail(), password)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        { "itemGroupDtos": null }
                        """)
                .when()
                .post("/eurders")
                .then()
                .assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value());

        assertThat(eurderDatabase.getEurderDatabase())
                .containsExactlyInAnyOrderEntriesOf(beforeRequest);
    }

    @Test
    void addEurder_withZeroAmount_returnsBadRequestWithoutSaving() {
        Map<String, Eurder> beforeRequest = new HashMap<>(eurderDatabase.getEurderDatabase());

        given()
                .baseUri("http://localhost")
                .port(port)
                .auth().preemptive().basic(customer.getEmail(), password)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        { "itemGroupDtos": [{ "itemId": "%s", "amount": 0 }] }
                        """.formatted(coffee.getId()))
                .when()
                .post("/eurders")
                .then()
                .assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value());

        assertThat(eurderDatabase.getEurderDatabase())
                .containsExactlyInAnyOrderEntriesOf(beforeRequest);
    }

    @Test
    void addEurder_withNegativeAmount_returnsBadRequestWithoutSaving() {
        Map<String, Eurder> beforeRequest = new HashMap<>(eurderDatabase.getEurderDatabase());

        given()
                .baseUri("http://localhost")
                .port(port)
                .auth().preemptive().basic(customer.getEmail(), password)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        { "itemGroupDtos": [{ "itemId": "%s", "amount": -1 }] }
                        """.formatted(coffee.getId()))
                .when()
                .post("/eurders")
                .then()
                .assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value());

        assertThat(eurderDatabase.getEurderDatabase())
                .containsExactlyInAnyOrderEntriesOf(beforeRequest);
    }

    @Test
    void addEurder_withSomeButInsufficientStock_shipsInSevenDays() {
        LocalDate beforeRequest = LocalDate.now();

        JsonPath response = given()
                .baseUri("http://localhost")
                .port(port)
                .auth().preemptive().basic(customer.getEmail(), password)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        { "itemGroupDtos": [{ "itemId": "%s", "amount": 4 }] }
                        """.formatted(coffee.getId()))
                .when()
                .post("/eurders")
                .then()
                .assertThat()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .jsonPath();

        LocalDate afterRequest = LocalDate.now();
        assertThat(response.getList("itemGroups")).hasSize(1);
        assertThat(response.getInt("itemGroups[0].amount")).isEqualTo(4);
        assertThat(LocalDate.parse(response.getString("itemGroups[0].shippingDate")))
                .isBetween(beforeRequest.plusDays(7), afterRequest.plusDays(7));
        assertThat(new BigDecimal(response.getString("totalPrice"))).isEqualByComparingTo("359.80");
    }

    @Test
    void addEurder_changingTheCatalogPrice_doesNotChangeTheSavedPrice() {
        String eurderId = given()
                .baseUri("http://localhost")
                .port(port)
                .auth().preemptive().basic(customer.getEmail(), password)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        { "itemGroupDtos": [{ "itemId": "%s", "amount": 2 }] }
                        """.formatted(coffee.getId()))
                .when()
                .post("/eurders")
                .then()
                .assertThat()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .path("id");

        // Er is nog geen update-endpoint: simuleer een prijswijziging in de catalogus.
        Item updatedCoffee = new Item("Coffee", "Good coffee", new BigDecimal("100.00"), 3, coffee.getId());
        itemDatabase.getItemDatabase().put(coffee.getId(), updatedCoffee);

        Eurder savedEurder = eurderDatabase.getEurderDatabase().get(eurderId);
        assertThat(savedEurder).isNotNull();
        assertThat(savedEurder.getItemGroups()).hasSize(1);
        assertThat(savedEurder.getItemGroups().getFirst().getUnitPrice()).isEqualByComparingTo("89.95");
        assertThat(savedEurder.calculateTotalPrice()).isEqualByComparingTo("179.90");
    }
}
