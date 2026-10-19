package com.vavedin.euder.feature.user.domain;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserDatabase {

    private final ConcurrentHashMap<String, User> userDatabase;

    public UserDatabase() {
        userDatabase = new ConcurrentHashMap<String, User>();
    }

    public ConcurrentHashMap<String, User> getUserDatabase() {
        return userDatabase;
    }

    public Collection<User> getUsers() {
        return userDatabase.values();
    }
}
