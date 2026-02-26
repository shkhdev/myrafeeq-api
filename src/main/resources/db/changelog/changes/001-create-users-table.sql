--liquibase formatted sql

--changeset myrafeeq:001-create-users-table

CREATE TABLE users
(
    telegram_id          BIGINT                   NOT NULL PRIMARY KEY,
    first_name           VARCHAR(255)             NOT NULL,
    username             VARCHAR(255),
    language_code        VARCHAR(10),
    onboarding_completed BOOLEAN                  NOT NULL DEFAULT FALSE,
    version              INTEGER                  NOT NULL DEFAULT 0,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at           TIMESTAMP WITH TIME ZONE NOT NULL
);

COMMENT ON TABLE users IS 'Telegram users registered via the Mini App';
COMMENT ON COLUMN users.telegram_id IS 'Telegram user ID (primary key, not auto-generated)';
COMMENT ON COLUMN users.first_name IS 'User first name from Telegram';
COMMENT ON COLUMN users.username IS 'Telegram username (optional)';
COMMENT ON COLUMN users.language_code IS 'IETF language tag from Telegram client';
COMMENT ON COLUMN users.onboarding_completed IS 'Whether the user has completed the onboarding flow';
COMMENT ON COLUMN users.version IS 'Optimistic locking version';
COMMENT ON COLUMN users.created_at IS 'Timestamp (UTC) when record was created';
COMMENT ON COLUMN users.updated_at IS 'Timestamp (UTC) when record was last updated';

--rollback DROP TABLE users CASCADE;
