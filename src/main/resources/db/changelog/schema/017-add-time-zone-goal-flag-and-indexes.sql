--liquibase formatted sql

--changeset a.slelin:017-add-user-time-zone

-- Days (streak, daily goal, forecast) are counted in the time zone of the user; NULL - application default.
ALTER TABLE user_stats
    ADD COLUMN time_zone VARCHAR(64);

--rollback ALTER TABLE user_stats DROP COLUMN time_zone

--changeset a.slelin:017-add-daily-goal-flag

-- The goal is remembered per day: changing the daily goal later must not rewrite the history.
ALTER TABLE daily_activity
    ADD COLUMN goal_reached BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE daily_activity a
SET goal_reached = TRUE
FROM user_stats s
WHERE s.user_id = a.user_id
  AND a.cards_reviewed >= s.daily_goal;

--rollback ALTER TABLE daily_activity DROP COLUMN goal_reached

--changeset a.slelin:017-add-foreign-key-indexes

-- PostgreSQL does not index foreign keys itself: without these indexes every cascade delete
-- (card, deck, user) scans the child tables completely.
CREATE INDEX idx_card_progress_card ON card_progress (card_id);
CREATE INDEX idx_training_answer_card ON training_answer (card_id);
CREATE INDEX idx_training_session_card_card ON training_session_card (card_id);
CREATE INDEX idx_training_session_deck ON training_session (deck_id);
CREATE INDEX idx_deck_source_deck ON deck (source_deck_id);
CREATE INDEX idx_deck_likes_deck ON deck_likes (deck_id);
CREATE INDEX idx_deck_tag_tag ON deck_tag (tag_id);
CREATE INDEX idx_refresh_token_user ON refresh_token (user_id);

--rollback DROP INDEX IF EXISTS idx_card_progress_card;
--rollback DROP INDEX IF EXISTS idx_training_answer_card;
--rollback DROP INDEX IF EXISTS idx_training_session_card_card;
--rollback DROP INDEX IF EXISTS idx_training_session_deck;
--rollback DROP INDEX IF EXISTS idx_deck_source_deck;
--rollback DROP INDEX IF EXISTS idx_deck_likes_deck;
--rollback DROP INDEX IF EXISTS idx_deck_tag_tag;
--rollback DROP INDEX IF EXISTS idx_refresh_token_user;
