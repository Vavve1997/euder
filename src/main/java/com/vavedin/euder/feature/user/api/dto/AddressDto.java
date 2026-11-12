package com.vavedin.euder.feature.user.api.dto;

public class AddressDto {
    private String street, city;
    private String houseNumber;
    private String postalCode;

    public AddressDto() {
    }

    public AddressDto(String street, String houseNumber, String postalCode, String city) {
        this.street = street;
        this.city = city;
        this.houseNumber = houseNumber;
        this.postalCode = postalCode;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public String getPostalCode() {
        return postalCode;
    }
}
