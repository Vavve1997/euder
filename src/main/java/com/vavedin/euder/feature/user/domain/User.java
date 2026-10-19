package com.vavedin.euder.feature.user.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class User {

    private final String id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final Address address;
    private final String phoneNumber;

    public User(String firstName, String lastName, String email, Address address, String phoneNumber) {
        this(firstName,
                lastName,
                email,
                address,
                phoneNumber,
                UUID.randomUUID().toString());
    }

    public User(String firstName, String lastName, String email, Address address, String phoneNumber, String id) {
        this.firstName = requireNonBlank(firstName,"firstName");
        this.lastName = requireNonBlank(lastName,"lastName");
        this.email = requireNonBlank(email, "email");
        if (address == null) {
            throw new IllegalArgumentException("address must not be null");
        }
        this.address = address;
        this.phoneNumber = requireNonBlank(phoneNumber, "phoneNumber");
        this.id = requireNonBlank(id, "id");
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be null or blank"
            );
        }
        return value;
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
