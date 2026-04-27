package com.circle.backendprogrammering.order;

import com.circle.backendprogrammering.customer.Customer;
import com.circle.backendprogrammering.customer.CustomerAddress;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipping_address_id", nullable = false)
    private CustomerAddress shippingAddress;

    @Column(name = "shipping_charge", nullable = false, precision = 12, scale = 2)
    private BigDecimal shippingCharge;

    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Column(nullable = false)
    private boolean shipped = false;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}

    public Order(Customer customer, CustomerAddress shippingAddress, BigDecimal shippingCharge, BigDecimal totalPrice) {
        this.customer = customer;
        this.shippingAddress = shippingAddress;
        this.shippingCharge = shippingCharge;
        this.totalPrice = totalPrice;
    }

    public Long getId() { return id; }
    public Customer getCustomer() { return customer; }
    public CustomerAddress getShippingAddress() { return shippingAddress; }
    public BigDecimal getShippingCharge() { return shippingCharge; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public boolean isShipped() { return shipped; }
    public List<OrderItem> getItems() { return items; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }
}