package com.circle.backendprogrammering.customer;

import com.circle.backendprogrammering.customer.dto.CustomerCreateRequest;
import com.circle.backendprogrammering.exception.CustomerNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer getOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    public Customer create(CustomerCreateRequest req) {
        Customer c = new Customer(req.getName(), req.getPhoneNumber(), req.getEmail());
        return customerRepository.save(c);
    }

    public void delete(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new CustomerNotFoundException(id);
        }
        customerRepository.deleteById(id);
    }
}