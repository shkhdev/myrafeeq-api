--liquibase formatted sql

--changeset myrafeeq:004-create-prayer-tracking-table

CREATE TABLE prayer_tracking
(
    id          UUID PRIMARY KEY                  DEFAULT gen_random_uuid(),
    telegram_id BIGINT                   NOT NULL,
    date        DATE                     NOT NULL,
    prayer_name VARCHAR(10)              NOT NULL,
    prayed      BOOLEAN                  NOT NULL DEFAULT FALSE,
    toggled_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_tracking_user FOREIGN KEY (telegram_id) REFERENCES users (telegram_id),
    CONSTRAINT uq_tracking_user_date_prayer UNIQUE (telegram_id, date, prayer_name)
);

CREATE INDEX idx_tracking_telegram_id ON prayer_tracking (telegram_id);
CREATE INDEX idx_tracking_telegram_date ON prayer_tracking (telegram_id, date);

COMMENT
ON TABLE prayer_tracking IS 'Daily prayer completion tracking';
COMMENT
ON COLUMN prayer_tracking.telegram_id IS 'Foreign key to users table';
COMMENT
ON COLUMN prayer_tracking.date IS 'Date of the prayer';
COMMENT
ON COLUMN prayer_tracking.prayer_name IS 'Prayer name (FAJR, DHUHR, ASR, MAGHRIB, ISHA)';
COMMENT
ON COLUMN prayer_tracking.prayed IS 'Whether the prayer was performed';
COMMENT
ON COLUMN prayer_tracking.toggled_at IS 'Timestamp when the status was last toggled';
COMMENT
ON COLUMN prayer_tracking.created_at IS 'Timestamp (UTC) when record was created';

--rollback DROP TABLE prayer_tracking CASCADE;
