package com.vavedin.euder.feature.user.domain;

import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
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

    public Collection<User> getAllUsers() {
        return userDatabase.getUsers();
    }

    public Collection<User> getAllCustomers() {
        return userDatabase.getUsers().stream().filter(User::isCustomer).toList();
    }

    public Optional<User> findById(String id) {
        return Optional.ofNullable(userDatabase.getUserDatabase().get(id));
    }
}
