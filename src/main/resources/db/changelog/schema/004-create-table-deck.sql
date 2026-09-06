--liquibase formatted sql

--changeset a.slelin:004-create-table-deck

DROP TABLE IF EXISTS deck CASCADE;

CREATE TABLE deck
(
    id                 UUID PRIMARY KEY,
    owner_id           UUID         NOT NULL,
    source_deck_id     UUID,
    title              VARCHAR(255) NOT NULL,
    description        TEXT,
    source_language_id BIGINT       NOT NULL,
    target_language_id BIGINT       NOT NULL,
    is_public          BOOLEAN      NOT NULL,
    likes_count        BIGINT       NOT NULL,
    copies_count       BIGINT       NOT NULL,
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_deck_to_user FOREIGN KEY (owner_id) REFERENCES users (id),
    CONSTRAINT fk_deck_to_source_deck FOREIGN KEY (source_deck_id) REFERENCES deck (id),
    CONSTRAINT fk_deck_to_source_language FOREIGN KEY (source_language_id) REFERENCES language (id),
    CONSTRAINT fk_deck_to_target_language FOREIGN KEY (target_language_id) REFERENCES language (id)
);

--rollback DROP TABLE IF EXISTS deck CASCADE