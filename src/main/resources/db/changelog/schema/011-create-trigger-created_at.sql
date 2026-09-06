--liquibase formatted sql

--changeset a.slelin:0011-create-trigger-created_at context:!test

CREATE
    OR REPLACE FUNCTION check_created_at()
    RETURNS TRIGGER AS
'
    BEGIN
        IF TG_OP = ''UPDATE'' THEN
            IF OLD.created_at IS DISTINCT FROM NEW.created_at THEN
                RAISE EXCEPTION ''Cannot modify created_at field in % table.'', TG_TABLE_NAME;
            END IF;
        END IF;

        RETURN NEW;
    END;
' LANGUAGE plpgsql;

CREATE TRIGGER created_at_users_trigger
    BEFORE UPDATE
    ON users
    FOR EACH ROW
EXECUTE FUNCTION check_created_at();

CREATE TRIGGER created_at_deck_trigger
    BEFORE UPDATE
    ON deck
    FOR EACH ROW
EXECUTE FUNCTION check_created_at();

CREATE TRIGGER created_at_card_trigger
    BEFORE UPDATE
    ON card
    FOR EACH ROW
EXECUTE FUNCTION check_created_at();

CREATE TRIGGER created_at_deck_likes_trigger
    BEFORE UPDATE
    ON deck_likes
    FOR EACH ROW
EXECUTE FUNCTION check_created_at();

--rollback DROP TRIGGER IF EXISTS created_at_users_trigger ON users;
--rollback DROP TRIGGER IF EXISTS created_at_deck_trigger ON deck;
--rollback DROP TRIGGER IF EXISTS created_at_card_trigger ON card;
--rollback DROP TRIGGER IF EXISTS created_at_deck_likes_trigger ON deck_likes;
--rollback DROP FUNCTION IF EXISTS check_created_at;