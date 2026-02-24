--liquibase formatted sql

--changeset myrafeeq:003-create-user-preferences-table

CREATE TABLE user_preferences
(
    id                    UUID PRIMARY KEY                  DEFAULT gen_random_uuid(),
    telegram_id           BIGINT                   NOT NULL UNIQUE,
    city_id               VARCHAR(100),
    latitude              DOUBLE PRECISION,
    longitude             DOUBLE PRECISION,
    calculation_method    VARCHAR(20),
    madhab                VARCHAR(20),
    high_latitude_rule    VARCHAR(20),
    hijri_correction      INTEGER                  NOT NULL DEFAULT 0,
    time_format           VARCHAR(20),
    theme                 VARCHAR(10),
    notifications_enabled BOOLEAN                  NOT NULL DEFAULT TRUE,
    reminder_timing       VARCHAR(20),
    prayer_notifications  JSONB,
    manual_adjustments    JSONB,
    version               INTEGER                  NOT NULL DEFAULT 0,
    created_at            TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at            TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_preferences_user FOREIGN KEY (telegram_id) REFERENCES users (telegram_id),
    CONSTRAINT fk_preferences_city FOREIGN KEY (city_id) REFERENCES cities (id),
    CONSTRAINT chk_hijri_correction CHECK (hijri_correction BETWEEN -2 AND 2)
);

CREATE INDEX idx_preferences_telegram_id ON user_preferences (telegram_id);

COMMENT
ON TABLE user_preferences IS 'User prayer and app preferences';
COMMENT
ON COLUMN user_preferences.telegram_id IS 'Foreign key to users table';
COMMENT
ON COLUMN user_preferences.city_id IS 'Selected city for prayer calculations';
COMMENT
ON COLUMN user_preferences.calculation_method IS 'Prayer time calculation method';
COMMENT
ON COLUMN user_preferences.madhab IS 'Juristic method for Asr (STANDARD or HANAFI)';
COMMENT
ON COLUMN user_preferences.high_latitude_rule IS 'Adjustment rule for high latitudes';
COMMENT
ON COLUMN user_preferences.hijri_correction IS 'Manual Hijri date correction (-2 to +2)';
COMMENT
ON COLUMN user_preferences.time_format IS 'Time display format (TWELVE_HOUR or TWENTY_FOUR_HOUR)';
COMMENT
ON COLUMN user_preferences.theme IS 'UI theme preference';
COMMENT
ON COLUMN user_preferences.notifications_enabled IS 'Whether push notifications are enabled';
COMMENT
ON COLUMN user_preferences.reminder_timing IS 'When to send prayer reminders';
COMMENT
ON COLUMN user_preferences.prayer_notifications IS 'Per-prayer notification toggle (JSON object)';
COMMENT
ON COLUMN user_preferences.manual_adjustments IS 'Per-prayer time adjustments in minutes (JSON object)';

--rollback DROP TABLE user_preferences CASCADE;
