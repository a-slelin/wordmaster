--liquibase formatted sql

--changeset a.slelin:008-create-table-card

CREATE TABLE card
(
    id               UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    deck_id          UUID         NOT NULL,
    word             VARCHAR(255) NOT NULL,
    translation      VARCHAR(255) NOT NULL,
    transcription    VARCHAR(255),
    example_sentence TEXT,
    image_url        VARCHAR(1024),
    audio_url        VARCHAR(1024),
    position         BIGINT       NOT NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_card_to_deck FOREIGN KEY (deck_id) REFERENCES deck (id) ON DELETE CASCADE
);

CREATE INDEX idx_card_deck ON card (deck_id, position);

--rollback DROP TABLE IF EXISTS card CASCADE
