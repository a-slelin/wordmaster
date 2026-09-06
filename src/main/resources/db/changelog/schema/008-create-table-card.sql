--liquibase formatted sql

--changeset a.slelin:008-create-table-card

DROP TABLE IF EXISTS card CASCADE;

CREATE TABLE card
(
    id               UUID PRIMARY KEY,
    deck_id          UUID         NOT NULL,
    word             VARCHAR(255) NOT NULL,
    translation      VARCHAR(255) NOT NULL,
    transcription    VARCHAR(255),
    example_sentence TEXT,
    image_url        VARCHAR(255),
    audio_url        VARCHAR(255),
    position         BIGINT       NOT NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_card_to_deck FOREIGN KEY (deck_id) REFERENCES deck (id)
);

--rollback DROP TABLE IF EXISTS card CASCADE