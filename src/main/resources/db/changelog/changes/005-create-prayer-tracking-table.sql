--liquibase formatted sql

--changeset myrafeeq:005-create-prayer-tracking-table

CREATE TABLE prayer_tracking
(
    id          UUID PRIMARY KEY                  DEFAULT gen_random_uuid(),
    telegram_id BIGINT                   NOT NULL,
    prayer_date DATE                     NOT NULL,
    prayer_name VARCHAR(10)              NOT NULL,
    prayed      BOOLEAN                  NOT NULL DEFAULT FALSE,
    toggled_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    version     INTEGER                  NOT NULL DEFAULT 0,

    CONSTRAINT fk_tracking_user FOREIGN KEY (telegram_id) REFERENCES users (telegram_id),
    CONSTRAINT uq_tracking_user_date_prayer UNIQUE (telegram_id, prayer_date, prayer_name),
    CONSTRAINT chk_prayer_name CHECK (prayer_name IN ('FAJR', 'DHUHR', 'ASR', 'MAGHRIB', 'ISHA'))
);

CREATE INDEX idx_tracking_telegram_date ON prayer_tracking (telegram_id, prayer_date);

COMMENT
ON TABLE prayer_tracking IS 'Daily prayer completion tracking';
COMMENT
ON COLUMN prayer_tracking.telegram_id IS 'Foreign key to users table';
COMMENT
ON COLUMN prayer_tracking.prayer_date IS 'Date of the prayer';
COMMENT
ON COLUMN prayer_tracking.prayer_name IS 'Prayer name (FAJR, DHUHR, ASR, MAGHRIB, ISHA)';
COMMENT
ON COLUMN prayer_tracking.prayed IS 'Whether the prayer was performed';
COMMENT
ON COLUMN prayer_tracking.toggled_at IS 'Timestamp when the status was last toggled';
COMMENT
ON COLUMN prayer_tracking.created_at IS 'Timestamp (UTC) when record was created';

--rollback DROP TABLE prayer_tracking CASCADE;
