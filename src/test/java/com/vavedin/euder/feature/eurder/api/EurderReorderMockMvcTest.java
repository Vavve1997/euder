package com.vavedin.euder.feature.eurder.api;

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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EurderReorderMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDatabase userDatabase;

    @Autowired
    private ItemDatabase itemDatabase;

    @Autowired
    private EurderDatabase eurderDatabase;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User stannis;
    private User jon;
    private Item sword;
    private Eurder originalEurder;

    private final String password = "TheThroneIsMine!";

    @BeforeEach
    void setup() {
        stannis = createCustomer("Stannis");
        jon = createCustomer("Jon");

        // the price now is 150.
        sword = new Item(
                "Longsword",
                "Castle-forged steel",
                new BigDecimal("150.00"),
                10
        );

        itemDatabase.getItemDatabase().put(sword.getId(), sword);

        // original price was only 100
        ItemGroup originalGroup = new ItemGroup(
                sword.getId(),
                sword.getName(),
                new BigDecimal("100.00"),
                2,
                LocalDate.of(2020, 1, 1)
        );

        originalEurder = new Eurder(
                stannis.getId(),
                List.of(originalGroup)
        );

        eurderDatabase.getEurderDatabase().put(
                originalEurder.getId(),
                originalEurder
        );
    }

    private User createCustomer(String firstName) {
        String id = UUID.randomUUID().toString();

        User user = new User(
                firstName,
                "Test",
                id + "@westeros.test",
                new Address("Kingsroad", 1, 1000, "Winterfell"),
                "0123456789",
                id,
                Role.MEMBER,
                passwordEncoder.encode(password)
        );

        userDatabase.getUserDatabase().put(id, user);
        return user;
    }

    private String authorizationFor(User user) {
        String credentials = user.getEmail() + ":" + password;

        return "Basic " + Base64.getEncoder().encodeToString(
                credentials.getBytes(StandardCharsets.UTF_8)
        );
    }

    @AfterEach
    void cleanup() {
        for (Eurder eurder : eurderDatabase.getEurderDatabase().values()) {
            if (eurder.getCustomerId().equals(stannis.getId())
                    || eurder.getCustomerId().equals(jon.getId())) {
                eurderDatabase.getEurderDatabase().remove(eurder.getId());
            }
        }

        itemDatabase.getItemDatabase().remove(sword.getId());
        userDatabase.getUserDatabase().remove(stannis.getId());
        userDatabase.getUserDatabase().remove(jon.getId());
    }

    @Test
    void reorder_ownEurder_createsANewEurderUsingCurrentPrice() throws Exception {
        LocalDate beforeRequest = LocalDate.now();
        int numberOfEurdersBefore = eurderDatabase.getEurderDatabase().size();

        mockMvc.perform(
                post("/eurders/reorder/{id}", originalEurder.getId())
                        .header(HttpHeaders.AUTHORIZATION, authorizationFor(stannis)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.customerId").value(stannis.getId()))
                .andExpect(jsonPath("$.itemGroups.length()").value(1))
                .andExpect(jsonPath("$.itemGroups[0].itemId").value(sword.getId()))
                .andExpect(jsonPath("$.itemGroups[0].amount").value(2))
                .andExpect(jsonPath("$.itemGroups[0].unitPrice").value(150.0))
                .andExpect(jsonPath("$.itemGroups[0].totalPrice").value(300.0))
                .andExpect(jsonPath("$.totalPrice").value(300.0));

        LocalDate afterRequest = LocalDate.now();

        assertThat(eurderDatabase.getEurderDatabase())
                .hasSize(numberOfEurdersBefore + 1);

        List<Eurder> newEurders = eurderDatabase.getEurderDatabase()
                .values()
                .stream()
                .filter(eurder -> eurder.getCustomerId().equals(stannis.getId()))
                .filter(eurder -> !eurder.getId().equals(originalEurder.getId()))
                .toList();

        assertThat(newEurders).hasSize(1);

        Eurder newEurder = newEurders.getFirst();

        assertThat(newEurder.getId()).isNotEqualTo(originalEurder.getId());
        assertThat(newEurder.calculateTotalPrice())
                .isEqualByComparingTo("300.00");
        assertThat(newEurder.getItemGroups()).hasSize(1);

        ItemGroup newGroup = newEurder.getItemGroups().getFirst();

        assertThat(newGroup.getItemId()).isEqualTo(sword.getId());
        assertThat(newGroup.getAmount()).isEqualTo(2);
        assertThat(newGroup.getShippingDate())
                .isBetween(beforeRequest.plusDays(1), afterRequest.plusDays(1));

        // Original Eurder can not haved changed
        Eurder savedOriginal = eurderDatabase.getEurderDatabase()
                .get(originalEurder.getId());

        assertThat(savedOriginal).isNotNull();
        assertThat(savedOriginal.calculateTotalPrice())
                .isEqualByComparingTo("200.00");
        assertThat(savedOriginal.getItemGroups().getFirst().getUnitPrice())
                .isEqualByComparingTo("100.00");
        assertThat(savedOriginal.getItemGroups().getFirst().getShippingDate())
                .isEqualTo(LocalDate.of(2020, 1, 1));
    }

    @Test
    void reorder_withInsufficientCurrentStock_shipsInSevenDays() throws Exception {
        Item itemWithLowStock = new Item(
                sword.getName(),
                sword.getDescription(),
                sword.getPrice(),
                1,
                sword.getId()
        );

        itemDatabase.getItemDatabase().put(sword.getId(), itemWithLowStock);

        LocalDate beforeRequest = LocalDate.now();

        mockMvc.perform(
                        post("/eurders/reorder/{id}", originalEurder.getId())
                                .header(HttpHeaders.AUTHORIZATION,
                                        authorizationFor(stannis)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itemGroups[0].amount").value(2));

        LocalDate afterRequest = LocalDate.now();

        List<Eurder> newEurders = eurderDatabase.getEurderDatabase()
                .values()
                .stream()
                .filter(eurder -> eurder.getCustomerId().equals(stannis.getId()))
                .filter(eurder -> !eurder.getId().equals(originalEurder.getId()))
                .toList();

        assertThat(newEurders).hasSize(1);

        ItemGroup group = newEurders.getFirst().getItemGroups().getFirst();

        assertThat(group.getShippingDate())
                .isBetween(beforeRequest.plusDays(7), afterRequest.plusDays(7));
    }

    @Test
    void reorder_anotherCustomersEurder_returnsForbiddenWithoutSaving() throws Exception {

        Map<String, Eurder> beforeRequest =
                new HashMap<>(eurderDatabase.getEurderDatabase());

        mockMvc.perform(post("/eurders/reorder/{id}", originalEurder.getId())
                                .header(HttpHeaders.AUTHORIZATION,
                                        authorizationFor(jon)))
                .andExpect(status().isForbidden());

        assertThat(eurderDatabase.getEurderDatabase())
                .containsExactlyInAnyOrderEntriesOf(beforeRequest);
    }

    @Test
    void reorder_unknownEurder_returnsNotFoundWithoutSaving() throws Exception {
        Map<String, Eurder> beforeRequest =
                new HashMap<>(eurderDatabase.getEurderDatabase());

        mockMvc.perform(
                        post("/eurders/reorder/{id}", UUID.randomUUID().toString())
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        authorizationFor(stannis)
                                )
                )
                .andExpect(status().isNotFound());

        assertThat(eurderDatabase.getEurderDatabase())
                .containsExactlyInAnyOrderEntriesOf(beforeRequest);
    }

    @Test
    void reorder_withoutAuthentication_returnsUnauthorizedWithoutSaving()
            throws Exception {

        Map<String, Eurder> beforeRequest =
                new HashMap<>(eurderDatabase.getEurderDatabase());

        mockMvc.perform(
                        post("/eurders/reorder/{id}", originalEurder.getId())
                )
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(
                        HttpHeaders.WWW_AUTHENTICATE,
                        startsWith("Basic ")
                ));

        assertThat(eurderDatabase.getEurderDatabase())
                .containsExactlyInAnyOrderEntriesOf(beforeRequest);
    }


}