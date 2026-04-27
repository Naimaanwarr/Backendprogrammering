-- Customers
INSERT INTO customers (id, name, phone_number, email)
VALUES (1, 'Ola Nordmann', '12345678', 'ola@example.com')
    ON CONFLICT (id) DO NOTHING;

-- Customer Addresses
INSERT INTO customer_addresses (id, customer_id, street, city, zip, country)
VALUES (1, 1, 'Karl Johans gate 1', 'Oslo', '0154', 'Norway')
    ON CONFLICT (id) DO NOTHING;

-- Products
INSERT INTO products (id, name, description, price, status, quantity_on_hand)
VALUES
    (1, 'Fork', 'Standard steel fork', 49.00, 'ACTIVE', 100),
    (2, 'Spoon', 'Standard steel spoon', 39.00, 'ACTIVE', 100),
    (3, 'Knife', 'Standard steel knife', 59.00, 'ACTIVE', 50)
    ON CONFLICT (id) DO NOTHING;
