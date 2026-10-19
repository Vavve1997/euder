package com.vavedin.euder.feature.user.api.dto;

public class AddressDto {
    private String street, city;
    private int houseNumber, postalCode;

    public AddressDto() {
    }

    public AddressDto(String street, int houseNumber, int postalCode, String city) {
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

    public int getHouseNumber() {
        return houseNumber;
    }

    public int getPostalCode() {
        return postalCode;
    }
}
