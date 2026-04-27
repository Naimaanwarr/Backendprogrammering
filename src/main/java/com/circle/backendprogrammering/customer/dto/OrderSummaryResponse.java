package com.circle.backendprogrammering.customer.dto;

import java.math.BigDecimal;

public class OrderSummaryResponse {
    private Long id;
    private BigDecimal totalPrice;
    private boolean shipped;

    public OrderSummaryResponse(Long id, BigDecimal totalPrice, boolean shipped) {
        this.id = id;
        this.totalPrice = totalPrice;
        this.shipped = shipped;
    }

    public Long getId() { return id; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public boolean isShipped() { return shipped; }
}
