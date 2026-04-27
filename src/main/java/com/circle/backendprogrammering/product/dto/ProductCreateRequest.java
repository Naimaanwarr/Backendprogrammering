package com.circle.backendprogrammering.product.dto;

import com.circle.backendprogrammering.product.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class ProductCreateRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    @PositiveOrZero
    private BigDecimal price;

    @NotNull
    private ProductStatus status;

    @NotNull
    @PositiveOrZero
    private Integer quantityOnHand;

    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public ProductStatus getStatus() { return status; }
    public Integer getQuantityOnHand() { return quantityOnHand; }
}
