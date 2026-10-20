package com.vavedin.euder.feature.user.domain;

import com.vavedin.euder.feature.security.PasswordEncoder;
import com.vavedin.euder.feature.security.enums.Role;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserDatabase {

    private final ConcurrentHashMap<String, User> userDatabase;
    private final PasswordEncoder passwordEncoder;

    public UserDatabase(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        userDatabase = new ConcurrentHashMap<String, User>();
        User admin = new User("Admin", "Admin", "admin",
                new Address("Admin", 1, 1000, "Admin"),
                "123", UUID.randomUUID().toString(), Role.ADMIN,
                passwordEncoder.encode("root"));
        userDatabase.put(admin.getId(), admin);
    }

    public ConcurrentHashMap<String, User> getUserDatabase() {
        return userDatabase;
    }

    public Collection<User> getUsers() {
        return userDatabase.values();
    }
}
