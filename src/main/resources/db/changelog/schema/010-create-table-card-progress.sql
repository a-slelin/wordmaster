--liquibase formatted sql

--changeset a.slelin:010-create-table-card-progress

CREATE TABLE card_progress
(
    id               UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    user_id          UUID        NOT NULL,
    card_id          UUID        NOT NULL,
    correct_count    BIGINT      NOT NULL DEFAULT 0,
    incorrect_count  BIGINT      NOT NULL DEFAULT 0,
    status           VARCHAR(15) NOT NULL,
    last_reviewed_at TIMESTAMP,
    next_review_at   TIMESTAMP,
    ease_factor      FLOAT       NOT NULL DEFAULT 2.5,
    interval_days    INT         NOT NULL DEFAULT 0,
    repetitions      BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT uq_card_progress_user_card UNIQUE (user_id, card_id),
    CONSTRAINT fk_card_progress_to_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_card_progress_to_card FOREIGN KEY (card_id) REFERENCES card (id) ON DELETE CASCADE
);

CREATE INDEX idx_card_progress_next_review ON card_progress (user_id, next_review_at);

--rollback DROP TABLE IF EXISTS card_progress CASCADE
