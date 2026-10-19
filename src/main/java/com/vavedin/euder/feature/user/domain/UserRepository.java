package com.vavedin.euder.feature.user.domain;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository {

    private final UserDatabase userDatabase;

    public UserRepository(UserDatabase userDatabase) {
        this.userDatabase = userDatabase;
    }

    public User addUser(User user) {
        userDatabase.getUserDatabase().put(user.getId(), user);
        return user;
    }

    public Optional<User> findByEmail(String email) {
        return userDatabase.getUsers().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }
}
