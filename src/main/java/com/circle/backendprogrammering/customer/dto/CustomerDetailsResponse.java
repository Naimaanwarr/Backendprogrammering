package com.circle.backendprogrammering.customer.dto;

import java.util.List;

public class CustomerDetailsResponse {
    private Long id;
    private String name;
    private String phoneNumber;
    private String email;

    private List<AddressResponse> addresses;
    private List<OrderSummaryResponse> orderHistory;

    public CustomerDetailsResponse(Long id,
                                   String name,
                                   String phoneNumber,
                                   String email,
                                   List<AddressResponse> addresses,
                                   List<OrderSummaryResponse> orderHistory) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.addresses = addresses;
        this.orderHistory = orderHistory;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public List<AddressResponse> getAddresses() { return addresses; }
    public List<OrderSummaryResponse> getOrderHistory() { return orderHistory; }
}
