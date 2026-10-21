package com.vavedin.euder.feature.eurder.api;

import com.vavedin.euder.feature.eurder.api.dto.EurderDto;
import com.vavedin.euder.feature.eurder.api.dto.EurderReportDto;
import com.vavedin.euder.feature.eurder.api.dto.ItemGroupDto;
import com.vavedin.euder.feature.eurder.domain.Eurder;
import com.vavedin.euder.feature.eurder.domain.EurderDatabase;
import com.vavedin.euder.feature.eurder.domain.ItemGroup;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EurderReportEndToEndTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserDatabase userDatabase;

    @Autowired
    private EurderDatabase eurderDatabase;

    @Autowired
    private ItemDatabase itemDatabase;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User customer;
    private User otherCustomer;
    private User admin;

    private Item coffee;
    private Item tea;

    private final String password = "Secret123!";
    private final List<String> createdEurderIds = new ArrayList<>();

    @BeforeEach
    void setup() {
        customer = createUser(Role.MEMBER);
        otherCustomer = createUser(Role.MEMBER);
        admin = createUser(Role.ADMIN);

        coffee = new Item(
                "Coffee",
                "Good coffee",
                new BigDecimal("10.00"),
                20
        );

        tea = new Item(
                "Tea",
                "Good tea",
                new BigDecimal("3.50"),
                20
        );

        itemDatabase.getItemDatabase().put(coffee.getId(), coffee);
        itemDatabase.getItemDatabase().put(tea.getId(), tea);
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

    private Eurder storeEurder(User owner, ItemGroup... itemGroups) {
        Eurder eurder = new Eurder(
                owner.getId(),
                List.of(itemGroups),
                owner.getAddress()
        );

        eurderDatabase.getEurderDatabase().put(eurder.getId(), eurder);
        createdEurderIds.add(eurder.getId());

        return eurder;
    }

    private ItemGroup createItemGroup(Item item, int amount) {
        return new ItemGroup(
                item.getId(),
                item.getName(),
                item.getPrice(),
                amount,
                LocalDate.of(2026, 10, 21)
        );
    }

    @AfterEach
    void cleanup() {
        for (String id : createdEurderIds) {
            eurderDatabase.getEurderDatabase().remove(id);
        }

        userDatabase.getUserDatabase().remove(customer.getId());
        userDatabase.getUserDatabase().remove(otherCustomer.getId());
        userDatabase.getUserDatabase().remove(admin.getId());

        itemDatabase.getItemDatabase().remove(coffee.getId());
        itemDatabase.getItemDatabase().remove(tea.getId());
    }

    @Test
    void getReport_returnsOwnEurdersWithCorrectDetailsAndTotals() {
        //total: 2 × 10.00 + 3 × 3.50 = 30.50
        Eurder firstEurder = storeEurder(
                customer,
                createItemGroup(coffee, 2),
                createItemGroup(tea, 3)
        );

        //total:  1 × 10.00 = 10.00
        Eurder secondEurder = storeEurder(
                customer,
                createItemGroup(coffee, 1)
        );

        // This should not enter report
        storeEurder(
                otherCustomer,
                createItemGroup(coffee, 10)
        );

        EurderReportDto report = given()
                .baseUri("http://localhost")
                .port(port)
                .auth().preemptive().basic(customer.getEmail(), password)
                .when()
                .get("/eurders/report")
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .extract()
                .as(EurderReportDto.class);

        assertThat(report.getEurders())
                .extracting(EurderDto::getId)
                .containsExactlyInAnyOrder(
                        firstEurder.getId(),
                        secondEurder.getId()
                );

        assertThat(report.getTotalPrice())
                .isEqualByComparingTo("40.50");

        EurderDto firstResult = report.getEurders().stream()
                .filter(eurder -> eurder.getId().equals(firstEurder.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(firstResult.getCustomerId())
                .isEqualTo(customer.getId());

        assertThat(firstResult.getTotalPrice())
                .isEqualByComparingTo("30.50");

        assertThat(firstResult.getItemGroups())
                .extracting(ItemGroupDto::getItemId)
                .containsExactlyInAnyOrder(coffee.getId(), tea.getId());

        ItemGroupDto coffeeGroup = firstResult.getItemGroups().stream()
                .filter(group -> group.getItemId().equals(coffee.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(coffeeGroup.getItemName()).isEqualTo("Coffee");
        assertThat(coffeeGroup.getAmount()).isEqualTo(2);
        assertThat(coffeeGroup.getTotalPrice())
                .isEqualByComparingTo("20.00");

        ItemGroupDto teaGroup = firstResult.getItemGroups().stream()
                .filter(group -> group.getItemId().equals(tea.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(teaGroup.getItemName()).isEqualTo("Tea");
        assertThat(teaGroup.getAmount()).isEqualTo(3);
        assertThat(teaGroup.getTotalPrice())
                .isEqualByComparingTo("10.50");

        EurderDto secondResult = report.getEurders().stream()
                .filter(eurder -> eurder.getId().equals(secondEurder.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(secondResult.getCustomerId())
                .isEqualTo(customer.getId());

        assertThat(secondResult.getTotalPrice())
                .isEqualByComparingTo("10.00");

        assertThat(secondResult.getItemGroups()).hasSize(1);
        assertThat(secondResult.getItemGroups().getFirst().getItemName())
                .isEqualTo("Coffee");
        assertThat(secondResult.getItemGroups().getFirst().getAmount())
                .isEqualTo(1);
        assertThat(secondResult.getItemGroups().getFirst().getTotalPrice())
                .isEqualByComparingTo("10.00");
    }

    @Test
    void getReport_withoutOwnEurders_returnsEmptyListAndZeroTotal() {
        storeEurder(otherCustomer, createItemGroup(coffee, 2));

        EurderReportDto report = given()
                .baseUri("http://localhost")
                .port(port)
                .auth().preemptive().basic(customer.getEmail(), password)
                .when()
                .get("/eurders/report")
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(EurderReportDto.class);

        assertThat(report.getEurders()).isEmpty();
        assertThat(report.getTotalPrice())
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getReport_afterCatalogPriceChanges_keepsThePurchasePrice() {
        storeEurder(customer, createItemGroup(coffee, 2));

        Item updatedCoffee = new Item(
                "Coffee",
                "Good coffee",
                new BigDecimal("99.00"),
                20,
                coffee.getId()
        );

        itemDatabase.getItemDatabase().put(coffee.getId(), updatedCoffee);

        EurderReportDto report = given()
                .baseUri("http://localhost")
                .port(port)
                .auth().preemptive().basic(customer.getEmail(), password)
                .when()
                .get("/eurders/report")
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(EurderReportDto.class);

        assertThat(report.getEurders()).hasSize(1);
        assertThat(report.getTotalPrice())
                .isEqualByComparingTo("20.00");

        EurderDto eurder = report.getEurders().getFirst();

        assertThat(eurder.getTotalPrice())
                .isEqualByComparingTo("20.00");
        assertThat(eurder.getItemGroups()).hasSize(1);

        ItemGroupDto group = eurder.getItemGroups().getFirst();

        assertThat(group.getUnitPrice())
                .isEqualByComparingTo("10.00");
        assertThat(group.getTotalPrice())
                .isEqualByComparingTo("20.00");
    }

    @Test
    void getReport_withoutAuthentication_returnsUnauthorized() {
        given()
                .baseUri("http://localhost")
                .port(port)
                .when()
                .get("/eurders/report")
                .then()
                .assertThat()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .header("WWW-Authenticate", startsWith("Basic "));
    }

    @Test
    void getReport_withWrongPassword_returnsUnauthorized() {
        given()
                .baseUri("http://localhost")
                .port(port)
                .auth().preemptive().basic(
                        customer.getEmail(), "wrong-password"
                )
                .when()
                .get("/eurders/report")
                .then()
                .assertThat()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .header("WWW-Authenticate", startsWith("Basic "));
    }

    @Test
    void getReport_asAdminWithoutPermission_returnsForbidden() {
        given()
                .baseUri("http://localhost")
                .port(port)
                .auth().preemptive().basic(admin.getEmail(), password)
                .when()
                .get("/eurders/report")
                .then()
                .assertThat()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }
}