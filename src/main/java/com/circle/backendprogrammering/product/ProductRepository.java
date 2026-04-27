package com.circle.backendprogrammering.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Product p
           SET p.quantityOnHand = p.quantityOnHand - :qty
         WHERE p.id = :productId
           AND p.quantityOnHand >= :qty
    """)
    int decrementStockIfAvailable(@Param("productId") Long productId,
                                  @Param("qty") int qty);
}