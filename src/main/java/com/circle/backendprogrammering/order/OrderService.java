package com.circle.backendprogrammering.order;

import com.circle.backendprogrammering.customer.Customer;
import com.circle.backendprogrammering.customer.CustomerAddress;
import com.circle.backendprogrammering.customer.CustomerAddressService;
import com.circle.backendprogrammering.customer.CustomerService;
import com.circle.backendprogrammering.exception.OrderNotFoundException;
import com.circle.backendprogrammering.order.dto.*;
import com.circle.backendprogrammering.product.Product;
import com.circle.backendprogrammering.product.ProductService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final CustomerService customerService;
    private final CustomerAddressService addressService;
    private final ProductService productService;

    public OrderService(OrderRepository orderRepository,
                        CustomerService customerService,
                        CustomerAddressService addressService,
                        ProductService productService) {
        this.orderRepository = orderRepository;
        this.customerService = customerService;
        this.addressService = addressService;
        this.productService = productService;
    }

    public List<Order> listByCustomer(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {

        Customer customer = customerService.getOrThrow(request.getCustomerId());

        CustomerAddress address =
                addressService.getForCustomerOrThrow(customer.getId(), request.getShippingAddressId());

        BigDecimal shippingCharge = request.getShippingCharge();
        if (shippingCharge == null) shippingCharge = BigDecimal.ZERO;

        Order order = new Order(customer, address, shippingCharge, BigDecimal.ZERO);

        BigDecimal itemsTotal = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {

            int qty = itemRequest.getQuantity();

            Product product = productService.decrementStock(itemRequest.getProductId(), qty);

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(qty));
            itemsTotal = itemsTotal.add(lineTotal);

            OrderItem orderItem = new OrderItem(product, qty, product.getPrice());
            order.addItem(orderItem);
        }

        BigDecimal total = itemsTotal.add(shippingCharge);
        order.setTotalPrice(total);

        Order saved = orderRepository.save(order);

        log.info("Order placed: orderId={}, customerId={}, items={}, total={}",
                saved.getId(), customer.getId(), saved.getItems().size(), saved.getTotalPrice());

        return toResponse(saved);
    }

    @Transactional
    public OrderResponse getOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return toResponse(order);
    }

    private OrderResponse toResponse(Order order) {

        List<OrderLineResponse> lines = order.getItems().stream()
                .map(i -> new OrderLineResponse(
                        i.getProduct().getId(),
                        i.getProduct().getName(),
                        i.getQuantity(),
                        i.getUnitPrice()
                ))
                .toList();

        CustomerSummary customer = new CustomerSummary(
                order.getCustomer().getId(),
                order.getCustomer().getName(),
                order.getCustomer().getPhoneNumber(),
                order.getCustomer().getEmail()
        );

        AddressSummary address = new AddressSummary(
                order.getShippingAddress().getId(),
                order.getShippingAddress().getStreet(),
                order.getShippingAddress().getCity(),
                order.getShippingAddress().getZip(),
                order.getShippingAddress().getCountry()
        );

        return new OrderResponse(
                order.getId(),
                customer,
                address,
                order.getShippingCharge(),
                order.getTotalPrice(),
                order.isShipped(),
                lines
        );
    }
}