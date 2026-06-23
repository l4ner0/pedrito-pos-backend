CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE businesses (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(150) NOT NULL,
    ruc         VARCHAR(20),
    address     VARCHAR(255),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE business_settings (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id   UUID NOT NULL UNIQUE REFERENCES businesses(id) ON DELETE CASCADE,
    yape_number   VARCHAR(15),
    yape_qr_url   VARCHAR(500),
    print_enabled BOOLEAN NOT NULL DEFAULT true,
    ticket_footer VARCHAR(255)
);

CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id   UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(150) NOT NULL,
    avatar_url    VARCHAR(500),
    role          VARCHAR(20) NOT NULL DEFAULT 'CAJERO'
                  CHECK (role IN ('ADMIN','CAJERO')),
    active        BOOLEAN NOT NULL DEFAULT true,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE categories (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    name        VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (business_id, name)
);

CREATE TABLE products (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id          UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    category_id          UUID REFERENCES categories(id) ON DELETE SET NULL,
    name                 VARCHAR(150) NOT NULL,
    sku                  VARCHAR(60),
    price                NUMERIC(10,2) NOT NULL CHECK (price >= 0),
    stock                INT NOT NULL DEFAULT 0 CHECK (stock >= 0),
    low_stock_threshold  INT NOT NULL DEFAULT 8,
    active               BOOLEAN NOT NULL DEFAULT true,
    version              BIGINT NOT NULL DEFAULT 0,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (business_id, sku)
);

CREATE TABLE sales (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id     UUID NOT NULL REFERENCES businesses(id),
    user_id         UUID NOT NULL REFERENCES users(id),
    ticket_code     VARCHAR(30) NOT NULL UNIQUE,
    subtotal        NUMERIC(10,2) NOT NULL CHECK (subtotal >= 0),
    discount_amount NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (discount_amount >= 0),
    total           NUMERIC(10,2) NOT NULL CHECK (total >= 0),
    payment_method  VARCHAR(20) NOT NULL
                    CHECK (payment_method IN ('Efectivo','Tarjeta','Yape')),
    amount_received NUMERIC(10,2),
    change_given    NUMERIC(10,2),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE sale_items (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sale_id      UUID NOT NULL REFERENCES sales(id) ON DELETE CASCADE,
    product_id   UUID NOT NULL REFERENCES products(id),
    product_name VARCHAR(150) NOT NULL,
    unit_price   NUMERIC(10,2) NOT NULL CHECK (unit_price >= 0),
    quantity     INT NOT NULL CHECK (quantity > 0),
    line_total   NUMERIC(10,2) NOT NULL CHECK (line_total >= 0)
);

CREATE INDEX idx_products_business   ON products(business_id) WHERE active;
CREATE INDEX idx_sales_business_date ON sales(business_id, created_at);
CREATE INDEX idx_sale_items_sale     ON sale_items(sale_id);