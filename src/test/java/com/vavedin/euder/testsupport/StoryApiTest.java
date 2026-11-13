package com.vavedin.euder.testsupport;

import com.vavedin.euder.feature.eurder.domain.*;
import com.vavedin.euder.feature.item.domain.*;
import com.vavedin.euder.feature.security.PasswordEncoder;
import com.vavedin.euder.feature.security.enums.Role;
import com.vavedin.euder.feature.user.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("story-tests")
public abstract class StoryApiTest {
    protected static final String PASSWORD = "TheThroneIsMine!";
    @LocalServerPort private int port;
    @Autowired protected UserRepository users;
    @Autowired protected ItemRepository items;
    @Autowired protected EurderRepository eurders;
    @Autowired protected PasswordEncoder passwords;
    @Autowired protected JdbcTemplate jdbc;
    protected Templates guest, admin, member;
    protected User tyrion, stannis;

    @BeforeEach
    void prepareDatabase() {
        // Real HTTP requests run in another thread: no test @Transactional.
        // Delete children first; Eurder deliberately has no REMOVE cascade.
        jdbc.update("DELETE FROM eurder.item_groups");
        jdbc.update("DELETE FROM eurder.eurders");
        jdbc.update("DELETE FROM eurder.items");
        jdbc.update("DELETE FROM eurder.users");
        tyrion = saveUser("tyrion", Role.ADMIN);
        stannis = saveUser("stannis", Role.MEMBER);
        guest = new Templates(port);
        admin = guest.withBasicAuth(tyrion.getEmail(), PASSWORD);
        member = guest.withBasicAuth(stannis.getEmail(), PASSWORD);
    }

    protected User saveUser(String name, Role role) {
        return users.saveAndFlush(new User(name, "Westeros", name + "@westeros.test",
                new Address("Dragonstone Keep", "1", "1000", "Dragonstone"),
                "0123456789", UUID.randomUUID().toString(), role, passwords.encode(PASSWORD)));
    }

    protected Item saveItem(String name, String price, int stock) {
        return items.save(new Item(name, "Made in Westeros", new BigDecimal(price), stock));
    }

    protected ItemGroup group(Item item, int amount, LocalDate date) {
        return new ItemGroup(item.getId(), item.getName(), item.getPrice(), amount, date);
    }

    protected Eurder saveEurder(User customer, ItemGroup... groups) {
        Address a = customer.getAddress();
        return eurders.saveAndFlush(new Eurder(customer.getId(), List.of(groups),
                new Address(a.street(), a.houseNumber(), a.postalCode(), a.city())));
    }
}
