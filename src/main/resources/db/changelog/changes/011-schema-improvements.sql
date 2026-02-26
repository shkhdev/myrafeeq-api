--liquibase formatted sql

--changeset myrafeeq:011-schema-improvements

-- 1. Drop redundant indexes
-- idx_preferences_telegram_id duplicates the UNIQUE constraint index on telegram_id
DROP INDEX idx_preferences_telegram_id;
-- idx_tracking_telegram_id is a prefix of idx_tracking_telegram_date (telegram_id, date)
DROP INDEX idx_tracking_telegram_id;

-- 2. Rename prayer_tracking.date to prayer_date (reserved SQL keyword)
ALTER TABLE prayer_tracking RENAME COLUMN date TO prayer_date;

-- 3. Change user_preferences PK from surrogate UUID to telegram_id (1:1 with users)
ALTER TABLE user_preferences DROP CONSTRAINT user_preferences_pkey;
ALTER TABLE user_preferences DROP COLUMN id;
ALTER TABLE user_preferences DROP CONSTRAINT user_preferences_telegram_id_key;
ALTER TABLE user_preferences ADD PRIMARY KEY (telegram_id);

-- 4. Add index on user_preferences.city_id FK
CREATE INDEX idx_preferences_city_id ON user_preferences (city_id);

-- 5. Add CHECK constraints on enum fields
ALTER TABLE prayer_tracking
    ADD CONSTRAINT chk_prayer_name
        CHECK (prayer_name IN ('FAJR', 'DHUHR', 'ASR', 'MAGHRIB', 'ISHA'));

ALTER TABLE user_preferences
    ADD CONSTRAINT chk_calculation_method
        CHECK (calculation_method IS NULL OR
               calculation_method IN ('MWL', 'ISNA', 'EGYPT', 'KARACHI', 'UMM_AL_QURA',
                                      'DUBAI', 'QATAR', 'KUWAIT', 'SINGAPORE', 'MBOUZ'));

ALTER TABLE user_preferences
    ADD CONSTRAINT chk_madhab
        CHECK (madhab IS NULL OR madhab IN ('SHAFI', 'HANAFI'));

ALTER TABLE user_preferences
    ADD CONSTRAINT chk_high_latitude_rule
        CHECK (high_latitude_rule IS NULL OR
               high_latitude_rule IN ('MIDDLE_OF_NIGHT', 'ONE_SEVENTH', 'ANGLE_BASED'));

ALTER TABLE user_preferences
    ADD CONSTRAINT chk_time_format
        CHECK (time_format IS NULL OR time_format IN ('TWELVE_HOUR', 'TWENTY_FOUR_HOUR'));

ALTER TABLE user_preferences
    ADD CONSTRAINT chk_theme
        CHECK (theme IS NULL OR theme IN ('LIGHT', 'DARK', 'SYSTEM'));

ALTER TABLE user_preferences
    ADD CONSTRAINT chk_reminder_timing
        CHECK (reminder_timing IS NULL OR
               reminder_timing IN ('ON_TIME', 'FIVE_MIN', 'TEN_MIN', 'FIFTEEN_MIN', 'THIRTY_MIN'));

-- Update comments for renamed column
COMMENT ON COLUMN prayer_tracking.prayer_date IS 'Date of the prayer';

--rollback ALTER TABLE user_preferences DROP CONSTRAINT chk_reminder_timing;
--rollback ALTER TABLE user_preferences DROP CONSTRAINT chk_theme;
--rollback ALTER TABLE user_preferences DROP CONSTRAINT chk_time_format;
--rollback ALTER TABLE user_preferences DROP CONSTRAINT chk_high_latitude_rule;
--rollback ALTER TABLE user_preferences DROP CONSTRAINT chk_madhab;
--rollback ALTER TABLE user_preferences DROP CONSTRAINT chk_calculation_method;
--rollback ALTER TABLE prayer_tracking DROP CONSTRAINT chk_prayer_name;
--rollback DROP INDEX idx_preferences_city_id;
--rollback ALTER TABLE user_preferences DROP CONSTRAINT user_preferences_pkey;
--rollback ALTER TABLE user_preferences ADD COLUMN id UUID DEFAULT gen_random_uuid();
--rollback UPDATE user_preferences SET id = gen_random_uuid() WHERE id IS NULL;
--rollback ALTER TABLE user_preferences ALTER COLUMN id SET NOT NULL;
--rollback ALTER TABLE user_preferences ADD PRIMARY KEY (id);
--rollback ALTER TABLE user_preferences ADD CONSTRAINT user_preferences_telegram_id_key UNIQUE (telegram_id);
--rollback CREATE INDEX idx_preferences_telegram_id ON user_preferences (telegram_id);
--rollback ALTER TABLE prayer_tracking RENAME COLUMN prayer_date TO date;
--rollback CREATE INDEX idx_tracking_telegram_id ON prayer_tracking (telegram_id);
