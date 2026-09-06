--liquibase formatted sql

--changeset a.slelin:005-create-table-training-session

DROP TABLE IF EXISTS training_session CASCADE;

CREATE TABLE training_session
(
    id            UUID PRIMARY KEY,
    user_id       UUID      NOT NULL,
    deck_id       UUID      NOT NULL,
    started_at    TIMESTAMP NOT NULL,
    finished_at   TIMESTAMP,
    cards_total   BIGINT    NOT NULL,
    cards_correct BIGINT,
    CONSTRAINT fk_training_session_to_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_training_session_to_deck FOREIGN KEY (deck_id) REFERENCES deck (id)
);

--rollback DROP TABLE IF EXISTS training_session CASCADE