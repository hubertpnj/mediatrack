CREATE TABLE review (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES user_account(id),
    media_id   BIGINT NOT NULL REFERENCES media(id),
    rating     NUMERIC(3,1) CHECK (rating >= 0 AND rating <= 10 AND rating * 2 = FLOOR(rating * 2)),
    comment    TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, media_id)
);
