package com.circle.backendprogrammering.customer;

import com.circle.backendprogrammering.customer.dto.AddressCreateRequest;
import com.circle.backendprogrammering.exception.AddressDoesNotBelongToCustomerException;
import com.circle.backendprogrammering.exception.AddressNotFoundException;
import com.circle.backendprogrammering.exception.CustomerNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerAddressServiceTest {

    private CustomerAddressRepository addressRepository;
    private CustomerService customerService;
    private CustomerAddressService addressService;

    @BeforeEach
    void setup() {
        addressRepository = mock(CustomerAddressRepository.class);
        customerService = mock(CustomerService.class);
        addressService = new CustomerAddressService(addressRepository, customerService);
    }

    @Test
    void listByCustomer_customerExists_returnsAddresses() {
        Long customerId = 1L;

        when(customerService.getOrThrow(customerId)).thenReturn(new Customer("X", "123", "x@example.com"));
        when(addressRepository.findByCustomerId(customerId)).thenReturn(List.of(
                new CustomerAddress("Street1", "City1", "0001", "Norway"),
                new CustomerAddress("Street2", "City2", "0002", "Norway")
        ));

        List<CustomerAddress> res = addressService.listByCustomer(customerId);

        assertThat(res).hasSize(2);
        verify(customerService, times(1)).getOrThrow(customerId);
        verify(addressRepository, times(1)).findByCustomerId(customerId);
    }

    @Test
    void listByCustomer_customerNotFound_throws() {
        Long customerId = 99L;

        when(customerService.getOrThrow(customerId)).thenThrow(new CustomerNotFoundException(customerId));

        assertThatThrownBy(() -> addressService.listByCustomer(customerId))
                .isInstanceOf(CustomerNotFoundException.class);

        verify(addressRepository, never()).findByCustomerId(anyLong());
    }

    @Test
    void getOrThrow_found_returnsAddress() {
        Long addressId = 10L;
        CustomerAddress a = new CustomerAddress("Street", "City", "0000", "Norway");
        setId(a, addressId);

        when(addressRepository.findById(addressId)).thenReturn(Optional.of(a));

        CustomerAddress res = addressService.getOrThrow(addressId);

        assertThat(res.getId()).isEqualTo(addressId);
        verify(addressRepository, times(1)).findById(addressId);
    }

    @Test
    void getOrThrow_notFound_throws() {
        Long addressId = 404L;

        when(addressRepository.findById(addressId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.getOrThrow(addressId))
                .isInstanceOf(AddressNotFoundException.class);

        verify(addressRepository, times(1)).findById(addressId);
    }

    @Test
    void createForCustomer_happyPath_savesAddress() {
        Long customerId = 1L;

        Customer customer = new Customer("Ola", "12345678", "ola@example.com");
        setId(customer, customerId);

        AddressCreateRequest req = request("Karl Johans gate 1", "Oslo", "0154", "Norway");

        when(customerService.getOrThrow(customerId)).thenReturn(customer);

        when(addressRepository.save(any(CustomerAddress.class))).thenAnswer(inv -> {
            CustomerAddress saved = inv.getArgument(0);
            setId(saved, 99L);
            return saved;
        });

        CustomerAddress saved = addressService.createForCustomer(customerId, req);

        assertThat(saved.getId()).isEqualTo(99L);
        assertThat(saved.getStreet()).isEqualTo("Karl Johans gate 1");
        assertThat(saved.getCity()).isEqualTo("Oslo");
        assertThat(saved.getZip()).isEqualTo("0154");
        assertThat(saved.getCountry()).isEqualTo("Norway");

        assertThat(saved.getCustomer()).isNotNull();
        assertThat(saved.getCustomer().getId()).isEqualTo(customerId);

        verify(customerService, times(1)).getOrThrow(customerId);
        verify(addressRepository, times(1)).save(any(CustomerAddress.class));
    }

    @Test
    void delete_existing_deletes() {
        Long addressId = 1L;

        when(addressRepository.existsById(addressId)).thenReturn(true);

        addressService.delete(addressId);

        verify(addressRepository, times(1)).existsById(addressId);
        verify(addressRepository, times(1)).deleteById(addressId);
    }

    @Test
    void delete_notFound_throws() {
        Long addressId = 404L;

        when(addressRepository.existsById(addressId)).thenReturn(false);

        assertThatThrownBy(() -> addressService.delete(addressId))
                .isInstanceOf(AddressNotFoundException.class);

        verify(addressRepository, times(1)).existsById(addressId);
        verify(addressRepository, never()).deleteById(anyLong());
    }

    @Test
    void getForCustomerOrThrow_happyPath_returnsAddress() {
        Long customerId = 1L;
        Long addressId = 10L;

        CustomerAddress a = new CustomerAddress("Street", "City", "0000", "Norway");
        setId(a, addressId);

        when(addressRepository.findByIdAndCustomerId(addressId, customerId)).thenReturn(Optional.of(a));

        CustomerAddress res = addressService.getForCustomerOrThrow(customerId, addressId);

        assertThat(res.getId()).isEqualTo(addressId);

        verify(addressRepository, times(1)).findByIdAndCustomerId(addressId, customerId);
    }

    @Test
    void getForCustomerOrThrow_notOwnedOrMissing_throws() {
        Long customerId = 1L;
        Long addressId = 10L;

        when(addressRepository.findByIdAndCustomerId(addressId, customerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.getForCustomerOrThrow(customerId, addressId))
                .isInstanceOf(AddressDoesNotBelongToCustomerException.class);

        verify(addressRepository, times(1)).findByIdAndCustomerId(addressId, customerId);
    }

    private static AddressCreateRequest request(String street, String city, String zip, String country) {
        return new AddressCreateRequest() {
            @Override public String getStreet() { return street; }
            @Override public String getCity() { return city; }
            @Override public String getZip() { return zip; }
            @Override public String getCountry() { return country; }
        };
    }

    private static void setId(Object entity, Long id) {
        try {
            var f = entity.getClass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}