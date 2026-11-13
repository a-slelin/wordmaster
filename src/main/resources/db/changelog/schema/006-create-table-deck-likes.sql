--liquibase formatted sql

--changeset a.slelin:006-create-table-deck-likes

CREATE TABLE deck_likes
(
    user_id    UUID      NOT NULL,
    deck_id    UUID      NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, deck_id),
    CONSTRAINT fk_deck_likes_to_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_deck_likes_to_deck FOREIGN KEY (deck_id) REFERENCES deck (id) ON DELETE CASCADE
);

--rollback DROP TABLE IF EXISTS deck_likes CASCADE
