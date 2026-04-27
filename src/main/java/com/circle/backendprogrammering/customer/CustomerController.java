package com.circle.backendprogrammering.customer;

import com.circle.backendprogrammering.customer.dto.CustomerCreateRequest;
import com.circle.backendprogrammering.customer.dto.CustomerDetailsResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerDetailsService detailsService;

    public CustomerController(CustomerService customerService, CustomerDetailsService detailsService) {
        this.customerService = customerService;
        this.detailsService = detailsService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerDetailsResponse create(@Valid @RequestBody CustomerCreateRequest req) {
        Customer created = customerService.create(req);
        return detailsService.getDetails(created.getId());
    }

    @GetMapping("/{id}")
    public CustomerDetailsResponse get(@PathVariable Long id) {
        return detailsService.getDetails(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        customerService.delete(id);
    }
}