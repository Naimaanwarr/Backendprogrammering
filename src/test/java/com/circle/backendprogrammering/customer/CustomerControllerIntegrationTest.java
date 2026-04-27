package com.circle.backendprogrammering.customer;

import com.circle.backendprogrammering.BackendProgrammeringApplication;
import com.circle.backendprogrammering.TestcontainersConfiguration;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BackendProgrammeringApplication.class)
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class CustomerControllerIntegrationTest {


    @Autowired MockMvc mockMvc;

    @Autowired CustomerRepository customerRepository;
    @Autowired CustomerAddressRepository addressRepository;
    @Autowired OrderRepository orderRepository;

    @BeforeEach
    void setup() {
        orderRepository.deleteAll();
        addressRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void createCustomer_returns201_andFullDetails() throws Exception {

        String body = """
                {
                  "name": "Ola Nordmann",
                  "phoneNumber": "12345678",
                  "email": "ola@example.com"
                }
                """;

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Ola Nordmann"))
                .andExpect(jsonPath("$.phoneNumber").value("12345678"))
                .andExpect(jsonPath("$.email").value("ola@example.com"))
                .andExpect(jsonPath("$.addresses").isArray())
                .andExpect(jsonPath("$.orderHistory").isArray());

        assertThat(customerRepository.count()).isEqualTo(1);
    }

    @Test
    void getCustomer_returns200_andDetails() throws Exception {

        Customer c = new Customer("Kari Nordmann", "99999999", "kari@example.com");
        Customer saved = customerRepository.save(c);

        mockMvc.perform(get("/api/customers/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Kari Nordmann"))
                .andExpect(jsonPath("$.email").value("kari@example.com"))
                .andExpect(jsonPath("$.addresses").isArray())
                .andExpect(jsonPath("$.orderHistory").isArray());
    }

    @Test
    void getCustomer_notFound_returns404() throws Exception {

        mockMvc.perform(get("/api/customers/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deleteCustomer_returns204_andRemovesCustomer() throws Exception {

        Customer c = new Customer("Delete Me", "11111111", "delete@example.com");
        Customer saved = customerRepository.save(c);

        mockMvc.perform(delete("/api/customers/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        assertThat(customerRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void createCustomer_validationError_returns400() throws Exception {

        String body = """
                {
                  "name": "",
                  "phoneNumber": "12345678",
                  "email": "not-an-email"
                }
                """;

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}