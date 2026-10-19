package com.vavedin.euder.feature.user.domain;

import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private final UserDatabase userDatabase;

    public UserRepository(UserDatabase userDatabase) {
        this.userDatabase = userDatabase;
    }

    public User addUser(User user) {
    }
}
