--liquibase formatted sql

--changeset a.slelin:005-create-table-training-session

CREATE TABLE training_session
(
    id            UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    user_id       UUID        NOT NULL,
    deck_id       UUID        NOT NULL,
    mode          VARCHAR(15) NOT NULL,
    direction     VARCHAR(15) NOT NULL,
    started_at    TIMESTAMP   NOT NULL,
    finished_at   TIMESTAMP,
    cards_total   BIGINT      NOT NULL,
    cards_correct BIGINT,
    answers_total BIGINT      NOT NULL DEFAULT 0,
    xp_earned     BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT fk_training_session_to_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_training_session_to_deck FOREIGN KEY (deck_id) REFERENCES deck (id) ON DELETE CASCADE
);

CREATE INDEX idx_training_session_user ON training_session (user_id);

--rollback DROP TABLE IF EXISTS training_session CASCADE
