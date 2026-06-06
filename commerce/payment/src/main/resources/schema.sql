CREATE TABLE IF NOT EXISTS payments (
    payment_id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    product_total NUMERIC(38, 2) NOT NULL,
    delivery_total NUMERIC(38, 2) NOT NULL,
    fee_total NUMERIC(38, 2) NOT NULL,
    total_payment NUMERIC(38, 2) NOT NULL,
    state VARCHAR(20) NOT NULL,

    CONSTRAINT uq_payments_order_id UNIQUE (order_id)
);