package com.vavedin.euder.feature.user.api.dto;

public class CreateUserDto {

    private String firstName, lastName, email, phoneNumber;
    private AddressDto address;
    private String password;

    public CreateUserDto() {
    }

    public CreateUserDto(String firstName, String lastName, String email, AddressDto address, String phoneNumber, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.password = password;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public AddressDto getAddress() {
        return address;
    }

    public String getPassword() {
        return password;
    }
}
