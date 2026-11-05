-- E-Commerce Auto: database schema (PostgreSQL)

CREATE TABLE IF NOT EXISTS users (
    user_id       SERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name    VARCHAR(100),
    last_name     VARCHAR(100),
    phone         VARCHAR(30),
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS car_makes (
    make_id    SERIAL PRIMARY KEY,
    make_name  VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS car_models (
    model_id   SERIAL PRIMARY KEY,
    make_id    INTEGER NOT NULL REFERENCES car_makes (make_id) ON DELETE CASCADE,
    model_name VARCHAR(100) NOT NULL,
    body_type  VARCHAR(30),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (make_id, model_name)
);

CREATE TABLE IF NOT EXISTS car_listings (
    listing_id   SERIAL PRIMARY KEY,
    user_id      INTEGER NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    model_id     INTEGER NOT NULL REFERENCES car_models (model_id),
    title        VARCHAR(200) NOT NULL,
    description  TEXT,
    price        NUMERIC(12, 2) NOT NULL CHECK (price > 0),
    year         INTEGER NOT NULL CHECK (year BETWEEN 1900 AND 2100),
    mileage      INTEGER CHECK (mileage >= 0),
    fuel_type    VARCHAR(30),
    transmission VARCHAR(30),
    color        VARCHAR(50),
    location     VARCHAR(100),
    is_active    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_listings_active_created ON car_listings (is_active, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_listings_user ON car_listings (user_id);
CREATE INDEX IF NOT EXISTS idx_listings_model ON car_listings (model_id);
