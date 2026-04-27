package com.circle.backendprogrammering.order;

import com.circle.backendprogrammering.customer.Customer;
import com.circle.backendprogrammering.customer.CustomerAddress;
import com.circle.backendprogrammering.customer.CustomerAddressService;
import com.circle.backendprogrammering.customer.CustomerService;
import com.circle.backendprogrammering.exception.*;
import com.circle.backendprogrammering.order.dto.OrderItemRequest;
import com.circle.backendprogrammering.order.dto.OrderResponse;
import com.circle.backendprogrammering.order.dto.PlaceOrderRequest;
import com.circle.backendprogrammering.product.Product;
import com.circle.backendprogrammering.product.ProductService;
import com.circle.backendprogrammering.product.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private CustomerService customerService;
    private CustomerAddressService addressService;
    private ProductService productService;

    private OrderService orderService;

    @BeforeEach
    void setup() {
        orderRepository = mock(OrderRepository.class);
        customerService = mock(CustomerService.class);
        addressService = mock(CustomerAddressService.class);
        productService = mock(ProductService.class);

        orderService = new OrderService(orderRepository, customerService, addressService, productService);
    }

    @Test
    void placeOrder_happyPath_calculatesTotal_andDecrementsStock_andSavesOrder() {
        PlaceOrderRequest req = TestData.placeOrderRequest(
                1L, 1L, new BigDecimal("100.00"),
                List.of(
                        TestData.item(1L, 2),
                        TestData.item(2L, 1)
                )
        );

        Customer customer = TestData.customerWithId(1L);
        CustomerAddress address = TestData.addressWithId(1L, customer);

        Product fork = TestData.productWithId(1L, "Fork", new BigDecimal("49.00"), 100);
        Product spoon = TestData.productWithId(2L, "Spoon", new BigDecimal("39.00"), 100);

        when(customerService.getOrThrow(1L)).thenReturn(customer);
        when(addressService.getForCustomerOrThrow(1L, 1L)).thenReturn(address);

        // decrementStock returnerer "lagret" produkt med redusert stock
        Product forkAfter = TestData.productWithId(1L, "Fork", new BigDecimal("49.00"), 98);
        Product spoonAfter = TestData.productWithId(2L, "Spoon", new BigDecimal("39.00"), 99);

        when(productService.decrementStock(1L, 2)).thenReturn(forkAfter);
        when(productService.decrementStock(2L, 1)).thenReturn(spoonAfter);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        when(orderRepository.save(orderCaptor.capture())).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            TestData.setId(o, 10L); // simuler DB id
            return o;
        });

        OrderResponse res = orderService.placeOrder(req);

        assertThat(res.getId()).isEqualTo(10L);
        assertThat(res.getCustomer().getId()).isEqualTo(1L);
        assertThat(res.getShippingAddress().getId()).isEqualTo(1L);
        assertThat(res.getTotalPrice()).isEqualByComparingTo("237.00");
        assertThat(res.getItems()).hasSize(2);

        verify(productService, times(1)).decrementStock(1L, 2);
        verify(productService, times(1)).decrementStock(2L, 1);

        verify(orderRepository, times(1)).save(any(Order.class));

        Order saved = orderCaptor.getValue();
        assertThat(saved.getTotalPrice()).isEqualByComparingTo("237.00");
        assertThat(saved.getItems()).hasSize(2);
    }

    @Test
    void placeOrder_customerNotFound_throws() {
        PlaceOrderRequest req = TestData.placeOrderRequest(
                1L, 1L, new BigDecimal("0.00"),
                List.of(TestData.item(1L, 1))
        );

        when(customerService.getOrThrow(1L)).thenThrow(new CustomerNotFoundException(1L));

        assertThatThrownBy(() -> orderService.placeOrder(req))
                .isInstanceOf(CustomerNotFoundException.class);

        verifyNoInteractions(addressService, productService, orderRepository);
    }

    @Test
    void placeOrder_addressNotFound_throws() {
        PlaceOrderRequest req = TestData.placeOrderRequest(
                1L, 1L, new BigDecimal("0.00"),
                List.of(TestData.item(1L, 1))
        );

        Customer customer = TestData.customerWithId(1L);

        when(customerService.getOrThrow(1L)).thenReturn(customer);
        when(addressService.getForCustomerOrThrow(1L, 1L)).thenThrow(new AddressNotFoundException(1L));

        assertThatThrownBy(() -> orderService.placeOrder(req))
                .isInstanceOf(AddressNotFoundException.class);

        verifyNoInteractions(productService, orderRepository);
    }

    @Test
    void placeOrder_addressDoesNotBelongToCustomer_throws() {
        PlaceOrderRequest req = TestData.placeOrderRequest(
                1L, 1L, new BigDecimal("0.00"),
                List.of(TestData.item(1L, 1))
        );

        Customer customer = TestData.customerWithId(1L);

        when(customerService.getOrThrow(1L)).thenReturn(customer);
        when(addressService.getForCustomerOrThrow(1L, 1L))
                .thenThrow(new AddressDoesNotBelongToCustomerException(1L, 1L));

        assertThatThrownBy(() -> orderService.placeOrder(req))
                .isInstanceOf(AddressDoesNotBelongToCustomerException.class);

        verifyNoInteractions(productService, orderRepository);
    }

    @Test
    void placeOrder_productNotFound_throws() {
        PlaceOrderRequest req = TestData.placeOrderRequest(
                1L, 1L, new BigDecimal("0.00"),
                List.of(TestData.item(1L, 1))
        );

        Customer customer = TestData.customerWithId(1L);
        CustomerAddress address = TestData.addressWithId(1L, customer);

        when(customerService.getOrThrow(1L)).thenReturn(customer);
        when(addressService.getForCustomerOrThrow(1L, 1L)).thenReturn(address);
        when(productService.decrementStock(1L, 1)).thenThrow(new ProductNotFoundException(1L));

        assertThatThrownBy(() -> orderService.placeOrder(req))
                .isInstanceOf(ProductNotFoundException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrder_outOfStock_throws_andDoesNotSaveOrder() {
        PlaceOrderRequest req = TestData.placeOrderRequest(
                1L, 1L, new BigDecimal("0.00"),
                List.of(TestData.item(1L, 5))
        );

        Customer customer = TestData.customerWithId(1L);
        CustomerAddress address = TestData.addressWithId(1L, customer);

        when(customerService.getOrThrow(1L)).thenReturn(customer);
        when(addressService.getForCustomerOrThrow(1L, 1L)).thenReturn(address);
        when(productService.decrementStock(1L, 5)).thenThrow(new OutOfStockException(1L));

        assertThatThrownBy(() -> orderService.placeOrder(req))
                .isInstanceOf(OutOfStockException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void getOrder_orderNotFound_throws() {
        when(orderRepository.findById(123L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder(123L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void getOrder_happyPath_mapsToResponse() {
        Customer customer = TestData.customerWithId(1L);
        CustomerAddress address = TestData.addressWithId(1L, customer);

        Product fork = TestData.productWithId(1L, "Fork", new BigDecimal("49.00"), 100);
        OrderItem item = new OrderItem(fork, 2, new BigDecimal("49.00"));

        Order order = new Order(customer, address, new BigDecimal("100.00"), new BigDecimal("237.00"));
        TestData.setId(order, 77L);
        order.addItem(item);

        when(orderRepository.findById(77L)).thenReturn(Optional.of(order));

        OrderResponse res = orderService.getOrder(77L);

        assertThat(res.getId()).isEqualTo(77L);
        assertThat(res.getCustomer().getId()).isEqualTo(1L);
        assertThat(res.getShippingAddress().getId()).isEqualTo(1L);
        assertThat(res.getTotalPrice()).isEqualByComparingTo("237.00");
        assertThat(res.getItems()).hasSize(1);
        assertThat(res.getItems().get(0).getProductName()).isEqualTo("Fork");
    }

    static class TestData {

        static PlaceOrderRequest placeOrderRequest(Long customerId, Long addressId, BigDecimal shipping, List<OrderItemRequest> items) {
            return new PlaceOrderRequest() {
                @Override public Long getCustomerId() { return customerId; }
                @Override public Long getShippingAddressId() { return addressId; }
                @Override public BigDecimal getShippingCharge() { return shipping; }
                @Override public List<OrderItemRequest> getItems() { return items; }
            };
        }

        static OrderItemRequest item(Long productId, Integer qty) {
            return new OrderItemRequest() {
                @Override public Long getProductId() { return productId; }
                @Override public Integer getQuantity() { return qty; }
            };
        }

        static Customer customerWithId(Long id) {
            Customer c = new Customer("X", "123", "x@example.com");
            setId(c, id);
            return c;
        }

        static CustomerAddress addressWithId(Long id, Customer customer) {
            CustomerAddress a = new CustomerAddress("Street", "City", "0000", "Norway");
            customer.addAddress(a);
            setId(a, id);
            return a;
        }

        static Product productWithId(Long id, String name, BigDecimal price, int stock) {
            Product p = new Product(name, "desc", price, ProductStatus.ACTIVE, stock);
            setId(p, id);
            return p;
        }

        static void setId(Object entity, Long id) {
            try {
                var f = entity.getClass().getDeclaredField("id");
                f.setAccessible(true);
                f.set(entity, id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}