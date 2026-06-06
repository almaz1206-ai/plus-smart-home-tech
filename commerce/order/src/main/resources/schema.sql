CREATE TABLE IF NOT EXISTS orders (
    order_id UUID NOT NULL PRIMARY KEY,
    shopping_cart_id UUID,
    payment_id UUID,
    delivery_id UUID,
    state VARCHAR(20) NOT NULL,
    delivery_weight DOUBLE PRECISION,
    delivery_volume DOUBLE PRECISION,
    fragile BOOLEAN,
    total_price NUMERIC(38, 2),
    product_price NUMERIC(38, 2),
    delivery_price NUMERIC(38, 2),
    username VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS order_products (
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity BIGINT NOT NULL,

    PRIMARY KEY (order_id, product_id),

    CONSTRAINT fk_order_products_order
        FOREIGN KEY (order_id)
        REFERENCES orders(order_id)
);