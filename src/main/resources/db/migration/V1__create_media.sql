CREATE TABLE media (
    id           BIGSERIAL PRIMARY KEY,
    dtype        VARCHAR(31) NOT NULL,
    title        VARCHAR(255) NOT NULL,
    release_date DATE
);
