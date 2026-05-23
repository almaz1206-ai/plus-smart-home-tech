CREATE TABLE IF NOT EXISTS shopping_cart (
    shopping_cart_id UUID NOT NULL,
    username VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS cart_product (
    cart_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity BIGINT
);