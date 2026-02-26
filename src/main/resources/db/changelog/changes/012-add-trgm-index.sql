--liquibase formatted sql

--changeset myrafeeq:012-add-trgm-index
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_cities_name_trgm ON cities USING gin (LOWER(name) gin_trgm_ops);
