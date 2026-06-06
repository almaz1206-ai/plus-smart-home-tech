CREATE TABLE IF NOT EXISTS warehouse (
    product_id UUID NOT NULL UNIQUE PRIMARY KEY,
    fragile BOOLEAN,
    width DOUBLE PRECISION NOT NULL,
    height DOUBLE PRECISION NOT NULL,
    depth DOUBLE PRECISION NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    quantity BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS order_booking (
    order_id UUID NOT NULL PRIMARY KEY,
    delivery_id UUID
);

CREATE TABLE IF NOT EXISTS order_booking_products (
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity BIGINT NOT NULL,
    PRIMARY KEY (order_id, product_id),
    CONSTRAINT fk_order_booking_products_order
        FOREIGN KEY (order_id)
        REFERENCES order_booking(order_id)
        ON DELETE CASCADE
);