package com.circle.backendprogrammering.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

public class PlaceOrderRequest {

    @NotNull
    private Long customerId;

    @NotNull
    private Long shippingAddressId;

    @NotNull
    @PositiveOrZero
    private BigDecimal shippingCharge;

    @NotEmpty
    private List<@Valid OrderItemRequest> items;

    public Long getCustomerId() { return customerId; }
    public Long getShippingAddressId() { return shippingAddressId; }
    public BigDecimal getShippingCharge() { return shippingCharge; }
    public List<OrderItemRequest> getItems() { return items; }
}