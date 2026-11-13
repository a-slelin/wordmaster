--liquibase formatted sql

--changeset a.slelin:009-create-table-training-answer

CREATE TABLE training_answer
(
    id          UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    session_id  UUID        NOT NULL,
    card_id     UUID        NOT NULL,
    grade       VARCHAR(15) NOT NULL,
    is_correct  BOOLEAN     NOT NULL,
    user_answer VARCHAR(255),
    answered_at TIMESTAMP   NOT NULL,
    CONSTRAINT fk_training_answer_to_training_session FOREIGN KEY (session_id) REFERENCES training_session (id) ON DELETE CASCADE,
    CONSTRAINT fk_training_answer_to_card FOREIGN KEY (card_id) REFERENCES card (id) ON DELETE CASCADE
);

CREATE INDEX idx_training_answer_session ON training_answer (session_id);

--rollback DROP TABLE IF EXISTS training_answer CASCADE
