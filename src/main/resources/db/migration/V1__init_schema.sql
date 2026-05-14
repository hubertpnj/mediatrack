-- =============================================================
-- V1 – Initial schema for Mediatrack
-- =============================================================

-- ----------------------------------------------------------------
-- MEDIA hierarchy (JOINED inheritance)
-- ----------------------------------------------------------------

CREATE TABLE media (
    id              UUID         NOT NULL,
    dtype           VARCHAR(20)  NOT NULL,
    title           VARCHAR(255) NOT NULL,
    release_year    INT,
    description     TEXT,
    cover_image_url VARCHAR(500),
    average_rating  NUMERIC(3, 2),
    rating_count    INT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_media PRIMARY KEY (id)
);

CREATE TABLE movie (
    id                UUID        NOT NULL,
    duration          INT,
    original_language VARCHAR(10),
    CONSTRAINT pk_movie    PRIMARY KEY (id),
    CONSTRAINT fk_movie_media FOREIGN KEY (id) REFERENCES media (id)
);

CREATE TABLE game (
    id          UUID        NOT NULL,
    esrb_rating VARCHAR(10),
    CONSTRAINT pk_game      PRIMARY KEY (id),
    CONSTRAINT fk_game_media FOREIGN KEY (id) REFERENCES media (id)
);

CREATE TABLE game_platform (
    game_id  UUID        NOT NULL,
    platform VARCHAR(50) NOT NULL,
    CONSTRAINT pk_game_platform PRIMARY KEY (game_id, platform),
    CONSTRAINT fk_game_platform_game FOREIGN KEY (game_id) REFERENCES game (id) ON DELETE CASCADE
);

CREATE TABLE book (
    id         UUID        NOT NULL,
    isbn       VARCHAR(20),
    page_count INT,
    language   VARCHAR(10),
    CONSTRAINT pk_book      PRIMARY KEY (id),
    CONSTRAINT fk_book_media FOREIGN KEY (id) REFERENCES media (id)
);

CREATE TABLE album (
    id          UUID NOT NULL,
    track_count INT,
    duration    INT,
    CONSTRAINT pk_album      PRIMARY KEY (id),
    CONSTRAINT fk_album_media FOREIGN KEY (id) REFERENCES media (id)
);

-- ----------------------------------------------------------------
-- PARTY hierarchy (JOINED inheritance)
-- ----------------------------------------------------------------

CREATE TABLE party (
    id         UUID         NOT NULL,
    dtype      VARCHAR(20)  NOT NULL,
    name       VARCHAR(255) NOT NULL,
    image_url  VARCHAR(500),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_party PRIMARY KEY (id)
);

CREATE TABLE person (
    id            UUID NOT NULL,
    birth_date    DATE,
    death_date    DATE,
    birth_country VARCHAR(100),
    CONSTRAINT pk_person      PRIMARY KEY (id),
    CONSTRAINT fk_person_party FOREIGN KEY (id) REFERENCES party (id)
);

CREATE TABLE organization (
    id           UUID        NOT NULL,
    founded_year INT,
    country      VARCHAR(100),
    website      VARCHAR(500),
    CONSTRAINT pk_organization      PRIMARY KEY (id),
    CONSTRAINT fk_organization_party FOREIGN KEY (id) REFERENCES party (id)
);

-- ----------------------------------------------------------------
-- Junction: Party ↔ Party (membership)
-- ----------------------------------------------------------------

CREATE TABLE party_membership (
    id              UUID         NOT NULL,
    person_id       UUID         NOT NULL,
    organization_id UUID         NOT NULL,
    role            VARCHAR(100),
    start_date      DATE,
    end_date        DATE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_party_membership           PRIMARY KEY (id),
    CONSTRAINT fk_membership_person          FOREIGN KEY (person_id)       REFERENCES person (id),
    CONSTRAINT fk_membership_organization    FOREIGN KEY (organization_id) REFERENCES organization (id)
);

-- ----------------------------------------------------------------
-- Junction: Party ↔ Media (contribution)
-- ----------------------------------------------------------------

CREATE TABLE contribution (
    id           UUID        NOT NULL,
    party_id     UUID        NOT NULL,
    media_id     UUID        NOT NULL,
    role         VARCHAR(50) NOT NULL,
    credit_order INT         NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_contribution        PRIMARY KEY (id),
    CONSTRAINT fk_contribution_party  FOREIGN KEY (party_id) REFERENCES party (id),
    CONSTRAINT fk_contribution_media  FOREIGN KEY (media_id) REFERENCES media (id)
);

-- ----------------------------------------------------------------
-- User domain
-- ----------------------------------------------------------------

CREATE TABLE user_account (
    id            UUID         NOT NULL,
    username      VARCHAR(50)  NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    user_role     VARCHAR(50)  NOT NULL DEFAULT 'ROLE_USER',
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_user_account   PRIMARY KEY (id),
    CONSTRAINT uq_user_username  UNIQUE (username),
    CONSTRAINT uq_user_email     UNIQUE (email)
);

CREATE TABLE library_entry (
    id           UUID        NOT NULL,
    user_id      UUID        NOT NULL,
    media_id     UUID        NOT NULL,
    status       VARCHAR(20),
    rating       SMALLINT,
    review       TEXT,
    started_at   TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_library_entry          PRIMARY KEY (id),
    CONSTRAINT uq_library_entry_user_media UNIQUE (user_id, media_id),
    CONSTRAINT fk_library_entry_user     FOREIGN KEY (user_id)  REFERENCES user_account (id),
    CONSTRAINT fk_library_entry_media    FOREIGN KEY (media_id) REFERENCES media (id),
    CONSTRAINT chk_library_entry_rating  CHECK (rating BETWEEN 1 AND 10)
);

CREATE TABLE user_list (
    id          UUID         NOT NULL,
    user_id     UUID         NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    is_public   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_user_list        PRIMARY KEY (id),
    CONSTRAINT fk_user_list_user   FOREIGN KEY (user_id) REFERENCES user_account (id)
);

CREATE TABLE user_list_item (
    list_id    UUID        NOT NULL,
    media_id   UUID        NOT NULL,
    sort_order INT         NOT NULL DEFAULT 0,
    added_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_user_list_item        PRIMARY KEY (list_id, media_id),
    CONSTRAINT fk_user_list_item_list   FOREIGN KEY (list_id)  REFERENCES user_list (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_list_item_media  FOREIGN KEY (media_id) REFERENCES media (id)
);

CREATE TABLE activity_event (
    id          UUID        NOT NULL,
    user_id     UUID        NOT NULL,
    media_id    UUID,
    event_type  VARCHAR(50) NOT NULL,
    payload     JSONB,
    occurred_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT pk_activity_event       PRIMARY KEY (id),
    CONSTRAINT fk_activity_event_user  FOREIGN KEY (user_id)  REFERENCES user_account (id),
    CONSTRAINT fk_activity_event_media FOREIGN KEY (media_id) REFERENCES media (id)
);

-- ----------------------------------------------------------------
-- Media grouping
-- ----------------------------------------------------------------

CREATE TABLE media_group (
    id          UUID        NOT NULL,
    name        VARCHAR(255) NOT NULL,
    group_type  VARCHAR(20) NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_media_group PRIMARY KEY (id)
);

CREATE TABLE media_group_item (
    group_id   UUID        NOT NULL,
    media_id   UUID        NOT NULL,
    sort_order INT         NOT NULL DEFAULT 0,
    added_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_media_group_item         PRIMARY KEY (group_id, media_id),
    CONSTRAINT fk_media_group_item_group   FOREIGN KEY (group_id) REFERENCES media_group (id) ON DELETE CASCADE,
    CONSTRAINT fk_media_group_item_media   FOREIGN KEY (media_id) REFERENCES media (id)
);

-- ----------------------------------------------------------------
-- Indexes for common query patterns
-- ----------------------------------------------------------------

CREATE INDEX idx_media_dtype        ON media (dtype);
CREATE INDEX idx_media_release_year ON media (release_year);
CREATE INDEX idx_party_dtype        ON party (dtype);
CREATE INDEX idx_contribution_media ON contribution (media_id);
CREATE INDEX idx_contribution_party ON contribution (party_id);
CREATE INDEX idx_library_entry_user ON library_entry (user_id);
CREATE INDEX idx_library_entry_status ON library_entry (status);
CREATE INDEX idx_activity_event_user ON activity_event (user_id);
CREATE INDEX idx_activity_event_occurred ON activity_event (occurred_at DESC);
