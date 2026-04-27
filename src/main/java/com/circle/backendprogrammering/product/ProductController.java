package com.circle.backendprogrammering.product;

import com.circle.backendprogrammering.product.dto.ProductCreateRequest;
import com.circle.backendprogrammering.product.dto.ProductResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductCreateRequest req) {
        Product saved = productService.create(req);

        return new ProductResponse(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                saved.getPrice(),
                saved.getStatus(),
                saved.getQuantityOnHand()
        );
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id) {
        Product p = productService.getOrThrow(id);

        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getStatus(),
                p.getQuantityOnHand()
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }
}
