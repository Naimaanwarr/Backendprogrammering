-- V1__init.sql
-- Core schema for ForkMasters

CREATE TABLE customers (
                           id              BIGSERIAL PRIMARY KEY,
                           name            VARCHAR(255) NOT NULL,
                           phone_number    VARCHAR(50)  NOT NULL,
                           email           VARCHAR(255) NOT NULL UNIQUE,
                           created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE customer_addresses (
                                    id          BIGSERIAL PRIMARY KEY,
                                    customer_id BIGINT NOT NULL,
                                    street      VARCHAR(255) NOT NULL,
                                    city        VARCHAR(120) NOT NULL,
                                    zip         VARCHAR(30)  NOT NULL,
                                    country     VARCHAR(120) NOT NULL,
                                    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

                                    CONSTRAINT fk_customer_addresses_customer
                                        FOREIGN KEY (customer_id) REFERENCES customers(id)
                                            ON DELETE CASCADE
);

CREATE TABLE products (
                          id                  BIGSERIAL PRIMARY KEY,
                          name                VARCHAR(255) NOT NULL,
                          description         TEXT,
                          price               NUMERIC(12,2) NOT NULL CHECK (price >= 0),
                          status              VARCHAR(50) NOT NULL,
                          quantity_on_hand    INTEGER NOT NULL CHECK (quantity_on_hand >= 0),
                          created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE orders (
                        id                  BIGSERIAL PRIMARY KEY,
                        customer_id         BIGINT NOT NULL,
                        shipping_address_id BIGINT NOT NULL,
                        shipping_charge     NUMERIC(12,2) NOT NULL CHECK (shipping_charge >= 0),
                        total_price         NUMERIC(12,2) NOT NULL CHECK (total_price >= 0),
                        shipped             BOOLEAN NOT NULL DEFAULT FALSE,
                        created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                        CONSTRAINT fk_orders_customer
                            FOREIGN KEY (customer_id) REFERENCES customers(id),

                        CONSTRAINT fk_orders_shipping_address
                            FOREIGN KEY (shipping_address_id) REFERENCES customer_addresses(id)
);

CREATE TABLE order_items (
                             id          BIGSERIAL PRIMARY KEY,
                             order_id    BIGINT NOT NULL,
                             product_id  BIGINT NOT NULL,
                             quantity    INTEGER NOT NULL CHECK (quantity > 0),
                             unit_price  NUMERIC(12,2) NOT NULL CHECK (unit_price >= 0),

                             CONSTRAINT fk_order_items_order
                                 FOREIGN KEY (order_id) REFERENCES orders(id)
                                     ON DELETE CASCADE,

                             CONSTRAINT fk_order_items_product
                                 FOREIGN KEY (product_id) REFERENCES products(id),

                             CONSTRAINT uq_order_items_order_product UNIQUE (order_id, product_id)
);

CREATE INDEX idx_customer_addresses_customer_id ON customer_addresses(customer_id);
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_shipping_address_id ON orders(shipping_address_id);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);
