CREATE TABLE media_group (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    group_type  VARCHAR(20) NOT NULL,
    description TEXT
);

CREATE TABLE media_group_item (
    id       BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL REFERENCES media_group(id),
    media_id BIGINT NOT NULL REFERENCES media(id),
    position INTEGER NOT NULL,
    UNIQUE (group_id, media_id)
);
