package com.circle.backendprogrammering.customer.dto;

import jakarta.validation.constraints.NotBlank;

public class AddressCreateRequest {

    @NotBlank
    private String street;

    @NotBlank
    private String city;

    @NotBlank
    private String zip;

    @NotBlank
    private String country;

    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getZip() { return zip; }
    public String getCountry() { return country; }
}
