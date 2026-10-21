package com.vavedin.euder.feature.eurder.api;


import com.vavedin.euder.feature.eurder.domain.Eurder;
import com.vavedin.euder.feature.eurder.domain.EurderDatabase;
import com.vavedin.euder.feature.eurder.domain.ItemGroup;
import com.vavedin.euder.feature.item.domain.Item;
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
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class EurderShipmentMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDatabase userDatabase;

    @Autowired
    private EurderDatabase eurderDatabase;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User tyrion;
    private User stannis;

    private final String password = "TheThroneIsMine!";

    private String authorizationFor(User user) {
        String credentials = user.getEmail() + ":" + password;

        return "Basic " + Base64.getEncoder().encodeToString(
                credentials.getBytes(StandardCharsets.UTF_8)
        );
    }

    private User createUser(String id, String firstName, String lastName, String email, Role role, String street, String city) {
        User user = new User(
                firstName,
                lastName,
                email,
                new Address(street, 1, 1000, city),
                "0123456789",
                id,
                role,
                passwordEncoder.encode(password)
        );

        userDatabase.getUserDatabase().put(user.getId(), user);
        return user;
    }

    private ItemGroup group(Item item, int amount, LocalDate shippingDate) {
        return new ItemGroup(
                item.getId(),
                item.getName(),
                item.getPrice(),
                amount,
                shippingDate
        );
    }

    @BeforeEach
    void setup() {
        tyrion = createUser(
                "tyrion",
                "Tyrion",
                "Lannister",
                "tyrion@westeros.test",
                Role.ADMIN,
                "Tower of the Hand",
                "Kings Landing"
        );
        stannis = createUser(
                "stannis",
                "Stannis",
                "Baratheon",
                "stannis@westeros.test",
                Role.MEMBER,
                "Dragonstone Keep",
                "Dragonstone"
        );
    }

    @AfterEach
    void cleanup() {
        userDatabase.getUserDatabase().remove(tyrion.getId());
        userDatabase.getUserDatabase().remove(stannis.getId());
        eurderDatabase.getEurderDatabase().clear();

    }

    @Test
    void getShipments_asAdmin_returnsOnlyTodaysGroupsAndShippingAddress() throws Exception {
        //Given
        LocalDate today = LocalDate.now();

        Item bread = new Item("Hot Pie's bread",
                "Freshly baked bread.",
                new BigDecimal("2.50"),
                50);
        Item wildfire = new Item("Wildfire jar",
                "Handle with care. Keep far away from the high Septon.",
                new BigDecimal("25.00"),
                5);
        Item wine = new Item("Arbor Gold",
                "Tyrion-approved wine from the Arbor.",
                new BigDecimal("18.00"),
                24);

        List<ItemGroup> itemsForStannisEurder = List.of(group(bread, 4, today),
                group(wildfire, 10, today.plusDays(1)),
                group(wine, 2, today.minusDays(1)));

        Address shippingAddress =
                new Address("Great Pyramid", 1, 8000, "Meereen");

        Eurder stannisEurder = new Eurder(stannis.getId(), itemsForStannisEurder, shippingAddress);

        eurderDatabase.getEurderDatabase().put(stannisEurder.getId(), stannisEurder);

        //when & then
        mockMvc.perform(
                get("/eurders/shipping_today")
                        .header(HttpHeaders.AUTHORIZATION, authorizationFor(tyrion)))
                        .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].eurderId").value(stannisEurder.getId()))
                .andExpect(jsonPath("$[0].itemGroups.length()").value(1))
                .andExpect(jsonPath("$[0].itemGroups[0].itemId").value(bread.getId()))
                .andExpect(jsonPath("$[0].itemGroups[0].amount").value(4))
                .andExpect(jsonPath("$[0].itemGroups[0].shippingDate").value(today.toString()))
                .andExpect(jsonPath("$[0].shippingAddress.street").value("Great Pyramid"))
                .andExpect(jsonPath("$[0].shippingAddress.houseNumber").value(1))
                .andExpect(jsonPath("$[0].shippingAddress.postalCode").value(8000))
                .andExpect(jsonPath("$[0].shippingAddress.city").value("Meereen"))
                .andExpect(jsonPath("$.length()").value(1));

    }

    @Test
    void getShipments_asAdmin_returnsAllEurdersWithGroupsShippingToday() throws Exception {
        //Given
        LocalDate today = LocalDate.now();
        User varys = createUser(
                "varys",
                "Varys",
                "The Spider",
                "varys@westeros.test",
                Role.MEMBER,
                "Whisperers Lane",
                "Kings Landing"
        );

        Item bread = new Item("Hot Pie's bread",
                "Freshly baked bread.",
                new BigDecimal("2.50"),
                50);
        Item wildfire = new Item("Wildfire jar",
                "Handle with care. Keep far away from the high Septon.",
                new BigDecimal("25.00"),
                5);
        Item wine = new Item("Arbor Gold",
                "Tyrion-approved wine from the Arbor.",
                new BigDecimal("18.00"),
                24);
        Item cloak = new Item("Night's Watch cloak",
                "A warm black cloak. Because winter is coming.",
                new BigDecimal("60.00"),
                8);

        List<ItemGroup> itemsForStannisEurder1 = List.of(group(bread, 4, today));
        List<ItemGroup> itemsForStannisEurder2 = List.of(group(wildfire, 7, today.minusDays(1)));
        List<ItemGroup> itemsForVarysEurder1 = List.of(group(cloak, 1, today),
                group(wildfire, 1, today.plusDays(1)));
        List<ItemGroup> itemsForVarysEurder2 = List.of(group(wine, 4, today.plusDays(3)));

        Eurder stannisEurder1 = new Eurder(stannis.getId(), itemsForStannisEurder1, stannis.getAddress());
        Eurder stannisEurder2 = new Eurder(stannis.getId(), itemsForStannisEurder2, stannis.getAddress());
        Eurder varysEurder1 = new Eurder(varys.getId(), itemsForVarysEurder1, varys.getAddress());
        Eurder varysEurder2 = new Eurder(varys.getId(), itemsForVarysEurder2, varys.getAddress());

        eurderDatabase.getEurderDatabase().put(stannisEurder1.getId(), stannisEurder1);
        eurderDatabase.getEurderDatabase().put(stannisEurder2.getId(), stannisEurder2);
        eurderDatabase.getEurderDatabase().put(varysEurder1.getId(), varysEurder1);
        eurderDatabase.getEurderDatabase().put(varysEurder2.getId(), varysEurder2);

        //When & Then
        mockMvc.perform(
                        get("/eurders/shipping_today")
                                .header(HttpHeaders.AUTHORIZATION, authorizationFor(tyrion)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(
                        jsonPath("$[*].eurderId")
                                .value(containsInAnyOrder(
                                        stannisEurder1.getId(),
                                        varysEurder1.getId()
                                )))
                .andExpect(
                        jsonPath("$[*].itemGroups[*].itemId")
                                .value(containsInAnyOrder(
                                        bread.getId(),
                                        cloak.getId()
                                )));



    }

}
