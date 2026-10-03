--liquibase formatted sql

--changeset a.slelin:014-create-table-user-stats

CREATE TABLE user_stats
(
    user_id          UUID PRIMARY KEY,
    xp               BIGINT NOT NULL DEFAULT 0,
    current_streak   INT    NOT NULL DEFAULT 0,
    longest_streak   INT    NOT NULL DEFAULT 0,
    last_active_date DATE,
    daily_goal       INT    NOT NULL DEFAULT 20,
    CONSTRAINT fk_user_stats_to_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

--rollback DROP TABLE IF EXISTS user_stats CASCADE
