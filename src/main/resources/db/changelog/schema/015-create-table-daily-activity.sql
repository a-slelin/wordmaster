--liquibase formatted sql

--changeset a.slelin:015-create-table-daily-activity

CREATE TABLE daily_activity
(
    user_id        UUID   NOT NULL,
    activity_date  DATE   NOT NULL,
    cards_reviewed INT    NOT NULL DEFAULT 0,
    xp_earned      BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, activity_date),
    CONSTRAINT fk_daily_activity_to_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

--rollback DROP TABLE IF EXISTS daily_activity CASCADE
