package com.vavedin.euder.feature.item.api;

import com.vavedin.euder.feature.item.domain.ItemDatabase;
import com.vavedin.euder.feature.security.PasswordEncoder;
import com.vavedin.euder.feature.security.enums.Role;
import com.vavedin.euder.feature.user.domain.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.*;
import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ItemEndToEndTest {
    @LocalServerPort
    private int port;
    @Autowired
    private UserDatabase users;
    @Autowired
    private ItemDatabase items;
    @Autowired
    private PasswordEncoder encoder;
    private String adminEmail, memberEmail;
    private final List<String> userIds = new ArrayList<>();
    private final List<String> itemIds = new ArrayList<>();

    @BeforeEach
    void setup() {
        adminEmail = UUID.randomUUID() + "@example.com";
        memberEmail = UUID.randomUUID() + "@example.com";
        String hash = encoder.encode("Secret123!");
        addUser(adminEmail, Role.ADMIN, hash);
        addUser(memberEmail, Role.MEMBER, hash);
    }

    private void addUser(String email, Role role, String hash) {
        String id = UUID.randomUUID().toString();
        users.getUserDatabase().put(id, new User("Test", "User", email,
                new Address("Street", 1, 1000, "City"), "012345", id, role, hash));
        userIds.add(id);
    }

    @AfterEach
    void cleanup() {
        userIds.forEach(users.getUserDatabase()::remove);
        itemIds.forEach(items.getItemDatabase()::remove);
    }

    private String basic(String email, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((email + ":" + password).getBytes(StandardCharsets.UTF_8));
    }

    private Response post(String authorization) {
        var request = given()
                .baseUri("http://localhost")
                .port(port)
                .contentType("application/json")
                .body(Map.of("name", "Coffee machine", "description", "Makes coffee", "price", 89.95, "amount", 12));
        if (authorization != null) request.header("Authorization", authorization);
        return request.when().post("/items");
    }

    @Test
    void adminCanCreateItem_andItIsStored() {
        Response response = post(basic(adminEmail, "Secret123!"));
        String id = response.jsonPath().getString("id");
        if (id != null) itemIds.add(id);
        response.then().statusCode(201).contentType("application/json")
                .body("name", equalTo("Coffee machine"))
                .body("description", equalTo("Makes coffee"))
                .body("amount", equalTo(12));
        assertThat(id).isNotBlank();
        assertThat(response.jsonPath().getDouble("price")).isEqualTo(89.95);
        var saved = items.getItemDatabase().get(id);
        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("Coffee machine");
        assertThat(saved.getDescription()).isEqualTo("Makes coffee");
        assertThat(saved.getPrice()).isEqualByComparingTo("89.95");
        assertThat(saved.getAmount()).isEqualTo(12);
    }

    @Test
    void missingCredentials_returns401_andDoesNotStoreAnything() {
        assertRejected(null, 401);
    }

    @Test
    void wrongPassword_returns401_andDoesNotStoreAnything() {
        assertRejected(basic(adminEmail, "wrong"), 401);
    }

    @Test
    void unknownUser_returns401_andDoesNotStoreAnything() {
        assertRejected(basic("unknown@example.com", "Secret123!"), 401);
    }

    @Test
    void member_returns403_andDoesNotStoreAnything() {
        assertRejected(basic(memberEmail, "Secret123!"), 403);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Bearer token", "Basic !!!", "Basic bm9jb2xvbg=="})
    void malformedCredentials_returns401(String header) {
        assertRejected(header, 401);
    }

    private void assertRejected(String header, int status) {
        var before = new HashMap<>(items.getItemDatabase());
        Response response = post(header);
        response.then().statusCode(status);
        response.then()
                .log().ifValidationFails()
                .statusCode(status);
        if (status == 401) response.then().header("WWW-Authenticate", startsWith("Basic "));
        assertThat(items.getItemDatabase()).containsExactlyInAnyOrderEntriesOf(before);
    }
}
