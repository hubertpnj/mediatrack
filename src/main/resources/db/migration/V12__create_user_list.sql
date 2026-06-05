CREATE TABLE user_list (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES user_account(id),
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    is_public   BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE user_list_item (
    id       BIGSERIAL PRIMARY KEY,
    list_id  BIGINT NOT NULL REFERENCES user_list(id),
    media_id BIGINT NOT NULL REFERENCES media(id),
    position INTEGER NOT NULL,
    added_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (list_id, media_id)
);
