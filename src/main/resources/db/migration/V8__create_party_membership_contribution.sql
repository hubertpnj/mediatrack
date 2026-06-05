CREATE TABLE party_membership (
    id              BIGSERIAL PRIMARY KEY,
    person_id       BIGINT NOT NULL REFERENCES person(id),
    organization_id BIGINT NOT NULL REFERENCES organization(id),
    role            VARCHAR(255) NOT NULL,
    start_date      DATE,
    end_date        DATE
);

CREATE TABLE contribution (
    id            BIGSERIAL PRIMARY KEY,
    party_id      BIGINT NOT NULL REFERENCES party(id),
    media_id      BIGINT NOT NULL REFERENCES media(id),
    role          VARCHAR(50) NOT NULL,
    display_order INTEGER
);
