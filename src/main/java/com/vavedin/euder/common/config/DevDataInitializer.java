package com.vavedin.euder.common.config;

import com.vavedin.euder.feature.eurder.domain.Eurder;
import com.vavedin.euder.feature.eurder.domain.EurderRepository;
import com.vavedin.euder.feature.eurder.domain.ItemGroup;
import com.vavedin.euder.feature.item.domain.Item;
import com.vavedin.euder.feature.item.domain.ItemRepository;
import com.vavedin.euder.feature.security.PasswordEncoder;
import com.vavedin.euder.feature.security.enums.Role;
import com.vavedin.euder.feature.user.domain.Address;
import com.vavedin.euder.feature.user.domain.User;
import com.vavedin.euder.feature.user.domain.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@Profile("dev")
public class DevDataInitializer implements CommandLineRunner {

    private static final Logger logger =
            LoggerFactory.getLogger(DevDataInitializer.class);

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final EurderRepository eurderRepository;
    private final PasswordEncoder passwordEncoder;

    public DevDataInitializer(
            UserRepository userRepository,
            ItemRepository itemRepository,
            EurderRepository eurderRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.eurderRepository = eurderRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        LocalDate today = LocalDate.now();

        // USERS
        createUser(
                "tyrion",
                "Tyrion",
                "Lannister",
                "tyrion@westeros.test",
                "ADragonDrinks!",
                Role.ADMIN,
                "Tower of the Hand",
                "Kings Landing"
        );

        createUser(
                "varys",
                "Varys",
                "The Spider",
                "varys@westeros.test",
                "LittleBirds123!",
                Role.ADMIN,
                "Whisperers Lane",
                "Kings Landing"
        );

        User stannis = createUser(
                "stannis",
                "Stannis",
                "Baratheon",
                "stannis@westeros.test",
                "TheThroneIsMine!",
                Role.MEMBER,
                "Dragonstone Keep",
                "Dragonstone"
        );

        User jon = createUser(
                "jon",
                "Jon",
                "Snow",
                "jon@westeros.test",
                "YouKnowNothing!",
                Role.MEMBER,
                "Castle Black",
                "The Wall"
        );

        User daenerys = createUser(
                "daenerys",
                "Daenerys",
                "Targaryen",
                "daenerys@westeros.test",
                "Dracarys123!",
                Role.MEMBER,
                "Great Pyramid",
                "Meereen"
        );

        createUser(
                "arya",
                "Arya",
                "Stark",
                "arya@westeros.test",
                "NotToday123!",
                Role.MEMBER,
                "House of Black and White",
                "Braavos"
        );

        // ITEMS
        Item sword = createItem(
                "longsword",
                "Castle-forged longsword",
                "A reliable blade for defending your claim to the throne.",
                "150.00",
                12
        );

        Item shield = createItem(
                "shield",
                "Baratheon shield",
                "A sturdy shield bearing the crowned stag.",
                "80.00",
                5
        );

        Item wildfire = createItem(
                "wildfire",
                "Wildfire jar",
                "Handle with care. Keep far away from the high Septon.",
                "25.00",
                0
        );

        Item bread = createItem(
                "bread",
                "Hot Pie's bread",
                "Freshly baked bread.",
                "2.50",
                50
        );

        Item wine = createItem(
                "wine",
                "Arbor Gold",
                "Tyrion-approved wine from the Arbor.",
                "18.00",
                24
        );

        Item raven = createItem(
                "raven",
                "Messenger raven",
                "For urgent messages and questionable political alliances.",
                "45.00",
                3
        );

        Item cloak = createItem(
                "cloak",
                "Night's Watch cloak",
                "A warm black cloak. Because winter is coming.",
                "60.00",
                8
        );

        Item dragonglass = createItem(
                "dragonglass",
                "Dragonglass dagger",
                "An essential accessory north of the Wall.",
                "35.00",
                2
        );

        // EURDERS
        // Dit zijn vooraf opgebouwde snapshots.
        // De verzenddatums worden hier expliciet gekozen als testdata.

        createEurder(
                "STANNIS-1",
                stannis,
                group(sword, 2, today.minusDays(3)),
                group(shield, 1, today.minusDays(3))
        );

        createEurder(
                "STANNIS-2",
                stannis,
                group(wildfire, 10, today.plusDays(7)),
                group(bread, 4, today)
        );

        createEurder(
                "STANNIS-3",
                stannis,
                group(wine, 3, today),
                group(raven, 1, today)
        );

        createEurder(
                "JON-1",
                jon,
                group(cloak, 1, today.plusDays(1)),
                group(dragonglass, 4, today.plusDays(7))
        );

        createEurder(
                "DAENERYS-1",
                daenerys,
                group(wildfire, 3, today.plusDays(7)),
                group(raven, 2, today)
        );

        logger.info(
                "Westeros dev data loaded: 6 users, 8 items and 5 eurders"
        );
    }

    private User createUser(
            String id,
            String firstName,
            String lastName,
            String email,
            String password,
            Role role,
            String street,
            String city
    ) {
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

        return userRepository.addUser(user);
    }

    private Item createItem(
            String id,
            String name,
            String description,
            String price,
            int amount
    ) {
        Item item = new Item(
                name,
                description,
                new BigDecimal(price),
                amount,
                id
        );

        return itemRepository.addItem(item);
    }

    private ItemGroup group(
            Item item,
            int amount,
            LocalDate shippingDate
    ) {
        return new ItemGroup(
                item.getId(),
                item.getName(),
                item.getPrice(),
                amount,
                shippingDate
        );
    }

    private void createEurder(
            String label,
            User customer,
            ItemGroup... itemGroups
    ) {
        Eurder eurder = new Eurder(
                customer.getId(),
                List.of(itemGroups),
                customer.getAddress()
        );

        eurderRepository.addEurder(eurder);

        // Eurder genereert zelf een UUID.
        // Via deze log vind je welk id bij welke testbestelling hoort.
        logger.info(
                "Dev eurder {} | id={} | customer={} | total={}",
                label,
                eurder.getId(),
                customer.getEmail(),
                eurder.calculateTotalPrice()
        );
    }
}