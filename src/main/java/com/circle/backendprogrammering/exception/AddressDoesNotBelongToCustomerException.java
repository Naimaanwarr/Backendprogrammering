package com.circle.backendprogrammering.exception;

public class AddressDoesNotBelongToCustomerException extends RuntimeException {
    public AddressDoesNotBelongToCustomerException(Long customerId, Long addressId) {
        super("Address " + addressId + " does not belong to customer " + customerId);
    }
}