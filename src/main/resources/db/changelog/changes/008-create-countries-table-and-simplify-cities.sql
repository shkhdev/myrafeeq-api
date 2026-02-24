--liquibase formatted sql

--changeset myrafeeq:008-create-countries-table-and-simplify-cities

-- Create countries table
CREATE TABLE countries
(
    code           VARCHAR(2)   NOT NULL PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    default_method VARCHAR(20),
    default_madhab VARCHAR(20)
);

COMMENT
ON TABLE countries IS 'Countries with default prayer calculation settings';
COMMENT
ON COLUMN countries.code IS 'ISO 3166-1 alpha-2 country code';
COMMENT
ON COLUMN countries.name IS 'Country name in English';
COMMENT
ON COLUMN countries.default_method IS 'Default prayer calculation method for this country';
COMMENT
ON COLUMN countries.default_madhab IS 'Default madhab for this country';

-- Seed countries
INSERT INTO countries (code, name, default_method, default_madhab)
VALUES ('UZ', 'Uzbekistan', 'MBOUZ', 'HANAFI');

-- Alter cities.country_code to VARCHAR(2) to match countries PK
ALTER TABLE cities ALTER COLUMN country_code TYPE VARCHAR(2);

-- Add FK from cities to countries
ALTER TABLE cities
    ADD CONSTRAINT fk_cities_country FOREIGN KEY (country_code) REFERENCES countries (code);

-- Drop localized name columns, recommended_method, and population
ALTER TABLE cities DROP COLUMN name_ar;
ALTER TABLE cities DROP COLUMN name_uz;
ALTER TABLE cities DROP COLUMN name_ru;
ALTER TABLE cities DROP COLUMN recommended_method;
ALTER TABLE cities DROP COLUMN population;

-- Rename name_en to name
ALTER TABLE cities RENAME COLUMN name_en TO name;

-- Drop old index on name_en, create new one on name
DROP INDEX IF EXISTS idx_cities_name_en;
CREATE INDEX idx_cities_name ON cities (LOWER(name));

--rollback ALTER TABLE cities RENAME COLUMN name TO name_en;
--rollback ALTER TABLE cities ADD COLUMN name_ar VARCHAR(255);
--rollback ALTER TABLE cities ADD COLUMN name_uz VARCHAR(255);
--rollback ALTER TABLE cities ADD COLUMN name_ru VARCHAR(255);
--rollback ALTER TABLE cities ADD COLUMN recommended_method VARCHAR(20);
--rollback ALTER TABLE cities ADD COLUMN population INTEGER;
--rollback ALTER TABLE cities DROP CONSTRAINT fk_cities_country;
--rollback DROP INDEX IF EXISTS idx_cities_name;
--rollback CREATE INDEX idx_cities_name_en ON cities (LOWER(name_en));
--rollback DROP TABLE countries;
