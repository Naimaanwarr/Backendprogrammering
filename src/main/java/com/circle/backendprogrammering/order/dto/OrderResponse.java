package com.circle.backendprogrammering.order.dto;

import java.math.BigDecimal;
import java.util.List;

public class OrderResponse {
    private Long id;
    private CustomerSummary customer;
    private AddressSummary shippingAddress;
    private BigDecimal shippingCharge;
    private BigDecimal totalPrice;
    private boolean shipped;
    private List<OrderLineResponse> items;

    public OrderResponse(Long id,
                         CustomerSummary customer,
                         AddressSummary shippingAddress,
                         BigDecimal shippingCharge,
                         BigDecimal totalPrice,
                         boolean shipped,
                         List<OrderLineResponse> items) {
        this.id = id;
        this.customer = customer;
        this.shippingAddress = shippingAddress;
        this.shippingCharge = shippingCharge;
        this.totalPrice = totalPrice;
        this.shipped = shipped;
        this.items = items;
    }

    public Long getId() { return id; }
    public CustomerSummary getCustomer() { return customer; }
    public AddressSummary getShippingAddress() { return shippingAddress; }
    public BigDecimal getShippingCharge() { return shippingCharge; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public boolean isShipped() { return shipped; }
    public List<OrderLineResponse> getItems() { return items; }
}