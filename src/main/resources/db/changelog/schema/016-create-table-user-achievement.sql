--liquibase formatted sql

--changeset a.slelin:016-create-table-user-achievement

CREATE TABLE user_achievement
(
    user_id     UUID        NOT NULL,
    code        VARCHAR(50) NOT NULL,
    unlocked_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, code),
    CONSTRAINT fk_user_achievement_to_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

--rollback DROP TABLE IF EXISTS user_achievement CASCADE
