--liquibase formatted sql

--changeset a.slelin:010-create-table-card-progress

DROP TABLE IF EXISTS card_progress CASCADE;

CREATE TABLE card_progress
(
    id               UUID PRIMARY KEY,
    user_id          UUID        NOT NULL,
    card_id          UUID        NOT NULL,
    correct_count    BIGINT      NOT NULL,
    incorrect_count  BIGINT      NOT NULL,
    status           VARCHAR(15) NOT NULL,
    last_reviewed_at TIMESTAMP   NOT NULL,
    next_review_at   TIMESTAMP   NOT NULL,
    ease_factor      FLOAT       NOT NULL,
    interval_days    INT         NOT NULL,
    repetitions      BIGINT      NOT NULL,
    CONSTRAINT fk_card_progress_to_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_card_progress_to_card FOREIGN KEY (card_id) REFERENCES card (id)
);

--rollback DROP TABLE IF EXISTS card_progress CASCADE