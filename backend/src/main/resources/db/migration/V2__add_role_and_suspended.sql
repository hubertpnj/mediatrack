-- Align user_account.user_role values with new UserRole enum (USER/MODERATOR/ADMIN)
UPDATE user_account SET user_role = 'USER'      WHERE user_role = 'ROLE_USER';
UPDATE user_account SET user_role = 'MODERATOR' WHERE user_role = 'ROLE_MODERATOR';
UPDATE user_account SET user_role = 'ADMIN'     WHERE user_role = 'ROLE_ADMIN';

ALTER TABLE user_account RENAME COLUMN user_role TO role;
ALTER TABLE user_account ALTER COLUMN role SET DEFAULT 'USER';

ALTER TABLE user_account ADD COLUMN suspended BOOLEAN NOT NULL DEFAULT FALSE;

-- Suggestion workflow
CREATE TABLE suggestion (
    id            UUID        NOT NULL,
    entity_type   VARCHAR(20) NOT NULL,
    proposed_data JSONB       NOT NULL,
    status        VARCHAR(10) NOT NULL DEFAULT 'PENDING',
    submitted_by  UUID        NOT NULL,
    reviewed_by   UUID,
    review_note   TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_suggestion              PRIMARY KEY (id),
    CONSTRAINT fk_suggestion_submitted_by FOREIGN KEY (submitted_by) REFERENCES user_account (id),
    CONSTRAINT fk_suggestion_reviewed_by  FOREIGN KEY (reviewed_by)  REFERENCES user_account (id)
);

CREATE INDEX idx_suggestion_status       ON suggestion (status);
CREATE INDEX idx_suggestion_entity_type  ON suggestion (entity_type);
CREATE INDEX idx_suggestion_submitted_by ON suggestion (submitted_by);
