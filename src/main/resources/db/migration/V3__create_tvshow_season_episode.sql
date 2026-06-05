CREATE TABLE tv_show (
    id BIGINT PRIMARY KEY REFERENCES media(id)
);

CREATE TABLE season (
    id             BIGSERIAL PRIMARY KEY,
    show_id        BIGINT NOT NULL REFERENCES tv_show(id),
    season_number  INTEGER NOT NULL,
    title          VARCHAR(255),
    release_date   DATE,
    UNIQUE (show_id, season_number)
);

CREATE TABLE episode (
    id              BIGSERIAL PRIMARY KEY,
    season_id       BIGINT NOT NULL REFERENCES season(id),
    episode_number  INTEGER NOT NULL,
    title           VARCHAR(255) NOT NULL,
    duration_minutes INTEGER,
    release_date    DATE,
    UNIQUE (season_id, episode_number)
);
