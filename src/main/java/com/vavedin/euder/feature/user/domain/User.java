package com.vavedin.euder.feature.user.domain;

import java.util.UUID;

public class User {

    private final String id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final Address address;
    private final String phoneNumber;

    public User(String firstName, String lastName, String email, Address address, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public Address getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
