CREATE TABLE book (
    id         BIGINT PRIMARY KEY REFERENCES media(id),
    isbn       VARCHAR(13),
    page_count INTEGER
);
