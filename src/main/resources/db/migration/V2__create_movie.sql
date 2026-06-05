CREATE TABLE movie (
    id               BIGINT PRIMARY KEY REFERENCES media(id),
    duration_minutes INTEGER
);
