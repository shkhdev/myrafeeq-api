--liquibase formatted sql

--changeset myrafeeq:004-create-user-preferences-table

CREATE TABLE user_preferences
(
    telegram_id           BIGINT                   NOT NULL PRIMARY KEY,
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
    CONSTRAINT chk_hijri_correction CHECK (hijri_correction BETWEEN -2 AND 2),
    CONSTRAINT chk_calculation_method CHECK (calculation_method IS NULL OR
                                             calculation_method IN ('MWL', 'ISNA', 'EGYPT', 'KARACHI', 'UMM_AL_QURA',
                                                                    'DUBAI', 'QATAR', 'KUWAIT', 'SINGAPORE', 'MBOUZ')),
    CONSTRAINT chk_madhab CHECK (madhab IS NULL OR madhab IN ('SHAFI', 'HANAFI')),
    CONSTRAINT chk_high_latitude_rule CHECK (high_latitude_rule IS NULL OR
                                             high_latitude_rule IN ('MIDDLE_OF_NIGHT', 'ONE_SEVENTH', 'ANGLE_BASED')),
    CONSTRAINT chk_time_format CHECK (time_format IS NULL OR time_format IN ('TWELVE_HOUR', 'TWENTY_FOUR_HOUR')),
    CONSTRAINT chk_theme CHECK (theme IS NULL OR theme IN ('LIGHT', 'DARK', 'SYSTEM')),
    CONSTRAINT chk_reminder_timing CHECK (reminder_timing IS NULL OR
                                          reminder_timing IN
                                          ('ON_TIME', 'FIVE_MIN', 'TEN_MIN', 'FIFTEEN_MIN', 'THIRTY_MIN'))
);

CREATE INDEX idx_preferences_city_id ON user_preferences (city_id);

COMMENT ON TABLE user_preferences IS 'User prayer and app preferences';
COMMENT ON COLUMN user_preferences.telegram_id IS 'Foreign key to users table (also primary key, 1:1 with users)';
COMMENT ON COLUMN user_preferences.city_id IS 'Selected city for prayer calculations';
COMMENT ON COLUMN user_preferences.calculation_method IS 'Prayer time calculation method';
COMMENT ON COLUMN user_preferences.madhab IS 'Juristic method for Asr (SHAFI or HANAFI)';
COMMENT ON COLUMN user_preferences.high_latitude_rule IS 'Adjustment rule for high latitudes';
COMMENT ON COLUMN user_preferences.hijri_correction IS 'Manual Hijri date correction (-2 to +2)';
COMMENT ON COLUMN user_preferences.time_format IS 'Time display format (TWELVE_HOUR or TWENTY_FOUR_HOUR)';
COMMENT ON COLUMN user_preferences.theme IS 'UI theme preference';
COMMENT ON COLUMN user_preferences.notifications_enabled IS 'Whether push notifications are enabled';
COMMENT ON COLUMN user_preferences.reminder_timing IS 'When to send prayer reminders';
COMMENT ON COLUMN user_preferences.prayer_notifications IS 'Per-prayer notification toggle (JSON object)';
COMMENT ON COLUMN user_preferences.manual_adjustments IS 'Per-prayer time adjustments in minutes (JSON object)';

--rollback DROP TABLE user_preferences CASCADE;
