package com.circle.backendprogrammering.customer;

import com.circle.backendprogrammering.customer.dto.AddressCreateRequest;
import com.circle.backendprogrammering.exception.AddressDoesNotBelongToCustomerException;
import com.circle.backendprogrammering.exception.AddressNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerAddressService {

    private final CustomerAddressRepository addressRepository;
    private final CustomerService customerService;

    public CustomerAddressService(CustomerAddressRepository addressRepository,
                                  CustomerService customerService) {
        this.addressRepository = addressRepository;
        this.customerService = customerService;
    }

    public List<CustomerAddress> listByCustomer(Long customerId) {
        customerService.getOrThrow(customerId);
        return addressRepository.findByCustomerId(customerId);
    }

    public CustomerAddress getOrThrow(Long id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new AddressNotFoundException(id));
    }

    public CustomerAddress createForCustomer(Long customerId, AddressCreateRequest req) {
        Customer customer = customerService.getOrThrow(customerId);

        CustomerAddress address = new CustomerAddress(
                req.getStreet(),
                req.getCity(),
                req.getZip(),
                req.getCountry()
        );

        customer.addAddress(address);

        return addressRepository.save(address);
    }

    public void delete(Long id) {
        if (!addressRepository.existsById(id)) {
            throw new AddressNotFoundException(id);
        }
        addressRepository.deleteById(id);
    }

    public CustomerAddress getForCustomerOrThrow(Long customerId, Long addressId) {
        return addressRepository.findByIdAndCustomerId(addressId, customerId)
                .orElseThrow(() -> new AddressDoesNotBelongToCustomerException(customerId, addressId));
    }
}