--liquibase formatted sql

--changeset a.slelin:004-create-table-deck

CREATE TABLE deck
(
    id                 UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    owner_id           UUID         NOT NULL,
    source_deck_id     UUID,
    title              VARCHAR(255) NOT NULL,
    description        TEXT,
    icon               VARCHAR(16),
    color              VARCHAR(16),
    source_language_id BIGINT       NOT NULL,
    target_language_id BIGINT       NOT NULL,
    is_public          BOOLEAN      NOT NULL DEFAULT FALSE,
    is_official        BOOLEAN      NOT NULL DEFAULT FALSE,
    likes_count        BIGINT       NOT NULL DEFAULT 0,
    copies_count       BIGINT       NOT NULL DEFAULT 0,
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_deck_to_user FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_deck_to_source_deck FOREIGN KEY (source_deck_id) REFERENCES deck (id) ON DELETE SET NULL,
    CONSTRAINT fk_deck_to_source_language FOREIGN KEY (source_language_id) REFERENCES language (id),
    CONSTRAINT fk_deck_to_target_language FOREIGN KEY (target_language_id) REFERENCES language (id)
);

CREATE INDEX idx_deck_owner ON deck (owner_id);
CREATE INDEX idx_deck_public ON deck (is_public);

--rollback DROP TABLE IF EXISTS deck CASCADE
