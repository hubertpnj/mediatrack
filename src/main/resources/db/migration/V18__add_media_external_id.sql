CREATE TABLE media_external_id (
    id          BIGSERIAL PRIMARY KEY,
    media_id    BIGINT       NOT NULL REFERENCES media (id) ON DELETE CASCADE,
    source      VARCHAR(50)  NOT NULL,
    external_id VARCHAR(255) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_source_external_id UNIQUE (source, external_id)
);

CREATE INDEX idx_media_external_id_media_id ON media_external_id (media_id);

INSERT INTO media_external_id (media_id, source, external_id)
SELECT id, 'TMDB', tmdb_id::TEXT FROM movie WHERE tmdb_id IS NOT NULL;

INSERT INTO media_external_id (media_id, source, external_id)
SELECT id, 'TMDB', tmdb_id::TEXT FROM tv_show WHERE tmdb_id IS NOT NULL;

ALTER TABLE movie DROP COLUMN tmdb_id;
ALTER TABLE tv_show DROP COLUMN tmdb_id;
