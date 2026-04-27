package com.circle.backendprogrammering.product;

import com.circle.backendprogrammering.exception.ProductNotFoundException;
import com.circle.backendprogrammering.product.dto.ProductCreateRequest;
import org.springframework.stereotype.Service;
import com.circle.backendprogrammering.exception.OutOfStockException;
import jakarta.transaction.Transactional;


@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product getOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public Product create(ProductCreateRequest req) {
        Product product = new Product(
                req.getName(),
                req.getDescription(),
                req.getPrice(),
                req.getStatus(),
                req.getQuantityOnHand()
        );

        return productRepository.save(product);
    }

    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }

    @Transactional
    public Product decrementStock(Long productId, int qty) {
        getOrThrow(productId);

        int updated = productRepository.decrementStockIfAvailable(productId, qty);
        if (updated == 0) {
            throw new OutOfStockException(productId);
        }

        return getOrThrow(productId);
    }

}
