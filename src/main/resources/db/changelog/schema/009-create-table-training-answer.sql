--liquibase formatted sql

--changeset a.slelin:009-create-table-training-answer

DROP TABLE IF EXISTS training_answer CASCADE;

CREATE TABLE training_answer
(
    id          UUID PRIMARY KEY,
    session_id  UUID      NOT NULL,
    card_id     UUID      NOT NULL,
    is_correct  BOOLEAN   NOT NULL,
    answered_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_training_answer_to_training_session FOREIGN KEY (session_id) REFERENCES training_session (id),
    CONSTRAINT fk_training_answer_to_card FOREIGN KEY (card_id) REFERENCES card (id)
);

--rollback DROP TABLE IF EXISTS training_answer CASCADE