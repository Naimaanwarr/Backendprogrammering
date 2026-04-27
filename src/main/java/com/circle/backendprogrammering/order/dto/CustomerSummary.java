package com.circle.backendprogrammering.order.dto;

public class CustomerSummary {
    private Long id;
    private String name;
    private String phoneNumber;
    private String email;

    public CustomerSummary(Long id, String name, String phoneNumber, String email) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
}
