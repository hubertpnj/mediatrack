CREATE TABLE party (
    id    BIGSERIAL PRIMARY KEY,
    dtype VARCHAR(31) NOT NULL
);

CREATE TABLE person (
    id         BIGINT PRIMARY KEY REFERENCES party(id),
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL,
    birth_date DATE
);

CREATE TABLE organization (
    id           BIGINT PRIMARY KEY REFERENCES party(id),
    name         VARCHAR(255) NOT NULL,
    founded_year INTEGER
);
