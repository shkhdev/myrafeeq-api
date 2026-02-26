--liquibase formatted sql

--changeset myrafeeq:010-add-version-columns

ALTER TABLE users
    ADD COLUMN version INTEGER NOT NULL DEFAULT 0;
ALTER TABLE user_preferences
    ADD COLUMN version INTEGER NOT NULL DEFAULT 0;
ALTER TABLE prayer_tracking
    ADD COLUMN version INTEGER NOT NULL DEFAULT 0;

--rollback ALTER TABLE users DROP COLUMN version;
--rollback ALTER TABLE user_preferences DROP COLUMN version;
--rollback ALTER TABLE prayer_tracking DROP COLUMN version;
