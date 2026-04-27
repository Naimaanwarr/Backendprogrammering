package com.circle.backendprogrammering.product;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class ProductEntityTest {

    @Test
    void equals_sameReference_true() {
        Product p = new Product("Fork", "Steel", new BigDecimal("10.00"), ProductStatus.ACTIVE, 5);

        assertThat(p).isEqualTo(p);
    }

    @Test
    void equals_otherType_false() {
        Product p = new Product("Fork", "Steel", new BigDecimal("10.00"), ProductStatus.ACTIVE, 5);

        assertThat(p.equals("not a product")).isFalse();
    }

    @Test
    void equals_bothIdsNull_false() {
        Product p1 = new Product("Fork", "Steel", new BigDecimal("10.00"), ProductStatus.ACTIVE, 5);
        Product p2 = new Product("Fork", "Steel", new BigDecimal("10.00"), ProductStatus.ACTIVE, 5);

        assertThat(p1).isNotEqualTo(p2);
    }

    @Test
    void equals_sameId_true_andDifferentId_false() {
        Product p1 = new Product("Fork", "Steel", new BigDecimal("10.00"), ProductStatus.ACTIVE, 5);
        Product p2 = new Product("Fork", "Steel", new BigDecimal("10.00"), ProductStatus.ACTIVE, 5);
        Product p3 = new Product("Fork", "Steel", new BigDecimal("10.00"), ProductStatus.ACTIVE, 5);

        setId(p1, 1L);
        setId(p2, 1L);
        setId(p3, 2L);

        assertThat(p1).isEqualTo(p2);
        assertThat(p1).isNotEqualTo(p3);
    }

    @Test
    void hashCode_runs() {
        Product p = new Product("Fork", "Steel", new BigDecimal("10.00"), ProductStatus.ACTIVE, 5);

        assertThat(p.hashCode()).isNotZero();
    }

    private static void setId(Object entity, Long id) {
        try {
            var f = entity.getClass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}