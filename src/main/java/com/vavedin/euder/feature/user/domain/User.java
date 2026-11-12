package com.vavedin.euder.feature.user.domain;

import com.vavedin.euder.feature.security.PasswordEncoder;
import com.vavedin.euder.feature.security.enums.Feature;
import com.vavedin.euder.feature.security.enums.Role;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(nullable = false, length = 255)
    private String id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Embedded
    private Address address;

    @Column(name = "phone_number", nullable = false, length = 30)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String hashedPassword;


    public User(String firstName, String lastName, String email, Address address, String phoneNumber, String hashedPassword) {
        this(firstName,
                lastName,
                email,
                address,
                phoneNumber,
                UUID.randomUUID().toString(),
                Role.MEMBER,
                hashedPassword);
    }

    public User(String firstName, String lastName, String email, Address address, String phoneNumber, String id, String hashedPassword) {
        this(firstName,
                lastName,
                email,
                address,
                phoneNumber,
                id,
                Role.MEMBER,
                hashedPassword);
    }

    public User(String firstName, String lastName, String email, Address address, String phoneNumber, String id, Role role, String hashedPassword) {
        this.firstName = requireNonBlank(firstName, "firstName");
        this.lastName = requireNonBlank(lastName, "lastName");
        this.email = requireNonBlank(email, "email");
        if (address == null) {
            throw new IllegalArgumentException("address must not be null");
        }
        this.address = address;
        this.phoneNumber = requireNonBlank(phoneNumber, "phoneNumber");
        this.id = requireNonBlank(id, "id");
        if (role == null) {
            throw new IllegalArgumentException("role must not be null");
        }
        this.role = role;
        this.hashedPassword = requireNonBlank(hashedPassword, "hashedPassword");
    }

    public User() {

    }

    public boolean canHaveAccessTo(Feature feature) {
        return role.containsFeature(feature);
    }

    public boolean doesPasswordMatch(String rawPassword, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
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

    public boolean isCustomer() {
        return role == Role.MEMBER;
    }
}
