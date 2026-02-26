--liquibase formatted sql

--changeset myrafeeq:002-create-countries-table

CREATE TABLE countries
(
    code           VARCHAR(2)   NOT NULL PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    default_method VARCHAR(20),
    default_madhab VARCHAR(20)
);

COMMENT ON TABLE countries IS 'Countries with default prayer calculation settings';
COMMENT ON COLUMN countries.code IS 'ISO 3166-1 alpha-2 country code';
COMMENT ON COLUMN countries.name IS 'Country name in English';
COMMENT ON COLUMN countries.default_method IS 'Default prayer calculation method for this country';
COMMENT ON COLUMN countries.default_madhab IS 'Default madhab for this country';

--rollback DROP TABLE countries CASCADE;
