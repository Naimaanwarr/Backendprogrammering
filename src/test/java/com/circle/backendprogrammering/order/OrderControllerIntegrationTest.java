package com.circle.backendprogrammering.order;

import com.circle.backendprogrammering.BackendProgrammeringApplication;
import com.circle.backendprogrammering.TestcontainersConfiguration;
import com.circle.backendprogrammering.customer.Customer;
import com.circle.backendprogrammering.customer.CustomerAddress;
import com.circle.backendprogrammering.customer.CustomerAddressRepository;
import com.circle.backendprogrammering.customer.CustomerRepository;
import com.circle.backendprogrammering.product.Product;
import com.circle.backendprogrammering.product.ProductRepository;
import com.circle.backendprogrammering.product.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = BackendProgrammeringApplication.class)
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class OrderControllerIntegrationTest {

    @Autowired MockMvc mockMvc;

    @Autowired CustomerRepository customerRepository;
    @Autowired CustomerAddressRepository addressRepository;
    @Autowired ProductRepository productRepository;
    @Autowired OrderRepository orderRepository;

    private Long customerId;
    private Long addressId;
    private Long forkId;
    private Long spoonId;

    @BeforeEach
    void setup() {
        orderRepository.deleteAll();
        addressRepository.deleteAll();
        productRepository.deleteAll();
        customerRepository.deleteAll();

        // Seed: Customer + Address
        Customer c = new Customer("Ola Nordmann", "12345678", "ola@example.com");
        CustomerAddress a = new CustomerAddress("Karl Johans gate 1", "Oslo", "0154", "Norway");
        c.addAddress(a);

        Customer savedCustomer = customerRepository.save(c);
        customerId = savedCustomer.getId();
        addressId = addressRepository.findByCustomerId(customerId).get(0).getId();

        // Seed: Products
        Product fork = new Product("Fork", "Standard steel fork", new BigDecimal("49.00"), ProductStatus.ACTIVE, 100);
        Product spoon = new Product("Spoon", "Standard steel spoon", new BigDecimal("39.00"), ProductStatus.ACTIVE, 100);

        forkId = productRepository.save(fork).getId();
        spoonId = productRepository.save(spoon).getId();
    }

    @Test
    void placeOrder_happyPath_returns201_andDecrementsStockInDb() throws Exception {
        String body = """
                {
                  "customerId": %d,
                  "shippingAddressId": %d,
                  "shippingCharge": 100.00,
                  "items": [
                    { "productId": %d, "quantity": 2 },
                    { "productId": %d, "quantity": 1 }
                  ]
                }
                """.formatted(customerId, addressId, forkId, spoonId);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.totalPrice").value(237.00))
                .andExpect(jsonPath("$.shipped").value(false))
                // customer details
                .andExpect(jsonPath("$.customer.id").value(customerId))
                .andExpect(jsonPath("$.customer.name").value("Ola Nordmann"))
                .andExpect(jsonPath("$.customer.email").value("ola@example.com"))
                // shipping address details
                .andExpect(jsonPath("$.shippingAddress.id").value(addressId))
                .andExpect(jsonPath("$.shippingAddress.city").value("Oslo"))
                // order lines
                .andExpect(jsonPath("$.items.length()").value(2));

        Product forkAfter = productRepository.findById(forkId).orElseThrow();
        Product spoonAfter = productRepository.findById(spoonId).orElseThrow();

        assertThat(forkAfter.getQuantityOnHand()).isEqualTo(98);
        assertThat(spoonAfter.getQuantityOnHand()).isEqualTo(99);

        // order saved
        assertThat(orderRepository.count()).isEqualTo(1);
    }

    @Test
    void placeOrder_outOfStock_returns409_andDoesNotCreateOrder() throws Exception {
        Product fork = productRepository.findById(forkId).orElseThrow();
        fork.setQuantityOnHand(1);
        productRepository.save(fork);

        String body = """
                {
                  "customerId": %d,
                  "shippingAddressId": %d,
                  "shippingCharge": 0.00,
                  "items": [
                    { "productId": %d, "quantity": 2 }
                  ]
                }
                """.formatted(customerId, addressId, forkId);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(containsStringIgnoringCase("out of stock")));

        assertThat(orderRepository.count()).isEqualTo(0);

        Product forkAfter = productRepository.findById(forkId).orElseThrow();
        assertThat(forkAfter.getQuantityOnHand()).isEqualTo(1);
    }

    @Test
    void placeOrder_customerNotFound_returns404() throws Exception {
        String body = """
                {
                  "customerId": 999999,
                  "shippingAddressId": %d,
                  "shippingCharge": 0.00,
                  "items": [
                    { "productId": %d, "quantity": 1 }
                  ]
                }
                """.formatted(addressId, forkId);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
