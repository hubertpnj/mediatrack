CREATE TABLE album (
    id         BIGINT PRIMARY KEY REFERENCES media(id),
    album_type VARCHAR(20) NOT NULL
);

CREATE TABLE track (
    id               BIGSERIAL PRIMARY KEY,
    album_id         BIGINT NOT NULL REFERENCES album(id),
    track_number     INTEGER NOT NULL,
    title            VARCHAR(255) NOT NULL,
    duration_seconds INTEGER,
    UNIQUE (album_id, track_number)
);
