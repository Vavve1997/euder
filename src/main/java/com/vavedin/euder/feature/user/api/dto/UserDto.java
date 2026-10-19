package com.vavedin.euder.feature.user.api.dto;

public class UserDto {

    private String firstName, lastName, email, phoneNumber, id;
    private AddressDto addressDto;

    public UserDto() {
    }

    public UserDto(String firstName, String lastName, String email, AddressDto addressDto, String phoneNumber, String id) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.addressDto = addressDto;
        this.phoneNumber = phoneNumber;
        this.id = id;
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

    public String getId() {
        return id;
    }

    public AddressDto getAddressDto() {
        return addressDto;


    }
}
