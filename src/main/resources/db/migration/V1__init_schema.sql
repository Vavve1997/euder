CREATE SCHEMA IF NOT EXISTS eurder;
CREATE TABLE users
(
    id            VARCHAR(255) NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(254) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    phone_number  VARCHAR(30)  NOT NULL,

    street        VARCHAR(200) NOT NULL,
    house_number  VARCHAR(20)  NOT NULL,
    postal_code   VARCHAR(20)  NOT NULL,
    city          VARCHAR(100) NOT NULL,

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email),

    CONSTRAINT chk_users_first_name
        CHECK (TRIM(first_name) <> ''),
    CONSTRAINT chk_users_last_name
        CHECK (TRIM(last_name) <> ''),
    CONSTRAINT chk_users_email
        CHECK (TRIM(email) <> ''),
    CONSTRAINT chk_users_password_hash
        CHECK (TRIM(password_hash) <> ''),
    CONSTRAINT chk_users_role
        CHECK (role IN ('ADMIN', 'MEMBER')),
    CONSTRAINT chk_users_phone_number
        CHECK (TRIM(phone_number) <> ''),

    CONSTRAINT chk_users_street
        CHECK (TRIM(street) <> ''),
    CONSTRAINT chk_users_house_number
        CHECK (TRIM(house_number) <> ''),
    CONSTRAINT chk_users_postal_code
        CHECK (TRIM(postal_code) <> ''),
    CONSTRAINT chk_users_city
        CHECK (TRIM(city) <> '')
);


CREATE TABLE items
(
    id          VARCHAR(255)   NOT NULL,
    name        VARCHAR(200)   NOT NULL,
    description TEXT           NOT NULL,
    price       NUMERIC(12, 2) NOT NULL,
    amount      INTEGER        NOT NULL,
    version     BIGINT         NOT NULL DEFAULT 0,

    CONSTRAINT pk_items PRIMARY KEY (id),

    CONSTRAINT chk_items_name
        CHECK (TRIM(name) <> ''),
    CONSTRAINT chk_items_description
        CHECK (TRIM(description) <> ''),
    CONSTRAINT chk_items_price
        CHECK (price > 0),
    CONSTRAINT chk_items_amount
        CHECK (amount >= 0),
    CONSTRAINT chk_items_version
        CHECK (version >= 0)
);


CREATE TABLE eurders
(
    id                    VARCHAR(255) NOT NULL,
    customer_id           VARCHAR(255) NOT NULL,

    -- Kopie van het leveradres op het moment van bestellen.
    shipping_street       VARCHAR(200) NOT NULL,
    shipping_house_number VARCHAR(20)  NOT NULL,
    shipping_postal_code  VARCHAR(20)  NOT NULL,
    shipping_city         VARCHAR(100) NOT NULL,

    CONSTRAINT pk_eurders PRIMARY KEY (id),

    CONSTRAINT fk_eurders_customer
        FOREIGN KEY (customer_id) REFERENCES users (id),

    CONSTRAINT chk_eurders_shipping_street
        CHECK (TRIM(shipping_street) <> ''),
    CONSTRAINT chk_eurders_shipping_house_number
        CHECK (TRIM(shipping_house_number) <> ''),
    CONSTRAINT chk_eurders_shipping_postal_code
        CHECK (TRIM(shipping_postal_code) <> ''),
    CONSTRAINT chk_eurders_shipping_city
        CHECK (TRIM(shipping_city) <> '')
);


CREATE TABLE item_groups
(
    id            VARCHAR(255)   NOT NULL,
    eurder_id     VARCHAR(255)   NOT NULL,
    item_id       VARCHAR(255)   NOT NULL,

    -- Kopie van de naam en prijs op het moment van bestellen.
    item_name     VARCHAR(200)   NOT NULL,
    unit_price    NUMERIC(12, 2) NOT NULL,
    amount        INTEGER        NOT NULL,
    shipping_date DATE           NOT NULL,

    CONSTRAINT pk_item_groups PRIMARY KEY (id),

    CONSTRAINT fk_item_groups_eurder
        FOREIGN KEY (eurder_id) REFERENCES eurders (id),

    CONSTRAINT fk_item_groups_item
        FOREIGN KEY (item_id) REFERENCES items (id),

    CONSTRAINT chk_item_groups_item_name
        CHECK (TRIM(item_name) <> ''),
    CONSTRAINT chk_item_groups_unit_price
        CHECK (unit_price > 0),
    CONSTRAINT chk_item_groups_amount
        CHECK (amount > 0)
);


-- Bestellingen van een bepaalde klant ophalen.
CREATE INDEX idx_eurders_customer_id
    ON eurders (customer_id);

-- ItemGroups van een bestelling ophalen.
CREATE INDEX idx_item_groups_eurder_id
    ON item_groups (eurder_id);

-- Verwijzingen naar een item opzoeken.
CREATE INDEX idx_item_groups_item_id
    ON item_groups (item_id);

-- Story 9: ItemGroups vinden die vandaag verzonden worden.
CREATE INDEX idx_item_groups_shipping_date
    ON item_groups (shipping_date);

-- Story 10: voorraad sorteren en filteren op hoeveelheid.
CREATE INDEX idx_items_amount
    ON items (amount);