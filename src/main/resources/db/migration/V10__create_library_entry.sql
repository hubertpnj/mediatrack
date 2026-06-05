CREATE TABLE library_entry (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES user_account(id),
    media_id   BIGINT NOT NULL REFERENCES media(id),
    status     VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    completed  BOOLEAN NOT NULL DEFAULT FALSE,
    added_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, media_id)
);
