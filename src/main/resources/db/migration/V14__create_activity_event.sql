CREATE TABLE activity_event (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES user_account(id),
    event_type  VARCHAR(100) NOT NULL,
    media_id    BIGINT REFERENCES media(id),
    entry_id    BIGINT REFERENCES library_entry(id),
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
