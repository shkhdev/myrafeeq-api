--liquibase formatted sql

--changeset myrafeeq:003-create-cities-table

CREATE
EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE cities
(
    id           VARCHAR(100)     NOT NULL PRIMARY KEY,
    name         VARCHAR(255)     NOT NULL,
    country_code VARCHAR(2)       NOT NULL,
    latitude     DOUBLE PRECISION NOT NULL,
    longitude    DOUBLE PRECISION NOT NULL,
    timezone     VARCHAR(100)     NOT NULL,

    CONSTRAINT fk_cities_country FOREIGN KEY (country_code) REFERENCES countries (code)
);

CREATE INDEX idx_cities_country_code ON cities (country_code);
CREATE INDEX idx_cities_name ON cities (LOWER(name));
CREATE INDEX idx_cities_name_trgm ON cities USING gin (LOWER (name) gin_trgm_ops);

COMMENT
ON TABLE cities IS 'Pre-configured cities with coordinates, timezone, and localized names';
COMMENT
ON COLUMN cities.id IS 'URL-safe city identifier (e.g. tashkent, mecca)';
COMMENT
ON COLUMN cities.name IS 'City name in English';
COMMENT
ON COLUMN cities.country_code IS 'ISO 3166-1 alpha-2 country code';
COMMENT
ON COLUMN cities.latitude IS 'Geographic latitude';
COMMENT
ON COLUMN cities.longitude IS 'Geographic longitude';
COMMENT
ON COLUMN cities.timezone IS 'IANA timezone identifier';

--rollback DROP TABLE cities CASCADE;
