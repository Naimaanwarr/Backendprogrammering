package com.circle.backendprogrammering.customer;

import com.circle.backendprogrammering.customer.dto.AddressCreateRequest;
import com.circle.backendprogrammering.customer.dto.AddressResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class CustomerAddressController {

    private final CustomerAddressService addressService;

    public CustomerAddressController(CustomerAddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping("/api/customers/{customerId}/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    public AddressResponse create(@PathVariable Long customerId,
                                  @Valid @RequestBody AddressCreateRequest req) {
        CustomerAddress saved = addressService.createForCustomer(customerId, req);
        return new AddressResponse(
                saved.getId(),
                saved.getStreet(),
                saved.getCity(),
                saved.getZip(),
                saved.getCountry()
        );
    }

    @GetMapping("/api/addresses/{id}")
    public AddressResponse get(@PathVariable Long id) {
        CustomerAddress a = addressService.getOrThrow(id);
        return new AddressResponse(
                a.getId(),
                a.getStreet(),
                a.getCity(),
                a.getZip(),
                a.getCountry()
        );
    }

    @DeleteMapping("/api/addresses/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        addressService.delete(id);
    }
}