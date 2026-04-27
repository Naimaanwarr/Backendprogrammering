package com.circle.backendprogrammering.product;

import com.circle.backendprogrammering.BackendProgrammeringApplication;
import com.circle.backendprogrammering.TestcontainersConfiguration;
import com.circle.backendprogrammering.customer.CustomerAddressRepository;
import com.circle.backendprogrammering.customer.CustomerRepository;
import com.circle.backendprogrammering.order.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BackendProgrammeringApplication.class)
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class ProductControllerIntegrationTest {

    @Autowired MockMvc mockMvc;

    @Autowired ProductRepository productRepository;

    @Autowired OrderRepository orderRepository;
    @Autowired CustomerAddressRepository addressRepository;
    @Autowired CustomerRepository customerRepository;

    @BeforeEach
    void setup() {
        orderRepository.deleteAll();
        addressRepository.deleteAll();
        customerRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    void createProduct_returns201_andPersists() throws Exception {

        String body = """
                {
                  "name": "Fork",
                  "description": "Steel fork",
                  "price": 49.00,
                  "status": "ACTIVE",
                  "quantityOnHand": 100
                }
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Fork"))
                .andExpect(jsonPath("$.description").value("Steel fork"))
                .andExpect(jsonPath("$.price").value(49.00))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.quantityOnHand").value(100));

        assertThat(productRepository.count()).isEqualTo(1);
    }

    @Test
    void getProduct_returns200() throws Exception {

        Product p = new Product(
                "Knife",
                "Sharp knife",
                new BigDecimal("59.00"),
                ProductStatus.ACTIVE,
                50
        );

        Product saved = productRepository.save(p);

        mockMvc.perform(get("/api/products/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Knife"))
                .andExpect(jsonPath("$.description").value("Sharp knife"))
                .andExpect(jsonPath("$.price").value(59.00))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.quantityOnHand").value(50));
    }

    @Test
    void getProduct_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/products/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deleteProduct_returns204_andRemoves() throws Exception {

        Product p = new Product(
                "Spoon",
                "Steel spoon",
                new BigDecimal("39.00"),
                ProductStatus.ACTIVE,
                100
        );

        Product saved = productRepository.save(p);

        mockMvc.perform(delete("/api/products/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        assertThat(productRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void deleteProduct_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createProduct_validationError_returns400() throws Exception {

        String body = """
                {
                  "name": "",
                  "description": "x",
                  "price": -10,
                  "status": "ACTIVE",
                  "quantityOnHand": -5
                }
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
