package com.circle.backendprogrammering.product.dto;

import com.circle.backendprogrammering.product.ProductStatus;

import java.math.BigDecimal;

public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private ProductStatus status;
    private Integer quantityOnHand;

    public ProductResponse(Long id,
                           String name,
                           String description,
                           BigDecimal price,
                           ProductStatus status,
                           Integer quantityOnHand) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.status = status;
        this.quantityOnHand = quantityOnHand;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public ProductStatus getStatus() { return status; }
    public Integer getQuantityOnHand() { return quantityOnHand; }
}
