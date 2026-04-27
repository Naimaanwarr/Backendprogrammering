package com.circle.backendprogrammering.customer.dto;

public class AddressResponse {
    private Long id;
    private String street;
    private String city;
    private String zip;
    private String country;

    public AddressResponse(Long id, String street, String city, String zip, String country) {
        this.id = id;
        this.street = street;
        this.city = city;
        this.zip = zip;
        this.country = country;
    }

    public Long getId() { return id; }
    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getZip() { return zip; }
    public String getCountry() { return country; }
}
