--liquibase formatted sql

--changeset a.slelin:012-create-table-training-session-card

CREATE TABLE training_session_card
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID    NOT NULL,
    card_id    UUID    NOT NULL,
    position   BIGINT  NOT NULL,
    done       BOOLEAN NOT NULL DEFAULT FALSE,
    attempts   INT     NOT NULL DEFAULT 0,
    CONSTRAINT uq_training_session_card UNIQUE (session_id, card_id),
    CONSTRAINT fk_training_session_card_to_session FOREIGN KEY (session_id) REFERENCES training_session (id) ON DELETE CASCADE,
    CONSTRAINT fk_training_session_card_to_card FOREIGN KEY (card_id) REFERENCES card (id) ON DELETE CASCADE
);

--rollback DROP TABLE IF EXISTS training_session_card CASCADE
