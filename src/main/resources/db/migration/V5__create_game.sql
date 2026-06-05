CREATE TABLE game (
    id BIGINT PRIMARY KEY REFERENCES media(id)
);

CREATE TABLE game_platform (
    game_id  BIGINT NOT NULL REFERENCES game(id),
    platform VARCHAR(50) NOT NULL,
    PRIMARY KEY (game_id, platform)
);
