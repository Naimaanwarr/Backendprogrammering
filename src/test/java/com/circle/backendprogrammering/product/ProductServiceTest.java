package com.circle.backendprogrammering.product;

import com.circle.backendprogrammering.exception.OutOfStockException;
import com.circle.backendprogrammering.exception.ProductNotFoundException;
import com.circle.backendprogrammering.product.dto.ProductCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    private ProductRepository productRepository;
    private ProductService productService;

    @BeforeEach
    void setup() {
        productRepository = mock(ProductRepository.class);
        productService = new ProductService(productRepository);
    }

    @Test
    void getOrThrow_found_returnsProduct() {
        Product p = new Product("Fork", "desc", new BigDecimal("10.00"), ProductStatus.ACTIVE, 10);
        setId(p, 1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(p));

        Product res = productService.getOrThrow(1L);

        assertThat(res.getId()).isEqualTo(1L);
        verify(productRepository).findById(1L);
    }

    @Test
    void getOrThrow_notFound_throws() {
        when(productRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getOrThrow(404L))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository).findById(404L);
    }

    @Test
    void create_savesAndReturnsProduct() {
        ProductCreateRequest req = createReq("Knife", "Sharp", new BigDecimal("59.00"), ProductStatus.ACTIVE, 50);

        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            setId(p, 7L);
            return p;
        });

        Product saved = productService.create(req);

        assertThat(saved.getId()).isEqualTo(7L);
        assertThat(saved.getName()).isEqualTo("Knife");
        assertThat(saved.getPrice()).isEqualByComparingTo("59.00");
        assertThat(saved.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(saved.getQuantityOnHand()).isEqualTo(50);

        verify(productRepository).save(any(Product.class));
    }

    @Test
    void delete_existing_deletes() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.delete(1L);

        verify(productRepository).existsById(1L);
        verify(productRepository).deleteById(1L);
    }

    @Test
    void delete_notFound_throws() {
        when(productRepository.existsById(404L)).thenReturn(false);

        assertThatThrownBy(() -> productService.delete(404L))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository).existsById(404L);
        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    void decrementStock_productNotFound_throws_andDoesNotUpdate() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.decrementStock(1L, 1))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, never()).decrementStockIfAvailable(anyLong(), anyInt());
    }

    @Test
    void decrementStock_outOfStock_throws() {
        Product p = new Product("Fork", "desc", new BigDecimal("10.00"), ProductStatus.ACTIVE, 1);
        setId(p, 1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        when(productRepository.decrementStockIfAvailable(1L, 5)).thenReturn(0);

        assertThatThrownBy(() -> productService.decrementStock(1L, 5))
                .isInstanceOf(OutOfStockException.class);

        verify(productRepository).decrementStockIfAvailable(1L, 5);
    }

    @Test
    void decrementStock_happyPath_updates_andReturnsFreshProduct() {
        Product before = new Product("Fork", "desc", new BigDecimal("10.00"), ProductStatus.ACTIVE, 10);
        setId(before, 1L);

        Product after = new Product("Fork", "desc", new BigDecimal("10.00"), ProductStatus.ACTIVE, 8);
        setId(after, 1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(before), Optional.of(after));
        when(productRepository.decrementStockIfAvailable(1L, 2)).thenReturn(1);

        Product res = productService.decrementStock(1L, 2);

        assertThat(res.getQuantityOnHand()).isEqualTo(8);
        verify(productRepository).decrementStockIfAvailable(1L, 2);
    }

    private static ProductCreateRequest createReq(String name, String desc, BigDecimal price, ProductStatus status, int qoh) {
        return new ProductCreateRequest() {
            @Override public String getName() { return name; }
            @Override public String getDescription() { return desc; }
            @Override public BigDecimal getPrice() { return price; }
            @Override public ProductStatus getStatus() { return status; }
            @Override public Integer getQuantityOnHand() { return qoh; }
        };
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