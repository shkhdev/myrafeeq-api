--liquibase formatted sql

--changeset myrafeeq:002-create-cities-table

CREATE TABLE cities
(
    id                 VARCHAR(100)     NOT NULL PRIMARY KEY,
    name_en            VARCHAR(255)     NOT NULL,
    name_ar            VARCHAR(255)     NOT NULL,
    name_uz            VARCHAR(255)     NOT NULL,
    name_ru            VARCHAR(255)     NOT NULL,
    country_code       VARCHAR(3)       NOT NULL,
    latitude           DOUBLE PRECISION NOT NULL,
    longitude          DOUBLE PRECISION NOT NULL,
    timezone           VARCHAR(100)     NOT NULL,
    recommended_method VARCHAR(20),
    population         INTEGER
);

CREATE INDEX idx_cities_country_code ON cities (country_code);
CREATE INDEX idx_cities_name_en ON cities (LOWER(name_en));

COMMENT
ON TABLE cities IS 'Pre-configured cities with coordinates, timezone, and localized names';
COMMENT
ON COLUMN cities.id IS 'URL-safe city identifier (e.g. tashkent, mecca)';
COMMENT
ON COLUMN cities.name_en IS 'City name in English';
COMMENT
ON COLUMN cities.name_ar IS 'City name in Arabic';
COMMENT
ON COLUMN cities.name_uz IS 'City name in Uzbek';
COMMENT
ON COLUMN cities.name_ru IS 'City name in Russian';
COMMENT
ON COLUMN cities.country_code IS 'ISO 3166-1 alpha-2 country code';
COMMENT
ON COLUMN cities.latitude IS 'Geographic latitude';
COMMENT
ON COLUMN cities.longitude IS 'Geographic longitude';
COMMENT
ON COLUMN cities.timezone IS 'IANA timezone identifier';
COMMENT
ON COLUMN cities.recommended_method IS 'Recommended prayer calculation method for this city';
COMMENT
ON COLUMN cities.population IS 'Approximate population for search ranking';

--rollback DROP TABLE cities CASCADE;
