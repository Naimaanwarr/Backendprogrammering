package com.circle.backendprogrammering.customer;

import com.circle.backendprogrammering.customer.dto.AddressResponse;
import com.circle.backendprogrammering.customer.dto.CustomerDetailsResponse;
import com.circle.backendprogrammering.customer.dto.OrderSummaryResponse;
import com.circle.backendprogrammering.order.OrderService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerDetailsService {

    private final CustomerService customerService;
    private final CustomerAddressService addressService;
    private final OrderService orderService;

    public CustomerDetailsService(CustomerService customerService,
                                  CustomerAddressService addressService,
                                  OrderService orderService) {
        this.customerService = customerService;
        this.addressService = addressService;
        this.orderService = orderService;
    }

    public CustomerDetailsResponse getDetails(Long customerId) {
        Customer c = customerService.getOrThrow(customerId);

        List<AddressResponse> addresses = addressService.listByCustomer(customerId).stream()
                .map(a -> new AddressResponse(
                        a.getId(),
                        a.getStreet(),
                        a.getCity(),
                        a.getZip(),
                        a.getCountry()
                ))
                .toList();

        List<OrderSummaryResponse> history = orderService.listByCustomer(customerId).stream()
                .map(o -> new OrderSummaryResponse(
                        o.getId(),
                        o.getTotalPrice(),
                        o.isShipped()
                ))
                .toList();

        return new CustomerDetailsResponse(
                c.getId(),
                c.getName(),
                c.getPhoneNumber(),
                c.getEmail(),
                addresses,
                history
        );
    }
}