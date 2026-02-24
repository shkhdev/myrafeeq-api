--liquibase formatted sql

--changeset myrafeeq:009-drop-version-columns

ALTER TABLE users DROP COLUMN version;
ALTER TABLE user_preferences DROP COLUMN version;

--rollback ALTER TABLE users ADD COLUMN version INTEGER NOT NULL DEFAULT 0;
--rollback ALTER TABLE user_preferences ADD COLUMN version INTEGER NOT NULL DEFAULT 0;
